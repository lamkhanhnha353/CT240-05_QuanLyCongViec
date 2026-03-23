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
            <textarea v-model="taskData.description" rows="3" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-600 focus:border-blue-500 outline-none text-sm bg-slate-50 dark:bg-slate-900/50 custom-scrollbar resize-none"></textarea>
          </div>

          <div class="bg-slate-50 dark:bg-slate-900/30 p-5 rounded-2xl border border-slate-100 dark:border-slate-700/50">
            <div class="flex justify-between items-center mb-2">
              <label class="block text-xs font-black text-slate-400 uppercase tracking-widest flex items-center">
                <span class="text-base mr-2">☑️</span> Checklist
              </label>
              <span class="text-xs font-bold" :class="progressPercentage === 100 ? 'text-emerald-500' : 'text-blue-500'">{{ completedSubtasksCount }}/{{ subtasks.length }} hoàn thành</span>
            </div>
            
            <div class="w-full bg-slate-200 dark:bg-slate-700 h-2 rounded-full mb-4 overflow-hidden shadow-inner">
              <div class="h-full transition-all duration-500 rounded-full" :class="progressPercentage === 100 ? 'bg-emerald-500' : 'bg-blue-500'" :style="{ width: progressPercentage + '%' }"></div>
            </div>

            <div class="space-y-2 mb-4">
              <div v-for="sub in displayedSubtasks" :key="sub.id" class="flex items-center group bg-white dark:bg-slate-800 p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm transition-all hover:border-blue-300">
                <input type="checkbox" :checked="sub.isCompleted" @change="toggleSubtask(sub)" class="w-4 h-4 text-blue-600 rounded border-slate-300 focus:ring-blue-500 cursor-pointer" />
                <span class="ml-3 flex-1 text-sm font-medium transition-all" :class="sub.isCompleted ? 'line-through text-slate-400' : 'text-slate-700 dark:text-slate-200'">{{ sub.title }}</span>
                <button @click="deleteSubtask(sub.id)" class="opacity-0 group-hover:opacity-100 text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 p-1.5 rounded-lg transition-all">✕</button>
              </div>
              <div v-if="subtasks.length === 0" class="text-center text-xs text-slate-400 italic py-2">Chưa có mục nào.</div>
              
              <button v-if="subtasks.length > 3" @click="showAllSubtasks = !showAllSubtasks" class="w-full text-center text-xs font-bold text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 py-2 mt-1 bg-blue-50 dark:bg-blue-900/20 rounded-lg transition-colors">
                {{ showAllSubtasks ? 'Thu gọn danh sách ▲' : `Xem thêm ${subtasks.length - 3} mục nữa ▼` }}
              </button>
            </div>

            <div class="flex items-center space-x-2">
              <input v-model="newSubtaskTitle" @keydown.enter.prevent="addSubtask" type="text" placeholder="Thêm việc cần làm..." class="flex-1 px-4 py-2.5 text-sm rounded-xl border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800 outline-none focus:border-blue-500 shadow-sm transition-colors" />
              <button @click="addSubtask" :disabled="!newSubtaskTitle.trim() || isAddingSubtask" class="px-5 py-2.5 bg-slate-100 dark:bg-slate-700 hover:bg-blue-100 hover:text-blue-600 text-slate-600 dark:text-slate-300 text-sm font-bold rounded-xl transition-all disabled:opacity-50">Thêm</button>
            </div>
          </div>

          <div class="bg-slate-50 dark:bg-slate-900/30 p-5 rounded-2xl border border-slate-100 dark:border-slate-700/50">
            <div class="flex justify-between items-center mb-3">
              <label class="block text-xs font-black text-slate-400 uppercase tracking-widest flex items-center">
                <span class="text-base mr-2">📎</span> Tài liệu đính kèm ({{ attachments.length }})
              </label>
              
              <button @click="triggerTaskUpload" :disabled="isUploadingTaskFile" class="text-xs font-bold text-blue-600 hover:text-blue-700 bg-blue-50 hover:bg-blue-100 dark:bg-blue-900/40 dark:hover:bg-blue-900/60 px-3 py-1.5 rounded-lg transition-colors flex items-center disabled:opacity-50">
                <span v-if="isUploadingTaskFile" class="w-3 h-3 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mr-1.5"></span>
                <span v-else>+ Tải lên</span>
              </button>
              <input type="file" ref="taskFileInput" @change="handleTaskFileUpload" class="hidden" />
            </div>
            
            <div class="space-y-2">
              <div v-for="att in attachments" :key="att.id" class="flex items-center group bg-white dark:bg-slate-800 p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm transition-all hover:border-blue-300">
                 
                 <img v-if="isImage(att.fileUrl)" :src="att.fileUrl" class="w-8 h-8 rounded object-cover cursor-pointer border border-slate-200 dark:border-slate-600" @click="selectedImage = att.fileUrl"/>
                 <div v-else class="w-8 h-8 rounded bg-blue-50 dark:bg-blue-900/30 text-blue-500 border border-blue-100 dark:border-blue-800 flex items-center justify-center text-sm">📄</div>
                 
                 <div class="ml-3 flex-1 min-w-0">
                   <span v-if="isImage(att.fileUrl)" @click="selectedImage = att.fileUrl" class="cursor-pointer text-sm font-bold text-slate-700 dark:text-slate-200 truncate block hover:text-blue-600 hover:underline">{{ att.fileName }}</span>
                   <a v-else :href="att.fileUrl" target="_blank" class="text-sm font-bold text-slate-700 dark:text-slate-200 truncate block hover:text-blue-600 hover:underline">{{ att.fileName }}</a>
                   <p class="text-[9px] text-slate-400 font-medium">Bởi {{ att.user }} • {{ formatChatTime(att.time) }}</p>
                 </div>
                 
                 <button @click="deleteAttachment(att.id)" class="opacity-0 group-hover:opacity-100 text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 p-1.5 rounded-lg transition-all" title="Xóa tài liệu">🗑️</button>
              </div>
              <div v-if="attachments.length === 0" class="text-center text-xs text-slate-400 italic py-2">Chưa có tài liệu đính kèm.</div>
            </div>
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
          <button @click="deleteTask" class="text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 px-4 py-2 rounded-lg font-bold text-sm transition-all flex items-center">🗑️ Xóa thẻ này</button>
          <div class="flex space-x-3">
            <button @click="$emit('close')" class="px-5 py-2.5 bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 font-bold rounded-xl hover:bg-slate-200 dark:hover:bg-slate-600 transition-colors">Đóng</button>
            <button @click="submitEdit" :disabled="isSubmitting" class="px-6 py-2.5 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 shadow-md transition-colors flex items-center">
              <span v-if="isSubmitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>Lưu thay đổi
            </button>
          </div>
        </div>
      </div>

      <div class="w-full md:w-[55%] flex flex-col relative h-full bg-slate-50 dark:bg-[#0f172a]">
        
        <div class="flex border-b border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shrink-0">
            <button @click="activeTab = 'comments'" :class="activeTab === 'comments' ? 'border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-500' : 'border-transparent text-slate-500 hover:bg-slate-50 dark:hover:bg-slate-700/50'" class="flex-1 py-5 font-black text-sm border-b-2 transition-all flex items-center justify-center outline-none">
                <span class="mr-2 text-lg">💬</span> Thảo luận
                <span class="ml-2 bg-blue-100 text-blue-600 dark:bg-blue-900/50 dark:text-blue-400 text-[10px] px-2 py-0.5 rounded-full">{{ comments.length }}</span>
            </button>
            <button @click="activeTab = 'logs'" :class="activeTab === 'logs' ? 'border-purple-600 text-purple-600 dark:text-purple-400 dark:border-purple-500' : 'border-transparent text-slate-500 hover:bg-slate-50 dark:hover:bg-slate-700/50'" class="flex-1 py-5 font-black text-sm border-b-2 transition-all flex items-center justify-center outline-none">
                <span class="mr-2 text-lg">📜</span> Lịch sử hoạt động
            </button>
        </div>
        
        <div v-show="activeTab === 'comments'" class="flex-1 flex flex-col h-full overflow-hidden">
            <div class="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar" id="chatContainer">
                <div v-if="comments.length === 0" class="text-center text-slate-400 text-sm mt-10">Chưa có bình luận nào. Bắt đầu thảo luận ngay!</div>
                
                <div v-for="c in comments" :key="c.id" class="flex flex-col" :class="c.user === currentUserName ? 'items-end' : 'items-start'">
                  <span class="text-[10px] text-slate-400 font-bold mb-1 px-1">{{ c.user }} • {{ formatChatTime(c.time) }}</span>
                  <div class="flex flex-col gap-1.5" :class="c.user === currentUserName ? 'items-end' : 'items-start'">
                    <div v-if="c.content" class="max-w-[280px] md:max-w-md px-4 py-3 text-sm shadow-sm whitespace-pre-wrap break-words" :class="c.user === currentUserName ? 'bg-blue-600 text-white rounded-2xl rounded-tr-sm' : 'bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 border border-slate-200 dark:border-slate-600 rounded-2xl rounded-tl-sm'">{{ c.content.replace(/\\n/g, '\n') }}</div>
                    <div v-if="c.fileUrl" class="mt-1">
                      <img v-if="isImage(c.fileUrl)" :src="c.fileUrl" @click="selectedImage = c.fileUrl" class="w-32 h-32 md:w-40 md:h-40 object-cover rounded-xl cursor-pointer hover:opacity-90 border-2 border-slate-200 dark:border-slate-600 shadow-sm transition-all" />
                      <a v-else :href="c.fileUrl" target="_blank" class="flex items-center space-x-2 px-4 py-3 rounded-xl text-xs font-bold shadow-sm transition-colors" :class="c.user === currentUserName ? 'bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-200 dark:bg-blue-900/40 dark:border-blue-800' : 'bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 dark:bg-slate-800 dark:text-slate-200'"><span class="text-xl leading-none">📎</span><span class="underline">Tệp đính kèm</span></a>
                    </div>
                  </div>
                </div>
            </div>
            
            <div class="p-4 border-t border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shrink-0">
                <div v-if="isUploadingFile" class="mb-2 px-3 py-2 bg-blue-50 dark:bg-blue-900/30 rounded-xl flex items-center text-xs text-blue-600 font-bold"><span class="w-3 h-3 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mr-2"></span> Đang tải lên...</div>
                <div v-if="uploadedFileUrl" class="mb-2 px-3 py-2 bg-emerald-50 dark:bg-emerald-900/30 rounded-xl flex justify-between items-center text-xs border border-emerald-200 dark:border-emerald-800"><span class="truncate text-emerald-600 dark:text-emerald-400 font-bold flex items-center"><span class="mr-1 text-base">🖼️</span> Đã đính kèm 1 tệp</span><button @click="uploadedFileUrl = ''" class="text-red-500 hover:bg-red-100 dark:hover:bg-red-900/30 px-2 py-1 rounded-md font-bold transition-colors">Hủy</button></div>

                <div class="flex items-end space-x-2 relative">
                   <input type="file" ref="fileInput" @change="handleChatFileUpload" class="hidden" />
                   <button @click="triggerChatUpload" class="w-12 h-12 shrink-0 flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-all text-xl shadow-sm border border-transparent hover:border-blue-100" title="Đính kèm">📎</button>
                   <textarea v-model="newCommentText" @keydown.enter.exact.prevent="sendComment" placeholder="Nhập bình luận... (Enter gửi, Shift+Enter xuống dòng)" class="flex-1 px-4 py-3.5 rounded-2xl border border-slate-200 dark:border-slate-600 outline-none text-sm bg-slate-50 dark:bg-slate-900 dark:text-white resize-none custom-scrollbar focus:border-blue-500 transition-colors" style="min-height: 48px; max-height: 120px;"></textarea>
                   <button @click="sendComment" :disabled="!newCommentText.trim() && !uploadedFileUrl" class="w-12 h-12 shrink-0 rounded-full bg-blue-600 flex items-center justify-center text-white hover:bg-blue-700 disabled:opacity-50 transition-all shadow-md active:scale-95"><svg class="w-5 h-5 ml-1" fill="currentColor" viewBox="0 0 24 24"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"></path></svg></button>
                </div>
            </div>
        </div>

        <div v-show="activeTab === 'logs'" class="flex-1 overflow-y-auto p-6 bg-slate-50 dark:bg-[#0f172a] custom-scrollbar">
            <div v-if="taskLogs.length === 0" class="text-center text-slate-400 text-sm mt-10">Chưa có hoạt động nào được ghi nhận.</div>
            <div v-else class="relative border-l-2 border-slate-200 dark:border-slate-700 ml-3 space-y-6">
                <div v-for="log in taskLogs" :key="log.id" class="relative pl-6 animate-fade-in">
                    <div class="absolute -left-[9px] top-1 w-4 h-4 rounded-full bg-white dark:bg-slate-800 border-2 border-purple-500"></div>
                    <p class="text-sm text-slate-800 dark:text-slate-200 leading-snug"><span class="font-black text-purple-600 dark:text-purple-400">{{ log.user }}</span> {{ log.action }}</p>
                    <p class="text-[10px] font-bold text-slate-400 mt-1">{{ formatChatTime(log.time) }}</p>
                </div>
            </div>
        </div>
      </div>
      
      <div v-if="selectedImage" @click="selectedImage = null" class="fixed inset-0 z-[60] bg-black/95 flex items-center justify-center p-4 cursor-pointer backdrop-blur-sm">
         <img :src="selectedImage" @click.stop class="max-w-[95vw] max-h-[95vh] rounded-xl shadow-2xl object-contain border border-white/10" />
         <button class="absolute top-6 right-6 w-12 h-12 bg-black/50 hover:bg-red-600 text-white rounded-full flex items-center justify-center text-xl font-black transition-all">✕</button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';

