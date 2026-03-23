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
        
        <div v-if="isSidebarOpen" class="mb-4 px-4 text-[10px] font-black text-slate-500 uppercase tracking-widest">Dự án: {{ projectName }}</div>
        
        <button @click="currentTab = 'board'" class="w-full flex items-center py-3 rounded-xl font-bold transition-all group" :class="[isSidebarOpen ? 'px-4' : 'justify-center px-0', currentTab === 'board' ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/50' : 'text-slate-400 hover:bg-slate-800 hover:text-white']">
          <span class="text-xl shrink-0">📌</span>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Bảng Công việc</span>
        </button>

        <button @click="currentTab = 'statistics'" class="w-full flex items-center py-3 rounded-xl font-bold transition-all group mt-2" :class="[isSidebarOpen ? 'px-4' : 'justify-center px-0', currentTab === 'statistics' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/50' : 'text-slate-400 hover:bg-slate-800 hover:text-white']">
          <span class="text-xl shrink-0">📊</span>
          <span v-if="isSidebarOpen" class="ml-3 whitespace-nowrap">Thống Kê Dự Án</span>
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
          <h1 class="text-xl font-black text-slate-800 dark:text-white tracking-tight truncate">
            <template v-if="currentTab === 'board'">{{ projectName }}</template>
            <template v-else-if="currentTab === 'statistics'">Thống kê: {{ projectName }}</template>
            <template v-else-if="currentTab === 'meeting'">Phòng Họp: {{ projectName }}</template>
            <template v-else>Thảo Luận: {{ projectName }}</template>
          </h1>
          <span v-if="currentTab === 'board' || currentTab === 'statistics'" class="px-2.5 py-1 bg-slate-100 dark:bg-slate-700 text-slate-500 dark:text-slate-300 text-[10px] font-bold uppercase rounded-md border border-slate-200 dark:border-slate-600 shrink-0">{{ userRole }}</span>
          <span v-if="currentTab === 'meeting'" class="px-2.5 py-1 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 text-[10px] font-bold uppercase rounded-md shrink-0">LIVE</span>
        </div>
        
        <div class="flex space-x-3 shrink-0 pl-4 items-center">
          <button v-if="['OWNER', 'MANAGER'].includes(userRole)" @click="showMemberModal = true" class="px-4 py-2 bg-blue-50 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 text-sm font-bold rounded-xl hover:bg-blue-100 dark:hover:bg-blue-500/20 transition-all border border-blue-100 flex items-center">
            <span class="mr-2">👥</span> Mời Thành Viên
          </button>
          
          <button @click="toggleMemberSidebar" class="px-4 py-2 bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-200 text-sm font-bold rounded-xl hover:bg-slate-200 dark:hover:bg-slate-600 transition-all border border-slate-200 dark:border-slate-600 flex items-center">
            <span class="mr-2">🧑‍🤝‍🧑</span> Thành viên ({{ projectMembers.length }})
          </button>
        </div>
      </header>

      <ProjectStatistics v-if="currentTab === 'statistics'" :projectId="projectId" />
      <KanbanBoard v-if="currentTab === 'board'" :projectId="projectId" :userRole="userRole" />
      <MeetingRoom v-if="currentTab === 'meeting'" :projectId="projectId" :projectName="projectName" :userRole="userRole" />
      <ProjectChat v-if="currentTab === 'chat'" :projectId="projectId" />

    </main>

    <div v-if="isMemberSidebarOpen" @click="toggleMemberSidebar" class="fixed inset-0 bg-slate-900/20 dark:bg-black/40 backdrop-blur-sm z-40 transition-opacity"></div>
    <aside :class="isMemberSidebarOpen ? 'translate-x-0' : 'translate-x-full'" class="fixed top-0 right-0 w-80 lg:w-96 h-screen bg-white dark:bg-slate-800 shadow-2xl z-50 transition-transform duration-300 ease-in-out border-l border-slate-200 dark:border-slate-700 flex flex-col">
      
      <div class="h-16 px-6 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between shrink-0 bg-slate-50 dark:bg-slate-800/50">
        <h3 class="font-black text-slate-800 dark:text-white flex items-center">
          <span class="text-xl mr-2">👥</span> Quản lý Thành viên
        </h3>
        <button @click="toggleMemberSidebar" class="p-2 rounded-full text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-700 hover:text-slate-600 dark:hover:text-white transition-colors outline-none">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>

      <div class="flex-1 overflow-y-auto p-4 custom-scrollbar space-y-6">
        
        <div>
          <h4 class="text-[10px] font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3 flex items-center px-2">
            <span class="mr-2">👑</span> Ban Quản Trị ({{ managers.length }})
          </h4>
          <div class="space-y-2">
            <div v-for="user in managers" :key="user.id" class="p-3 bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 rounded-xl flex items-center group hover:shadow-md transition-all relative">
              <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-yellow-400 to-orange-500 text-white font-black flex items-center justify-center shrink-0 uppercase shadow-sm">
                {{ user.fullName.charAt(0) }}
              </div>
              <div class="ml-3 flex-1 min-w-0">
                <p class="text-sm font-bold text-slate-800 dark:text-white truncate flex items-center">
                  {{ user.fullName }}
                  <span v-if="user.role === 'OWNER'" class="ml-2 px-1.5 py-0.5 bg-yellow-100 text-yellow-700 text-[8px] rounded-md border border-yellow-200">OWNER</span>
                  <span v-if="user.role === 'MANAGER'" class="ml-2 px-1.5 py-0.5 bg-purple-100 text-purple-700 text-[8px] rounded-md border border-purple-200">MANAGER</span>
                </p>
                <p class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ user.email }}</p>
              </div>

              <div v-if="userRole === 'OWNER' && user.role !== 'OWNER'" class="relative" v-click-outside="() => { if(activeActionMenu === user.id) activeActionMenu = null }">
                <button @click.stop="toggleActionMenu(user.id)" class="p-1.5 text-slate-400 hover:text-slate-700 dark:hover:text-white rounded-md hover:bg-slate-100 dark:hover:bg-slate-700 opacity-0 group-hover:opacity-100 transition-all">
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/></svg>
                </button>
                <div v-if="activeActionMenu === user.id" class="absolute right-0 mt-1 w-44 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-100 dark:border-slate-700 z-50 py-1 overflow-hidden">
                  <button @click="updateMemberRole(user.id, 'MEMBER')" class="w-full text-left px-4 py-2.5 hover:bg-slate-50 dark:hover:bg-slate-700 text-sm font-medium text-slate-700 dark:text-slate-200">👇 Giáng xuống Thành viên</button>
                  <div class="h-px bg-slate-100 dark:bg-slate-700 my-1"></div>
                  <button @click="kickMember(user.id, user.fullName)" class="w-full text-left px-4 py-2.5 hover:bg-red-50 dark:hover:bg-red-500/10 text-sm font-bold text-red-500">🗑️ Xóa khỏi dự án</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div>
          <h4 class="text-[10px] font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3 flex items-center px-2 mt-4">
            <span class="mr-2">👤</span> Thành Viên ({{ regularMembers.length }})
          </h4>
          <div v-if="regularMembers.length === 0" class="text-center py-6 text-xs text-slate-400 font-medium italic border border-dashed border-slate-200 dark:border-slate-700 rounded-xl">
            Chưa có thành viên nào.
          </div>
          <div class="space-y-2">
            <div v-for="user in regularMembers" :key="user.id" class="p-3 bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 rounded-xl flex items-center group hover:shadow-md transition-all relative">
              <div class="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 font-black flex items-center justify-center shrink-0 uppercase border border-slate-200 dark:border-slate-600">
                {{ user.fullName.charAt(0) }}
              </div>
              <div class="ml-3 flex-1 min-w-0">
                <p class="text-sm font-bold text-slate-800 dark:text-white truncate">{{ user.fullName }}</p>
                <p class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ user.email }}</p>
              </div>

              <div v-if="['OWNER', 'MANAGER'].includes(userRole)" class="relative" v-click-outside="() => { if(activeActionMenu === user.id) activeActionMenu = null }">
                <button @click.stop="toggleActionMenu(user.id)" class="p-1.5 text-slate-400 hover:text-slate-700 dark:hover:text-white rounded-md hover:bg-slate-100 dark:hover:bg-slate-700 opacity-0 group-hover:opacity-100 transition-all">
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/></svg>
                </button>
                <div v-if="activeActionMenu === user.id" class="absolute right-0 mt-1 w-44 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-100 dark:border-slate-700 z-50 py-1 overflow-hidden">
                  <button v-if="userRole === 'OWNER'" @click="updateMemberRole(user.id, 'MANAGER')" class="w-full text-left px-4 py-2.5 hover:bg-slate-50 dark:hover:bg-slate-700 text-sm font-medium text-slate-700 dark:text-slate-200">👆 Thăng làm Quản lý</button>
                  <div v-if="userRole === 'OWNER'" class="h-px bg-slate-100 dark:bg-slate-700 my-1"></div>
                  <button @click="kickMember(user.id, user.fullName)" class="w-full text-left px-4 py-2.5 hover:bg-red-50 dark:hover:bg-red-500/10 text-sm font-bold text-red-500">🗑️ Xóa khỏi dự án</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="userRole !== 'OWNER'" class="p-6 border-t border-slate-200 dark:border-slate-700 shrink-0 bg-slate-50 dark:bg-slate-800/50">
        <button @click="leaveProject" class="w-full py-3 bg-white dark:bg-slate-800 border border-red-200 dark:border-red-900/50 text-red-500 font-bold rounded-xl hover:bg-red-50 dark:hover:bg-red-500/10 transition-all flex justify-center items-center">
          <span class="mr-2">🚪</span> Rời khỏi dự án
        </button>
      </div>
    </aside>
    
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
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import ProjectStatistics from "@/components/ProjectStatistics.vue"; 
import KanbanBoard from "@/components/KanbanBoard.vue";
import MeetingRoom from "@/components/MeetingRoom.vue";
import ProjectChat from "@/components/ProjectChat.vue";
import { useToast } from "../composables/useToast";

