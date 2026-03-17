package com.bytedance.firstDemo.data.model;

import android.database.Cursor;

import org.json.JSONObject;

/**
 * 数据模型：用于展示会话首页的一条会话消息，以及聊天页的单条消息。
 *
 * 支持三类会话：
 * - 私聊好友（TYPE_FRIEND）
 * - 群聊消息（TYPE_GROUP）
 * - 系统消息（TYPE_SYSTEM：互动消息 / 陌生人消息）
 *
 * 消息内容类型（msgType）：
 * - 1：纯文本
 * - 2：图片
 * - 3：运营卡片（带按钮）
 */
public class MessageModel {

    // ===== 会话类型 =====
    public static final int TYPE_FRIEND = 1;      // 好友私聊
    public static final int TYPE_GROUP  = 2;      // 群聊
    public static final int TYPE_SYSTEM = 3;      // 系统消息（互动消息等）

    // ---- 好友消息字段 ----
    private String friendName;
    private String friendContent;
    private String friendAvatar;
    private int friendAvatarResId;
    private boolean friendOnline;

    public boolean showTime;
    public String timeText;   // 显示给 UI 的时间文本（如：昨天 16:20）

    public boolean isPinned;

    // ---- 群聊消息字段 ----
    private String groupName;
    private String groupSender;
    private String groupContent;
    private int groupAvatarResId;

    // ---- 系统消息字段 ----
    private String systemTitle;        // 例如 “互动消息”
    private String systemContent;      // 例如 “捞薯条赞了你的评论”
    private int systemIconResId;       // 不同类型对应不同图标（互动/陌生人）
    private JSONObject systemExtra;    // 扩展字段（互动消息结构存这里）

    // ---- 通用字段 ----
    private int type;
    private String time;

    // ==== 好友/群聊消息 DB 字段（来自 message 表） ====
    public int id;
    public String sessionId;

    public String sender;
    public String content;
    public int unread;
    public boolean isMe;

    // ===== 消息内容类型 & 扩展字段 =====
    public int msgType = 1;      // 1 文本；2 图片；3 运营消息
    public String avatarPath;    // 聊天列表中使用的头像路径

    public String imagePath;     // 图片消息使用
    public String actionText;    // 运营消息按钮文字
    public String actionPayload; // 运营消息附加参数（如活动 id）

    // ===== Setter（对外保留） =====

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(int type) {
        this.type = type;
    }

    // ───────────────────────────────────────────────
    // 从 message 表读取（好友/群聊消息）
    // ───────────────────────────────────────────────

    /**
     * 将 SQLite Cursor 解析为 MessageModel（用于好友消息/群聊消息）。
     */
    public static MessageModel fromCursor(Cursor c) {
        MessageModel m = new MessageModel();

        m.id = c.getInt(c.getColumnIndexOrThrow("id"));
        m.sessionId = c.getString(c.getColumnIndexOrThrow("sessionId"));
        m.type = c.getInt(c.getColumnIndexOrThrow("type"));
        m.sender = c.getString(c.getColumnIndexOrThrow("sender"));
        m.content = c.getString(c.getColumnIndexOrThrow("content"));
        m.time = c.getString(c.getColumnIndexOrThrow("time"));
        m.unread = c.getInt(c.getColumnIndexOrThrow("unread"));
        m.isMe = c.getInt(c.getColumnIndexOrThrow("isMe")) == 1;

        int idxMsgType = c.getColumnIndex("msgType");
        m.msgType = idxMsgType >= 0 ? c.getInt(idxMsgType) : 1;

        int idxImage = c.getColumnIndex("imagePath");
        m.imagePath = (idxImage >= 0) ? c.getString(idxImage) : null;

        int idxActionText = c.getColumnIndex("actionText");
        m.actionText = (idxActionText >= 0) ? c.getString(idxActionText) : null;

        int idxActionPayload = c.getColumnIndex("actionPayload");
        m.actionPayload = (idxActionPayload >= 0) ? c.getString(idxActionPayload) : null;

        if (m.type == TYPE_FRIEND) {
            m.friendName = m.sessionId;
            m.friendContent = m.content;

        } else if (m.type == TYPE_GROUP) {
            m.groupName = m.sessionId;
            m.groupSender = m.sender;
            m.groupContent = m.content;
        }

        return m;
    }

    // ───────────────────────────────────────────────
    // 从系统消息表读取（INTERACTION / STRANGER 等）
    // ───────────────────────────────────────────────

    /**
     * 创建一个系统消息类型的 MessageModel，用于会话列表混排。
     *
     * @param model 系统消息 SystemMessageModel
     * @param icon  系统消息的图标资源
     */
    public static MessageModel fromSystem(SystemMessageModel model, int icon) {
        MessageModel m = new MessageModel();

        m.type = TYPE_SYSTEM;
        m.time = model.time;
        m.unread = model.unread;

        m.systemTitle = model.title;         // 如 “互动消息”
        m.systemContent = model.content;     // 如 “捞薯条赞了你的评论”
        m.systemIconResId = icon;            // UI 根据类型指定图标
        m.systemExtra = model.extra;         // 存互动消息结构（user/action/target）

        // 系统消息默认当作文本展示
        m.msgType = 1;
        return m;
    }



    // ===== Getter（适配器依赖这些方法） =====

    public int getType() { return type; }

    // ---- 好友消息 ----
    public String getFriendName() { return friendName; }
    public String getFriendContent() { return friendContent; }
    public String getFriendAvatar() { return friendAvatar; }
    public int getFriendAvatarResId() { return friendAvatarResId; }
    public boolean isFriendOnline() { return friendOnline; }

    // ---- 群聊消息 ----
    public String getGroupName() { return groupName; }
    public String getGroupSender() { return groupSender; }
    public String getGroupContent() { return groupContent; }
    public int getGroupAvatarResId() { return groupAvatarResId; }

    // ---- 系统消息 ----
    public String getSystemTitle() { return systemTitle; }
    public String getSystemContent() { return systemContent; }
    public int getSystemIconResId() { return systemIconResId; }
    public JSONObject getSystemExtra() { return systemExtra; }

    // ---- 通用 ----
    public String getTime() { return time; }

    // ---- 消息体裁相关 ----
    public int getMsgType() { return msgType; }
    public String getImagePath() { return imagePath; }
    public String getActionText() { return actionText; }
    public String getActionPayload() { return actionPayload; }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

}
