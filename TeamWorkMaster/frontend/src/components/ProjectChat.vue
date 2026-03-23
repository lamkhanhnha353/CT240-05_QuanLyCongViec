<template>
  <div class="flex-1 flex h-full bg-white dark:bg-slate-900 overflow-hidden border-l border-slate-200 dark:border-slate-800 relative">
    
    <div class="flex-1 flex flex-col h-full min-w-0 transition-all duration-300">
      
      <div class="h-16 px-6 lg:px-8 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50 shrink-0">
        <div class="flex items-center space-x-3">
          <span class="text-2xl">💬</span>
          <div>
            <h2 class="text-lg font-black text-slate-800 dark:text-white leading-tight">Kênh Thảo Luận Chung</h2>
            <p class="text-xs font-bold text-slate-500 dark:text-slate-400 hidden md:block">Nơi mọi người trong dự án giao lưu và chia sẻ tài liệu</p>
          </div>
        </div>
        
        <div class="flex items-center space-x-4">
          <div class="text-sm font-bold text-slate-400 bg-slate-100 dark:bg-slate-800 px-3 py-1 rounded-lg hidden sm:block">
            {{ messages.length }} tin nhắn
          </div>
          <button @click="toggleRightSidebar" class="w-10 h-10 rounded-full flex items-center justify-center transition-all" :class="isRightSidebarOpen ? 'bg-blue-100 text-blue-600 dark:bg-blue-900/40 dark:text-blue-400' : 'text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar bg-slate-50/50 dark:bg-transparent" id="generalChatContainer">
        <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full opacity-50 space-y-4">
          <div class="w-24 h-24 bg-blue-100 dark:bg-blue-900/30 text-blue-500 rounded-full flex items-center justify-center text-4xl">👋</div>
          <h3 class="text-lg font-bold text-slate-600 dark:text-slate-300">Chào mừng đến với Kênh Thảo Luận!</h3>
          <p class="text-sm font-medium text-slate-400 text-center max-w-md">Hãy gửi một tin nhắn, hình ảnh hoặc tài liệu đính kèm để bắt đầu.</p>
        </div>

        <div v-for="(msg, index) in messages" :key="index" class="flex items-start space-x-4 group">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 text-white flex items-center justify-center font-black shrink-0 shadow-sm">
            {{ msg.user.charAt(0).toUpperCase() }}
          </div>
          
          <div class="flex-1 max-w-[85%] lg:max-w-[75%]">
            <div class="flex items-baseline space-x-2 mb-1">
              <span class="font-bold text-slate-800 dark:text-slate-200">{{ msg.user }}</span>
              <span class="text-[10px] font-bold text-slate-400">{{ formatChatTime(msg.time) }}</span>
            </div>

            <div class="flex items-center gap-3">
              <div class="bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 p-3 rounded-2xl rounded-tl-none shadow-sm inline-flex flex-col gap-2">
                <div v-if="msg.content" class="text-sm text-slate-700 dark:text-slate-300 leading-relaxed whitespace-pre-wrap break-words" v-html="formatContentWithLinks(msg.content)"></div>
                
                <div v-if="msg.fileUrl" class="mt-1">
                  <img v-if="isImage(msg.fileUrl)" :src="msg.fileUrl" class="max-w-full max-h-64 rounded-xl border border-slate-200 dark:border-slate-700 object-cover cursor-pointer hover:opacity-90 transition-opacity" @click="openImageModal(msg.fileUrl)" alt="Đính kèm" />
                  <a v-else :href="msg.fileUrl" target="_blank" class="flex items-center space-x-3 p-3 bg-blue-50 dark:bg-blue-900/20 hover:bg-blue-100 dark:hover:bg-blue-900/40 border border-blue-100 dark:border-blue-800/50 rounded-xl transition-colors">
                    <span class="text-2xl">📄</span>
                    <span class="text-sm font-bold text-blue-600 dark:text-blue-400 underline decoration-blue-300 dark:decoration-blue-700 underline-offset-2">Tệp đính kèm</span>
                  </a>
                </div>
              </div>

              <button v-if="msg.user === currentUserName" @click="confirmRevoke(msg.id)" class="opacity-0 group-hover:opacity-100 p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-full transition-all shrink-0" title="Thu hồi tin nhắn">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
              </button>
            </div>
            
          </div>
        </div>
      </div>

      <div class="p-4 lg:p-6 bg-white dark:bg-slate-800 border-t border-slate-200 dark:border-slate-700 shrink-0 flex flex-col gap-3">
        <div v-if="selectedFile" class="flex items-center space-x-3 p-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl w-max relative group animate-fade-in">
          <img v-if="filePreview" :src="filePreview" class="w-10 h-10 object-cover rounded-lg border border-slate-200 dark:border-slate-600" />
          <span v-else class="text-3xl">📄</span>
          <div class="flex flex-col pr-6">
            <span class="text-sm font-bold text-slate-700 dark:text-slate-300 max-w-[200px] truncate">{{ selectedFile.name }}</span>
            <span class="text-[10px] text-slate-500 font-bold uppercase">{{ (selectedFile.size / 1024 / 1024).toFixed(2) }} MB</span>
          </div>
          <button @click="cancelAttachment" class="w-6 h-6 bg-slate-200 dark:bg-slate-700 hover:bg-red-100 hover:text-red-500 text-slate-500 rounded-full flex items-center justify-center font-black transition-colors absolute -top-2 -right-2 shadow-sm">&times;</button>
        </div>

        <div class="flex items-end space-x-2 lg:space-x-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-2xl p-2 focus-within:ring-2 focus-within:ring-blue-500/50 transition-all shadow-inner">
          <button @click="triggerFileInput" class="w-10 h-10 shrink-0 rounded-xl text-slate-400 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-500/10 flex items-center justify-center transition-colors outline-none" title="Đính kèm file hoặc ảnh">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"></path></svg>
          </button>
          <input type="file" ref="fileInputRef" @change="handleFileChange" class="hidden" accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.zip" />

          <textarea 
            v-model="newMessage" @keydown.enter.prevent="handleSend" rows="1"
            placeholder="Nhập tin nhắn..." 
            class="flex-1 py-2 bg-transparent outline-none text-sm text-slate-800 dark:text-white resize-none custom-scrollbar" 
            style="min-height: 40px; max-height: 120px;"
          ></textarea>
          
          <button @click="handleSend" :disabled="isSending || (!newMessage.trim() && !selectedFile)" class="w-10 h-10 shrink-0 rounded-xl bg-blue-600 flex items-center justify-center text-white hover:bg-blue-700 disabled:opacity-50 transition-all shadow-md">
            <svg v-if="!isSending" class="w-5 h-5 ml-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path></svg>
            <div v-else class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
          </button>
        </div>
        <div v-if="isUploadingFile" class="text-xs font-bold text-blue-500 animate-pulse px-2">⏳ Đang tải tệp lên mây...</div>
      </div>
    </div>

    <aside :class="isRightSidebarOpen ? 'w-80 border-l border-slate-200 dark:border-slate-700' : 'w-0 border-l-0'" class="bg-slate-50 dark:bg-slate-800/30 flex flex-col shrink-0 transition-all duration-300 overflow-hidden h-full z-10 relative">
      <div class="h-16 px-5 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between shrink-0 bg-white dark:bg-slate-800">
        <h3 class="font-black text-slate-800 dark:text-white whitespace-nowrap">Thông tin hội thoại</h3>
        <button @click="toggleRightSidebar" class="text-slate-400 hover:text-slate-600 dark:hover:text-white rounded-full p-1"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
      </div>

      <div class="flex-1 overflow-y-auto p-5 space-y-8 custom-scrollbar">
        <div>
          <h4 class="text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3 flex items-center"><span class="mr-2">🖼️</span> Ảnh đính kèm ({{ sharedImages.length }})</h4>
          <div v-if="sharedImages.length > 0" class="grid grid-cols-3 gap-2">
            <div v-for="(img, idx) in sharedImages" :key="idx" class="aspect-square rounded-lg overflow-hidden border border-slate-200 dark:border-slate-700 cursor-pointer hover:opacity-80 transition-all" @click="openImageModal(img.fileUrl)" :title="`Gửi bởi ${img.user}`">
              <img :src="img.fileUrl" class="w-full h-full object-cover" />
            </div>
          </div>
          <p v-else class="text-xs text-slate-400 italic">Chưa có ảnh nào được chia sẻ.</p>
        </div>

        <div class="h-px bg-slate-200 dark:bg-slate-700"></div>

        <div>
          <h4 class="text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3 flex items-center"><span class="mr-2">📁</span> Tài liệu ({{ sharedFiles.length }})</h4>
          <div v-if="sharedFiles.length > 0" class="space-y-2">
            <a v-for="(file, idx) in sharedFiles" :key="idx" :href="file.fileUrl" target="_blank" class="flex items-center p-2 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors group">
              <div class="w-8 h-8 rounded bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 flex items-center justify-center mr-3 shrink-0">📄</div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-bold text-slate-700 dark:text-slate-200 truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">Tệp đính kèm</p>
                <p class="text-[10px] text-slate-500">{{ file.user }} • {{ formatChatDateOnly(file.time) }}</p>
              </div>
            </a>
          </div>
          <p v-else class="text-xs text-slate-400 italic">Chưa có tài liệu nào được chia sẻ.</p>
        </div>

        <div class="h-px bg-slate-200 dark:bg-slate-700"></div>

        <div>
          <h4 class="text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-3 flex items-center"><span class="mr-2">🔗</span> Link chia sẻ ({{ sharedLinks.length }})</h4>
          <div v-if="sharedLinks.length > 0" class="space-y-3">
            <a v-for="(link, idx) in sharedLinks" :key="idx" :href="link.url" target="_blank" class="block p-3 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 hover:border-blue-300 dark:hover:border-blue-600 hover:shadow-sm transition-all">
              <p class="text-sm font-medium text-blue-600 dark:text-blue-400 break-all line-clamp-2 leading-snug">{{ link.url }}</p>
              <p class="text-[10px] text-slate-500 mt-2 font-bold">{{ link.user }} • {{ formatChatDateOnly(link.time) }}</p>
            </a>
          </div>
          <p v-else class="text-xs text-slate-400 italic">Chưa có link nào được chia sẻ.</p>
        </div>
      </div>
    </aside>

    <div v-if="selectedImageToView" class="fixed inset-0 z-[70] bg-black/90 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in" @click="selectedImageToView = null">
      <button @click="selectedImageToView = null" class="absolute top-6 right-6 text-white/50 hover:text-white text-5xl font-bold transition-colors outline-none">&times;</button>
      <img :src="selectedImageToView" class="max-w-full max-h-full object-contain rounded-lg shadow-2xl cursor-default" @click.stop />
    </div>

    <div v-if="confirmModal.show" class="fixed inset-0 z-[80] bg-slate-900/60 dark:bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in">
      <div class="bg-white dark:bg-slate-800 rounded-3xl p-6 max-w-sm w-full shadow-2xl text-slate-800 dark:text-white">
        <h3 class="text-xl font-bold mb-2 flex items-center"><span class="text-red-500 mr-2">🗑️</span> Thu hồi tin nhắn</h3>
        <p class="text-sm text-slate-500 dark:text-slate-400 mb-6 font-medium leading-relaxed">Bạn có chắc chắn muốn thu hồi tin nhắn này không? Hành động này sẽ xóa tin nhắn vĩnh viễn.</p>
        <div class="flex justify-end gap-3">
          <button @click="confirmModal.show = false" class="px-5 py-2.5 bg-slate-100 dark:bg-slate-700 font-bold rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-600 transition-colors">Hủy</button>
          <button @click="executeRevoke" class="px-5 py-2.5 bg-red-500 hover:bg-red-600 font-bold text-white rounded-xl shadow-lg shadow-red-500/30 transition-all flex items-center">
            <span v-if="isRevoking" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
            Xác nhận
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import { useToast } from '../composables/useToast'; 

