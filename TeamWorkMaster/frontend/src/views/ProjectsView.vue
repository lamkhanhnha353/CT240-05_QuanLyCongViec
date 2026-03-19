<template>
  <div class="min-h-screen bg-[#f8f9fa] flex font-sans">
    
    <aside class="w-64 bg-slate-900 text-white flex flex-col shadow-2xl z-10 shrink-0">
      <div class="p-6 border-b border-slate-800 flex items-center space-x-3">
        <div class="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center font-black text-xl shadow-lg">T</div>
        <div>
          <h2 class="text-xl font-black tracking-tighter">PROJECT ALPHA</h2>
          <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5">Marketing Dept</p>
        </div>
      </div>

      <nav class="flex-1 p-4 space-y-2">
        <router-link to="/dashboard" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
          <span>📊</span><span>Bảng điều khiển</span>
        </router-link>
        <router-link to="/projects" class="flex items-center space-x-3 px-4 py-3 bg-blue-600/10 text-blue-500 rounded-xl font-bold transition-all">
          <span>📂</span><span>Dự án</span>
        </router-link>
        <router-link to="/tasks" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
          <span>☑️</span><span>Công việc của tôi</span>
        </router-link>
      </nav>

      <div class="p-6">
        <button @click="handleLogout" class="w-full flex items-center justify-center py-3 bg-slate-800 hover:bg-red-500/20 hover:text-red-400 text-slate-400 font-bold rounded-xl transition-all">
          Đăng xuất
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col h-screen overflow-hidden">
      
      <header class="h-20 bg-white border-b border-slate-200 flex items-center justify-between px-10 shrink-0">
        <h1 class="text-xl font-bold text-slate-800">Không gian làm việc</h1>
        <div class="flex items-center space-x-6">
          <div class="relative">
            <span class="absolute left-3 top-2.5 text-slate-400">🔍</span>
            <input v-model="searchQuery" type="text" placeholder="Tìm kiếm dự án..." class="w-64 pl-10 pr-4 py-2 bg-slate-100 border-none rounded-full text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
          </div>
          <div class="flex items-center space-x-3 border-l border-slate-200 pl-6">
            <div class="text-right">
              <p class="text-sm font-bold text-slate-700">{{ currentUser }}</p>
              <p class="text-xs text-slate-400 font-medium">Đang trực tuyến</p>
            </div>
            <div class="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold shadow-md uppercase">{{ firstLetter }}</div>
          </div>
        </div>
      </header>

      <div v-if="currentView === 'LIST'" class="p-10 overflow-y-auto h-full">
        
        <transition name="fade" mode="out-in">
          
          <div v-if="isCreating" key="formView">
            <CreateProjectForm 
              :isEdit="isEditMode" 
              :initialData="editData" 
              @project-created="handleFormSuccess" 
              @cancel="isCreating = false" 
            />
          </div>

          <div v-else key="listView">
            
            <div class="flex justify-between items-center mb-8">
              <div>
                <h2 class="text-3xl font-black text-slate-800">Tất cả Dự án</h2>
                <p class="text-slate-500 mt-1 font-medium">Chọn một dự án để xem chi tiết bảng công việc (Kanban).</p>
              </div>
              <button @click="openCreateForm" class="px-6 py-2.5 bg-slate-800 text-white font-bold rounded-xl shadow-lg hover:bg-slate-700 active:scale-95 transition-all">
                + Khởi tạo Dự án
              </button>
            </div>

            <div v-if="loading" class="text-center py-20 text-slate-400 font-bold">Đang tải dữ liệu...</div>
            
            <div v-else-if="filteredProjects.length === 0" class="text-center py-20">
              <div class="text-6xl mb-4">📭</div>
              <h3 class="text-xl font-bold text-slate-700">Chưa có dự án nào</h3>
            </div>

            <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
              <div v-for="p in filteredProjects" :key="p.id" class="bg-white rounded-3xl p-6 border transition-all duration-200 group relative" :class="starredProjects.includes(p.id) ? 'border-yellow-300 bg-yellow-50/10' : 'border-slate-200 hover:shadow-xl hover:border-blue-400'">
                
                <div class="absolute top-4 right-4">
                  <button @click.stop="toggleMenu(p.id)" class="p-2 text-slate-400 hover:bg-slate-100 rounded-lg transition-colors">
                    <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/></svg>
                  </button>
                  
                  <div v-if="activeMenu === p.id" class="absolute right-0 mt-2 w-48 bg-white rounded-2xl shadow-xl border border-slate-100 z-50 py-2 overflow-hidden">
                    <button v-if="['OWNER', 'MANAGER'].includes(p.myRole)" @click.stop="openEditForm(p)" class="w-full text-left px-5 py-3 hover:bg-slate-50 text-sm font-bold text-slate-700 flex items-center"><span class="mr-2">✏️</span> Chỉnh sửa</button>
                    <button @click.stop="openMemberModal(p)" class="w-full text-left px-5 py-3 hover:bg-slate-50 text-sm font-bold text-slate-700 flex items-center"><span class="mr-2">👥</span> Thành viên</button>
                    <button @click.stop="toggleStar(p.id)" class="w-full text-left px-5 py-3 hover:bg-slate-50 text-sm font-bold flex items-center" :class="starredProjects.includes(p.id) ? 'text-slate-500' : 'text-yellow-600'">
                      <span class="mr-2">⭐</span> {{ starredProjects.includes(p.id) ? 'Bỏ đánh dấu' : 'Đánh dấu sao' }}
                    </button>
                    <div v-if="p.myRole === 'OWNER'" class="my-1 border-t border-slate-100"></div>
                    <button v-if="p.myRole === 'OWNER'" @click.stop="deleteProject(p.id)" class="w-full text-left px-5 py-3 hover:bg-red-50 text-sm font-bold text-red-600 flex items-center"><span class="mr-2">🗑️</span> Xóa dự án</button>
                  </div>
                </div>

                <div class="flex space-x-2 mb-4">
                  <span :class="getPriorityClass(p.priority)" class="px-3 py-1.5 text-[10px] font-black uppercase tracking-widest rounded-lg border">{{ formatPriority(p.priority) }}</span>
                  <span v-if="p.myRole === 'OWNER'" class="px-3 py-1.5 bg-yellow-400 text-yellow-900 text-[10px] font-black rounded-lg border border-yellow-500 shadow-sm">👑 OWNER</span>
                  <span v-else-if="p.myRole === 'MANAGER'" class="px-3 py-1.5 bg-purple-500 text-white text-[10px] font-black rounded-lg shadow-sm">💼 MANAGER</span>
                  <span v-else class="px-3 py-1.5 bg-slate-200 text-slate-600 text-[10px] font-black rounded-lg border border-slate-300">👤 MEMBER</span>
                </div>

                <div @click="openBoard(p)" class="cursor-pointer">
                  <h3 class="text-xl font-extrabold text-slate-800 mb-2 group-hover:text-blue-600 transition-colors truncate">
                    {{ p.name }} <span v-if="starredProjects.includes(p.id)" class="text-yellow-400 ml-1 text-lg">★</span>
                  </h3>
                  <p class="text-slate-500 text-sm line-clamp-2 h-10 mb-6 leading-relaxed">{{ p.description || 'Chưa có mô tả ngắn cho dự án này.' }}</p>
                </div>

                <div class="mb-5">
                  <div class="flex justify-between items-end mb-2">
                    <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Tiến độ</span>
                    <span class="text-sm font-black" :class="getProgress(p.completedTasks, p.totalTasks) === 100 ? 'text-emerald-500' : 'text-blue-600'">
                      {{ getProgress(p.completedTasks, p.totalTasks) }}%
                    </span>
                  </div>
                  <div class="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                    <div :class="getProgress(p.completedTasks, p.totalTasks) === 100 ? 'bg-emerald-500' : 'bg-blue-600'" class="h-full rounded-full transition-all duration-1000 ease-out" :style="'width: ' + getProgress(p.completedTasks, p.totalTasks) + '%'"></div>
                  </div>
                </div>

                <div class="pt-4 border-t border-slate-100 flex justify-between items-center text-xs font-bold text-slate-500">
                  <div class="flex items-center space-x-1.5" :class="isDeadlineNear(p.endDate) ? 'text-red-500' : ''">
                    <span>⏳</span>
                    <span>{{ formatDate(p.endDate) }}</span>
                  </div>
                  <div class="flex items-center space-x-1.5 bg-slate-50 px-2 py-1 rounded-md border border-slate-200">
                    <span class="text-blue-500">☑️</span>
                    <span>{{ p.completedTasks }}/{{ p.totalTasks }} Task</span>
                  </div>
                </div>

              </div>
            </div>
          </div>
        </transition>
      </div>

      <div v-else class="flex flex-col h-full bg-[#f4f5f7]">
        <div class="px-10 py-6 shrink-0 bg-white border-b border-slate-200">
          <div class="flex items-center space-x-2 text-sm font-bold text-slate-400 mb-2 cursor-pointer w-fit hover:text-blue-600 transition-colors" @click="currentView = 'LIST'">
            <span>← Quay lại Danh sách Bảng</span>
          </div>
          
          <div class="flex justify-between items-end">
            <div>
              <h1 class="text-3xl font-black text-slate-800 tracking-tight">{{ boardProject.name }}</h1>
            </div>
            <div v-if="['OWNER', 'MANAGER'].includes(boardProject.myRole)" class="flex space-x-3">
              <button @click="openMemberModal(boardProject)" class="px-4 py-2 bg-blue-50 text-blue-600 font-bold rounded-xl hover:bg-blue-100 transition-all shadow-sm">
                👥 Quản lý Nhóm
              </button>
            </div>
          </div>
        </div>

        <div class="flex-1 p-10 flex items-center justify-center text-slate-400 font-bold text-xl">
          Khu vực Kanban Kéo Thả sẽ được tích hợp dữ liệu thật ở giai đoạn sau!
        </div>
      </div>

    </main>

    <div v-if="showMemberModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-md rounded-3xl shadow-2xl p-8">
        <h2 class="text-2xl font-bold text-slate-800 mb-6">Mời thành viên</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-bold text-slate-700 mb-2">Email người dùng</label>
            <input v-model="newMember.email" type="email" placeholder="nhanvien@teamwork.com" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none" />
          </div>
          <div>
            <label class="block text-sm font-bold text-slate-700 mb-2">Vai trò (Role)</label>
            <select v-model="newMember.role" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none cursor-pointer bg-white">
              <option value="MEMBER">👤 Thành viên (Chỉ xem và làm task)</option>
              <option value="MANAGER" v-if="selectedProject.myRole === 'OWNER'">💼 Quản lý (Sửa dự án, mời người)</option>
            </select>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-8">
          <button @click="showMemberModal = false" class="px-5 py-2.5 bg-slate-100 font-bold text-slate-600 rounded-xl hover:bg-slate-200">Hủy</button>
          <button @click="addMember" class="px-5 py-2.5 bg-blue-600 font-bold text-white rounded-xl hover:bg-blue-700 shadow-lg shadow-blue-500/30">Gửi lời mời</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import CreateProjectForm from "../components/CreateProjectForm.vue"; 
