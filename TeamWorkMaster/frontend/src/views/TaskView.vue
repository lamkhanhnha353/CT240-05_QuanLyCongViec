<template>
  <div class="min-h-screen bg-[#f8f9fa] flex font-sans">
    
    <aside class="w-64 bg-slate-900 text-white flex flex-col shadow-2xl z-20 shrink-0">
     <div class="p-6 border-b border-slate-800 flex items-center space-x-3 shrink-0">
        <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center font-black text-xl shadow-lg shadow-blue-500/30 border border-blue-400/20">P</div>
        
        <div>
          <h2 class="text-xl font-black tracking-tighter text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-indigo-300">TEAMWORK</h2>
          <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5">Master System</p>
        </div>
      </div>
      <nav class="flex-1 p-4 space-y-2">
        <router-link to="/dashboard" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
          <span>📊</span><span>Bảng điều khiển</span>
        </router-link>
        <router-link to="/projects" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
          <span>📂</span><span>Dự án</span>
        </router-link>
        <router-link to="/tasks" class="flex items-center space-x-3 px-4 py-3 bg-blue-600/10 text-blue-500 rounded-xl font-bold transition-all">
          <span>☑️</span><span>Công việc của tôi</span>
        </router-link>
        <router-link to="/account" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
          <span>👤</span><span>Tài khoản</span>
        </router-link>
      </nav>
      <div class="p-6">
        <button @click="handleLogout" class="w-full flex items-center justify-center py-3 bg-slate-800 hover:bg-red-500/20 hover:text-red-400 text-slate-400 font-bold rounded-xl transition-all">
          Đăng xuất
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col h-screen overflow-hidden bg-slate-50">
      
      <header class="h-20 bg-white border-b border-slate-200 flex items-center justify-between px-10 shrink-0 z-10 shadow-sm">
        <h1 class="text-xl font-bold text-slate-800">Không gian làm việc</h1>
        
        <div class="flex items-center space-x-6">
          <div class="relative flex items-center group">
            <span class="absolute left-3 top-2.5 text-slate-400">🔍</span>
            <input v-model="searchQuery" type="text" placeholder="Tìm kiếm công việc..." class="w-64 pl-10 pr-10 py-2 bg-slate-100 border-none rounded-full text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
          </div>

          <div class="relative" v-click-outside="() => showNotifMenu = false">
            <button @click="toggleNotifMenu" class="relative p-2 text-slate-400 hover:bg-slate-100 rounded-full transition-all focus:outline-none">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>
              <span v-if="unreadCount > 0" class="absolute top-1.5 right-1.5 w-4 h-4 bg-red-500 text-white text-[10px] font-bold flex items-center justify-center rounded-full border-2 border-white">{{ unreadCount }}</span>
            </button>

            <div v-if="showNotifMenu" class="absolute right-0 mt-3 w-80 bg-white rounded-2xl shadow-2xl border border-slate-100 z-50 overflow-hidden origin-top-right animate-fade-in">
              <div class="p-4 border-b border-slate-100 bg-slate-50 flex justify-between items-center">
                <h3 class="font-bold text-slate-800">Thông báo</h3>
                <span class="text-xs font-bold text-blue-600 bg-blue-100 px-2 py-1 rounded-md">{{ unreadCount }} mới</span>
              </div>
              <div class="max-h-[350px] overflow-y-auto p-6 text-center text-slate-400 text-sm font-medium">
                Chức năng thông báo
              </div>
            </div>
          </div>

          <div class="flex items-center space-x-3 border-l border-slate-300 pl-6">
            <div class="text-right">
              <p class="text-sm font-bold text-slate-700">{{ currentUser }}</p>
              <p class="text-xs text-blue-500 font-bold">Đang trực tuyến</p>
            </div>
            <div class="w-10 h-10 bg-gradient-to-tr from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-black shadow-md uppercase">{{ firstLetter }}</div>
          </div>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-10 custom-scrollbar">
        <div class="max-w-[1400px] mx-auto">
          
          <div class="flex flex-col md:flex-row justify-between items-start md:items-end gap-6 mb-8">
            <div>
              <h2 class="text-3xl font-black text-slate-800">Công việc của tôi</h2>
              <p class="text-slate-500 mt-1 font-medium">Quản lý các nhiệm vụ được giao trên tất cả các dự án.</p>
            </div>
            
            <div class="flex gap-4">
              <div class="bg-white px-5 py-3 rounded-2xl shadow-sm border border-slate-200 flex flex-col items-center min-w-[100px]">
                <span class="text-2xl font-black text-slate-800">{{ taskStats.total }}</span>
                <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tổng số</span>
              </div>
              <div class="bg-orange-50 px-5 py-3 rounded-2xl shadow-sm border border-orange-100 flex flex-col items-center min-w-[100px]">
                <span class="text-2xl font-black text-orange-600">{{ taskStats.inProgress }}</span>
                <span class="text-[10px] font-bold text-orange-500 uppercase tracking-widest">Đang làm</span>
              </div>
              <div class="bg-emerald-50 px-5 py-3 rounded-2xl shadow-sm border border-emerald-100 flex flex-col items-center min-w-[100px]">
                <span class="text-2xl font-black text-emerald-600">{{ taskStats.done }}</span>
                <span class="text-[10px] font-bold text-emerald-500 uppercase tracking-widest">Hoàn thành</span>
              </div>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-4 mb-8 bg-white p-4 rounded-2xl shadow-sm border border-slate-200">
            <span class="text-sm font-bold text-slate-500 uppercase tracking-widest mr-2">Bộ lọc trạng thái:</span>
            
            <div class="flex space-x-2">
              <button @click="filterStatus = 'ALL'" :class="filterStatus === 'ALL' ? 'bg-slate-800 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'" class="px-4 py-2 rounded-xl text-sm font-bold transition-colors">Tất cả</button>
              <button @click="filterStatus = 'TODO'" :class="filterStatus === 'TODO' ? 'bg-blue-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'" class="px-4 py-2 rounded-xl text-sm font-bold transition-colors">Cần làm</button>
              <button @click="filterStatus = 'IN_PROGRESS'" :class="filterStatus === 'IN_PROGRESS' ? 'bg-orange-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'" class="px-4 py-2 rounded-xl text-sm font-bold transition-colors">Đang thực hiện</button>
              <button @click="filterStatus = 'DONE'" :class="filterStatus === 'DONE' ? 'bg-emerald-500 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'" class="px-4 py-2 rounded-xl text-sm font-bold transition-colors">Đã hoàn thành</button>
            </div>

            <div class="h-6 w-px bg-slate-300 mx-2 hidden md:block"></div>
            
            <select v-model="sortBy" class="bg-slate-50 border border-slate-200 text-slate-700 px-4 py-2 rounded-xl text-sm font-bold outline-none focus:border-blue-500 cursor-pointer ml-auto">
              <option value="deadline">⏳ Gần hạn chót nhất</option>
              <option value="priority">🔥 Ưu tiên cao nhất</option>
              <option value="newest">🕒 Mới cập nhật</option>
            </select>
          </div>

          <div v-if="loading" class="text-center py-20">
            <div class="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-400 font-bold tracking-widest uppercase text-xs">Đang lấy dữ liệu công việc...</p>
          </div>
          
          <div v-else-if="filteredTasks.length === 0" class="text-center py-20 bg-white rounded-3xl border-2 border-dashed border-slate-200">
            <div class="text-6xl mb-4 opacity-50">🌴</div>
            <h3 class="text-xl font-bold text-slate-700 mb-2">Không có công việc nào</h3>
            <p class="text-slate-500 text-sm">Bạn đã xử lý xong hết mọi việc hoặc không tìm thấy kết quả.</p>
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            <div v-for="task in paginatedTasks" :key="task.id" class="bg-white rounded-3xl p-6 border shadow-sm hover:shadow-xl transition-all duration-300 relative overflow-hidden group cursor-pointer flex flex-col h-[240px]" :class="getCardBorder(task.status)">
              
              <div class="absolute left-0 top-0 bottom-0 w-1.5" :class="getPriorityColor(task.priority)"></div>

              <div class="flex justify-between items-start mb-3 pl-2">
                <div class="flex items-center space-x-2 max-w-[65%]">
                  <span class="text-lg">📁</span>
                  <span class="text-[10px] font-black text-slate-500 uppercase tracking-widest truncate">{{ task.projectName }}</span>
                </div>
                <span class="px-2.5 py-1 text-[9px] font-black uppercase tracking-wider rounded-lg shadow-sm" :class="getStatusBadge(task.status)">
                  {{ formatStatus(task.status) }}
                </span>
              </div>

              <div class="pl-2 flex-1">
                <h3 class="text-lg font-extrabold text-slate-800 mb-2 line-clamp-2 group-hover:text-blue-600 transition-colors" :title="task.title">{{ task.title }}</h3>
                <p class="text-xs text-slate-500 line-clamp-2 leading-relaxed">{{ task.description || 'Không có mô tả chi tiết.' }}</p>
              </div>

              <div class="mt-4 pt-4 border-t border-slate-100 flex justify-between items-center pl-2">
                <div class="flex items-center text-xs font-bold" :class="getDeadlineClass(task.endDate, task.status)">
                  <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                  {{ formatDate(task.endDate) }}
                  <span v-if="isOverdue(task.endDate) && task.status !== 'DONE'" class="ml-2 bg-red-100 text-red-600 px-1.5 py-0.5 rounded text-[8px] uppercase">Trễ hạn</span>
                </div>
                
                <div class="flex items-center space-x-1.5 bg-slate-50 px-2 py-1 rounded-lg border border-slate-200">
                  <span class="text-[10px] font-black text-slate-500 uppercase tracking-widest">Ưu tiên:</span>
                  <span class="text-[10px] font-black uppercase" :class="getTextPriorityColor(task.priority)">{{ formatPriority(task.priority) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="totalPages > 1" class="mt-10 flex justify-center items-center space-x-2">
            <button @click="currentPage--" :disabled="currentPage === 1" class="px-5 py-2.5 bg-white border border-slate-200 rounded-xl font-bold text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition-all shadow-sm">Trước</button>
            <div class="flex space-x-1.5">
              <button v-for="page in totalPages" :key="page" @click="currentPage = page" class="w-11 h-11 rounded-xl font-black text-sm transition-all shadow-sm flex items-center justify-center" :class="currentPage === page ? 'bg-blue-600 text-white' : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'">
                {{ page }}
              </button>
            </div>
            <button @click="currentPage++" :disabled="currentPage === totalPages" class="px-5 py-2.5 bg-white border border-slate-200 rounded-xl font-bold text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition-all shadow-sm">Sau</button>
          </div>

        </div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";


const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = function(event) {
      if (!(el === event.target || el.contains(event.target))) binding.value(event, el);
    };
    document.body.addEventListener('click', el.clickOutsideEvent);
  },
  unmounted(el) { document.body.removeEventListener('click', el.clickOutsideEvent); }
};