const props = defineProps({
  projectId: { type: String, required: true }
});

const { addToast } = useToast();

const currentUserName = ref(localStorage.getItem("fullName") || localStorage.getItem("username") || "Bạn");
const currentUserId = localStorage.getItem("userId") || 1; 

const messages = ref([]);
const newMessage = ref("");
const isSending = ref(false);
const isRevoking = ref(false); 

const isRightSidebarOpen = ref(false);
const toggleRightSidebar = () => { isRightSidebarOpen.value = !isRightSidebarOpen.value; };

const selectedImageToView = ref(null);
const openImageModal = (url) => { selectedImageToView.value = url; };

const fileInputRef = ref(null);
const selectedFile = ref(null);
const filePreview = ref(null);
const isUploadingFile = ref(false);

let pollingInterval = null;

onMounted(() => {
  fetchMessages();
  pollingInterval = setInterval(() => { fetchMessagesSilently(); }, 3000);
});

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
});

const fetchMessages = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/project-chat?projectId=${props.projectId}`);
    if (res.ok) {
      messages.value = await res.json();
      scrollToBottom();
    }
  } catch (error) { console.error("Lỗi tải tin nhắn:", error); }
};

const fetchMessagesSilently = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/project-chat?projectId=${props.projectId}`);
    if (res.ok) {
      const newData = await res.json();
      if (newData.length > messages.value.length) {
        messages.value = newData;
        scrollToBottom();
      } else {
        messages.value = newData;
      }
    }
  } catch (error) {}
};

