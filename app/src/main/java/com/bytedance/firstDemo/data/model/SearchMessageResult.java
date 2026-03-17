package com.bytedance.firstDemo.data.model;

public class SearchMessageResult {

    /** 匹配到的消息 id，用于跳转聊天页精准定位 */
    public int messageId;

    /** 消息内容（需要在 Adapter 中做关键字高亮处理） */
    public String content;

    /** 消息时间（yyyy-MM-dd HH:mm:ss） */
    public String time;

    /** 是否我发送的（用于区分左右气泡样式） */
    public boolean isMe;

    public SearchMessageResult(int messageId, String content, String time, boolean isMe) {
        this.messageId = messageId;
        this.content = content;
        this.time = time;
        this.isMe = isMe;
    }
}