import { useToast } from "../composables/useToast";

const router = useRouter();
const { addToast } = useToast();

const currentUser = ref("Khách");
const firstLetter = ref("K");
const activeMenu = ref(null);

const currentView = ref('LIST');
const projects = ref([]);
const loading = ref(true);
const isCreating = ref(false);
const isEditMode = ref(false);
const editData = ref(null);
const searchQuery = ref("");

const boardProject = ref(null);
const showMemberModal = ref(false);
const selectedProject = ref({});
const newMember = ref({ email: '', role: 'MEMBER' });

const starredProjects = ref([]);

onMounted(() => {
  window.addEventListener('click', () => { activeMenu.value = null; });
  const storedUser = localStorage.getItem("username");
  if (storedUser) {
    currentUser.value = storedUser;
    firstLetter.value = storedUser.charAt(0).toUpperCase();
    
    const storedStars = localStorage.getItem(`starred_${storedUser}`);
    if (storedStars) starredProjects.value = JSON.parse(storedStars);
  }
  fetchProjects(); 
});

const toggleStar = (id) => {
  activeMenu.value = null; 
  if (starredProjects.value.includes(id)) {
    starredProjects.value = starredProjects.value.filter(starId => starId !== id);
    addToast("Đã bỏ đánh dấu sao!", "success");
  } else {
    starredProjects.value.push(id);
    addToast("Đã đánh dấu sao dự án!", "success");
  }
  localStorage.setItem(`starred_${currentUser.value}`, JSON.stringify(starredProjects.value));
};

