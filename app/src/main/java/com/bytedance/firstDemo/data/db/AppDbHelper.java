package com.bytedance.firstDemo.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * App 数据库。
 */
public class AppDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "user_data.db";

    /**
     * 数据库版本号：
     * v6：新增 system_message 表
     * v7：message 增加 msgType
     * v8：friend 表增加 isPinned / isHidden
     * v9：message 表增加 imagePath / actionText / actionPayload
     */
    public static final int DB_VERSION = 9;

    private static AppDbHelper instance;

    public static synchronized AppDbHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new AppDbHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    public AppDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private static final String SQL_CREATE_USER =
            "CREATE TABLE IF NOT EXISTS user (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "account TEXT UNIQUE, " +
                    "password TEXT, " +
                    "nickname TEXT, " +
                    "avatar TEXT)";

    private static final String SQL_CREATE_FRIEND =
            "CREATE TABLE IF NOT EXISTS friend (" +
                    "friendId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nickname TEXT, " +
                    "remark TEXT, " +
                    "avatar TEXT, " +
                    "unread INTEGER DEFAULT 0, " +
                    "lastTime TEXT, " +
                    "lastMessage TEXT, " +
                    "ownerAccount TEXT, " +
                    "isPinned INTEGER DEFAULT 0, " +
                    "isHidden INTEGER DEFAULT 0" +
                    ")";

    private static final String SQL_CREATE_MESSAGE =
            "CREATE TABLE IF NOT EXISTS message (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sessionId TEXT, " +
                    "type INTEGER, " +
                    "sender TEXT, " +
                    "content TEXT, " +
                    "time TEXT, " +
                    "unread INTEGER, " +
                    "isMe INTEGER, " +
                    "imagePath TEXT, " +          // v9 新增：图片消息资源
                    "actionText TEXT, " +         // v9 新增：运营消息按钮文案
                    "actionPayload TEXT, " +      // v9 新增：运营消息附加参数
                    "msgType INTEGER DEFAULT 1" + // 消息类型：1 文本 2 图片 3 运营
                    ")";

    private static final String SQL_CREATE_SYSTEM_MESSAGE =
            "CREATE TABLE IF NOT EXISTS system_message (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "type INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "time TEXT NOT NULL, " +
                    "unread INTEGER DEFAULT 1, " +
                    "extra TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_FRIEND);
        db.execSQL(SQL_CREATE_MESSAGE);
        db.execSQL(SQL_CREATE_SYSTEM_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN msgType INTEGER DEFAULT 1");
            } catch (Exception ignored) {}
        }

        // v8 增加 isPinned / isHidden
        if (oldVersion < 8) {
            try {
                db.execSQL("ALTER TABLE friend ADD COLUMN isPinned INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE friend ADD COLUMN isHidden INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }

        // ⭐ v9：给 message 增加 imagePath / actionText / actionPayload
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN imagePath TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN actionText TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN actionPayload TEXT");
            } catch (Exception ignored) {}
        }
    }
}
