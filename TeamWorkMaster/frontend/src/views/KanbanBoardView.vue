<template>
  <div class="min-h-screen bg-[#f4f5f7] dark:bg-slate-900 flex font-sans text-slate-800 dark:text-slate-200 transition-colors duration-300 overflow-hidden h-screen">
    
    <aside :class="isSidebarOpen ? 'w-64' : 'w-20'" class="bg-slate-900 dark:bg-[#0f172a] text-white flex flex-col shadow-2xl z-20 shrink-0 transition-all duration-300 relative border-r border-slate-800 dark:border-slate-800/50">
      <div class="h-16 flex items-center border-b border-slate-800 dark:border-slate-800/50" :class="isSidebarOpen ? 'px-6 justify-between' : 'justify-center'">
        <div v-if="isSidebarOpen" class="flex flex-col overflow-hidden">
          <h2 class="text-xl font-black text-blue-500 tracking-tighter whitespace-nowrap">TEAMWORK</h2>
          <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5 whitespace-nowrap">Master System</p>
        </div>
        <button @click="toggleSidebar" class="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors focus:outline-none">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
        </button>
      </div>

      <nav class="flex-1 py-6 space-y-2 overflow-y-auto custom-scrollbar" :class="isSidebarOpen ? 'px-4' : 'px-2'">
        <router-link to="/dashboard" class="flex items-center py-3 rounded-xl font-bold text-slate-400 hover:bg-slate-800 hover:text-white transition-all group" :class="isSidebarOpen ? 'px-4' : 'justify-center px-0'">
          <svg class="w-6 h-6 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Bảng điều khiển</span>
        </router-link>
        <router-link to="/projects" class="flex items-center py-3 rounded-xl font-bold text-slate-400 hover:bg-slate-800 hover:text-white transition-all group" :class="isSidebarOpen ? 'px-4' : 'justify-center px-0'">
          <svg class="w-6 h-6 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"></path></svg>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Quản lý Dự án</span>
        </router-link>

        <div v-if="isSidebarOpen" class="mt-8 mb-2 px-4 text-[10px] font-black text-slate-500 uppercase tracking-widest border-t border-slate-800 pt-6">Dự án: {{ projectName }}</div>
        
        <button @click="currentTab = 'board'" class="w-full flex items-center py-3 rounded-xl font-bold transition-all group" :class="[isSidebarOpen ? 'px-4' : 'justify-center px-0', currentTab === 'board' ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/50' : 'text-slate-400 hover:bg-slate-800 hover:text-white']">
          <span class="text-xl shrink-0">📌</span>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Bảng Công việc</span>
        </button>

        <button @click="currentTab = 'chat'" class="w-full flex items-center py-3 rounded-xl font-bold transition-all group mt-2" :class="[isSidebarOpen ? 'px-4' : 'justify-center px-0', currentTab === 'chat' ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-900/50' : 'text-slate-400 hover:bg-slate-800 hover:text-white']">
          <span class="text-xl shrink-0">💬</span>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Thảo Luận Chung</span>
        </button>

        <button @click="currentTab = 'meeting'" class="w-full flex items-center py-3 rounded-xl font-bold transition-all group mt-2" :class="[isSidebarOpen ? 'px-4' : 'justify-center px-0', currentTab === 'meeting' ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-900/50' : 'text-slate-400 hover:bg-slate-800 hover:text-white']">
          <span class="text-xl shrink-0">📹</span>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Phòng Họp Nhóm</span>
        </button>
      </nav>

      <div class="p-4 border-t border-slate-800 space-y-2">
        <button @click="toggleTheme" class="w-full flex items-center py-3 rounded-xl font-bold text-amber-300 hover:bg-slate-800 transition-all" :class="isSidebarOpen ? 'px-4' : 'justify-center px-0'">
          <span v-if="!isDarkMode" class="text-xl shrink-0">🌙</span>
          <span v-else class="text-xl shrink-0">☀️</span>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">{{ isDarkMode ? 'Chế độ Sáng' : 'Chế độ Tối' }}</span>
        </button>
        <button @click="handleLogout" class="w-full flex items-center py-3 rounded-xl font-bold text-red-400 hover:bg-red-500/10 transition-all" :class="isSidebarOpen ? 'px-4' : 'justify-center px-0'">
          <span class="text-xl shrink-0">🚪</span>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col min-w-0 transition-colors duration-300 relative">
      
      <header class="h-16 bg-white dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between px-8 shrink-0 z-10 shadow-sm transition-colors duration-300">
        <div class="flex items-center space-x-4">
          <button @click="$router.push('/projects')" class="p-2 text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-full transition-colors shrink-0" title="Trở về danh sách dự án">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
          </button>
          <div class="h-6 w-px bg-slate-200 dark:bg-slate-700 shrink-0"></div>
          <h1 class="text-xl font-black text-slate-800 dark:text-white tracking-tight truncate">{{ currentTab === 'board' ? projectName : `Phòng Họp: ${projectName}` }}</h1>
          <span v-if="currentTab === 'board'" class="px-2.5 py-1 bg-slate-100 dark:bg-slate-700 text-slate-500 dark:text-slate-300 text-[10px] font-bold uppercase rounded-md border border-slate-200 dark:border-slate-600 shrink-0">{{ userRole }}</span>
          <span v-if="currentTab === 'meeting'" class="px-2.5 py-1 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 text-[10px] font-bold uppercase rounded-md shrink-0">LIVE</span>
        </div>
        
        <div class="flex space-x-3 shrink-0 pl-4">
          <button v-if="['OWNER', 'MANAGER'].includes(userRole) && currentTab === 'board'" @click="showMemberModal = true" class="px-4 py-2 bg-blue-50 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 text-sm font-bold rounded-xl hover:bg-blue-100 dark:hover:bg-blue-500/20 transition-all border border-blue-100 flex items-center">
            <span class="mr-2">👥</span> Mời Thành Viên
          </button>
        </div>
      </header>

     <KanbanBoard v-if="currentTab === 'board'" :projectId="projectId" :userRole="userRole" />
      
      <MeetingRoom v-if="currentTab === 'meeting'" :projectId="projectId" :projectName="projectName" :userRole="userRole" />
      
      <ProjectChat v-if="currentTab === 'chat'" :projectId="projectId" />

    </main>

    <div v-if="showMemberModal" class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white dark:bg-slate-800 w-full max-w-md rounded-3xl shadow-2xl p-8 relative overflow-visible animate-fade-in text-slate-800 dark:text-white">
        <h2 class="text-2xl font-bold mb-2">Mời vào dự án</h2>
        <div class="space-y-5">
          <div class="relative" v-click-outside="() => showDropdown = false">
            <label class="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-2">Tìm Username hoặc Email</label>
            <input v-model="memberSearchQuery" @input="handleSearchInput" @focus="showDropdown = searchResults.length > 0" type="text" autocomplete="off" placeholder="Gõ tên hoặc email..." class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 dark:bg-slate-900 dark:text-white" />
            <div v-if="showDropdown && searchResults.length > 0" class="absolute w-full mt-2 bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 rounded-xl shadow-xl z-50 max-h-60 overflow-y-auto">
              <div v-for="user in searchResults" :key="user.id" @click="selectUser(user)" class="flex items-center p-3 hover:bg-blue-50 dark:hover:bg-slate-700 cursor-pointer border-b border-slate-50 dark:border-slate-700/50 transition-colors">
                <div><p class="text-sm font-bold">{{ user.fullName }}</p><p class="text-xs text-slate-500">{{ user.email }}</p></div>
              </div>
            </div>
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase mb-2">Quyền hạn</label>
            <select v-model="newMemberRole" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 dark:text-white text-sm"><option value="MEMBER">Thành viên</option><option value="MANAGER" v-if="userRole === 'OWNER'">Quản lý</option></select>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-8">
          <button @click="showMemberModal = false" class="px-5 py-2.5 bg-slate-100 dark:bg-slate-700 font-bold rounded-xl">Hủy</button>
          <button @click="submitInvite" class="px-5 py-2.5 bg-blue-600 font-bold text-white rounded-xl shadow-lg">Gửi lời mời</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import KanbanBoard from "@/components/KanbanBoard.vue";