const props = defineProps({ task: { type: Object, required: true }, projectMembers: { type: Array, required: true }});
const emit = defineEmits(['close', 'updated']);

const activeTab = ref('comments'); 
const currentUserName = ref(localStorage.getItem("fullName") || "Bạn"); 
const isSubmitting = ref(false);
let pollingInterval = null; 

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

// 🟢 CHECKLIST & HIỂN THỊ "XEM THÊM" 🟢
const subtasks = ref([]);
const showAllSubtasks = ref(false); // Quản lý trạng thái thu gọn
const displayedSubtasks = computed(() => {
  return showAllSubtasks.value ? subtasks.value : subtasks.value.slice(0, 3);
});

const newSubtaskTitle = ref("");
const isAddingSubtask = ref(false);
const completedSubtasksCount = computed(() => subtasks.value.filter(s => s.isCompleted).length);
const progressPercentage = computed(() => subtasks.value.length === 0 ? 0 : Math.round((completedSubtasksCount.value / subtasks.value.length) * 100));

const fetchSubtasks = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/tasks/subtasks?taskId=${taskData.value.id}`);
    if (res.ok) subtasks.value = await res.json();
  } catch (e) {}
};
const addSubtask = async () => {
  if (!newSubtaskTitle.value.trim()) return;
  isAddingSubtask.value = true;
  try {
    await fetch("http://localhost:8080/api/tasks/subtasks", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ taskId: taskData.value.id, title: newSubtaskTitle.value.trim() }) });
    newSubtaskTitle.value = ""; fetchSubtasks(); fetchTaskLogs(); 
  } catch (e) {} finally { isAddingSubtask.value = false; }
};
const toggleSubtask = async (sub) => {
  try {
    await fetch("http://localhost:8080/api/tasks/subtasks", { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ subtaskId: sub.id, isCompleted: (!sub.isCompleted).toString() }) });
    fetchSubtasks(); fetchTaskLogs();
  } catch (e) { sub.isCompleted = !sub.isCompleted; }
};
const deleteSubtask = async (id) => {
  try {
    await fetch("http://localhost:8080/api/tasks/subtasks", { method: "DELETE", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ subtaskId: id }) });
    fetchSubtasks(); fetchTaskLogs();
  } catch (e) {}
};

// TÀI LIỆU ĐÍNH KÈM TASK
const attachments = ref([]);
const taskFileInput = ref(null);
const isUploadingTaskFile = ref(false);

const fetchAttachments = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/tasks/attachments?taskId=${taskData.value.id}`);
    if (res.ok) attachments.value = await res.json();
  } catch (e) {}
};

