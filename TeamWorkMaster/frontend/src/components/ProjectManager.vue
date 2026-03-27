<template>
  <div class="project-dashboard">
    <header class="dashboard-header">
      <div class="header-left">
        <div class="logo-icon">🚀</div>
        <div>
          <h1>Hệ Thống Quản Lý Dự Án</h1>
          <p class="subtitle">Theo dõi tiến độ và phân bổ nguồn lực hiệu quả</p>
        </div>
      </div>
      <div class="header-right">
        <button @click="openCreateModal" class="btn-primary elevate">
          <span class="icon">+</span> Khởi Tạo Dự Án
        </button>
      </div>
    </header>

    <section class="stats-grid">
      <div class="stat-card border-left-dark">
        <div class="stat-info">
          <h3>Tổng Dự Án</h3>
          <p class="stat-number">{{ projects.length }}</p>
        </div>
        <div class="stat-icon">📂</div>
      </div>
      <div class="stat-card border-left-info">
        <div class="stat-info">
          <h3>Đang Chờ</h3>
          <p class="stat-number">{{ statCount('PENDING') }}</p>
        </div>
        <div class="stat-icon text-info">⏳</div>
      </div>
      <div class="stat-card border-left-warning">
        <div class="stat-info">
          <h3>Đang Thực Hiện</h3>
          <p class="stat-number">{{ statCount('IN_PROGRESS') }}</p>
        </div>
        <div class="stat-icon text-warning">🔥</div>
      </div>
      <div class="stat-card border-left-success">
        <div class="stat-info">
          <h3>Đã Hoàn Thành</h3>
          <p class="stat-number">{{ statCount('COMPLETED') }}</p>
        </div>
        <div class="stat-icon text-success">✅</div>
      </div>
    </section>

    <main class="dashboard-content elevate">
      <div class="toolbar">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input v-model="searchTerm" type="text" placeholder="Tìm tên dự án, ID..." />
        </div>
        <div class="filter-box">
          <select v-model="filterStatus">
            <option value="ALL">Tất cả trạng thái</option>
            <option value="PENDING">Đang chờ</option>
            <option value="IN_PROGRESS">Đang thực hiện</option>
            <option value="COMPLETED">Hoàn thành</option>
            <option value="CANCELED">Đã hủy</option>
          </select>
          <button @click="fetchProjects" class="btn-secondary" title="Tải lại dữ liệu">🔄 Làm mới</button>
        </div>
      </div>

      <div class="table-container">
        <div v-if="loading" class="state-box loading">
          <div class="spinner"></div>
          <p>Đang đồng bộ dữ liệu với máy chủ...</p>
        </div>
        
        <div v-else-if="filteredProjects.length === 0" class="state-box empty">
          <div class="empty-icon">📭</div>
          <h3>Chưa có dữ liệu</h3>
          <p>Bạn chưa có dự án nào hoặc không tìm thấy kết quả phù hợp.</p>
          <button @click="openCreateModal" class="btn-primary mt-3">Tạo dự án đầu tiên</button>
        </div>

        <table v-else class="modern-table">
          <thead>
            <tr>
              <th width="5%">ID</th>
              <th width="30%">Tên Dự Án & Mô Tả</th>
              <th width="15%">Thời Gian</th>
              <th width="15%">Tiến Độ</th>
              <th width="15%">Trạng Thái</th>
              <th width="20%" class="text-right">Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in filteredProjects" :key="p.id" class="table-row">
              <td class="col-id">#{{ p.id }}</td>
              <td>
                <div class="pro-name">{{ p.name }}</div>
                <div class="pro-desc" :title="p.description">{{ truncateText(p.description, 40) }}</div>
              </td>
              <td class="col-time">
                <div class="date-line"><span class="label">Bắt đầu:</span> {{ formatDate(p.startDate) }}</div>
                <div class="date-line"><span class="label">Kết thúc:</span> <strong class="text-danger">{{ formatDate(p.endDate) }}</strong></div>
              </td>
              <td>
                <div class="progress-wrap">
                  <div class="progress-bar" :style="'width: ' + getProgress(p.status) + '%'"></div>
                </div>
                <div class="progress-text">{{ getProgress(p.status) }}% hoàn thành</div>
              </td>
              <td>
                <span :class="'status-badge badge-' + p.status.toLowerCase()">
                  {{ formatStatus(p.status) }}
                </span>
              </td>
              <td class="actions-cell text-right">
                <button @click="openMemberModal(p)" class="btn-action btn-team" title="Quản lý thành viên">👥</button>
                <button @click="editProject(p)" class="btn-action btn-edit" title="Chỉnh sửa">✏️</button>
                <button @click="deleteProject(p.id)" class="btn-action btn-delete" title="Xóa">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <div v-if="showProjectModal" class="modal-backdrop">
      <div class="modal-box elevate-high animate-pop">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Cập Nhật Dự Án' : 'Khởi Tạo Dự Án Mới' }}</h2>
          <button @click="closeModals" class="btn-close">×</button>
        </div>
        <form @submit.prevent="saveProject" class="modal-body">
          <div class="form-group">
            <label>Tên dự án <span class="required">*</span></label>
            <input v-model="form.name" type="text" placeholder="Nhập tên chiến dịch, dự án..." required />
          </div>
          <div class="form-group">
            <label>Mô tả chi tiết</label>
            <textarea v-model="form.desc" rows="3" placeholder="Ghi chú các mục tiêu quan trọng..."></textarea>
          </div>
          <div class="form-row">
            <div class="form-group col-6">
              <label>Ngày bắt đầu <span class="required">*</span></label>
              <input v-model="form.startDate" type="date" required />
            </div>
            <div class="form-group col-6">
              <label>Ngày kết thúc <span class="required">*</span></label>
              <input v-model="form.endDate" type="date" required />
            </div>
          </div>
          <div v-if="isEditing" class="form-group">
            <label>Cập nhật trạng thái</label>
            <select v-model="form.status" class="status-selector">
              <option value="PENDING">⏳ Đang chờ xử lý</option>
              <option value="IN_PROGRESS">🔥 Đang thực hiện</option>
              <option value="COMPLETED">✅ Đã hoàn thành</option>
              <option value="CANCELED">❌ Đã hủy bỏ</option>
            </select>
          </div>
          <div class="modal-footer">
            <button type="button" @click="closeModals" class="btn-secondary">Hủy bỏ</button>
            <button type="submit" class="btn-primary shadow-btn">
              {{ isEditing ? '💾 Lưu Thay Đổi' : '🚀 Xóa Mù Tạo Mới' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showMemberModal" class="modal-backdrop">
      <div class="modal-box elevate-high animate-pop">
        <div class="modal-header">
          <h2>👥 Nhóm Dự Án: <span class="text-primary">{{ selectedProject.name }}</span></h2>
          <button @click="closeModals" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          
          <div class="add-member-box">
            <h4>Thêm thành viên mới</h4>
            <div class="add-member-form">
              <input v-model="newMember.userId" type="number" placeholder="Nhập ID User (VD: 2)" required />
              <select v-model="newMember.role">
                <option value="MEMBER">Thành viên (Member)</option>
                <option value="MANAGER">Quản lý (Manager)</option>
                <option value="OWNER">Trưởng dự án (Owner)</option>
              </select>
              <button @click="addMember" class="btn-primary btn-sm">+ Thêm</button>
            </div>
          </div>

          <hr class="divider" />

          <div class="member-list">
            <h4>Thành viên hiện tại</h4>
            <div class="member-item">
              <div class="member-avatar">A</div>
              <div class="member-info">
                <strong>Admin (ID: 1)</strong>
                <span>admin@teamwork.com</span>
              </div>
              <div class="member-role role-owner">OWNER</div>
            </div>
            <div class="empty-members text-center text-muted py-3">
              Chưa có thành viên nào khác được thêm.
            </div>
          </div>

        </div>
        <div class="modal-footer">
          <button type="button" @click="closeModals" class="btn-secondary">Đóng</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import dayjs from 'dayjs';

export default {
  data() {
    return {
      apiUrl: 'http://localhost:8080/api/projects',
      projects: [],
      
      loading: true,
      searchTerm: '',
      filterStatus: 'ALL',
      
      showProjectModal: false,
      isEditing: false,
      form: { id: null, name: '', desc: '', startDate: '', endDate: '', status: 'PENDING', userId: 1 },
      
      showMemberModal: false,
      selectedProject: {},
      newMember: { userId: '', role: 'MEMBER' }
    };
  },
  computed: {
    filteredProjects() {
      return this.projects.filter(p => {
        const searchStr = this.searchTerm.toLowerCase();
        const matchSearch = p.name.toLowerCase().includes(searchStr) || 
                            (p.id && p.id.toString().includes(searchStr));
        const matchStatus = this.filterStatus === 'ALL' || p.status === this.filterStatus;
        return matchSearch && matchStatus;
      });
    }
  },
  methods: {
    async fetchProjects() {
      this.loading = true;
      try {
        const res = await fetch(`${this.apiUrl}/list`);
        if (!res.ok) throw new Error(`HTTP Error: ${res.status}`);
        const data = await res.json();
        this.projects = data;
      } catch (error) {
        console.error("LỖI FETCH:", error);
        alert("Không thể tải danh sách dự án. Hãy kiểm tra Backend đã bật chưa!");
      } finally {
        this.loading = false;
      }
    },

    async saveProject() {
      if (dayjs(this.form.endDate).isBefore(dayjs(this.form.startDate))) {
        alert("Lỗi Logic: Ngày kết thúc không thể trước ngày bắt đầu!");
        return;
      }

      const isUpdate = this.isEditing;
      const url = isUpdate ? `${this.apiUrl}/update` : `${this.apiUrl}/create`;
      const payload = { ...this.form, userId: 1 }; 
      if (isUpdate) payload.projectId = this.form.id;

      try {
        const res = await fetch(url, {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        
        const result = await res.json();
        if (res.ok) {
          alert(isUpdate ? "Đã cập nhật dự án thành công!" : "Tạo dự án mới thành công!");
          this.closeModals();
          this.fetchProjects(); 
        } else {
          alert(`Lỗi từ máy chủ: ${result.error}`);
        }
      } catch (error) {
        console.error("LỖI SAVE:", error);
        alert("Lỗi kết nối API. Máy chủ có vẻ đang tắt?");
      }
    },

    async deleteProject(id) {
      if (!confirm(`CẢNH BÁO: Bạn chuẩn bị xóa vĩnh viễn dự án #${id}.\nBạn có chắc chắn không?`)) return;
      try {
        const res = await fetch(`${this.apiUrl}/delete`, {
          method: 'POST',
          body: JSON.stringify({ projectId: id, userId: 1 })
        });
        if (res.ok) {
          this.fetchProjects(); 
        } else {
          const result = await res.json();
          alert(`Xóa thất bại: ${result.error}`);
        }
      } catch (error) {
        alert("Lỗi kết nối khi xóa!");
      }
    },

    openMemberModal(project) {
      this.selectedProject = project;
      this.newMember = { userId: '', role: 'MEMBER' };
      this.showMemberModal = true;
    },
    
  
    async addMember() {
      if (!this.newMember.userId) return alert("Vui lòng nhập ID người dùng!");
      
      try {
        const payload = {
          projectId: this.selectedProject.id,
          userId: this.newMember.userId,
          role: this.newMember.role
        };

        const res = await fetch(`${this.apiUrl}/add-member`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        });

        const result = await res.json();

        if (res.ok) {
          alert(`Đã thêm User ID ${this.newMember.userId} với quyền ${this.newMember.role} thành công!`);
          this.newMember.userId = ''; 
         
        } else {
          alert(`Lỗi: ${result.error}`);
        }
      } catch (error) {
        console.error("LỖI THÊM THÀNH VIÊN:", error);
        alert("Lỗi kết nối đến máy chủ khi thêm thành viên!");
      }
    },

    openCreateModal() {
      this.isEditing = false;
      this.form = { id: null, name: '', desc: '', startDate: '', endDate: '', status: 'PENDING', userId: 1 };
      this.showProjectModal = true;
    },
    editProject(p) {
      this.isEditing = true;
      this.form = { ...p, desc: p.description }; 
      this.showProjectModal = true;
    },
    closeModals() {
      this.showProjectModal = false;
      this.showMemberModal = false;
    },
    statCount(status) {
      return this.projects.filter(p => p.status === status).length;
    },
    formatDate(dateStr) {
      if (!dateStr || dateStr === 'null') return 'Chưa rõ';
      return dayjs(dateStr).format('DD/MM/YYYY');
    },
    formatStatus(status) {
      const map = { PENDING: 'Đang chờ', IN_PROGRESS: 'Đang làm', COMPLETED: 'Hoàn thành', CANCELED: 'Đã hủy' };
      return map[status] || status;
    },
    truncateText(text, length) {
      if (!text || text === 'null') return 'Không có mô tả';
      return text.length > length ? text.substring(0, length) + '...' : text;
    },
    getProgress(status) {
      const map = { PENDING: 5, IN_PROGRESS: 45, COMPLETED: 100, CANCELED: 0 };
      return map[status] || 0;
    }
  },
  mounted() {
    this.fetchProjects(); 
  }
};
</script>

<style scoped>

.project-dashboard {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1e293b;
  padding: 24px;
  background-color: #f8fafc;
  min-height: 100vh;
}

/* Utils */
.elevate { box-shadow: 0 1px 3px rgba(0,0,0,0.1), 0 1px 2px rgba(0,0,0,0.06); }
.elevate-high { box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04); }
.text-right { text-align: right; }
.text-danger { color: #e11d48; }
.text-info { color: #0284c7; }
.text-warning { color: #d97706; }
.text-success { color: #16a34a; }
.text-muted { color: #64748b; }
.mt-3 { margin-top: 16px; }

/* Buttons */
button { font-family: inherit; cursor: pointer; border: none; outline: none; transition: all 0.2s; }
.btn-primary { background: #2563eb; color: white; padding: 10px 18px; border-radius: 8px; font-weight: 600; }
.btn-primary:hover { background: #1d4ed8; }
.shadow-btn { box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.3); }
.btn-secondary { background: #f1f5f9; color: #475569; padding: 10px 18px; border-radius: 8px; font-weight: 500; border: 1px solid #cbd5e1; }
.btn-secondary:hover { background: #e2e8f0; }
.btn-sm { padding: 6px 12px; font-size: 13px; }

/* Header */
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; }
.header-left { display: flex; align-items: center; gap: 16px; }
.logo-icon { font-size: 36px; background: white; padding: 12px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.header-left h1 { font-size: 24px; font-weight: 700; margin: 0 0 4px 0; color: #0f172a; }
.subtitle { margin: 0; color: #64748b; font-size: 14px; }

/* Stats Grid */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.stat-card { background: white; border-radius: 12px; padding: 24px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); border-left: 4px solid #cbd5e1; }
.stat-info h3 { font-size: 13px; text-transform: uppercase; color: #64748b; margin: 0 0 8px 0; letter-spacing: 0.5px; }
.stat-number { font-size: 32px; font-weight: 800; color: #0f172a; margin: 0; line-height: 1; }
.stat-icon { font-size: 32px; opacity: 0.8; }
.border-left-dark { border-left-color: #334155; }
.border-left-info { border-left-color: #0ea5e9; }
.border-left-warning { border-left-color: #f59e0b; }
.border-left-success { border-left-color: #22c55e; }

/* Main Content */
.dashboard-content { background: white; border-radius: 16px; padding: 24px; }

/* Toolbar */
.toolbar { display: flex; justify-content: space-between; margin-bottom: 24px; flex-wrap: wrap; gap: 16px; }
.search-box { position: relative; width: 350px; }
.search-icon { position: absolute; left: 12px; top: 10px; font-size: 14px; opacity: 0.5; }
.search-box input { width: 100%; padding: 10px 10px 10px 36px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; background: #f8fafc; }
.search-box input:focus { background: white; border-color: #2563eb; outline: none; }
.filter-box { display: flex; gap: 12px; }
.filter-box select { padding: 10px 16px; border: 1px solid #e2e8f0; border-radius: 8px; background: white; font-size: 14px; cursor: pointer; }

/* States (Loading & Empty) */
.state-box { text-align: center; padding: 60px 20px; color: #64748b; background: #f8fafc; border-radius: 12px; border: 2px dashed #e2e8f0; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.state-box h3 { color: #1e293b; margin-bottom: 8px; }
.spinner { width: 40px; height: 40px; border: 4px solid #e2e8f0; border-top-color: #2563eb; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 16px; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Table */
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table th { background: #f8fafc; color: #64748b; font-size: 12px; text-transform: uppercase; font-weight: 700; padding: 14px 16px; text-align: left; border-bottom: 2px solid #e2e8f0; }
.modern-table td { padding: 16px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.table-row:hover { background: #f8fafc; }

/* Table Inner Elements */
.col-id { color: #94a3b8; font-weight: 600; font-size: 13px; }
.pro-name { font-weight: 700; color: #0f172a; font-size: 15px; margin-bottom: 4px; }
.pro-desc { color: #64748b; font-size: 13px; }
.date-line { font-size: 13px; color: #475569; margin-bottom: 4px; }
.date-line .label { color: #94a3b8; display: inline-block; width: 65px; }

/* Progress Bar */
.progress-wrap { width: 100%; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; margin-bottom: 6px; }
.progress-bar { height: 100%; background: linear-gradient(90deg, #3b82f6, #0ea5e9); transition: width 0.5s ease; }
.progress-text { font-size: 12px; color: #64748b; font-weight: 600; }

/* Badges */
.status-badge { padding: 6px 12px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; display: inline-block; }
.badge-pending { background: #fef9c3; color: #a16207; }
.badge-in_progress { background: #e0f2fe; color: #0284c7; }
.badge-completed { background: #dcfce7; color: #15803d; }
.badge-canceled { background: #ffe4e6; color: #be123c; }

/* Action Buttons in Table */
.actions-cell { white-space: nowrap; }
.btn-action { width: 32px; height: 32px; border-radius: 8px; background: transparent; font-size: 15px; margin-left: 8px; display: inline-flex; align-items: center; justify-content: center; }
.btn-action:hover { background: #e2e8f0; transform: scale(1.1); }
.btn-team:hover { background: #e0e7ff; color: #4f46e5; }
.btn-edit:hover { background: #fef3c7; }
.btn-delete:hover { background: #fee2e2; }

/* Modals */
.modal-backdrop { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 9999; }
.modal-box { background: white; width: 100%; max-width: 600px; border-radius: 16px; overflow: hidden; display: flex; flex-direction: column; max-height: 90vh; }
.animate-pop { animation: popIn 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes popIn { 0% { opacity: 0; transform: translateY(20px) scale(0.95); } 100% { opacity: 1; transform: translateY(0) scale(1); } }

.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid #e2e8f0; background: #f8fafc; }
.modal-header h2 { font-size: 18px; font-weight: 700; color: #0f172a; margin: 0; }
.btn-close { font-size: 24px; color: #94a3b8; background: none; line-height: 1; padding: 4px; border-radius: 4px; }
.btn-close:hover { color: #0f172a; background: #e2e8f0; }

.modal-body { padding: 24px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 13px; font-weight: 600; color: #475569; }
.required { color: #ef4444; }
.form-group input, .form-group textarea, .status-selector { padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; background: #fff; transition: all 0.2s; }
.form-group input:focus, .form-group textarea:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1); outline: none; }
.form-row { display: flex; gap: 16px; }
.col-6 { flex: 1; }

.modal-footer { padding: 20px 24px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 12px; background: #f8fafc; }

/* Member Modal Specifics */
.add-member-box { background: #f1f5f9; padding: 16px; border-radius: 12px; border: 1px dashed #cbd5e1; }
.add-member-box h4 { margin: 0 0 12px 0; font-size: 14px; color: #334155; }
.add-member-form { display: flex; gap: 12px; margin-bottom: 8px; }
.add-member-form input, .add-member-form select { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; }
.add-member-form input { flex: 1; }

.divider { border: 0; height: 1px; background: #e2e8f0; margin: 20px 0; }

.member-list h4 { margin: 0 0 16px 0; font-size: 14px; color: #334155; }
.member-item { display: flex; align-items: center; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 8px; }
.member-avatar { width: 40px; height: 40px; background: #2563eb; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 16px; margin-right: 12px; }
.member-info { flex: 1; display: flex; flex-direction: column; }
.member-info strong { color: #0f172a; font-size: 14px; }
.member-info span { color: #64748b; font-size: 12px; }
.member-role { font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 12px; background: #f1f5f9; color: #475569; }
.role-owner { background: #fef9c3; color: #854d0e; }
</style>