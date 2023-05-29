package com.washington.chattertrace.Data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

/**
 * The RoomDatabase of the recordings
 */
@Database(entities = {RecordItem.class}, version = 1)
public abstract class RecordItemRoomDatabase extends RoomDatabase {
    public abstract RecordItemDAO recordItemDAO();
    private static RecordItemRoomDatabase INSTANCE;

    public static RecordItemRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RecordItemRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RecordItemRoomDatabase.class, "recorditem_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