const router = useRouter();

const currentUser = ref("Khách");
const firstLetter = ref("K");
const loading = ref(true);

const tasks = ref([]);
const searchQuery = ref("");
const filterStatus = ref("ALL");
const sortBy = ref("deadline");

// Thông báo
const unreadCount = ref(0);
const showNotifMenu = ref(false);
const toggleNotifMenu = () => { showNotifMenu.value = !showNotifMenu.value; };

// Phân trang
const currentPage = ref(1);
const itemsPerPage = ref(9);

onMounted(() => {
  const storedUser = localStorage.getItem("username");
  if (storedUser) {
    currentUser.value = storedUser;
    firstLetter.value = storedUser.charAt(0).toUpperCase();
  }
  fetchMyTasks();
});

const handleLogout = () => {
  localStorage.clear();
  router.push("/");
};



const fetchMyTasks = async () => {
  loading.value = true;
  try {
 
    const userId = localStorage.getItem("userId");
    const response = await fetch(`http://localhost:8080/api/tasks/my-tasks`, { 
      headers: { "User-ID": userId } 
    });
    
    if (response.ok) {
        tasks.value = await response.json();
    } else {
        console.error("Lỗi lấy dữ liệu từ Server");
    }
    
    loading.value = false;
    
 
    
  } catch (error) {
    console.error("Lỗi:", error);
    loading.value = false;
  }
};


