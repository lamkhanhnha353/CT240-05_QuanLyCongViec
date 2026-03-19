<template>
  <div class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
    <div class="bg-slate-800 border border-slate-700 rounded-xl shadow-2xl w-full max-w-sm overflow-hidden p-6 text-center">
      
      <!-- Icon Cảnh báo -->
      <div class="mx-auto w-16 h-16 bg-red-500/20 rounded-full flex items-center justify-center mb-4">
        <svg class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
        </svg>
      </div>

      <h3 class="text-xl font-bold text-white mb-2">Xóa Tài Khoản?</h3>
      <p class="text-slate-400 text-sm mb-6">
        Bạn có chắc chắn muốn xóa vĩnh viễn tài khoản <span class="font-bold text-white">{{ user?.username }}</span> không? Hành động này không thể hoàn tác.
      </p>

      <div class="flex justify-center space-x-3">
        <button @click="$emit('close')" class="px-5 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded-lg font-medium transition">
          Hủy bỏ
        </button>
        <button @click="$emit('confirm', user.id)" class="px-5 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium shadow-lg shadow-red-500/30 transition">
          Đồng ý Xóa
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
defineProps({
  user: {
    type: Object,
    required: true
  }
})
defineEmits(['close', 'confirm'])
</script>