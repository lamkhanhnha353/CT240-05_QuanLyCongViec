<template>
  <div class="min-h-screen bg-[#0f172a] flex items-center justify-center p-4 font-sans">
    <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md p-10 relative overflow-hidden">
      
      <div class="absolute top-0 left-0 w-full h-2 bg-blue-600"></div>

      <div class="text-center mb-10 mt-2">
        <h1 class="text-3xl font-black text-slate-800 uppercase tracking-wider">TEAMWORK MASTER</h1>
        <div class="flex flex-col items-center mt-3">
          <div class="w-12 h-1 bg-blue-600 rounded-full mb-3"></div>
          <p class="text-slate-400 font-bold text-xs tracking-[0.2em] uppercase">CORE DASHBOARD V1.0</p>
        </div>
      </div>
      
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
          <div class="text-right mt-3">
            <button type="button" @click="showForgotPassword = true" class="text-[11px] font-bold text-blue-600 hover:text-blue-800 hover:underline transition-all">
              Quên mật khẩu?
            </button>
          </div>
        </div>
        
        <button 
          type="submit" 
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-black text-sm py-4 rounded-2xl shadow-lg shadow-blue-500/40 transition-all uppercase tracking-widest mt-2 active:scale-95"
        >
          ĐĂNG NHẬP HỆ THỐNG
        </button>
      </form>
      
      <div class="mt-8 pt-8 border-t border-slate-100 text-center">
        <p class="text-slate-500 font-semibold text-sm">
          Chưa có tài khoản? 
          <router-link to="/register" class="text-blue-600 hover:text-blue-800 transition-colors">Đăng ký ngay</router-link>
        </p>
        <p class="text-slate-300 font-bold text-[10px] uppercase tracking-widest mt-8">
          © 2026 DEVELOPER TEAM
        </p>
      </div>
      
      <div v-if="showForgotPassword" class="absolute inset-0 bg-white z-20 p-10 flex flex-col justify-center animate-fade-in">
        <button @click="showForgotPassword = false" class="absolute top-6 right-6 p-2 text-slate-400 hover:text-slate-800 hover:bg-slate-100 rounded-full transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
        
        <div class="text-center mb-8">
          <div class="w-16 h-16 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4 text-2xl shadow-inner">🔒</div>
          <h2 class="text-2xl font-black text-slate-800 uppercase tracking-wider">Khôi phục mật khẩu</h2>
          <p class="text-xs font-bold text-slate-400 mt-2 leading-relaxed">Vui lòng nhập Email đã đăng ký. Hệ thống sẽ gửi một mật khẩu mới gồm 6 ký tự đến Email của bạn.</p>
        </div>

        <form @submit.prevent="submitForgotPassword" class="space-y-6">
          <div>
            <label class="block text-[11px] font-black text-slate-500 uppercase tracking-widest mb-2">EMAIL CỦA BẠN</label>
            <input 
              v-model="forgotEmail" 
              type="email" 
              required
              placeholder="example@gmail.com"
              class="w-full bg-slate-50 border border-slate-100 rounded-2xl px-5 py-4 text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all font-bold"
            >
          </div>
          
          <button 
            type="submit" 
            :disabled="isSendingEmail"
            class="w-full bg-slate-800 hover:bg-slate-900 disabled:opacity-50 text-white font-black text-sm py-4 rounded-2xl shadow-lg transition-all uppercase tracking-widest active:scale-95 flex justify-center items-center"
          >
            <span v-if="isSendingEmail" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></span>
            {{ isSendingEmail ? 'ĐANG XỬ LÝ...' : 'GỬI MẬT KHẨU MỚI' }}
          </button>
        </form>
        <button @click="showForgotPassword = false" class="mt-6 text-[11px] font-bold text-slate-400 hover:text-slate-600 uppercase tracking-widest text-center w-full transition-colors">
          Quay lại đăng nhập
        </button>
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

// --- BIẾN CHO PHẦN QUÊN MẬT KHẨU ---
const showForgotPassword = ref(false)
const forgotEmail = ref('')
const isSendingEmail = ref(false)

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
    
    const data = await response.json();
    
    if (data.success) {
      localStorage.setItem('isLoggedIn', 'true');
      localStorage.setItem('username', username.value);
      localStorage.setItem('role', data.role);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('fullName', data.fullName);
      
      await new Promise(resolve => setTimeout(resolve, 100));
      
      if (data.role === 'ADMIN') {
        router.push('/admin'); 
      } else {
        router.push('/dashboard'); 
      }
    } else {
      alert(data.message || 'Đăng nhập thất bại');
    }
  } catch (error) {
    alert("Không thể kết nối đến Máy chủ Java! Vui lòng kiểm tra lại Backend.");
  }
}

// --- HÀM XỬ LÝ QUÊN MẬT KHẨU ---
const submitForgotPassword = async () => {
  if (!forgotEmail.value) return;
  
  isSendingEmail.value = true;
  try {
    // Gọi xuống Backend, Backend sẽ tự tạo chuỗi 6 ký tự, lưu vào DB và gửi Mail
    const response = await fetch('http://localhost:8080/api/forgot-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: forgotEmail.value })
    });
    
    if (response.ok) {
      alert(`Thành công! Một mật khẩu mới đã được gửi đến email: ${forgotEmail.value}`);
      showForgotPassword.value = false; // Đóng màn hình quên mật khẩu
      forgotEmail.value = ''; // Xóa trắng ô input
    } else {
      const data = await response.json();
      alert(`Lỗi: ${data.error || 'Email không tồn tại trong hệ thống!'}`);
    }
  } catch (error) {
    alert("Lỗi kết nối máy chủ! Vui lòng thử lại sau.");
  } finally {
    isSendingEmail.value = false;
  }
}
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { 
  from { opacity: 0; transform: translateY(-10px); } 
  to { opacity: 1; transform: translateY(0); } 
}
</style>