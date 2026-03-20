<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-slate-800 border border-slate-700 rounded-xl shadow-2xl w-full max-w-md overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-700 flex justify-between items-center bg-slate-900/50">
        <h3 class="text-lg font-bold text-white">Thêm Tài Khoản Mới</h3>
        <button @click="$emit('close')" class="text-slate-400 hover:text-red-400 transition text-xl font-bold">&times;</button>
      </div>

      <form @submit.prevent="submitForm" class="p-6 space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-1">Tên tài khoản (Username)</label>
          <input v-model="formData.username" @input="validateUsername" @blur="validateUsername" type="text" 
            :class="['w-full bg-slate-900 border rounded-lg px-4 py-2 text-white focus:outline-none transition', errors.username ? 'border-red-500 focus:border-red-500' : 'border-slate-700 focus:border-blue-500']">
          <p v-if="errors.username" class="text-red-500 text-xs mt-1.5 font-medium">{{ errors.username }}</p>
        </div>
        
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-1">Họ và Tên</label>
          <input v-model="formData.fullname" @input="validateFullname" @blur="validateFullname" type="text" 
            :class="['w-full bg-slate-900 border rounded-lg px-4 py-2 text-white focus:outline-none transition', errors.fullname ? 'border-red-500 focus:border-red-500' : 'border-slate-700 focus:border-blue-500']">
          <p v-if="errors.fullname" class="text-red-500 text-xs mt-1.5 font-medium">{{ errors.fullname }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-1">Email</label>
          <input v-model="formData.email" @input="validateEmail" @blur="validateEmail" type="text" 
            :class="['w-full bg-slate-900 border rounded-lg px-4 py-2 text-white focus:outline-none transition', errors.email ? 'border-red-500 focus:border-red-500' : 'border-slate-700 focus:border-blue-500']">
          <p v-if="errors.email" class="text-red-500 text-xs mt-1.5 font-medium">{{ errors.email }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-300 mb-1">Mật khẩu</label>
          <div class="relative">
            <input v-model="formData.password" @input="validatePassword" @blur="validatePassword" :type="showPassword ? 'text' : 'password'" 
              :class="['w-full bg-slate-900 border rounded-lg pl-4 pr-10 py-2 text-white focus:outline-none transition', errors.password ? 'border-red-500 focus:border-red-500' : 'border-slate-700 focus:border-blue-500']">
            <button type="button" @click="showPassword = !showPassword" class="absolute right-3 top-2.5 text-slate-400 hover:text-white transition">
              <span class="font-bold text-xs">{{ showPassword ? 'ẨN' : 'HIỆN' }}</span>
            </button>
          </div>
          <p v-if="errors.password" class="text-red-500 text-xs mt-1.5 font-medium">{{ errors.password }}</p>
        </div>

        <div class="pt-4 flex justify-end space-x-3">
          <button type="button" @click="$emit('close')" class="px-4 py-2 text-slate-300 hover:text-white transition">Hủy</button>
          <button type="submit" class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg font-medium transition">Tạo tài khoản</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useToast } from '../composables/useToast' // Import composable Toast

const emit = defineEmits(['close', 'refresh']) // Đổi sang 'refresh' để báo cho bảng tải lại
const { addToast } = useToast() // Khởi tạo Toast

const formData = ref({ username: '', fullname: '', email: '', password: '' })
const errors = ref({ username: '', fullname: '', email: '', password: '' })
const showPassword = ref(false)

const validateUsername = () => {
  if (!formData.value.username) { errors.value.username = 'Vui lòng không để trống tên tài khoản.'; return false; }
  if (!/^[a-zA-Z0-9_]+$/.test(formData.value.username)) { errors.value.username = 'Chỉ được chứa chữ cái, số và dấu gạch dưới.'; return false; }
  errors.value.username = ''; return true;
}

const validateFullname = () => {
  if (!formData.value.fullname.trim()) { errors.value.fullname = 'Vui lòng nhập họ và tên.'; return false; }
  errors.value.fullname = ''; return true;
}

const validateEmail = () => {
  if (!formData.value.email) { errors.value.email = 'Vui lòng không để trống email.'; return false; }
  if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(formData.value.email)) { errors.value.email = 'Email không hợp lệ.'; return false; }
  errors.value.email = ''; return true;
}

const validatePassword = () => {
  if (!formData.value.password) { errors.value.password = 'Vui lòng không để trống mật khẩu.'; return false; }
  if (formData.value.password.length < 6) { errors.value.password = 'Mật khẩu phải có ít nhất 6 ký tự.'; return false; }
  errors.value.password = ''; return true;
}

// Gọi trực tiếp API ở đây để bắt lỗi và bôi đỏ
const submitForm = async () => {
  const isUserValid = validateUsername();
  const isNameValid = validateFullname();
  const isEmailValid = validateEmail();
  const isPassValid = validatePassword();

  if (isUserValid && isNameValid && isEmailValid && isPassValid) {
    try {
      const response = await fetch('http://localhost:8080/api/admin/users/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData.value)
      });
      const data = await response.json();
      
      if (data.success) {
        addToast("Tạo tài khoản thành công!", "success");
        emit('refresh'); // Load lại danh sách bảng
        emit('close');   // Đóng Modal
      } else {
        // KIỂM TRA TRÙNG LẶP DỰA VÀO JAVA TRẢ VỀ
        if (data.field === 'username') {
          errors.value.username = data.message;
        } else if (data.field === 'email') {
          errors.value.email = data.message;
        } else {
          addToast(data.message, "error");
        }
      }
    } catch (error) {
      addToast("Lỗi kết nối Server Java!", "error"); 
      console.error(error);
    }
  }
}
</script>