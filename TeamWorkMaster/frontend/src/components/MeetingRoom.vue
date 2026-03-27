<template>
  <div class="flex-1 p-6 flex flex-col gap-6 overflow-y-auto custom-scrollbar bg-slate-50 dark:bg-slate-900 h-full">
    
    <div v-if="isManager" class="flex flex-col lg:flex-row gap-6 shrink-0 animate-fade-in">
      
      <div class="flex-1 bg-white dark:bg-slate-800 rounded-3xl overflow-hidden shadow-sm border border-slate-200 dark:border-slate-700 flex flex-col items-center justify-center p-8 text-center relative min-h-[350px]">
        <div class="absolute top-0 left-0 w-full h-48 bg-gradient-to-b from-blue-50 dark:from-slate-700/50 to-transparent"></div>
        <div class="relative z-10 flex flex-col items-center w-full max-w-lg">
          <div class="w-20 h-20 bg-white rounded-2xl shadow-md flex items-center justify-center mb-6 p-4 shrink-0">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full"><path d="M14 4H5C3.89543 4 3 4.89543 3 6V18C3 19.1046 3.89543 20 5 20H14C15.1046 20 16 19.1046 16 18V6C16 4.89543 15.1046 4 14 4Z" fill="#00832d"/><path d="M21 7.5L16 10.5V13.5L21 16.5V7.5Z" fill="#00aa4b"/></svg>
          </div>

          <div v-if="!currentMeeting" class="w-full">
            <h2 class="text-2xl font-black text-slate-800 dark:text-white mb-2">Họp trực tuyến dự án</h2>
            <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mb-6">Bạn là Quản lý. Hãy lấy link Google Meet và mở phòng cho nhóm.</p>
            <a href="https://meet.google.com/new" target="_blank" class="px-6 py-3 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 font-bold rounded-xl hover:bg-emerald-200 transition-colors mb-6 flex items-center w-max mx-auto">
              <span class="mr-2">🚀</span> 1. Lấy link Meet mới
            </a>
            <div class="w-full flex items-center space-x-2">
              <input v-model="meetLinkInput" type="text" placeholder="2. Dán link vào đây..." class="flex-1 px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl text-sm outline-none focus:ring-2 focus:ring-blue-500 dark:text-white" />
              <button @click="startMeeting" :disabled="!meetLinkInput || isStarting" class="px-6 py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-all shadow-md">
                {{ isStarting ? 'Đang mở...' : 'Mở Phòng' }}
              </button>
            </div>
          </div>

          <div v-else class="w-full flex flex-col items-center animate-fade-in">
            <div class="px-4 py-1.5 bg-red-100 text-red-600 font-black text-[10px] uppercase tracking-widest rounded-full flex items-center mb-4 animate-pulse">
              <span class="w-2 h-2 bg-red-500 rounded-full mr-2"></span> Đang diễn ra
            </div>
            <h2 class="text-2xl font-black text-slate-800 dark:text-white mb-2">Phòng họp đang mở</h2>
            <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mb-8">Bạn đang chủ trì cuộc họp này.</p>
            <a :href="currentMeeting.meetLink" target="_blank" class="px-10 py-4 bg-emerald-500 hover:bg-emerald-600 text-white font-black rounded-2xl shadow-lg shadow-emerald-500/30 transition-all flex items-center text-lg hover:scale-105">
              <span class="mr-3">👋</span> Tham gia Google Meet
            </a>
          </div>
        </div>
      </div>

      <div class="w-full lg:w-96 flex flex-col gap-6 shrink-0">
        <div class="bg-white dark:bg-slate-800 rounded-3xl p-6 shadow-sm border border-slate-200 dark:border-slate-700 flex flex-col items-center">
          <h3 class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-4">Thời gian</h3>
          <div class="text-5xl font-black text-slate-800 dark:text-white mb-6 tabular-nums tracking-tight">{{ formattedTime }}</div>
          <div class="flex gap-3 w-full">
            <button @click="toggleTimer" :disabled="!currentMeeting" class="flex-1 py-3 rounded-xl font-bold text-white shadow-md transition-all disabled:opacity-50 disabled:cursor-not-allowed" :class="isTimerRunning ? 'bg-red-500 hover:bg-red-600' : 'bg-blue-600 hover:bg-blue-700'">
              {{ isTimerRunning ? 'Tạm Dừng' : 'Bắt Đầu' }}
            </button>
            <button @click="resetTimer" :disabled="!currentMeeting" class="px-5 py-3 rounded-xl font-bold bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 disabled:opacity-50">Reset</button>
          </div>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-3xl p-6 shadow-sm border border-slate-200 dark:border-slate-700 flex-1 flex flex-col min-h-[250px]">
          <h3 class="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-4">📝 Biên bản</h3>
          <textarea 
            v-model="meetingNotes"
            :disabled="!currentMeeting"
            placeholder="Quản lý ghi chú nội dung cuộc họp tại đây..." 
            class="flex-1 w-full p-4 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/50 text-sm font-medium outline-none focus:ring-2 focus:ring-blue-500 resize-none custom-scrollbar dark:text-white disabled:opacity-70 disabled:cursor-not-allowed"
          ></textarea>
          <button @click="endMeeting" :disabled="!currentMeeting || isEnding" class="mt-4 w-full py-3 bg-red-500 hover:bg-red-600 text-white font-bold rounded-xl transition-all shadow-md disabled:opacity-50">
            {{ isEnding ? 'Đang lưu...' : '⏹ Kết Thúc & Lưu Lịch Sử' }}
          </button>
        </div>
      </div>
    </div>

    <div v-else class="flex flex-col shrink-0 animate-fade-in">
      <div class="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-200 dark:border-slate-700 flex flex-col items-center justify-center p-12 text-center min-h-[400px]">
        <div class="w-24 h-24 bg-blue-50 dark:bg-slate-700/50 rounded-full flex items-center justify-center mb-6">
          <span class="text-4xl">📹</span>
        </div>

        <template v-if="!currentMeeting">
          <h2 class="text-2xl font-black text-slate-800 dark:text-white mb-3">Chưa có cuộc họp nào</h2>
          <p class="text-sm text-slate-500 dark:text-slate-400 mb-8 max-w-sm leading-relaxed">Vui lòng chờ Quản lý dự án tạo link và mở phòng họp. Hệ thống sẽ tự động cập nhật khi có thông báo.</p>
          <div class="px-6 py-3 bg-slate-50 dark:bg-slate-900 text-slate-500 dark:text-slate-400 font-bold rounded-xl flex items-center border border-slate-200 dark:border-slate-700">
            <span class="w-4 h-4 border-2 border-slate-400 border-t-transparent rounded-full animate-spin mr-3"></span>
            Đang theo dõi tín hiệu phòng họp...
          </div>
        </template>

        <template v-else>
          <div class="px-4 py-1.5 bg-red-100 text-red-600 font-black text-[10px] uppercase tracking-widest rounded-full flex items-center mb-4 animate-pulse">
            <span class="w-2.5 h-2.5 bg-red-500 rounded-full mr-2"></span> Đang diễn ra
          </div>
          <h2 class="text-3xl font-black text-slate-800 dark:text-white mb-3">Phòng họp đang mở!</h2>
          <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mb-8">Người chủ trì: <strong class="text-slate-700 dark:text-slate-200">{{ currentMeeting.hostName }}</strong></p>
          <a :href="currentMeeting.meetLink" target="_blank" class="px-10 py-4 bg-blue-600 hover:bg-blue-700 text-white font-black rounded-2xl shadow-lg shadow-blue-500/30 transition-all flex items-center text-lg hover:scale-105 hover:-translate-y-1">
            <span class="mr-3">👋</span> Tham Gia Vào Google Meet
          </a>
        </template>
      </div>
    </div>

    <div class="bg-white dark:bg-slate-800 rounded-3xl p-8 shadow-sm border border-slate-200 dark:border-slate-700 flex-1 shrink-0">
      <h3 class="text-lg font-black text-slate-800 dark:text-white mb-6 flex items-center">
        <span class="mr-2">🗄️</span> Lịch sử cuộc họp ({{ historyMeetings.length }})
      </h3>
      
      <div v-if="historyMeetings.length === 0" class="text-center py-10 text-slate-500 font-medium">
        Chưa có dữ liệu lịch sử cuộc họp nào.
      </div>

      <div v-else class="space-y-4">
        <div v-for="meet in historyMeetings" :key="meet.id" class="p-5 border border-slate-100 dark:border-slate-700 rounded-2xl hover:shadow-md transition-shadow bg-slate-50/50 dark:bg-slate-800/50 flex flex-col md:flex-row md:items-start gap-4 group">
          <div class="shrink-0 w-12 h-12 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-xl flex items-center justify-center font-black">
            {{ getDateOnly(meet.startTime) }}
          </div>
          
          <div class="flex-1 w-full">
            <div class="flex items-center justify-between mb-2">
              <h4 class="font-bold text-slate-800 dark:text-white text-sm">Host: {{ meet.hostName }}</h4>
              <span class="text-[10px] font-bold text-slate-400 bg-slate-200 dark:bg-slate-700 px-2 py-1 rounded-md">{{ getTimeRange(meet.startTime, meet.endTime) }}</span>
            </div>

            <div v-if="editingMeetingId === meet.id" class="w-full flex flex-col gap-2 mt-3 animate-fade-in">
              <textarea v-model="editNotesContent" rows="4" class="w-full p-3 rounded-xl border border-blue-300 dark:border-blue-600 focus:ring-2 focus:ring-blue-500 outline-none text-sm bg-white dark:bg-slate-900 dark:text-white custom-scrollbar"></textarea>
              <div class="flex justify-end gap-2">
                <button @click="cancelEdit" class="px-4 py-2 text-xs font-bold text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-lg transition-colors">Hủy</button>
                <button @click="saveEditNotes(meet.id)" :disabled="isSavingEdit" class="px-4 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-md transition-all flex items-center">
                  <span v-if="isSavingEdit" class="w-3 h-3 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
                  Lưu thay đổi
                </button>
              </div>
            </div>

            <div v-else class="relative">
              <p class="text-sm text-slate-600 dark:text-slate-300 bg-white dark:bg-slate-900 p-3 rounded-lg border border-slate-100 dark:border-slate-700 whitespace-pre-wrap pr-10">
                {{ meet.notes || 'Không có biên bản ghi chú.' }}
              </p>
              
              <button v-if="isManager" @click="startEdit(meet)" class="absolute top-2 right-2 p-1.5 text-slate-400 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/30 rounded-md opacity-0 group-hover:opacity-100 transition-all" title="Chỉnh sửa biên bản">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
              </button>
            </div>

          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useToast } from '../composables/useToast'; 

