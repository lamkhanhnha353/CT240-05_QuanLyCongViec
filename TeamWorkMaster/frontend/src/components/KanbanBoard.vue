<template>
  <div class="flex flex-col w-full h-full relative min-h-0">
    
    <div class="flex flex-col sm:flex-row justify-between px-6 pt-4 pb-2 shrink-0 items-start sm:items-center gap-4">
      <div class="flex flex-1 w-full max-w-2xl gap-3">
        <div class="relative flex-1">
          <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">🔍</span>
          <input v-model="searchQuery" type="text" placeholder="Tìm tên công việc..." class="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm outline-none focus:border-blue-500 shadow-sm dark:text-white transition-all" />
        </div>
        <div class="relative w-48 shrink-0">
          <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">🏷️</span>
          <input v-model="searchTag" type="text" placeholder="Lọc theo nhãn..." class="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm outline-none focus:border-blue-500 shadow-sm dark:text-white transition-all" />
        </div>
      </div>

      <div class="flex items-center space-x-3 shrink-0">
        <button @click="openTaskModal('TODO')" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-bold rounded-xl shadow-md shadow-blue-500/30 transition-all flex items-center hover:-translate-y-0.5">
          <span class="mr-2 text-lg leading-none">+</span> Thêm công việc
        </button>

        <div class="bg-slate-200/70 dark:bg-slate-800/70 p-1.5 rounded-xl flex space-x-1 shadow-inner border border-slate-300/50 dark:border-slate-700/50">
          <button @click="viewMode = 'board'" :class="viewMode === 'board' ? 'bg-white dark:bg-slate-700 shadow-md text-blue-600 dark:text-blue-400' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-300/50 dark:hover:bg-slate-700/50'" class="p-2 rounded-lg transition-all" title="Dạng Kanban">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg>
          </button>
          <button @click="viewMode = 'table'" :class="viewMode === 'table' ? 'bg-white dark:bg-slate-700 shadow-md text-blue-600 dark:text-blue-400' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-300/50 dark:hover:bg-slate-700/50'" class="p-2 rounded-lg transition-all" title="Dạng Danh Sách">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
          </button>
        </div>
      </div>
    </div>

    <div v-if="viewMode === 'board'" class="flex-1 min-h-0 overflow-x-auto overflow-y-hidden px-6 pb-6 pt-2 flex items-start gap-6 custom-scrollbar w-full">
      <div v-for="column in kanbanColumns" :key="column.id" class="flex-1 min-w-[320px] max-w-[400px] flex flex-col h-full max-h-full">
        
        <div class="flex justify-between items-center mb-3 px-1 shrink-0">
          <h3 class="font-bold text-slate-700 dark:text-slate-200 flex items-center text-sm uppercase tracking-wider">
            <span class="w-3 h-3 rounded-full mr-2 shadow-sm" :class="column.colorClass"></span>
            {{ column.title }}
            <span class="ml-2 bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 text-xs px-2 py-0.5 rounded-full font-black">{{ boardTasks[column.id]?.filter(matchFilter).length || 0 }}</span>
          </h3>
        </div>

        <div class="bg-slate-200/50 dark:bg-[#1e293b]/50 rounded-2xl p-3 pb-4 flex-1 min-h-0 overflow-y-auto flex flex-col border border-slate-200/60 dark:border-slate-700/60 shadow-inner custom-scrollbar">
          <draggable 
            v-model="boardTasks[column.id]" group="tasks" item-key="id" 
            @change="onTaskMoved($event, column.id)" class="flex-1 min-h-[100px] space-y-3"
            ghost-class="opacity-40" drag-class="cursor-grabbing" :animation="200"
          >
            <template #item="{ element }">
              <div v-show="matchFilter(element)" @click="openEditModal(element)" class="bg-white dark:bg-slate-800 p-4 rounded-xl shadow-sm border-l-4 cursor-grab active:cursor-grabbing hover:shadow-md transition-all relative group" :class="getPriorityBorder(element.priority)">
                
                <div class="flex justify-between items-start mb-2">
                  <span class="text-[10px] font-black uppercase tracking-widest px-2 py-1 rounded-md" :class="getPriorityTag(element.priority)">{{ formatPriority(element.priority) }}</span>
                </div>
                
                <h4 class="font-bold text-slate-800 dark:text-white text-sm mb-1 leading-snug pr-2" :class="column.id === 'CANCEL' ? 'line-through opacity-70' : ''">{{ element.title }}</h4>
                
                <div v-if="element.tags" class="flex flex-wrap gap-1.5 mb-2 mt-1">
                  <span v-for="(tag, idx) in getTagsArray(element.tags)" :key="idx" class="px-2 py-0.5 rounded-md text-[9px] font-black tracking-wider uppercase shadow-sm" :class="getTagColor(tag)">
                    {{ tag }}
                  </span>
                </div>

                <p v-if="element.description" class="text-xs text-slate-500 dark:text-slate-400 line-clamp-2 mb-3">{{ element.description }}</p>
                <p v-else class="text-xs text-slate-400 dark:text-slate-500 italic mb-3">Không có mô tả</p>
                
                <div class="flex justify-between items-center border-t border-slate-50 dark:border-slate-700/50 pt-3 mt-2">
                  <div class="flex items-center space-x-1.5 text-xs font-bold" :class="isDeadlineNear(element.deadline) && column.id !== 'DONE' && column.id !== 'CANCEL' ? 'text-red-500 dark:text-red-400' : 'text-slate-500 dark:text-slate-400'">
                    <span>⏳</span> 
                    <span v-if="!element.startDate && (!element.deadline || element.deadline === 'null')">Chưa có hạn</span>
                    <span v-else>{{ formatDate(element.startDate) }} - {{ formatDate(element.deadline) }}</span>
                  </div>

                  <div class="flex -space-x-1 relative z-0">
                    <template v-if="getAssigneeArray(element.assigneeName).length > 0">
                      <div class="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-[9px] font-black shadow-sm" :title="getAssigneeArray(element.assigneeName)[0]">
                        {{ getAssigneeArray(element.assigneeName)[0].charAt(0).toUpperCase() }}
                      </div>
                      <div v-if="getAssigneeArray(element.assigneeName).length > 1" 
                           class="w-6 h-6 rounded-full bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-[9px] font-black shadow-sm" title="Nhiều người tham gia">
                        +{{ getAssigneeArray(element.assigneeName).length - 1 }}
                      </div>
                    </template>
                    <div v-else class="w-6 h-6 rounded-full bg-slate-100 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 border-dashed flex items-center justify-center text-slate-400 text-[10px]" title="Chưa phân công">?</div>
                  </div>

                </div>
              </div>
            </template>
          </draggable>
        </div>
      </div>
    </div>

    <div v-if="viewMode === 'table'" class="flex-1 min-h-0 overflow-auto px-6 pb-6 pt-2 custom-scrollbar w-full flex flex-col">
      <div class="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-200 dark:border-slate-700 overflow-hidden flex flex-col flex-1 min-h-0">
        <div class="overflow-y-auto custom-scrollbar flex-1 min-h-0">
          <table class="w-full text-left border-collapse">
            <thead class="sticky top-0 bg-slate-50 dark:bg-slate-800 z-10 shadow-sm border-b border-slate-200 dark:border-slate-700">
              <tr class="text-xs uppercase tracking-widest text-slate-500 dark:text-slate-400">
                <th class="py-4 px-6 font-black w-2/5">Tên công việc & Nhãn</th>
                <th class="py-4 px-4 font-black">Trạng thái</th>
                <th class="py-4 px-4 font-black">Ưu tiên</th>
                <th class="py-4 px-4 font-black">Phân công</th>
                <th class="py-4 px-6 font-black text-right">Thời gian</th>
              </tr>
            </thead>
            <tbody v-if="paginatedTasks.length > 0">
              <tr v-for="task in paginatedTasks" :key="task.id" @click="openEditModal(task)" class="border-b border-slate-100 dark:border-slate-700/50 hover:bg-blue-50/50 dark:hover:bg-slate-700/30 cursor-pointer transition-colors group">
                <td class="py-4 px-6">
                  <p class="font-bold text-slate-800 dark:text-white text-sm mb-1 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors" :class="task.status === 'CANCEL' ? 'line-through opacity-60' : ''">{{ task.title }}</p>
                  <div v-if="task.tags" class="flex flex-wrap gap-1 mt-1.5">
                    <span v-for="(tag, idx) in getTagsArray(task.tags)" :key="idx" class="px-2 py-0.5 rounded text-[9px] font-black uppercase" :class="getTagColor(tag)">{{ tag }}</span>
                  </div>
                </td>
                <td class="py-4 px-4">
                  <span class="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider" :class="getStatusStyle(task.status)">{{ formatStatus(task.status) }}</span>
                </td>
                <td class="py-4 px-4">
                  <span class="text-[10px] font-black uppercase tracking-widest px-2 py-1 rounded-md" :class="getPriorityTag(task.priority)">{{ formatPriority(task.priority) }}</span>
                </td>
                <td class="py-4 px-4">
                  <div class="flex -space-x-1">
                    <template v-if="getAssigneeArray(task.assigneeName).length > 0">
                      <div class="w-7 h-7 rounded-full bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-xs font-black shadow-sm" :title="getAssigneeArray(task.assigneeName)[0]">{{ getAssigneeArray(task.assigneeName)[0].charAt(0).toUpperCase() }}</div>
                      <div v-if="getAssigneeArray(task.assigneeName).length > 1" class="w-7 h-7 rounded-full bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 border-2 border-white dark:border-slate-800 flex items-center justify-center text-xs font-black shadow-sm">+{{ getAssigneeArray(task.assigneeName).length - 1 }}</div>
                    </template>
                    <div v-else class="w-7 h-7 rounded-full bg-slate-100 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 border-dashed flex items-center justify-center text-slate-400 text-xs">?</div>
                  </div>
                </td>
                <td class="py-4 px-6 text-right">
                  <span class="text-sm font-bold text-slate-600 dark:text-slate-400 flex flex-col items-end">
                    <span v-if="task.startDate && task.startDate !== 'null'" class="text-xs text-slate-400">{{ formatDate(task.startDate) }}</span>
                    <span :class="isDeadlineNear(task.deadline) && task.status !== 'DONE' && task.status !== 'CANCEL' ? 'text-red-500' : ''">{{ formatDate(task.deadline) }}</span>
                  </span>
                </td>
              </tr>
            </tbody>
            <tbody v-else>
              <tr><td colspan="5" class="py-12 text-center text-slate-500 font-medium">Không tìm thấy công việc nào.</td></tr>
            </tbody>
          </table>
        </div>
        
        <div v-if="filteredAllTasks.length > 0" class="px-6 py-4 border-t border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 flex items-center justify-between shrink-0">
          <span class="text-sm font-medium text-slate-500 dark:text-slate-400">
            Hiển thị <span class="font-bold text-slate-800 dark:text-white">{{ (currentPage - 1) * itemsPerPage + 1 }}</span> - <span class="font-bold text-slate-800 dark:text-white">{{ Math.min(currentPage * itemsPerPage, filteredAllTasks.length) }}</span> 
            trong <span class="font-bold text-slate-800 dark:text-white">{{ filteredAllTasks.length }}</span> việc
          </span>
          <div class="flex items-center space-x-2">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-600 text-sm font-bold text-slate-600 dark:text-slate-300 disabled:opacity-40 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors">Trước</button>
            <span class="px-3 text-sm font-bold text-slate-700 dark:text-slate-300">Trang {{ currentPage }} / {{ totalPages }}</span>
            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages" class="px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-600 text-sm font-bold text-slate-600 dark:text-slate-300 disabled:opacity-40 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors">Sau</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="pendingMove" class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-[60] p-4">
      <div class="bg-white dark:bg-slate-800 rounded-3xl shadow-2xl w-full max-w-md p-8 text-center animate-fade-in border border-slate-200 dark:border-slate-700">
        <div class="w-16 h-16 bg-blue-50 dark:bg-blue-900/50 text-blue-600 flex items-center justify-center rounded-full mx-auto mb-5 text-3xl shadow-inner">📦</div>
        <h3 class="text-2xl font-black text-slate-800 dark:text-white mb-3">Chuyển trạng thái?</h3>
        <p class="text-slate-500 dark:text-slate-400 mb-8 leading-relaxed">
          Xác nhận chuyển <br/><b class="text-slate-800 dark:text-slate-200 text-lg">"{{ pendingMove.task.title }}"</b> <br/>sang cột <b class="text-blue-600 dark:text-blue-400 uppercase tracking-wide">{{ formatStatus(pendingMove.newStatus) }}</b>?
        </p>
        <div class="flex space-x-4">
          <button @click="cancelTaskMove" class="flex-1 py-3 bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 font-bold rounded-xl hover:bg-slate-200 dark:hover:bg-slate-600 transition-all border border-transparent hover:border-slate-300">Hủy, quay lại</button>
          <button @click="confirmTaskMove" class="flex-1 py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-500/30 hover:-translate-y-0.5">Xác nhận ngay</button>
        </div>
      </div>
    </div>

    <TaskCreateModal v-if="showTaskModal" :projectId="projectId" :projectMembers="projectMembers" :initialColumn="columnForNewTask" @close="showTaskModal = false" @created="onTaskCreated" />
    <TaskDetailModal v-if="showEditModal" :task="editTaskData" :projectMembers="projectMembers" @close="showEditModal = false" @updated="onTaskUpdated" />
  </div>
