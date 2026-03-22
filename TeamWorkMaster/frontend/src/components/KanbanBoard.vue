<template>
  <div class="flex-1 overflow-x-auto overflow-y-hidden p-6 flex items-start gap-6 custom-scrollbar w-full h-full relative">
    <div v-for="column in kanbanColumns" :key="column.id" class="flex-1 min-w-[320px] max-w-[600px] flex flex-col h-full max-h-full">
      <div class="flex justify-between items-center mb-3 px-1 shrink-0">
        <h3 class="font-bold text-slate-700 dark:text-slate-200 flex items-center text-sm uppercase tracking-wider">
          <span class="w-3 h-3 rounded-full mr-2 shadow-sm" :class="column.colorClass"></span>
          {{ column.title }}
          <span class="ml-2 bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 text-xs px-2 py-0.5 rounded-full font-black">{{ boardTasks[column.id]?.length || 0 }}</span>
        </h3>
      </div>

      <div class="bg-slate-200/50 dark:bg-[#1e293b]/50 rounded-2xl p-3 flex-1 overflow-y-auto flex flex-col border border-slate-200/60 dark:border-slate-700/60 shadow-inner custom-scrollbar">
        <draggable 
          v-model="boardTasks[column.id]" group="tasks" item-key="id" 
          @change="onTaskMoved($event, column.id)" class="flex-1 min-h-[150px] space-y-3"
          ghost-class="opacity-40" drag-class="cursor-grabbing" :animation="200"
        >
          <template #item="{ element }">
            <div @click="openEditModal(element)" class="bg-white dark:bg-slate-800 p-4 rounded-xl shadow-sm border-l-4 cursor-grab active:cursor-grabbing hover:shadow-md transition-all relative group" :class="getPriorityBorder(element.priority)">
              <div class="flex justify-between items-start mb-2">
                <span class="text-[10px] font-black uppercase tracking-widest px-2 py-1 rounded-md" :class="getPriorityTag(element.priority)">{{ formatPriority(element.priority) }}</span>
              </div>
              <h4 class="font-bold text-slate-800 dark:text-white text-sm mb-1 leading-snug pr-2">{{ element.title }}</h4>
              <p v-if="element.description" class="text-xs text-slate-500 dark:text-slate-400 line-clamp-2 mb-3">{{ element.description }}</p>
              <p v-else class="text-xs text-slate-400 dark:text-slate-500 italic mb-3">Không có mô tả</p>
              <div class="flex justify-between items-center border-t border-slate-50 dark:border-slate-700/50 pt-3 mt-2">
                <div class="flex items-center space-x-1.5 text-xs font-bold" :class="isDeadlineNear(element.deadline) ? 'text-red-500 dark:text-red-400' : 'text-slate-400 dark:text-slate-500'">
                  <span>⏳</span> <span>{{ formatDate(element.deadline) }}</span>
                </div>
                <div v-if="element.assigneeName" class="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-300 flex items-center justify-center text-[10px] font-black shadow-sm" :title="element.assigneeName">
                  {{ element.assigneeName.charAt(0).toUpperCase() }}
                </div>
                <div v-else class="w-6 h-6 rounded-full bg-slate-100 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 border-dashed flex items-center justify-center text-slate-400 dark:text-slate-400 text-[10px]" title="Chưa phân công">?</div>
              </div>
            </div>
          </template>
        </draggable>

        <button @click="openTaskModal(column.id)" class="mt-3 w-full py-2.5 rounded-xl text-sm font-bold text-slate-500 dark:text-slate-400 hover:bg-slate-300/50 dark:hover:bg-slate-700/50 hover:text-slate-700 dark:hover:text-slate-200 transition-colors flex items-center justify-center border border-transparent hover:border-slate-300 dark:hover:border-slate-600 shrink-0">
          <span class="mr-1">+</span> Thêm thẻ
        </button>
      </div>
    </div>

    <div v-if="showTaskModal" class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white dark:bg-slate-800 w-full max-w-lg rounded-3xl shadow-2xl p-8 relative animate-fade-in text-slate-800 dark:text-white">
        <h2 class="text-2xl font-bold mb-6">Thêm Công việc Mới</h2>
        <div class="space-y-4">
          <div><label class="block text-xs font-bold mb-2">Tên công việc *</label><input v-model="newTask.title" type="text" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 dark:text-white" /></div>
          <div><label class="block text-xs font-bold mb-2">Mô tả chi tiết</label><textarea v-model="newTask.description" rows="3" class="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 dark:text-white"></textarea></div>
          <div class="grid grid-cols-2 gap-4">
            <div><label class="block text-xs font-bold mb-2">Mức độ ưu tiên</label><select v-model="newTask.priority" class="w-full px-4 py-3 rounded-xl border bg-slate-50 dark:bg-slate-900 dark:text-white"><option value="LOW">Thấp</option><option value="MEDIUM">Vừa</option><option value="HIGH">Cao</option></select></div>
            <div><label class="block text-xs font-bold mb-2">Hạn chót</label><input v-model="newTask.deadline" type="date" class="w-full px-4 py-3 rounded-xl border bg-slate-50 dark:bg-slate-900 dark:text-white dark:[color-scheme:dark]" /></div>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-8">
          <button @click="showTaskModal = false" class="px-5 py-2.5 bg-slate-100 dark:bg-slate-700 font-bold rounded-xl">Hủy</button>
          <button @click="submitTask" class="px-5 py-2.5 bg-blue-600 font-bold text-white rounded-xl">Tạo thẻ</button>
        </div>
      </div>
    </div>

    <div v-if="showEditModal" class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div class="bg-white dark:bg-slate-800 w-full max-w-5xl rounded-3xl shadow-2xl flex flex-col md:flex-row h-[85vh] max-h-[800px] text-slate-800 dark:text-slate-200 animate-fade-in">
        
        <div class="w-full md:w-1/2 p-6 md:p-8 border-r border-slate-200 dark:border-slate-700 flex flex-col overflow-y-auto custom-scrollbar">
          <div class="flex justify-between items-start mb-6"><h2 class="text-2xl font-bold">Chi tiết Công việc</h2><button @click="deleteSelectedTask" class="text-red-500 font-bold text-sm">Xóa thẻ này</button></div>
          <div class="space-y-5 flex-1">
            <div><label class="block text-xs font-bold mb-2">Tên công việc</label><input v-model="editTaskData.title" type="text" class="w-full px-4 py-3 rounded-xl border bg-slate-50 dark:bg-slate-900 dark:text-white" /></div>
            <div><label class="block text-xs font-bold mb-2">Mô tả</label><textarea v-model="editTaskData.description" rows="4" class="w-full px-4 py-3 rounded-xl border bg-slate-50 dark:bg-slate-900 dark:text-white custom-scrollbar"></textarea></div>
            <div><label class="block text-xs font-bold mb-2">Giao cho</label><select v-model="editTaskData.assigneeId" class="w-full px-4 py-3 rounded-xl border bg-slate-50 dark:bg-slate-900 dark:text-white"><option value="0">--- Trống ---</option><option v-for="u in projectMembers" :key="u.id" :value="u.id">{{ u.fullName }}</option></select></div>
            <div class="grid grid-cols-2 gap-4">
              <div><label class="block text-xs font-bold mb-2">Ưu tiên</label><select v-model="editTaskData.priority" class="w-full px-4 py-3 rounded-xl border bg-slate-50 dark:bg-slate-900 dark:text-white"><option value="LOW">Thấp</option><option value="MEDIUM">Vừa</option><option value="HIGH">Cao</option></select></div>
              <div><label class="block text-xs font-bold mb-2">Hạn chót</label><input v-model="editTaskData.deadline" type="date" class="w-full px-4 py-3 rounded-xl border bg-slate-50 dark:bg-slate-900 dark:text-white dark:[color-scheme:dark]" /></div>
            </div>
          </div>
          <div class="flex justify-end space-x-3 mt-6 pt-6 border-t border-slate-100 dark:border-slate-700 shrink-0">
            <button @click="showEditModal = false" class="px-5 py-2.5 bg-slate-100 dark:bg-slate-700 font-bold rounded-xl">Đóng</button>
            <button @click="submitEditTask" class="px-5 py-2.5 bg-blue-600 font-bold text-white rounded-xl">Lưu thay đổi</button>
          </div>
        </div>

        <div class="w-full md:w-1/2 flex flex-col relative h-full bg-slate-50 dark:bg-slate-900/50">
          <div class="p-5 border-b border-slate-200 dark:border-slate-700 font-bold text-lg">💬 Thảo luận</div>
          <div class="flex-1 overflow-y-auto p-6 space-y-5 custom-scrollbar" id="chatContainer">
             <div v-for="c in comments" :key="c.id" class="flex flex-col" :class="c.user === currentUserName ? 'items-end' : 'items-start'">
                <span class="text-[11px] text-slate-500 font-bold mb-1 px-1">{{ c.user }}</span>
                <div class="max-w-[85%] px-4 py-3 rounded-2xl text-sm shadow-sm" :class="c.user === currentUserName ? 'bg-blue-600 text-white rounded-tr-sm' : 'bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 border border-slate-200 dark:border-slate-600 rounded-tl-sm'">
                  {{ c.content }}
                </div>
             </div>
          </div>
          <div class="p-4 border-t border-slate-200 dark:border-slate-700 flex items-end space-x-2 bg-white dark:bg-slate-800">
             <textarea v-model="newCommentText" @keydown.enter.prevent="sendComment" placeholder="Nhập bình luận..." class="flex-1 px-4 py-3 rounded-2xl border outline-none text-sm bg-slate-50 dark:bg-slate-900 dark:text-white resize-none custom-scrollbar" style="min-height: 44px; max-height: 120px;"></textarea>
             <button @click="sendComment" class="w-11 h-11 shrink-0 rounded-full bg-blue-600 flex items-center justify-center text-white hover:bg-blue-700 transition-all">Gửi</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import draggable from "vuedraggable";

