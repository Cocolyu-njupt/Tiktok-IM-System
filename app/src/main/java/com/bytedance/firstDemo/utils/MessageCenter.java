// ======================== MessageCenter.java ========================
package com.bytedance.firstDemo.utils;

import android.os.Handler;
import android.os.Looper;

import com.bytedance.firstDemo.data.model.FriendModel;
import com.bytedance.firstDemo.data.model.JsonMessage;
import com.bytedance.firstDemo.data.repository.FriendRepository;
import com.bytedance.firstDemo.feature.message.MessageViewModel;

import java.util.List;
import java.util.Random;

public class MessageCenter {

    private static MessageCenter instance;
    private Handler handler;
    private Runnable task;
    private final Random random = new Random();

    private MessageCenter() {
        handler = new Handler(Looper.getMainLooper());
    }

    public static MessageCenter getInstance() {
        if (instance == null) instance = new MessageCenter();
        return instance;
    }

    /** 每 5 秒推送一条随机类型消息 */
    public void start(MessageViewModel vm, FriendRepository friendRepo) {

        stop();

        task = new Runnable() {
            @Override
            public void run() {

                List<FriendModel> friends = friendRepo.getAllFriends();
                if (friends.isEmpty()) {
                    handler.postDelayed(this, 5000);
                    return;
                }

                FriendModel f = friends.get(random.nextInt(friends.size()));

                int type = random.nextInt(3); // 0 文本、1 图片、2 运营卡
                int num = random.nextInt(9999);

                if (type == 0) {
                    vm.addFriendMessage(null, f.friendId, f.nickname, false, "自动文本消息 " + num);
                }
                else if (type == 1) {
                    JsonMessage m = new JsonMessage();
                    m.sessionId = "friend_" + f.friendId;
                    m.type = 1;
                    m.msgType = 2; // 图片
                    m.senderName = f.nickname;
                    m.content = "[图片]";
                    m.time = TimeUtils.now();
                    m.isMe = false;
                    vm.insertExternalMessage(m); // 需要你在 VM 加一个 insertExternalMessage() 方法
                }
                else {
                    JsonMessage m = new JsonMessage();
                    m.sessionId = "friend_" + f.friendId;
                    m.type = 1;
                    m.msgType = 3; // 运营卡片
                    m.senderName = f.nickname;
                    m.content = "自动运营消息 " + num;
                    m.actionText = "查看详情";
                    m.time = TimeUtils.now();
                    m.isMe = false;
                    vm.insertExternalMessage(m);
                }

                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(task, 5000);
    }

    public void stop() {
        if (handler != null && task != null) {
            handler.removeCallbacks(task);
            task = null;
        }
    }
}
