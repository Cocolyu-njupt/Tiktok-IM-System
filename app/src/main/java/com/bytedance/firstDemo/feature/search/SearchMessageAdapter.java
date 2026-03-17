package com.bytedance.firstDemo.feature.search;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.model.FriendModel;
import com.bytedance.firstDemo.data.model.SearchMessageResult;
import com.bytedance.firstDemo.data.repository.FriendRepository;
import com.bytedance.firstDemo.utils.AvatarUtils;
import com.bytedance.firstDemo.utils.HighlightHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchMessageAdapter
        extends RecyclerView.Adapter<SearchMessageAdapter.VH> {

    private List<SearchMessageResult> list = new ArrayList<>();
    private int friendId;
    private String keyword;
    private OnMsgClick listener;

    public interface OnMsgClick {
        void click(int messageId);
    }

    public SearchMessageAdapter(int friendId, String keyword,
                                OnMsgClick listener) {
        this.friendId = friendId;
        this.keyword = keyword;
        this.listener = listener;
    }

    public void updateList(List<SearchMessageResult> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        SearchMessageResult m = list.get(pos);

        // 高亮文本
        SpannableString span = new SpannableString(m.content);
        int idx = m.content.indexOf(keyword);
        if (idx >= 0) {
            span.setSpan(new ForegroundColorSpan(Color.YELLOW),
                    idx, idx + keyword.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        h.tvContent.setText(span);

        h.tvTime.setText(m.time);

        // 头像
        FriendRepository repo = new FriendRepository(h.itemView.getContext());
        FriendModel fm = repo.getFriendById(friendId);
        AvatarUtils.loadAvatar(h.itemView.getContext(), fm.avatar, h.ivAvatar);

        // 点击事件
        h.itemView.setOnClickListener(v -> listener.click(m.messageId));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvContent, tvTime;

        VH(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvContent = v.findViewById(R.id.tvContent);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
    public void updateList(List<SearchMessageResult> newList, String keyword) {
        this.list = newList;
        this.keyword = keyword;
        notifyDataSetChanged();
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

}

