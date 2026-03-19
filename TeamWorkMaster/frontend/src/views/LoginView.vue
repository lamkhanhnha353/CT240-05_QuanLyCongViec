<template>
  <div class="min-h-screen bg-[#0f172a] flex items-center justify-center p-4 font-sans">
    <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md p-10 relative overflow-hidden">
      
      <!-- Viền xanh trên cùng của thẻ -->
      <div class="absolute top-0 left-0 w-full h-2 bg-blue-600"></div>

      <!-- Tiêu đề -->
      <div class="text-center mb-10 mt-2">
        <h1 class="text-3xl font-black text-slate-800 uppercase tracking-wider">TEAMWORK MASTER</h1>
        <div class="flex flex-col items-center mt-3">
          <div class="w-12 h-1 bg-blue-600 rounded-full mb-3"></div>
          <p class="text-slate-400 font-bold text-xs tracking-[0.2em] uppercase">CORE DASHBOARD V1.0</p>
        </div>
      </div>
      
      <!-- Form Đăng nhập -->
      <form @submit.prevent="handleLogin" class="space-y-6">
        
        <div>
          <label class="block text-[11px] font-black text-slate-500 uppercase tracking-widest mb-2">TÀI KHOẢN</label>
          <input 
            v-model="username" 
            type="text" 
            placeholder="Nhập tên tài khoản..."
            class="w-full bg-slate-50 border border-slate-100 rounded-2xl px-5 py-4 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all font-bold"
          >
        </div>
        
        <div>
          <label class="block text-[11px] font-black text-slate-500 uppercase tracking-widest mb-2">MẬT KHẨU</label>
          <input 
            v-model="password" 
            type="password" 
            placeholder="••••••"
            class="w-full bg-slate-50 border border-slate-100 rounded-2xl px-5 py-4 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all font-bold tracking-widest"
          >
        </div>
        
        <button 
          type="submit" 
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-black text-sm py-4 rounded-2xl shadow-lg shadow-blue-500/40 transition-all uppercase tracking-widest mt-2 active:scale-95"
        >
          ĐĂNG NHẬP HỆ THỐNG
        </button>
      </form>
      
      <!-- Footer -->
      <div class="mt-8 pt-8 border-t border-slate-100 text-center">
        <p class="text-slate-500 font-semibold text-sm">
          Chưa có tài khoản? 
          <router-link to="/register" class="text-blue-600 hover:text-blue-800 transition-colors">Đăng ký ngay</router-link>
        </p>
        <p class="text-slate-300 font-bold text-[10px] uppercase tracking-widest mt-8">
          © 2026 DEVELOPER TEAM
        </p>
      </div>
      
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')

const handleLogin = async () => {
  if (!username.value || !password.value) {
    alert("Vui lòng điền đầy đủ Tài khoản và Mật khẩu!");
    return;
  }
  
  try {
    const response = await fetch('http://localhost:8080/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        username: username.value, 
        password: password.value 
      })
    });
    
    console.log('Response Status:', response.status);
    console.log('Response OK:', response.ok);
    
    const data = await response.json();
    console.log('Response Data:', data);
    
    if (data.success) {
      // Lưu Token giả lập vào LocalStorage
      localStorage.setItem('isLoggedIn', 'true');
      localStorage.setItem('username', username.value);
      localStorage.setItem('role', data.role); // Data.role từ Java gửi sang
      localStorage.setItem('userId', data.userId);
      // Force sync localStorage
      await new Promise(resolve => setTimeout(resolve, 100));
      
      console.log('LocalStorage saved:', {
        isLoggedIn: localStorage.getItem('isLoggedIn'),
        username: localStorage.getItem('username'),
        role: localStorage.getItem('role')
      });
      
      // Chuyển hướng theo Quyền (Role)
      if (data.role === 'ADMIN') {
        console.log('Navigating to /admin');
        router.push('/admin'); // Admin thì ném vào Tổng hành dinh
      } else {
        console.log('Navigating to /dashboard');
        router.push('/dashboard'); // Member thì vào Bảng chung
      }
    } else {
      // Hiển thị thông báo khi sai pass hoặc bị khóa
      alert(data.message || 'Đăng nhập thất bại');
      console.log('Login failed:', data);
    }
  } catch (error) {
    alert("Không thể kết nối đến Máy chủ Java! Vui lòng kiểm tra lại Backend.");
    console.error('Login error:', error);
  }
}
</script>