</template>

<script setup>
import TaskCreateModal from "@/components/TaskCreateModal.vue";
import TaskDetailModal from "@/components/TaskDetailModal.vue";
import { ref, computed, onMounted, watch } from "vue";
import draggable from "vuedraggable";
import { useToast } from "../composables/useToast";

const props = defineProps({ projectId: { type: String, required: true }, userRole: { type: String, required: true } });
const { addToast } = useToast();

const viewMode = ref('board'); 
const searchQuery = ref("");
const searchTag = ref("");

const kanbanColumns = ref([
  { id: 'TODO', title: 'Cần làm', colorClass: 'bg-slate-400' },
  { id: 'IN_PROGRESS', title: 'Đang thực hiện', colorClass: 'bg-blue-500' },
  { id: 'DONE', title: 'Đã hoàn thành', colorClass: 'bg-emerald-500' },
  { id: 'CANCEL', title: 'Đã hủy', colorClass: 'bg-red-500' }
]);

const boardTasks = ref({ 'TODO': [], 'IN_PROGRESS': [], 'DONE': [], 'CANCEL': [] });

const matchFilter = (task) => {
  let matchName = true;
  let matchTag = true;
  if (searchQuery.value.trim()) matchName = task.title.toLowerCase().includes(searchQuery.value.trim().toLowerCase());
  if (searchTag.value.trim()) {
    const taskTags = task.tags ? task.tags.toLowerCase() : "";
    matchTag = taskTags.includes(searchTag.value.trim().toLowerCase());
  }
  return matchName && matchTag;
};

