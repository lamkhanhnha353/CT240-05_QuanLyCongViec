<template>
  <div class="min-h-screen bg-slate-50 flex font-sans">
    
    <aside class="w-64 bg-slate-900 text-white flex flex-col shadow-2xl z-10">
      <div class="p-6 border-b border-slate-800">
        <h2 class="text-2xl font-black text-blue-500 tracking-tighter">TEAMWORK</h2>
        <p class="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1">Master System</p>
      </div>
      
      <nav class="flex-1 p-4 space-y-2">
        <a href="#" class="block px-4 py-3 bg-blue-600 rounded-xl font-bold shadow-lg shadow-blue-900/50 transition-all">Bảng điều khiển</a>
        <a href="#" class="block px-4 py-3 hover:bg-slate-800 rounded-xl font-bold text-slate-400 hover:text-white transition-all">Quản lý Dự án</a>
        <a href="#" class="block px-4 py-3 hover:bg-slate-800 rounded-xl font-bold text-slate-400 hover:text-white transition-all">Công việc của tôi</a>
      </nav>

      <div class="p-4 border-t border-slate-800">
        <button @click="handleLogout" class="w-full py-3 text-center text-red-400 font-bold hover:bg-red-500/10 rounded-xl transition-all">
          Đăng xuất
        </button>
      </div>
    </aside>

    <main class="flex-1 p-10">
      <header class="flex justify-between items-center mb-10">
        <div>
          <h1 class="text-3xl font-extrabold text-slate-800">Tổng quan hệ thống</h1>
          <p class="text-slate-500 font-medium mt-1">Chào mừng bạn quay trở lại làm việc!</p>
        </div>
        <div class="flex items-center space-x-4 bg-white px-5 py-2 rounded-full shadow-sm border border-slate-200">
          <span class="font-bold text-slate-700">Xin chào, {{ currentUser }}</span>
          
          <div class="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white font-black shadow-md">
            {{ firstLetter }}
          </div>
        </div>
      </header>

      <div class="grid grid-cols-3 gap-6">
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-100 hover:shadow-md transition-all">
          <h3 class="text-slate-500 font-bold mb-2 uppercase text-xs tracking-wider">Tổng số dự án</h3>
          <p class="text-4xl font-black text-blue-600">12</p>
        </div>
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-100 hover:shadow-md transition-all">
          <h3 class="text-slate-500 font-bold mb-2 uppercase text-xs tracking-wider">Công việc đang làm</h3>
          <p class="text-4xl font-black text-orange-500">34</p>
        </div>
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-100 hover:shadow-md transition-all">
          <h3 class="text-slate-500 font-bold mb-2 uppercase text-xs tracking-wider">Hoàn thành tháng này</h3>
          <p class="text-4xl font-black text-emerald-500">89</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Tạo biến để lưu tên người dùng và chữ cái đầu tiên
const currentUser = ref('Khách')
const firstLetter = ref('K')

// Hàm này sẽ tự động chạy ngay khi trang Dashboard vừa mở lên
onMounted(() => {
  const storedUser = localStorage.getItem('username')
  if (storedUser) {
    currentUser.value = storedUser
    // Lấy chữ cái đầu tiên và viết hoa (Ví dụ: 'admin' -> 'A')
    firstLetter.value = storedUser.charAt(0).toUpperCase()
  }
})

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn');
  localStorage.removeItem('username');
  alert("Đã đăng xuất khỏi hệ thống!");
  router.push('/');
}
</script>