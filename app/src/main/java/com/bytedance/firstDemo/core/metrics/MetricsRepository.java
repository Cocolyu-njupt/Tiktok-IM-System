package com.bytedance.firstDemo.core.metrics;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class MetricsRepository extends SQLiteOpenHelper {

    private static final String DB_NAME = "metrics.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "metric_events";

    public MetricsRepository(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "eventName TEXT," +
                "page TEXT," +
                "userId TEXT," +
                "timestamp INTEGER," +
                "params TEXT" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 若未来需要升级，可在此迁移
    }

    /** 插入事件 */
    public void insertEvent(MetricEvent e) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("eventName", e.eventName);
        cv.put("page", e.page);
        cv.put("userId", e.userId);
        cv.put("timestamp", e.timestamp);
        cv.put("params", e.params);
        db.insert(TABLE, null, cv);
    }

    /** 查询全部事件（用于 Dashboard） */
    public List<MetricEvent> getAllEvents() {
        List<MetricEvent> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY timestamp DESC", null);
        while (c.moveToNext()) {
            MetricEvent e = new MetricEvent();
            e.id = c.getInt(c.getColumnIndexOrThrow("id"));
            e.eventName = c.getString(c.getColumnIndexOrThrow("eventName"));
            e.page = c.getString(c.getColumnIndexOrThrow("page"));
            e.userId = c.getString(c.getColumnIndexOrThrow("userId"));
            e.timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"));
            e.params = c.getString(c.getColumnIndexOrThrow("params"));
            list.add(e);
        }
        c.close();
        return list;
    }
}
