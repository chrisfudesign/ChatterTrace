package com.washington.chattertrace.DataLogic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Display;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.washington.chattertrace.Helper;
import com.washington.chattertrace.service.SuspendwindowService;
import com.washington.chattertrace.utils.Utils;
import com.washington.chattertrace.utils.ViewModleMain;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.tensorflow.lite.task.audio.classifier.Classifications;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A singleton class for managing local recorded data
 * Function: save/delete recording, provide time naming of the file
 *           Manage uploading, Buffer upload files, Manager preceding files
 * Created by mingrui on 7/16/2018.
 */

public class DataManager {
    private static final DataManager instance = new DataManager();

    private boolean autoUpload = false; // whether the recording should be auto uploaded or manually uploaded

    private SpeechRecognizer speech = null;
    private int bufferSize = 0; // buffer file or not; if buffered, the file will be delayed to upload after the buffer size is reached
    private int maxFilesBeforeDelete = 0; // 0 - never delete; > 0 - delete the old files if more than the number of recording exists
    private String folderName = null; // the folder name of the recorded files

    // file list arrays
    private ArrayList<RecordItem> mFolderFileList; // mFolderFileList stores all recording files in the folder
    //we need the lists to be thread-safe
    private List<String> mFileUploading = Collections.synchronizedList(new ArrayList<String>()); // the uploading file list
    private List<String> mFileBuffer = Collections.synchronizedList(new ArrayList<String>()); // the uploading buffer

    // if the clip is not kept, we store them in this buffer
    private List<RecordItem> mShouldNotKeepBuffer = new ArrayList<RecordItem>(); // only useful in preceding mode

    // permanent storage. DB is for file information, Preferences is for uploading buffer
    private RecordItemDAO recordItemDAO;
    private Context context;
    private SharedPreferences preferences;

    private Handler mHandler = new Handler();

    private float DISPLAY_THRESHOLD = 0.3f;
    private int DEFAULT_NUM_OF_RESULTS = 2;
    private float DEFAULT_OVERLAP_VALUE = 0.5f;
    private String YAMNET_MODEL = "yamnet.tflite";
    private String SPEECH_COMMAND_MODEL = "speech.tflite";

    private boolean shouldRecognizeSpeechForAnchor = true;

    private String currentlyProcessingAudioFile = "";

    private int numOfSegmentForAudio = 5;

    private AudioClassifier classifier;
    private TensorAudio tensorAudio;
//    private ScheduledThreadPoolExecutor executor;

//    private Runnable classifyRunnable = new Runnable() {
//        @Override
//        public void run() {
//            classifyAudio();
//        }
//    };

    private void initClassifier() {
        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder()
                .setNumThreads(2);

        AudioClassifier.AudioClassifierOptions options = AudioClassifier.AudioClassifierOptions.builder()
                .setScoreThreshold(DISPLAY_THRESHOLD)
                .setMaxResults(DEFAULT_NUM_OF_RESULTS)
                .setBaseOptions(baseOptionsBuilder.build())
                .build();
        try {
            classifier = AudioClassifier.createFromFileAndOptions(context, YAMNET_MODEL, options);
            tensorAudio = classifier.createInputTensorAudio();
        } catch (IOException ignored) {

        }

    }

