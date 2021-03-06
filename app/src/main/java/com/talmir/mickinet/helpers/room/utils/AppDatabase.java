package com.talmir.mickinet.helpers.room.utils;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.talmir.mickinet.helpers.room.received.IReceivedFilesDao;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;
import com.talmir.mickinet.helpers.room.sent.ISentFilesDao;
import com.talmir.mickinet.helpers.room.sent.SentFilesEntity;

/**
 * This is the backend. The database. This used to be done by the OpenHelper.
 * The fact that this has very few comments emphasizes its coolness.
 */
@Database(entities = {SentFilesEntity.class, ReceivedFilesEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract IReceivedFilesDao mReceivedFilesDao();
    public abstract ISentFilesDao mSentFilesDao();

    private static AppDatabase DATABASE_INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (DATABASE_INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (DATABASE_INSTANCE == null) {
                    DATABASE_INSTANCE = Room
                            .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "statistics")
                            .build();
                }
            }
        }
        return DATABASE_INSTANCE;
    }

    public static void destroyInstance() {
        DATABASE_INSTANCE = null;
    }
}
