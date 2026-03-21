<template>
  <div class="min-h-screen bg-[#f4f5f7] flex flex-col font-sans h-screen overflow-hidden">
    
    <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 shrink-0 z-10 shadow-sm">
      <div class="flex items-center space-x-4">
        <button @click="goBack" class="p-2 text-slate-400 hover:bg-slate-100 rounded-full transition-colors" title="Trở về danh sách dự án">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
        </button>
        <div class="h-6 w-px bg-slate-200"></div>
        <h1 class="text-xl font-black text-slate-800 tracking-tight">{{ projectName }}</h1>
        <span class="px-2.5 py-1 bg-slate-100 text-slate-500 text-[10px] font-bold uppercase rounded-md border border-slate-200">{{ userRole }}</span>
      </div>
      
      <div class="flex space-x-3">
        <button v-if="['OWNER', 'MANAGER'].includes(userRole)" @click="openMemberModal" class="px-4 py-2 bg-blue-50 text-blue-600 text-sm font-bold rounded-xl hover:bg-blue-100 transition-all border border-blue-100 flex items-center">
          <span class="mr-2">👥</span> Mời Thành Viên
        </button>
      </div>
    </header>

    <main class="flex-1 overflow-x-auto p-8 flex items-start space-x-6">
      
      <div v-for="column in kanbanColumns" :key="column.id" class="w-80 shrink-0 flex flex-col max-h-full">
        <div class="flex justify-between items-center mb-4 px-1">
          <h3 class="font-bold text-slate-700 flex items-center text-sm uppercase tracking-wider">
            <span class="w-3 h-3 rounded-full mr-2 shadow-sm" :class="column.colorClass"></span>
            {{ column.title }}
            <span class="ml-2 bg-slate-200 text-slate-600 text-xs px-2 py-0.5 rounded-full font-black">{{ boardTasks[column.id]?.length || 0 }}</span>
          </h3>
        </div>

        <div class="bg-slate-200/50 rounded-2xl p-3 flex-1 overflow-y-auto flex flex-col border border-slate-200/60 shadow-inner">
          <draggable 
            v-model="boardTasks[column.id]" 
            group="tasks" 
            item-key="id" 
            @change="onTaskMoved($event, column.id)" 
            class="flex-1 min-h-[150px] space-y-3"
            ghost-class="opacity-40"
            drag-class="cursor-grabbing"
            :animation="200"
          >
            <template #item="{ element }">
              <div @click="openEditModal(element)" class="bg-white p-4 rounded-xl shadow-sm border-l-4 cursor-grab active:cursor-grabbing hover:shadow-md transition-all relative group"
                   :class="getPriorityBorder(element.priority)">
                
                <div class="flex justify-between items-start mb-2">
                  <span class="text-[10px] font-black uppercase tracking-widest px-2 py-1 rounded-md" :class="getPriorityTag(element.priority)">
                    {{ formatPriority(element.priority) }}
                  </span>
                </div>

                <h4 class="font-bold text-slate-800 text-sm mb-1 leading-snug pr-2">{{ element.title }}</h4>
                <p v-if="element.description" class="text-xs text-slate-500 line-clamp-2 mb-3">{{ element.description }}</p>
                <p v-else class="text-xs text-slate-400 italic mb-3">Không có mô tả</p>
                
                <div class="flex justify-between items-center border-t border-slate-50 pt-3 mt-2">
                  <div class="flex items-center space-x-1.5 text-xs font-bold" :class="isDeadlineNear(element.deadline) ? 'text-red-500' : 'text-slate-400'">
                    <span>⏳</span>
                    <span>{{ formatDate(element.deadline) }}</span>
                  </div>
                  <div v-if="element.assigneeName" class="w-6 h-6 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-[10px] font-black" :title="element.assigneeName">
                    {{ element.assigneeName.charAt(0).toUpperCase() }}
                  </div>
                  <div v-else class="w-6 h-6 rounded-full bg-slate-100 border border-slate-200 border-dashed flex items-center justify-center text-slate-400 text-[10px]" title="Chưa phân công">?</div>
                </div>
              </div>
            </template>
          </draggable>

          <button @click="openTaskModal(column.id)" class="mt-3 w-full py-2.5 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-300/50 hover:text-slate-700 transition-colors flex items-center justify-center border border-transparent hover:border-slate-300">
            <span class="mr-1">+</span> Thêm thẻ
          </button>
        </div>
      </div>
    </main>

    <div v-if="showTaskModal" class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-lg rounded-3xl shadow-2xl p-8 relative animate-fade-in">
        <h2 class="text-2xl font-bold text-slate-800 mb-6">Thêm Công việc Mới</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Tên công việc <span class="text-red-500">*</span></label>
            <input v-model="newTask.title" type="text" placeholder="Nhập tên việc cần làm..." class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 focus:bg-white transition-all" />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Mô tả chi tiết</label>
            <textarea v-model="newTask.description" rows="3" placeholder="Mô tả công việc này..." class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 focus:bg-white transition-all"></textarea>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Mức độ ưu tiên</label>
              <select v-model="newTask.priority" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none cursor-pointer bg-slate-50 text-sm font-medium">
                <option value="LOW">Thấp (Low)</option>
                <option value="MEDIUM">Vừa (Medium)</option>
                <option value="HIGH">Cao (High)</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Hạn chót</label>
              <input v-model="newTask.deadline" type="date" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 transition-all" />
            </div>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-8">
          <button @click="showTaskModal = false" class="px-5 py-2.5 bg-slate-100 font-bold text-slate-600 rounded-xl hover:bg-slate-200 transition-all">Hủy</button>
          <button @click="submitTask" class="px-5 py-2.5 bg-blue-600 font-bold text-white rounded-xl hover:bg-blue-700 shadow-lg shadow-blue-500/30 transition-all">Tạo thẻ</button>
        </div>
      </div>
    </div>

    <div v-if="showEditModal" class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-lg rounded-3xl shadow-2xl p-8 relative animate-fade-in">
        <div class="flex justify-between items-start mb-6">
          <h2 class="text-2xl font-bold text-slate-800">Chi tiết Công việc</h2>
          <button @click="deleteSelectedTask" class="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all" title="Xóa thẻ này">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
          </button>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Tên công việc</label>
            <input v-model="editTaskData.title" type="text" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 focus:bg-white transition-all" />
          </div>
          
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Mô tả chi tiết</label>
            <textarea v-model="editTaskData.description" rows="3" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 focus:bg-white transition-all"></textarea>
          </div>
          
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Giao cho (Assignee)</label>
            <select v-model="editTaskData.assigneeId" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none cursor-pointer bg-slate-50 text-sm font-medium">
              <option value="0">--- Chưa phân công ---</option>
              <option v-for="user in projectMembers" :key="user.id" :value="user.id">
                {{ user.fullName }} ({{ user.email }})
              </option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Ưu tiên</label>
              <select v-model="editTaskData.priority" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none cursor-pointer bg-slate-50 text-sm font-medium">
                <option value="LOW">Thấp</option>
                <option value="MEDIUM">Vừa</option>
                <option value="HIGH">Cao</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Hạn chót</label>
              <input v-model="editTaskData.deadline" type="date" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 transition-all" />
            </div>
          </div>
        </div>

        <div class="flex justify-end space-x-3 mt-8 border-t border-slate-100 pt-5">
          <button @click="showEditModal = false" class="px-5 py-2.5 bg-slate-100 font-bold text-slate-600 rounded-xl hover:bg-slate-200 transition-all">Đóng</button>
          <button @click="submitEditTask" class="px-5 py-2.5 bg-blue-600 font-bold text-white rounded-xl hover:bg-blue-700 shadow-lg shadow-blue-500/30 transition-all">Lưu thay đổi</button>
        </div>
      </div>
    </div>

    <div v-if="showMemberModal" class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white w-full max-w-md rounded-3xl shadow-2xl p-8 relative overflow-visible animate-fade-in">
        <h2 class="text-2xl font-bold text-slate-800 mb-2">Mời vào dự án</h2>
        <p class="text-sm text-slate-500 mb-6 font-medium">Dự án: <span class="font-bold text-slate-700">{{ projectName }}</span></p>
        
        <div class="space-y-5">
          <div class="relative" v-click-outside="() => showDropdown = false">
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Tìm kiếm Username hoặc Email</label>
            <div class="relative">
              <span class="absolute left-4 top-3 text-slate-400">@</span>
              <input 
                v-model="memberSearchQuery" 
                @input="handleSearchInput"
                @focus="showDropdown = searchResults.length > 0"
                type="text" 
                autocomplete="off"
                placeholder="Gõ tên hoặc email để tìm..." 
                class="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium bg-slate-50 focus:bg-white transition-all" 
              />
              <div v-if="isSearching" class="absolute right-4 top-3.5">
                <div class="w-4 h-4 border-2 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>
              </div>
            </div>

            <div v-if="showDropdown && searchResults.length > 0" class="absolute w-full mt-2 bg-white border border-slate-100 rounded-xl shadow-xl z-50 max-h-60 overflow-y-auto">
              <div v-for="user in searchResults" :key="user.id" @click="selectUser(user)" class="flex items-center p-3 hover:bg-blue-50 cursor-pointer border-b border-slate-50 transition-colors">
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-slate-200 to-slate-300 flex items-center justify-center text-slate-600 font-bold text-xs uppercase mr-3">
                  {{ user.fullName.charAt(0) }}
                </div>
                <div>
                  <p class="text-sm font-bold text-slate-800 leading-none">{{ user.fullName }} <span class="text-xs text-slate-400 font-medium ml-1">(@{{ user.username }})</span></p>
                  <p class="text-xs text-slate-500 mt-1">{{ user.email }}</p>
                </div>
              </div>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Cấp quyền (Role)</label>
            <select v-model="newMemberRole" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-blue-500 outline-none cursor-pointer bg-white text-sm font-medium">
              <option value="MEMBER">👤 Thành viên (Chỉ xem và làm task)</option>
              <option value="MANAGER" v-if="userRole === 'OWNER'">💼 Quản lý (Sửa dự án, mời người)</option>
            </select>
          </div>
        </div>

        <div class="flex justify-end space-x-3 mt-8">
          <button @click="showMemberModal = false" class="px-5 py-2.5 bg-slate-100 font-bold text-slate-600 rounded-xl hover:bg-slate-200 transition-all">Hủy</button>
          <button @click="submitInvite" class="px-5 py-2.5 bg-blue-600 font-bold text-white rounded-xl hover:bg-blue-700 shadow-lg shadow-blue-500/30 transition-all flex items-center">
            <span>Gửi lời mời</span>
            <span class="ml-2">→</span>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import draggable from "vuedraggable";
