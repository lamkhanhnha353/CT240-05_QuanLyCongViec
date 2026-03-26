# 🚀 TeamWork Master

**Hệ thống Quản lý Công việc Nhóm** — Đồ án môn Nguyên lý Xây dựng Phần mềm (CT240)

> Nhóm 5 · Trường CNTT & TT · Phiên bản 1.0 · 03/2026

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Cấu trúc thư mục](#-cấu-trúc-thư-mục)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Hướng dẫn cài đặt](#-hướng-dẫn-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [Kiểm thử](#-kiểm-thử)
- [Design Patterns](#-design-patterns)
- [Thành viên nhóm](#-thành-viên-nhóm)

---

## 🌟 Giới thiệu

TeamWork Master là ứng dụng quản lý công việc nhóm quy mô vừa, xây dựng theo kiến trúc **Client-Server** kết hợp mô hình **Microkernel (Plugin-based)**. Hệ thống giúp các nhóm dự án phân công, theo dõi tiến độ và cộng tác hiệu quả trong một nền tảng tập trung.

**Điểm nổi bật kỹ thuật:**

- Kiến trúc Plugin động: nạp tính năng mới qua Java Reflection mà không cần sửa code lõi.
- Đa luồng thực sự: `ScheduledExecutorService` cho DeadlineBot, `SwingWorker` cho thống kê, `Runnable` cho giám sát deadline.
- Thông báo email tự động qua SendGrid API.
- Phân quyền RBAC 3 cấp: Admin → Manager → Member.
- 7 file kiểm thử JUnit 5 bao phủ unit test và integration test.

---

## ✨ Tính năng

| Nhóm chức năng | Tính năng                                                                                                      |
| -------------- | -------------------------------------------------------------------------------------------------------------- |
| **Dự án**      | Tạo, chỉnh sửa, xóa dự án · Phân quyền thành viên (Owner/Manager/Member) · Mời thành viên qua thông báo        |
| **Công việc**  | Bảng Kanban kéo-thả · Tạo/sửa/xóa task · Subtask (checklist) · Đính kèm file · Lịch sử thay đổi (Activity Log) |
| **Người dùng** | Đăng ký · Đăng nhập · Phân quyền hệ thống · Khóa/mở tài khoản · Đổi mật khẩu · Quên mật khẩu                   |
| **Thông báo**  | Thông báo in-app (chuông) · Email tự động qua SendGrid · Nhắc deadline trước 3 ngày, đúng ngày, quá hạn        |
| **Bình luận**  | Bình luận trực tiếp trong task · Đính kèm file trong comment                                                   |
| **Thống kê**   | Biểu đồ trạng thái công việc · Dashboard Admin · Xuất Excel                                                    |
| **Khác**       | Chat nội bộ dự án · Quản lý cuộc họp · Plugin Loader qua Reflection                                            |

---

## 🏗 Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────┐
│              CLIENT (Trình duyệt Web)                │
│         Vue 3 + Tailwind CSS + Pinia + Axios         │
│  LoginView · DashboardView · KanbanBoardView · ...   │
└──────────────────┬──────────────────────────────────┘
                   │  RESTful API (HTTP/JSON · Port 8080)
┌──────────────────▼──────────────────────────────────┐
│              SERVER (Java Backend)                   │
│  ┌─────────────────────────────────────────────┐    │
│  │             Core Framework                  │    │
│  │  ApiServer · DashboardFrame · PluginLoader  │    │
│  │  DatabaseConnection (Singleton)             │    │
│  │  GlobalExceptionHandler · PermissionService │    │
│  └────────────────┬────────────────────────────┘    │
│                   │ IPlugin / IHostContext           │
│  ┌────────────────▼────────────────────────────┐    │
│  │              Plugin Layer                   │    │
│  │  DeadlineBot · StatisticsPlugin             │    │
│  │  TaskActionNotifier · ProjectPlugin         │    │
│  │  EmailService · HelloWorldPlugin            │    │
│  └─────────────────────────────────────────────┘    │
│                   │  JDBC                           │
│  ┌────────────────▼────────────────────────────┐    │
│  │         Data Access Layer (DAO)              │    │
│  │  UserDAO · ProjectDAO · TaskDAO · CommentDAO │    │
│  │  NotificationDAO · MeetingDAO · ...          │    │
│  └─────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────┘
                   │  MySQL Connector/J
┌──────────────────▼──────────────────────────────────┐
│           MySQL 8.0 (Port 3306 / Docker 3307)        │
│  TBL_USERS · TBL_PROJECTS · TBL_TASKS               │
│  TBL_COMMENTS · TBL_NOTIFICATIONS · ...             │
└──────────────────────────────────────────────────────┘
```

### Phân tầng (Multi-layer)

| Tầng               | Công nghệ          | Trách nhiệm                                      |
| ------------------ | ------------------ | ------------------------------------------------ |
| **Presentation**   | Vue 3 SPA          | Hiển thị UI, gọi API, quản lý state              |
| **Business Logic** | Java Core + Plugin | Xử lý nghiệp vụ, phân quyền, đa luồng            |
| **Data Access**    | DAO + JDBC         | Truy vấn MySQL, tách biệt hoàn toàn với Business |

---

## 🛠 Công nghệ sử dụng

**Backend**

- Java 17 · `com.sun.net.httpserver` (HTTP server tự xây)
- MySQL Connector/J 9.6.0
- SendGrid API (gửi email)
- JUnit 5 (kiểm thử)

**Frontend**

- Vue 3 · Vite 7 · Tailwind CSS 4
- Pinia (state management) · Vue Router 5
- Axios · Chart.js · vuedraggable · xlsx-js-style · Day.js

**Database**

- MySQL 8.0

**DevOps / Tools**

- Docker + Docker Compose (khởi động MySQL nhanh)
- VS Code (khuyến nghị)

---

## 📁 Cấu trúc thư mục

```
TeamWorkMaster/
├── backend/
│   ├── lib/
│   │   └── mysql-connector-j-9.6.0.jar
│   ├── src/com/teamwork/
│   │   ├── contract/          # Hợp đồng Plugin
│   │   │   ├── IPlugin.java
│   │   │   └── IHostContext.java
│   │   ├── core/              # Nhân hệ thống
│   │   │   ├── ApiServer.java         # ~1300 dòng, toàn bộ REST endpoint
│   │   │   ├── DashboardFrame.java    # Swing UI quản trị server
│   │   │   ├── NotificationService.java
│   │   │   ├── Task.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── db/                # Data Access Layer
│   │   │   ├── DatabaseConnection.java  # Singleton
│   │   │   ├── UserDAO.java
│   │   │   ├── ProjectDAO.java
│   │   │   ├── TaskDAO.java
│   │   │   ├── CommentDAO.java
│   │   │   ├── NotificationDAO.java
│   │   │   └── ... (8 DAO khác)
│   │   ├── kernel/            # Plugin infrastructure
│   │   │   ├── PluginLoader.java    # Nạp Plugin qua Reflection
│   │   │   ├── PluginManager.java   # Broadcast sự kiện tới Plugin
│   │   │   └── DeadlineMonitor.java # Runnable giám sát deadline
│   │   ├── model/
│   │   │   └── Comment.java
│   │   ├── plugins/           # Các Plugin mở rộng
│   │   │   ├── DeadlineBot.java         # ScheduledExecutorService
│   │   │   ├── StatisticsPlugin.java    # SwingWorker
│   │   │   ├── TaskActionNotifier.java  # Gửi email khi có sự kiện task
│   │   │   ├── EmailService.java        # SendGrid API
│   │   │   ├── ProjectPlugin.java
│   │   │   └── HelloWorldPlugin.java
│   │   ├── service/
│   │   │   └── PermissionService.java  # Kiểm tra RBAC
│   │   └── utils/
│   │       └── PasswordUtil.java       # SHA-256 + Salt
│   └── tests/com/teamwork/
│       ├── core/TaskCoreTest.java
│       ├── db/ProjectTaskIntegrationTest.java
│       ├── db/UserDAOIntegrationTest.java
│       ├── kernel/DeadlineMonitorTest.java
│       ├── service/PermissionServiceTest.java
│       └── utils/PasswordUtilTest.java + LCKPasswordUtilTest.java
├── frontend/
│   ├── src/
│   │   ├── views/             # Các trang chính
│   │   │   ├── LoginView.vue · RegisterView.vue
│   │   │   ├── DashboardView.vue · ProjectsView.vue
│   │   │   ├── KanbanBoardView.vue · TaskView.vue
│   │   │   ├── AccountView.vue
│   │   │   └── AdminView.vue · AdminUsersView.vue · AdminDashboardView.vue
│   │   ├── components/        # Các component tái sử dụng
│   │   │   ├── KanbanBoard.vue · TaskCreateModal.vue · TaskDetailModal.vue
│   │   │   ├── ProjectStatistics.vue · ProjectChat.vue · MeetingRoom.vue
│   │   │   └── ... (10 component khác)
│   │   ├── router/index.js    # Định tuyến + Navigation Guard
│   │   ├── stores/            # Pinia state management
│   │   └── api/taskApi.js
│   ├── package.json
│   └── vite.config.js
├── database/
│   └── init_teamwork_master.sql   # Script tạo toàn bộ schema + dữ liệu mẫu
├── docs/
│   └── version_docs/version1/
│       ├── Maudacta.docx      # Tài liệu SRS
│       ├── Mauthietke.docx    # Tài liệu SDD
│       ├── sodoclassv1.oom    # Sơ đồ lớp
│       └── usecasev1.mdj      # Sơ đồ Use Case
└── docker-compose.yml
```

---

## 💻 Yêu cầu hệ thống

| Thành phần        | Phiên bản tối thiểu           |
| ----------------- | ----------------------------- |
| Java JDK          | 17 trở lên                    |
| Node.js           | 20.19.0 hoặc 22.12.0 trở lên  |
| MySQL             | 8.0 trở lên                   |
| Docker (tùy chọn) | 20.x trở lên                  |
| RAM               | 4 GB trở lên                  |
| OS                | Windows 10/11 · macOS · Linux |

---

## ⚙️ Hướng dẫn cài đặt

### Bước 1 — Clone dự án

```bash
git clone <repository-url>
cd CT240-05_QuanLyCongViec-master/TeamWorkMaster
```

### Bước 2 — Khởi động Database

**Cách A: Dùng Docker (khuyến nghị — nhanh nhất)**

```bash
# Khởi động MySQL trong container (port 3307 trên máy host)
docker-compose up -d

# Kiểm tra container đã chạy chưa
docker ps
```

> Database `teamwork_master` sẽ được tạo tự động với đầy đủ schema và tài khoản mẫu.

**Cách B: Dùng MySQL Workbench / MySQL CLI**

```bash
# Mở MySQL Workbench → tạo Database tên "teamwork_master"
# Sau đó chạy file SQL:
mysql -u root -p < database/init_teamwork_master.sql
```

### Bước 3 — Cài đặt Frontend

```bash
cd frontend
npm install
```

### Bước 4 — Cấu hình Backend

Mở file `backend/src/com/teamwork/db/DatabaseConnection.java` và chỉnh thông tin kết nối:

```java
private final String URL = "jdbc:mysql://localhost:3306/teamwork_master?useUnicode=true&characterEncoding=UTF-8";
//  Nếu dùng Docker Compose: đổi port thành 3307
//  private final String URL = "jdbc:mysql://localhost:3307/teamwork_master?...";

private final String USER = "root";
private final String PASSWORD = "mật_khẩu_của_bạn";  // Mặc định Docker: "123456"
```

---

## 🔧 Cấu hình

### Cấu hình Email (SendGrid)

Để bật tính năng gửi email thông báo, mở `backend/src/com/teamwork/plugins/EmailService.java`:

```java
private static final String API_KEY = "SG.xxxx...";  // Điền SendGrid API Key của bạn

// Điền địa chỉ email người gửi đã xác minh trên SendGrid:
"\"from\": {\"email\": \"your-email@gmail.com\"}"
```

> **Lưu ý bảo mật:** Không commit API Key lên Git. Sử dụng biến môi trường hoặc file `.env` khi triển khai thực tế.

### Thêm thư viện cho VS Code

1. Mở VS Code → góc dưới trái → **JAVA PROJECTS** → **Referenced Libraries** → nhấn `+`
2. Chọn file `backend/lib/mysql-connector-j-9.6.0.jar`
3. Làm tương tự với `lib/junit-platform-console-standalone-1.13.0-M3.jar` (để chạy test)

---

## ▶️ Chạy ứng dụng

### Khởi động Backend

```bash
# Chạy file DashboardFrame.java (entry point)
# Trong VS Code: F5 hoặc nhấn nút Run
```

Sau khi cửa sổ **Server Dashboard** hiện ra:

1. Nhấn menu **Hệ thống Quản Trị → Bật API Server (Cho phép Web kết nối)**
   - Server sẽ lắng nghe tại `http://localhost:8080`
2. _(Tùy chọn)_ Nhấn **Nạp Hello World Plugin** hoặc **Chạy Plugin Thống Kê** để test Plugin

> `DeadlineBot` được khởi động tự động khi server bật — sẽ gửi email nhắc deadline lúc 8:00 AM mỗi ngày.

### Khởi động Frontend

```bash
cd frontend
npm run dev
```

Truy cập ứng dụng tại: **http://localhost:5173**

### Tài khoản mẫu (sau khi chạy init SQL)

| Role  | Tên đăng nhập | Mật khẩu |
| ----- | ------------- | -------- |
| Admin | `admin`       | `123456` |

---

## 🧪 Kiểm thử

Dự án có **7 file test JUnit 5** bao gồm:

| File                              | Loại        | Nội dung                                                   |
| --------------------------------- | ----------- | ---------------------------------------------------------- |
| `TaskCoreTest.java`               | Unit        | Logic Task: constructor, setStatus, notification flags     |
| `DeadlineMonitorTest.java`        | Unit        | Logic phát hiện deadline: trước 3 ngày, đúng ngày, quá hạn |
| `PermissionServiceTest.java`      | Unit        | Phân quyền OWNER/MANAGER/MEMBER + Plugin system            |
| `PasswordUtilTest.java`           | Unit        | SHA-256 hashing, tính nhất quán, độ nhạy                   |
| `LCKPasswordUtilTest.java`        | Unit        | Kiểm thử bổ sung cho PasswordUtil                          |
| `UserDAOIntegrationTest.java`     | Integration | CRUD tài khoản người dùng với DB thật                      |
| `ProjectTaskIntegrationTest.java` | Integration | Tạo project, task, quan hệ với DB thật                     |

### Chạy Unit Test (không cần DB)

```bash
# Dùng VS Code Test Explorer hoặc:
java -jar lib/junit-platform-console-standalone-1.13.0-M3.jar \
  --class-path backend/src:backend/tests \
  --select-class com.teamwork.core.TaskCoreTest
```

> **Lưu ý:** Integration test (`UserDAOIntegrationTest`, `ProjectTaskIntegrationTest`) yêu cầu MySQL đang chạy và đã cấu hình kết nối đúng.

---

## 🎨 Design Patterns

| Pattern                       | Class/Component                        | Mô tả                                                                                                   |
| ----------------------------- | -------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| **Singleton**                 | `DatabaseConnection`                   | Đảm bảo chỉ một instance kết nối DB được tạo                                                            |
| **Plugin / Reflection**       | `PluginLoader`                         | Dùng `Class.forName()` nạp động class Plugin qua tên đầy đủ; validate bằng `IPlugin.isAssignableFrom()` |
| **Observer** _(một phần)_     | `PluginManager`                        | `notifyProjectDeleted()` broadcast sự kiện tới tất cả Plugin đã đăng ký                                 |
| **Strategy** _(cơ sở)_        | `IPlugin` / `NotificationService`      | Mỗi Plugin là một chiến lược xử lý độc lập; `NotificationService` có thể mở rộng nhiều kênh gửi         |
| **Template Method** _(cơ sở)_ | `SwingWorker` trong `StatisticsPlugin` | `doInBackground()` / `process()` / `done()` là bộ khung xử lý nền chuẩn                                 |
| **DAO**                       | `UserDAO`, `TaskDAO`, ...              | Trừu tượng hóa truy cập DB, tách biệt hoàn toàn với Business Logic                                      |

---

## 👥 Thành viên nhóm

| Họ và tên            | MSSV     |
| -------------------- | -------- |
| Lâm Khánh Nhả        | B2303836 |
| Trịnh Minh Khang     | B2303821 |
| Nguyễn Huỳnh Tuấn Vỉ | B2303857 |
| Nguyễn Trường Duy    | B2306611 |
| Lâm Thành Đô         | B2306613 |

---

## 📄 Tài liệu

| Tài liệu | Đường dẫn ||---|---|
| Đặc tả Yêu cầu Phần mềm (SRS) | `docs/version_docs/version2/Maudacta.docx` |
| Tài liệu Thiết kế Phần mềm (SDD) | `docs/version_docs/version2/Mauthietke.docx` |
| Sơ đồ lớp (Class Diagram) | `docs/version_docs/version1/sodoclassv1.oom` |
| Sơ đồ Use Case | `docs/version_docs/version1/usecasev1.mdj` |

---

_CT240 — Nguyên lý Xây dựng Phần mềm · Trường CNTT & TT · 2026_