const props = defineProps({
  projectId: { type: String, required: true },
  projectName: { type: String, required: true },
  userRole: { type: String, required: true } 
});

const { addToast } = useToast();
const currentUserId = localStorage.getItem("userId") || 1;
const isManager = computed(() => props.userRole === 'OWNER' || props.userRole === 'MANAGER');

// --- STATE QUẢN LÝ PHÒNG HỌP ---
const meetLinkInput = ref("");
const currentMeeting = ref(null); 
const historyMeetings = ref([]);
const meetingNotes = ref("");
const isStarting = ref(false);
const isEnding = ref(false);

// --- STATE CHỈNH SỬA BIÊN BẢN ---
const editingMeetingId = ref(null);
const editNotesContent = ref("");
const isSavingEdit = ref(false);

// --- STATE BẤM GIỜ ---
let pollingInterval = null;
let timerInterval = null;
const timeElapsed = ref(0);
const isTimerRunning = ref(false);

onMounted(() => {
  fetchCurrentMeeting();
  fetchMeetingHistory();
  pollingInterval = setInterval(() => {
    fetchCurrentMeetingSilently();
  }, 3000);
});

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
  if (timerInterval) clearInterval(timerInterval);
});


const fetchCurrentMeeting = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/meetings?projectId=${props.projectId}&type=current`);
    if (res.ok) {
      const text = await res.text();
      currentMeeting.value = (text !== "null" && text.trim() !== "") ? JSON.parse(text) : null;
    }
  } catch (error) { console.error("Lỗi lấy phòng họp:", error); }
};

const fetchCurrentMeetingSilently = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/meetings?projectId=${props.projectId}&type=current`);
    if (res.ok) {
      const text = await res.text();
      currentMeeting.value = (text !== "null" && text.trim() !== "") ? JSON.parse(text) : null;
    }
  } catch (error) {}
};