const vClickOutside = {
  mounted(el, binding) { el.clickOutsideEvent = function(event) { if (!(el === event.target || el.contains(event.target))) binding.value(event, el); }; document.body.addEventListener('click', el.clickOutsideEvent); },
  unmounted(el) { document.body.removeEventListener('click', el.clickOutsideEvent); }
};

const route = useRoute();
const router = useRouter();
const { addToast } = useToast();

const projectId = route.params.projectId; 
const projectName = ref(route.query.projectName || "Dự án Không tên");
const userRole = ref(route.query.role || "MEMBER");
const currentUserName = ref(localStorage.getItem("fullName") || "Bạn"); 
const currentUserId = ref(localStorage.getItem("userId"));

// 🟢 TRẢ LẠI TRẠNG THÁI BOARD LÀ DEFAULT 🟢
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

// SIDEBAR THÀNH VIÊN
const isMemberSidebarOpen = ref(false);
const projectMembers = ref([]);
const activeActionMenu = ref(null);

const managers = computed(() => projectMembers.value.filter(m => m.role === 'OWNER' || m.role === 'MANAGER'));
const regularMembers = computed(() => projectMembers.value.filter(m => m.role === 'MEMBER'));

const toggleMemberSidebar = () => { 
  isMemberSidebarOpen.value = !isMemberSidebarOpen.value; 
  if (isMemberSidebarOpen.value) fetchProjectMembers(); 
};
const toggleActionMenu = (userId) => { activeActionMenu.value = activeActionMenu.value === userId ? null : userId; };

