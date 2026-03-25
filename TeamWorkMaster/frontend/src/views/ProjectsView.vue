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
        <router-link to="/projects" class="flex items-center space-x-3 px-4 py-3 bg-blue-600/10 text-blue-500 rounded-xl font-bold transition-all">
          <span>📂</span><span>Dự án</span>
        </router-link>
        <router-link to="/tasks" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
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
      
      <header class="h-20 bg-white border-b border-slate-200 flex items-center justify-between px-10 shrink-0 z-10">
        <h1 class="text-xl font-bold text-slate-800">Không gian làm việc</h1>
        
        <div class="flex items-center space-x-6">
          <div class="relative flex items-center group">
            <span class="absolute left-3 top-2.5 text-slate-400">🔍</span>
           <input ref="searchInput" v-model="searchQuery" type="text" placeholder="Tìm kiếm dự án (Ctrl+K)..." class="w-64 pl-10 pr-10 py-2 bg-slate-100 border-none rounded-full text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
            <button @click="startVoiceSearch" class="absolute right-3 top-2 flex items-center transition-colors focus:outline-none" :class="isListening ? 'text-red-500 animate-pulse' : 'text-slate-400 hover:text-blue-500'" title="Tìm bằng giọng nói">
              <svg v-if="!isListening" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"></path></svg>
              <span v-else class="text-lg">🎙️</span>
            </button>
          </div>

          <div class="relative" v-click-outside="() => showNotifMenu = false">
            <button @click="toggleNotifMenu" class="relative p-2 text-slate-400 hover:bg-slate-100 rounded-full transition-all">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>
              <span v-if="unreadCount > 0" class="absolute top-1.5 right-1.5 w-4 h-4 bg-red-500 text-white text-[10px] font-bold flex items-center justify-center rounded-full border-2 border-white">{{ unreadCount }}</span>
            </button>

            <div v-if="showNotifMenu" class="absolute right-0 mt-3 w-80 bg-white rounded-2xl shadow-2xl border border-slate-100 z-50 overflow-hidden origin-top-right animate-fade-in">
              <div class="p-4 border-b border-slate-100 bg-slate-50 flex justify-between items-center">
                <h3 class="font-bold text-slate-800">Thông báo</h3>
                <span class="text-xs font-bold text-blue-600 bg-blue-100 px-2 py-1 rounded-md">{{ unreadCount }} mới</span>
              </div>
              <div class="max-h-[350px] overflow-y-auto">
                <div v-if="notifications.length === 0" class="p-6 text-center text-slate-400 text-sm font-medium">
                  Bạn không có thông báo nào.
                </div>
                <div v-for="notif in notifications" :key="notif.id" class="p-4 border-b border-slate-50 transition-colors relative group" :class="notif.isRead ? 'bg-white opacity-60' : 'bg-blue-50/50 hover:bg-blue-50'">
                  
                  <button @click.stop="deleteNotif(notif.id)" class="absolute top-2 right-2 p-1 text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-md opacity-0 group-hover:opacity-100 transition-all" title="Xóa thông báo">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                  </button>

                  <p class="text-sm font-bold text-slate-800 mb-1 pr-6">{{ notif.title }}</p>
                  <p class="text-xs text-slate-600 leading-relaxed mb-3">{{ notif.message }}</p>
                  
                  <div v-if="!notif.isRead">
                    <div v-if="notif.title === 'Lời mời dự án mới'" class="flex space-x-2">
                      <button @click="respondInvite(notif, true)" class="flex-1 py-1.5 bg-blue-600 text-white text-xs font-bold rounded-lg hover:bg-blue-700 transition-all">Đồng ý</button>
                      <button @click="respondInvite(notif, false)" class="flex-1 py-1.5 bg-slate-200 text-slate-700 text-xs font-bold rounded-lg hover:bg-slate-300 transition-all">Từ chối</button>
                    </div>
                    <div v-else class="flex justify-end">
                      <button @click="markAsRead(notif.id)" class="px-4 py-1.5 bg-slate-200 text-slate-600 text-xs font-bold rounded-lg hover:bg-slate-300 hover:text-slate-800 transition-all">
                        Đã hiểu
                      </button>
                    </div>
                  </div>

                  <div v-else class="text-[10px] font-bold text-slate-400 uppercase tracking-widest flex items-center">
                    <span class="mr-1">✓</span> Đã xử lý
                  </div>
                </div>
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

      <div class="p-10 overflow-y-auto h-full custom-scrollbar">
        <transition name="fade" mode="out-in">
          
          <div v-if="isCreating" key="formView">
            <CreateProjectForm :isEdit="isEditMode" :initialData="editData" @project-created="handleFormSuccess" @cancel="isCreating = false" />
          </div>

          <div v-else key="listView">
            
            <div class="flex justify-between items-center mb-6">
              <div>
                <h2 class="text-3xl font-black text-slate-800">Tất cả Dự án</h2>
                <p class="text-slate-500 mt-1 font-medium">Chọn dự án để vào không gian làm việc. Các dự án đang chờ xác nhận sẽ không hiển thị ở đây.</p>
              </div>
              <button @click="openCreateForm" class="px-6 py-3 bg-slate-800 text-white font-bold rounded-xl shadow-lg hover:bg-slate-700 active:scale-95 transition-all whitespace-nowrap">
                + Khởi tạo Dự án
              </button>
            </div>

            <div class="flex flex-wrap items-center gap-4 mb-8 bg-white p-4 rounded-2xl shadow-sm border border-slate-200">
              <span class="text-sm font-bold text-slate-500 uppercase tracking-widest mr-2">Bộ lọc:</span>
              
              <select v-model="filterPriority" class="bg-slate-50 border border-slate-200 text-slate-700 px-4 py-2 rounded-xl text-sm font-bold outline-none focus:border-blue-500 cursor-pointer">
                <option value="ALL">Mọi ưu tiên</option>
                <option value="HIGH">🔴 Ưu tiên Cao</option>
                <option value="MEDIUM">🟠 Ưu tiên Trung bình</option>
                <option value="LOW">🔵 Ưu tiên Thấp</option>
              </select>

              <select v-model="filterRole" class="bg-slate-50 border border-slate-200 text-slate-700 px-4 py-2 rounded-xl text-sm font-bold outline-none focus:border-blue-500 cursor-pointer">
                <option value="ALL">Mọi vai trò</option>
                <option value="OWNER">👑 Trưởng dự án (Owner)</option>
                <option value="MANAGER">💼 Quản lý (Manager)</option>
                <option value="MEMBER">👤 Thành viên (Member)</option>
              </select>

              <div class="h-6 w-px bg-slate-300 mx-2"></div> <span class="text-sm font-bold text-slate-500 uppercase tracking-widest mr-2">Sắp xếp:</span>
              <select v-model="sortBy" class="bg-slate-50 border border-slate-200 text-slate-700 px-4 py-2 rounded-xl text-sm font-bold outline-none focus:border-blue-500 cursor-pointer">
                <option value="newest">🕒 Mới nhất (Mặc định)</option>
                <option value="name_asc">🔤 Tên A-Z</option>
                <option value="name_desc">🔤 Tên Z-A</option>
              </select>

              <button v-if="filterPriority !== 'ALL' || filterRole !== 'ALL' || sortBy !== 'newest'" @click="resetFilters" class="ml-auto text-sm text-red-500 font-bold hover:underline">
                Xóa bộ lọc
              </button>
            </div>

            <div v-if="loading" class="text-center py-20 text-slate-400 font-bold">Đang tải dữ liệu...</div>
            <div v-else-if="filteredProjects.length === 0" class="text-center py-20">
              <div class="text-6xl mb-4">📭</div>
              <h3 class="text-xl font-bold text-slate-700">Chưa có dự án nào</h3>
            </div>

            <div v-else>
              <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                <div v-for="p in paginatedProjects" :key="p.id" class="bg-white rounded-3xl p-6 border transition-all duration-200 group relative" :class="starredProjects.includes(p.id) ? 'border-yellow-300 bg-yellow-50/10' : 'border-slate-200 hover:shadow-xl hover:border-blue-400'">
                  
                  <div class="absolute top-4 right-4 z-10">
                    <button @click.stop="toggleMenu(p.id)" class="p-2 text-slate-400 hover:bg-slate-100 rounded-lg transition-colors">
                      <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/></svg>
                    </button>
                    <div v-if="activeMenu === p.id" class="absolute right-0 mt-2 w-48 bg-white rounded-2xl shadow-xl border border-slate-100 z-50 py-2 overflow-hidden">
                      <button v-if="['OWNER', 'MANAGER'].includes(p.myRole)" @click.stop="openEditForm(p)" class="w-full text-left px-5 py-3 hover:bg-slate-50 text-sm font-bold text-slate-700 flex items-center"><span class="mr-2">✏️</span> Chỉnh sửa</button>
                      <button @click.stop="toggleStar(p.id)" class="w-full text-left px-5 py-3 hover:bg-slate-50 text-sm font-bold flex items-center" :class="starredProjects.includes(p.id) ? 'text-slate-500' : 'text-yellow-600'">
                        <span class="mr-2">⭐</span> {{ starredProjects.includes(p.id) ? 'Bỏ đánh dấu' : 'Đánh dấu sao' }}
                      </button>
                      <div v-if="p.myRole === 'OWNER'" class="my-1 border-t border-slate-100"></div>
                      <button v-if="p.myRole === 'OWNER'" @click.stop="deleteProject(p.id)" class="w-full text-left px-5 py-3 hover:bg-red-50 text-sm font-bold text-red-600 flex items-center"><span class="mr-2">🗑️</span> Xóa dự án</button>
                    </div>
                  </div>

                  <div @click="goToBoard(p)" class="cursor-pointer">
                    <div class="flex space-x-2 mb-4">
                      <span :class="getPriorityClass(p.priority)" class="px-3 py-1.5 text-[10px] font-black uppercase tracking-widest rounded-lg border">{{ formatPriority(p.priority) }}</span>
                      <span v-if="p.myRole === 'OWNER'" class="px-3 py-1.5 bg-yellow-400 text-yellow-900 text-[10px] font-black rounded-lg border border-yellow-500 shadow-sm">👑 OWNER</span>
                      <span v-else-if="p.myRole === 'MANAGER'" class="px-3 py-1.5 bg-purple-500 text-white text-[10px] font-black rounded-lg shadow-sm">💼 MANAGER</span>
                      <span v-else class="px-3 py-1.5 bg-slate-200 text-slate-600 text-[10px] font-black rounded-lg border border-slate-300">👤 MEMBER</span>
                    </div>

                    <h3 class="text-xl font-extrabold text-slate-800 mb-2 group-hover:text-blue-600 transition-colors truncate">
                      {{ p.name }} <span v-if="starredProjects.includes(p.id)" class="text-yellow-400 ml-1 text-lg">★</span>
                    </h3>
                    <p class="text-slate-500 text-sm line-clamp-2 h-10 mb-6 leading-relaxed">{{ p.description || 'Chưa có mô tả ngắn cho dự án này.' }}</p>

                    <div class="mb-5">
                      <div class="flex justify-between items-end mb-2">
                        <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Tiến độ</span>
                        <span class="text-sm font-black" :class="getProgress(p.completedTasks, p.totalTasks) === 100 ? 'text-emerald-500' : 'text-blue-600'">{{ getProgress(p.completedTasks, p.totalTasks) }}%</span>
                      </div>
                      <div class="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                        <div :class="getProgress(p.completedTasks, p.totalTasks) === 100 ? 'bg-emerald-500' : 'bg-blue-600'" class="h-full rounded-full transition-all duration-1000 ease-out" :style="'width: ' + getProgress(p.completedTasks, p.totalTasks) + '%'"></div>
                      </div>
                    </div>

                    <div class="pt-4 border-t border-slate-100 flex justify-between items-center text-xs font-bold text-slate-500">
                      <div class="flex items-center space-x-1.5" :class="isDeadlineNear(p.endDate) ? 'text-red-500' : ''"><span>⏳</span><span>{{ formatDate(p.endDate) }}</span></div>
                      <div class="flex items-center space-x-1.5 bg-slate-50 px-2 py-1 rounded-md border border-slate-200"><span class="text-blue-500">☑️</span><span>{{ p.completedTasks }}/{{ p.totalTasks }} Task</span></div>
                    </div>
                  </div>

                </div>
              </div>

              <div v-if="totalPages > 1" class="mt-12 flex justify-center items-center space-x-3">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="px-5 py-2.5 bg-white border border-slate-200 rounded-xl font-bold text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition-all shadow-sm">Trước</button>
                <div class="flex space-x-1.5">
                  <button v-for="page in totalPages" :key="page" @click="changePage(page)" class="w-11 h-11 rounded-xl font-black text-sm transition-all shadow-sm flex items-center justify-center" :class="currentPage === page ? 'bg-slate-800 text-white' : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'">
                    {{ page }}
                  </button>
                </div>
                <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages" class="px-5 py-2.5 bg-white border border-slate-200 rounded-xl font-bold text-slate-600 hover:bg-slate-50 disabled:opacity-40 transition-all shadow-sm">Sau</button>
              </div>

            </div>

          </div>
        </transition>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRouter } from "vue-router";
