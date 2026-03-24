<template>
  <div class="h-full flex flex-col bg-slate-50 dark:bg-[#0B1121] animate-fade-in rounded-tl-3xl text-slate-800 dark:text-slate-200 transition-colors duration-300">
    
    <div class="px-8 pt-10 pb-6 shrink-0">
      <div class="flex flex-wrap justify-between items-start gap-4 mb-2">
        <div class="flex items-center space-x-6">
          <h2 class="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">Thông báo</h2>
          
          <div class="relative group hidden sm:block">
            <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400 group-focus-within:text-blue-500 transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
            </div>
            <input v-model="searchQuery" type="text" placeholder="Tìm kiếm thông báo..." 
                   class="pl-10 pr-4 py-2 bg-white dark:bg-[#151C2C] border border-slate-200 dark:border-white/5 rounded-lg text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 text-slate-800 dark:text-white outline-none w-80 md:w-96 transition-all shadow-sm" />
            <button v-if="searchQuery" @click="searchQuery = ''" class="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-200">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
          </div>
        </div>

        <div class="flex bg-slate-200/50 dark:bg-[#151C2C] p-1 rounded-lg border border-slate-200 dark:border-white/5 shadow-sm">
          <button @click="filterType = 'ALL'" :class="filterType === 'ALL' ? 'bg-white dark:bg-[#25304B] text-slate-900 dark:text-white shadow-sm' : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'" class="px-4 py-1.5 text-sm font-semibold rounded-md transition-all">Tất cả</button>
          <button @click="filterType = 'UNREAD'" :class="filterType === 'UNREAD' ? 'bg-white dark:bg-[#25304B] text-slate-900 dark:text-white shadow-sm' : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'" class="px-4 py-1.5 text-sm font-semibold rounded-md transition-all flex items-center">
            Chưa đọc
            <span v-if="unreadCount > 0" class="ml-1.5 w-2 h-2 rounded-full bg-orange-500"></span>
          </button>
        </div>
      </div>
      <p class="text-sm text-slate-500 dark:text-slate-400 font-medium">Cập nhật những thay đổi mới nhất từ dự án của bạn.</p>
      
      <button v-if="unreadCount > 0" @click="markAllAsRead" class="mt-4 text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline">
        Đánh dấu tất cả là đã đọc ({{ unreadCount }})
      </button>
    </div>

    <div class="flex-1 overflow-y-auto px-8 pb-8 custom-scrollbar">
      
      <div v-if="isLoading" class="flex justify-center mt-10">
        <div class="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <div v-else-if="groupedNotifications.length === 0" class="h-full flex flex-col items-center justify-center text-center opacity-60">
        <svg class="w-16 h-16 mb-4 text-slate-400 dark:text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path></svg>
        <h3 class="text-lg font-bold text-slate-700 dark:text-slate-300">Không có thông báo nào</h3>
        <p class="text-sm text-slate-500 mt-1">Khi có sự kiện mới, nó sẽ xuất hiện ở đây.</p>
      </div>

      <div v-for="group in groupedNotifications" :key="group.label" class="mb-8 animate-fade-in-up">
        <div class="flex items-center mb-4">
          <h3 class="text-[10px] font-black text-slate-500 dark:text-slate-500 uppercase tracking-[0.15em]">{{ group.label }}</h3>
          <div class="flex-1 h-px bg-slate-200 dark:bg-[#1E293B] ml-4"></div>
        </div>

        <div class="space-y-3">
          <div v-for="notif in group.items" :key="notif.id" 
              @click="markAsRead(notif)"
               class="group relative flex items-start p-4 bg-white dark:bg-[#151C2C] rounded-xl cursor-pointer hover:shadow-md transition-all duration-200 border border-slate-200 dark:border-transparent hover:border-slate-300 dark:hover:border-white/10"
               :class="getLeftBorderClass(notif.type)">
            
            <button @click.stop="deleteNotification(notif.id)" class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 p-1.5 text-slate-400 hover:text-red-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-all z-10" title="Xóa thông báo">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>

            <div class="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 mr-4 shadow-sm" :class="getIconBoxClass(notif.type)">
              <span v-html="getSvgIcon(notif.type)"></span>
            </div>

            <div class="flex-1 min-w-0 pr-8">
              <h4 class="text-sm font-bold flex items-center mb-1" :class="notif.isRead ? 'text-slate-600 dark:text-slate-300' : 'text-slate-900 dark:text-white'">
                {{ notif.title }}
              </h4>
              
              <p class="text-[13px] leading-relaxed line-clamp-3 whitespace-pre-wrap" :class="notif.isRead ? 'text-slate-500 dark:text-slate-400' : 'text-slate-700 dark:text-slate-300'">{{ notif.content.replace(/\\n/g, '\n') }}</p>
            </div>

            <div class="flex flex-col items-end shrink-0 ml-2 space-y-2 mt-0.5">
              <span class="text-[10px] font-medium text-slate-400 dark:text-slate-500 whitespace-nowrap">{{ formatRelativeTime(notif.createdAt) }}</span>
              <div v-if="!notif.isRead" class="w-2 h-2 rounded-full bg-orange-500 shadow-[0_0_8px_rgba(249,115,22,0.6)]"></div>
            </div>

          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  projectId: { type: Number, required: true }
});

