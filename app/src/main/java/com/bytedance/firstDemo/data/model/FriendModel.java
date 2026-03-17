package com.bytedance.firstDemo.data.model;

/**
 * 好友数据模型：对应 FriendDbHelper 的每一条记录
 */
public class FriendModel {

    public int friendId;
    public String nickname;
    public String remark;
    public String avatar;
    public int unread;
    public String lastTime;
    public String lastMessage;
    public String ownerAccount;

    // ⭐ 新增字段
    public int isPinned;   // 0/1
    public int isHidden;   // 0/1

    public FriendModel() {}

    @Override
    public String toString() {
        return "FriendModel{" +
                "friendId=" + friendId +
                ", nickname='" + nickname + '\'' +
                ", remark='" + remark + '\'' +
                ", avatar='" + avatar + '\'' +
                ", unread=" + unread +
                ", lastTime='" + lastTime + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", ownerAccount='" + ownerAccount + '\'' +
                ", isPinned=" + isPinned +
                ", isHidden=" + isHidden +
                '}';
    }
}