<template>
  <div class="min-h-screen bg-slate-50 flex font-sans">
    
    <aside class="w-64 bg-slate-900 text-white flex flex-col shadow-2xl z-20 shrink-0">
      <div class="p-6 border-b border-slate-800 flex items-center space-x-3">
        <div class="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center font-black text-xl shadow-lg">T</div>
        <div>
          <h2 class="text-xl font-black tracking-tighter">PROJECT ALPHA</h2>
          <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5">Marketing Dept</p>
        </div>
      </div>
      <nav class="flex-1 p-4 space-y-2">
        <router-link to="/dashboard" class="flex items-center space-x-3 px-4 py-3 bg-blue-600/10 text-blue-500 rounded-xl font-bold transition-all">
          <span>📊</span><span>Bảng điều khiển</span>
        </router-link>
        <router-link to="/projects" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
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

    <main class="flex-1 flex flex-col h-screen overflow-hidden bg-slate-50 min-w-0">
      
      <header class="h-20 bg-white border-b border-slate-200 flex items-center justify-between px-6 md:px-10 shrink-0 z-10 shadow-sm">
        <h1 class="text-xl font-black text-slate-800">Tổng quan Hệ thống</h1>
        
        <div class="flex items-center space-x-6">
          <div class="relative hidden md:block">
            <span class="absolute left-3 top-2.5 text-slate-400">🔍</span>
            <input type="text" placeholder="Tìm kiếm nhanh..." class="w-64 pl-10 pr-4 py-2 bg-slate-100 border-none rounded-full text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none transition-all" />
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
              <div class="max-h-[350px] overflow-y-auto">
                <div v-if="notifications.length === 0" class="p-6 text-center text-slate-400 text-sm font-medium">Bạn không có thông báo nào.</div>
              </div>
            </div>
          </div>

          <div class="flex items-center space-x-3 border-l border-slate-300 pl-6 hidden sm:flex">
            <div class="text-right">
              <p class="text-sm font-bold text-slate-700">{{ currentUser }}</p>
              <p class="text-xs text-blue-500 font-bold">Đang trực tuyến</p>
            </div>
            <div class="w-10 h-10 bg-gradient-to-tr from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-black shadow-md uppercase">{{ firstLetter }}</div>
          </div>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-6 md:p-10 custom-scrollbar">
        <div class="max-w-[1400px] mx-auto space-y-8">
          
          <div class="flex flex-col md:flex-row justify-between md:items-end gap-4 animate-fade-in-up">
            <div>
              <h2 class="text-3xl font-black text-slate-900 mb-2">👋 Chào {{ currentUser }}, chúc một ngày hiệu quả!</h2>
              <p class="text-slate-500 mt-1 font-medium">Hãy tập trung vào những ưu tiên cao nhất của bạn trong ngày hôm nay.</p>
            </div>
            <div class="px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm font-bold text-slate-600 shadow-sm flex items-center shrink-0">
              <svg class="w-5 h-5 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
              Hôm nay: {{ currentDate }}
            </div>
          </div>

          <div v-if="loading" class="text-center py-10">
             <div class="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
             <p class="text-xs font-bold text-slate-400 uppercase tracking-widest">Đang tải số liệu...</p>
          </div>

          <div v-else>
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-8 animate-fade-in-up" style="animation-delay: 0.1s;">
              
              <div class="bg-white p-6 rounded-3xl border border-slate-200 relative overflow-hidden group cursor-pointer hover:-translate-y-1 hover:shadow-lg transition-all shadow-sm" @click="$router.push('/tasks')">
                <div class="absolute -right-6 -top-6 w-24 h-24 bg-blue-50 rounded-full group-hover:scale-150 transition-transform duration-500"></div>
                <div class="flex justify-between items-start mb-4 relative z-10">
                  <div class="w-12 h-12 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center text-2xl shadow-inner">📅</div>
                  <span class="text-[10px] font-black text-blue-600 bg-blue-50 px-2.5 py-1 rounded-lg uppercase tracking-wider border border-blue-100">Today</span>
                </div>
                <h3 class="text-4xl font-black text-slate-800 mb-1 relative z-10">{{ dashboardStats.todayTasks }}</h3>
                <p class="text-xs font-bold text-slate-400 uppercase tracking-widest relative z-10">Việc cần làm hôm nay</p>
              </div>

              <div class="bg-red-50 p-6 rounded-3xl border border-red-200 relative overflow-hidden group cursor-pointer hover:-translate-y-1 hover:shadow-lg hover:shadow-red-200 transition-all shadow-sm" @click="$router.push('/tasks')">
                <div class="absolute -right-6 -top-6 w-24 h-24 bg-red-100 rounded-full group-hover:scale-150 transition-transform duration-500"></div>
                <div class="flex justify-between items-start mb-4 relative z-10">
                  <div class="w-12 h-12 bg-white text-red-500 rounded-2xl flex items-center justify-center text-2xl shadow-sm border border-red-100">⚠️</div>
                  <span class="text-[10px] font-black text-red-600 bg-white px-2.5 py-1 rounded-lg uppercase tracking-wider border border-red-200 shadow-sm">Urgent</span>
                </div>
                <h3 class="text-4xl font-black text-red-600 mb-1 relative z-10">{{ dashboardStats.overdueTasks }}</h3>
                <p class="text-xs font-bold text-red-400 uppercase tracking-widest relative z-10">Công việc bị trễ hạn</p>
              </div>

              <div class="bg-white p-6 rounded-3xl border border-slate-200 relative overflow-hidden group cursor-pointer hover:-translate-y-1 hover:shadow-lg transition-all shadow-sm" @click="$router.push('/tasks')">
                <div class="absolute -right-6 -top-6 w-24 h-24 bg-orange-50 rounded-full group-hover:scale-150 transition-transform duration-500"></div>
                <div class="flex justify-between items-start mb-4 relative z-10">
                  <div class="w-12 h-12 bg-orange-100 text-orange-500 rounded-2xl flex items-center justify-center text-2xl shadow-inner">⏳</div>
                  <span class="text-[10px] font-black text-orange-600 bg-orange-50 px-2.5 py-1 rounded-lg uppercase tracking-wider border border-orange-100">Pending</span>
                </div>
                <h3 class="text-4xl font-black text-slate-800 mb-1 relative z-10">{{ dashboardStats.pendingTasks }}</h3>
                <p class="text-xs font-bold text-slate-400 uppercase tracking-widest relative z-10">Việc đang thực hiện</p>
              </div>

              <div class="bg-white p-6 rounded-3xl border border-slate-200 relative overflow-hidden group cursor-pointer hover:-translate-y-1 hover:shadow-lg transition-all shadow-sm" @click="$router.push('/projects')">
                <div class="absolute -right-6 -top-6 w-24 h-24 bg-emerald-50 rounded-full group-hover:scale-150 transition-transform duration-500"></div>
                <div class="flex justify-between items-start mb-4 relative z-10">
                  <div class="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-2xl flex items-center justify-center text-2xl shadow-inner">🚀</div>
                  <span class="text-[10px] font-black text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-lg uppercase tracking-wider border border-emerald-100">Weekly</span>
                </div>
                <h3 class="text-4xl font-black text-emerald-600 mb-1 relative z-10">{{ dashboardStats.performance }}%</h3>
                <p class="text-xs font-bold text-slate-400 uppercase tracking-widest relative z-10">Hiệu suất hoàn thành</p>
              </div>

            </div>

            <div class="grid grid-cols-1 xl:grid-cols-3 gap-8 animate-fade-in-up" style="animation-delay: 0.2s;">
              
              <div class="xl:col-span-2 bg-white rounded-3xl border border-slate-200 shadow-sm flex flex-col h-[500px]">
                <div class="p-6 border-b border-slate-100 flex justify-between items-center shrink-0">
                  <h3 class="text-xl font-black text-slate-800 flex items-center"><span class="mr-2">🔥</span> Hoạt động gần đây</h3>
                  <span class="text-xs font-bold text-slate-400 bg-slate-100 px-3 py-1 rounded-lg">Cập nhật liên tục</span>
                </div>
                
                <div class="p-6 flex-1 overflow-y-auto custom-scrollbar">
                  <div v-if="recentActivities.length === 0" class="text-center py-10 opacity-50">
                    <div class="text-4xl mb-3">😴</div>
                    <p class="text-sm font-bold text-slate-500">Chưa có hoạt động nào gần đây.</p>
                  </div>

                  <div v-else class="space-y-6 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px before:h-full before:w-0.5 before:bg-slate-200">
                    <div v-for="(activity, index) in recentActivities" :key="index" class="relative flex items-start">
                      
                      <div class="w-10 h-10 rounded-full border-4 border-white flex items-center justify-center font-black text-white shadow-sm z-10 shrink-0" :class="activity.color">
                        {{ activity.avatar }}
                      </div>

                      <div class="flex-1 pl-4 pb-2">
                        <div class="bg-slate-50 p-4 rounded-2xl border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
                          <div class="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-2">
                            <p class="text-sm text-slate-600 leading-relaxed">
                              <span class="font-bold text-slate-800">{{ activity.user }}</span> 
                              {{ activity.action }}
                              <span class="font-bold text-blue-600 cursor-pointer hover:underline">{{ activity.target }}</span>
                            </p>
                            <span class="text-[10px] font-bold text-slate-400 shrink-0 whitespace-nowrap">{{ activity.time }}</span>
                          </div>
                        </div>
                      </div>

                    </div>
                  </div>
                </div>
              </div>

              <div class="bg-white rounded-3xl border border-slate-200 shadow-sm p-6 flex-1 flex flex-col h-[500px]">
                <h3 class="text-lg font-black text-slate-800 mb-4 flex items-center shrink-0"><span class="mr-2">🚨</span> Cần xử lý gấp</h3>
                
                <div class="space-y-4 flex-1 overflow-y-auto custom-scrollbar pr-2">
                  <div v-if="urgentTasks.length === 0" class="text-center py-10 flex flex-col items-center">
                     <span class="text-4xl mb-2">🎉</span>
                     <p class="text-xs font-bold text-emerald-500 bg-emerald-50 p-2 rounded-lg">Tuyệt vời! Bạn không có việc nào trễ hạn.</p>
                  </div>

                  <div v-for="task in urgentTasks" :key="task.id" class="p-4 bg-white border border-slate-200 rounded-2xl hover:border-red-300 hover:shadow-md transition-all cursor-pointer group" @click="$router.push('/tasks')">
                    <h4 class="text-sm font-bold text-slate-800 group-hover:text-blue-600 transition-colors mb-1.5 line-clamp-2">{{ task.title }}</h4>
                    <p class="text-[10px] font-bold text-slate-400 mb-3 uppercase tracking-wider truncate">{{ task.project }}</p>
                    <div class="flex justify-between items-center">
                      <span class="text-[10px] font-black px-2.5 py-1 rounded bg-red-50 text-red-600 border border-red-100">{{ task.timeLeft }}</span>
                      <button class="text-xs font-bold text-blue-600 hover:underline">Xử lý &rarr;</button>
                    </div>
                  </div>
                </div>
              </div>

            </div>

          </div>

        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

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

