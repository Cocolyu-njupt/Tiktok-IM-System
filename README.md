# 抖音增长客户端方向 — IM & Dashboard

本项目实现了一个简化版的抖音消息体系，包括：消息列表、好友会话、聊天界面、置顶与备注、搜索、动态消息中心、弱网模拟、行为埋点与可视化数据看板等功能。

采用 **MVVM + Repository + SQLite** 架构，以本地模拟服务端，实现持久化、可扩展的消息系统。

---

## 📂 目录（Table of Contents）

- [项目结构 (Architecture Overview)](#项目结构)
- [核心功能 (Features)](#核心功能-features)
  - [消息列表页](#消息列表页)
  - [聊天页面](#聊天页面)
  - [好友备注](#好友备注)
  - [消息搜索](#消息搜索)
  - [弱网体验](#弱网体验)
  - [动态消息中心](#动态消息中心)
  - [数据看板](#数据看板)
- [技术栈](#技术栈)
- [数据库设计与迁移方案](#数据库设计与迁移方案)
- [遇到的问题与解决方案](#遇到的问题与解决方案)
- [运行方式](#运行方式)

---

## 项目结构

项目按照功能模块划分，以 MVVM + Repository 层级组织：



```text
app/
 ├── core/
 │     ├── db/                # SQLite（初始化、DAO、迁移）
 │     ├── metrics/           # 埋点事件、数据聚合、分析工具
 │     ├── network/           # Retrofit（天气模块）
 │     └── utils/             # 工具类（时间、JSON、高亮）
 │
 ├── data/
 │     ├── model/             # 数据模型
 │     ├── repository/        # 数据访问层
 │     └── user/              # 登录态维护
 │
 ├── feature/
 │     ├── message/           # 消息列表、置顶、隐藏、弱网、搜索入口
 │     ├── chat/              # 聊天页（文本/图片/运营卡片）
 │     ├── dashboard/         # 数据看板（三页结构 + 图表）
 │     ├── search/            # 搜索（命中高亮、定位会话）
 │     ├── weather/           # 天气示例模块
 │     ├── login/             # 登录注册
 │     └── mine/              # 我的页面（数据看板入口）
 │
 ├── utils/                   # 工具函数（时间、头像、消息中心、高亮等）
 │
 └── layout/                  # 弹窗组件、自定义视图

```
---

## 核心功能 (Features)

### 消息列表页
- 好友消息 + 系统消息混排  
- 展示头像、昵称/备注、摘要、时间文案、未读  
- 下拉刷新、上滑加载更多（模拟分页）  
- 系统消息自动插入  
- 支持置顶、取消置顶、删除、隐藏  
- 根据备注动态更新 UI  

---

### 聊天页面
- 文本消息、图片消息、运营消息三类布局  
- 自动切换左右气泡  
- 自动滚动到底部  
- 输入框适配  
- 时间分段条自动插入  

---

### 好友备注
- 长按消息进入备注页  
- 可编辑备注并写入 SQLite  
- 冷启动自动恢复  

---

### 消息搜索
- 按昵称、备注、内容搜索  
- 关键词高亮  
- 结果按会话分组  
- 可跳转定位到具体消息区间  

---

### 弱网体验
- 首刷随机模拟弱网  
- 加载 → 失败 → 重试流程  
- Skeleton 占位 UI  

---

### 动态消息中心
- 每 5 秒自动插入消息  
- 写入 SQLite → UI 自动刷新  
- 未读数实时更新  
- 新消息自动滚动至顶部  

---

### 数据看板
- 展示曝光、进入、点击、停留等多维指标  
- 原始数据 → 二级指标 → 图表展示三页结构  
- MPAndroidChart（饼图/折线图/柱状图）  
- MetricsAnalyzer 聚合行为数据  

---

## 技术栈

### 架构层
- **MVVM**：ViewModel 管理状态，View 只渲染 UI  
- **Repository**：统一封装 SQLite / JSON / 网络  
- **模块化特性分层**：按业务拆分 message/chat/search/dashboard 等模块  

### 数据层
- **SQLite 本地存储**：消息、好友、系统消息、埋点、用户  
- **分页（LIMIT/OFFSET）与搜索（LIKE）**  
- **结构化模型化数据**：MessageModel / FriendModel / MetricEvent  
- **数据源融合**：assets 初始化 + SQLite 持久化  

### UI 层
- **RecyclerView 多类型布局**：消息/搜索/天气/看板  
- **自定义弹窗**：备注、会话操作、头像选择  
- **ViewPager2**：用于看板多页结构  

### 系统层
- **Handler 定时任务**：模拟服务端推送消息  
- **Fragment 生命周期驱动逻辑**：停留时长、弱网、刷新  
- **MetricsCenter 本地埋点系统**：行为事件 + 指标聚合  

### 网络层
- **Retrofit + Gson**：全国天气示例模块  
- Repository 层转换网络响应状态  

### 工具层
- TimeUtils：聊天时间格式化  
- HighlightHelper：搜索关键词高亮  
- AvatarUtils：头像加载与随机生成  

---

## 数据库设计与迁移方案

本项目基于 SQLite 构建完整消息系统、用户体系、埋点体系的数据持久化结构。所有关键业务数据均采用本地数据库存储，以便实现冷启动恢复、历史消息加载、搜索、行为统计等功能。

---

## 1. 数据库结构概览

项目包含以下核心数据表：

| 表名 | 描述 |
|------|------|
| **messages** | 聊天记录（文本 / 图片 / 运营卡片 / 时间提示） |
| **system_messages** | 系统推送消息（例如互动提醒） |
| **friends** | 好友详情（备注、头像、未读、置顶、隐藏） |
| **user** | 登录态与用户信息 |
| **metrics** | 埋点事件（曝光、点击、进入、停留时长等） |

---

## 2. 各表字段结构

以下为所有表的字段与功能说明。

---

### **2.1 messages（聊天记录表）**

用于存储会话内的所有消息，包括时间提示项。

| 字段名 | 类型 | 说明 |
|--------|--------|------------------------------------------------|
| id | INTEGER PRIMARY KEY AUTOINCREMENT | 唯一主键 |
| sessionId | TEXT | 会话 ID，格式：`friend_{id}` |
| senderId | INTEGER | 发送者用户 ID |
| type | INTEGER | 消息类型（0 文本 / 1 图片 / 2 运营卡片 / 3 时间提示） |
| content | TEXT | 文本消息内容 |
| imageUrl | TEXT | 图片 URL（图片消息使用） |
| timestamp | LONG | 发送时间戳（毫秒） |
| showTime | INTEGER | 是否为时间提示（1 是 / 0 否） |

### **2.2 system_messages（系统消息表）**

用于存储系统推送（如互动提醒）。

| 字段名     | 类型                           | 说明                     |
|------------|--------------------------------|--------------------------|
| id         | INTEGER PRIMARY KEY AUTOINCREMENT | 主键                     |
| type       | INTEGER                        | 系统消息类型（如互动=1） |
| content    | TEXT                           | 文本内容                 |
| extra      | TEXT                           | 扩展 JSON 数据           |
| timestamp  | LONG                           | 推送时间戳               |

---

### **2.3 friends（好友信息表）**

维护好友的展示信息及状态。

| 字段名   | 类型              | 说明                         |
|----------|-------------------|------------------------------|
| id       | INTEGER PRIMARY KEY | 好友 ID                      |
| nickname | TEXT              | 原始昵称                     |
| remark   | TEXT              | 备注名                       |
| avatar   | TEXT              | 头像地址                     |
| unread   | INTEGER           | 未读数量                     |
| isPinned | INTEGER           | 是否置顶（1=置顶）           |
| isHidden | INTEGER           | 是否隐藏（1=隐藏）           |

---

### **2.4 user（用户登录态）**

记录当前登录用户。

| 字段名   | 类型              | 说明                         |
|----------|-------------------|------------------------------|
| id       | INTEGER PRIMARY KEY | 用户 ID                      |
| username | TEXT              | 用户名                       |
| password | TEXT              | 密码（示例项目明文存储）     |
| avatar   | TEXT              | 头像 URL                     |

---

### **2.5 metrics（埋点事件表）**

用于记录用户行为事件，供 Dashboard 分析使用。

| 字段名    | 类型                            | 说明                              |
|-----------|---------------------------------|-----------------------------------|
| id        | INTEGER PRIMARY KEY AUTOINCREMENT | 主键                               |
| module    | TEXT                            | 所属功能模块（message/chat/...）   |
| event     | TEXT                            | 事件类型（expose/enter/click/stay）|
| value     | TEXT                            | 扩展数据（JSON）                   |
| timestamp | LONG                            | 事件发生时间戳                     |

### 3.数据库升级代码示例
```
/**
     * 数据库版本号：
     * v6：新增 system_message 表
     * v7：message 增加 msgType
     * v8：friend 表增加 isPinned / isHidden
     * v9：message 表增加 imagePath / actionText / actionPayload
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN msgType INTEGER DEFAULT 1");
            } catch (Exception ignored) {}
        }

        // v8 增加 isPinned / isHidden
        if (oldVersion < 8) {
            try {
                db.execSQL("ALTER TABLE friend ADD COLUMN isPinned INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE friend ADD COLUMN isHidden INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }

        // v9：给 message 增加 imagePath / actionText / actionPayload
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN imagePath TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN actionText TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE message ADD COLUMN actionPayload TEXT");
            } catch (Exception ignored) {}
        }
    }
```
## 遇到的问题与解决方案

### 1. 会话列表出现遗漏或排序异常

**问题表现**  
会话列表中部分会话未显示、排序位置错误、置顶会话未置顶或混排错乱。

**产生原因**  
- `buildSessionList()` 在构建会话列表时，没有同时处理 **隐藏标记（isHidden）过滤** 与 **置顶标记（isPinned）分组**。  
- 系统消息在插入时使用 `list.add(0, ...)`，但未考虑置顶会话排序顺序，导致系统消息可能出现在错误位置。   
- 列表最终排序仅按 `timestamp` 排序，未加入 `isPinned` 的排序权重，造成置顶会话仍按时间排序。

**解决方案**  
- 在构建会话列表时按顺序执行：过滤隐藏 → 补全好友信息 → 插入系统消息 → 分成置顶与普通两个列表 → 分别按时间排序 → 合并。  
- 排序逻辑使用 `(isPinned DESC, timestamp DESC)` 明确写入排序规则。  

---

### 2. 搜索系统跨多表查询耦合严重

**问题表现**  
搜索结果头像缺失、昵称不准确、跳转会话定位异常等问题时常出现。

**产生原因**  
- 搜索数据来自 `messages` 表，但 UI 必须同时显示 `friends` 表内的昵称、备注、头像。  
- 搜索结果如果只按 message 查询，无法补全 Friend 信息；如果补全方式在 Adapter 内执行，会因异步或多次绑定造成数据不一致。  
- 搜索结果需要按 sessionId 分组，但 message 表本身不具备会话维度信息，需要手动构建。

**解决方案**  
- 在 Repository 中统一完成：message 查询 → 按 sessionId 聚合 → 补全 friend 信息 → 生成 SearchResult 模型。  
- UI 只渲染 Repository 产出的结构化数据。  
- 使用统一的数据模型（SearchConversationResult / SearchMessageResult）承载搜索结果。



---

### 3. 未读状态更新错乱（数据库与 UI 不一致）

**问题表现**  
进入聊天页未读未清零、收到消息时未读数错误、搜索返回后 unread 变为异常值。

**产生原因**  
- 未读数的计算依赖当前 UI 列表，而不是数据库中的总消息量。  
- ChatActivity 打开时仅更新 UI，未将 unread 持久化为 0。  
- MessageCenter 新增消息后只更新 messages 表，但未同步更新 friends 表中的 unread 字段。  
- 多处调用 `notifyXXX` 导致 UI 的 unread 与数据库 unread 不一致。

**解决方案**  
- 将 unread 逻辑全部放到 FriendRepository：任何新增消息、进入会话、搜索跳转均需修改数据库中的 unread 字段。  
- ChatActivity 打开会话时立即执行 `friendRepo.clearUnread(friendId)`。  
- 系统消息插入时统一更新 unread 并通知 MessageFragment 重构列表。


---

### 4. 聊天页时间戳错乱与时间提示重复插入

**问题表现**  
出现 1970 时间、重复时间提示、多次刷新后时间段大量堆叠。

**产生原因**  
- 数据库中部分消息的 time 字段为空或为 0，被解析为 Unix 时间戳 0。  
- 填充timestamp的函数将已有的timestamp再次作为“普通消息”参与时间判断，导致重复插入。  

**解决方案**  
- time 字段为 0 的消息直接跳过时间提示计算。  
- 在插入时间提示前过滤掉 TYPE_TIME 的旧提示项，保证只对真实消息计算。  
- 重写 fillTimestamp，使其对同一输入始终生成一致的列表。

---

### 5. 聊天页分页导致滚动跳动或位置重置

**问题表现**  
加载更多旧消息时页面跳回顶部或底部，阅读位置丢失。

**产生原因**  
- LiveData 刷新消息列表后默认触发 scrollToBottom，使分页加载时位置被强制重置。  
- 在 RecyclerView 顶部插入数据后，系统会重新计算布局，使现有 item 的位置发生偏移。  
- 未记录分页加载前的 anchor 位置与像素偏移。

**解决方案**  
- 加载更多前记录 firstVisiblePosition 与 firstItemTopOffset。  
- 插入新数据后使用 scrollToPositionWithOffset() 精准恢复位置。  
- 在分页场景下禁用自动滚动到底部，只在“发送消息”操作时启用。

---

## 运行方式

本项目为 Android 原生应用，基于 **Android Studio + Gradle** 开发。  
代码完全本地运行，不依赖远端服务器（消息数据、埋点、用户信息等均基于 SQLite 本地存储）。

### **1. 环境要求**

| 工具 | 版本建议 |
|------|----------|
| Android Studio | Arctic Fox / Bumblebee / Electric Eel 及以上 |
| JDK | 1.8 或 11 |
| Gradle | 项目自带 Wrapper |
| Android SDK | 24+（最低支持 Android 7.0） |

---

### **2. 克隆项目**

```bash
git clone https://github.com/Conrad-Lee/AndroidProjects.git