const getAssigneeArray = (namesStr) => {
  if (!namesStr) return [];
  return namesStr.split(',').map(n => n.trim()).filter(n => n);
};

const getTagsArray = (tagsStr) => {
  if (!tagsStr) return [];
  return tagsStr.split(',').map(t => t.trim()).filter(t => t);
};

// 🟢 ĐÃ FIX LẠI THUẬT TOÁN MÀU SẮC NHÃN (Bất kể hoa thường đều ra 1 màu) 🟢
const getTagColor = (tag) => {
  const str = tag.trim().toLowerCase(); // Chuẩn hóa về chữ thường để tính mã màu
  const colors = [
    'bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-400 border border-red-200 dark:border-red-800/50', 
    'bg-blue-100 text-blue-600 dark:bg-blue-900/40 dark:text-blue-400 border border-blue-200 dark:border-blue-800/50', 
    'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/40 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800/50', 
    'bg-purple-100 text-purple-600 dark:bg-purple-900/40 dark:text-purple-400 border border-purple-200 dark:border-purple-800/50', 
    'bg-amber-100 text-amber-600 dark:bg-amber-900/40 dark:text-amber-400 border border-amber-200 dark:border-amber-800/50', 
    'bg-pink-100 text-pink-600 dark:bg-pink-900/40 dark:text-pink-400 border border-pink-200 dark:border-pink-800/50',
    'bg-cyan-100 text-cyan-600 dark:bg-cyan-900/40 dark:text-cyan-400 border border-cyan-200 dark:border-cyan-800/50',
    'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/40 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800/50'
  ];
  let hash = 0;
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash);
  return colors[Math.abs(hash) % colors.length];
};

