<template>
  <div class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
    <div class="bg-white dark:bg-slate-800 w-full max-w-7xl rounded-3xl shadow-2xl flex flex-col md:flex-row h-[92vh] max-h-[900px] text-slate-800 dark:text-slate-200 animate-fade-in overflow-hidden">
      
      <div class="w-full md:w-[45%] p-6 md:p-8 border-r border-slate-200 dark:border-slate-700 flex flex-col h-full bg-white dark:bg-slate-800">
        
        <div class="flex justify-between items-start mb-6 shrink-0">
          <div class="flex items-center text-sm font-bold text-slate-400 mb-2">
            <span class="px-2 py-1 bg-slate-100 dark:bg-slate-700 rounded-md mr-2">TASK-{{ taskData.id }}</span>
            <span :class="getStatusStyle(taskData.status)" class="px-2 py-1 rounded-md">{{ formatStatus(taskData.status) }}</span>
          </div>
          <button @click="$emit('close')" class="p-2 bg-slate-100 hover:bg-slate-200 dark:bg-slate-700 dark:hover:bg-slate-600 rounded-full transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>

        <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-6">
          <div>
            <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Tên công việc</label>
            <input v-model="taskData.title" type="text" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 focus:border-blue-500 font-bold text-lg outline-none bg-transparent" />
          </div>

          <div>
            <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Mô tả</label>
            <textarea v-model="taskData.description" rows="5" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 focus:border-blue-500 outline-none text-sm bg-slate-50 dark:bg-slate-900/50 custom-scrollbar resize-none"></textarea>
          </div>

          <div>
            <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Nhãn (Tags)</label>
            <input v-model="taskData.tags" type="text" placeholder="Ngăn cách bằng dấu phẩy..." class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 outline-none text-sm bg-transparent" />
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 bg-slate-50 dark:bg-slate-900/30 p-5 rounded-2xl border border-slate-100 dark:border-slate-700/50">
            <div class="lg:col-span-2">
              <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Người thực hiện</label>
              <div class="flex flex-wrap gap-2 mb-3">
                <div v-for="id in taskData.assigneeIds" :key="id" class="px-3 py-1.5 bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 text-xs font-bold rounded-lg flex items-center shadow-sm">
                  {{ getMemberName(id) }}
                  <button @click="removeAssignee(id)" class="ml-2 text-blue-500 hover:text-red-500 transition-colors">✕</button>
                </div>
              </div>
              <select @change="addAssignee" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-600 outline-none text-sm font-bold bg-white dark:bg-slate-800">
                <option value="">+ Thêm người thực hiện...</option>
                <option v-for="user in unassignedMembers" :key="user.id" :value="user.id">{{ user.fullName }}</option>
              </select>
            </div>

            <div>
              <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Độ ưu tiên</label>
              <select v-model="taskData.priority" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 outline-none text-sm font-bold bg-white dark:bg-slate-800">
                <option value="LOW">Thấp</option>
                <option value="MEDIUM">Vừa</option>
                <option value="HIGH">Cao</option>
              </select>
            </div>
            
            <div>
              <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Trạng thái (Cột)</label>
              <select v-model="taskData.status" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 outline-none text-sm font-bold bg-white dark:bg-slate-800">
                <option value="TODO">Cần làm</option>
                <option value="IN_PROGRESS">Đang thực hiện</option>
                <option value="DONE">Đã hoàn thành</option>
                <option value="CANCEL">Đã hủy</option>
              </select>
            </div>

            <div>
              <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Bắt đầu</label>
              <input v-model="taskData.startDate" type="date" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 outline-none text-sm bg-white dark:bg-slate-800 dark:[color-scheme:dark]" />
            </div>

            <div>
              <label class="block text-xs font-black text-slate-400 uppercase tracking-widest mb-2">Hạn chót</label>
              <input v-model="taskData.deadline" type="date" :min="taskData.startDate" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 outline-none text-sm bg-white dark:bg-slate-800 dark:[color-scheme:dark]" />
            </div>
          </div>
        </div>

        <div class="flex justify-between items-center mt-6 pt-6 border-t border-slate-100 dark:border-slate-700 shrink-0">
          <button @click="deleteTask" class="text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 px-4 py-2 rounded-lg font-bold text-sm transition-all flex items-center">
            🗑️ Xóa thẻ này
          </button>
          <div class="flex space-x-3">
            <button @click="$emit('close')" class="px-5 py-2.5 bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 font-bold rounded-xl hover:bg-slate-200 dark:hover:bg-slate-600 transition-colors">Đóng</button>
            <button @click="submitEdit" :disabled="isSubmitting" class="px-6 py-2.5 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 shadow-md transition-colors flex items-center">
              <span v-if="isSubmitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
              Lưu thay đổi
            </button>
          </div>
        </div>
      </div>

      <div class="w-full md:w-[55%] flex flex-col relative h-full bg-slate-50 dark:bg-[#0f172a]">
        <div class="p-6 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between shrink-0 bg-white dark:bg-slate-800">
          <h3 class="font-black text-lg flex items-center"><span class="mr-2">💬</span> Thảo luận nội bộ</h3>
          <span class="bg-blue-100 text-blue-600 dark:bg-blue-900/50 dark:text-blue-400 text-xs font-bold px-2 py-1 rounded-md">{{ comments.length }} bình luận</span>
        </div>
        
        <div class="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar" id="chatContainer">
            <div v-if="comments.length === 0" class="text-center text-slate-400 text-sm mt-10">Chưa có bình luận nào. Bắt đầu thảo luận ngay!</div>
            
            <div v-for="c in comments" :key="c.id" class="flex flex-col" :class="c.user === currentUserName ? 'items-end' : 'items-start'">
              <span class="text-[10px] text-slate-400 font-bold mb-1 px-1">{{ c.user }} • {{ formatChatTime(c.time) }}</span>
              
              <div class="flex flex-col gap-1.5" :class="c.user === currentUserName ? 'items-end' : 'items-start'">
                
                <div v-if="c.content" class="max-w-[280px] md:max-w-md px-4 py-3 text-sm shadow-sm whitespace-pre-wrap break-words" 
                     :class="c.user === currentUserName ? 'bg-blue-600 text-white rounded-2xl rounded-tr-sm' : 'bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 border border-slate-200 dark:border-slate-600 rounded-2xl rounded-tl-sm'">
                  {{ c.content.replace(/\\n/g, '\n') }}
                </div>
                
                <div v-if="c.fileUrl" class="mt-1">
                  <img v-if="isImage(c.fileUrl)" :src="c.fileUrl" @click="selectedImage = c.fileUrl" 
                       class="w-32 h-32 md:w-40 md:h-40 object-cover rounded-xl cursor-pointer hover:opacity-90 border-2 border-slate-200 dark:border-slate-600 shadow-sm transition-all" />
                  
                  <a v-else :href="c.fileUrl" target="_blank" class="flex items-center space-x-2 px-4 py-3 rounded-xl text-xs font-bold shadow-sm transition-colors" 
                     :class="c.user === currentUserName ? 'bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-200 dark:bg-blue-900/40 dark:border-blue-800' : 'bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 dark:bg-slate-800 dark:text-slate-200'">
                    <span class="text-xl leading-none">📎</span> 
                    <span class="underline">Tệp đính kèm</span>
                  </a>
                </div>
              </div>
            </div>
        </div>
        
        <div class="p-4 border-t border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shrink-0">
            <div v-if="isUploadingFile" class="mb-2 px-3 py-2 bg-blue-50 dark:bg-blue-900/30 rounded-xl flex items-center text-xs text-blue-600 font-bold">
              <span class="w-3 h-3 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mr-2"></span> Đang tải lên Cloudinary...
            </div>
            <div v-if="uploadedFileUrl" class="mb-2 px-3 py-2 bg-emerald-50 dark:bg-emerald-900/30 rounded-xl flex justify-between items-center text-xs border border-emerald-200 dark:border-emerald-800">
              <span class="truncate text-emerald-600 dark:text-emerald-400 font-bold flex items-center"><span class="mr-1 text-base">🖼️</span> Đã đính kèm 1 tệp</span>
              <button @click="uploadedFileUrl = ''" class="text-red-500 hover:bg-red-100 dark:hover:bg-red-900/30 px-2 py-1 rounded-md font-bold transition-colors">Hủy</button>
            </div>

            <div class="flex items-end space-x-2 relative">
               <input type="file" ref="fileInput" @change="handleFileUpload" class="hidden" />
               <button @click="triggerUpload" class="w-12 h-12 shrink-0 flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/30 rounded-full transition-all text-xl shadow-sm border border-transparent hover:border-blue-100" title="Đính kèm">
                 📎
               </button>
               
               <textarea v-model="newCommentText" @keydown.enter.exact.prevent="sendComment" placeholder="Nhập bình luận... (Enter gửi, Shift+Enter xuống dòng)" class="flex-1 px-4 py-3.5 rounded-2xl border border-slate-200 dark:border-slate-600 outline-none text-sm bg-slate-50 dark:bg-slate-900 dark:text-white resize-none custom-scrollbar focus:border-blue-500 focus:bg-white dark:focus:bg-slate-800 transition-colors" style="min-height: 48px; max-height: 120px;"></textarea>
               
               <button @click="sendComment" :disabled="!newCommentText.trim() && !uploadedFileUrl" class="w-12 h-12 shrink-0 rounded-full bg-blue-600 flex items-center justify-center text-white hover:bg-blue-700 disabled:opacity-50 disabled:bg-slate-300 dark:disabled:bg-slate-700 transition-all shadow-md active:scale-95">
                 <svg class="w-5 h-5 ml-1" fill="currentColor" viewBox="0 0 24 24"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"></path></svg>
               </button>
            </div>
        </div>
      </div>
      
      <div v-if="selectedImage" @click="selectedImage = null" class="fixed inset-0 z-[60] bg-black/95 flex items-center justify-center p-4 cursor-pointer backdrop-blur-sm">
         <img :src="selectedImage" class="max-w-[95vw] max-h-[95vh] rounded-xl shadow-2xl object-contain border border-white/10" />
         <button class="absolute top-6 right-6 w-12 h-12 bg-black/50 hover:bg-red-600 text-white rounded-full flex items-center justify-center text-xl font-black transition-all">✕</button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';