const filteredProjects = computed(() => {
  return projects.value.filter(p => {
    const searchStr = searchQuery.value.toLowerCase();
    return p.name.toLowerCase().includes(searchStr) || 
           (p.description && p.description.toLowerCase().includes(searchStr));
  });
});

const fetchProjects = async () => {
  loading.value = true;
  try {
    const userId = localStorage.getItem("userId");
    const response = await fetch("http://localhost:8080/api/projects/list", {
      headers: { "User-ID": userId } 
    });
    if (!response.ok) throw new Error("Lỗi mạng");
    projects.value = await response.json();
  } catch (error) {
    addToast("Không thể tải danh sách dự án!", "error");
  } finally {
    loading.value = false;
  }
};

const toggleMenu = (id) => { activeMenu.value = activeMenu.value === id ? null : id; };

const openCreateForm = () => { isEditMode.value = false; editData.value = null; isCreating.value = true; };
const openEditForm = (project) => { activeMenu.value = null; isEditMode.value = true; editData.value = project; isCreating.value = true; };
const handleFormSuccess = () => { isCreating.value = false; fetchProjects(); };
const openBoard = (project) => { boardProject.value = project; currentView.value = 'BOARD'; };
const handleLogout = () => { localStorage.clear(); router.push("/"); };

const deleteProject = async (id) => {
  activeMenu.value = null;
  if (!confirm(`CẢNH BÁO: Bạn có chắc chắn muốn xóa vĩnh viễn dự án này không?`)) return;
  try {
    const userId = localStorage.getItem("userId");
    const res = await fetch(`http://localhost:8080/api/projects/delete`, { 
      method: 'POST', 
      headers: { 
        'Content-Type': 'application/json',
        'User-ID': userId 
      },
      body: JSON.stringify({ projectId: id }) 
    });
    const result = await res.json();
    if (res.ok) { addToast("Đã xóa dự án vĩnh viễn!", "success"); fetchProjects(); } 
    else { addToast(result.error, "error"); }
  } catch (error) { addToast("Lỗi máy chủ khi xóa!", "error"); }
};