import { useToast } from "../composables/useToast";

const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = function(event) {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event, el);
      }
    };
    document.body.addEventListener('click', el.clickOutsideEvent);
  },
  unmounted(el) {
    document.body.removeEventListener('click', el.clickOutsideEvent);
  }
};

const route = useRoute();
const router = useRouter();
const { addToast } = useToast();

const projectId = route.params.projectId; 
const projectName = ref(route.query.projectName || "Dự án Không tên");
const userRole = ref(route.query.role || "MEMBER");

const kanbanColumns = ref([
  { id: 'TODO', title: 'Cần làm', colorClass: 'bg-slate-400' },
  { id: 'IN_PROGRESS', title: 'Đang thực hiện', colorClass: 'bg-blue-500' },
  { id: 'DONE', title: 'Đã hoàn thành', colorClass: 'bg-emerald-500' }
]);

const boardTasks = ref({ 'TODO': [], 'IN_PROGRESS': [], 'DONE': [] });

const showTaskModal = ref(false);
const newTask = ref({ title: '', description: '', priority: 'MEDIUM', deadline: '', targetColumn: 'TODO' });

// --- BIẾN MỚI THÊM CHO SỬA/XÓA/GIAO VIỆC ---
const showEditModal = ref(false);
const editTaskData = ref({ id: null, title: '', description: '', priority: 'MEDIUM', deadline: '', assigneeId: 0 });
const projectMembers = ref([]);