const props = defineProps({
  projectId: { type: String, required: true },
  userRole: { type: String, required: true }
});

const currentUserName = ref(localStorage.getItem("fullName") || localStorage.getItem("username") || "Bạn"); 

const kanbanColumns = ref([
  { id: 'TODO', title: 'Cần làm', colorClass: 'bg-slate-400' },
  { id: 'IN_PROGRESS', title: 'Đang thực hiện', colorClass: 'bg-blue-500' },
  { id: 'DONE', title: 'Đã hoàn thành', colorClass: 'bg-emerald-500' }
]);

const boardTasks = ref({ 'TODO': [], 'IN_PROGRESS': [], 'DONE': [] });
const showTaskModal = ref(false);
const newTask = ref({ title: '', description: '', priority: 'MEDIUM', deadline: '', targetColumn: 'TODO' });

const showEditModal = ref(false);
const editTaskData = ref({ id: null, title: '', description: '', priority: 'MEDIUM', deadline: '', assigneeId: 0 });
const projectMembers = ref([]);

const comments = ref([]);
const newCommentText = ref("");

onMounted(() => {
  fetchTasks();
  fetchProjectMembers(); 
});

const fetchTasks = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/tasks/list?projectId=${props.projectId}`);
    if (res.ok) {
        const data = await res.json();
        boardTasks.value = { 'TODO': [], 'IN_PROGRESS': [], 'DONE': [] };
        data.forEach(task => { boardTasks.value[task.status] ? boardTasks.value[task.status].push(task) : boardTasks.value['TODO'].push(task); });
    }
  } catch (error) { console.error(error); }
};

const fetchProjectMembers = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/projects/members-list?projectId=${props.projectId}`);
    if (res.ok) projectMembers.value = await res.json();
  } catch (error) {}
};

