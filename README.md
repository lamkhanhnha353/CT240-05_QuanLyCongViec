

# ⚙️ HƯỚNG DẪN CÀI ĐẶT BACKEND (JAVA) CHO TEAM

Chào mọi người, để chạy được Backend Java kết nối với MySQL trên máy cá nhân, anh em làm theo các bước sau nhé:

### Bước 1: Chuẩn bị Database
1. Mở MySQL Workbench.
2. Tạo một Database tên là `teamwork_master`.
3. Mở file mã nguồn `DatabaseConnection.java` trong thư mục `backend/src/.../db/`.
4. Sửa lại biến `USER` và `PASSWORD` thành tài khoản MySQL trên máy của mọi người (thường là root / 123456).

### Bước 2: Liên kết thư viện MySQL (Rất quan trọng)
Dự án có sử dụng file `mysql-connector-j.jar` nằm trong thư mục `lib`. Để VS Code nhận diện được file này:
1. Nhìn xuống góc dưới cùng bên trái của VS Code, mở rộng mục **"JAVA PROJECTS"**.
2. Tìm đến chữ **"Referenced Libraries"** (như trong ảnh `image_9d48dd.png` của thư mục đồ án).
3. Bấm vào dấu **`+`** bên cạnh chữ đó.
4. Một cửa sổ hiện ra, tìm đường dẫn chỉ thẳng vào file `mysql-connector-j.jar` trong thư mục `lib` của dự án và bấm **Select**.
5. Nhấn F5 chạy thử file `DashboardFrame.java` để xem nó báo dòng chữ `[DATABASE] DA KET NOI THANH CONG` là xong!


### 🗄️ HƯỚNG DẪN ĐỒNG BỘ DATABASE (MYSQL) CHO TEAM
Vì Database nằm ở máy cá nhân của mỗi người, nên khi  kéo code về lần đầu tiên, hệ thống sẽ chưa có Database `teamwork_master` để chạy. 

Mọi người làm đúng 2 bước này để tạo Database y hệt nhau nhé:
1. Mở MySQL Workbench trên máy lên.
2. Mở file `init_teamwork_master.sql` (nằm trong thư mục `database` của dự án) bằng MySQL Workbench.
3. Bấm nút **Hình tia sét** (Execute) để chạy toàn bộ file này. 

Kết quả 💥 Máy của mọi người đã có sẵn Database `teamwork_master`, có luôn các bảng và có sẵn tài khoản `admin` (pass: `123456`) để đăng nhập test web rồi nhé! Cứ thế mà bật Backend Java lên chạy thôi.


# 🚀 HƯỚNG DẪN CHẠY FRONTEND (DỰ ÁN TEAMWORK MASTER)
Chào mọi người! Đây là thư mục chứa mã nguồn giao diện Web (Vue 3 + Tailwind CSS) của dự án chúng ta. 

⚠️ **LƯU Ý QUAN TRỌNG VỀ THƯ MỤC `node_modules`:**
Khi mọi người pull (kéo) code từ Github/Gitlab về máy, sẽ **KHÔNG THẤY** thư mục `node_modules`. Đừng lo lắng, đây không phải là lỗi! 
Thư mục này chứa các thư viện rất nặng nên đã được cấu hình ẩn đi (`.gitignore`) để việc đẩy code lên mạng nhanh hơn. 

Để chạy được dự án trên máy cá nhân, chỉ cần làm đúng 3 bước sau:

---

### Bước 1: Yêu cầu hệ thống
Đảm bảo máy tính của anh em đã cài đặt **Node.js**. 
*(Mở Terminal/CMD lên gõ `node -v` để kiểm tra. Nếu có hiện ra số phiên bản là OK, chưa có thì lên trang chủ tải về).*

### Bước 2: Tải thư viện (Chỉ làm 1 lần duy nhất khi mới kéo code về)
Mở Terminal/CMD, dùng lệnh `cd` để trỏ đúng vào thư mục `frontend` này. Sau đó gõ lệnh sau và nhấn Enter:
```bash
npm install




# Ghi chú gọn lại:

# 🚀 DỰ ÁN TEAMWORK MASTER (QUẢN LÝ CÔNG VIỆC)

Chào anh em! Đây là repository chính thức của dự án. 
Để hệ thống chạy trơn tru trên máy cá nhân sau khi pull code về, anh em vui lòng làm theo đúng thứ tự 3 phần dưới đây nhé.

---

## 🗄️ PHẦN 1: CÀI ĐẶT DATABASE (MySQL)
Vì Database nằm ở máy cá nhân, anh em cần chạy file SQL để tạo các bảng giống hệt nhau:
1. Mở phần mềm **MySQL Workbench**.
2. Chọn `File -> Open SQL Script...` và chọn file **`init_teamwork_master.sql`** (nằm trong thư mục `database` của dự án).
3. Bấm nút **Hình tia sét** ⚡ (Execute) để chạy toàn bộ file.
> **Kết quả:** Máy anh em đã có sẵn Database `teamwork_master` cùng 3 bảng (Users, Projects, Tasks) và tài khoản mặc định `admin` / pass: `123456`.

---

## ⚙️ PHẦN 2: CÀI ĐẶT BACKEND (Java Server)
Backend được viết bằng Java thuần. Để kết nối được với MySQL, anh em cần liên kết thư viện:
1. Mở file `DatabaseConnection.java` (trong thư mục `backend/src/.../db/`), kiểm tra lại biến `USER` và `PASSWORD` xem đã khớp với tài khoản MySQL trên máy mình chưa (thường là `root` / `123456`).
2. Nhìn xuống góc dưới cùng bên trái của VS Code, mở rộng mục **JAVA PROJECTS**.
3. Tìm đến mục **Referenced Libraries**, bấm vào dấu **`+`**.
4. Trỏ đường dẫn đến file **`mysql-connector-j.jar`** (nằm trong thư mục `backend/lib/`) và bấm Select.
5. Mở file `DashboardFrame.java` và nhấn F5 để chạy. Nếu Terminal báo `[DATABASE] DA KET NOI THANH CONG` là thành công! Bấm tiếp nút **"Bật API Server"** trên Menu giao diện Java.

---

## 💻 PHẦN 3: CÀI ĐẶT FRONTEND (Vue.js + Tailwind)
> ⚠️ **Lưu ý:** Thư mục `node_modules` đã được ẩn đi để không đẩy lên Git cho nhẹ. Anh em cần tải lại bằng lệnh sau:
1. Mở Terminal mới, dùng lệnh `cd frontend` để di chuyển vào thư mục Web.
2. Gõ lệnh: **`npm install`** (Chỉ cần làm 1 lần duy nhất để tải thư viện).
3. Sau khi tải xong, gõ lệnh: **`npm run dev`** để khởi chạy trang Web.
4. Giữ phím `Ctrl` + Click vào đường link `http://localhost:5173/` để mở web.

🎉 **XONG!** Anh em dùng tài khoản `admin` và mật khẩu `123456` để đăng nhập và bắt đầu code nhé. Chúc anh em code vui vẻ và không bug!