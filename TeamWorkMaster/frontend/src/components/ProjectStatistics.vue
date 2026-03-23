<template>
  <div class="flex flex-col w-full h-full p-6 lg:p-8 overflow-y-auto custom-scrollbar bg-slate-50 dark:bg-slate-900 transition-colors">
    
    <header class="mb-8 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-black text-slate-800 dark:text-white">Thống kê Dự án</h2>
        <p class="text-sm text-slate-500 dark:text-slate-400 font-medium mt-1">Báo cáo tiến độ, khối lượng công việc và cảnh báo rủi ro.</p>
      </div>
      
      <div class="flex items-center space-x-3">
        
        <ExportExcelButton 
          :projectName="projectName" 
          :tasks="rawTasksList" 
          :members="rawMembersList" 
        />
        
        <button @click="fetchData" class="px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-sm font-bold text-slate-600 dark:text-slate-300 hover:bg-slate-50 shadow-sm transition-all flex items-center">
          <span class="mr-2" :class="isLoading ? 'animate-spin' : ''">🔄</span> Làm mới
        </button>
      </div>
    </header>

    <div v-if="isLoading" class="flex justify-center items-center py-20">
      <div class="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"></div>
    </div>

    <div v-else>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        
        <div class="bg-white dark:bg-slate-800 p-5 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 relative overflow-hidden">
          <div class="absolute -right-4 -top-4 text-6xl opacity-5">📊</div>
          <h3 class="text-slate-500 dark:text-slate-400 font-bold uppercase text-[10px] tracking-widest mb-1">Tổng tiến độ</h3>
          <div class="flex items-end space-x-2">
            <p class="text-3xl font-black text-blue-600">{{ completionRate }}%</p>
            <span class="text-xs font-bold text-slate-400 mb-1">({{ stats.done }}/{{ stats.total }})</span>
          </div>
          <div class="w-full bg-slate-100 dark:bg-slate-700 h-1.5 rounded-full mt-3 overflow-hidden">
            <div class="bg-blue-600 h-full rounded-full transition-all duration-1000" :style="{ width: completionRate + '%' }"></div>
          </div>
        </div>
        
        <div class="bg-white dark:bg-slate-800 p-5 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 relative overflow-hidden">
          <div class="absolute -right-4 -top-4 text-6xl opacity-5">⏳</div>
          <h3 class="text-slate-500 dark:text-slate-400 font-bold uppercase text-[10px] tracking-widest mb-1">Đang thực hiện</h3>
          <p class="text-3xl font-black text-amber-500">{{ stats.inProgress }}</p>
          <p class="text-xs font-bold text-slate-400 mt-2">Chờ xử lý: {{ stats.todo }}</p>
        </div>

        <div class="bg-red-50 dark:bg-red-900/10 p-5 rounded-2xl shadow-sm border border-red-100 dark:border-red-900/30 relative overflow-hidden group">
          <div class="absolute -right-4 -top-4 text-6xl opacity-10 group-hover:scale-110 transition-transform">🚨</div>
          <h3 class="text-red-500 dark:text-red-400 font-bold uppercase text-[10px] tracking-widest mb-1">Đã trễ hạn</h3>
          <p class="text-3xl font-black text-red-600 dark:text-red-500">{{ stats.overdue }}</p>
          <p class="text-xs font-bold text-red-400 mt-2 flex items-center"><span class="w-2 h-2 rounded-full bg-red-500 animate-pulse mr-1.5"></span> Cần xử lý gấp!</p>
        </div>

        <div class="bg-orange-50 dark:bg-orange-900/10 p-5 rounded-2xl shadow-sm border border-orange-100 dark:border-orange-900/30 relative overflow-hidden">
          <div class="absolute -right-4 -top-4 text-6xl opacity-10">⚠️</div>
          <h3 class="text-orange-600 dark:text-orange-400 font-bold uppercase text-[10px] tracking-widest mb-1">Tới hạn tuần này</h3>
          <p class="text-3xl font-black text-orange-600 dark:text-orange-500">{{ stats.dueThisWeek }}</p>
          <p class="text-xs font-bold text-orange-400 mt-2">Theo dõi sát sao</p>
        </div>

      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <div class="lg:col-span-1 space-y-6">
          <div class="bg-white dark:bg-slate-800 p-5 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700">
            <h3 class="text-sm font-black text-slate-800 dark:text-white mb-4">Tỷ trọng Trạng thái</h3>
            <div class="relative h-[220px] flex items-center justify-center"><canvas id="statusChart"></canvas></div>
          </div>
          <div class="bg-white dark:bg-slate-800 p-5 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700">
            <h3 class="text-sm font-black text-slate-800 dark:text-white mb-4 flex items-center justify-between">Độ ưu tiên dự án <span v-if="stats.highPriority > 0" class="bg-red-100 text-red-600 text-[10px] px-2 py-0.5 rounded animate-pulse">HOT</span></h3>
            <div class="relative h-[220px] flex items-center justify-center"><canvas id="priorityChart"></canvas></div>
          </div>
        </div>

        <div class="lg:col-span-2">
          <div class="bg-white dark:bg-slate-800 p-6 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 h-full flex flex-col">
            <div class="mb-6">
              <h3 class="text-lg font-black text-slate-800 dark:text-white">Khối lượng công việc theo Thành viên</h3>
              <p class="text-xs text-slate-500 dark:text-slate-400 font-medium mt-1">Phân tích số lượng việc <b>đang xử lý</b> (Chưa xong) của từng người để cân bằng tài nguyên.</p>
            </div>
            <div class="flex-1 relative min-h-[300px] w-full"><canvas id="workloadChart"></canvas></div>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-slate-800 p-6 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 mb-10">
        <div class="flex justify-between items-end mb-6">
          <div>
            <h3 class="text-lg font-black text-slate-800 dark:text-white flex items-center"><span class="mr-2">🚨</span> Cần chú ý khẩn cấp</h3>
            <p class="text-xs text-slate-500 dark:text-slate-400 font-medium mt-1">Danh sách công việc đã trễ hạn, sắp tới hạn hoặc mức ưu tiên Cao đang bị tắc nghẽn.</p>
          </div>
          <span class="bg-red-100 dark:bg-red-900/40 text-red-600 dark:text-red-400 text-xs font-bold px-3 py-1.5 rounded-lg">{{ urgentTasks.length }} cảnh báo</span>
        </div>

        <div class="overflow-x-auto custom-scrollbar">
          <table class="w-full text-left border-collapse">
            <thead class="bg-slate-50 dark:bg-slate-800/80 border-y border-slate-200 dark:border-slate-700">
              <tr class="text-[11px] uppercase tracking-widest text-slate-500 dark:text-slate-400">
                <th class="py-4 px-4 font-black">Công việc</th>
                <th class="py-4 px-4 font-black">Người phụ trách</th>
                <th class="py-4 px-4 font-black">Cột hiện tại</th>
                <th class="py-4 px-4 font-black text-right">Tình trạng rủi ro</th>
              </tr>
            </thead>
            <tbody v-if="urgentTasks.length > 0">
              <tr v-for="task in urgentTasks" :key="task.id" class="border-b border-slate-100 dark:border-slate-700/50 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                <td class="py-4 px-4">
                  <p class="font-bold text-slate-800 dark:text-white text-sm mb-1">{{ task.title }}</p>
                  <span v-if="task.priority === 'HIGH'" class="px-2 py-0.5 bg-red-100 text-red-600 text-[9px] font-black rounded uppercase">Ưu tiên Cao</span>
                </td>
                <td class="py-4 px-4">
                  <div class="flex flex-wrap gap-1">
                    <span v-for="(name, idx) in task.assigneeNames" :key="idx" class="px-2 py-1 bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 text-xs font-bold rounded-md">
                      {{ name }}
                    </span>
                  </div>
                </td>
                <td class="py-4 px-4">
                  <span class="text-xs font-bold px-2.5 py-1 rounded-md" :class="task.status === 'TODO' ? 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300' : 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400'">
                    {{ task.status === 'TODO' ? 'Cần làm' : 'Đang thực hiện' }}
                  </span>
                </td>
                <td class="py-4 px-4 text-right">
                  <div v-if="task.risk === 'OVERDUE'" class="inline-flex items-center text-red-600 dark:text-red-400 font-bold text-sm bg-red-50 dark:bg-red-900/20 px-3 py-1.5 rounded-lg">
                    Trễ {{ Math.abs(task.daysLeft) }} ngày!
                  </div>
                  <div v-else-if="task.risk === 'DUE_SOON'" class="inline-flex items-center text-orange-600 dark:text-orange-400 font-bold text-sm bg-orange-50 dark:bg-orange-900/20 px-3 py-1.5 rounded-lg">
                    Còn {{ task.daysLeft }} ngày
                  </div>
                  <div v-else class="inline-flex items-center text-purple-600 dark:text-purple-400 font-bold text-sm bg-purple-50 dark:bg-purple-900/20 px-3 py-1.5 rounded-lg">
                    Nguy cơ ngâm lâu
                  </div>
                </td>
              </tr>
            </tbody>
            <tbody v-else>
              <tr>
                <td colspan="4" class="py-12 text-center">
                  <div class="text-4xl mb-3">🎉</div>
                  <p class="text-slate-500 font-bold">Tuyệt vời! Hiện tại không có công việc nào gặp rủi ro.</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, onUnmounted, reactive } from "vue";
