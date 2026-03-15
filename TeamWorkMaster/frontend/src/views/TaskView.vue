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
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider border-b border-slate-200">
            <th class="p-4 font-bold">Tên công việc</th>
            <th class="p-4 font-bold text-center">Tình trạng</th>
            <th class="p-4 font-bold text-center">Ngày bắt đầu / Hết hạn</th>
            <th class="p-4 font-bold">Người giao</th>
            <th class="p-4 font-bold">Người thực hiện</th>
            <th class="p-4 font-bold w-48">Chú thích</th>
            <th class="p-4 font-bold text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="task in paginatedTasks" :key="task.id" class="hover:bg-slate-50 transition-colors">
            <td class="p-4">
              <div class="font-bold text-slate-800">{{ task.title }}</div>
              <div class="text-xs font-medium mt-1" :class="task.priority === 'High' ? 'text-red-500' : (task.priority === 'Medium' ? 'text-orange-500' : 'text-blue-500')">
                Mức độ: {{ task.priority || 'Medium' }}
              </div>
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

            <td class="p-4 text-sm">
              <div class="flex flex-col gap-1 items-center">
                <div class="text-slate-600">
                  <span class="font-medium">Bắt đầu:</span> {{ task.startDate ? task.startDate.split(' ')[0] : '---' }}
                </div>
                <div class="text-red-500 font-medium">
                  <span class="font-bold">Hạn chót:</span> ⏳ {{ task.deadline ? task.deadline.split(' ')[0] : '---' }}
                </div>
              </div>
            </td>

            <td class="p-4">
              <div class="flex items-center gap-2" v-if="task.assigner">
                <div :class="['w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shadow-sm', getUser(task.assigner).color]">
                  {{ getUser(task.assigner).name.charAt(0) }}
                </div>
                <span class="font-medium text-slate-700">{{ getUser(task.assigner).name }}</span>
              </div>
              <div v-else class="text-slate-400 italic text-sm text-center">---</div>
            </td>

            <td class="p-4">
              <div class="flex items-center gap-2" v-if="task.assignee">
                <div :class="['w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shadow-sm', getUser(task.assignee).color]">
                  {{ getUser(task.assignee).name.charAt(0) }}
                </div>
                <span class="font-medium text-slate-700">{{ getUser(task.assignee).name }}</span>
              </div>
              <div v-else class="text-slate-400 italic text-sm text-center">Chưa giao</div>
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
            <td colspan="7" class="p-8 text-center text-slate-500 font-medium">Không tìm thấy công việc nào phù hợp với bộ lọc.</td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="p-4 border-t border-slate-200 bg-slate-50 flex justify-end items-center gap-1 text-sm font-bold text-slate-600">
        <button 
          v-for="page in totalPages" 
          :key="page"
          @click="goToPage(page)"
          :class="['px-3 py-1.5 rounded-lg hover:bg-slate-200 transition-colors', currentPage === page ? 'bg-blue-600 text-white hover:bg-blue-700' : '']"
        >
          {{ page }}
        </button>

        <button 
          @click="nextPage" 
          :disabled="currentPage === totalPages"
          class="px-3 py-1.5 ml-2 rounded-lg hover:bg-slate-200 disabled:opacity-40 disabled:hover:bg-transparent transition-colors"
        >
          Trang sau ›
        </button>

        <button 
          @click="goToLastPage" 
          :disabled="currentPage === totalPages"
          class="px-3 py-1.5 rounded-lg hover:bg-slate-200 disabled:opacity-40 disabled:hover:bg-transparent transition-colors"
        >
          Trang cuối »
        </button>
      </div>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden animate-fade-in-up">
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <h2 class="text-xl font-extrabold text-slate-800">{{ isEditing ? '✏️ Sửa công việc' : '✨ Giao việc mới' }}</h2>
          <button @click="showModal = false" class="text-slate-400 hover:text-red-500 font-bold text-xl">&times;</button>
        </div>
        
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">Tên công việc *</label>
            <input v-model="formTask.title" required type="text" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Nhập tên công việc..." />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Tình trạng</label>
              <select v-model="formTask.progress" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="TODO">Chưa bắt đầu</option>
                <option value="IN_PROGRESS">Đang làm</option>
                <option value="DONE">Hoàn thành</option>
                <option value="CANCEL">Đã hủy</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Độ ưu tiên</label>
              <select v-model="formTask.priority" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="Low">Thấp</option>
                <option value="Medium">Trung bình</option>
                <option value="High">Cao</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Người giao</label>
              <select v-model="formTask.assigner" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="">-- Trống --</option>
                <option v-for="user in mockUsers" :key="user.id" :value="user.id">{{ user.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Người thực hiện</label>
              <select v-model="formTask.assignee" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="">-- Chọn thành viên --</option>
                <option v-for="user in mockUsers" :key="user.id" :value="user.id">{{ user.name }}</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Ngày bắt đầu</label>
              <input v-model="formTask.startDate" type="date" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Hạn chót</label>
              <input v-model="formTask.deadline" type="date" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>
          </div>

          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">Chú thích</label>
            <textarea v-model="formTask.notes" rows="2" class="w-full px-4 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Ghi chú thêm..."></textarea>
          </div>

          <div class="pt-4 flex justify-end gap-3 border-t">
            <button type="button" @click="showModal = false" class="px-5 py-2 text-slate-600 font-bold hover:bg-slate-100 rounded-xl transition-all">Hủy</button>
            <button type="submit" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-lg transition-all">
              Lưu công việc
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

// ==========================================
// 1. DỮ LIỆU GIẢ VÀ HỆ THỐNG USER
// ==========================================

const mockUsers = ref([
  { id: 1, name: 'Minh Khang', color: 'bg-indigo-100 text-indigo-700' },
  { id: 2, name: 'Thu Thảo', color: 'bg-pink-100 text-pink-700' },
  { id: 3, name: 'Hoàng Long', color: 'bg-emerald-100 text-emerald-700' },
  { id: 4, name: 'Admin', color: 'bg-amber-100 text-amber-700' }
]);

const getUser = (id) => {
  return mockUsers.value.find(u => u.id === id) || { name: 'Unknown', color: 'bg-slate-100 text-slate-500' };
};

// Đã thêm nhiều Task mẫu để vượt quá 5, giúp thấy ngay thanh phân trang
const tasks = ref([
  { id: 1, title: 'Thiết kế Database MySQL', progress: 'DONE', priority: 'High', assigner: 4, assignee: 1, startDate: '2026-03-01', deadline: '2026-03-10', notes: 'Thiết kế có ENUM' },
  { id: 2, title: 'Code giao diện TaskView', progress: 'IN_PROGRESS', priority: 'High', assigner: 1, assignee: 2, startDate: '2026-03-12', deadline: '2026-03-18', notes: 'Dùng Tailwind CSS. Chú ý responsive trên mobile nhé!' },
  { id: 3, title: 'Viết API Đăng nhập', progress: 'TODO', priority: 'Medium', assigner: 4, assignee: 3, startDate: '', deadline: '2026-03-25', notes: 'Nhớ hash password' },
  { id: 4, title: 'Họp chốt ý tưởng với Giảng viên', progress: 'CANCEL', priority: 'Low', assigner: 1, assignee: '', startDate: '2026-03-15', deadline: '2026-03-15', notes: 'Giảng viên bận' },
  { id: 5, title: 'Tìm hiểu thư viện Axios', progress: 'DONE', priority: 'Medium', assigner: 2, assignee: 1, startDate: '2026-03-05', deadline: '2026-03-07', notes: 'Để gọi API' },
  { id: 6, title: 'Sửa lỗi giao diện Responsive', progress: 'IN_PROGRESS', priority: 'High', assigner: 1, assignee: 2, startDate: '2026-03-16', deadline: '2026-03-20', notes: 'Lỗi trên màn hình điện thoại' },
  { id: 7, title: 'Viết báo cáo đồ án', progress: 'TODO', priority: 'High', assigner: 4, assignee: 1, startDate: '2026-04-01', deadline: '2026-04-15', notes: 'Gồm file Word và Slide' }
]);

// ==========================================
// 2. TRẠNG THÁI GIAO DIỆN & LỌC
// ==========================================
const showModal = ref(false);
const isEditing = ref(false);
const editId = ref(null);

const searchQuery = ref('');
const sortBy = ref('default'); 
const filterStatus = ref('ALL'); 

const formTask = ref({ title: '', projectId: '', priority: 'Medium', progress: 'TODO', assigner: '', assignee: '', startDate: '', deadline: '', notes: '' });

const formatStatus = (status) => {
  const map = { 'TODO': 'Chưa bắt đầu', 'IN_PROGRESS': 'Đang làm', 'DONE': 'Hoàn thành', 'CANCEL': 'Đã hủy' };
  return map[status] || status;
};

// ==========================================
// 3. LOGIC LỌC, SẮP XẾP & PHÂN TRANG (MỚI)
// ==========================================

// Biến cho Phân trang
const currentPage = ref(1);
const tasksPerPage = 5; // <--- Cài đặt đúng 5 task 1 trang như yêu cầu

// Reset về trang 1 mỗi khi người dùng Gõ tìm kiếm hoặc Chọn bộ lọc mới
watch([searchQuery, filterStatus, sortBy], () => {
  currentPage.value = 1;
});

// 3.1. Danh sách đã Lọc & Sắp xếp (Dành cho tính toán tổng trang)
const filteredAndSortedTasks = computed(() => {
  let result = tasks.value;
  
  if (searchQuery.value) {
    result = result.filter(t => t.title.toLowerCase().includes(searchQuery.value.toLowerCase()));
  }

  if (filterStatus.value !== 'ALL') {
    result = result.filter(t => t.progress === filterStatus.value);
  }
  
  if (sortBy.value === 'deadline') {
    result.sort((a, b) => (!a.deadline ? 1 : !b.deadline ? -1 : new Date(a.deadline) - new Date(b.deadline)));
  } else if (sortBy.value === 'priority') {
    const w = { 'High': 3, 'Medium': 2, 'Low': 1 };
    result.sort((a, b) => (w[b.priority] || 0) - (w[a.priority] || 0));
  }
  return result;
});

// 3.2. Tính tổng số trang
const totalPages = computed(() => {
  return Math.ceil(filteredAndSortedTasks.value.length / tasksPerPage);
});

// 3.3. Cắt mảng để lấy ĐÚNG 5 task cho Trang hiện tại (Cái này render ra Table)
const paginatedTasks = computed(() => {
  const startIndex = (currentPage.value - 1) * tasksPerPage;
  const endIndex = startIndex + tasksPerPage;
  return filteredAndSortedTasks.value.slice(startIndex, endIndex);
});

// CÁC HÀM CHUYỂN TRANG
const goToPage = (page) => {
  currentPage.value = page;
};
const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
const goToLastPage = () => {
  currentPage.value = totalPages.value;
};

// ==========================================
// 4. CÁC HÀM XỬ LÝ NÚT BẤM (Local)
// ==========================================
const openCreateModal = () => {
  isEditing.value = false;
  formTask.value = { title: '', projectId: '', priority: 'Medium', progress: 'TODO', assigner: '', assignee: '', startDate: '', deadline: '', notes: '' };
  showModal.value = true;
};

const openEditModal = (task) => {
  isEditing.value = true;
  editId.value = task.id;
  formTask.value = { ...task }; 
  showModal.value = true;
};

const handleSubmit = () => {
  if (isEditing.value) {
    const index = tasks.value.findIndex(t => t.id === editId.value);
    if (index !== -1) tasks.value[index] = { ...formTask.value, id: editId.value };
  } else {
    const newTask = { ...formTask.value, id: Date.now() };
    tasks.value.unshift(newTask); // Thêm task mới vào đầu mảng
  }
  showModal.value = false;
  currentPage.value = 1; // Tạo xong thì chuyển về trang 1 để thấy luôn
};

const handleDeleteTask = (id) => {
  if (confirm("⚠️ Bạn có chắc chắn muốn xóa công việc này không?")) {
    tasks.value = tasks.value.filter(t => t.id !== id);
    // Nếu xóa task cuối cùng của trang, tự lùi về trang trước
    if (paginatedTasks.value.length === 0 && currentPage.value > 1) {
      currentPage.value--;
    }
  }
};
</script>

<style scoped>
.animate-fade-in-up { animation: fadeInUp 0.3s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>