    public boolean classifyAudio() {
        Log.d("SCREENWAKE", "start classify audio");
        if (!Objects.equals(currentlyProcessingAudioFile, "")) {
            float[] processedAudio = decodeWavToFloatArray(currentlyProcessingAudioFile);
            assert processedAudio != null;
            if (processedAudio.length < numOfSegmentForAudio) {
                return false;
            }
            int segmentedAudioLength = processedAudio.length / numOfSegmentForAudio;
            float[][] segmentedAudio = new float[numOfSegmentForAudio][segmentedAudioLength];
            String[] segmentedPrediction = new String[numOfSegmentForAudio];

            for (int i = 0; i < numOfSegmentForAudio; i++) {
                segmentedAudio[i] = Arrays.copyOfRange(processedAudio, i * segmentedAudioLength, i * segmentedAudioLength + segmentedAudioLength);
                tensorAudio.load(segmentedAudio[i]);
                List<Classifications> output = classifier.classify(tensorAudio);
                if(output != null && output.get(0) != null && output.get(0).getCategories().size() >0 &&
                        output.get(0).getCategories().get(0) != null){
                    try {
                        segmentedPrediction[i] = output.get(0).getCategories().get(0).getLabel();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (int i = 0; i < numOfSegmentForAudio - 1; i++) {
                System.out.println(segmentedPrediction[i]);
                Log.d("SCREENWAKE", "type of prediction: " + segmentedPrediction[i]);
                if (Objects.equals(segmentedPrediction[i], "Speech") ||
                        Objects.equals(segmentedPrediction[i], "Roaring cats (lions, tigers)") ||
                        Objects.equals(segmentedPrediction[i], "Music")||
                        Objects.equals(segmentedPrediction[i], "Breathing")||
                        Objects.equals(segmentedPrediction[i], "Snoring")) {
                    Log.d("SCREENWAKE", "found speech");
                    // Trigger notification
                    return true;
                }
            }
        }
        return false;
    }

    public static float[] decodeWavToFloatArray(String filePath) {
        try {
            System.out.println(filePath);
            FileInputStream fileInputStream = new FileInputStream(filePath);
            byte[] header = new byte[44]; // WAV file header is 44 bytes

            // Read and skip the header bytes
            int bytesRead = fileInputStream.read(header);
            if (bytesRead != 44) {
                throw new IOException("Invalid WAV file format");
            }

            // Get audio format information from the header
            int numChannels = header[22];
            int bitsPerSample = header[34] & 0xFF | (header[35] & 0xFF) << 8;
            int sampleRate = header[24] & 0xFF | (header[25] & 0xFF) << 8 | (header[26] & 0xFF) << 16 | (header[27] & 0xFF) << 24;

            // Check audio format compatibility (16-bit, PCM)
            if (bitsPerSample != 16) {
                throw new IOException("Unsupported WAV file format. Only 16-bit PCM is supported.");
            }

            // Calculate the number of audio samples
            int dataSize = (int) (fileInputStream.getChannel().size() - 44);
            int numSamples = dataSize / 2; // 16-bit = 2 bytes per sample

            if(numSamples <= 0){
                throw new IOException("Cannot read emtpy WAV file");
            }

            // Read audio data into a short array
            float[] audioData = new float[numSamples];
            byte[] buffer = new byte[2];
            for (int i = 0; i < numSamples; i++) {
                fileInputStream.read(buffer);
                audioData[i] = ( (buffer[0] & 0xFF) | (buffer[1] << 8));
            }

            // Convert short array to float array and normalize values to range [-1.0, 1.0]
            float[] floatArray = new float[numSamples];
            for (int i = 0; i < numSamples; i++) {
                floatArray[i] = audioData[i] / 32768.0f;
            }
            

//            int first = 0;
//            for (int i = 0; i < numSamples; i++) {
//                if (floatArray[i] > 0.0) {
//                    first = i;
//                }
//            }
//
//            int last = numSamples + 10;
//            for (int i = 0; i < numSamples; i++) {
//                if (floatArray[numSamples - i - 1] > 0.0) {
//                    last = i;
//                }
//            }
//
//            System.out.println(first);
//            System.out.println(last);

            fileInputStream.close();

            return floatArray;
        } catch (IOException e) {
            Log.e("WavDecoder", "Error decoding WAV file: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * When the manager is instantiated for the first time
     * call the Initialize at first after set the folder name
     * The function initializes the database and retrieves the recording lists in the database
     * Also load the file buffer
     */
    public void Initialize(Context context) {
        this.context = context;

        RecordItemRoomDatabase db = RecordItemRoomDatabase.getDatabase(context);
        recordItemDAO = db.recordItemDAO();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mFolderFileList = new ArrayList<RecordItem>(recordItemDAO.getAllRecordings());

                // Synchronize the state of the database with the actual files on disk
                for (int i = mFolderFileList.size()-1; i >= 0; --i){
                    RecordItem item = mFolderFileList.get(i);
                    final File record = new File(item.path);
                    if (!record.exists() || !item.should_keep) {
                        mFolderFileList.remove(i);
                        new deleteAsyncTask(recordItemDAO).execute(item);
                        //and delete those who should not exist
                        if (record.exists()){
                            record.delete();
                        }
                    }
                }
            }
        });
        loadBuffer();

        initClassifier();
    }

    // singleton
    private DataManager(){}

    /**
     * Get the instance of the manager. It is singleton class, thus there would only be one instance of the class through the whole application
     * Use this function to retrieve the manager
     */
    public static DataManager getInstance() { return instance; }

    // setters

    /**
     * How many files should be kept in the local storage
     * @param maxFilesBeforeDelete the amount of local recording files should be kept
     */
    public void setMaxFilesBeforeDelete(int maxFilesBeforeDelete) {
        this.maxFilesBeforeDelete = maxFilesBeforeDelete;
    }

    /**
     * Whether the recordings should be uploaded automatically
     */
    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    /**
     * Set the folder name where the recording clips should be stored
     * @param folderName
     */
    public void setFolderName2(String folderName) throws IOException{
        System.out.println(Environment.getExternalStorageDirectory());
        this.folderName = Environment.getExternalStorageDirectory() +
                File.separator + folderName;
        System.out.println(this.folderName);
        File folder = new File(this.folderName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (!success) {
            this.folderName = null;
            throw new IOException("Create Folder Failed - Permission Denied");
        }
    }

    public void setFolderName(String folderName) throws IOException {
        String directoryType = Environment.DIRECTORY_DOCUMENTS;
        File directory = this.context.getExternalFilesDir(directoryType);
        String directoryName = folderName;

        if (directory != null) {
            File subDirectory = new File(directory, directoryName);
            if (!subDirectory.exists()) {
                if (subDirectory.mkdirs()) {
                    // Directory creation successful
                    System.out.println("Created folder successful");
                } else {
                    // Directory creation failed
                    throw new IOException("Failed to create directory");
                }
            } else {
                // Directory already exists
                this.folderName = null;
                System.out.println("ALREADY EXISTS");
            }
            this.folderName = subDirectory.getAbsolutePath();

        } else {
            // External storage is not available or accessible
            this.folderName = null;
            throw new IOException("External storage not accessible");
        }
    }

    /**
     * Set the buffer size of the uploading function. The buffer would only be effective when auto-upload is enabled
     * @param bufferSize how large (how many files would be stored before uploading) of the buffer
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    // getters

    /**
     * Get the recording item at position of the mFolderFileList
     * @param pos
     */
    public RecordItem getItemAtPos(int pos) {
        if (mFolderFileList.size() > pos && pos >= 0){
            return mFolderFileList.get(pos);
        }
        return null;
    }

    /**
     * Get how many recording items in mFolderFileList (in the local storage folder)
     */
    public int getItemCount() {
        return mFolderFileList.size();
    }

    // buffer

    /**
     * Store the buffer information in permanent storage
     */
    public void storeBuffer() {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String buffer_json = gson.toJson(mFileBuffer);
        editor.putString("bufferList", buffer_json);
        editor.apply();
    }

    /**
     * Load the buffer information from permanent storage
     */
    private void loadBuffer() {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String buffer_json = preferences.getString("bufferList", null);
        if (buffer_json != null) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            mFileBuffer = gson.fromJson(buffer_json, type);
            mFileBuffer = Collections.synchronizedList(mFileBuffer);
        }

        // check if every file in bufferlist exists
        for (int i = mFileBuffer.size()-1; i >= 0; --i){
            File f = new File(mFileBuffer.get(i));
            if (!f.exists())
                mFileBuffer.remove(i);
        }
    }

    /**
     * The buffer is full. Upload the first file in the buffer
     */
    private void uploadBuffer() {
        if (!autoUpload || !Helper.CheckNetworkConnected(context)) return;
        String filename;
        synchronized (mFileBuffer) {
            final String fname = mFileBuffer.remove(0);
            filename = fname;
        }
        uploadFile(filename);
    }

    // uploading
    /**
     * Upload the file through fileuploader (here is the amazon uploader)
     */
    public void uploadFile(String fname) {
        if (!Helper.CheckNetworkConnected(context) || mFileUploading.contains(fname)) return;
        if (bufferSize > 0){
            synchronized (mFileUploading) {
                mFileUploading.add(fname);
            }
        }
        String[] tokens = fname.split("/");
        System.out.println("START UPLOADING HERE TO BE IMPLEMENTED");
//        DataUploader.AmazonAWSUploader(context, fname, "public/" + tokens[tokens.length-1]);

    }

    /**
     * Get notified when upload file is finished
     */
    public void OnUploadFinished(String filename) {
        // when uploading finished, upload the next buffer if buffersize is enough
        if (bufferSize > 0) {
            synchronized (mFileUploading) {
                mFileUploading.remove(filename);
            }
            if (mFileBuffer.size() > bufferSize)
                uploadBuffer();
        }
        // update the item information
        RecordItem item = findItemByPath(filename);
        if (item != null) {
            item.uploaded = true;
            new updateAsyncTask(recordItemDAO).execute(item);
        }
    }

    public void OnUploadError(String filename) {
        if (bufferSize > 0) {
            // if upload error, we add the file to buffer again
            synchronized (mFileUploading) {
                mFileUploading.remove(filename);
            }
            synchronized (mFileBuffer) {
                mFileBuffer.add(0, filename);
            }
            if (!Helper.CheckNetworkConnected(context)) return;
            if (mFileBuffer.size() > bufferSize)
                uploadBuffer();
        } else {
            if (!Helper.CheckNetworkConnected(context)) return;
            // upload again
            uploadFile(filename);
        }
    }

    // fileNames
    /**
     * Get the file name based on current name
     */
    public String getRecordingNameOfTime(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        timeStamp += ".wav";
        return folderName + File.separator + timeStamp;
    }

    /**
     * Get the file name based on current name, with custom prefix
     * @param prefix
     */
    public String getRecordingNameOfTimeWithPrefix(String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        timeStamp = prefix + timeStamp + ".wav";
        return folderName + File.separator + timeStamp;
    }

    // new recording added, clean old files
    /**
     * Get called when new recording finishes
     * Create the RecordItem instance for new recording
     * Clean old local files if maxFilesBeforeDelete > 0, upload files if auto_upload is on
     * If the file should not be kept, it will be stored in mShouldNotKeepBuffer and be deleted later
     * If the file should be kept, it will be stored in the mFolderFileList
     * @param filename the file name of the new recording
     * @param createdate the time of the recording started
     * @param duration the recording duration
     * @param shouldkeep whether the recording should be kept
     * @param preceding_mode if preceding mode is on and the file shouldkeep is true,
     *                       it will also keep most recent two files in mShouldNotKeepBuffer, and move them to mFolderFileList
     *                       Because they are the preceding recordings of the formal recording file
     * @param merge_with_preceding  if shouldkeep is true, whether the current file should merge with the preceding file into one file.
     */
    public void newRecordingAdded(String filename, String createdate, int duration, boolean shouldkeep, boolean preceding_mode, boolean merge_with_preceding) {
        RecordItem newitem = new RecordItem();
        newitem.path = filename;
        String[] tokens = filename.split("/");
        newitem.filename = tokens[tokens.length-1];
        newitem.createDate = createdate;
        newitem.duration = duration;
        newitem.uploaded = false;
        newitem.should_keep = shouldkeep;

        Boolean displayOn = false;
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        for (Display display : dm.getDisplays()) {
            Log.d("SCREENWAKE", "display silent: " + display.getState());
            if (display.getState()  == Display.STATE_ON) {
                displayOn = true;
            }
        }

        if(!displayOn){
            Log.d("SCREENWAKE", "display off, return");
            return;
        }

        if (shouldkeep) {
            if (preceding_mode){
                // if in preceding mode and the shouldkeep is true, it means the recording file is triggered intentionally
                // rather than the background recording. Thus we should store its preceding two clips
                int bfsize = mShouldNotKeepBuffer.size();

                //if should merge files
                if (merge_with_preceding){
                    // we set bfsize - 2 because we want preceding two file clips, as only one preceding might not be long enough
                    float mtime = 0;
                    ArrayList<String> mergelist = new ArrayList<String>();
                    for (int i = bfsize - 1; i >= Math.max(0, bfsize - 2); --i) {
                        RecordItem item = mShouldNotKeepBuffer.remove(i);
                        mtime += item.duration;
                        new deleteAsyncTask(recordItemDAO).execute(item);
                        mergelist.add(item.path);
                    }
                    newitem.duration += mtime;
                    mergelist.add(filename);
                    MergeThread mtd = new MergeThread(mergelist.toArray(new String[0]));
                    mtd.start();
                }
                //else we save them one by one
                else {
                    // we set bfsize - 2 because we want preceding two file clips, as only one preceding might not be long enough
                    for (int i = bfsize - 1; i >= Math.max(0, bfsize - 2); --i) {
                        RecordItem item = mShouldNotKeepBuffer.remove(i);
                        item.should_keep = true;
                        mFolderFileList.add(0, item);
                        new updateAsyncTask(recordItemDAO).execute(item);
                        if (autoUpload) {
                            if (bufferSize == 0) {
                                uploadFile(item.path);
                            } else {
                                synchronized (mFileBuffer) {
                                    mFileBuffer.add(item.path);
                                }
                            }
                        }
                    }
                }
            }

            mFolderFileList.add(0, newitem);
            new insertAsyncTask(recordItemDAO).execute(newitem);
        } else {

            // if should_keep is false, then it is temporary background clips for preceding files
            // we store them in the shouldnotkeepbuffer, and when the buffer is full, we delete the first file
            mShouldNotKeepBuffer.add(newitem);
            new insertAsyncTask(recordItemDAO).execute(newitem);

            if (mShouldNotKeepBuffer.size() > 3) {
                RecordItem item = mShouldNotKeepBuffer.remove(0);
                new deleteAsyncTask(recordItemDAO).execute(item);
                File file = new File(item.path);
                file.delete();
            }
            currentlyProcessingAudioFile = newitem.path;
            boolean classificationResult = classifyAudio();
            Log.d("SCREENWAKE", "classification result: " + classificationResult);

            if (classificationResult) {
                NotificationHelper.showNotification(context, "", "It seems that your family member just made a bid for connection, please click this to record a response.");
                // GET THE LOGIC OF RECORDING 30s+1min CORRECT
                // Learn how to implement broadcast
                if(Boolean.FALSE.equals(ViewModleMain.isShowSuspendWindow.getValue())){
                    Utils.showBubblewithTimeout(context);
                }
            }
        }
        deleteFilesOutOfMaxFiles();
        processUpload(filename, shouldkeep);
    }

//    public void pushNotification() {
//        NotificationHelper.showNotification(context, "Possible bid for connection?", "It seems that your family member just made a bid for connection, please click this to record a response.");
//    }
//

    private void processUpload(String filename, boolean shouldkeep) {
        if (autoUpload && shouldkeep) {
            // if no buffer, upload new files
            if (bufferSize == 0) {
                //upload
                uploadFile(filename);
            } else {
                synchronized (mFileBuffer) {
                    mFileBuffer.add(filename);
                    storeBuffer();
                }
                if (mFileBuffer.size() > bufferSize)
                    uploadBuffer();
            }
        }
    }

    // local file operations

    /**
     * Delete old local files if current local files is more than the maxFilesBeforeDelete parameter
     */
    private void deleteFilesOutOfMaxFiles() {
        if (folderName != null){
            if (maxFilesBeforeDelete <= 0) return;
            int size = mFolderFileList.size();
            if (size > maxFilesBeforeDelete){
                for (int i = size-1; i >= maxFilesBeforeDelete; --i){
                    String fname = mFolderFileList.get(i).path;
                    // if the file is in the buffer, we should wait until it's uploaded
                    if ( !(mFileBuffer.contains(fname) || mFileUploading.contains(fname)) ) {
                        deleteFile(mFolderFileList.get(i));
                    }
                }
            }
        }
    }

    /**
     * Delete a local file
     */
    public void deleteFile(RecordItem item) {
        File file = new File(item.path);
        file.delete();

        // remove from every list
        mFolderFileList.remove(item);
        new deleteAsyncTask(recordItemDAO).execute(item);
        synchronized (mFileBuffer) {
            mFileBuffer.remove(item.path);
        }
        synchronized (mFileUploading) {
            mFileUploading.remove(item.path);
        }
    }

    /**
     * Return the item with a certain path
     * @param fname the path of the item
     */
    private RecordItem findItemByPath(String fname) {
        for(RecordItem item : mFolderFileList) {
            if(item.path == fname) {
                return item;
            }
        }
        return null;
    }

    // DB Operations
    /**
     * Async class for inserting the item in DB
     */
    private static class insertAsyncTask extends AsyncTask<RecordItem, Void, Void> {

        private RecordItemDAO mAsyncTaskDao;

        insertAsyncTask(RecordItemDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final RecordItem... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    /**
     * Async class for deleting the item in DB
     */
    private static class deleteAsyncTask extends AsyncTask<RecordItem, Void, Void> {

        private RecordItemDAO mAsyncTaskDao;

        deleteAsyncTask(RecordItemDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final RecordItem... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    /**
     * Async class for updating the item in DB
     */
    private static class updateAsyncTask extends AsyncTask<RecordItem, Void, Void> {

        private RecordItemDAO mAsyncTaskDao;

        updateAsyncTask(RecordItemDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final RecordItem... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private class MergeThread extends Thread {
        String[] fnames;
        String outpath;
        public MergeThread(String[] fnames){
            this.fnames = fnames;
            outpath = fnames[0].replace(".wav", "-merge.wav");
        }

        public void run() {
            AudioMediaOperations.MergeAudios(fnames, outpath, new AudioMediaOperations.OperationCallbacks() {
                @Override
                public void onAudioOperationFinished() {
                    //delete fnames
//                    Log.e("[Log]", "onAudioOperationFinished: Merge Finished!" + fnames[fnames.length-1]);
                    for (String s: fnames){
                        File f = new File(s);
                        f.delete();
                    }
                    File from = new File(outpath);
                    File to = new File(fnames[fnames.length-1]);
                    from.renameTo(to);
                    deleteFilesOutOfMaxFiles();
                    processUpload(fnames[fnames.length-1], true);
                }

                @Override
                public void onAudioOperationError(Exception e) {
//                    Log.e("[Log]", "onAudioOperationFinished: Merge Failed...!");
                    for (String s: fnames){
                        File f = new File(s);
                        f.delete();
                    }
                }
            });

        }
    }
}
