<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-900 font-sans py-10">
    <div class="max-w-md w-full bg-white p-10 rounded-3xl shadow-2xl border-t-8 border-emerald-500">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-black text-slate-800 tracking-tighter">ĐĂNG KÝ TÀI KHOẢN</h1>
        <div class="h-1 w-20 bg-emerald-500 mx-auto mt-2 rounded-full"></div>
      </div>

      <form @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label class="block text-xs font-black text-slate-500 uppercase mb-2 ml-1">Họ và Tên</label>
          <input v-model="fullName" type="text" required class="w-full px-5 py-3 rounded-2xl bg-slate-50 border-none ring-2 ring-slate-100 focus:ring-emerald-500 outline-none transition-all text-slate-700 font-bold" placeholder="Nguyễn Văn A">
        </div>

        <div>
          <label class="block text-xs font-black text-slate-500 uppercase mb-2 ml-1">Email</label>
          <input v-model="email" type="email" required class="w-full px-5 py-3 rounded-2xl bg-slate-50 border-none ring-2 ring-slate-100 focus:ring-emerald-500 outline-none transition-all text-slate-700 font-bold" placeholder="nva@teamwork.com">
        </div>

        <div>
          <label class="block text-xs font-black text-slate-500 uppercase mb-2 ml-1">Tài khoản</label>
          <input v-model="username" type="text" required class="w-full px-5 py-3 rounded-2xl bg-slate-50 border-none ring-2 ring-slate-100 focus:ring-emerald-500 outline-none transition-all text-slate-700 font-bold" placeholder="nva123">
        </div>

        <div>
          <label class="block text-xs font-black text-slate-500 uppercase mb-2 ml-1">Mật khẩu</label>
          <input v-model="password" type="password" required class="w-full px-5 py-3 rounded-2xl bg-slate-50 border-none ring-2 ring-slate-100 focus:ring-emerald-500 outline-none transition-all text-slate-700 font-bold" placeholder="••••••••">
        </div>

        <button type="submit" class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-black py-4 rounded-2xl shadow-lg shadow-emerald-200 transition-all active:scale-95 uppercase tracking-widest text-sm mt-4">
          Tạo Tài Khoản
        </button>
      </form>

      <div class="mt-6 text-center">
        <button @click="router.push('/')" class="text-sm font-bold text-slate-400 hover:text-emerald-500 transition-colors">
          Đã có tài khoản? Quay lại Đăng nhập
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const fullName = ref('')
const email = ref('')
const username = ref('')
const password = ref('')
const router = useRouter()

const handleRegister = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/register', {
      username: username.value,
      password: password.value,
      fullName: fullName.value,
      email: email.value
    });

    if (response.data.success) {
      alert("Đăng ký thành công! Hãy đăng nhập để tiếp tục.");
      router.push('/'); // Quay ve trang Dang nhap
    }
  } catch (error) {
    if (error.response && error.response.status === 400) {
      alert("Lỗi: Tài khoản hoặc Email này đã có người sử dụng!");
    } else {
      alert("Không thể kết nối đến Backend Java.");
    }
  }
}
</script>