import CreateProjectForm from "../components/CreateProjectForm.vue"; 
import { useToast } from "../composables/useToast";

const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = function(event) {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event, el);
      }
    };
    document.body.addEventListener('click', el.clickOutsideEvent);
  },
  unmounted(el) { document.body.removeEventListener('click', el.clickOutsideEvent); }
};

const router = useRouter();
const { addToast } = useToast();

const currentUser = ref("Khách");
const firstLetter = ref("K");
const activeMenu = ref(null);

const projects = ref([]);
const loading = ref(true);
const isCreating = ref(false);
const isEditMode = ref(false);
const editData = ref(null);
const searchQuery = ref("");
const searchInput = ref(null);
const starredProjects = ref([]);

// VOICE SEARCH
const isListening = ref(false);


const notifications = ref([]);
const showNotifMenu = ref(false);
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length);

// BỘ LỌC & SẮP XẾP MỚI
const filterPriority = ref('ALL');
const filterRole = ref('ALL');
const sortBy = ref('newest'); // Mặc định hiển thị dự án mới nhất lên đầu

// PHÂN TRANG (9 DỰ ÁN MỖI TRANG)
const currentPage = ref(1);
const itemsPerPage = ref(9); 

const resetFilters = () => {
  filterPriority.value = 'ALL';
  filterRole.value = 'ALL';
  sortBy.value = 'newest';
  currentPage.value = 1;
};