const onTaskMoved = async (event, newStatusColumn) => {
  if (event.added) {
    const task = event.added.element;
    try {
      await fetch("http://localhost:8080/api/tasks/update-status", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ taskId: task.id, status: newStatusColumn })
      });
    } catch (error) { fetchTasks(); }
  }
};

const openTaskModal = (columnId) => { newTask.value = { title: '', description: '', priority: 'MEDIUM', deadline: '', targetColumn: columnId }; showTaskModal.value = true; };

const submitTask = async () => {
  if (!newTask.value.title.trim()) return alert("Tên công việc không được trống!");
  try {
    const res = await fetch("http://localhost:8080/api/tasks/create", {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ projectId: props.projectId, ...newTask.value, assigneeId: 0 })
    });
    if (res.ok) { showTaskModal.value = false; fetchTasks(); } 
  } catch (error) {}
};

const openEditModal = (task) => {
  editTaskData.value = { id: task.id, title: task.title, description: task.description || '', priority: task.priority, deadline: task.deadline && task.deadline !== 'null' ? task.deadline : '', assigneeId: task.assigneeId || 0 };
  showEditModal.value = true;
  comments.value = [];
  fetchComments(task.id);
};

const submitEditTask = async () => {
  try {
    const res = await fetch("http://localhost:8080/api/tasks/update-details", {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ taskId: editTaskData.value.id, ...editTaskData.value })
    });
    if (res.ok) { showEditModal.value = false; fetchTasks(); }
  } catch (error) {}
};