import Chart from 'chart.js/auto';
import { useRoute } from "vue-router"; 
// 🟢 Import Component Xuất Excel vừa tạo
import ExportExcelButton from "./ExportExcelButton.vue"; 

const props = defineProps({ projectId: { type: String, required: true } });
const route = useRoute(); 
// 🟢 Lấy tên dự án từ thanh địa chỉ để in ra file Excel
const projectName = ref(route.query.projectName || "Dự án"); 

const isLoading = ref(true);

const stats = reactive({
  total: 0, todo: 0, inProgress: 0, done: 0, cancel: 0,
  highPriority: 0, mediumPriority: 0, lowPriority: 0,
  overdue: 0, dueThisWeek: 0
});

const completionRate = ref(0);
const workloadLabels = ref([]);
const workloadData = ref([]);
const urgentTasks = ref([]); 

// 🟢 2 Biến lưu trữ dữ liệu thô cho File Excel
const rawTasksList = ref([]);
const rawMembersList = ref([]);

let statusChartInstance = null;
let priorityChartInstance = null;
let workloadChartInstance = null;

const fetchData = async () => {
  isLoading.value = true;
  try {
    const [resTasks, resMembers] = await Promise.all([
      fetch(`http://localhost:8080/api/tasks/list?projectId=${props.projectId}`),
      fetch(`http://localhost:8080/api/projects/members-list?projectId=${props.projectId}`)
    ]);

    if (!resTasks.ok || !resMembers.ok) throw new Error("Lỗi API");

    const tasks = await resTasks.json();
    const members = await resMembers.json();

    // 🟢 Cập nhật dữ liệu cho Excel
    rawTasksList.value = tasks;
    rawMembersList.value = members;

    Object.keys(stats).forEach(key => stats[key] = 0);
    stats.total = tasks.length;
    const workloadMap = {};
    members.forEach(m => workloadMap[m.id] = { name: m.fullName, count: 0 });
    workloadMap['unassigned'] = { name: 'Chưa phân công', count: 0 };

    const today = new Date();
    today.setHours(0,0,0,0);
    
    const urgentList = []; 

    tasks.forEach(t => {
      // 1. Phân loại Trạng thái & Ưu tiên
      if (t.status === 'TODO') stats.todo++;
      else if (t.status === 'IN_PROGRESS') stats.inProgress++;
      else if (t.status === 'DONE') stats.done++;
      else if (t.status === 'CANCEL') stats.cancel++;

      if (t.priority === 'HIGH') stats.highPriority++;
      else if (t.priority === 'MEDIUM') stats.mediumPriority++;
      else stats.lowPriority++;

      // 2. Tính toán rủi ro và nạp vào danh sách Khẩn Cấp
      if (t.status !== 'DONE' && t.status !== 'CANCEL') {
        let risk = null;
        let daysLeft = null;
        
        if (t.deadline && t.deadline !== 'null') {
          const taskDate = new Date(t.deadline);
          taskDate.setHours(0,0,0,0);
          const diffDays = Math.ceil((taskDate - today) / (1000 * 60 * 60 * 24));
          daysLeft = diffDays;
          
          if (diffDays < 0) { risk = 'OVERDUE'; stats.overdue++; }
          else if (diffDays >= 0 && diffDays <= 7) { 
            stats.dueThisWeek++; 
            if (diffDays <= 3) risk = 'DUE_SOON'; 
          }
        } 
        else if (t.priority === 'HIGH') {
          risk = 'HIGH_PRIORITY';
        }

        if (risk) {
          const assignees = (t.assigneeIds || '').split(',').filter(id => id).map(id => {
            const m = members.find(x => x.id == id);
            return m ? m.fullName : 'Tài khoản đã xóa';
          });
          urgentList.push({
            ...t, risk, daysLeft, assigneeNames: assignees.length > 0 ? assignees : ['Chưa phân công']
          });
        }
      }

      // 3. Tính Workload
      if (t.status === 'TODO' || t.status === 'IN_PROGRESS') {
        if (!t.assigneeIds || t.assigneeIds.trim() === '') workloadMap['unassigned'].count++;
        else t.assigneeIds.split(',').forEach(id => { if (workloadMap[id]) workloadMap[id].count++; });
      }
    });

    urgentList.sort((a, b) => {
        const riskScore = { 'OVERDUE': 3, 'DUE_SOON': 2, 'HIGH_PRIORITY': 1 };
        if (riskScore[a.risk] !== riskScore[b.risk]) return riskScore[b.risk] - riskScore[a.risk];
        if (a.daysLeft !== null && b.daysLeft !== null) return a.daysLeft - b.daysLeft;
        return 0;
    });
    urgentTasks.value = urgentList; 

    const activeTasks = stats.total - stats.cancel;
    completionRate.value = activeTasks > 0 ? Math.round((stats.done / activeTasks) * 100) : 0;

    const sortedWorkload = Object.values(workloadMap).filter(w => w.count > 0).sort((a, b) => b.count - a.count);
    workloadLabels.value = sortedWorkload.map(w => w.name);
    workloadData.value = sortedWorkload.map(w => w.count);

  } catch (error) { console.error("Lỗi tính toán thống kê:", error); } 
  finally { isLoading.value = false; await nextTick(); renderCharts(); }
};