const currentUser = ref("Khách");
const firstLetter = ref("K");
const currentDate = ref("");
const loading = ref(true);

const unreadCount = ref(0);
const showNotifMenu = ref(false);
const notifications = ref([]);
const toggleNotifMenu = () => { showNotifMenu.value = !showNotifMenu.value; };
const handleLogout = () => { localStorage.clear(); router.push("/"); };

// Dữ liệu rỗng chờ API cập nhật
const dashboardStats = ref({ todayTasks: 0, overdueTasks: 0, pendingTasks: 0, performance: 0 });
const recentActivities = ref([]);
const urgentTasks = ref([]);

onMounted(() => {
  const storedUser = localStorage.getItem("username");
  if (storedUser) {
    currentUser.value = storedUser;
    firstLetter.value = storedUser.charAt(0).toUpperCase();
  }
  const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  currentDate.value = new Date().toLocaleDateString('vi-VN', options);

  fetchDashboardData();
});

// 🟢 KẾT NỐI BACKEND API
const fetchDashboardData = async () => {
  loading.value = true;
  try {
    const userId = localStorage.getItem("userId") || 1;
    // Chờ Java tạo API này:
    const response = await fetch(`http://localhost:8080/api/dashboard/summary`, {
      headers: { "User-ID": userId }
    });

    if (response.ok) {
      const data = await response.json();
      dashboardStats.value = data.stats;
      recentActivities.value = data.activities;
      urgentTasks.value = data.urgentTasks;
    } else {
      // GIẢ LẬP DỮ LIỆU NẾU BACKEND CHƯA CÓ API
      simulateData();
    }
  } catch (error) {
    simulateData(); // Fallback khi lỗi kết nối
  } finally {
    loading.value = false;
  }
};