// XỬ LÝ PHÍM TẮT (KEYBOARD SHORTCUTS)

const handleGlobalKeydown = (e) => {
  // Bấm ESC: Đóng form tạo dự án & Menu thông báo
  if (e.key === 'Escape') {
    isCreating.value = false;
    showNotifMenu.value = false;
    if (searchInput.value) searchInput.value.blur(); 
    return;
  }

  // Ctrl + K hoặc phím / : Mở ô tìm kiếm
  if ((e.ctrlKey && e.key === 'k') || (e.key === '/' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA')) {
    e.preventDefault(); 
    if (searchInput.value) searchInput.value.focus();
    return;
  }

  // Alt + N : Bật form tạo dự án mới
  if (e.altKey && e.key.toLowerCase() === 'n') {
    e.preventDefault();
    openCreateForm();
    return;
  }
};

onMounted(() => {
  const storedUser = localStorage.getItem("username");
  if (storedUser) {
    currentUser.value = storedUser;
    firstLetter.value = storedUser.charAt(0).toUpperCase();
    const storedStars = localStorage.getItem(`starred_${storedUser}`);
    if (storedStars) starredProjects.value = JSON.parse(storedStars);
  }
  fetchProjects(); 
  fetchNotifications();
  setInterval(fetchNotifications, 10000); 
});

onMounted(() => {
  const storedUser = localStorage.getItem("username");
  if (storedUser) {
    currentUser.value = storedUser;
    firstLetter.value = storedUser.charAt(0).toUpperCase();
    const storedStars = localStorage.getItem(`starred_${storedUser}`);
    if (storedStars) starredProjects.value = JSON.parse(storedStars);
  }
  fetchProjects(); 
  fetchNotifications();
  setInterval(fetchNotifications, 10000); 

  // Bật lắng nghe phím tắt khi vào trang
  window.addEventListener('keydown', handleGlobalKeydown);
});

// Tắt lắng nghe phím tắt khi chuyển sang trang khác (Chống lỗi bộ nhớ)
onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalKeydown);
});