onMounted(() => { fetchData(); });
watch(() => props.projectId, () => { fetchData(); });

onUnmounted(() => {
  if (statusChartInstance) statusChartInstance.destroy();
  if (priorityChartInstance) priorityChartInstance.destroy();
  if (workloadChartInstance) workloadChartInstance.destroy();
});

const renderCharts = () => {
  if (statusChartInstance) statusChartInstance.destroy();
  if (priorityChartInstance) priorityChartInstance.destroy();
  if (workloadChartInstance) workloadChartInstance.destroy();

  Chart.defaults.font.family = "'Inter', sans-serif";
  Chart.defaults.color = '#64748b';

  const ctxStatus = document.getElementById('statusChart');
  if (ctxStatus) {
    statusChartInstance = new Chart(ctxStatus, {
      type: 'doughnut',
      data: {
        labels: ['Cần làm', 'Đang làm', 'Đã xong', 'Đã hủy'],
        datasets: [{ data: [stats.todo, stats.inProgress, stats.done, stats.cancel], backgroundColor: ['#94a3b8', '#3b82f6', '#10b981', '#ef4444'], borderWidth: 0, hoverOffset: 5 }]
      },
      options: { responsive: true, maintainAspectRatio: false, cutout: '65%', plugins: { legend: { position: 'right', labels: { usePointStyle: true, boxWidth: 8 } } } }
    });
  }

  const ctxPriority = document.getElementById('priorityChart');
  if (ctxPriority) {
    priorityChartInstance = new Chart(ctxPriority, {
      type: 'doughnut',
      data: {
        labels: ['Cao', 'Trung bình', 'Thấp'],
        datasets: [{ data: [stats.highPriority, stats.mediumPriority, stats.lowPriority], backgroundColor: ['#ef4444', '#f59e0b', '#cbd5e1'], borderWidth: 0, hoverOffset: 5 }]
      },
      options: { responsive: true, maintainAspectRatio: false, cutout: '65%', plugins: { legend: { position: 'right', labels: { usePointStyle: true, boxWidth: 8 } } } }
    });
  }

  const ctxWorkload = document.getElementById('workloadChart');
  if (ctxWorkload && workloadLabels.value.length > 0) {
    workloadChartInstance = new Chart(ctxWorkload, {
      type: 'bar',
      data: {
        labels: workloadLabels.value,
        datasets: [{ label: 'Số việc đang cầm', data: workloadData.value, backgroundColor: '#6366f1', borderRadius: 6, barThickness: 35 }]
      },
      options: {
        indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } },
        scales: { x: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { borderDash: [5, 5] } }, y: { grid: { display: false }, ticks: { font: { weight: 'bold' } } } }
      }
    });
  } else if (ctxWorkload) {
    const ctx = ctxWorkload.getContext('2d');
    ctx.font = "14px Inter"; ctx.fillStyle = "#94a3b8"; ctx.textAlign = "center";
    ctx.fillText("Mọi người đang rảnh rỗi, không có việc nào đang xử lý!", ctxWorkload.width/2, ctxWorkload.height/2);
  }
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.dark .custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; }
.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>