const props = defineProps({
  task: { type: Object, required: true },
  projectMembers: { type: Array, required: true }
});

const emit = defineEmits(['close', 'updated']);
const currentUserName = ref(localStorage.getItem("fullName") || "Bạn"); 
const isSubmitting = ref(false);
let pollingInterval = null; // 🟢 Khai báo biến lưu Interval tải tin nhắn

const taskData = ref({
  id: props.task.id,
  title: props.task.title,
  description: props.task.description || '',
  priority: props.task.priority,
  status: props.task.status,
  startDate: props.task.startDate && props.task.startDate !== 'null' ? props.task.startDate : '',
  deadline: props.task.deadline && props.task.deadline !== 'null' ? props.task.deadline : '',
  tags: props.task.tags || '',
  assigneeIds: props.task.assigneeIds ? props.task.assigneeIds.split(',').map(id => parseInt(id)) : []
});

const unassignedMembers = computed(() => props.projectMembers.filter(m => !taskData.value.assigneeIds.includes(m.id)));
const addAssignee = (e) => { const id = parseInt(e.target.value); if(id) taskData.value.assigneeIds.push(id); e.target.value = ""; };
const removeAssignee = (id) => { taskData.value.assigneeIds = taskData.value.assigneeIds.filter(i => i !== id); };
const getMemberName = (id) => { const u = props.projectMembers.find(m => m.id === id); return u ? u.fullName : 'Unknown'; };

