<template>
  <div class="space-y-6 pb-6">
    
    <div class="bg-slate-800 p-6 rounded-xl shadow-sm border border-slate-700 flex flex-col md:flex-row md:justify-between md:items-center gap-4">
      <div>
        <h1 class="text-2xl font-bold text-white">Quản lý Cư dân mạng</h1>
        <p class="text-slate-400 text-sm mt-1">Hệ thống hiển thị tài khoản người sử dụng (Member)</p>
      </div>
      
      <div class="flex flex-col md:flex-row items-center space-y-3 md:space-y-0 md:space-x-4 w-full md:w-auto">
        
        <select v-model="statusFilter" class="w-full md:w-40 bg-slate-900 border border-slate-700 text-slate-300 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block p-2.5 outline-none cursor-pointer">
          <option value="all">Tất cả trạng thái</option>
          <option value="active">Đang hoạt động</option>
          <option value="locked">Đã bị khóa</option>
        </select>

        <div class="relative w-full md:w-64">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg class="w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Tìm username, họ tên, email..." 
            class="w-full bg-slate-900 border border-slate-700 text-white text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block pl-10 p-2.5 transition-colors outline-none"
          >
        </div>

        <button @click="isAddModalOpen = true" class="w-full md:w-auto whitespace-nowrap bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg font-semibold flex items-center justify-center space-x-2 transition shadow-lg shadow-blue-500/20">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
          <span>Tạo mới</span>
        </button>
      </div>
    </div>

    <div class="bg-slate-800 rounded-xl shadow-sm border border-slate-700 flex flex-col">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse whitespace-nowrap">
          <thead>
            <tr class="bg-slate-900 text-slate-400 text-xs uppercase tracking-wider border-b border-slate-700">
              <th class="py-4 px-4 font-semibold text-center w-12">STT</th>
              <th class="py-4 px-4 font-semibold">Username</th>
              <th class="py-4 px-4 font-semibold">Họ và Tên</th>
              <th class="py-4 px-4 font-semibold">Email</th>
              <th class="py-4 px-4 font-semibold w-40">Ngày tạo</th> <th class="py-4 px-4 font-semibold text-center w-32">Trạng thái</th>
              <th class="py-4 px-4 font-semibold text-right w-36">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-700/50 text-slate-300 text-sm">
            <tr v-for="(user, index) in paginatedUsers" :key="user.id" class="hover:bg-slate-750/50 transition-colors" :class="!user.isActive ? 'opacity-60 bg-slate-800/30' : ''">
              
              <td class="py-4 px-4 text-slate-500 font-bold text-center">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
              <td class="py-4 px-4 font-bold text-white">{{ user.username }}</td>
              <td class="py-4 px-4 font-medium">{{ user.fullname }}</td>
              <td class="py-4 px-4">{{ user.email }}</td>
              
              <td class="py-4 px-4 text-xs text-slate-400 font-medium">
                {{ formatDateTime(user.createdAt) }}
              </td>
              
              <td class="py-4 px-4 text-center">
                <span v-if="user.isActive" class="px-3 py-1 text-[11px] font-black rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 uppercase tracking-widest">Hoạt động</span>
                <span v-else class="px-3 py-1 text-[11px] font-black rounded-full bg-red-500/10 text-red-400 border border-red-500/20 uppercase tracking-widest">Bị khóa</span>
              </td>
              
              <td class="py-4 px-4 text-right space-x-1">
                <button @click="openEditModal(user)" class="p-2 rounded-lg text-slate-400 hover:text-blue-400 hover:bg-slate-700 transition-all" title="Sửa thông tin">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                </button>
                
                <button @click="handleToggleLock(user)" class="p-2 rounded-lg transition-all" 
                  :class="user.isActive ? 'text-slate-400 hover:text-yellow-400 hover:bg-slate-700' : 'text-yellow-500 bg-yellow-500/10 hover:bg-yellow-500/20'" 
                  :title="user.isActive ? 'Khóa tài khoản' : 'Mở khóa tài khoản'">
                  <svg v-if="!user.isActive" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path></svg>
                  <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                </button>

                <button @click="openDeleteModal(user)" class="p-2 rounded-lg text-slate-400 hover:text-red-400 hover:bg-slate-700 transition-all" title="Xóa tài khoản">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </button>
              </td>
            </tr>
            <tr v-if="paginatedUsers.length === 0">
              <td colspan="7" class="p-8 text-center text-slate-500 italic">Không tìm thấy người dùng nào phù hợp.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="p-4 border-t border-slate-700 flex justify-between items-center bg-slate-900/50 rounded-b-xl">
        <span class="text-sm text-slate-400">
          Trang <span class="font-bold text-white">{{ currentPage }}</span> / {{ totalPages }}
        </span>
        <div class="flex space-x-2">
          <button @click="prevPage" :disabled="currentPage === 1" class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors" :class="currentPage === 1 ? 'bg-slate-800 text-slate-600 cursor-not-allowed' : 'bg-slate-700 text-white hover:bg-slate-600'">Trước</button>
          
          <button v-for="page in totalPages" :key="page" @click="currentPage = page" class="w-8 h-8 rounded-lg text-sm font-bold transition-colors flex items-center justify-center" :class="currentPage === page ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/30' : 'bg-slate-700 text-slate-300 hover:bg-slate-600'">
            {{ page }}
          </button>

          <button @click="nextPage" :disabled="currentPage === totalPages" class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors" :class="currentPage === totalPages ? 'bg-slate-800 text-slate-600 cursor-not-allowed' : 'bg-slate-700 text-white hover:bg-slate-600'">Sau</button>
        </div>
      </div>
    </div>

    <AddUserModal v-if="isAddModalOpen" @close="isAddModalOpen = false" @refresh="loadUsers" />
    <EditUserModal v-if="isEditModalOpen" :user="selectedUser" @close="isEditModalOpen = false" @refresh="loadUsers" />
    <ConfirmDeleteModal v-if="isDeleteModalOpen" :user="selectedUser" @close="isDeleteModalOpen = false" @confirm="handleDeleteUser" />
    
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AddUserModal from '../components/AddUserModal.vue'
import EditUserModal from '../components/EditUserModal.vue'
import ConfirmDeleteModal from '../components/ConfirmDeleteModal.vue' 

