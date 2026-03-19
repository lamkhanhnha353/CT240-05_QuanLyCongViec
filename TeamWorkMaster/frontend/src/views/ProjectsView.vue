<template>
  <div class="min-h-screen bg-slate-50 flex font-sans">
    <aside class="w-64 bg-slate-900 text-white flex flex-col shadow-2xl z-10">
      <div class="p-6 border-b border-slate-800">
        <h2 class="text-2xl font-black text-blue-500 tracking-tighter">TEAMWORK</h2>
        <p class="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1">Master System</p>
      </div>

      <nav class="flex-1 p-4 space-y-2">
        <router-link to="/dashboard" class="block px-4 py-3 hover:bg-slate-800 rounded-xl font-bold text-slate-400 hover:text-white transition-all">
          Bảng điều khiển
        </router-link>
        <router-link to="/projects" class="block px-4 py-3 bg-blue-600 rounded-xl font-bold shadow-lg shadow-blue-900/50 transition-all">
          Quản lý Dự án
        </router-link>
        <router-link to="/tasks" class="block px-4 py-3 hover:bg-slate-800 rounded-xl font-bold text-slate-400 hover:text-white transition-all">
          Công việc của tôi
        </router-link>
      </nav>

      <div class="p-4 border-t border-slate-800">
        <button @click="handleLogout" class="w-full py-3 text-center text-red-400 font-bold hover:bg-red-500/10 rounded-xl transition-all">
          Đăng xuất
        </button>
      </div>
    </aside>

    <main class="flex-1 p-10 h-screen overflow-y-auto relative">
      <header class="flex justify-between items-center mb-8">
        <div>
          <h1 class="text-3xl font-extrabold text-slate-800">Quản lý Dự án</h1>
          <p class="text-slate-500 font-medium mt-1">Khởi tạo và theo dõi tiến độ các dự án của bạn.</p>
        </div>
        
        <div class="flex items-center space-x-4">
          <div class="bg-yellow-50 border border-yellow-200 px-4 py-2 rounded-xl flex items-center space-x-2">
            <span class="text-xs font-bold text-yellow-700 uppercase">Test Quyền:</span>
            <select v-model="simulatedRole" class="bg-transparent text-sm font-bold text-yellow-800 outline-none cursor-pointer">
              <option value="OWNER">👑 Trưởng dự án (Owner)</option>
              <option value="MANAGER">💼 Quản lý (Manager)</option>
              <option value="MEMBER">👤 Thành viên (Member)</option>
            </select>
          </div>

          <div class="flex items-center space-x-3 bg-white px-5 py-2 rounded-full shadow-sm border border-slate-200">
            <span class="font-bold text-slate-700">Xin chào, {{ currentUser }}</span>
            <div class="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white font-black shadow-md">
              {{ firstLetter }}
            </div>
          </div>
        </div>
      </header>

      <div v-if="!isCreating" class="bg-white p-4 rounded-2xl shadow-sm border border-slate-100 flex justify-between items-center mb-8">
        <div class="flex space-x-4 flex-1 max-w-2xl">
          <div class="relative flex-1">
            <span class="absolute left-3 top-3 text-slate-400">🔍</span>
            <input v-model="searchQuery" type="text" placeholder="Tìm tên dự án, mô tả, ID..." class="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all" />
          </div>
          <select v-model="filterStatus" class="px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:border-blue-500 outline-none font-medium text-slate-600 cursor-pointer transition-all">
            <option value="ALL">Tất cả trạng thái</option>
            <option value="PENDING">⏳ Đang chờ</option>
            <option value="IN_PROGRESS">🔥 Đang làm</option>
            <option value="COMPLETED">✅ Hoàn thành</option>
            <option value="CANCELED">❌ Đã hủy</option>
          </select>
        </div>

        <div class="flex items-center space-x-3">
          <div v-if="filteredProjects.length > 0 && simulatedRole === 'OWNER'" class="flex items-center space-x-2 bg-slate-50 px-4 py-2.5 rounded-xl border border-slate-200 cursor-pointer hover:bg-slate-100 transition-colors" @click="toggleSelectAllBtn">
            <input type="checkbox" :checked="isAllSelected" class="w-4 h-4 text-blue-600 rounded pointer-events-none" />
            <span class="text-sm font-bold text-slate-600 select-none">Chọn tất cả</span>
          </div>

          <transition name="fade">
            <button v-if="selectedProjects.length > 0 && simulatedRole === 'OWNER'" @click="deleteSelectedProjects" class="px-4 py-2.5 bg-red-100 text-red-600 font-bold rounded-xl hover:bg-red-200 transition-all flex items-center space-x-2">
              <span>Xóa {{ selectedProjects.length }} mục</span>
            </button>
          </transition>

          <button v-if="['OWNER', 'MANAGER'].includes(simulatedRole)" @click="openCreateForm" class="px-5 py-2.5 bg-blue-600 text-white font-bold rounded-xl shadow-md hover:bg-blue-700 transition-all active:scale-95">
            + Tạo Dự Án Mới
          </button>
        </div>
      </div>

      <CreateProjectForm 
        v-if="isCreating" 
        :isEdit="isEditMode" 
        :initialData="editData" 
        @project-created="handleFormSuccess" 
        @cancel="closeForm" 
      />

      <div v-if="!isCreating" class="pb-16">
        <div v-if="loading" class="bg-white rounded-3xl p-16 shadow-sm border border-slate-100 text-center text-slate-500 font-medium">
          <div class="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          Đang tải dữ liệu...
        </div>
        
        <div v-else-if="filteredProjects.length === 0" class="bg-white rounded-3xl p-20 shadow-sm border border-slate-100 text-center">
          <div class="text-7xl mb-6">📭</div>
          <h3 class="text-2xl font-bold text-slate-800 mb-3">Không tìm thấy dự án</h3>
          <p class="text-slate-500 text-lg mb-6">Chưa có dự án nào hoặc không khớp với điều kiện lọc/tìm kiếm của bạn.</p>
        </div>

        <div v-else class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
          <div v-for="p in filteredProjects" :key="p.id" class="bg-white rounded-3xl p-6 border transition-all duration-200 flex flex-col relative group" :class="selectedProjects.includes(p.id) ? 'border-blue-500 ring-2 ring-blue-500/20 shadow-md bg-blue-50/10' : 'border-slate-200 shadow-sm hover:shadow-md hover:border-blue-300'">
            
            <div class="flex justify-between items-start mb-4">
              <label v-if="simulatedRole === 'OWNER'" class="flex items-center space-x-3 cursor-pointer group-hover:text-blue-600 transition-colors">
                <input type="checkbox" v-model="selectedProjects" :value="p.id" class="w-5 h-5 text-blue-600 rounded border-slate-300 focus:ring-blue-500 cursor-pointer" />
                <span class="font-black text-slate-300 text-lg">#{{ p.id }}</span>
              </label>
              <span v-else class="font-black text-slate-300 text-lg">#{{ p.id }}</span>
              
              <span :class="getStatusClass(p.status)" class="px-3 py-1.5 rounded-xl text-[11px] font-bold uppercase tracking-wider border">
                {{ formatStatus(p.status) }}
              </span>
            </div>

            <div class="flex-1 mb-6">
              <h3 class="text-xl font-extrabold text-slate-800 mb-2 truncate" :title="p.name">{{ p.name }}</h3>
              <p class="text-sm text-slate-500 line-clamp-2 h-10" :title="p.description">{{ p.description || 'Chưa có mô tả chi tiết cho dự án này.' }}</p>
            </div>

            <div class="bg-slate-50 rounded-2xl p-4 mb-6 border border-slate-100">
              <div class="flex justify-between items-center mb-2">
                <span class="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center"><span class="mr-2">🚀</span> Bắt đầu</span>
                <span class="text-sm font-bold text-slate-700">{{ formatDate(p.startDate) }}</span>
              </div>
              <div class="w-full h-px bg-slate-200 my-2"></div>
              <div class="flex justify-between items-center mt-2">
                <span class="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center"><span class="mr-2">🏁</span> Deadline</span>
                <span class="text-sm font-black text-red-500">{{ formatDate(p.endDate) }}</span>
              </div>
            </div>

            <div class="flex space-x-2 pt-4 border-t border-slate-100 mt-auto">
              <button @click="openMemberModal(p)" class="flex-1 flex items-center justify-center space-x-2 py-2.5 bg-blue-50 text-blue-600 hover:bg-blue-600 hover:text-white rounded-xl font-bold transition-all">
                <span>{{ ['OWNER', 'MANAGER'].includes(simulatedRole) ? '👥 Quản lý' : '👁️ Xem Nhóm' }}</span>
              </button>
              
              <button v-if="['OWNER', 'MANAGER'].includes(simulatedRole)" @click="openEditForm(p)" class="px-4 flex items-center justify-center py-2.5 bg-yellow-50 text-yellow-600 hover:bg-yellow-500 hover:text-white rounded-xl transition-all" title="Sửa dự án">
                ✏️
              </button>

              <button v-if="simulatedRole === 'OWNER'" @click="deleteProject(p.id)" class="px-4 flex items-center justify-center py-2.5 bg-red-50 text-red-500 hover:bg-red-500 hover:text-white rounded-xl transition-all" title="Xóa dự án">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showMemberModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-lg rounded-3xl shadow-2xl overflow-hidden transform transition-all">
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <h2 class="text-lg font-bold text-slate-800">
            👥 Nhóm dự án: <span class="text-blue-600">{{ selectedProject.name }}</span>
          </h2>
          <button @click="closeMemberModal" class="text-slate-400 hover:text-red-500 text-3xl leading-none">&times;</button>
        </div>
        
        <div class="p-6">
          <div v-if="['OWNER', 'MANAGER'].includes(simulatedRole)" class="bg-blue-50/50 p-4 rounded-2xl border border-blue-100 mb-6">
            <h4 class="font-bold text-blue-800 mb-3 text-sm uppercase tracking-wider">Mời thành viên mới</h4>
            <div class="flex flex-col space-y-3">
              <input v-model="newMember.email" type="email" placeholder="Nhập địa chỉ Email người dùng..." class="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none" />
              <div class="flex space-x-2">
                <select v-model="newMember.role" class="flex-1 px-3 py-2 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none bg-white">
                  <option value="MEMBER">Thành viên (Member)</option>
                  <option value="MANAGER">Quản lý (Manager)</option>
                  <option value="OWNER">Trưởng dự án (Owner)</option>
                </select>
                <button @click="addMember" class="px-6 py-2 bg-blue-600 text-white font-bold rounded-xl shadow-md hover:bg-blue-700 active:scale-95 transition-all">
                  Thêm
                </button>
              </div>
            </div>
          </div>

          <div>
            <h4 class="font-bold text-slate-700 mb-3 text-sm uppercase tracking-wider">Thành viên hiện tại</h4>
            <div class="flex items-center p-3 border border-slate-100 rounded-2xl mb-2 bg-white shadow-sm">
              <div class="w-10 h-10 bg-blue-600 text-white font-bold rounded-full flex items-center justify-center mr-3 shadow-inner">A</div>
              <div class="flex-1">
                <div class="font-bold text-slate-800">Admin (ID: 1)</div>
                <div class="text-xs text-slate-500">admin@teamwork.com</div>
              </div>
              <div class="px-3 py-1 bg-yellow-100 text-yellow-800 text-xs font-bold rounded-lg tracking-wide border border-yellow-200">OWNER</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import CreateProjectForm from "../components/CreateProjectForm.vue"; 

