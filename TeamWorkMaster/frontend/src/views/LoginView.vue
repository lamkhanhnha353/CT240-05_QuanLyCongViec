<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-900 font-sans">
    <div class="max-w-md w-full bg-white p-10 rounded-3xl shadow-2xl border-t-8 border-blue-600">
      <div class="text-center mb-10">
        <h1 class="text-3xl font-black text-slate-800 tracking-tighter">TEAMWORK MASTER</h1>
        <div class="h-1 w-20 bg-blue-600 mx-auto mt-2 rounded-full"></div>
        <p class="text-slate-400 mt-3 text-xs uppercase font-bold tracking-widest">Core Dashboard v1.0</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-5">
        <div>
          <label class="block text-xs font-black text-slate-500 uppercase mb-2 ml-1">Tai khoan</label>
          <input v-model="username" type="text" class="w-full px-5 py-4 rounded-2xl bg-slate-50 border-none ring-2 ring-slate-100 focus:ring-blue-500 outline-none transition-all text-slate-700 font-bold" placeholder="admin">
        </div>

        <div>
          <label class="block text-xs font-black text-slate-500 uppercase mb-2 ml-1">Mat khau</label>
          <input v-model="password" type="password" class="w-full px-5 py-4 rounded-2xl bg-slate-50 border-none ring-2 ring-slate-100 focus:ring-blue-500 outline-none transition-all text-slate-700 font-bold" placeholder="••••••••">
        </div>

        <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-black py-4 rounded-2xl shadow-lg shadow-blue-200 transition-all active:scale-95 uppercase tracking-widest text-sm">
          Dang Nhap He Thong
        </button>
      </form>

      <div class="mt-6 text-center">
        <button @click="$router.push('/register')" type="button" class="text-sm font-bold text-slate-400 hover:text-blue-500 transition-colors">
          Chưa có tài khoản? Đăng ký ngay
        </button>
      </div>

      <div class="mt-8 pt-6 border-t border-slate-100 text-center">
        <span class="text-[10px] font-bold text-slate-300 uppercase tracking-widest">© 2026 Developer Team</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router' // 1. Import hàm chuyển trang

const username = ref('')
const password = ref('')
const router = useRouter() // 2. Khởi tạo router

const handleLogin = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/login', {
      username: username.value,
      password: password.value
    });

    if (response.data.success) {
      alert("Đăng nhập thành công!");
      
      // Phát vé: Lưu trạng thái và tên tài khoản vào bộ nhớ trình duyệt
      localStorage.setItem('isLoggedIn', 'true');
      localStorage.setItem('username', username.value);
      
      // Chuyển hướng sang trang Dashboard
      router.push('/dashboard'); 
    }
    
    else {
      alert("Tài khoản hoặc mật khẩu không chính xác!");
    }
  } catch (error) {
    console.error("Lỗi kết nối Server:", error);
    alert("Không thể kết nối đến Backend. Bạn đã bật Server Java chưa?");
  }
}
</script>