import MeetingRoom from "@/components/MeetingRoom.vue";
import ProjectChat from "@/components/ProjectChat.vue";

// 🟢 1. IMPORT USETOAST VÀO ĐÂY 🟢
import { useToast } from "../composables/useToast";

const vClickOutside = {
  mounted(el, binding) { el.clickOutsideEvent = function(event) { if (!(el === event.target || el.contains(event.target))) binding.value(event, el); }; document.body.addEventListener('click', el.clickOutsideEvent); },
  unmounted(el) { document.body.removeEventListener('click', el.clickOutsideEvent); }
};

const route = useRoute();
const router = useRouter();

// 🟢 2. KHỞI TẠO HÀM ADDTOAST 🟢
const { addToast } = useToast();

const projectId = route.params.projectId; 
const projectName = ref(route.query.projectName || "Dự án Không tên");
const userRole = ref(route.query.role || "MEMBER");
const currentUserName = ref(localStorage.getItem("fullName") || "Bạn"); 

const currentTab = ref('board');
const isSidebarOpen = ref(true);
const isDarkMode = ref(localStorage.getItem('theme') === 'dark');

const toggleSidebar = () => { isSidebarOpen.value = !isSidebarOpen.value; };
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value;
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light');
  if (isDarkMode.value) document.documentElement.classList.add('dark');
  else document.documentElement.classList.remove('dark');
};

