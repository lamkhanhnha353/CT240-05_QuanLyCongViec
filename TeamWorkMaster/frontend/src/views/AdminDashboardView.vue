<template>
  <div class="space-y-6 pb-8">
    
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-2xl font-bold text-white">Tổng quan Hệ thống</h1>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      
      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-blue-500/10 transition-shadow">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Tổng người dùng</h3>
          <div class="p-2 bg-blue-500/20 rounded-lg text-blue-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-white">1,250</h2>
          <span class="text-green-400 text-sm font-bold flex items-center mb-1">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
            +12%
          </span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-yellow-500/10 transition-shadow">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Dự án đang chạy</h3>
          <div class="p-2 bg-yellow-500/20 rounded-lg text-yellow-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-white">45</h2>
          <span class="text-red-400 text-sm font-bold flex items-center mb-1">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0v-8m0 8l-8-8-4 4-6-6"></path></svg>
            -5%
          </span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-green-500/10 transition-shadow">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Công việc hoàn thành</h3>
          <div class="p-2 bg-green-500/20 rounded-lg text-green-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-white">892</h2>
          <span class="text-green-400 text-sm font-bold flex items-center mb-1">
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
            +18%
          </span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg hover:shadow-blue-500/10 transition-shadow">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-slate-400 text-sm font-semibold">Sức khỏe hệ thống</h3>
          <div class="p-2 bg-blue-500/20 rounded-lg text-blue-400">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.618 5.984A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016zM12 9v2m0 4h.01"></path></svg>
          </div>
        </div>
        <div class="flex items-end justify-between">
          <h2 class="text-3xl font-bold text-white">98%</h2>
          <span class="text-slate-400 text-sm font-semibold mb-1">Tốt</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 lg:col-span-2 flex flex-col shadow-lg">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-lg font-bold text-white">User Activity</h3>
          <select class="bg-slate-800 text-slate-300 text-sm border border-slate-600 rounded-lg px-3 py-1.5 focus:outline-none focus:border-blue-500 cursor-pointer">
            <option>Last 7 days</option>
            <option>Last 30 days</option>
            <option>This Year</option>
          </select>
        </div>
        <div class="flex-1 relative w-full min-h-[200px] mt-4">
          <svg class="w-full h-full absolute inset-0" preserveAspectRatio="none" viewBox="0 0 500 150">
            <defs>
              <linearGradient id="gradientBlue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.4" />
                <stop offset="100%" stop-color="#3b82f6" stop-opacity="0" />
              </linearGradient>
            </defs>
            <path d="M0,120 C50,120 80,60 150,70 C220,80 250,130 320,110 C390,90 440,70 500,80 L500,150 L0,150 Z" fill="url(#gradientBlue)"></path>
            <path d="M0,120 C50,120 80,60 150,70 C220,80 250,130 320,110 C390,90 440,70 500,80" fill="none" stroke="#3b82f6" stroke-width="4" stroke-linecap="round"></path>
          </svg>
        </div>
        <div class="flex justify-between text-xs text-slate-500 font-bold mt-4 px-2 uppercase">
          <span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span>
        </div>
      </div>

      <div class="bg-[#1e293b] rounded-2xl p-6 border border-slate-700 shadow-lg flex flex-col">
        <h3 class="text-lg font-bold text-white mb-6">Task Distribution</h3>
        <div class="flex-1 flex flex-col items-center justify-center">
          
          <div class="relative w-48 h-48">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
              <path class="text-slate-700" stroke-width="4" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="text-emerald-400" stroke-dasharray="100, 100" stroke-width="4" stroke-linecap="round" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="text-orange-500" stroke-dasharray="90, 100" stroke-width="4" stroke-linecap="round" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              <path class="text-blue-500" stroke-dasharray="65, 100" stroke-width="4" stroke-linecap="round" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
            <div class="absolute inset-0 flex flex-col items-center justify-center">
              <span class="text-3xl font-black text-white">1.2k</span>
              <span class="text-xs text-slate-400 font-bold tracking-widest mt-1">TOTAL</span>
            </div>
          </div>

          <div class="w-full mt-8 space-y-3 px-4">
            <div class="flex justify-between items-center text-sm">
              <div class="flex items-center text-slate-300 font-medium">
                <span class="w-2.5 h-2.5 rounded-full bg-blue-500 mr-3"></span> Development
              </div>
              <span class="font-bold text-white">65%</span>
            </div>
            <div class="flex justify-between items-center text-sm">
              <div class="flex items-center text-slate-300 font-medium">
                <span class="w-2.5 h-2.5 rounded-full bg-orange-500 mr-3"></span> Design
              </div>
              <span class="font-bold text-white">25%</span>
            </div>
            <div class="flex justify-between items-center text-sm">
              <div class="flex items-center text-slate-300 font-medium">
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 mr-3"></span> Other
              </div>
              <span class="font-bold text-white">10%</span>
            </div>
          </div>

        </div>
      </div>
    </div>

    <div class="bg-[#1e293b] rounded-2xl border border-slate-700 shadow-lg overflow-hidden mt-6">
      <div class="p-6 border-b border-slate-700 flex justify-between items-center bg-[#1e293b]">
        <h3 class="text-lg font-bold text-white">Hoạt động gần đây</h3>
        <button class="text-sm font-semibold text-blue-400 hover:text-blue-300 transition">Xem tất cả</button>
      </div>
      
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
            <tr class="text-slate-400 text-xs uppercase tracking-wider bg-slate-900/50">
              <th class="px-6 py-4 font-semibold">Hành động</th>
              <th class="px-6 py-4 font-semibold">Người thực hiện</th>
              <th class="px-6 py-4 font-semibold">Dự án</th>
              <th class="px-6 py-4 font-semibold">Thời gian</th>
              <th class="px-6 py-4 font-semibold text-right">Trạng thái</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-700/50 text-sm">
            <tr class="hover:bg-slate-800/50 transition-colors">
              <td class="px-6 py-4 text-white font-medium">Cập nhật thiết kế UI</td>
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-8 h-8 rounded-full bg-blue-500/20 text-blue-400 flex items-center justify-center font-bold mr-3 text-xs">TH</div>
                  <span class="text-slate-300 font-medium">Trần Hùng</span>
                </div>
              </td>
              <td class="px-6 py-4 text-slate-400">Mobile App</td>
              <td class="px-6 py-4 text-slate-500">2 phút trước</td>
              <td class="px-6 py-4 text-right">
                <span class="px-3 py-1 text-[10px] font-black rounded-full bg-green-500/10 text-green-400 border border-green-500/20">HOÀN THÀNH</span>
              </td>
            </tr>
            
            <tr class="hover:bg-slate-800/50 transition-colors">
              <td class="px-6 py-4 text-white font-medium">Tạo báo cáo tháng 10</td>
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-8 h-8 rounded-full bg-yellow-500/20 text-yellow-400 flex items-center justify-center font-bold mr-3 text-xs">LA</div>
                  <span class="text-slate-300 font-medium">Lê Anh</span>
                </div>
              </td>
              <td class="px-6 py-4 text-slate-400">Quản trị hệ thống</td>
              <td class="px-6 py-4 text-slate-500">15 phút trước</td>
              <td class="px-6 py-4 text-right">
                <span class="px-3 py-1 text-[10px] font-black rounded-full bg-yellow-500/10 text-yellow-400 border border-yellow-500/20">ĐANG XỬ LÝ</span>
              </td>
            </tr>

            <tr class="hover:bg-slate-800/50 transition-colors">
              <td class="px-6 py-4 text-white font-medium">Xoá người dùng cũ</td>
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-8 h-8 rounded-full bg-slate-600 text-white flex items-center justify-center font-bold mr-3 text-xs">QT</div>
                  <span class="text-slate-300 font-medium">Quản trị viên</span>
                </div>
              </td>
              <td class="px-6 py-4 text-slate-400">Bảo mật</td>
              <td class="px-6 py-4 text-slate-500">1 giờ trước</td>
              <td class="px-6 py-4 text-right">
                <span class="px-3 py-1 text-[10px] font-black rounded-full bg-green-500/10 text-green-400 border border-green-500/20">HOÀN THÀNH</span>
              </td>
            </tr>

            <tr class="hover:bg-slate-800/50 transition-colors">
              <td class="px-6 py-4 text-white font-medium">Phê duyệt ngân sách</td>
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-8 h-8 rounded-full bg-purple-500/20 text-purple-400 flex items-center justify-center font-bold mr-3 text-xs">NM</div>
                  <span class="text-slate-300 font-medium">Nguyễn Mai</span>
                </div>
              </td>
              <td class="px-6 py-4 text-slate-400">FinTech Pro</td>
              <td class="px-6 py-4 text-slate-500">3 giờ trước</td>
              <td class="px-6 py-4 text-right">
                <span class="px-3 py-1 text-[10px] font-black rounded-full bg-red-500/10 text-red-400 border border-red-500/20">BỊ TỪ CHỐI</span>
              </td>
            </tr>

            <tr class="hover:bg-slate-800/50 transition-colors">
              <td class="px-6 py-4 text-white font-medium">Fix bug login</td>
              <td class="px-6 py-4">
                <div class="flex items-center">
                  <div class="w-8 h-8 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold mr-3 text-xs">PH</div>
                  <span class="text-slate-300 font-medium">Phạm Hải</span>
                </div>
              </td>
              <td class="px-6 py-4 text-slate-400">Mobile App</td>
              <td class="px-6 py-4 text-slate-500">5 giờ trước</td>
              <td class="px-6 py-4 text-right">
                <span class="px-3 py-1 text-[10px] font-black rounded-full bg-green-500/10 text-green-400 border border-green-500/20">HOÀN THÀNH</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
  </div>
</template>

<script setup>
// Đây là Component giao diện mẫu, dữ liệu hiện tại đang được code cứng (Mock data) 
// để đảm bảo giao diện hiển thị đẹp mắt ngay lập tức.
// Bạn có thể dễ dàng thay thế bằng dữ liệu lấy từ Java API (fetch) ở các bước sau.
</script>