onMounted(() => {
  if (!projectId) {
    addToast("Lỗi: Không tìm thấy ID dự án!", "error");
    router.push('/projects');
    return;
  }
  fetchTasks();
  fetchProjectMembers(); // Gọi API lấy người trong dự án
});

const goBack = () => {
  router.push('/projects');
};

const fetchTasks = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/tasks/list?projectId=${projectId}`);
    if (!res.ok) throw new Error("Lỗi fetch");
    const data = await res.json();
    
    boardTasks.value = { 'TODO': [], 'IN_PROGRESS': [], 'DONE': [] };
    data.forEach(task => {
      if (boardTasks.value[task.status]) {
        boardTasks.value[task.status].push(task);
      } else {
        boardTasks.value['TODO'].push(task);
      }
    });
  } catch (error) { 
    addToast("Lỗi khi tải bảng Kanban!", "error"); 
  }
};

// Hàm lấy thành viên dự án
const fetchProjectMembers = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/projects/members-list?projectId=${projectId}`);
    if (res.ok) projectMembers.value = await res.json();
  } catch (error) {
    console.error("Lỗi tải danh sách thành viên", error);
  }
};

const onTaskMoved = async (event, newStatusColumn) => {
  if (event.added) {
    const task = event.added.element;
    try {
      const res = await fetch("http://localhost:8080/api/tasks/update-status", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ taskId: task.id, status: newStatusColumn })
      });
      if (!res.ok) {
        addToast("Lỗi khi lưu vị trí thẻ", "error");
        fetchTasks(); 
      }
    } catch (error) { 
      addToast("Mất kết nối với máy chủ!", "error");
      fetchTasks();
    }
  }
};

