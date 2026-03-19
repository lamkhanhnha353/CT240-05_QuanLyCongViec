<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-slate-800 border border-slate-700 rounded-xl shadow-2xl w-full max-w-sm overflow-hidden p-6 text-center">
      
      <div class="mx-auto w-16 h-16 rounded-full flex items-center justify-center mb-4"
           :class="user?.isActive ? 'bg-yellow-500/20' : 'bg-emerald-500/20'">
        <svg v-if="user?.isActive" class="w-8 h-8 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
        <svg v-else class="w-8 h-8 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path></svg>
      </div>

      <h3 class="text-xl font-bold text-white mb-2">{{ user?.isActive ? 'Khóa Tài Khoản?' : 'Mở Khóa Tài Khoản?' }}</h3>
      <p class="text-slate-400 text-sm mb-6">
        Bạn có chắc chắn muốn {{ user?.isActive ? 'khóa' : 'mở khóa' }} tài khoản <span class="font-bold text-white">{{ user?.username }}</span> không?
      </p>

      <div class="flex justify-center space-x-3">
        <button @click="$emit('close')" class="px-5 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded-lg font-medium transition">
          Hủy bỏ
        </button>
        <button @click="$emit('confirm', user)" class="px-5 py-2 text-white rounded-lg font-medium shadow-lg transition"
                :class="user?.isActive ? 'bg-yellow-600 hover:bg-yellow-700 shadow-yellow-500/30' : 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-500/30'">
          Đồng ý
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
defineProps({
  user: { type: Object, required: true }
})
defineEmits(['close', 'confirm'])
</script>