const startVoiceSearch = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) { alert("Trình duyệt không hỗ trợ tìm kiếm giọng nói. Vui lòng dùng Chrome!"); return; }
  
  if (isListening.value) return; 
  const recognition = new SpeechRecognition();
  recognition.lang = 'vi-VN';
  
  recognition.onstart = () => { isListening.value = true; };
  recognition.onresult = (event) => {
    searchQuery.value = event.results[0][0].transcript.replace(/[.,!?]$/, '').trim();
  };
  recognition.onerror = () => { isListening.value = false; };
  recognition.onend = () => { isListening.value = false; };
  
  recognition.start();
};

const goToBoard = (project) => {
  router.push({
    path: `/board/${project.id}`,
    query: { projectName: project.name, role: project.myRole } 
  });
};

const toggleNotifMenu = () => { showNotifMenu.value = !showNotifMenu.value; };

const fetchNotifications = async () => {
  const userId = localStorage.getItem("userId");
  if (!userId) return;
  try {
    const response = await fetch("http://localhost:8080/api/notifications/list", { headers: { "User-ID": userId } });
    if (response.ok) notifications.value = await response.json();
  } catch (error) { console.error("Lỗi fetch thông báo", error); }
};

const markAsRead = async (notifId) => {
  const userId = localStorage.getItem("userId");
  try {
    const response = await fetch(`http://localhost:8080/api/notifications/read?id=${notifId}`, {
      method: "PUT",
      headers: { "User-ID": userId }
    });
    if (response.ok) {
      fetchNotifications(); 
    }
  } catch (error) {
    console.error("Lỗi cập nhật trạng thái thông báo", error);
  }
};