// Dữ liệu giả lập chạy tạm
const simulateData = () => {
  dashboardStats.value = { todayTasks: 3, overdueTasks: 1, pendingTasks: 8, performance: 85 };
  recentActivities.value = [
    { user: 'Nguyễn Văn A', action: 'vừa hoàn thành công việc', target: 'Thiết kế Database ERD', time: '10 phút trước', avatar: 'A', color: 'bg-emerald-500' },
    { user: 'Trần Thị B', action: 'đã đính kèm tài liệu vào', target: 'Báo cáo Tài chính Quý 1', time: '1 giờ trước', avatar: 'B', color: 'bg-blue-500' },
    { user: 'Hệ thống', action: 'đã gửi cảnh báo trễ hạn công việc', target: 'Fix lỗi giao diện Mobile', time: 'Hôm qua', avatar: '⚙️', color: 'bg-slate-700' },
  ];
  urgentTasks.value = [
    { id: 101, title: 'Fix lỗi responsive trang chủ Mobile', project: 'Tối ưu Landing Page', timeLeft: 'Trễ 2 ngày' },
    { id: 102, title: 'Gửi bảng báo giá cho đối tác', project: 'Chiến dịch Marketing Tết', timeLeft: 'Hết hạn hôm nay' },
  ];
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
.animate-fade-in-up { animation: fadeInUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards; opacity: 0; transform: translateY(15px); }

@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
@keyframes fadeInUp { to { opacity: 1; transform: translateY(0); } }

.line-clamp-1 { display: -webkit-box; -webkit-line-clamp: 1; line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.custom-scrollbar::-webkit-scrollbar { width: 5px; height: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
</style>