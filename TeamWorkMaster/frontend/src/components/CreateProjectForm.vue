<template>
  <div class="flex flex-col md:flex-row bg-[#1e293b] rounded-3xl shadow-2xl overflow-hidden mb-8 border border-slate-700 animate-fade-in">
    
    <div class="w-full md:w-1/3 bg-[#0f172a] p-10 flex flex-col justify-between border-r border-slate-800">
      <div>
        <p class="text-xs font-black text-blue-500 uppercase tracking-[0.2em] mb-3">
          {{ isEdit ? 'THAO TÁC' : 'KHỞI TẠO' }}
        </p>
        <h2 class="text-4xl text-white font-bold leading-tight mb-6">
          {{ isEdit ? 'Cập nhật' : 'Dự án' }} <br/>
          {{ isEdit ? 'dự án' : 'mới' }}
        </h2>
        <p class="text-slate-400 text-sm leading-relaxed">
          Thiết lập không gian làm việc tối ưu và bắt đầu thực hiện các mục tiêu chiến lược của bạn ngay hôm nay.
        </p>
      </div>

      <div class="mt-12">
        <div class="flex items-center space-x-3 mb-4">
          <div class="w-8 h-8 rounded-lg bg-slate-800 flex items-center justify-center text-slate-300">
            ✨
          </div>
          <span class="text-sm font-semibold text-slate-300">Gợi ý từ Hệ thống</span>
        </div>
        <div class="bg-[#1e293b]/50 border border-slate-700/50 p-4 rounded-2xl">
          <p class="text-xs text-slate-400 leading-relaxed">
            Hệ thống sẽ tự động phân bổ tài nguyên và đưa ra cảnh báo dựa trên mức độ ưu tiên mà bạn chọn cho dự án này.
          </p>
        </div>
      </div>
    </div>

    <div class="w-full md:w-2/3 p-10">
      <form @submit.prevent="submitProject" class="space-y-6">
        
        <div>
          <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
            Tên dự án <span class="text-red-500">*</span>
          </label>
          <input 
            v-model="project.name" 
            type="text" 
            required 
            placeholder="Nhập tên dự án tại đây..." 
            class="w-full px-5 py-3.5 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-all font-medium" 
          />
        </div>

        <div>
          <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
            Mức độ ưu tiên
          </label>
          <div class="flex space-x-3">
            <button 
              type="button" 
              @click="project.priority = 'LOW'" 
              :class="project.priority === 'LOW' ? 'bg-blue-600 text-white border-blue-600' : 'bg-slate-900/50 text-slate-400 border-slate-700 hover:bg-slate-700'" 
              class="px-6 py-2.5 rounded-lg border text-sm font-bold transition-all"
            >
              Thấp
            </button>
            <button 
              type="button" 
              @click="project.priority = 'MEDIUM'" 
              :class="project.priority === 'MEDIUM' ? 'bg-blue-600 text-white border-blue-600' : 'bg-slate-900/50 text-slate-400 border-slate-700 hover:bg-slate-700'" 
              class="px-6 py-2.5 rounded-lg border text-sm font-bold transition-all"
            >
              Trung bình
            </button>
            <button 
              type="button" 
              @click="project.priority = 'HIGH'" 
              :class="project.priority === 'HIGH' ? 'bg-blue-600 text-white border-blue-600' : 'bg-slate-900/50 text-slate-400 border-slate-700 hover:bg-slate-700'" 
              class="px-6 py-2.5 rounded-lg border text-sm font-bold transition-all"
            >
              Cao
            </button>
          </div>
        </div>

        <div>
          <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
            Mô tả mục tiêu
          </label>
          <textarea 
            v-model="project.description" 
            rows="3" 
            placeholder="Mục tiêu chính của dự án này là gì?" 
            class="w-full px-5 py-3.5 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-all font-medium"
          ></textarea>
        </div>

        <div class="grid grid-cols-2 gap-6">
          <div>
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
              Ngày bắt đầu <span class="text-red-500">*</span>
            </label>
            <input 
              v-model="project.startDate" 
              type="date" 
              required 
              :disabled="isEdit" 
              class="w-full px-5 py-3.5 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-all disabled:opacity-50 [color-scheme:dark] cursor-pointer" 
            />
          </div>
          <div>
            <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
              Hạn chót <span class="text-red-500">*</span>
            </label>
            <input 
              v-model="project.endDate" 
              type="date" 
              required 
              class="w-full px-5 py-3.5 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-all [color-scheme:dark] cursor-pointer" 
            />
          </div>
        </div>

        <div v-if="isEdit">
          <label class="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
            Trạng thái Dự án
          </label>
          <select 
            v-model="project.status" 
            class="w-full px-5 py-3.5 rounded-xl bg-slate-900/50 border border-slate-700 text-white focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-all [color-scheme:dark] cursor-pointer"
          >
            <option value="PENDING">⏳ Đang chờ (Pending)</option>
            <option value="IN_PROGRESS">🔥 Đang thực hiện (In Progress)</option>
            <option value="COMPLETED">✅ Đã hoàn thành (Completed)</option>
            <option value="CANCELED">❌ Đã hủy bỏ (Canceled)</option>
          </select>
        </div>

        <div class="flex justify-between items-center pt-4">
          <button 
            type="button" 
            @click="$emit('cancel')" 
            class="text-sm font-bold text-slate-400 hover:text-white transition-colors py-2"
          >
            Hủy bỏ
          </button>
          
          <button 
            type="submit" 
            class="px-8 py-3.5 bg-blue-600 text-white text-sm font-bold rounded-xl shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-all active:scale-95 flex items-center space-x-3"
          >
            <span>{{ isEdit ? 'Lưu Cập Nhật' : 'Tạo Dự Án Ngay' }}</span>
            <span class="text-lg leading-none">→</span>
          </button>
        </div>

      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useToast } from "../composables/useToast";