const openMemberModal = (project) => { activeMenu.value = null; selectedProject.value = project; newMember.value = { email: '', role: 'MEMBER' }; showMemberModal.value = true; };
const addMember = async () => {
  if (!newMember.value.email) return addToast("Vui lòng nhập Email!", "warning");
  try {
    const payload = { projectId: selectedProject.value.id, email: newMember.value.email, role: newMember.value.role };
    const res = await fetch("http://localhost:8080/api/projects/add-member", { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    const result = await res.json();
    if (res.ok) { addToast(`Đã gửi lời mời đến ${newMember.value.email}`, "success"); showMemberModal.value = false; } 
    else { addToast(result.error, "error"); }
  } catch (error) { addToast("Lỗi khi thêm thành viên!", "error"); }
};

// UI FORMATTERS
const formatDate = (dateStr) => {
  if (!dateStr || dateStr === 'null') return 'Chưa có';
  const [year, month, day] = dateStr.split('-');
  return (year && month && day) ? `${day}/${month}/${year}` : dateStr;
};

const isDeadlineNear = (dateStr) => {
  if (!dateStr || dateStr === 'null') return false;
  const daysLeft = (new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24);
  return daysLeft > 0 && daysLeft <= 3; 
};

const getProgress = (completed, total) => {
  if (!total || total === 0) return 0;
  return Math.round((completed / total) * 100);
};

const formatPriority = (priority) => {
  if (priority === 'HIGH') return 'Cao';
  if (priority === 'MEDIUM') return 'Trung bình';
  return 'Thấp';
};

const getPriorityClass = (priority) => {
  if (priority === 'HIGH') return 'bg-red-100 text-red-700 border-red-200';
  if (priority === 'MEDIUM') return 'bg-orange-100 text-orange-700 border-orange-200';
  return 'bg-blue-100 text-blue-700 border-blue-200';
};
</script>

<style scoped>
/* Hiệu ứng chuyển cảnh Mở/Đóng Form */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.line-clamp-2 { 
  display: -webkit-box; 
  -webkit-line-clamp: 2; 
  line-clamp: 2;
  -webkit-box-orient: vertical; 
  overflow: hidden; 
}
</style>