const confirmModal = ref({ show: false, messageId: null });
const confirmRevoke = (id) => { confirmModal.value = { show: true, messageId: id }; };

const executeRevoke = async () => {
  if (!confirmModal.value.messageId) return;
  isRevoking.value = true;
  
  try {
    const res = await fetch(`http://localhost:8080/api/project-chat?messageId=${confirmModal.value.messageId}`, {
      method: "DELETE",
      headers: { "User-ID": currentUserId }
    });

    if (res.ok) {
      addToast("Đã xóa tin nhắn thành công!", "success"); 
      fetchMessages(); 
    } else {
      addToast("Không thể xóa tin nhắn này.", "error"); 
    }
  } catch (error) {
    addToast("Đã xảy ra lỗi hệ thống.", "error");
  } finally {
    isRevoking.value = false;
    confirmModal.value.show = false; 
  }
};

const isImage = (url) => url ? url.match(/\.(jpeg|jpg|gif|png|webp|svg)$/i) != null : false;
const sharedImages = computed(() => messages.value.filter(m => m.fileUrl && isImage(m.fileUrl)).reverse());
const sharedFiles = computed(() => messages.value.filter(m => m.fileUrl && !isImage(m.fileUrl)).reverse());
const sharedLinks = computed(() => {
  let links = [];
  const urlRegex = /(https?:\/\/[^\s]+)/g; 
  messages.value.forEach(m => {
    if (m.content) {
      const found = m.content.match(urlRegex);
      if (found) found.forEach(url => links.push({ url: url, user: m.user, time: m.time }));
    }
  });
  return links.reverse(); 
});