const router = useRouter();
const currentUser = ref("Khách");
const firstLetter = ref("K");

// --- MỚI THÊM: STATE CHO RBAC ---
const simulatedRole = ref("OWNER"); // Mặc định là Owner để test full quyền

// --- STATE CƠ BẢN ---
const isCreating = ref(false); 
const isEditMode = ref(false);
const editData = ref(null);
const projects = ref([]);
const loading = ref(true);
const selectedProjects = ref([]); 

// --- STATE TÌM KIẾM & LỌC ---
const searchQuery = ref("");
const filterStatus = ref("ALL");

// --- STATE MODAL THÀNH VIÊN ---
const showMemberModal = ref(false);
const selectedProject = ref({});
const newMember = ref({ email: '', role: 'MEMBER' });

// --- COMPUTED: TÌM KIẾM & LỌC DỮ LIỆU ---
const filteredProjects = computed(() => {
  return projects.value.filter(p => {
    // Lọc theo Text
    const searchStr = searchQuery.value.toLowerCase();
    const matchSearch = p.name.toLowerCase().includes(searchStr) || 
                        (p.description && p.description.toLowerCase().includes(searchStr)) ||
                        p.id.toString().includes(searchStr);
    
    // Lọc theo Status
    const matchStatus = filterStatus.value === 'ALL' || p.status === filterStatus.value;
    
    return matchSearch && matchStatus;
  });
});

