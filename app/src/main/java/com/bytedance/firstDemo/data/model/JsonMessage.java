package com.bytedance.firstDemo.data.model;

public class JsonMessage {

    public int type;
    public int id;
    public String sessionId;
    // 好友
    public String friendName;
    public String friendAvatar;
    public boolean online;
    public String sender;
    public int msgType = 1;  // 默认文本消息

    // 群聊
    public String groupName;
    public String groupAvatar;
    public String senderName;

    // 通用
    public String content;
    public String time;
    public int unread;

    /** 图片消息使用：展示图片的路径（本地 assets 或固定文件名） */
    public String imagePath;

    /** 运营消息使用：按钮文案，例如“领取奖励” */
    public String actionText;

    /** 运营消息使用：附加参数，例如活动ID或跳转协议 */
    public String actionPayload;

    // 额外：判断是否我发的、是否已读
    public boolean isMe;
    public boolean isRead;

}
