package com.bytedance.firstDemo.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bytedance.firstDemo.data.db.AppDbHelper;
import com.bytedance.firstDemo.data.model.JsonMessage;
import com.bytedance.firstDemo.data.model.MessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository：负责 message 表的增删查操作。
 * 用于消息写入、按会话查询、获取最新会话列表、清空消息等功能。
 */
public class MessageRepository {

    private final AppDbHelper dbHelper;

    /**
     * 构造函数：初始化数据库助手。
     */
    public MessageRepository(Context ctx) {
        dbHelper = new AppDbHelper(ctx);
    }

    // ===== 写入相关方法 =====

    /**
     * 向 message 表写入一条新消息。
     *
     * @param jm JsonMessage 对象（包含 sessionId / sender / content 等字段）
     */
    public void insertMessage(JsonMessage jm) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("sessionId", jm.sessionId);
        cv.put("type", jm.type);
        cv.put("sender", jm.senderName);
        cv.put("content", jm.content);
        cv.put("time", jm.time);
        cv.put("unread", jm.unread);
        cv.put("isMe", jm.isMe ? 1 : 0);

        // ⭐ 消息体裁相关字段
        cv.put("msgType", jm.msgType);                      // 1 文本 2 图片 3 运营
        cv.put("imagePath", jm.imagePath);                  // 图片消息使用（可为 null）
        cv.put("actionText", jm.actionText);                // 运营消息按钮文案
        cv.put("actionPayload", jm.actionPayload);          // 运营消息附加参数

        db.insert("message", null, cv);
    }

    // ===== 按会话查询相关方法 =====


    // 兼容老代码：不需要分页时，直接拿这个
    public List<MessageModel> getMessagesBySession(String sessionId) {
        // 不分页：给一个很大的 limit，offset = 0
        return getMessagesBySession(sessionId, Integer.MAX_VALUE, 0);
    }

    /**
     * 分页查询某个会话的聊天记录（倒序分页，符合 IM 标准）
     *
     * @param sessionId 会话 ID（如 "friend_15"）
     * @param limit     单页条数（如 20）
     * @param offset    偏移量（pageIndex * limit）
     */
    /** 聊天页分页（倒序） */
    public List<MessageModel> getMessagesBySession(String sessionId, int limit, int offset) {
        List<MessageModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT * FROM message WHERE sessionId = ? ORDER BY id DESC LIMIT ? OFFSET ?";

        Cursor c = db.rawQuery(sql, new String[]{
                sessionId,
                String.valueOf(limit),
                String.valueOf(offset)
        });

        if (c != null) {
            while (c.moveToNext()) {
                list.add(MessageModel.fromCursor(c));
            }
            c.close();
        }
        return list;
    }




    // ===== 会话列表（每个会话最新一条）相关方法 =====

    /**
     * 查询所有会话的最新一条消息（用于消息首页）。
     * 会话按最后一条消息时间倒序排列。
     *
     * @return 每个会话的最新消息
     */
    public List<MessageModel> getAllSessionsLastMessage() {
        List<MessageModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT * FROM message " +
                        "WHERE time IN (SELECT MAX(time) FROM message GROUP BY sessionId) " +
                        "ORDER BY time DESC";

        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext()) {
            list.add(MessageModel.fromCursor(c));
        }

        c.close();
        return list;
    }

    // ===== 调试数据读取方法 =====

    /**
     * 获取 message 表的全部消息（调试用）。
     */
    public List<MessageModel> getAllMessages() {
        List<MessageModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM message ORDER BY time DESC", null);

        while (c.moveToNext()) {
            list.add(MessageModel.fromCursor(c));
        }

        c.close();
        return list;
    }

    // ===== 清空数据相关方法 =====

    /**
     * 清空 message 表所有数据。
     */
    public void clearMessages() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM message");
    }


    // ======================= 新增搜索功能 =======================

    /**
     * 搜索所有会话中包含关键字的消息（用于一级搜索页）
     * 返回所有匹配的 MessageModel 列表，由 ViewModel 再按 sessionId 分组
     */
    public List<MessageModel> searchAllMessages(String keyword) {
        List<MessageModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT * FROM message " +
                        "WHERE content LIKE '%' || ? || '%' " +
                        "ORDER BY time DESC";

        Cursor c = db.rawQuery(sql, new String[]{ keyword });

        while (c.moveToNext()) {
            list.add(MessageModel.fromCursor(c));
        }

        c.close();
        return list;
    }

    /**
     * 搜索某个会话中的所有匹配消息（用于二级搜索页）
     */
    public List<MessageModel> searchMessagesInSession(String sessionId, String keyword) {
        List<MessageModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT * FROM message " +
                        "WHERE sessionId = ? " +
                        "AND content LIKE '%' || ? || '%' " +
                        "ORDER BY time DESC";

        Cursor c = db.rawQuery(sql, new String[]{ sessionId, keyword });

        while (c.moveToNext()) {
            list.add(MessageModel.fromCursor(c));
        }

        c.close();
        return list;
    }

// ======================= 搜索部分结束 =======================

}
