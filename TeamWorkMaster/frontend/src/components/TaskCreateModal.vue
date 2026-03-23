<template>
  <div class="fixed inset-0 bg-slate-900/60 dark:bg-black/80 backdrop-blur-sm flex items-center justify-center z-50 p-4 overflow-y-auto">
    <div class="bg-white dark:bg-slate-800 w-full max-w-4xl rounded-3xl shadow-2xl relative animate-fade-in text-slate-800 dark:text-slate-200 flex flex-col my-auto border border-slate-200 dark:border-slate-700">
      
      <div class="flex justify-between items-center px-8 py-5 border-b border-slate-100 dark:border-slate-700 shrink-0 bg-slate-50 dark:bg-slate-800/50 rounded-t-3xl">
        <h2 class="text-2xl font-black text-slate-800 dark:text-white flex items-center">
          <span class="mr-3 text-3xl">✨</span> Khởi tạo Công việc mới
        </h2>
        <button @click="$emit('close')" class="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/20 rounded-xl transition-all">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>

      <div class="p-8 flex flex-col md:flex-row gap-8 overflow-y-auto custom-scrollbar">
        
        <div class="flex-1 space-y-6">
          <div>
            <label class="block text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2">Tên công việc <span class="text-red-500">*</span></label>
            <input v-model="taskData.title" type="text" placeholder="Nhập tiêu đề công việc..." class="w-full px-5 py-3.5 rounded-xl border border-slate-200 dark:border-slate-700 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none text-base font-bold bg-slate-50 dark:bg-slate-900 dark:text-white transition-all" />
          </div>
          
          <div>
            <label class="block text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2">Mô tả chi tiết</label>
            <textarea v-model="taskData.description" rows="8" placeholder="Viết chi tiết yêu cầu, các bước thực hiện..." class="w-full px-5 py-4 rounded-xl border border-slate-200 dark:border-slate-700 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none text-sm bg-slate-50 dark:bg-slate-900 dark:text-white custom-scrollbar resize-none transition-all"></textarea>
          </div>

          <div>
            <label class="block text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2 flex items-center">
              Nhãn (Tags) 
              <span class="ml-2 normal-case font-medium text-slate-400 text-[10px]">(Cách nhau bằng dấu phẩy)</span>
            </label>
            <input v-model="taskData.tags" type="text" placeholder="VD: Giao diện, Lỗi, Khẩn cấp..." class="w-full px-5 py-3 rounded-xl border border-slate-200 dark:border-slate-700 focus:border-blue-500 outline-none text-sm font-medium bg-slate-50 dark:bg-slate-900 dark:text-white" />
          </div>
        </div>

        <div class="w-full md:w-[320px] shrink-0 flex flex-col gap-6 p-6 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-700">
          
          <div>
            <label class="block text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2">🧑‍🤝‍🧑 Người thực hiện</label>
            
            <div v-if="taskData.assigneeIds.length > 0" class="flex flex-wrap gap-2 mb-3">
              <div v-for="id in taskData.assigneeIds" :key="id" class="px-3 py-1.5 bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 text-xs font-bold rounded-lg flex items-center shadow-sm">
                {{ getMemberName(id) }}
                <button @click="removeAssignee(id)" class="ml-2 text-blue-500 hover:text-red-500 transition-colors">✕</button>
              </div>
            </div>

            <select @change="addAssignee" class="w-full px-4 py-3.5 rounded-xl border border-slate-200 dark:border-slate-600 focus:border-blue-500 outline-none text-sm font-bold bg-white dark:bg-slate-900 dark:text-white appearance-none cursor-pointer shadow-sm">
              <option value="">+ Thêm người thực hiện...</option>
              <option v-for="user in unassignedMembers" :key="user.id" :value="user.id">{{ user.fullName }}</option>
            </select>
          </div>

          <div class="h-px w-full bg-slate-200 dark:bg-slate-700 my-1"></div>

          <div>
            <label class="block text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2">🔥 Độ ưu tiên</label>
            <div class="flex bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-600 overflow-hidden p-1 shadow-sm">
              <button @click="taskData.priority = 'LOW'" :class="taskData.priority === 'LOW' ? 'bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-white shadow-sm' : 'text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800'" class="flex-1 py-2.5 text-xs font-bold rounded-lg transition-all">Thấp</button>
              <button @click="taskData.priority = 'MEDIUM'" :class="taskData.priority === 'MEDIUM' ? 'bg-orange-100 text-orange-600 dark:bg-orange-900/50 dark:text-orange-400 shadow-sm' : 'text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800'" class="flex-1 py-2.5 text-xs font-bold rounded-lg transition-all">Vừa</button>
              <button @click="taskData.priority = 'HIGH'" :class="taskData.priority === 'HIGH' ? 'bg-red-100 text-red-600 dark:bg-red-900/50 dark:text-red-400 shadow-sm' : 'text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800'" class="flex-1 py-2.5 text-xs font-bold rounded-lg transition-all">Cao</button>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2">Bắt đầu</label>
              <input v-model="taskData.startDate" type="date" class="w-full px-3 py-3 rounded-xl border border-slate-200 dark:border-slate-600 text-sm font-medium bg-white dark:bg-slate-900 dark:text-white shadow-sm dark:[color-scheme:dark]" />
            </div>
            <div>
              <label class="block text-xs font-black text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2">Hạn chót</label>
              <input v-model="taskData.deadline" type="date" :min="taskData.startDate" class="w-full px-3 py-3 rounded-xl border border-slate-200 dark:border-slate-600 text-sm font-medium bg-white dark:bg-slate-900 dark:text-white shadow-sm dark:[color-scheme:dark]" />
            </div>
          </div>

        </div>
      </div>

      <div class="px-8 py-5 border-t border-slate-100 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 rounded-b-3xl flex justify-end space-x-3 shrink-0">
        <button @click="$emit('close')" class="px-6 py-3 bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-200 font-bold rounded-xl hover:bg-slate-50 dark:hover:bg-slate-600 transition-all shadow-sm">
          Hủy bỏ
        </button>
        <button @click="submit" :disabled="isSubmitting" class="px-8 py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-lg shadow-blue-500/30 transition-all flex items-center disabled:opacity-50 disabled:cursor-not-allowed">
          <span v-if="isSubmitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
          <span v-else class="mr-2 text-lg leading-none">+</span> 
          Tạo công việc
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  projectId: { type: String, required: true },
  projectMembers: { type: Array, required: true }
});

