<template>
  <div class="flex h-screen bg-slate-900 text-slate-200 font-sans">
    
    <aside class="w-64 bg-slate-950 border-r border-slate-800 flex flex-col">
      <div class="h-16 flex items-center justify-center border-b border-slate-800">
        <h1 class="text-xl font-black text-white tracking-widest">
          <span class="text-red-500">ADMIN</span> CORE
        </h1>
      </div>

      <nav class="flex-1 p-4 space-y-2">
        <router-link to="/admin" class="block px-4 py-3 rounded-lg hover:bg-slate-800 transition-colors font-semibold" active-class="bg-blue-600 text-white">
          📊 Tổng quan Hệ thống
        </router-link>
        <router-link to="/admin/users" class="block px-4 py-3 rounded-lg hover:bg-slate-800 transition-colors font-semibold" active-class="bg-blue-600 text-white">
          🛡️ Quản lý user
        </router-link>
        <router-link to="/admin/logs" class="block px-4 py-3 rounded-lg hover:bg-slate-800 transition-colors font-semibold" active-class="bg-blue-600 text-white">
          🕵️‍♂️ Nhật ký Hệ thống
        </router-link>
      </nav>

      <div class="p-4 border-t border-slate-800">
        <button @click="handleLogout" class="w-full bg-red-600 hover:bg-red-700 text-white py-2 rounded-lg font-bold transition">
          Đăng xuất hệ thống
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col overflow-hidden">
     <header class="h-16 flex items-center justify-between px-8 bg-slate-900 border-b border-slate-800">
        <h2 class="text-lg font-bold text-slate-400">Hệ Thống Quản Trị Trung Tâm</h2>
        <div class="flex items-center space-x-3">
          <span class="font-bold text-white">Xin chào, Quản trị viên {{ adminName }}</span>
          <div class="w-10 h-10 bg-red-600 rounded-full flex items-center justify-center text-white font-black shadow-md shadow-red-500/50">
            A
          </div>
        </div>
      </header>

      <div class="flex-1 overflow-auto p-8">
        <router-view></router-view> 
      </div>
    </main>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const adminName = ref('Admin')

onMounted(() => {
  const storedUser = localStorage.getItem('username')
  if (storedUser) adminName.value = storedUser
})

const handleLogout = () => {
  localStorage.clear() // Xóa sạch vé
  alert("Đã thoát chế độ Quản trị!")
  router.push('/')
}
</script>