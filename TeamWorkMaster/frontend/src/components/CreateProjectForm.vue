<template>
  <div class="bg-white p-8 rounded-3xl shadow-sm border border-slate-100 mb-8">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-extrabold text-slate-800">
        {{ isEdit ? 'Cập Nhật Dự Án' : 'Tạo Dự Án Mới' }}
      </h2>
      <button @click="$emit('cancel')" class="text-slate-400 hover:text-red-500 font-bold transition-colors">
        ✕ Đóng
      </button>
    </div>

    <form @submit.prevent="submitProject" class="space-y-6">
      <div>
        <label class="block text-sm font-bold text-slate-700 mb-2">Tên dự án <span class="text-red-500">*</span></label>
        <input v-model="project.name" type="text" required placeholder="Nhập tên dự án của bạn..." class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all" />
      </div>

      <div>
        <label class="block text-sm font-bold text-slate-700 mb-2">Mô tả chi tiết</label>
        <textarea v-model="project.description" rows="4" placeholder="Mô tả mục tiêu của dự án này..." class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"></textarea>
      </div>

      <div class="grid grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-bold text-slate-700 mb-2">Ngày bắt đầu <span class="text-red-500">*</span></label>
          <input v-model="project.startDate" type="date" required :disabled="isEdit" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all disabled:bg-slate-100 disabled:text-slate-400" />
          <p v-if="isEdit" class="text-xs text-slate-400 mt-1">* Không thể đổi ngày bắt đầu sau khi đã tạo.</p>
        </div>
        <div>
          <label class="block text-sm font-bold text-slate-700 mb-2">Ngày dự kiến kết thúc <span class="text-red-500">*</span></label>
          <input v-model="project.endDate" type="date" required class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all" />
        </div>
      </div>

      <div v-if="isEdit" class="bg-slate-50 p-4 rounded-xl border border-slate-100">
        <label class="block text-sm font-bold text-slate-700 mb-2">Cập nhật Trạng thái</label>
        <select v-model="project.status" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none bg-white transition-all">
          <option value="PENDING">⏳ Đang chờ (Pending)</option>
          <option value="IN_PROGRESS">🔥 Đang làm (In Progress)</option>
          <option value="COMPLETED">✅ Hoàn thành (Completed)</option>
          <option value="CANCELED">❌ Đã hủy (Canceled)</option>
        </select>
      </div>

      <div class="flex justify-end pt-4 space-x-3">
        <button type="button" @click="$emit('cancel')" class="px-6 py-3 bg-slate-100 text-slate-600 font-bold rounded-xl hover:bg-slate-200 transition-all">
          Hủy bỏ
        </button>
        <button type="submit" class="px-8 py-3 bg-blue-600 text-white font-bold rounded-xl shadow-lg shadow-blue-600/30 hover:bg-blue-700 transition-all active:scale-95">
          {{ isEdit ? '💾 Lưu Cập Nhật' : '🚀 Tạo Dự Án Ngay' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

// Khai báo Props để nhận dữ liệu từ file cha truyền vào
const props = defineProps({
  initialData: {
    type: Object,
    default: null
  },
  isEdit: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(["project-created", "cancel"]);

const project = ref({
  id: null,
  name: "",
  description: "",
  startDate: "",
  endDate: "",
  status: "PENDING"
});

// Nếu là chế độ Sửa, nạp dữ liệu cũ vào Form
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
      alert("Lỗi: Ngày kết thúc không được nhỏ hơn ngày bắt đầu!");
      return;
    }

    // Chọn URL và Payload tùy theo chế độ
    const url = props.isEdit ? "http://localhost:8080/api/projects/update" : "http://localhost:8080/api/projects/create";
    
    // Nắn lại cục data cho đúng chuẩn API Backend của bạn
    const payload = props.isEdit 
      ? { 
          projectId: project.value.id, 
          name: project.value.name, 
          desc: project.value.description, 
          status: project.value.status, 
          endDate: project.value.endDate 
        } 
      : project.value;

    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "Không thể kết nối tới Server!");
    }

    alert(props.isEdit ? "Cập nhật dự án thành công!" : "Tạo dự án mới thành công!");
    emit("project-created", Date.now());
    
  } catch (error) {
    alert("Lỗi: " + error.message);
  }
};
</script>