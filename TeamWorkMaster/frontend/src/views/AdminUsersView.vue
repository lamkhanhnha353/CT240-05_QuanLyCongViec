<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center bg-slate-800 p-6 rounded-xl shadow-sm border border-slate-700">
      <div>
        <h1 class="text-2xl font-bold text-white">Quản lý người dùng</h1>
        <p class="text-slate-400 text-sm mt-1">Hệ thống chỉ hiển thị tài khoản của người sử dụng (Member)</p>
      </div>
      <button @click="isAddModalOpen = true" class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg font-semibold flex items-center space-x-2 transition shadow-lg shadow-blue-500/20">
        <span>+ Tạo tài khoản mới</span>
      </button>
    </div>

    <!-- Bảng -->
    <div class="bg-slate-800 rounded-xl shadow-sm overflow-hidden border border-slate-700">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-900 text-slate-300 text-sm uppercase tracking-wider border-b border-slate-700">
            <th class="p-4 font-semibold text-center w-16">STT</th>
            <th class="p-4 font-semibold">Người dùng</th>
            <th class="p-4 font-semibold">Email</th>
            <th class="p-4 font-semibold">Trạng thái</th>
            <th class="p-4 font-semibold text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-700 text-slate-200">
          <tr v-for="(user, index) in users" :key="user.id" class="hover:bg-slate-750 transition-colors">
            <td class="p-4 text-slate-400 font-bold text-center">{{ index + 1 }}</td>
            <td class="p-4 font-medium">{{ user.username }} <br><span class="text-xs text-slate-400">{{ user.fullname }}</span></td>
            <td class="p-4">{{ user.email }}</td>
            <td class="p-4">
              <span v-if="user.isActive" class="px-3 py-1 text-xs font-bold rounded-full bg-green-500/20 text-green-400">Hoạt động</span>
              <span v-else class="px-3 py-1 text-xs font-bold rounded-full bg-red-500/20 text-red-400">Bị khóa</span>
            </td>
            <td class="p-4 text-right space-x-2">
              <button @click="openEditModal(user)" class="p-2 rounded-lg text-slate-400 hover:text-blue-400 hover:bg-slate-700 hover:scale-110 transition-all duration-200" title="Sửa thông tin">✏️</button>
              <button class="p-2 rounded-lg text-slate-400 hover:text-yellow-400 hover:bg-slate-700 hover:scale-110 transition-all duration-200" title="Khóa/Mở khóa">🔒</button>
              <button class="p-2 rounded-lg text-slate-400 hover:text-red-400 hover:bg-slate-700 hover:scale-110 transition-all duration-200" title="Xóa tài khoản">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Gọi Modal (Đã tích hợp sự kiện refresh bảng) -->
    <AddUserModal 
      v-if="isAddModalOpen" 
      @close="isAddModalOpen = false" 
      @refresh="loadUsers" 
    />
    
    <EditUserModal 
      v-if="isEditModalOpen" 
      :user="selectedUser" 
      @close="isEditModalOpen = false" 
      @refresh="loadUsers" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
// PHẢI CHẮC CHẮN LÀ BẠN CÓ ĐỦ 2 FILE NÀY TRONG THƯ MỤC COMPONENTS
import AddUserModal from '../components/AddUserModal.vue'
import EditUserModal from '../components/EditUserModal.vue'

const isAddModalOpen = ref(false)
const isEditModalOpen = ref(false)
const users = ref([]) 
const selectedUser = ref(null) 

const loadUsers = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/admin/users')
    users.value = await response.json() 
  } catch (error) {
    console.error("Lỗi lấy danh sách:", error)
  }
}

onMounted(() => {
  loadUsers()
})

const openEditModal = (user) => {
  selectedUser.value = user 
  isEditModalOpen.value = true
}
</script>