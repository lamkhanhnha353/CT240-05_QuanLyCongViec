<template>
  <div class="space-y-6 relative">
    
    <header class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-extrabold text-slate-800">Quản lý Công việc</h1>
        <p class="text-slate-500 font-medium mt-1">Theo dõi tiến độ và phân công công việc.</p>
      </div>
      <button @click="openCreateModal" class="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-lg transition-all">
        + Tạo công việc
      </button>
    </header>

    <div class="bg-white p-4 rounded-2xl shadow-sm border border-slate-200 flex flex-col md:flex-row gap-4 justify-between items-center">
      <div class="w-full md:w-1/3 relative">
        <input v-model="searchQuery" type="text" placeholder="🔍 Tìm kiếm tên công việc..." class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" />
      </div>
      
      <div class="w-full md:w-auto flex flex-wrap gap-4 items-center">
        <div class="flex items-center gap-2">
          <span class="text-sm font-bold text-slate-500">Trạng thái:</span>
          <select v-model="filterStatus" class="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none font-medium cursor-pointer text-slate-700">
            <option value="ALL">Tất cả</option>
            <option value="TODO">Chưa bắt đầu</option>
            <option value="IN_PROGRESS">Đang làm</option>
            <option value="DONE">Hoàn thành</option>
            <option value="CANCEL">Đã hủy</option>
          </select>
        </div>

        <div class="flex items-center gap-2">
          <span class="text-sm font-bold text-slate-500">Sắp xếp:</span>
          <select v-model="sortBy" class="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none font-medium cursor-pointer text-slate-700">
            <option value="default">Mặc định</option>
            <option value="deadline">⏳ Gần Deadline nhất</option>
            <option value="priority">🔥 Quan trọng nhất</option>
          </select>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="p-6 text-center text-slate-500" v-if="isLoading">Đang tải dữ liệu...</div>
      
      <table v-else class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider border-b border-slate-200">
            <th class="p-4 font-bold">Tên công việc</th>
            <th class="p-4 font-bold">Dự án</th>
            <th class="p-4 font-bold text-center">Tình trạng</th>
            <th class="p-4 font-bold text-center">Hạn chót</th>
            <th class="p-4 font-bold">Người thực hiện</th>
            <th class="p-4 font-bold w-48">Chú thích</th>
            <th class="p-4 font-bold text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="task in paginatedTasks" :key="task.id" class="hover:bg-slate-50 transition-colors">
            
            <td class="p-4">
              <div class="font-bold text-slate-800">{{ task.title }}</div>
              <div class="text-xs font-medium mt-1" :class="task.priority === 'HIGH' ? 'text-red-500' : (task.priority === 'MEDIUM' ? 'text-orange-500' : 'text-blue-500')">
                Mức độ: {{ task.priority || 'MEDIUM' }}
              </div>
            </td>

            <td class="p-4 font-medium text-slate-600">
              <span class="bg-indigo-50 text-indigo-700 px-2 py-1 rounded text-xs border border-indigo-100 font-bold" v-if="task.projectId">
                📁 {{ getProject(task.projectId).name }}
              </span>
            </td>
            
            <td class="p-4 text-center">
              <span class="px-3 py-1 rounded-full text-xs font-bold"
                :class="{
                  'bg-slate-100 text-slate-600': task.progress === 'TODO',
                  'bg-blue-100 text-blue-700': task.progress === 'IN_PROGRESS',
                  'bg-green-100 text-green-700': task.progress === 'DONE',
                  'bg-red-100 text-red-700': task.progress === 'CANCEL'
                }">
                {{ formatStatus(task.progress) }}
              </span>
            </td>

            <td class="p-4 text-center">
              <div class="text-red-500 font-bold text-sm" v-if="task.deadline">
                ⏳ {{ task.deadline.split(' ')[0] }}
              </div>
              <span v-else class="text-slate-400">---</span>
            </td>

            <td class="p-4">
              <div class="flex items-center gap-2" v-if="task.assignee">
                <div :class="['w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shadow-sm', getUser(task.assignee).color]">
                  {{ getUser(task.assignee).name.charAt(0) }}
                </div>
                <span class="font-medium text-slate-700">{{ getUser(task.assignee).name }}</span>
              </div>
              <div v-else class="text-slate-400 italic text-sm">Chưa giao</div>
            </td>

            <td class="p-4">
              <div class="text-sm text-slate-600 line-clamp-2" :title="task.notes">
                {{ task.notes || '---' }}
              </div>
            </td>

            <td class="p-4 text-center">
              <button @click="openEditModal(task)" class="text-blue-500 hover:text-blue-700 font-bold text-sm px-2 mr-2">Sửa</button>
              <button @click="handleDeleteTask(task.id)" class="text-red-500 hover:text-red-700 font-bold text-sm px-2">Xóa</button>
            </td>
          </tr>
          <tr v-if="paginatedTasks.length === 0">
            <td colspan="7" class="p-8 text-center text-slate-500 font-medium">Không tìm thấy công việc nào.</td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="p-4 border-t border-slate-200 bg-slate-50 flex justify-end items-center gap-1 text-sm font-bold text-slate-600">
        <button v-for="page in totalPages" :key="page" @click="goToPage(page)" :class="['px-3 py-1.5 rounded-lg hover:bg-slate-200 transition-colors', currentPage === page ? 'bg-blue-600 text-white hover:bg-blue-700' : '']">{{ page }}</button>
        <button @click="nextPage" :disabled="currentPage === totalPages" class="px-3 py-1.5 ml-2 rounded-lg hover:bg-slate-200 disabled:opacity-40 transition-colors">Trang sau ›</button>
        <button @click="goToLastPage" :disabled="currentPage === totalPages" class="px-3 py-1.5 rounded-lg hover:bg-slate-200 disabled:opacity-40 transition-colors">Trang cuối »</button>
      </div>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden animate-fade-in-up">
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <h2 class="text-xl font-extrabold text-slate-800">{{ isEditing ? '✏️ Sửa công việc' : '✨ Giao việc mới' }}</h2>
          <button @click="showModal = false" class="text-slate-400 hover:text-red-500 font-bold text-xl">&times;</button>
        </div>
        
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          
          <div class="grid grid-cols-2 gap-4">
            <div class="col-span-2">
              <label class="block text-sm font-bold text-slate-700 mb-1">Tên công việc *</label>
              <input v-model="formTask.title" required type="text" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Nhập tên công việc..." />
            </div>

            <div class="col-span-2 sm:col-span-1">
              <label class="block text-sm font-bold text-slate-700 mb-1">Dự án *</label>
              <select v-model="formTask.projectId" required class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="" disabled>-- Chọn dự án --</option>
                <option v-for="project in mockProjects" :key="project.id" :value="project.id">{{ project.name }}</option>
              </select>
            </div>

            <div class="col-span-2 sm:col-span-1">
              <label class="block text-sm font-bold text-slate-700 mb-1">Tình trạng</label>
              <select v-model="formTask.progress" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="TODO">Chưa bắt đầu</option>
                <option value="IN_PROGRESS">Đang làm</option>
                <option value="DONE">Hoàn thành</option>
                <option value="CANCEL">Đã hủy</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4 border-t border-slate-100 pt-3">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Người thực hiện</label>
              <select v-model="formTask.assignee" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="">-- Trống --</option>
                <option v-for="user in mockUsers" :key="user.id" :value="user.id">{{ user.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Hạn chót</label>
              <input v-model="formTask.deadline" type="date" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>
          </div>

          <div class="border-t border-slate-100 pt-3">
            <label class="block text-sm font-bold text-slate-700 mb-1">Độ ưu tiên</label>
            <select v-model="formTask.priority" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
              <option value="LOW">Thấp</option>
              <option value="MEDIUM">Trung bình</option>
              <option value="HIGH">Cao</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">Chú thích</label>
            <textarea v-model="formTask.notes" rows="2" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Ghi chú thêm..."></textarea>
          </div>

          <div class="pt-4 flex justify-end gap-3 border-t">
            <button type="button" @click="showModal = false" class="px-5 py-2 text-slate-600 font-bold hover:bg-slate-100 rounded-xl transition-all">Hủy</button>
            <button type="submit" :disabled="isSubmitting" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-lg transition-all">
              {{ isSubmitting ? 'Đang lưu...' : 'Lưu công việc' }}
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { taskApi } from '../api/taskApi';

// ==========================================
// 1. DỮ LIỆU TỪ ĐIỂN (USER & PROJECT)
// ==========================================

const mockProjects = ref([
  { id: 1, name: 'Website TeamWork' },
  { id: 2, name: 'App Bán hàng' },
  { id: 3, name: 'Hệ thống Quản lý Sinh viên' }
]);

const getProject = (id) => {
  return mockProjects.value.find(p => p.id === id) || { name: 'Không xác định' };
};

const mockUsers = ref([
  { id: 1, name: 'Minh Khang', color: 'bg-indigo-100 text-indigo-700' },
  { id: 2, name: 'Thu Thảo', color: 'bg-pink-100 text-pink-700' },
  { id: 3, name: 'Hoàng Long', color: 'bg-emerald-100 text-emerald-700' },
  { id: 4, name: 'Admin', color: 'bg-amber-100 text-amber-700' }
]);

const getUser = (id) => {
  return mockUsers.value.find(u => u.id === id) || { name: 'Unknown', color: 'bg-slate-100 text-slate-500' };
};

// ==========================================
// 2. KHAI BÁO BIẾN DỮ LIỆU
// ==========================================
const tasks = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);

const showModal = ref(false);
const isEditing = ref(false);
const editId = ref(null);

const searchQuery = ref('');
const sortBy = ref('default'); 
const filterStatus = ref('ALL'); 

// Khớp với DB cũ: Không có startDate, không có assigner
const formTask = ref({ title: '', projectId: '', priority: 'MEDIUM', progress: 'TODO', assignee: '', deadline: '', notes: '' });

const formatStatus = (status) => {
  const map = { 'TODO': 'Chưa bắt đầu', 'IN_PROGRESS': 'Đang làm', 'DONE': 'Hoàn thành', 'CANCEL': 'Đã hủy' };
  return map[status] || status;
};

// ==========================================
// 3. HÀM GỌI API (GET DATABASE)
// ==========================================
const fetchTasks = async () => {
  isLoading.value = true;
  try { 
    tasks.value = await taskApi.getAllTasks(); 
  } catch (error) { 
    console.error("Lỗi khi tải dữ liệu:", error); 
  } finally { 
    isLoading.value = false; 
  }
};

// ==========================================
// 4. LOGIC LỌC, SẮP XẾP & PHÂN TRANG
// ==========================================
const currentPage = ref(1);
const tasksPerPage = 5;

watch([searchQuery, filterStatus, sortBy], () => { currentPage.value = 1; });

const filteredAndSortedTasks = computed(() => {
  let result = tasks.value;
  if (searchQuery.value) result = result.filter(t => t.title.toLowerCase().includes(searchQuery.value.toLowerCase()));
  if (filterStatus.value !== 'ALL') result = result.filter(t => t.progress === filterStatus.value);
  
  if (sortBy.value === 'deadline') {
    result.sort((a, b) => (!a.deadline ? 1 : !b.deadline ? -1 : new Date(a.deadline) - new Date(b.deadline)));
  } else if (sortBy.value === 'priority') {
    const w = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 };
    result.sort((a, b) => (w[b.priority] || 0) - (w[a.priority] || 0));
  }
  return result;
});

