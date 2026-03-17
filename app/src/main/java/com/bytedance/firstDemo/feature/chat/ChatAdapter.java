package com.bytedance.firstDemo.feature.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.model.MessageModel;
import com.bytedance.firstDemo.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天页适配器：支持
 * - 左文字消息
 * - 右文字消息
 * - 左图片消息
 * - 右图片消息
 * - 左运营卡片消息
 * - 右运营卡片消息
 * - 时间提示
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ====== ViewType 常量 ======
    private static final int TYPE_TIME         = 0;
    private static final int TYPE_TEXT_LEFT    = 1;
    private static final int TYPE_TEXT_RIGHT   = 2;
    private static final int TYPE_IMAGE_LEFT   = 3;
    private static final int TYPE_IMAGE_RIGHT  = 4;
    private static final int TYPE_OP_LEFT      = 5;
    private static final int TYPE_OP_RIGHT     = 6;

    private List<MessageModel> list = new ArrayList<>();
    private final String myAvatar;
    private final String friendAvatar;

    private final LayoutInflater inflater;

    public ChatAdapter(Context ctx, String myAvatar, String friendAvatar) {
        this.inflater = LayoutInflater.from(ctx);
        this.myAvatar = myAvatar;
        this.friendAvatar = friendAvatar;
    }

    /**
     * 更新整个消息列表
     */
    public void updateList(List<MessageModel> newList) {
        if (newList == null) {
            this.list = new ArrayList<>();
        } else {
            this.list = newList;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel m = list.get(position);

        // 时间行：showTime = true 且 content == null
        if (m.showTime && m.content == null) {
            return TYPE_TIME;
        }

        // 根据 msgType + isMe 判断具体类型
        int msgType = m.getMsgType();
        boolean isMe = m.isMe;

        if (msgType == 2) { // 图片
            return isMe ? TYPE_IMAGE_RIGHT : TYPE_IMAGE_LEFT;
        } else if (msgType == 3) { // 运营卡片
            return isMe ? TYPE_OP_RIGHT : TYPE_OP_LEFT;
        } else {
            // 默认：文本
            return isMe ? TYPE_TEXT_RIGHT : TYPE_TEXT_LEFT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        if (viewType == TYPE_TIME) {
            View v = inflater.inflate(R.layout.item_chat_time, parent, false);
            return new TimeHolder(v);
        }

        if (viewType == TYPE_TEXT_RIGHT) {
            View v = inflater.inflate(R.layout.item_chat_right, parent, false);
            return new TextRightHolder(v);
        }

        if (viewType == TYPE_TEXT_LEFT) {
            View v = inflater.inflate(R.layout.item_chat_left, parent, false);
            return new TextLeftHolder(v);
        }

        if (viewType == TYPE_IMAGE_RIGHT) {
            View v = inflater.inflate(R.layout.item_chat_image_right, parent, false);
            return new ImageRightHolder(v);
        }

        if (viewType == TYPE_IMAGE_LEFT) {
            View v = inflater.inflate(R.layout.item_chat_image_left, parent, false);
            return new ImageLeftHolder(v);
        }

        if (viewType == TYPE_OP_RIGHT) {
            View v = inflater.inflate(R.layout.item_chat_operate_right, parent, false);
            return new OpRightHolder(v);
        }

        // 默认走左侧运营卡片
        View v = inflater.inflate(R.layout.item_chat_operate_left, parent, false);
        return new OpLeftHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        MessageModel m = list.get(position);
        Context ctx = holder.itemView.getContext();

        if (holder instanceof TimeHolder) {
            TimeHolder h = (TimeHolder) holder;
            h.tvTime.setText(m.timeText);
            return;
        }

        if (holder instanceof TextRightHolder) {
            TextRightHolder h = (TextRightHolder) holder;
            h.tvContent.setText(m.content);
            AvatarUtils.loadAvatar(ctx, myAvatar, h.ivAvatar);
            return;
        }

        if (holder instanceof TextLeftHolder) {
            TextLeftHolder h = (TextLeftHolder) holder;
            h.tvContent.setText(m.content);
            AvatarUtils.loadAvatar(ctx, friendAvatar, h.ivAvatar);
            return;
        }

        if (holder instanceof ImageRightHolder) {
            ImageRightHolder h = (ImageRightHolder) holder;
            // 简单展示一张占位图，你可以后续改成根据 m.getImagePath() 加载
            h.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
            AvatarUtils.loadAvatar(ctx, myAvatar, h.ivAvatar);
            return;
        }

        if (holder instanceof ImageLeftHolder) {
            ImageLeftHolder h = (ImageLeftHolder) holder;
            h.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
            AvatarUtils.loadAvatar(ctx, friendAvatar, h.ivAvatar);
            return;
        }

        if (holder instanceof OpRightHolder) {
            OpRightHolder h = (OpRightHolder) holder;
            bindOperateCard(ctx, h.tvTitle, h.tvContent, h.btnAction, m, true);
            AvatarUtils.loadAvatar(ctx, myAvatar, h.ivAvatar);
            return;
        }

        if (holder instanceof OpLeftHolder) {
            OpLeftHolder h = (OpLeftHolder) holder;
            bindOperateCard(ctx, h.tvTitle, h.tvContent, h.btnAction, m, false);
            AvatarUtils.loadAvatar(ctx, friendAvatar, h.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ====== 助手：绑定运营卡片 ======
    private void bindOperateCard(
            Context ctx,
            TextView tvTitle,
            TextView tvContent,
            Button btnAction,
            MessageModel m,
            boolean isMe
    ) {
        // 标题：你可以改为根据 isMe 或其他逻辑区分
        tvTitle.setText(isMe ? "我发起的活动" : "活动通知");
        tvContent.setText(m.content != null ? m.content : "");

        String btnText = m.getActionText();
        if (btnText == null || btnText.isEmpty()) {
            btnText = "查看详情";
        }
        btnAction.setText(btnText);

        btnAction.setOnClickListener(v -> {
            String msg = "点击运营消息：" + (m.content != null ? m.content : "");
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        });
    }

    // ================== Holder 定义 ==================

    /** 时间提示 ViewHolder */
    static class TimeHolder extends RecyclerView.ViewHolder {
        TextView tvTime;

        TimeHolder(View v) {
            super(v);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }

    /** 左侧文本消息 ViewHolder */
    static class TextLeftHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        ImageView ivAvatar;

        TextLeftHolder(View v) {
            super(v);
            tvContent = v.findViewById(R.id.tvContent);
            ivAvatar = v.findViewById(R.id.ivAvatarLeft);
        }
    }

    /** 右侧文本消息 ViewHolder */
    static class TextRightHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        ImageView ivAvatar;

        TextRightHolder(View v) {
            super(v);
            tvContent = v.findViewById(R.id.tvContent);
            ivAvatar = v.findViewById(R.id.ivAvatarRight);
        }
    }

    /** 左侧图片消息 ViewHolder */
    static class ImageLeftHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivAvatar;

        ImageLeftHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivImage);
            ivAvatar = v.findViewById(R.id.ivAvatarLeft);
        }
    }

    /** 右侧图片消息 ViewHolder */
    static class ImageRightHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivAvatar;

        ImageRightHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivImage);
            ivAvatar = v.findViewById(R.id.ivAvatarRight);
        }
    }

    /** 左侧运营卡片 ViewHolder */
    static class OpLeftHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        Button btnAction;
        ImageView ivAvatar;

        OpLeftHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            btnAction = v.findViewById(R.id.btnAction);
            ivAvatar = v.findViewById(R.id.ivAvatarLeft);
        }
    }

    /** 右侧运营卡片 ViewHolder */
    static class OpRightHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        Button btnAction;
        ImageView ivAvatar;

        OpRightHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            btnAction = v.findViewById(R.id.btnAction);
            ivAvatar = v.findViewById(R.id.ivAvatarRight);
        }
    }
}