const openTaskModal = (columnId) => {
  newTask.value = { title: '', description: '', priority: 'MEDIUM', deadline: '', targetColumn: columnId };
  showTaskModal.value = true;
};

const submitTask = async () => {
  if (!newTask.value.title.trim()) {
    return addToast("Tên công việc không được trống!", "warning");
  }
  try {
    const payload = { 
      projectId: projectId, 
      ...newTask.value, 
      assigneeId: 0 
    };
    const res = await fetch("http://localhost:8080/api/tasks/create", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (res.ok) { 
      addToast("Tạo thẻ thành công!", "success"); 
      showTaskModal.value = false; 
      fetchTasks(); 
    } else {
      addToast("Tạo thẻ thất bại", "error");
    }
  } catch (error) { 
    addToast("Lỗi kết nối máy chủ!", "error"); 
  }
};

// --- HÀM CHO MODAL SỬA/XÓA ---
const openEditModal = (task) => {
  editTaskData.value = { 
    id: task.id,
    title: task.title,
    description: task.description || '',
    priority: task.priority,
    deadline: task.deadline && task.deadline !== 'null' ? task.deadline : '',
    assigneeId: task.assigneeId || 0
  };
  showEditModal.value = true;
};

const submitEditTask = async () => {
  if (!editTaskData.value.title.trim()) return addToast("Tên công việc không được trống!", "warning");
  try {
    const res = await fetch("http://localhost:8080/api/tasks/update-details", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ taskId: editTaskData.value.id, ...editTaskData.value })
    });
    if (res.ok) {
      addToast("Cập nhật thành công!", "success");
      showEditModal.value = false;
      fetchTasks();
    } else { addToast("Cập nhật thất bại", "error"); }
  } catch (error) { addToast("Lỗi kết nối máy chủ!", "error"); }
};

