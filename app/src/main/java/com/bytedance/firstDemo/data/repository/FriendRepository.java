package com.bytedance.firstDemo.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bytedance.firstDemo.data.db.AppDbHelper;
import com.bytedance.firstDemo.data.model.FriendModel;
import com.bytedance.firstDemo.data.user.LoginStateManager;
import com.bytedance.firstDemo.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository：负责 friend 表的全部操作，包括置顶、隐藏、删除。
 */
public class FriendRepository {

    private final Context context;

    public FriendRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    // 添加好友
    public void addFriend(String nickname) {
        String owner = LoginStateManager.getCurrentAccount(context);
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();

        String avatar = AvatarUtils.getRandomAvatar(context);

        db.execSQL(
                "INSERT INTO friend (nickname, remark, avatar, unread, lastTime, lastMessage, ownerAccount, isPinned, isHidden) " +
                        "VALUES (?, '', ?, 0, '', '', ?, 0, 0)",
                new Object[]{nickname, avatar, owner}
        );
    }

    // 获取全部好友（自动排除隐藏 & 自动按置顶排序）
    // FriendRepository.java
    // 获取全部好友（自动排除隐藏 & 自动按置顶排序）
    public List<FriendModel> getAllFriends() {
        List<FriendModel> list = new ArrayList<>();
        String owner = LoginStateManager.getCurrentAccount(context);

        SQLiteDatabase db = AppDbHelper.getInstance(context).getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM friend WHERE ownerAccount=? AND isHidden=0 ORDER BY isPinned DESC, lastTime DESC", // 按置顶排序
                new String[]{owner}
        );

        while (c.moveToNext()) {
            FriendModel f = new FriendModel();
            f.friendId = c.getInt(c.getColumnIndexOrThrow("friendId"));
            f.nickname = c.getString(c.getColumnIndexOrThrow("nickname"));
            f.remark = c.getString(c.getColumnIndexOrThrow("remark"));
            f.avatar = c.getString(c.getColumnIndexOrThrow("avatar"));
            f.unread = c.getInt(c.getColumnIndexOrThrow("unread"));
            f.lastTime = c.getString(c.getColumnIndexOrThrow("lastTime"));
            f.lastMessage = c.getString(c.getColumnIndexOrThrow("lastMessage"));
            f.ownerAccount = owner;
            f.isPinned = c.getInt(c.getColumnIndexOrThrow("isPinned"));
            f.isHidden = c.getInt(c.getColumnIndexOrThrow("isHidden"));

            list.add(f);
        }

        c.close();
        return list;
    }



    // 根据 ID 获取
    public FriendModel getFriendById(int id) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM friend WHERE friendId=?",
                new String[]{String.valueOf(id)}
        );

        if (c.moveToFirst()) {
            FriendModel f = new FriendModel();
            f.friendId = c.getInt(c.getColumnIndexOrThrow("friendId"));
            f.nickname = c.getString(c.getColumnIndexOrThrow("nickname"));
            f.remark = c.getString(c.getColumnIndexOrThrow("remark"));
            f.avatar = c.getString(c.getColumnIndexOrThrow("avatar"));
            f.unread = c.getInt(c.getColumnIndexOrThrow("unread"));
            f.lastTime = c.getString(c.getColumnIndexOrThrow("lastTime"));
            f.lastMessage = c.getString(c.getColumnIndexOrThrow("lastMessage"));

            f.isPinned = c.getInt(c.getColumnIndexOrThrow("isPinned"));
            f.isHidden = c.getInt(c.getColumnIndexOrThrow("isHidden"));

            c.close();
            return f;
        }

        c.close();
        return null;
    }

    // 更新最后一条消息
    public void updateFriendLastMessage(String nickname, String lastMessage, String lastTime) {
        String owner = LoginStateManager.getCurrentAccount(context);
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("lastMessage", lastMessage);
        values.put("lastTime", lastTime);

        db.update("friend", values, "nickname=? AND ownerAccount=?", new String[]{nickname, owner});
    }

    public void updateRemark(int friendId, String remark) {
        ContentValues values = new ContentValues();
        values.put("remark", remark);

        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();
        db.update("friend", values, "friendId=?", new String[]{String.valueOf(friendId)});
    }

    // 未读逻辑
    public void increaseUnread(int friendId) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();
        db.execSQL("UPDATE friend SET unread = unread + 1 WHERE friendId=?", new Object[]{friendId});
    }

    public void clearUnread(int friendId) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();
        db.execSQL("UPDATE friend SET unread = 0 WHERE friendId=?", new Object[]{friendId});
    }

    // ========================
    // ⭐⭐ 新增功能：置顶 / 隐藏 / 删除
    // ========================

    // 置顶
    // 置顶
    public void pinFriend(int friendId) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();
        db.execSQL("UPDATE friend SET isPinned = 1 WHERE friendId=?", new Object[]{friendId});

    }

    // 取消置顶
    public void unpinFriend(int friendId) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();
        db.execSQL("UPDATE friend SET isPinned = 0 WHERE friendId=?", new Object[]{friendId});
    }


    // 隐藏会话（不删除好友关系）
    public void hideFriend(int friendId) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();
        db.execSQL("UPDATE friend SET isHidden = 1 WHERE friendId=?", new Object[]{friendId});
    }

    // 删除好友 & 删除消息
    public void deleteFriend(int friendId) {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();

        // 删除 message（该好友的所有消息）
        String sessionId = "friend_" + friendId;
        db.execSQL("DELETE FROM message WHERE sessionId=?", new Object[]{sessionId});

        // 删除 friend
        db.execSQL("DELETE FROM friend WHERE friendId=?", new Object[]{friendId});
    }

    // 清空好友表
    public void clearFriends() {
        SQLiteDatabase db = AppDbHelper.getInstance(context).getWritableDatabase();
        db.execSQL("DELETE FROM friend");
    }
}