const props = defineProps({
  initialData: { type: Object, default: null },
  isEdit: { type: Boolean, default: false }
});

const emit = defineEmits(["project-created", "cancel"]);
const { addToast } = useToast();

const project = ref({
  id: null,
  name: "",
  description: "",
  startDate: "",
  endDate: "",
  status: "PENDING",
  priority: "MEDIUM"
});

onMounted(() => {
  if (props.isEdit && props.initialData) {
    project.value = { ...props.initialData };
  }
});

const submitProject = async () => {
  try {
    const start = new Date(project.value.startDate);
    const end = new Date(project.value.endDate);

    if (end < start) {
      addToast("Hạn chót không được trước ngày bắt đầu!", "warning");
      return;
    }

    const url = props.isEdit ? "http://localhost:8080/api/projects/update" : "http://localhost:8080/api/projects/create";
    
    let payload = {};
    if (props.isEdit) {
      payload = { 
        projectId: project.value.id, 
        name: project.value.name, 
        desc: project.value.description, 
        status: project.value.status, 
        endDate: project.value.endDate,
        priority: project.value.priority 
      };
    } else {
      payload = {
        ...project.value,
        userId: localStorage.getItem('userId')
      };
    }

    const currentUserId = localStorage.getItem('userId');

    const response = await fetch(url, {
      method: "POST",
      headers: { 
        "Content-Type": "application/json",
        "User-ID": currentUserId // BẮT BUỘC THÊM DÒNG NÀY ĐỂ ĐI QUA LỚP GIÁP CỦA JAVA
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.error || "Lỗi Server API!");
    }

    addToast(props.isEdit ? "Cập nhật dự án thành công!" : "Khởi tạo dự án mới thành công!", "success");
    emit("project-created", Date.now());
    
  } catch (error) {
    addToast("Lỗi: " + error.message, "error");
  }
};
</script>

<style scoped>
.animate-fade-in { 
  animation: fadeIn 0.3s ease-out forwards; 
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Ép lịch của trình duyệt chuyển sang giao diện tối */
input[type="date"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}
</style>