const deleteNotif = async (notifId) => {
  const userId = localStorage.getItem("userId");
  try {
    const response = await fetch(`http://localhost:8080/api/notifications/delete?id=${notifId}`, {
      method: "DELETE",
      headers: { "User-ID": userId }
    });
    if (response.ok) {
      fetchNotifications(); // Cập nhật lại danh sách liền
    }
  } catch (error) {
    console.error("Lỗi xóa thông báo", error);
  }
};

const respondInvite = async (notif, isAccept) => {
  const userId = localStorage.getItem("userId");
  try {
    const payload = { notificationId: notif.id, projectId: notif.projectId, isAccept: isAccept };
    const response = await fetch("http://localhost:8080/api/notifications/respond", {
      method: "POST",
      headers: { "Content-Type": "application/json", "User-ID": userId },
      body: JSON.stringify(payload)
    });
    if (response.ok) {
      addToast(isAccept ? "Đã tham gia dự án thành công!" : "Đã từ chối lời mời", "success");
      fetchNotifications();
      if(isAccept) fetchProjects(); 
    }
  } catch (error) { addToast("Lỗi xử lý hệ thống!", "error"); }
};

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

// 🟢 TÍNH TOÁN LỌC, SẮP XẾP
const filteredProjects = computed(() => {
  let result = projects.value.filter(p => {
    // 1. Tìm kiếm
    const searchStr = searchQuery.value.toLowerCase();
    const matchSearch = p.name.toLowerCase().includes(searchStr) || (p.description && p.description.toLowerCase().includes(searchStr));
    
    // 2. Lọc Ưu tiên
    const matchPriority = filterPriority.value === 'ALL' || p.priority === filterPriority.value;
    
    // 3. Lọc Vai trò
    const matchRole = filterRole.value === 'ALL' || p.myRole === filterRole.value;

    return matchSearch && matchPriority && matchRole;
  });

  // 4. Sắp xếp (DỰ ÁN MỚI TẠO LÊN ĐẦU TIÊN KHI CHỌN 'NEWEST')
  result.sort((a, b) => {
    if (sortBy.value === 'newest') {
      return b.id - a.id; 
    } else if (sortBy.value === 'name_asc') {
      return a.name.localeCompare(b.name);
    } else if (sortBy.value === 'name_desc') {
      return b.name.localeCompare(a.name);
    }
    return 0;
  });

  return result;
});