const submitEdit = async () => {
  if (!taskData.value.title.trim()) return alert("Thiếu tên công việc!");
  isSubmitting.value = true;
  try {
    const payload = { ...taskData.value, taskId: taskData.value.id, assigneeIds: taskData.value.assigneeIds.join(',') };
    
    // 1. Cập nhật chi tiết (Tên, mô tả, ngày tháng, người làm...)
    await fetch("http://localhost:8080/api/tasks/update-details", {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    });

    // 2. 🟢 GỌI THÊM LỆNH NÀY ĐỂ ÉP CẬP NHẬT TRẠNG THÁI (CỘT)
    await fetch("http://localhost:8080/api/tasks/update-status", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ taskId: taskData.value.id, status: taskData.value.status })
    });

    emit('updated');
  } catch(e) { 
    alert("Lỗi máy chủ!"); 
  } finally { 
    isSubmitting.value = false; 
  }
};

const deleteTask = async () => {
  if (!confirm("Chắc chắn xóa thẻ này vĩnh viễn?")) return;
  try {
    const res = await fetch("http://localhost:8080/api/tasks/delete", {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ taskId: taskData.value.id })
    });
    if (res.ok) emit('updated');
  } catch(e) {}
};

// ==========================================
// 🟢 LOGIC COMMENT & UPLOAD FILE THÔNG MINH
// ==========================================
const comments = ref([]);
const newCommentText = ref("");
const fileInput = ref(null);
const isUploadingFile = ref(false);
const uploadedFileUrl = ref("");
const selectedImage = ref(null);