const fetchProjectMembers = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/projects/members-list?projectId=${projectId}`);
    if (res.ok) projectMembers.value = await res.json();
  } catch (error) { addToast("Lỗi lấy danh sách thành viên", "error"); }
};

const updateMemberRole = async (targetUserId, newRole) => {
  activeActionMenu.value = null;
  try {
    const res = await fetch("http://localhost:8080/api/projects/update-role", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ projectId, targetUserId, newRole }) });
    if (res.ok) { addToast("Đã cập nhật quyền thành công!", "success"); fetchProjectMembers(); } 
    else { addToast("Không thể cập nhật quyền", "error"); }
  } catch(e) { addToast("Lỗi kết nối", "error"); }
};

const kickMember = async (targetUserId, targetName) => {
  activeActionMenu.value = null;
  if(!confirm(`Xóa ${targetName} khỏi dự án?`)) return;
  try {
    const res = await fetch("http://localhost:8080/api/projects/remove-member", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ projectId, targetUserId }) });
    if (res.ok) { addToast(`Đã xóa ${targetName}!`, "success"); fetchProjectMembers(); }
    else { addToast("Lỗi không thể xóa", "error"); }
  } catch(e) { addToast("Lỗi kết nối", "error"); }
};

const leaveProject = async () => {
  if(!confirm("RỜI KHỎI dự án này? Hành động này không thể hoàn tác!")) return;
  try {
    const res = await fetch("http://localhost:8080/api/projects/remove-member", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ projectId, targetUserId: currentUserId.value }) });
    if (res.ok) { addToast("Bạn đã rời khỏi dự án!", "success"); router.push("/projects"); }
    else { addToast("Không thể rời dự án", "error"); }
  } catch(e) { addToast("Lỗi kết nối", "error"); }
};

// MỜI THÀNH VIÊN
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

const submitInvite = async () => {
  if (!memberSearchQuery.value) { addToast("Vui lòng nhập Email hoặc Username!", "warning"); return; }
  try {
    const res = await fetch("http://localhost:8080/api/projects/add-member", { 
      method: 'POST', headers: { 'Content-Type': 'application/json' }, 
      body: JSON.stringify({ projectId: projectId, projectName: projectName.value, inviterName: currentUserName.value, identifier: memberSearchQuery.value, role: newMemberRole.value }) 
    });
    if (res.ok) {
      showMemberModal.value = false; addToast("Đã gửi lời mời thành công!", "success"); memberSearchQuery.value = "";
      if(isMemberSidebarOpen.value) fetchProjectMembers(); 
    } else { 
      const data = await res.json(); addToast(data.error || "Lỗi gửi lời mời", "error");
    }
  } catch (error) { addToast("Lỗi kết nối máy chủ", "error"); }
};

onMounted(() => {
  if (!projectId) { alert("Lỗi: Không tìm thấy dự án!"); router.push('/projects'); return; }
  if (isDarkMode.value) document.documentElement.classList.add('dark');
  else document.documentElement.classList.remove('dark');
  fetchProjectMembers();
});
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.custom-scrollbar::-webkit-scrollbar { width: 5px; height: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; }
</style>