const formatContentWithLinks = (text) => {
  if (!text) return "";
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  return text.replace(urlRegex, '<a href="$1" target="_blank" class="text-blue-600 dark:text-blue-400 hover:underline break-all">$1</a>');
};

const triggerFileInput = () => { fileInputRef.value.click(); };
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  if (file.size > 10 * 1024 * 1024) { 
    addToast("File quá lớn! Vui lòng chọn file dưới 10MB.", "error"); 
    return; 
  }
  selectedFile.value = file;
  if (file.type.startsWith('image/')) { filePreview.value = URL.createObjectURL(file); } 
  else { filePreview.value = null; }
};
const cancelAttachment = () => {
  selectedFile.value = null; filePreview.value = null;
  if (fileInputRef.value) fileInputRef.value.value = ""; 
};

// 🟢 ĐÃ CẬP NHẬT: GỌI BIẾN MÔI TRƯỜNG CHO CLOUDINARY
const uploadFileToCloudinary = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  
  // Lấy biến môi trường (Từ file .env)
  const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
  const uploadPreset = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET;

  formData.append("upload_preset", uploadPreset);
  formData.append("folder", `Teamwork_Master/Project_${props.projectId}`); 
  
  try {
    // Dùng backticks ` để nhúng biến cloudName vào URL, và dùng /auto/upload
    const response = await fetch(`https://api.cloudinary.com/v1_1/${cloudName}/auto/upload`, { 
      method: "POST", 
      body: formData 
    });
    const data = await response.json();
    return data.secure_url; 
  } catch (error) { 
    return null; 
  }
};

const handleSend = async (event) => {
  if (event && event.shiftKey) return; 
  if (!newMessage.value.trim() && !selectedFile.value) return; 
  
  const content = newMessage.value.trim();
  isSending.value = true;
  let finalFileUrl = "";

  if (selectedFile.value) {
    isUploadingFile.value = true;
    finalFileUrl = await uploadFileToCloudinary(selectedFile.value);
    isUploadingFile.value = false;
    if (!finalFileUrl) { 
      addToast("Lỗi tải file lên Cloudinary!", "error"); 
      isSending.value = false; 
      return; 
    }
  }

  try {
    const res = await fetch("http://localhost:8080/api/project-chat", {
      method: "POST",
      headers: { "Content-Type": "application/json", "User-ID": currentUserId },
      body: JSON.stringify({ projectId: props.projectId, content: content, fileUrl: finalFileUrl })
    });

    if (res.ok) {
      newMessage.value = ""; 
      cancelAttachment();
      fetchMessages(); 
    } else { 
      addToast("Lỗi lưu tin nhắn!", "error"); 
    }
  } catch (error) {
    addToast("Mất kết nối máy chủ!", "error"); 
  } finally { 
    isSending.value = false; 
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    const container = document.getElementById("generalChatContainer");
    if (container) container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
  });
};

const formatChatTime = (t) => {
  if (!t) return '';
  const date = new Date(t);
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' + date.toLocaleDateString('vi-VN');
};

const formatChatDateOnly = (t) => {
  if (!t) return '';
  return new Date(t).toLocaleDateString('vi-VN');
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; }
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>