const taskStats = computed(() => {
  return {
    total: tasks.value.length,
    inProgress: tasks.value.filter(t => t.status === 'IN_PROGRESS').length,
    done: tasks.value.filter(t => t.status === 'DONE').length
  };
});

const filteredTasks = computed(() => {
  let result = tasks.value.filter(t => {
    // 1. Lọc theo chữ tìm kiếm
    const searchStr = searchQuery.value.toLowerCase();
    const matchSearch = t.title.toLowerCase().includes(searchStr) || t.projectName.toLowerCase().includes(searchStr);
    
    // 2. Lọc theo trạng thái
    const matchStatus = filterStatus.value === 'ALL' || t.status === filterStatus.value;
    
    return matchSearch && matchStatus;
  });

  // Sắp xếp
  result.sort((a, b) => {
    if (sortBy.value === 'deadline') {
      return new Date(a.endDate) - new Date(b.endDate); // Gần hạn chót lên đầu
    } else if (sortBy.value === 'priority') {
      const pMap = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 };
      return pMap[b.priority] - pMap[a.priority]; // Ưu tiên cao lên đầu
    }
    return b.id - a.id; // Mới nhất lên đầu
  });

  return result;
});

const totalPages = computed(() => Math.ceil(filteredTasks.value.length / itemsPerPage.value) || 1);