const totalPages = computed(() => Math.ceil(filteredAndSortedTasks.value.length / tasksPerPage));
const paginatedTasks = computed(() => {
  const startIndex = (currentPage.value - 1) * tasksPerPage;
  return filteredAndSortedTasks.value.slice(startIndex, startIndex + tasksPerPage);
});

const goToPage = (page) => { currentPage.value = page; };
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++; };
const goToLastPage = () => { currentPage.value = totalPages.value; };

// ==========================================
// 5. CÁC HÀM XỬ LÝ NÚT BẤM
// ==========================================
const openCreateModal = () => {
  isEditing.value = false;
  formTask.value = { title: '', projectId: '', priority: 'MEDIUM', progress: 'TODO', assignee: '', deadline: '', notes: '' };
  showModal.value = true;
};

const openEditModal = (task) => {
  isEditing.value = true;
  editId.value = task.id;
  formTask.value = { 
    ...task, 
    deadline: task.deadline ? task.deadline.split(' ')[0] : '' 
  };
  showModal.value = true;
};

const handleSubmit = async () => {
  isSubmitting.value = true;
  try {
    if (isEditing.value) await taskApi.updateTask(editId.value, formTask.value);
    else await taskApi.createTask(formTask.value);
    
    showModal.value = false;
    currentPage.value = 1; 
    await fetchTasks(); 
  } catch (error) { 
    alert("Có lỗi xảy ra khi gọi Server!"); 
  } finally { 
    isSubmitting.value = false; 
  }
};

const handleDeleteTask = async (id) => {
  if (confirm("⚠️ Bạn có chắc chắn muốn xóa công việc này không?")) {
    try {
      await taskApi.deleteTask(id);
      if (paginatedTasks.value.length === 1 && currentPage.value > 1) currentPage.value--;
      await fetchTasks();
    } catch (error) { 
      alert("❌ Có lỗi xảy ra khi xóa!"); 
    }
  }
};

onMounted(() => {
  fetchTasks();
});
</script>

<style scoped>
.animate-fade-in-up { animation: fadeInUp 0.3s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>