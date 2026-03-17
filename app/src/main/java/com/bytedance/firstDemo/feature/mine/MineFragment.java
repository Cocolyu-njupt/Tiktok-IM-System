package com.bytedance.firstDemo.feature.mine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.repository.UserRepository;
import com.bytedance.firstDemo.data.user.LoginStateManager;
import com.bytedance.firstDemo.feature.login.LoginActivity;
import com.bytedance.firstDemo.feature.dashboard.DashboardActivity; // ⬅️ 新增 DashboardActivity 引入

import java.io.IOException;
import java.io.InputStream;

/**
 * “我的”页面 Fragment。
 * 展示用户头像、昵称、功能列表，并支持头像修改与退出登录。
 */
public class MineFragment extends Fragment {

    // 原有 item 文案（新增：数据看板）
    private final String[] itemLabels = {
            "个人信息", "我的收藏", "浏览历史",
            "设置", "关于我们", "意见反馈",
            "数据看板"           // ⬅️ 新增项（第 7 个）
    };

    // 原有 icon（新增一个图标，你可以换成更合适的）
    private final int[] itemIcons = {
            R.drawable.ic_profile,
            R.drawable.ic_star,
            R.drawable.ic_feedback,
            R.drawable.ic_history,
            R.drawable.ic_settings,
            R.drawable.ic_about,
            R.drawable.ic_analysis
    };

    private ImageView ivAvatar;
    private UserRepository repo;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        repo = new UserRepository(requireContext());

        // ---- 昵称 ----
        TextView nickname = view.findViewById(R.id.tvNickname);
        nickname.setText(LoginStateManager.getCurrentNickname(requireContext()));

        ivAvatar = view.findViewById(R.id.ivUserAvatar);

        // ---- 加载当前用户头像 ----
        String avatarPath = repo.getUserAvatar(requireContext());
        loadAvatarIntoView(avatarPath);

        // ---- 信息列表容器 ----
        LinearLayout containerItems = view.findViewById(R.id.ll_items_container);

        // ⚠️ fragment_mine.xml 中有 6 个 <include>，现在新增 1 个 item，所以你需要把 include 数量改成 7 个
        // 为避免你的 XML 未同步改动，这里多做一层保护：以 itemLabels 数量为准
        int childCount = containerItems.getChildCount();
        int requiredCount = itemLabels.length;

        if (childCount < requiredCount) {
            // 动态补齐 item（不会影响原功能）
            for (int i = childCount; i < requiredCount; i++) {
                View itemView = inflater.inflate(R.layout.item_setting, containerItems, false);
                containerItems.addView(itemView);
            }
        }

        // ---- 设置每个 item 的文字 + 图标 + 点击事件 ----
        for (int i = 0; i < itemLabels.length; i++) {
            View itemView = containerItems.getChildAt(i);
            TextView tvLabel = itemView.findViewById(R.id.tvItemLabel);
            ImageView ivIcon = itemView.findViewById(R.id.ivItemIcon);

            tvLabel.setText(itemLabels[i]);
            ivIcon.setImageResource(itemIcons[i]);

            int index = i;
            itemView.setOnClickListener(v -> handleItemClick(index));
        }

        // ---- 点击头像 ----
        ivAvatar.setOnClickListener(v -> {
            AvatarSelectionDialog dialog =
                    new AvatarSelectionDialog(requireContext(), selectedPath -> {

                        repo.updateUserAvatar(selectedPath, requireContext());
                        loadAvatarIntoView(selectedPath);
                        Toast.makeText(requireContext(), "头像已更新！", Toast.LENGTH_SHORT).show();
                    });

            dialog.show();
        });

        // ---- 退出登录 ----
        Button logoutButton = view.findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> {
            LoginStateManager.setLoggedIn(requireContext(), false);
            Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        // ---- 查看当前账号数据库信息 ----
        Button btnShowDb = view.findViewById(R.id.btnShowDatabaseInfo);
        btnShowDb.setOnClickListener(v -> {
            String info = repo.getCurrentUserInfo(requireContext());
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("数据库信息")
                    .setMessage(info)
                    .setPositiveButton("关闭", null)
                    .show();
        });

        return view;
    }

    // =============== 新增：点击事件分发（新增 Dashboard 跳转） ===============
    private void handleItemClick(int index) {
        String label = itemLabels[index];

        switch (label) {
            case "数据看板":
                // 跳转 DashboardActivity
                startActivity(new Intent(requireContext(), DashboardActivity.class));
                return;

            default:
                // 原有默认行为
                Toast.makeText(requireContext(), "点击了 " + label, Toast.LENGTH_SHORT).show();
        }
    }

    // ===== 加载头像 =====
    private void loadAvatarIntoView(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            ivAvatar.setImageResource(R.drawable.default_avatar);
            return;
        }

        try (InputStream is = requireContext().getAssets().open(avatarPath)) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ivAvatar.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            ivAvatar.setImageResource(R.drawable.default_avatar);
        }
    }
}

