package com.bytedance.firstDemo.feature.message;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.model.MessageModel;
import com.bytedance.firstDemo.utils.TimeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * RecyclerView 适配器：展示消息会话列表（好友 / 群聊 / 系统消息）。
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FRIEND = MessageModel.TYPE_FRIEND;
    private static final int VIEW_TYPE_GROUP = MessageModel.TYPE_GROUP;
    private static final int VIEW_TYPE_SYSTEM = MessageModel.TYPE_SYSTEM; // ⭐ 系统消息类型

    private final List<MessageModel> data;

    // ===== 点击事件相关方法 =====

    public interface OnItemClickListener {
        void onClick(MessageModel model);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    public interface OnItemLongClickListener {
        void onLongClick(MessageModel model);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.longClickListener = l;
    }

    /**
     * 构造函数。
     */
    public MessageAdapter(List<MessageModel> data) {
        this.data = data;
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // ---- 好友会话 ----
        if (viewType == VIEW_TYPE_FRIEND) {
            View view = inflater.inflate(R.layout.item_friend_message, parent, false);
            return new FriendViewHolder(view);

            // ---- 群聊会话 ----
        } else if (viewType == VIEW_TYPE_GROUP) {
            View view = inflater.inflate(R.layout.item_group_message, parent, false);
            return new GroupViewHolder(view);

            // ---- 系统消息会话 ----
        } else {
            View view = inflater.inflate(R.layout.item_system_message, parent, false);
            return new SystemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        MessageModel model = data.get(position);
        Log.d("MessageAdapter", "isPinned: " + model.isPinned());

        // ===== 好友会话 =====
        if (holder instanceof FriendViewHolder) {
            FriendViewHolder h = (FriendViewHolder) holder;

            loadAvatar(h.ivAvatar, model.getFriendAvatar(), h.itemView.getContext());
            h.tvName.setText(model.getFriendName());
            h.tvContent.setText(model.getFriendContent());
            h.tvTime.setText(TimeUtils.formatTime(model.getTime()));
            h.ivOnlineDot.setVisibility(model.isFriendOnline() ? View.VISIBLE : View.GONE);
            h.tvUnread.setVisibility(model.unread > 0 ? View.VISIBLE : View.GONE);
            if (model.isPinned()) {
                Log.d("MessageAdapter", "Applying gray background for pinned message");
                h.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray_background));
            } else {
                h.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.default_message_background));
            }

            if (model.unread > 0) {
                h.tvUnread.setVisibility(View.VISIBLE);
                if (model.unread > 99) {
                    h.tvUnread.setText("99+");
                } else {
                    h.tvUnread.setText(String.valueOf(model.unread));
                }
            } else {
                h.tvUnread.setVisibility(View.GONE);
            }


            // ===== 群聊会话 =====
        } else if (holder instanceof GroupViewHolder) {
            GroupViewHolder h = (GroupViewHolder) holder;
            h.ivAvatar.setImageResource(model.getGroupAvatarResId());
            h.tvGroupName.setText(model.getGroupName());
            h.tvSenderAndContent.setText(model.getGroupSender() + "：" + model.getGroupContent());
            h.tvTime.setText(TimeUtils.formatTime(model.getTime()));

            // ===== 系统消息会话（新逻辑） =====
        } else if (holder instanceof SystemViewHolder) {
            SystemViewHolder h = (SystemViewHolder) holder;

            // 图标
            h.ivIcon.setImageResource(model.getSystemIconResId());

            // 标题，例如 “互动消息”
            h.tvTitle.setText(model.getSystemTitle());

            // 内容，例如 “捞薯条赞了你的评论”
            h.tvContent.setText(model.getSystemContent());

            // 时间
            h.tvTime.setText(TimeUtils.formatTime(model.getTime()));

            // 未读红点
            if (model.unread > 0) {
                h.ivUnreadDot.setVisibility(View.VISIBLE);
            } else {
                h.ivUnreadDot.setVisibility(View.GONE);
            }
        }

        // ---- 点击事件 ----
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(model);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onLongClick(model);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    // ===== ViewHolder（好友会话） =====

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        ImageView ivOnlineDot;
        TextView tvName;
        TextView tvContent;
        TextView tvTime;
        TextView tvUnread;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivOnlineDot = itemView.findViewById(R.id.ivOnlineDot);
            tvName = itemView.findViewById(R.id.tvFriendName);
            tvContent = itemView.findViewById(R.id.tvFriendContent);
            tvTime = itemView.findViewById(R.id.tvFriendTime);
            tvUnread = itemView.findViewById(R.id.tvFriendUnread);
        }
    }

    // ===== ViewHolder（群聊会话） =====

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvGroupName;
        TextView tvSenderAndContent;
        TextView tvTime;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivGroupAvatar);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvSenderAndContent = itemView.findViewById(R.id.tvGroupSenderAndContent);
            tvTime = itemView.findViewById(R.id.tvGroupTime);
        }
    }

    // ===== ViewHolder（系统消息） =====

    /**
     * 系统消息列表项 ViewHolder（互动/陌生人消息）。
     */
    static class SystemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        ImageView ivUnreadDot;

        SystemViewHolder(@NonNull View itemView) {
            super(itemView);

            ivIcon = itemView.findViewById(R.id.ivSystemIcon);
            tvTitle = itemView.findViewById(R.id.tvSystemTitle);
            tvContent = itemView.findViewById(R.id.tvSystemContent);
            tvTime = itemView.findViewById(R.id.tvSystemTime);
            ivUnreadDot = itemView.findViewById(R.id.ivSystemUnread); // ⭐ 需要你在 XML 中给未读点加一个 id
        }
    }

    // ===== 列表刷新与分页方法 =====

    public void updateList(List<MessageModel> newList) {
        data.clear();
        data.addAll(newList);
        notifyDataSetChanged();
    }


    // ===== 工具方法 =====

    private void loadAvatar(ImageView iv, String avatarPath, Context ctx) {
        if (avatarPath != null && !avatarPath.isEmpty()) {
            try (InputStream in = ctx.getAssets().open("avatars/" + avatarPath)) {
                iv.setImageDrawable(Drawable.createFromStream(in, null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void appendList(List<MessageModel> more) {
        int start = data.size();
        data.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

}