onMounted(() => { 
  fetchComments(true); // Tải lần đầu ép cuộn xuống đáy
  
  // 🟢 THIẾT LẬP POLLING (Tự động tải lại tin nhắn sau 3 giây)
  pollingInterval = setInterval(() => {
    fetchComments(false); // Reload ẩn, không tự kéo màn hình
  }, 3000);
});

// Hủy bộ đếm khi tắt Modal để không làm nặng trình duyệt
onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
});

const fetchComments = async (forceScroll = false) => {
  try {
    const res = await fetch(`http://localhost:8080/api/comments?taskId=${taskData.value.id}`);
    if (res.ok) { 
      const newData = await res.json();
      
      // Logic chống giật: Chỉ cuộn xuống nếu có tin nhắn mới hoặc bị ép buộc (Lúc mở Modal hoặc lúc mới gửi xong)
      if (forceScroll || newData.length > comments.value.length) {
        comments.value = newData;
        scrollToBottom();
      } else {
        comments.value = newData; // Cập nhật ngầm
      }
    }
  } catch (e) {}
};

const triggerUpload = () => { fileInput.value.click(); };

const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  isUploadingFile.value = true;
  const formData = new FormData();
  formData.append('file', file);
  
  // 🟢 GỌI BIẾN MÔI TRƯỜNG RA SỬ DỤNG
  const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
  const uploadPreset = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET;

  formData.append('upload_preset', uploadPreset); 
  formData.append('cloud_name', cloudName); 

  try {
    // 🟢 NHÉT BIẾN CLOUD NAME VÀO ĐƯỜNG LINK BẰNG CÚ PHÁP ${...}
    const res = await fetch(`https://api.cloudinary.com/v1_1/${cloudName}/auto/upload`, {
      method: "POST",
      body: formData
    });
    const data = await res.json();
    uploadedFileUrl.value = data.secure_url;
  } catch (error) {
    alert("Lỗi tải tệp lên Cloudinary!");
  } finally {
    isUploadingFile.value = false;
    fileInput.value.value = ""; 
  }
};

const sendComment = async () => {
  if (!newCommentText.value.trim() && !uploadedFileUrl.value) return;
  
  const content = newCommentText.value.trim();
  const fileUrl = uploadedFileUrl.value;
  
  newCommentText.value = ""; 
  uploadedFileUrl.value = "";

  try {
    const userId = localStorage.getItem("userId") || 1; 
    await fetch("http://localhost:8080/api/comments", {
      method: "POST", headers: { "Content-Type": "application/json", "User-ID": userId },
      body: JSON.stringify({ taskId: taskData.value.id, content: content, fileUrl: fileUrl })
    });
    
    // Gửi xong thì ép tải lại và bắt buộc kéo màn hình xuống ngay lập tức
    fetchComments(true); 
  } catch (e) {} 
};

const isImage = (url) => { return url.match(/\.(jpeg|jpg|gif|png|webp|bmp)(\?.*)?$/i) !== null; };

const scrollToBottom = () => { 
  nextTick(() => { 
    const c = document.getElementById("chatContainer"); 
    if (c) c.scrollTo({ top: c.scrollHeight, behavior: 'smooth' }); 
  }); 
};

const formatStatus = (s) => s==='TODO'?'Cần làm':s==='IN_PROGRESS'?'Đang làm':s==='DONE'?'Hoàn thành':'Đã hủy';
const getStatusStyle = (s) => s==='TODO'?'bg-slate-200 text-slate-700':s==='IN_PROGRESS'?'bg-blue-100 text-blue-700':s==='DONE'?'bg-emerald-100 text-emerald-700':'bg-red-100 text-red-700';
const formatChatTime = (t) => !t ? '' : new Date(t).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' + new Date(t).toLocaleDateString('vi-VN');
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.custom-scrollbar::-webkit-scrollbar { width: 5px; height: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #475569; }
</style>