// PHÂN TRANG
const totalPages = computed(() => Math.ceil(filteredProjects.value.length / itemsPerPage.value) || 1);

const paginatedProjects = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  return filteredProjects.value.slice(start, start + itemsPerPage.value);
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) currentPage.value = page;
};

// Theo dõi khi filter thay đổi thì trả về trang 1
watch([searchQuery, filterPriority, filterRole, sortBy], () => {
  currentPage.value = 1;
});

// Chống lỗi khi totalPages bị thu nhỏ
watch(filteredProjects, () => {
  if (currentPage.value > totalPages.value) currentPage.value = totalPages.value || 1;
});

const fetchProjects = async () => {
  loading.value = true;
  try {
    const userId = localStorage.getItem("userId");
    const response = await fetch("http://localhost:8080/api/projects/list", { headers: { "User-ID": userId } });
    if (!response.ok) throw new Error("Lỗi mạng");
    projects.value = await response.json();
  } catch (error) { addToast("Không thể tải danh sách dự án!", "error"); } 
  finally { loading.value = false; }
};

const toggleMenu = (id) => { activeMenu.value = activeMenu.value === id ? null : id; };

//  MỞ FORM (TẠO MỚI & SỬA) ĐÃ FIX LỖI KHÔNG SỬA ĐƯỢC
const openCreateForm = () => { 
  isEditMode.value = false; 
  editData.value = null; 
  isCreating.value = true; 
};

const openEditForm = (project) => { 
  activeMenu.value = null; 
  isEditMode.value = true; 
 
  editData.value = JSON.parse(JSON.stringify(project)); 
  isCreating.value = true; 
};

const handleFormSuccess = () => { isCreating.value = false; fetchProjects(); };
const handleLogout = () => { localStorage.clear(); router.push("/"); };

const deleteProject = async (id) => {
  activeMenu.value = null;
  if (!confirm(`CẢNH BÁO: Bạn có chắc chắn muốn xóa vĩnh viễn dự án này không?`)) return;
  try {
    const userId = localStorage.getItem("userId");
    const res = await fetch(`http://localhost:8080/api/projects/delete`, { method: 'POST', headers: { 'Content-Type': 'application/json', 'User-ID': userId }, body: JSON.stringify({ projectId: id }) });
    if (res.ok) { addToast("Đã xóa dự án vĩnh viễn!", "success"); fetchProjects(); } else { const r = await res.json(); addToast(r.error, "error"); }
  } catch (error) { addToast("Lỗi máy chủ khi xóa!", "error"); }
};

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
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(10px); }
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
</style>