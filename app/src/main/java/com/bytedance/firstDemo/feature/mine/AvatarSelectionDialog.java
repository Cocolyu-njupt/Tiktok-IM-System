package com.bytedance.firstDemo.feature.mine;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.bytedance.firstDemo.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * 头像选择弹窗。
 * 从 assets/avatars/ 目录中动态加载全部头像，并支持用户点击选择。
 */
public class AvatarSelectionDialog extends Dialog {

    private AvatarSelectListener listener;

    /**
     * 构造函数。
     *
     * @param context  上下文
     * @param listener 选择头像后的回调
     */
    public AvatarSelectionDialog(@NonNull Context context, AvatarSelectListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_avatar_selection);

        LinearLayout container = findViewById(R.id.llAvatarContainer);

        // ---- 动态加载 assets/avatars 下的全部头像 ----
        try {
            String[] files = getContext().getAssets().list("avatars");

            if (files != null) {
                for (String fileName : files) {

                    String avatarPath = "avatars/" + fileName; // 完整路径

                    ImageView iv = new ImageView(getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, 120);
                    lp.topMargin = dp2px(8);
                    lp.bottomMargin = dp2px(8);
                    lp.leftMargin = dp2px(8);
                    lp.rightMargin = dp2px(8);
                    lp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
                    iv.setLayoutParams(lp);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iv.setBackgroundResource(R.drawable.default_avatar);

                    // 设置 Bitmap
                    try (InputStream is = getContext().getAssets().open(avatarPath)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        iv.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    iv.setOnClickListener(v -> selectAvatar(avatarPath));

                    container.addView(iv);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== 选择头像相关方法 =====

    /**
     * 点击头像后回调并关闭弹窗。
     */
    private void selectAvatar(String avatarPath) {
        if (listener != null) {
            listener.onAvatarSelected(avatarPath); // 返回 "avatars/xxx.png"
        }
        dismiss();
    }

    /**
     * dp 转 px 工具方法。
     */
    private int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 头像选择监听接口。
     */
    public interface AvatarSelectListener {
        void onAvatarSelected(String avatarPath);
    }
}