const triggerTaskUpload = () => { taskFileInput.value.click(); };

const handleTaskFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  isUploadingTaskFile.value = true;
  
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET); 
  formData.append('cloud_name', import.meta.env.VITE_CLOUDINARY_CLOUD_NAME); 

  try {
    const resCloud = await fetch(`https://api.cloudinary.com/v1_1/${import.meta.env.VITE_CLOUDINARY_CLOUD_NAME}/auto/upload`, { method: "POST", body: formData });
    const dataCloud = await resCloud.json();
    const uploadedUrl = dataCloud.secure_url;
    
    const userId = localStorage.getItem("userId") || 1;
    await fetch("http://localhost:8080/api/tasks/attachments", {
      method: "POST", headers: { "Content-Type": "application/json", "User-ID": userId },
      body: JSON.stringify({ taskId: taskData.value.id, fileName: file.name, fileUrl: uploadedUrl })
    });
    
    fetchAttachments(); 
    fetchTaskLogs();    
  } catch (error) {
    alert("Lỗi tải tệp lên Cloudinary!");
  } finally {
    isUploadingTaskFile.value = false;
    taskFileInput.value.value = ""; 
  }
};

const deleteAttachment = async (id) => {
  if (!confirm("Xóa tài liệu này khỏi công việc?")) return;
  try {
    await fetch("http://localhost:8080/api/tasks/attachments", { 
      method: "DELETE", headers: { "Content-Type": "application/json" }, 
      body: JSON.stringify({ attachmentId: id }) 
    });
    fetchAttachments();
    fetchTaskLogs();
  } catch (e) {}
};

