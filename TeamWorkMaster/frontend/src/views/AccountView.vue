<template>
  <div class="min-h-screen bg-[#f8f9fa] flex font-sans transition-all duration-300">
    
    <aside class="w-64 bg-slate-900 text-white flex flex-col shadow-2xl z-20 shrink-0 relative">
     <div class="p-6 border-b border-slate-800 flex items-center space-x-3 shrink-0">
        <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center font-black text-xl shadow-lg shadow-blue-500/30 border border-blue-400/20">P</div>
        
        <div>
          <h2 class="text-xl font-black tracking-tighter text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-indigo-300">TEAMWORK</h2>
          <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5">Master System</p>
        </div>
      </div>
      <nav class="flex-1 p-4 space-y-2">
        <router-link to="/dashboard" class="flex items-center space-x-3 px-4 py-3 rounded-xl font-semibold transition-all hover:bg-slate-800 text-slate-400 hover:text-white">
          <span class="text-xl shrink-0">📊</span><span>Bảng điều khiển</span>
        </router-link>
        <router-link to="/projects" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
          <span class="text-xl shrink-0">📂</span><span>Dự án</span>
        </router-link>
        <router-link to="/tasks" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-xl font-semibold text-slate-400 hover:text-white transition-all">
          <span class="text-xl shrink-0">☑️</span><span>Công việc của tôi</span>
        </router-link>
        <router-link to="/account" class="flex items-center space-x-3 px-4 py-3 bg-blue-600/10 text-blue-500 rounded-xl font-bold transition-all">
          <span class="text-xl shrink-0">👤</span><span>Tài khoản</span>
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
        <h1 class="text-xl font-black text-slate-800 truncate pr-4">Quản lý Tài khoản</h1>
        <div class="flex items-center space-x-6 shrink-0">
          <div class="relative" v-click-outside="() => showNotifMenu = false">
            <button @click="toggleNotifMenu" class="relative p-2 text-slate-400 hover:bg-slate-100 rounded-full transition-all focus:outline-none">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>
            </button>
          </div>
          <div class="flex items-center space-x-3 border-l border-slate-300 pl-6 hidden sm:flex">
            <div class="text-right">
              <p class="text-sm font-bold text-slate-700">{{ userProfile.fullName }}</p>
              <p class="text-xs text-blue-500 font-bold">Đang trực tuyến</p>
            </div>
            <div class="w-10 h-10 bg-gradient-to-tr from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-black shadow-md uppercase">{{ firstLetter }}</div>
          </div>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-6 md:p-10 custom-scrollbar">
        <div class="max-w-6xl mx-auto space-y-8">
          <div>
            <h2 class="text-3xl font-black text-slate-900 mb-2">Hồ sơ cá nhân</h2>
            <p class="text-slate-500 mt-1 font-medium">Quản lý thông tin định danh và bảo mật tài khoản của bạn.</p>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div class="lg:col-span-1">
              <div class="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm flex flex-col items-center text-center relative overflow-hidden">
                <div class="absolute top-0 left-0 w-full h-24 bg-gradient-to-r from-blue-600 to-indigo-600"></div>
                <div class="w-24 h-24 bg-white rounded-full p-1.5 relative z-10 mt-6 mb-4 shadow-md">
                  <div class="w-full h-full bg-gradient-to-tr from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white text-4xl font-black uppercase">
                    {{ firstLetter }}
                  </div>
                </div>
                <h3 class="text-xl font-black text-slate-800 mb-1">{{ userProfile.fullName }}</h3>
                <span class="px-3 py-1 bg-blue-50 text-blue-600 text-[10px] font-black uppercase tracking-widest rounded-lg border border-blue-100 mb-6">
                  {{ userProfile.role === 'ADMIN' ? 'Quản trị viên' : 'Thành viên' }}
                </span>
                <div class="w-full space-y-4 text-left border-t border-slate-100 pt-6">
                  <div>
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Tài khoản đăng nhập</p>
                    <p class="text-sm font-bold text-slate-700 bg-slate-50 px-3 py-2 rounded-lg border border-slate-100">@{{ userProfile.username }}</p>
                  </div>
                  <div>
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Email liên hệ</p>
                    <p class="text-sm font-bold text-slate-700 bg-slate-50 px-3 py-2 rounded-lg border border-slate-100 truncate">{{ userProfile.email || 'Chưa cập nhật email' }}</p>
                  </div>
                  <div>
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">ID Người dùng</p>
                    <p class="text-sm font-bold text-slate-700 bg-slate-50 px-3 py-2 rounded-lg border border-slate-100">#{{ userProfile.userId }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="lg:col-span-2">
              <div class="bg-white rounded-3xl p-8 border border-slate-200 shadow-sm w-full">
                
                <div class="flex items-center space-x-3 mb-8 pb-6 border-b border-slate-100">
                  <div class="w-12 h-12 bg-orange-100 text-orange-600 rounded-2xl flex items-center justify-center text-2xl shadow-inner shrink-0">🔒</div>
                  <div>
                    <h3 class="text-xl font-black text-slate-800">Đổi mật khẩu</h3>
                    <p class="text-xs text-slate-500 font-medium mt-1">Đảm bảo tài khoản của bạn đang sử dụng một mật khẩu mạnh và an toàn.</p>
                  </div>
                </div>

                <form @submit.prevent="handleChangePassword" class="space-y-6 max-w-2xl">
                  
                  <div>
                    <label class="block text-xs font-black text-slate-500 uppercase tracking-widest mb-2 ml-1">Mật khẩu hiện tại <span class="text-red-500">*</span></label>
                    <div class="relative">
                      <input 
                        v-model="passForm.oldPassword" 
                        :type="showOld ? 'text' : 'password'" 
                        required placeholder="••••••••"
                        class="w-full pl-5 pr-12 py-3.5 rounded-2xl bg-slate-50 border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all text-slate-700 font-bold tracking-widest"
                      >
                      <button type="button" @click="showOld = !showOld" class="absolute right-4 top-3.5 text-slate-400 hover:text-blue-500 transition-colors focus:outline-none">
                        <svg v-if="!showOld" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" /></svg>
                        <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>
                      </button>
                    </div>
                  </div>

                  <div>
                    <label class="block text-xs font-black text-slate-500 uppercase tracking-widest mb-2 ml-1">Mật khẩu mới <span class="text-red-500">*</span></label>
                    <div class="relative">
                      <input 
                        v-model="passForm.newPassword" 
                        :type="showNew ? 'text' : 'password'" 
                        required placeholder="••••••••"
                        class="w-full pl-5 pr-12 py-3.5 rounded-2xl bg-slate-50 border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all text-slate-700 font-bold tracking-widest"
                      >
                      <button type="button" @click="showNew = !showNew" class="absolute right-4 top-3.5 text-slate-400 hover:text-blue-500 transition-colors focus:outline-none">
                        <svg v-if="!showNew" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" /></svg>
                        <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>
                      </button>
                    </div>
                    <p class="text-[10px] text-slate-400 font-bold mt-2 ml-2">Mật khẩu phải có ít nhất 6 ký tự.</p>
                  </div>

                  <div>
                    <label class="block text-xs font-black text-slate-500 uppercase tracking-widest mb-2 ml-1">Xác nhận mật khẩu mới <span class="text-red-500">*</span></label>
                    <div class="relative">
                      <input 
                        v-model="passForm.confirmPassword" 
                        :type="showConfirm ? 'text' : 'password'" 
                        required placeholder="••••••••"
                        class="w-full pl-5 pr-12 py-3.5 rounded-2xl bg-slate-50 border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all text-slate-700 font-bold tracking-widest"
                      >
                      <button type="button" @click="showConfirm = !showConfirm" class="absolute right-4 top-3.5 text-slate-400 hover:text-blue-500 transition-colors focus:outline-none">
                        <svg v-if="!showConfirm" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" /></svg>
                        <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>
                      </button>
                    </div>
                    <p v-if="passwordError" class="text-[10px] text-red-500 font-bold mt-2 ml-2">{{ passwordError }}</p>
                  </div>

                  <div class="pt-4">
                    <button 
                      type="submit" 
                      :disabled="isSubmitting || !!passwordError"
                      class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-black text-sm px-8 py-4 rounded-2xl shadow-lg shadow-blue-500/30 transition-all uppercase tracking-widest active:scale-95 flex items-center justify-center min-w-[200px]"
                    >
                      <span v-if="isSubmitting" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></span>
                      {{ isSubmitting ? 'Đang cập nhật...' : 'Lưu mật khẩu mới' }}
                    </button>
                  </div>

                </form>
              </div>
            </div>

          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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