const paginatedTasks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  return filteredTasks.value.slice(start, start + itemsPerPage.value);
});

watch([searchQuery, filterStatus, sortBy], () => { currentPage.value = 1; });

const formatDate = (dateStr) => {
  if (!dateStr || dateStr === 'null') return 'Chưa có hạn';
  const [year, month, day] = dateStr.split('-');
  return (year && month && day) ? `${day}/${month}/${year}` : dateStr;
};

const isOverdue = (dateStr) => {
  if (!dateStr || dateStr === 'null') return false;
  return new Date(dateStr) < new Date(new Date().setHours(0,0,0,0));
};

const formatStatus = (status) => {
  const map = { 'TODO': 'Cần làm', 'IN_PROGRESS': 'Đang thực hiện', 'DONE': 'Đã hoàn thành', 'CANCELED': 'Đã hủy' };
  return map[status] || status;
};

const formatPriority = (priority) => {
  const map = { 'HIGH': 'Cao', 'MEDIUM': 'Trung bình', 'LOW': 'Thấp' };
  return map[priority] || priority;
};

// --- GIAO DIỆN COLORS ---
const getPriorityColor = (priority) => {
  if (priority === 'HIGH') return 'bg-red-500';
  if (priority === 'MEDIUM') return 'bg-orange-500';
  return 'bg-blue-500';
};

const getTextPriorityColor = (priority) => {
  if (priority === 'HIGH') return 'text-red-500';
  if (priority === 'MEDIUM') return 'text-orange-500';
  return 'text-blue-500';
};

const getStatusBadge = (status) => {
  if (status === 'TODO') return 'bg-blue-100 text-blue-600 border border-blue-200';
  if (status === 'IN_PROGRESS') return 'bg-orange-100 text-orange-600 border border-orange-200';
  if (status === 'DONE') return 'bg-emerald-100 text-emerald-600 border border-emerald-200';
  if (status === 'CANCELED') return 'bg-slate-100 text-slate-500 border border-slate-200';
};

const getCardBorder = (status) => {
  if (status === 'DONE') return 'border-emerald-100 hover:border-emerald-300 bg-emerald-50/10';
  if (status === 'CANCELED') return 'border-slate-200 hover:border-slate-300 opacity-70';
  return 'border-slate-200 hover:border-blue-300';
};

const getDeadlineClass = (dateStr, status) => {
  if (status === 'DONE' || status === 'CANCELED') return 'text-slate-400';
  if (isOverdue(dateStr)) return 'text-red-600';
  return 'text-slate-600';
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
</style>