const deleteSelectedTask = async () => {
  if (!confirm("Xóa thẻ này?")) return;
  try {
    const res = await fetch("http://localhost:8080/api/tasks/delete", {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ taskId: editTaskData.value.id })
    });
    if (res.ok) { showEditModal.value = false; fetchTasks(); }
  } catch (error) {}
};

const fetchComments = async (taskId) => {
  try {
    const res = await fetch(`http://localhost:8080/api/comments?taskId=${taskId}`);
    if (res.ok) { comments.value = await res.json(); scrollToBottom(); }
  } catch (error) {}
};

const sendComment = async (event) => {
  if (event && event.shiftKey) return; 
  if (!newCommentText.value.trim()) return;
  const content = newCommentText.value.trim();
  newCommentText.value = ""; 
  try {
    const userId = localStorage.getItem("userId") || 1; 
    const res = await fetch("http://localhost:8080/api/comments", {
      method: "POST", headers: { "Content-Type": "application/json", "User-ID": userId },
      body: JSON.stringify({ taskId: editTaskData.value.id, content: content })
    });
    if (res.ok) fetchComments(editTaskData.value.id); 
  } catch (error) {} 
};

const scrollToBottom = () => {
  nextTick(() => { const container = document.getElementById("chatContainer"); if (container) container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' }); });
};

const formatPriority = (p) => p === 'HIGH' ? 'Cao' : p === 'MEDIUM' ? 'Trung bình' : 'Thấp';
const getPriorityTag = (p) => p === 'HIGH' ? 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400' : p === 'MEDIUM' ? 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400' : 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300';
const getPriorityBorder = (p) => p === 'HIGH' ? 'border-l-red-500' : p === 'MEDIUM' ? 'border-l-orange-400' : 'border-l-slate-300 dark:border-l-slate-600';
const formatDate = (d) => (!d || d === 'null') ? 'Chưa có hạn' : d.split('-').length === 3 ? `${d.split('-')[2]}/${d.split('-')[1]}` : d;
const formatChatTime = (t) => !t ? '' : new Date(t).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' + new Date(t).toLocaleDateString('vi-VN');
const isDeadlineNear = (d) => { if (!d || d === 'null') return false; const left = (new Date(d) - new Date()) / (1000 * 60 * 60 * 24); return left > 0 && left <= 3; };
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.custom-scrollbar::-webkit-scrollbar { width: 5px; height: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; }
</style>