package com.concordia.insha.defender;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.concordia.insha.model.Event;
import com.concordia.insha.model.Process;

public class DefenderDBHelper extends SQLiteOpenHelper {
    private final String TAG = DefenderDBHelper.class.getName();

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "stats.db";

    // process table, collecting process name and timestamp
    private static final String PROCESS_TABLE_NAME = "process";
    private static final String ID = "id";
    private static final String TIMESTAMP = "timestamp";

    private static final String PROCESS_UID = "process_uid";
    private static final String PROCESS_PID = "process_pid";
    private static final String PROCESS_NAME = "process_name";

    private static final String PROCESS_TABLE_CREATE = "CREATE TABLE "
            + PROCESS_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
            + TIMESTAMP + " int," + PROCESS_UID + " TEXT, " + PROCESS_PID
            + " TEXT, " + PROCESS_NAME + " TEXT " + ");";

    // cpu usage table, collecting cpu usage and pid

    // events table, collecting platform events such as screen on/off toggle and
    // app install plus extra metadata depending on event type
    private static final String EVENT_TABLE_NAME = "event";
    private static final String EVENT_TYPE = "type";
    private static final String EVENT_MORE = "more";

    private static final String EVENT_TABLE_CREATE = "CREATE TABLE "
            + EVENT_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
            + TIMESTAMP + " int," + EVENT_TYPE + " int, " + EVENT_MORE
            + " TEXT " + ");";

    private static final String[] tables = new String[] { PROCESS_TABLE_NAME,
            EVENT_TABLE_NAME};

    private DefenderDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PROCESS_TABLE_CREATE);
        db.execSQL(EVENT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will be reconsidered but on upgrade conserve old data just in
        // case...
        for (String tabname : tables) {
            try {
                db.execSQL(String.format("ALTER TABLE %s RENAME TO %s",
                        tabname, tabname + oldVersion));
            } catch (Exception e) {
                // table probably doesnt exist
                continue;
            }
        }

        onCreate(db);
    }

    private static DefenderDBHelper mDBHelper;

    public synchronized static DefenderDBHelper getInstance(Context context) {

        if (mDBHelper == null) {
            mDBHelper = new DefenderDBHelper(context);
        }

        return mDBHelper;
    }

    public boolean insertProcess(Process p) {
        SQLiteDatabase defenderDB = this.getWritableDatabase();

        long insertedID = 0;
        ContentValues values = new ContentValues();

        values.put(TIMESTAMP, p.TimeStamp);
        values.put(PROCESS_PID, p.Pid);
        values.put(PROCESS_NAME, p.Name);
        values.put(PROCESS_UID, p.Uid);

        try {
            defenderDB.beginTransaction();
            insertedID = defenderDB.insert(PROCESS_TABLE_NAME, null, values);
            defenderDB.setTransactionSuccessful();
        } finally {
            defenderDB.endTransaction();
        }

        if (insertedID == -1) {
            return false;
        }

        return true;
    }


    public boolean insertEvent(Event ev) {
        SQLiteDatabase aidsDB = this.getWritableDatabase();

        long insertedID = 0;
        ContentValues values = new ContentValues();

        values.put(TIMESTAMP, ev.TimeStamp);
        values.put(EVENT_TYPE, ev.Type.ordinal());
        values.put(EVENT_MORE, ev.More);

        try {
            aidsDB.beginTransaction();
            insertedID = aidsDB.insert(EVENT_TABLE_NAME, null, values);
            aidsDB.setTransactionSuccessful();
        } finally {
            aidsDB.endTransaction();
        }
        if (insertedID == -1) {
            return false;
        }

        return true;
    }



    public List<Process> getProcesses(long fromTS, long toTS) {
        SQLiteDatabase aidsDB = this.getReadableDatabase();
        ArrayList<Process> pList = new ArrayList<Process>();

        SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(true,
                PROCESS_TABLE_NAME, new String[] { PROCESS_NAME, PROCESS_PID,
                        PROCESS_UID }, TIMESTAMP + " between ? and ?",
                new String[] { String.valueOf(fromTS), String.valueOf(toTS) },
                null, null, null, null);

        if (cursor.getCount() == 0) {
            return pList;
        }

        while (cursor.moveToNext()) {
            Process p = new Process();
            p.Name = cursor.getString(0);
            p.Pid = cursor.getString(1);
            p.Uid = cursor.getString(2);

            pList.add(p);
        }

        return pList;
    }


    public boolean resetAllData() {
        SQLiteDatabase aidsDB = this.getWritableDatabase();

        Log.i(TAG, "Resetting data based on user command");

        for (String tabname : tables) {
            aidsDB.delete(tabname, "1", null);
        }

        return true;
    }
}
