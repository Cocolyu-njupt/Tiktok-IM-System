package com.bytedance.firstDemo.data.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 系统消息数据模型。
 * 用于存放互动消息（后续可扩展陌生人消息、系统通知等）。
 *
 * 结构说明：
 * - 通用字段：id / type / title / content / time / unread
 * - 扩展字段：extra(JSON)，用于存放不同类型的差异化字段
 *
 * 示例：
 * 互动消息 extra：
 * {
 *     "user": "捞薯条",
 *     "action": "赞了",
 *     "target": "你的评论"
 * }
 */
public class SystemMessageModel {

    // ---- 消息类型（预留可扩展） ----
    public static final int TYPE_INTERACTION = 1;  // 互动消息
    public static final int TYPE_STRANGER = 2;     // 陌生人消息（后续扩展）

    // ---- 通用字段 ----
    public int id;
    public int type;       // 消息类型
    public String title;   // 标题（如“互动消息”）
    public String content; // 展示给用户的主内容（如“捞薯条赞了你的评论”）
    public String time;    // 时间字符串
    public int unread;     // 是否未读（0/1）

    // ---- 扩展字段（存 JSON，不同系统消息的差异字段都放在这里） ----
    public JSONObject extra;

    // ===== 构造方法 =====

    /**
     * 默认构造。
     */
    public SystemMessageModel() {}

    // ===== 工具方法：extra JSON 读写 =====

    /**
     * 向 extra 中写入键值。
     */
    public void putExtra(String key, String value) {
        if (extra == null) extra = new JSONObject();
        try {
            extra.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 extra 中读取字符串。
     *
     * @return 若不存在则返回 null
     */
    public String getExtraString(String key) {
        if (extra == null) return null;
        return extra.optString(key, null);
    }

    /**
     * 设置完整 JSON 文本。
     */
    public void setExtraFromString(String json) {
        if (json == null || json.isEmpty()) {
            extra = new JSONObject();
            return;
        }
        try {
            extra = new JSONObject(json);
        } catch (JSONException e) {
            extra = new JSONObject();
        }
    }

    /**
     * 将 extra 转为字符串，用于存数据库。
     */
    public String getExtraAsString() {
        return extra != null ? extra.toString() : "{}";
    }

    // ===== 判断方法 =====

    /**
     * 是否为互动消息。
     */
    public boolean isInteraction() {
        return type == TYPE_INTERACTION;
    }

    /**
     * 是否为陌生人消息（未来扩展）。
     */
    public boolean isStranger() {
        return type == TYPE_STRANGER;
    }
}
