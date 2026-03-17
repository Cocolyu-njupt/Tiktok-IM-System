package com.bytedance.firstDemo.data.model;

public class SearchConversationResult {

    /** 对应 message.sessionId，例如 friend_3 / group_xx */
    public String sessionId;

    /** 会话名称（备注 > 昵称 > 群名） */
    public String name;

    /** 头像路径（好友头像 / 群头像） */
    public String avatar;

    /** 匹配条数（xx 条相关聊天记录） */
    public int matchCount;

    /** 最新一条匹配消息（MessageModel 会自带 id/content/time/isMe 等） */
    public MessageModel latestMatch;

    public SearchConversationResult(String sessionId,
                                    String name,
                                    String avatar,
                                    int matchCount,
                                    MessageModel latestMatch) {
        this.sessionId = sessionId;
        this.name = name;
        this.avatar = avatar;
        this.matchCount = matchCount;
        this.latestMatch = latestMatch;
    }
}