const isAllSelected = computed(() => {
  return filteredProjects.value.length > 0 && selectedProjects.value.length === filteredProjects.value.length;
});

const toggleSelectAllBtn = () => {
  if (isAllSelected.value) {
    selectedProjects.value = [];
  } else {
    // Chỉ chọn những item đang hiển thị trên lưới đã qua bộ lọc
    selectedProjects.value = filteredProjects.value.map(p => p.id);
  }
};

const fetchProjects = async () => {
  loading.value = true;
  selectedProjects.value = []; 
  try {
    const response = await fetch("http://localhost:8080/api/projects/list");
    if (!response.ok) throw new Error("Lỗi mạng");
    projects.value = await response.json();
  } catch (error) {
    console.error("Không thể tải danh sách:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  const storedUser = localStorage.getItem("username");
  if (storedUser) {
    currentUser.value = storedUser;
    firstLetter.value = storedUser.charAt(0).toUpperCase();
  }
  fetchProjects(); 
});

// --- UI FORM INTERACTIONS ---
const openCreateForm = () => {
  isEditMode.value = false;
  editData.value = null;
  isCreating.value = true;
};

const openEditForm = (project) => {
  isEditMode.value = true;
  editData.value = project;
  isCreating.value = true;
};

const closeForm = () => {
  isCreating.value = false;
};

const handleFormSuccess = () => {
  isCreating.value = false;
  fetchProjects();
};

const handleLogout = () => {
  localStorage.removeItem("isLoggedIn");
  localStorage.removeItem("username");
  router.push("/");
};

// --- API XÓA ---
const deleteProject = async (id) => {
  if (!confirm(`Bạn có chắc muốn xóa dự án #${id}?`)) return;
  try {
    const res = await fetch(`http://localhost:8080/api/projects/delete`, {
      method: 'POST',
      body: JSON.stringify({ projectId: id })
    });
    if (res.ok) fetchProjects(); 
  } catch (error) {
    alert("Lỗi kết nối khi xóa!");
  }
};

const deleteSelectedProjects = async () => {
  if (!confirm(`Xóa vĩnh viễn ${selectedProjects.value.length} dự án đã chọn?`)) return;
  try {
    loading.value = true;
    const deletePromises = selectedProjects.value.map(id => {
      return fetch(`http://localhost:8080/api/projects/delete`, { method: 'POST', body: JSON.stringify({ projectId: id }) });
    });
    await Promise.all(deletePromises);
    selectedProjects.value = []; 
    fetchProjects(); 
  } catch (error) {
    alert("Có lỗi xảy ra khi xóa hàng loạt!");
    loading.value = false;
  }
};

// --- API THÀNH VIÊN ---
const openMemberModal = (project) => {
  selectedProject.value = project;
  newMember.value = { email: '', role: 'MEMBER' };
  showMemberModal.value = true;
};

const closeMemberModal = () => {
  showMemberModal.value = false;
};

const addMember = async () => {
  if (!newMember.value.email) return alert("Vui lòng nhập Email người dùng!");
  
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailPattern.test(newMember.value.email)) {
    return alert("Vui lòng nhập đúng định dạng Email (ví dụ: name@gmail.com)!");
  }

  try {
    const payload = { projectId: selectedProject.value.id, email: newMember.value.email, role: newMember.value.role };
    const res = await fetch("http://localhost:8080/api/projects/add-member", { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    const result = await res.json();

    if (res.ok) {
      alert(`Thành công! Đã mời User mang email [${newMember.value.email}] vào dự án.`);
      newMember.value.email = ''; 
    } else {
      alert(`Lỗi: ${result.error}`);
    }
  } catch (error) {
    alert("Lỗi kết nối đến máy chủ khi thêm thành viên!");
  }
};

// --- TIỆN ÍCH ---
const formatDate = (dateString) => {
  if (!dateString || dateString === 'null') return 'Chưa có';
  return new Date(dateString).toLocaleDateString('vi-VN');
};

const formatStatus = (status) => {
  const map = { PENDING: 'Đang chờ', IN_PROGRESS: 'Đang làm', COMPLETED: 'Hoàn thành', CANCELED: 'Đã hủy' };
  return map[status] || status;
};

const getStatusClass = (status) => {
  const map = {
    PENDING: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    IN_PROGRESS: 'bg-blue-100 text-blue-800 border-blue-200',
    COMPLETED: 'bg-emerald-100 text-emerald-800 border-emerald-200',
    CANCELED: 'bg-red-100 text-red-800 border-red-200'
  };
  return map[status] || 'bg-slate-100 text-slate-800 border-slate-200';
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; transform: translateY(-10px); }

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;  
  overflow: hidden;
}
</style>