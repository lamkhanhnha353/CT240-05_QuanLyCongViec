<template>
  <div class="space-y-6 relative">
    
    <header class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-extrabold text-slate-800">Quản lý Công việc</h1>
        <p class="text-slate-500 font-medium mt-1">Theo dõi tiến độ và phân công công việc.</p>
      </div>
      <button 
        @click="showCreateModal = true" 
        class="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-lg shadow-blue-600/30 transition-all flex items-center gap-2"
      >
        <span>+ Tạo công việc</span>
      </button>
    </header>

    <div class="bg-white p-4 rounded-2xl shadow-sm border border-slate-200 flex flex-col md:flex-row gap-4 justify-between items-center">
      <div class="w-full md:w-1/2 relative">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="🔍 Tìm kiếm theo tên công việc..."
          class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
        />
      </div>

      <div class="w-full md:w-auto flex gap-3">
        <span class="py-2 text-sm font-bold text-slate-500">Sắp xếp:</span>
        <select 
          v-model="sortBy" 
          class="px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none font-medium text-slate-700 cursor-pointer"
        >
          <option value="default">Mặc định</option>
          <option value="deadline">⏳ Gần Deadline nhất</option>
          <option value="priority">🔥 Quan trọng nhất</option>
        </select>
      </div>
    </div>

    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="p-6 text-center text-slate-500" v-if="isLoading">
        Đang tải dữ liệu...
      </div>

      <table v-else class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider border-b border-slate-200">
            <th class="p-4 font-bold">Tên công việc</th>
            <th class="p-4 font-bold">Tên dự án</th>
            <th class="p-4 font-bold text-center">Tiến độ</th>
            <th class="p-4 font-bold text-center">Người thực hiện</th>
            <th class="p-4 font-bold">Chú thích</th>
            <th class="p-4 font-bold text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="task in filteredAndSortedTasks" :key="task.id" class="hover:bg-slate-50 transition-colors">
            <td class="p-4">
              <div class="font-bold text-slate-800">{{ task.title }}</div>
              <div class="text-xs font-medium mt-1" 
                   :class="task.priority === 'High' ? 'text-red-500' : (task.priority === 'Medium' ? 'text-orange-500' : 'text-blue-500')">
                Mức độ: {{ task.priority || 'Medium' }}
              </div>
            </td>
            <td class="p-4 font-medium text-slate-600">{{ task.projectName || 'Chưa phân bổ' }}</td>
            <td class="p-4 text-center">
              <span class="px-3 py-1 rounded-full text-xs font-bold"
                :class="task.progress === 'Hoàn thành' ? 'bg-green-100 text-green-700' : (task.progress === 'Đang làm' ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-600')">
                {{ task.progress || 'Chưa bắt đầu' }}
              </span>
            </td>
            <td class="p-4 text-center font-medium text-slate-700">
              <div class="flex items-center justify-center gap-2">
                <div class="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-xs font-bold text-slate-500">
                  {{ task.assignee ? task.assignee.charAt(0).toUpperCase() : '?' }}
                </div>
                {{ task.assignee || 'Chưa giao' }}
              </div>
            </td>
            <td class="p-4">
              <div class="text-sm text-slate-600 line-clamp-2">{{ task.notes || 'Không có chú thích.' }}</div>
              <div class="text-xs text-red-500 font-bold mt-1" v-if="task.deadline">⏳ Hạn: {{ task.deadline.split(' ')[0] }}</div>
            </td>
            <td class="p-4 text-center">
              <button class="text-blue-500 hover:text-blue-700 font-bold text-sm px-2">Sửa</button>
            </td>
          </tr>
          
          <tr v-if="filteredAndSortedTasks.length === 0">
            <td colspan="6" class="p-8 text-center text-slate-500 font-medium">
              Không tìm thấy công việc nào phù hợp.
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showCreateModal" class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden animate-fade-in-up">
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <h2 class="text-xl font-extrabold text-slate-800">✨ Giao việc mới</h2>
          <button @click="showCreateModal = false" class="text-slate-400 hover:text-red-500 font-bold text-xl">&times;</button>
        </div>
        
        <form @submit.prevent="handleCreateTask" class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">Tên công việc *</label>
            <input v-model="newTask.title" required type="text" class="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Nhập tên..." />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Dự án</label>
              <select v-model="newTask.projectId" class="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="">-- Chọn --</option>
                <option value="1">Website TeamWork</option>
                <option value="2">App Bán hàng</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Độ ưu tiên</label>
              <select v-model="newTask.priority" class="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="Low">Thấp</option>
                <option value="Medium">Trung bình</option>
                <option value="High">Cao (Quan trọng)</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Người thực hiện</label>
              <input v-model="newTask.assignee" type="text" class="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Tên user..." />
            </div>
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-1">Hạn chót</label>
              <input v-model="newTask.deadline" type="date" class="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>
          </div>

          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">Chú thích</label>
            <textarea v-model="newTask.notes" rows="2" class="w-full px-4 py-2 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Ghi chú thêm..."></textarea>
          </div>

          <div class="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <button type="button" @click="showCreateModal = false" class="px-5 py-2 text-slate-600 font-bold hover:bg-slate-100 rounded-xl transition-all">Hủy</button>
            <button type="submit" :disabled="isSubmitting" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-lg shadow-blue-600/30 transition-all disabled:opacity-50">
              {{ isSubmitting ? 'Đang lưu...' : 'Lưu công việc' }}
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { taskApi } from '../api/taskApi';

