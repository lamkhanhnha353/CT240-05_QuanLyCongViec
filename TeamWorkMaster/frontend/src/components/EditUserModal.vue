<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-slate-800 border border-slate-700 rounded-xl shadow-2xl w-full max-w-md overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-700 flex justify-between items-center bg-slate-900/50">
        <h3 class="text-lg font-bold text-white">Cập Nhật Thông Tin</h3>
        <button @click="$emit('close')" class="text-slate-400 hover:text-red-400 transition text-xl font-bold">&times;</button>
      </div>

      <form @submit.prevent="submitForm" class="p-6 space-y-4">
        
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-1">Tên tài khoản (Không được sửa)</label>
          <input v-model="formData.username" disabled type="text" class="w-full bg-slate-700 border border-slate-600 rounded-lg px-4 py-2 text-slate-400 cursor-not-allowed focus:outline-none">
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

        <div class="bg-slate-900/50 p-4 rounded-lg border border-slate-700 space-y-3 mt-4">
          <h4 class="text-sm font-semibold text-slate-300 mb-2 flex justify-between items-center">
            Đổi mật khẩu
            <span class="text-xs font-normal text-slate-500">(Để trống nếu không đổi)</span>
          </h4>
          
          <div>
            <input v-model="formData.password" @input="validatePassword" @blur="validatePassword" placeholder="Nhập mật khẩu mới..." type="password" 
              :class="['w-full bg-slate-900 border rounded-lg px-4 py-2 text-white text-sm focus:outline-none transition', errors.password ? 'border-red-500' : 'border-slate-700 focus:border-blue-500']">
            <p v-if="errors.password" class="text-red-500 text-xs mt-1 font-medium">{{ errors.password }}</p>
          </div>
        </div>

        <div class="pt-4 flex justify-end space-x-3">
          <button type="button" @click="$emit('close')" class="px-4 py-2 text-slate-300 hover:text-white transition">Hủy</button>
          <button type="submit" class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg font-medium transition">Lưu thông tin</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({ user: { type: Object, required: true } })
const emit = defineEmits(['close', 'refresh'])

// Đã gỡ bỏ oldPassword
const formData = ref({ id: '', username: '', fullname: '', email: '', password: '' })
const errors = ref({ fullname: '', email: '', password: '' })

onMounted(() => {
  formData.value.id = props.user.id
  formData.value.username = props.user.username
  formData.value.fullname = props.user.fullname
  formData.value.email = props.user.email
})

const validateFullname = () => {
  if (!formData.value.fullname.trim()) { errors.value.fullname = 'Vui lòng nhập họ và tên.'; return false; }
  errors.value.fullname = ''; return true;
}

const validateEmail = () => {
  if (!formData.value.email) { errors.value.email = 'Vui lòng không để trống email.'; return false; }
  if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(formData.value.email)) { errors.value.email = 'Email không hợp lệ.'; return false; }
  errors.value.email = ''; return true;
}

// Logic mới chỉ kiểm tra độ dài nếu có nhập password mới
const validatePassword = () => {
  if (!formData.value.password) {
    errors.value.password = ''; 
    return true; 
  }
  if (formData.value.password.length < 6) {
    errors.value.password = 'Mật khẩu mới phải từ 6 ký tự.';
    return false;
  }
  errors.value.password = '';
  return true;
}

const submitForm = async () => {
  const isNameValid = validateFullname();
  const isEmailValid = validateEmail();
  const isPassValid = validatePassword(); 

  if (isNameValid && isEmailValid && isPassValid) {
    try {
      const response = await fetch('http://localhost:8080/api/admin/users/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData.value)
      });
      const data = await response.json();
      
      if (data.success) {
        alert("Cập nhật thành công!");
        emit('refresh'); 
        emit('close');   
      } else {
        // Chỉ bắt mỗi lỗi trùng Email
        if (data.field === 'email') {
          errors.value.email = data.message;
        } else {
          alert(data.message);
        }
      }
    } catch (error) {
      alert("Lỗi kết nối Server Java!");
      console.error(error);
    }
  }
}
</script>