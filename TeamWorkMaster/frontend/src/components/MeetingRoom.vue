<template>
  <div class="flex-1 p-6 flex flex-col lg:flex-row gap-6 overflow-hidden h-full">
    
    <div class="flex-1 bg-black rounded-3xl overflow-hidden shadow-lg border border-slate-200 dark:border-slate-700 relative">
      <iframe 
        :src="`https://meet.jit.si/TeamworkMaster_Project_${projectId}`" 
        allow="camera; microphone; fullscreen; display-capture; autoplay" 
        class="w-full h-full border-0 absolute inset-0"
      ></iframe>
    </div>

    <div class="w-full lg:w-96 flex flex-col gap-6 overflow-y-auto custom-scrollbar">
      
      <div class="bg-white dark:bg-slate-800 rounded-3xl p-6 shadow-sm border border-slate-200 dark:border-slate-700 flex flex-col items-center">
        <h3 class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-4">Thời gian họp</h3>
        <div class="text-5xl font-black text-slate-800 dark:text-white mb-6 tabular-nums tracking-tight">
          {{ formattedTime }}
        </div>
        <div class="flex gap-3 w-full">
          <button @click="toggleTimer" class="flex-1 py-3 rounded-xl font-bold text-white shadow-md transition-all" :class="isTimerRunning ? 'bg-red-500 hover:bg-red-600 shadow-red-500/30' : 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-500/30'">
            {{ isTimerRunning ? 'Tạm Dừng' : 'Bắt Đầu' }}
          </button>
          <button @click="resetTimer" class="px-5 py-3 rounded-xl font-bold bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-600 transition-all">
            Reset
          </button>
        </div>
      </div>

      <div class="bg-white dark:bg-slate-800 rounded-3xl p-6 shadow-sm border border-slate-200 dark:border-slate-700 flex-1 flex flex-col min-h-[300px]">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest">Biên bản cuộc họp</h3>
        </div>
        <textarea 
          v-model="meetingNotes"
          placeholder="Ghi chú lại các quyết định, task cần phân công..." 
          class="flex-1 w-full p-4 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/50 text-sm font-medium outline-none focus:ring-2 focus:ring-blue-500 resize-none custom-scrollbar dark:text-white transition-colors"
        ></textarea>
        <button @click="saveNotes" class="mt-4 w-full py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 shadow-md shadow-blue-500/30 transition-all">
          💾 Lưu Biên Bản
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

// Nhận ID và Tên dự án từ component cha truyền vào
const props = defineProps({
  projectId: { type: String, required: true },
  projectName: { type: String, required: true }
});

// Logic Bấm giờ
const timeElapsed = ref(0);
let timerInterval = null;
const isTimerRunning = ref(false);

const formattedTime = computed(() => {
  const h = Math.floor(timeElapsed.value / 3600).toString().padStart(2, '0');
  const m = Math.floor((timeElapsed.value % 3600) / 60).toString().padStart(2, '0');
  const s = (timeElapsed.value % 60).toString().padStart(2, '0');
  return `${h}:${m}:${s}`;
});

const toggleTimer = () => {
  if (isTimerRunning.value) { clearInterval(timerInterval); isTimerRunning.value = false; } 
  else { isTimerRunning.value = true; timerInterval = setInterval(() => { timeElapsed.value++; }, 1000); }
};

const resetTimer = () => { clearInterval(timerInterval); isTimerRunning.value = false; timeElapsed.value = 0; };

// Logic Ghi chú
const meetingNotes = ref("");
const saveNotes = () => { alert("Đã lưu biên bản cuộc họp tạm thời vào trình duyệt!"); };
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #475569; }
</style>