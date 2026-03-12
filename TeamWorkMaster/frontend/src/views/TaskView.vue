<template>
  <div class="task-page">
    <h2>📋 Quản lý công việc nhóm</h2>

    <div v-if="isLoading">Đang tải dữ liệu từ Backend...</div>

    <table
      v-else
      border="1"
      style="width: 100%; border-collapse: collapse; margin-top: 20px"
    >
      <thead>
        <tr style="background-color: #f4f4f4">
          <th>ID</th>
          <th>Tên công việc</th>
          <th>Trạng thái</th>
          <th>Độ ưu tiên</th>
          <th>Hạn chót</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="task in tasks" :key="task.id">
          <td style="text-align: center">{{ task.id }}</td>
          <td>{{ task.title }}</td>
          <td style="text-align: center">{{ task.status || "To Do" }}</td>
          <td style="text-align: center">{{ task.priority || "Medium" }}</td>
          <td style="text-align: center">{{ task.deadline || "Chưa có" }}</td>
        </tr>
      </tbody>
    </table>

    <div v-if="!isLoading && tasks.length === 0" style="margin-top: 15px">
      Chưa có công việc nào trong Database.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { taskApi } from "../api/taskApi";

const tasks = ref([]);
const isLoading = ref(true);

const fetchTasks = async () => {
  isLoading.value = true;
  try {
    const data = await taskApi.getAllTasks();
    tasks.value = data;
  } catch (error) {
    alert(
      "Không thể tải dữ liệu. Bạn đã chạy file ApiServer.java bên Backend chưa?",
    );
  } finally {
    isLoading.value = false;
  }
};

// Gọi API ngay khi mở trang
onMounted(() => {
  fetchTasks();
});
</script>

<style scoped>
.task-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}
th,
td {
  padding: 10px;
}
</style>