const tasks = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showCreateModal = ref(false); // Trạng thái Ẩn/Hiện Popup

// Biến cho Thanh Tìm kiếm và Lọc
const searchQuery = ref('');
const sortBy = ref('default'); 

// Dữ liệu form nhập liệu
const newTask = ref({
  title: '', projectId: '', priority: 'Medium', assignee: '', deadline: '', notes: ''
});

// GET Danh sách
const fetchTasks = async () => {
  isLoading.value = true;
  try {
    const data = await taskApi.getAllTasks();
    tasks.value = data;
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

// Hàm Sắp xếp và Lọc dữ liệu trên Giao diện
const filteredAndSortedTasks = computed(() => {
  let result = tasks.value;

  // 1. Lọc theo tên công việc (Search)
  if (searchQuery.value) {
    const lowerCaseQuery = searchQuery.value.toLowerCase();
    result = result.filter(task => task.title.toLowerCase().includes(lowerCaseQuery));
  }

  // 2. Sắp xếp (Sort)
  if (sortBy.value === 'deadline') {
    // Sắp xếp ngày gần nhất lên đầu
    result.sort((a, b) => {
      if (!a.deadline) return 1;
      if (!b.deadline) return -1;
      return new Date(a.deadline) - new Date(b.deadline);
    });
  } else if (sortBy.value === 'priority') {
    // Mức độ ưu tiên: High > Medium > Low
    const priorityWeight = { 'High': 3, 'Medium': 2, 'Low': 1 };
    result.sort((a, b) => {
      const weightA = priorityWeight[a.priority] || 0;
      const weightB = priorityWeight[b.priority] || 0;
      return weightB - weightA; // Giảm dần
    });
  }

  return result;
});

// POST Tạo mới
const handleCreateTask = async () => {
  isSubmitting.value = true;
  try {
    // Tạm thời chèn data giả lập để test hiển thị ngay lập tức (Nếu API chưa cập nhật cột)
    // Sau khi Backend update xong, bạn mở dòng await taskApi.createTask(newTask.value); ra nhé.
    
    // Gửi API
    await taskApi.createTask(newTask.value);
    
    // Đóng popup và xóa trắng Form
    showCreateModal.value = false;
    newTask.value = { title: '', projectId: '', priority: 'Medium', assignee: '', deadline: '', notes: '' };
    
    await fetchTasks();
  } catch (error) {
    alert("Có lỗi xảy ra khi gọi Server!");
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  fetchTasks();
});
</script>

<style scoped>
/* Hiệu ứng trượt và hiện ra cho Popup Modal */
.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out forwards;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>