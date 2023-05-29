package com.washington.chattertrace.data

import java.time.LocalDate
import java.time.LocalTime

/*
 * Data classes
 */
data class RecordingFolder(val id: Int, val date: LocalDate, val duration: LocalTime,
                           val numRecordings: Int, val isUploaded: Boolean)

data class Recording(val id: Int, val bidName: String, val duration: LocalTime);

/*
 * Maps
 */
val recordingMap: HashMap<String, List<Recording>> = hashMapOf();

/*
 * Dummy data setup
 */
val folderList: List<RecordingFolder> = mutableListOf(
    RecordingFolder(0,
        LocalDate.of(2023, 4, 20),
        LocalTime.of(0, 0, 0),
        4,
        true
    ),
    RecordingFolder(1,
        LocalDate.of(2023, 4, 19),
        LocalTime.of(0, 0, 0),
        4,
        true
    ),
    RecordingFolder(2,
        LocalDate.of(2023, 4, 18),
        LocalTime.of(0, 0, 0),
        4,
        false
    ),
    RecordingFolder(3,
        LocalDate.of(2023, 4, 17),
        LocalTime.of(0, 0, 0),
        4,
        false
    ),
    RecordingFolder(4,
        LocalDate.of(2023, 4, 16),
        LocalTime.of(0, 0,0),
        4,
        false
    ),
    RecordingFolder(5,
        LocalDate.of(2023, 4, 15),
        LocalTime.of(0, 0,0),
        4,
        false
    ),
    RecordingFolder(6,
        LocalDate.of(2023, 4, 14),
        LocalTime.of(0, 0,0),
        4,
        false
    ),
    RecordingFolder(7,
        LocalDate.of(2023, 4, 13),
        LocalTime.of(0, 0,0),
        4,
        false
    ),
    RecordingFolder(8,
        LocalDate.of(2023, 4, 12),
        LocalTime.of(0, 0,0),
        4,
        false
    )
);

var dates: List<String> = mutableListOf(
    "4-20", "4-19", "4-18", "4-17", "4-16", "4-15", "4-14", "4-13", "4-12"
)


fun dummyDataSetup() {
    var recordings: MutableList<Recording> = mutableListOf()
    for (i in 0..10) {
        recordings.add(
            Recording(i, "Bid Name", LocalTime.of(0, 0,0))
        )
    }

    for (date in dates) {
        recordingMap[date] = recordings.toList();
    }
}