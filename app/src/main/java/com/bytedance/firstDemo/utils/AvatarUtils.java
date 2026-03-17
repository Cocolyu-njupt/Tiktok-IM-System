package com.bytedance.firstDemo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AvatarUtils {

    // 获取 assets 文件夹中所有头像的文件名
    public static List<String> getAvatarFileNames(Context context) {
        List<String> avatarList = new ArrayList<>();
        try {
            AssetManager assetManager = context.getAssets();
            String[] files = assetManager.list("avatars"); // 读取 avatars 文件夹

            if (files != null) {
                for (String file : files) {
                    avatarList.add(file);  // 添加到列表中
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return avatarList;
    }

    // 随机选择一个头像文件
    public static String getRandomAvatar(Context context) {
        List<String> avatars = getAvatarFileNames(context);
        if (avatars.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(avatars.size());
        return avatars.get(randomIndex);  // 返回随机选择的头像文件名
    }

    // 从 assets/avatars 目录加载头像到 ImageView
    public static void loadAvatar(Context context, String avatarFileName, ImageView imageView) {
        if (avatarFileName == null) return;

        try {
            InputStream is = context.getAssets().open("avatars/" + avatarFileName);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