// LỊCH SỬ HOẠT ĐỘNG
const taskLogs = ref([]);
const fetchTaskLogs = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/tasks/logs?taskId=${taskData.value.id}`);
    if (res.ok) taskLogs.value = await res.json();
  } catch (e) {}
};

const submitEdit = async () => {
  if (!taskData.value.title.trim()) return alert("Thiếu tên công việc!");
  isSubmitting.value = true;
  try {
    const currentUserId = localStorage.getItem("userId") || 1;
    const payload = { ...taskData.value, taskId: taskData.value.id, assigneeIds: taskData.value.assigneeIds.join(',') };
    
    await fetch("http://localhost:8080/api/tasks/update-details", {
      method: 'POST', headers: { 'Content-Type': 'application/json', 'User-ID': currentUserId }, body: JSON.stringify(payload)
    });
    await fetch("http://localhost:8080/api/tasks/update-status", {
      method: "POST", headers: { "Content-Type": "application/json", 'User-ID': currentUserId },
      body: JSON.stringify({ taskId: taskData.value.id, status: taskData.value.status, oldStatus: props.task.status })
    });
    emit('updated');
  } catch(e) {}
  finally { isSubmitting.value = false; }
};

const deleteTask = async () => {
  if (!confirm("Chắc chắn xóa thẻ này vĩnh viễn?")) return;
  try {
    const res = await fetch("http://localhost:8080/api/tasks/delete", { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ taskId: taskData.value.id }) });
    if (res.ok) emit('updated');
  } catch(e) {}
};

// BÌNH LUẬN CHAT
const comments = ref([]);
const newCommentText = ref("");
const fileInput = ref(null);
const isUploadingFile = ref(false);
const uploadedFileUrl = ref("");
const selectedImage = ref(null);

// 🟢 INTERVAL TỰ ĐỘNG CẬP NHẬT CHAT, LỊCH SỬ VÀ CHECKLIST 🟢
onMounted(() => { 
  fetchComments(true); 
  fetchSubtasks(); 
  fetchAttachments(); 
  fetchTaskLogs(); 
  pollingInterval = setInterval(() => { 
    fetchComments(false); 
    fetchTaskLogs();   // Cập nhật Lịch sử liên tục
    fetchSubtasks();   // Cập nhật Checklist nếu ai đó đang check
    fetchAttachments(); 
  }, 3000);
});

onUnmounted(() => { if (pollingInterval) clearInterval(pollingInterval); });

const fetchComments = async (forceScroll = false) => {
  try {
    const res = await fetch(`http://localhost:8080/api/comments?taskId=${taskData.value.id}`);
    if (res.ok) { 
      const newData = await res.json();
      if (forceScroll || newData.length > comments.value.length) { comments.value = newData; scrollToBottom(); } 
      else { comments.value = newData; }
    }
  } catch (e) {}
};