const allTasks = computed(() => [
  ...(boardTasks.value['TODO'] || []), ...(boardTasks.value['IN_PROGRESS'] || []),
  ...(boardTasks.value['DONE'] || []), ...(boardTasks.value['CANCEL'] || [])
]);
const filteredAllTasks = computed(() => allTasks.value.filter(task => matchFilter(task)));

const currentPage = ref(1);
const itemsPerPage = ref(7); 
const totalPages = computed(() => Math.ceil(filteredAllTasks.value.length / itemsPerPage.value) || 1);
const paginatedTasks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  return filteredAllTasks.value.slice(start, start + itemsPerPage.value);
});
const changePage = (page) => { if (page >= 1 && page <= totalPages.value) currentPage.value = page; };
watch(filteredAllTasks, () => { if (currentPage.value > totalPages.value) currentPage.value = totalPages.value; });

const showTaskModal = ref(false);
const columnForNewTask = ref('TODO'); 
const showEditModal = ref(false);
const editTaskData = ref(null);
const projectMembers = ref([]);

// 🟢 STATE QUẢN LÝ XÁC NHẬN KÉO THẢ 🟢
const pendingMove = ref(null);

onMounted(() => { fetchTasks(); fetchProjectMembers(); });

const fetchTasks = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/tasks/list?projectId=${props.projectId}`);
    if (res.ok) {
        const data = await res.json();
        boardTasks.value = { 'TODO': [], 'IN_PROGRESS': [], 'DONE': [], 'CANCEL': [] };
        data.forEach(task => { 
          boardTasks.value[task.status] ? boardTasks.value[task.status].push(task) : boardTasks.value['TODO'].push(task); 
        });
    }
  } catch (error) { console.error(error); }
};

const fetchProjectMembers = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/projects/members-list?projectId=${props.projectId}`);
    if (res.ok) projectMembers.value = await res.json();
  } catch (error) {}
};

