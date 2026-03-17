package com.bytedance.firstDemo.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bytedance.firstDemo.data.db.AppDbHelper;
import com.bytedance.firstDemo.data.model.SystemMessageModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository：负责 system_message 表的增删查改。
 * 用于系统消息（互动消息 / 陌生人消息）的写入、查询、未读管理等。
 */
public class SystemMessageRepository {

    private final Context context;

    /**
     * 构造函数。
     */
    public SystemMessageRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    // ===== 插入系统消息（公共底层方法） =====

    /**
     * 插入一条系统消息（底层统一方法）。
     *
     * @param type    消息类型（INTERACTION / STRANGER）
     * @param title   标题（例如“互动消息”）
     * @param content 展示在 UI 的内容
     * @param time    时间
     * @param unread  是否未读（0/1）
     * @param extra   JSON 扩展字段
     */
    public void insertSystemMessage(
            int type,
            String title,
            String content,
            String time,
            int unread,
            JSONObject extra
    ) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("type", type);
        cv.put("title", title);
        cv.put("content", content);
        cv.put("time", time);
        cv.put("unread", unread);
        cv.put("extra", extra != null ? extra.toString() : "{}");

        db.insert("system_message", null, cv);
    }

    // ===== 互动消息插入（上层封装） =====

    /**
     * 插入一条互动系统消息。
     *
     * @param user            发起者昵称
     * @param action          行为：赞了你 / 评论了你 / 回复了你 / 关注了你 / @了你
     * @param commentContent  评论或回复内容（可为 null）
     * @param time            时间
     */
    public void insertInteractionMessage(
            String user,
            String action,
            String commentContent,
            String time
    ) {

        JSONObject extra = new JSONObject();
        try {
            extra.put("user", user);
            extra.put("action", action);
            if (commentContent != null) {
                extra.put("comment", commentContent);
            }
        } catch (Exception ignore) {}

        // ---- 拼接最终展示内容（重要） ----
        String content;
        if (commentContent == null || commentContent.isEmpty()) {
            // 只有 user + action
            content = user + action;
        } else {
            // user + action + ：+ 评论内容
            content =commentContent;
        }

        insertSystemMessage(
                SystemMessageModel.TYPE_INTERACTION,
                "互动消息",
                content,
                time,
                1,       // 未读
                extra
        );
    }


    // ===== 查询最新系统消息（用于消息列表混排） =====

    /**
     * 查询每个系统消息类型的最新一条消息。
     * 当前仅使用互动消息（type=1），未来可扩展更多类型。
     *
     * @param type 系统消息类型
     * @return 若不存在则返回 null
     */
    public SystemMessageModel getLatestMessage(int type) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM system_message WHERE type=? ORDER BY time DESC LIMIT 1",
                new String[]{String.valueOf(type)}
        );

        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        SystemMessageModel m = cursorToModel(c);
        c.close();
        return m;
    }

    // ===== 查询所有某类型的系统消息（用于详情页） =====

    /**
     * 查询某类系统消息的全部记录。
     */
    public List<SystemMessageModel> getAllMessages(int type) {
        List<SystemMessageModel> list = new ArrayList<>();
        SQLiteDatabase db = AppDbHelper.getInstance(context).getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM system_message WHERE type=? ORDER BY time DESC",
                new String[]{String.valueOf(type)}
        );

        while (c.moveToNext()) {
            list.add(cursorToModel(c));
        }

        c.close();
        return list;
    }

    // ===== 未读数相关方法 =====

    /**
     * 查询某类系统消息的未读数量。
     */
    public int getUnreadCount(int type) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM system_message WHERE type=? AND unread=1",
                new String[]{String.valueOf(type)}
        );

        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    /**
     * 将某类系统消息全部标记为已读。
     */
    public void clearUnread(int type) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();

        db.execSQL(
                "UPDATE system_message SET unread=0 WHERE type=?",
                new Object[]{type}
        );
    }

    // ===== 工具方法：Cursor → Model =====

    /**
     * 将 Cursor 转换为 SystemMessageModel。
     */
    private SystemMessageModel cursorToModel(Cursor c) {
        SystemMessageModel m = new SystemMessageModel();

        m.id = c.getInt(c.getColumnIndexOrThrow("id"));
        m.type = c.getInt(c.getColumnIndexOrThrow("type"));
        m.title = c.getString(c.getColumnIndexOrThrow("title"));
        m.content = c.getString(c.getColumnIndexOrThrow("content"));
        m.time = c.getString(c.getColumnIndexOrThrow("time"));
        m.unread = c.getInt(c.getColumnIndexOrThrow("unread"));

        String extraJson = c.getString(c.getColumnIndexOrThrow("extra"));
        m.setExtraFromString(extraJson);

        return m;
    }


}