const filterType = ref('ALL'); 
const searchQuery = ref('');
const notifications = ref([]);
const isLoading = ref(true);
let pollingInterval = null;

// 🟢 KHIÊN BẢO VỆ BẰNG LOCALSTORAGE: GHI NHỚ LỊCH SỬ XÓA & ĐỌC MÃI MÃI 🟢
const getLocalSet = (key) => new Set(JSON.parse(localStorage.getItem(key) || '[]'));
const saveLocalSet = (key, set) => localStorage.setItem(key, JSON.stringify([...set]));

const deletedIds = ref(getLocalSet('teamwork_deleted_notifs'));
const readIds = ref(getLocalSet('teamwork_read_notifs'));

const fetchNotifications = async () => {
  try {
    const userId = localStorage.getItem('userId') || 1;
    const res = await fetch('http://localhost:8080/api/notifications/list', {
      headers: { 'User-ID': userId }
    });
    
    if (res.ok) {
      const rawText = await res.text();
      const safeText = rawText.replace(/\n/g, "\\n").replace(/\r/g, "");
      const data = JSON.parse(safeText);
      
      const projectNotifs = data.filter(n => (n.projectId === props.projectId) || (n.ProjectID === props.projectId));

      const processedNotifs = projectNotifs.map(n => {
        const title = n.title || n.Title || '';
        const content = n.content || n.Content || '';
        
        const id = n.id || n.ID;
        // 🟢 NẾU Backend báo chưa đọc, nhưng LocalStorage báo đọc rồi -> Ép nó thành Đã Đọc
        let isRead = n.isRead !== undefined ? n.isRead : (n.IsRead === 1 || n.IsRead === true);
        isRead = isRead || readIds.value.has(id); 

        const createdAt = n.createdAt || n.CreatedAt || new Date();

        let type = 'OTHER'; 
        const titleUpper = title.toUpperCase();
        
        if (titleUpper.includes('[KHẨN CẤP]') || titleUpper.includes('TRỄ HẠN')) type = 'OVERDUE';
        else if (titleUpper.includes('[NHẮC NHỞ]') || titleUpper.includes('TỚI HẠN')) type = 'DUE_TODAY';
        else if (titleUpper.includes('[THÔNG BÁO]') || titleUpper.includes('SẮP TỚI HẠN')) type = 'DUE_SOON';
        else if (titleUpper.includes('[NHIỆM VỤ MỚI]')) type = 'TASK_ASSIGNED';
        else if (titleUpper.includes('[THAY ĐỔI DEADLINE]')) type = 'DEADLINE_CHANGED';
        else if (titleUpper.includes('[HOÀN THÀNH]')) type = 'TASK_COMPLETED';

        return { id, title, content, isRead, createdAt, type };
      });

      // 🟢 BỘ LỌC KÉP: Chặn thông báo 'OTHER' VÀ Chặn những thông báo có ID nằm trong sổ đen (deletedIds)
      notifications.value = processedNotifs.filter(n => n.type !== 'OTHER' && !deletedIds.value.has(n.id));
    }
  } catch (error) {
    console.error("Lỗi lấy thông báo:", error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchNotifications();
  pollingInterval = setInterval(fetchNotifications, 3000);
});

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
});

const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length);