const deleteSelectedTask = async () => {
  if (!confirm("Bạn có chắc chắn muốn xóa vĩnh viễn thẻ này?")) return;
  try {
    const res = await fetch("http://localhost:8080/api/tasks/delete", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ taskId: editTaskData.value.id })
    });
    if (res.ok) {
      addToast("Đã xóa thẻ!", "success");
      showEditModal.value = false;
      fetchTasks();
    } else { addToast("Xóa thất bại", "error"); }
  } catch (error) { addToast("Lỗi kết nối máy chủ!", "error"); }
};

// Logic Modal Mời
const showMemberModal = ref(false);
const memberSearchQuery = ref("");
const searchResults = ref([]);
const isSearching = ref(false);
const showDropdown = ref(false);
const newMemberRole = ref('MEMBER');
let searchTimeout = null;

const openMemberModal = () => {
  memberSearchQuery.value = "";
  searchResults.value = [];
  newMemberRole.value = 'MEMBER';
  showMemberModal.value = true;
};

const handleSearchInput = () => {
  clearTimeout(searchTimeout);
  const q = memberSearchQuery.value.trim();
  if (q.length < 2) {
    searchResults.value = [];
    showDropdown.value = false;
    return;
  }
  isSearching.value = true;
  searchTimeout = setTimeout(async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/users/search?q=${q}`);
      if (response.ok) {
        searchResults.value = await response.json();
        showDropdown.value = searchResults.value.length > 0;
      }
    } catch (error) {
    } finally {
      isSearching.value = false;
    }
  }, 400);
};

const selectUser = (user) => {
  memberSearchQuery.value = user.email;
  showDropdown.value = false;
};

const submitInvite = async () => {
  if (!memberSearchQuery.value) return addToast("Vui lòng nhập Username hoặc Email!", "warning");
  try {
    const currentUser = localStorage.getItem("username") || "Quản lý";
    const payload = { 
      projectId: projectId, 
      projectName: projectName.value,
      inviterName: currentUser,
      identifier: memberSearchQuery.value,
      role: newMemberRole.value 
    };
    
    const res = await fetch("http://localhost:8080/api/projects/add-member", { 
      method: 'POST', 
      headers: { 'Content-Type': 'application/json' }, 
      body: JSON.stringify(payload) 
    });
    
    const result = await res.json();
    if (res.ok) { 
      addToast(result.message || "Đã gửi lời mời thành công!", "success"); 
      showMemberModal.value = false; 
    } else { 
      addToast(result.error, "error"); 
    }
  } catch (error) { 
    addToast("Lỗi khi thêm thành viên!", "error"); 
  }
};

const formatPriority = (p) => {
  if (p === 'HIGH') return 'Cao';
  if (p === 'MEDIUM') return 'Trung bình';
  return 'Thấp';
};
const getPriorityTag = (p) => {
  if (p === 'HIGH') return 'bg-red-100 text-red-600';
  if (p === 'MEDIUM') return 'bg-orange-100 text-orange-600';
  return 'bg-slate-100 text-slate-600';
};
const getPriorityBorder = (p) => {
  if (p === 'HIGH') return 'border-l-red-500';
  if (p === 'MEDIUM') return 'border-l-orange-400';
  return 'border-l-slate-300';
};
const formatDate = (dateStr) => { 
  if (!dateStr || dateStr === 'null') return 'Chưa có hạn'; 
  const parts = dateStr.split('-'); 
  if (parts.length === 3) return `${parts[2]}/${parts[1]}`;
  return dateStr;
};
const isDeadlineNear = (dateStr) => { 
  if (!dateStr || dateStr === 'null') return false; 
  const daysLeft = (new Date(dateStr) - new Date()) / (1000 * 60 * 60 * 24); 
  return daysLeft > 0 && daysLeft <= 3; 
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
</style>