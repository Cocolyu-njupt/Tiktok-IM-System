package com.bytedance.firstDemo.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bytedance.firstDemo.data.db.AppDbHelper;
import com.bytedance.firstDemo.data.user.LoginStateManager;

public class UserRepository {

    private final AppDbHelper AppDbHelper;

    public UserRepository(Context context) {
        this.AppDbHelper = new AppDbHelper(context);
    }

    /**
     * 注册用户
     *
     * @return
     */
    public boolean registerUser(String account, String password, String nickname, String avatar) {
        SQLiteDatabase db = AppDbHelper.getWritableDatabase();
        db.execSQL(
                "INSERT INTO user (account, password, nickname, avatar) VALUES (?, ?, ?, ?)",
                new Object[]{account, password, nickname, avatar}
        );
        return false;
    }

    /** 登录校验 */
    public boolean validateLogin(String account, String password) {
        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM user WHERE account=? AND password=?",
                new String[]{account, password}
        );
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    /** 根据账号获取昵称 —— 保持与旧代码接口一致 */
    public String getNicknameByAccount(String account) {
        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT nickname FROM user WHERE account=?",
                new String[]{account}
        );

        if (c.moveToFirst()) {
            String name = c.getString(0);
            c.close();
            return name;
        }
        c.close();
        return "";
    }

    /** 判断用户是否存在 */
    public boolean userExists(String account) {
        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM user WHERE account=?",
                new String[]{account}
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    /** 获取当前账号昵称 */
    public String getCurrentNickname(Context ctx) {
        String account = LoginStateManager.getCurrentAccount(ctx);

        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT nickname FROM user WHERE account=?",
                new String[]{account}
        );

        if (c.moveToFirst()) {
            String name = c.getString(0);
            c.close();
            return name;
        }
        c.close();
        return "";
    }

    /** 获取当前用户头像 */
    public String getUserAvatar(String account) {
        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT avatar FROM user WHERE account=?",
                new String[]{account}
        );

        if (c.moveToFirst()) {
            String avatar = c.getString(0);
            c.close();
            return avatar;
        }
        c.close();
        return "";
    }

    /** 更新用户头像 */
    public void updateUserAvatar(String account, String avatar) {
        SQLiteDatabase db = AppDbHelper.getWritableDatabase();
        db.execSQL(
                "UPDATE user SET avatar=? WHERE account=?",
                new Object[]{avatar, account}
        );
    }


    /** 旧接口兼容 - 自动从 context 获取账号，再查头像 */
    public String getUserAvatar(Context ctx) {
        String account = LoginStateManager.getCurrentAccount(ctx);
        return getUserAvatar(account);
    }

    /** 旧接口兼容 - 根据 context 获取当前账号信息 */
    public String getCurrentUserInfo(Context ctx) {
        String account = LoginStateManager.getCurrentAccount(ctx);

        SQLiteDatabase db = AppDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT account, nickname, avatar FROM user WHERE account=?",
                new String[]{account}
        );

        if (c.moveToFirst()) {
            String result =
                    "账号: " + c.getString(0) + "\n" +
                            "昵称: " + c.getString(1) + "\n" +
                            "头像: " + c.getString(2);
            c.close();
            return result;
        }
        c.close();
        return "未找到用户信息";
    }

    /** 旧接口兼容：updateUserAvatar(String avatarPath, Context ctx) */
    public void updateUserAvatar(String avatarPath, Context ctx) {
        String account = LoginStateManager.getCurrentAccount(ctx);
        updateUserAvatar(account, avatarPath);
    }



}