const emit = defineEmits(['close', 'created']);

const isSubmitting = ref(false);

const taskData = ref({
  title: '',
  description: '',
  priority: 'MEDIUM',
  startDate: '',
  deadline: '',
  tags: '',
  targetColumn: 'TODO', // Luôn mặc định là TODO
  assigneeIds: [] // 🟢 Đổi thành mảng để lưu nhiều ID
});

// --- LOGIC XỬ LÝ NHIỀU NGƯỜI THỰC HIỆN ---
// Lọc ra những người CHƯA được chọn để đưa vào dropdown
const unassignedMembers = computed(() => {
  return props.projectMembers.filter(member => !taskData.value.assigneeIds.includes(member.id));
});

// Thêm người
const addAssignee = (event) => {
  const selectedId = parseInt(event.target.value);
  if (selectedId && !taskData.value.assigneeIds.includes(selectedId)) {
    taskData.value.assigneeIds.push(selectedId);
  }
  event.target.value = ""; // Reset dropdown về "+ Thêm người"
};

// Xóa người
const removeAssignee = (idToRemove) => {
  taskData.value.assigneeIds = taskData.value.assigneeIds.filter(id => id !== idToRemove);
};

// Lấy tên để hiển thị trên chip
const getMemberName = (id) => {
  const user = props.projectMembers.find(m => m.id === id);
  return user ? user.fullName : 'Unknown';
};

const submit = async () => {
  if (!taskData.value.title.trim()) {
    alert("Vui lòng nhập tên công việc!");
    return;
  }
  
  // 🟢 KIỂM TRA RÀNG BUỘC NGÀY THÁNG TRƯỚC KHI GỬI 🟢
  if (taskData.value.startDate && taskData.value.deadline) {
    const start = new Date(taskData.value.startDate);
    const end = new Date(taskData.value.deadline);
    if (end < start) {
      alert("Lỗi: Hạn chót không thể diễn ra trước Ngày bắt đầu!");
      return;
    }
  }

  isSubmitting.value = true;
  try {
    const res = await fetch("http://localhost:8080/api/tasks/create", {
      method: 'POST', 
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        projectId: props.projectId, 
        title: taskData.value.title,
        description: taskData.value.description,
        priority: taskData.value.priority,
        startDate: taskData.value.startDate,
        deadline: taskData.value.deadline,
        tags: taskData.value.tags,
        targetColumn: taskData.value.targetColumn,
        // 🟢 ĐÃ CẬP NHẬT: Biến mảng [1, 2] thành chuỗi "1,2" cho Java dễ đọc
        assigneeIds: taskData.value.assigneeIds.join(',') 
      })
    });
    
    if (res.ok) {
      emit('created'); 
    } else {
      alert("Lỗi tạo công việc từ Server");
    }
  } catch (error) {
    alert("Lỗi kết nối máy chủ!");
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #475569; }
</style>