<template>
  <div class="space-y-6 pb-8 animate-fade-in">
    
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-2xl font-bold text-white">Tổng quan Nền tảng</h1>
        <p class="text-slate-400 text-sm mt-1">Trạm quan sát và quản trị trung tâm của hệ thống.</p>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      
      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-blue-500/10 transition-all hover:-translate-y-1">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Tổng người dùng</h3>
          <div class="p-2 bg-blue-500/20 rounded-lg text-blue-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-white">{{ stats.totalUsers }}</h2>
          <span class="text-emerald-400 text-sm font-bold flex items-center mb-1">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
            +12%
          </span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-yellow-500/10 transition-all hover:-translate-y-1">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Tổng số dự án</h3>
          <div class="p-2 bg-yellow-500/20 rounded-lg text-yellow-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-white">{{ stats.totalProjects }}</h2>
          <span class="text-emerald-400 text-sm font-bold flex items-center mb-1">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
            +5%
          </span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-emerald-500/10 transition-all hover:-translate-y-1">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Thành viên mới (Tuần)</h3>
          <div class="p-2 bg-emerald-500/20 rounded-lg text-emerald-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-white">+{{ stats.newUsers }}</h2>
          <span class="text-slate-400 text-sm font-bold flex items-center mb-1">
            Tuần qua
          </span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-red-500/10 transition-all hover:-translate-y-1">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Tài khoản bị khóa</h3>
          <div class="p-2 bg-red-500/20 rounded-lg text-red-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-red-400">{{ stats.bannedUsers }}</h2>
          <span class="text-slate-400 text-sm font-semibold mb-1">Cần rà soát</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 lg:col-span-2 flex flex-col shadow-lg">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-lg font-bold text-white">Tăng trưởng Nền tảng</h3>
          <select class="bg-slate-800 text-slate-300 text-sm border border-slate-600 rounded-lg px-3 py-1.5 focus:outline-none focus:border-blue-500 cursor-pointer font-semibold">
            <option>7 ngày qua</option>
            <option>30 ngày qua</option>
            <option>Năm nay</option>
          </select>
        </div>
        <div class="flex-1 relative w-full min-h-[200px] mt-4 group">
          <svg class="w-full h-full absolute inset-0" preserveAspectRatio="none" viewBox="0 0 500 150">
            <defs>
              <linearGradient id="gradientBlue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.4" />
                <stop offset="100%" stop-color="#3b82f6" stop-opacity="0" />
              </linearGradient>
            </defs>
            <path d="M0,120 C50,120 80,60 150,70 C220,80 250,130 320,110 C390,90 440,70 500,80 L500,150 L0,150 Z" fill="url(#gradientBlue)"></path>
            <path class="group-hover:stroke-blue-400 transition-colors" d="M0,120 C50,120 80,60 150,70 C220,80 250,130 320,110 C390,90 440,70 500,80" fill="none" stroke="#3b82f6" stroke-width="4" stroke-linecap="round"></path>
          </svg>
        </div>
        <div class="flex justify-between text-xs text-slate-500 font-bold mt-4 px-2 uppercase tracking-widest">
          <span>T2</span><span>T3</span><span>T4</span><span>T5</span><span>T6</span><span>T7</span><span>CN</span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg flex flex-col">
        <h3 class="text-lg font-bold text-white mb-6">Trạng thái Tài khoản</h3>
        <div class="flex-1 flex flex-col items-center justify-center">
          
          <div class="relative w-48 h-48">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
              <path class="text-slate-700" stroke-width="4" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="text-blue-500" stroke-dasharray="100, 100" stroke-width="4" stroke-linecap="round" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="text-purple-500" stroke-dasharray="15, 100" stroke-width="4" stroke-linecap="round" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="text-red-500" stroke-dasharray="5, 100" stroke-width="4" stroke-linecap="round" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
            <div class="absolute inset-0 flex flex-col items-center justify-center">
              <span class="text-3xl font-black text-white">100%</span>
              <span class="text-[10px] text-slate-400 font-bold tracking-widest mt-1 uppercase">Cơ cấu</span>
            </div>
          </div>

          <div class="w-full mt-8 space-y-4 px-4">
            <div class="flex justify-between items-center text-sm">
              <div class="flex items-center text-slate-300 font-semibold">
                <span class="w-3 h-3 rounded-full bg-blue-500 mr-3 shadow-[0_0_8px_rgba(59,130,246,0.6)]"></span> Đang hoạt động
              </div>
              <span class="font-bold text-white">85%</span>
            </div>
            <div class="flex justify-between items-center text-sm">
              <div class="flex items-center text-slate-300 font-semibold">
                <span class="w-3 h-3 rounded-full bg-purple-500 mr-3 shadow-[0_0_8px_rgba(168,85,247,0.6)]"></span> Quản trị viên
              </div>
              <span class="font-bold text-white">10%</span>
            </div>
            <div class="flex justify-between items-center text-sm">
              <div class="flex items-center text-slate-300 font-semibold">
                <span class="w-3 h-3 rounded-full bg-red-500 mr-3 shadow-[0_0_8px_rgba(239,68,68,0.6)]"></span> Bị khóa (Banned)
              </div>
              <span class="font-bold text-white">5%</span>
            </div>
          </div>

        </div>
      </div>
    </div>

    <div class="bg-[#1e293b] rounded-2xl border border-slate-700 shadow-lg overflow-hidden mt-6">
      <div class="p-6 border-b border-slate-700 flex justify-between items-center bg-[#1e293b]">
        <h3 class="text-lg font-bold text-white flex items-center"><span class="mr-2">👋</span> Người dùng mới đăng ký gần đây</h3>
        <button @click="$router.push('/admin/users')" class="text-sm font-semibold text-blue-400 hover:text-blue-300 transition flex items-center">
          Quản lý người dùng <span class="ml-1">&rarr;</span>
        </button>
      </div>
      
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
            <tr class="text-slate-400 text-xs uppercase tracking-widest bg-slate-900/50">
              <th class="px-6 py-4 font-bold">Người dùng</th>
              <th class="px-6 py-4 font-bold">Email liên hệ</th>
              <th class="px-6 py-4 font-bold">Quyền hạn</th>
              <th class="px-6 py-4 font-bold">Thời gian gia nhập</th>
              <th class="px-6 py-4 font-bold text-right">Trạng thái</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-700/50 text-sm">
            
            <tr v-for="user in recentUsers" :key="user.id" class="hover:bg-slate-800/50 transition-colors">
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 text-white flex items-center justify-center font-black mr-4 shadow-md">
                    {{ user.name.charAt(0) }}
                  </div>
                  <div>
                    <p class="text-white font-bold">{{ user.name }}</p>
                    <p class="text-[10px] text-slate-500 font-semibold mt-0.5">@{{ user.username }}</p>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 text-slate-300 font-medium">{{ user.email }}</td>
              <td class="px-6 py-4">
                <span v-if="user.role === 'ADMIN'" class="text-[10px] font-black uppercase tracking-widest text-purple-400 border border-purple-500/30 bg-purple-500/10 px-2 py-1 rounded">Admin</span>
                <span v-else class="text-[10px] font-black uppercase tracking-widest text-blue-400 border border-blue-500/30 bg-blue-500/10 px-2 py-1 rounded">Member</span>
              </td>
              <td class="px-6 py-4 text-slate-400 text-xs font-semibold">{{ user.date }}</td>
              <td class="px-6 py-4 text-right">
                <span v-if="user.status === 'ACTIVE'" class="px-3 py-1.5 text-[10px] font-black tracking-widest rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">HOẠT ĐỘNG</span>
                <span v-else class="px-3 py-1.5 text-[10px] font-black tracking-widest rounded-lg bg-red-500/10 text-red-400 border border-red-500/20">BỊ KHÓA</span>
              </td>
            </tr>

          </tbody>
        </table>
      </div>
    </div>
    
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// Các biến lưu trữ dữ liệu trống ban đầu
const stats = ref({
  totalUsers: 0,
  totalProjects: 0,
  newUsers: 0,
  bannedUsers: 0
});

const recentUsers = ref([]);

// Tự động gọi hàm lấy dữ liệu khi mở trang
onMounted(() => {
  fetchAdminDashboardData();
});

const fetchAdminDashboardData = async () => {
  try {
    const userId = localStorage.getItem('userId');
    const response = await fetch('http://localhost:8080/api/admin/dashboard/summary', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'User-ID': userId
      }
    });

    if (response.ok) {
      const data = await response.json();
      stats.value = data.stats;
      recentUsers.value = data.recentUsers;
    } else {
      console.error("Không thể lấy dữ liệu Admin Dashboard");
    }
  } catch (error) {
    console.error("Lỗi kết nối đến Backend Java", error);
  }
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>