// 🟢 LOGIC KÉO THẢ MỚI: BẬT MODAL HỎI Ý KIẾN TRƯỚC KHI LƯU 🟢
const onTaskMoved = (event, newStatusColumn) => {
  if (event.added) { // event.added chỉ xảy ra khi thẻ bị kéo SANG CỘT KHÁC
    const task = event.added.element;
    pendingMove.value = { task: task, newStatus: newStatusColumn };
  }
};

const confirmTaskMove = async () => {
  if (!pendingMove.value) return;
  const { task, newStatus } = pendingMove.value;
  try {
    // 1. Cập nhật local để mở Modal chi tiết nó hiện đúng cột
    task.status = newStatus; 
    
    // 2. Lưu xuống Database
    await fetch("http://localhost:8080/api/tasks/update-status", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ taskId: task.id, status: newStatus })
    });
    fetchTasks();
    addToast("Chuyển trạng thái thành công!", "success");
  } catch (error) { fetchTasks(); }
  pendingMove.value = null; // Đóng modal
};

const cancelTaskMove = () => {
  pendingMove.value = null; // Đóng modal
  fetchTasks(); // Ép load lại dữ liệu để thẻ tự văng về cột cũ
};

const openTaskModal = (columnId) => { columnForNewTask.value = columnId; showTaskModal.value = true; };
const onTaskCreated = () => { showTaskModal.value = false; addToast("Tạo công việc thành công!", "success"); fetchTasks(); };
const openEditModal = (task) => { editTaskData.value = task; showEditModal.value = true; };
const onTaskUpdated = () => { showEditModal.value = false; addToast("Đã cập nhật công việc!", "success"); fetchTasks(); };

const formatStatus = (s) => s==='TODO'?'Cần làm':s==='IN_PROGRESS'?'Đang làm':s==='DONE'?'Hoàn thành':'Đã hủy';
const getStatusStyle = (s) => s==='TODO'?'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300':s==='IN_PROGRESS'?'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400':s==='DONE'?'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400':'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400';
const formatPriority = (p) => p === 'HIGH' ? 'Cao' : p === 'MEDIUM' ? 'Trung bình' : 'Thấp';
const getPriorityTag = (p) => p === 'HIGH' ? 'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400' : p === 'MEDIUM' ? 'bg-orange-100 text-orange-600 dark:bg-orange-900/30 dark:text-orange-400' : 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300';
const getPriorityBorder = (p) => p === 'HIGH' ? 'border-l-red-500' : p === 'MEDIUM' ? 'border-l-orange-400' : 'border-l-slate-300 dark:border-l-slate-600';
const formatDate = (d) => (!d || d === 'null') ? '' : d.split('-').length === 3 ? `${d.split('-')[2]}/${d.split('-')[1]}` : d;
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