const triggerChatUpload = () => { fileInput.value.click(); };
const handleChatFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  isUploadingFile.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET); 
  formData.append('cloud_name', import.meta.env.VITE_CLOUDINARY_CLOUD_NAME); 
  try {
    const res = await fetch(`https://api.cloudinary.com/v1_1/${import.meta.env.VITE_CLOUDINARY_CLOUD_NAME}/auto/upload`, { method: "POST", body: formData });
    const data = await res.json();
    uploadedFileUrl.value = data.secure_url;
  } catch (error) { alert("Lỗi tải tệp lên Cloudinary!"); } 
  finally { isUploadingFile.value = false; fileInput.value.value = ""; }
};

const sendComment = async () => {
  if (!newCommentText.value.trim() && !uploadedFileUrl.value) return;
  const content = newCommentText.value.trim();
  const fileUrl = uploadedFileUrl.value;
  newCommentText.value = ""; uploadedFileUrl.value = "";
  try {
    const userId = localStorage.getItem("userId") || 1; 
    await fetch("http://localhost:8080/api/comments", { method: "POST", headers: { "Content-Type": "application/json", "User-ID": userId }, body: JSON.stringify({ taskId: taskData.value.id, content: content, fileUrl: fileUrl }) });
    fetchComments(true); 
  } catch (e) {} 
};

const isImage = (url) => { return url.match(/\.(jpeg|jpg|gif|png|webp|bmp)(\?.*)?$/i) !== null; };
const scrollToBottom = () => { nextTick(() => { const c = document.getElementById("chatContainer"); if (c) c.scrollTo({ top: c.scrollHeight, behavior: 'smooth' }); }); };
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