const handleLogout = () => { localStorage.removeItem("isLoggedIn"); router.push("/"); };

onMounted(() => {
  if (!projectId) { alert("Lỗi: Không tìm thấy dự án!"); router.push('/projects'); return; }
  if (isDarkMode.value) document.documentElement.classList.add('dark');
  else document.documentElement.classList.remove('dark');
});

const showMemberModal = ref(false);
const memberSearchQuery = ref("");
const searchResults = ref([]);
const showDropdown = ref(false);
const newMemberRole = ref('MEMBER');
let searchTimeout = null;

const handleSearchInput = () => {
  clearTimeout(searchTimeout);
  const q = memberSearchQuery.value.trim();
  if (q.length < 2) { searchResults.value = []; showDropdown.value = false; return; }
  searchTimeout = setTimeout(async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/users/search?q=${q}`);
      if (res.ok) { searchResults.value = await res.json(); showDropdown.value = searchResults.value.length > 0; }
    } catch (e) {}
  }, 400);
};

const selectUser = (user) => { memberSearchQuery.value = user.email; showDropdown.value = false; };

// 🟢 3. SỬA LẠI HÀM SUBMIT ĐỂ DÙNG TOAST 🟢
const submitInvite = async () => {
  if (!memberSearchQuery.value) {
    addToast("Vui lòng nhập Email hoặc Username!", "warning");
    return;
  }
  
  try {
    const res = await fetch("http://localhost:8080/api/projects/add-member", { 
      method: 'POST', headers: { 'Content-Type': 'application/json' }, 
      body: JSON.stringify({ 
        projectId: projectId, 
        projectName: projectName.value, 
        inviterName: currentUserName.value, 
        identifier: memberSearchQuery.value, 
        role: newMemberRole.value 
      }) 
    });
    
    if (res.ok) {
      showMemberModal.value = false;
      addToast("Đã gửi lời mời thành công!", "success"); // Thông báo xanh xịn xò
      memberSearchQuery.value = ""; // Xóa trắng ô nhập để lần sau mời người khác
    } else { 
      const data = await res.json();
      addToast(data.error || "Lỗi gửi lời mời", "error"); // Báo lỗi đỏ
    }
  } catch (error) {
    addToast("Lỗi kết nối máy chủ", "error");
  }
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.custom-scrollbar::-webkit-scrollbar { width: 5px; height: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; }
</style>