const fetchMeetingHistory = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/meetings?projectId=${props.projectId}&type=history`);
    if (res.ok) {
      historyMeetings.value = await res.json();
    }
  } catch (error) { console.error("Lỗi lấy lịch sử:", error); }
};


const startMeeting = async () => {
  if(!meetLinkInput.value) return;
  isStarting.value = true;
  try {
    const currentUserName = localStorage.getItem("fullName") || "Quản lý";
    const res = await fetch("http://localhost:8080/api/meetings", {
      method: "POST",
      headers: { "Content-Type": "application/json", "User-ID": currentUserId },
      body: JSON.stringify({ 
        projectId: props.projectId, 
        meetLink: meetLinkInput.value,
        projectName: props.projectName,
        hostName: currentUserName
      })
    });
    if (res.ok) {
      addToast("Đã mở phòng và gửi thông báo!", "success");
      meetLinkInput.value = "";
      fetchCurrentMeeting();
    } else {
      addToast("Lỗi khi mở phòng", "error");
    }
  } catch(e) { addToast("Mất kết nối", "error"); } 
  finally { isStarting.value = false; }
};

const endMeeting = async () => {
  if(!currentMeeting.value) return;
  if(!confirm("Kết thúc và lưu lịch sử cuộc họp này?")) return;
  isEnding.value = true;
  try {
    const res = await fetch("http://localhost:8080/api/meetings", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ 
        action: "end",
        meetingId: currentMeeting.value.id, 
        notes: meetingNotes.value 
      })
    });
    if (res.ok) {
      addToast("Đã lưu lịch sử cuộc họp!", "success");
      currentMeeting.value = null;
      meetingNotes.value = "";
      resetTimer(); 
      fetchMeetingHistory(); 
    }
  } catch(e) { addToast("Lỗi máy chủ", "error"); } 
  finally { isEnding.value = false; }
};

// --- CHỈNH SỬA BIÊN BẢN ---
const startEdit = (meet) => {
  editingMeetingId.value = meet.id;
  editNotesContent.value = meet.notes;
};
const cancelEdit = () => {
  editingMeetingId.value = null;
  editNotesContent.value = "";
};
const saveEditNotes = async (meetingId) => {
  isSavingEdit.value = true;
  try {
    const res = await fetch("http://localhost:8080/api/meetings", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ 
        action: "updateNotes", 
        meetingId: meetingId, 
        notes: editNotesContent.value 
      })
    });
    if (res.ok) {
      addToast("Cập nhật biên bản thành công!", "success");
      editingMeetingId.value = null;
      fetchMeetingHistory(); 
    }
  } catch(e) { addToast("Mất kết nối máy chủ", "error"); } 
  finally { isSavingEdit.value = false; }
};

const formattedTime = computed(() => {
  const h = Math.floor(timeElapsed.value / 3600).toString().padStart(2, '0');
  const m = Math.floor((timeElapsed.value % 3600) / 60).toString().padStart(2, '0');
  const s = (timeElapsed.value % 60).toString().padStart(2, '0');
  return `${h}:${m}:${s}`;
});
const toggleTimer = () => {
  if (isTimerRunning.value) { 
    clearInterval(timerInterval); 
    isTimerRunning.value = false; 
  } else { 
    isTimerRunning.value = true; 
    timerInterval = setInterval(() => { timeElapsed.value++; }, 1000); 
  }
};
const resetTimer = () => { 
  clearInterval(timerInterval); 
  isTimerRunning.value = false; 
  timeElapsed.value = 0; 
};

const getDateOnly = (isoString) => {
  if (!isoString) return "";
  const d = new Date(isoString);
  return `${d.getDate()}/${d.getMonth()+1}`;
};
const getTimeRange = (start, end) => {
  if (!start) return "";
  const s = new Date(start).toLocaleTimeString('vi-VN', {hour:'2-digit', minute:'2-digit'});
  const e = end ? new Date(end).toLocaleTimeString('vi-VN', {hour:'2-digit', minute:'2-digit'}) : 'Đang diễn ra';
  return `${s} - ${e}`;
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #475569; }
</style>