const groupedNotifications = computed(() => {
  let filtered = notifications.value;
  if (filterType.value === 'UNREAD') filtered = filtered.filter(n => !n.isRead);
  
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase();
    filtered = filtered.filter(n => n.title.toLowerCase().includes(q) || n.content.toLowerCase().includes(q));
  }

  filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  const groups = {};
  filtered.forEach(n => {
    const label = getDateGroupLabel(n.createdAt);
    if (!groups[label]) groups[label] = [];
    groups[label].push(n);
  });
  return Object.keys(groups).map(key => ({ label: key, items: groups[key] }));
});

const getDateGroupLabel = (dateInput) => {
  const d = new Date(dateInput);
  const today = new Date();
  const yesterday = new Date(today); yesterday.setDate(yesterday.getDate() - 1);
  if (d.toDateString() === today.toDateString()) return 'HÔM NAY';
  if (d.toDateString() === yesterday.toDateString()) return 'HÔM QUA';
  return d.toLocaleDateString('vi-VN');
};

const markAsRead = async (notif) => { 
  if (!notif.isRead) {
    notif.isRead = true; 
    
    // 🟢 LƯU VÀO SỔ LOCALSTORAGE ĐỂ LẦN TẢI LẠI TRANG (HOẶC 3 GIÂY SAU) NÓ VẪN NHỚ
    readIds.value.add(notif.id);
    saveLocalSet('teamwork_read_notifs', readIds.value);

    try {
      await fetch('http://localhost:8080/api/notifications/respond', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'User-ID': localStorage.getItem('userId') || 1 },
        body: JSON.stringify({ notificationId: notif.id, projectId: props.projectId, isAccept: true })
      });
    } catch (e) {}
  }
};

const markAllAsRead = () => { 
  notifications.value.filter(n => !n.isRead).forEach(n => {
    n.isRead = true;
    readIds.value.add(n.id);
  }); 
  saveLocalSet('teamwork_read_notifs', readIds.value);
};

const deleteNotification = async (id) => { 
  // 🟢 1. Đưa ID vào sổ đen LocalStorage
  deletedIds.value.add(id);
  saveLocalSet('teamwork_deleted_notifs', deletedIds.value);
  
  // 🟢 2. Xóa khỏi màn hình ngay lập tức
  notifications.value = notifications.value.filter(n => n.id !== id); 
  
  try {
      await fetch('http://localhost:8080/api/notifications/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ notificationId: id })
      });
  } catch (e) {}
};

// ==========================================
// 🎨 UI HELPERS
// ==========================================

const getLeftBorderClass = (type) => {
  if (type === 'OVERDUE') return 'border-l-[3px] border-l-red-500';
  if (type === 'DUE_TODAY') return 'border-l-[3px] border-l-orange-500';
  if (type === 'DUE_SOON') return 'border-l-[3px] border-l-yellow-500';
  return 'border-l-[3px] border-l-transparent dark:hover:border-l-white/20'; 
};

const getIconBoxClass = (type) => {
  const styles = {
    'OVERDUE': 'bg-red-500/10 text-red-500',
    'DUE_TODAY': 'bg-orange-500/10 text-orange-500',
    'DUE_SOON': 'bg-yellow-500/10 text-yellow-500',
    'TASK_ASSIGNED': 'bg-blue-500/10 text-blue-500',
    'DEADLINE_CHANGED': 'bg-purple-500/10 text-purple-400',
    'TASK_COMPLETED': 'bg-emerald-500/10 text-emerald-500',
  };
  return styles[type] || 'bg-slate-100 dark:bg-white/5 text-slate-400';
};

const getSvgIcon = (type) => {
  const icons = {
    'OVERDUE': `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>`,
    'DUE_TODAY': `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`,
    'DUE_SOON': `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>`,
    'TASK_ASSIGNED': `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>`,
    'TASK_COMPLETED': `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`
  };
  return icons[type] || `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>`;
};

const formatRelativeTime = (dateInput) => {
  const date = new Date(dateInput);
  const now = new Date();
  const diffInMs = now - date;
  const diffInMins = Math.floor(diffInMs / (1000 * 60));
  const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));

  if (diffInMins < 1) return 'Vừa xong';
  if (diffInMins < 60) return `${diffInMins} phút trước`;
  if (diffInHours < 24 && date.getDate() === now.getDate()) return `${diffInHours} giờ trước`;
  
  const yesterday = new Date(now); yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return `Hôm qua, ${date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}`;
  }
  return `${date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} ${date.toLocaleDateString('vi-VN')}`;
};
</script>

<style scoped>
.animate-fade-in-up { animation: fadeInUp 0.4s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; }
</style>