const users = ref([]) 
const isAddModalOpen = ref(false)
const isEditModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const selectedUser = ref(null) 

// Biến cho bộ lọc và tìm kiếm
const searchQuery = ref('') 
const statusFilter = ref('all') 

// Hàm làm đẹp ngày tháng (Format Date)
const formatDateTime = (dateString) => {
  if (!dateString) return 'Chưa xác định';
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString; // Trả về nguyên gốc nếu lỗi parse
    
    // Format dạng: 15:30 - 24/10/2023
    const hours = date.getHours().toString().padStart(2, '0');
    const mins = date.getMinutes().toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    
    return `${hours}:${mins} - ${day}/${month}/${year}`;
  } catch (e) {
    return dateString;
  }
}

// Logic Lọc KÉP (Tìm kiếm Text + Dropdown Trạng thái)
const filteredUsers = computed(() => {
  let result = users.value;

  // 1. Lọc theo trạng thái (Dropdown)
  if (statusFilter.value === 'active') {
    result = result.filter(user => user.isActive);
  } else if (statusFilter.value === 'locked') {
    result = result.filter(user => !user.isActive);
  }

  // 2. Lọc theo ô tìm kiếm Text
  if (searchQuery.value) {
    const lowerQuery = searchQuery.value.toLowerCase();
    result = result.filter(user => 
      user.username.toLowerCase().includes(lowerQuery) || 
      user.email.toLowerCase().includes(lowerQuery) ||
      user.fullname.toLowerCase().includes(lowerQuery)
    );
  }

  return result;
})

const currentPage = ref(1)
const itemsPerPage = 6

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredUsers.value.length / itemsPerPage))
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredUsers.value.slice(start, end)
})

// Tự động quay về trang 1 khi gõ tìm kiếm hoặc đổi bộ lọc
watch([searchQuery, statusFilter], () => {
  currentPage.value = 1
})

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }

const loadUsers = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/admin/users')
    users.value = await response.json() 
    if (currentPage.value > totalPages.value && totalPages.value > 0) currentPage.value = totalPages.value;
  } catch (error) { console.error("Lỗi lấy danh sách:", error) }
}

onMounted(() => { loadUsers() })

const openEditModal = (user) => { selectedUser.value = user; isEditModalOpen.value = true }
const openDeleteModal = (user) => { selectedUser.value = user; isDeleteModalOpen.value = true }

const handleDeleteUser = async (userId) => {
  try {
    const response = await fetch('http://localhost:8080/api/admin/users/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: userId })
    });
    const data = await response.json();
    if (data.success) {
      isDeleteModalOpen.value = false;
      loadUsers(); 
    } else { alert("Xóa thất bại!"); }
  } catch (error) { alert("Lỗi kết nối Server!"); }
}

const handleToggleLock = async (user) => {
  const actionName = user.isActive ? "Khóa" : "Mở khóa";
  if (!confirm(`Bạn có chắc chắn muốn ${actionName} tài khoản [${user.username}] không?`)) return;

  try {
    const response = await fetch('http://localhost:8080/api/admin/users/toggle-lock', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: user.id })
    });
    
    const data = await response.json();
    if (data.success) {
      loadUsers(); 
    } else {
      alert("Lỗi: " + data.message);
    }
  } catch (error) {
    alert("Lỗi kết nối Server Java!");
  }
}
</script>