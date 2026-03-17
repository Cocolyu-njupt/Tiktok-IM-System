package com.bytedance.firstDemo.feature.search;

import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.model.MessageModel;
import com.bytedance.firstDemo.data.model.SearchConversationResult;
import com.bytedance.firstDemo.utils.AvatarUtils;
import com.bytedance.firstDemo.utils.HighlightHelper;

import java.util.List;

public class SearchConversationAdapter
        extends RecyclerView.Adapter<SearchConversationAdapter.VH> {

    private List<SearchConversationResult> list;
    private OnItemClick listener;

    public SearchConversationAdapter(List<SearchConversationResult> list,
                                     OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    public interface OnItemClick {
        void click(SearchConversationResult item);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        SearchConversationResult item = list.get(pos);

        AvatarUtils.loadAvatar(h.itemView.getContext(), item.avatar, h.ivAvatar);
        h.tvName.setText(item.name);
        h.tvCount.setText(item.matchCount + " 条相关聊天记录");

        h.itemView.setOnClickListener(v -> listener.click(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvCount;

        VH(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvName = v.findViewById(R.id.tvName);
            tvCount = v.findViewById(R.id.tvCount);
        }
    }
    public void updateList(List<SearchConversationResult> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

}