const showNotifMenu = ref(false);
const toggleNotifMenu = () => { showNotifMenu.value = !showNotifMenu.value; };


const showOld = ref(false);
const showNew = ref(false);
const showConfirm = ref(false);

const userProfile = ref({
  userId: '',
  username: 'Khách',
  fullName: 'Người dùng',
  role: 'MEMBER',
  email: '' 
});

const firstLetter = computed(() => userProfile.value.fullName.charAt(0).toUpperCase());

const passForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' });
const isSubmitting = ref(false);

onMounted(() => {
  userProfile.value.userId = localStorage.getItem("userId") || 'N/A';
  userProfile.value.username = localStorage.getItem("username") || 'Khách';
  userProfile.value.fullName = localStorage.getItem("fullName") || 'Người dùng';
  userProfile.value.role = localStorage.getItem("role") || 'MEMBER';
  
  fetchUserProfile(); 
});

const fetchUserProfile = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/users/profile?id=${userProfile.value.userId}`);
    if (response.ok) {
      const data = await response.json();
      userProfile.value.email = data.email;
      userProfile.value.username = data.username;
    }
  } catch (error) {
    console.log("Lỗi lấy Profile");
  }
};

const passwordError = computed(() => {
  if (passForm.value.confirmPassword && passForm.value.newPassword !== passForm.value.confirmPassword) {
    return '❌ Xác nhận mật khẩu không khớp!';
  }
  if (passForm.value.newPassword && passForm.value.newPassword.length < 6) {
    return '❌ Mật khẩu mới phải dài ít nhất 6 ký tự!';
  }
  return '';
});

const handleChangePassword = async () => {
  if (passwordError.value) return;

  isSubmitting.value = true;
  try {
    const response = await fetch('http://localhost:8080/api/users/change-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: userProfile.value.userId,
        oldPassword: passForm.value.oldPassword,
        newPassword: passForm.value.newPassword
      })
    });

    const data = await response.json();

    if (response.ok && data.success) {
      alert("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
      handleLogout();
    } else {
      alert(`Lỗi: ${data.message || 'Mật khẩu cũ không chính xác!'}`);
    }
  } catch (error) {
    alert("Lỗi kết nối máy chủ! Vui lòng kiểm tra lại Backend.");
  } finally {
    isSubmitting.value = false;
  }
};

const handleLogout = () => {
  localStorage.clear();
  router.push("/");
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
</style>