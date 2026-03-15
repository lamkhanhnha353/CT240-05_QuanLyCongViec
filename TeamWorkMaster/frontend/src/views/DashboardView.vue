<template>
  <div class="min-h-screen bg-slate-50 flex font-sans">
    
    <aside class="w-64 bg-slate-900 text-white flex flex-col shadow-2xl z-10">
      <div class="p-6 border-b border-slate-800">
        <h2 class="text-2xl font-black text-blue-500 tracking-tighter">TEAMWORK</h2>
        <p class="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1">Master System</p>
      </div>
      
      <nav class="flex-1 p-4 space-y-2">
        <a href="#" class="block px-4 py-3 bg-blue-600 rounded-xl font-bold shadow-lg shadow-blue-900/50 transition-all">Bảng điều khiển</a>
        <a href="#" class="block px-4 py-3 hover:bg-slate-800 rounded-xl font-bold text-slate-400 hover:text-white transition-all">Quản lý Dự án</a>
        <a href="#" class="block px-4 py-3 hover:bg-slate-800 rounded-xl font-bold text-slate-400 hover:text-white transition-all">Công việc của tôi</a>
      </nav>

      <div class="p-4 border-t border-slate-800">
        <button @click="handleLogout" class="w-full py-3 text-center text-red-400 font-bold hover:bg-red-500/10 rounded-xl transition-all">
          Đăng xuất
        </button>
      </div>
    </aside>

    <main class="flex-1 p-10 overflow-y-auto">
      <header class="flex justify-between items-center mb-10">
        <div>
          <h1 class="text-3xl font-extrabold text-slate-800">Tổng quan hệ thống</h1>
          <p class="text-slate-500 font-medium mt-1">Chào mừng bạn quay trở lại làm việc!</p>
        </div>
        <div class="flex items-center space-x-4 bg-white px-5 py-2 rounded-full shadow-sm border border-slate-200">
          <span class="font-bold text-slate-700">Xin chào, {{ currentUser }}</span>
          <div class="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white font-black shadow-md">
            {{ firstLetter }}
          </div>
        </div>
      </header>

      <div class="grid grid-cols-3 gap-6 mb-10">
        <div v-for="(stat, index) in statistics.labels" :key="index" 
             class="bg-white p-6 rounded-3xl shadow-sm border border-slate-100 hover:shadow-md transition-all flex justify-between items-center">
          <div>
            <h3 class="text-slate-500 font-bold mb-2 uppercase text-xs tracking-wider">Trạng thái: {{ stat }}</h3>
            <p class="text-4xl font-black" :class="getColorForStatus(stat)">{{ statistics.data[index] }}</p>
          </div>
          <div class="w-16 h-16 rounded-full border-4 flex items-center justify-center font-bold" 
               :class="getBorderColorForStatus(stat)">
            {{ Math.round((statistics.data[index] / totalTasks) * 100) }}%
          </div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-6">
        
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-100">
          <h2 class="text-xl font-bold text-slate-800 mb-4">Tiến độ dự án hiện tại</h2>
          <div class="mb-4">
            <div class="flex justify-between mb-1">
              <span class="text-sm font-medium text-slate-700">Hoàn thành tổng thể</span>
              <span class="text-sm font-medium text-blue-600">{{ progressPercent }}%</span>
            </div>
            <div class="w-full bg-slate-200 rounded-full h-2.5">
              <div class="bg-blue-600 h-2.5 rounded-full transition-all duration-1000" :style="{ width: progressPercent + '%' }"></div>
            </div>
          </div>
          <p class="text-sm text-slate-500 mt-4">
            Dữ liệu được lấy trực tiếp từ luồng nền của Java Backend (Plugin Thống Kê).
          </p>
        </div>

        <div class="bg-white p-6 rounded-3xl shadow-sm border border-slate-100 flex flex-col h-96">
          <h2 class="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
            <span>💬</span> Thảo luận công việc
          </h2>
          
          <div class="flex-1 overflow-y-auto space-y-4 pr-2 mb-4">
            <div v-for="comment in comments" :key="comment.id" 
                 class="p-3 rounded-2xl" 
                 :class="comment.user === currentUser ? 'bg-blue-50 ml-8' : 'bg-slate-50 mr-8'">
              <div class="flex justify-between items-baseline mb-1">
                <span class="text-sm font-bold text-slate-700">{{ comment.user }}</span>
                <span class="text-xs text-slate-400">{{ comment.time }}</span>
              </div>
              <p class="text-sm text-slate-600">{{ comment.content }}</p>
            </div>
          </div>

          <form @submit.prevent="submitComment" class="flex gap-2">
            <input v-model="newComment" type="text" placeholder="Nhập bình luận của bạn..." required
                   class="flex-1 px-4 py-2 border border-slate-200 rounded-xl focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all">
            <button type="submit" 
                    class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-xl font-bold transition-all disabled:opacity-50">
              Gửi
            </button>
          </form>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentUser = ref('Khách')
const firstLetter = ref('K')

// --- DATA CHO THỐNG KÊ (Sẽ lấy từ Backend Java) ---
// Định dạng JSON này khớp hoàn toàn với String mà Java đang return ở doInBackground()
const statistics = ref({
  labels: ["To Do", "In Progress", "Done"],
  data: [12, 5, 20]
})

// Tính tổng số lượng công việc
const totalTasks = computed(() => statistics.value.data.reduce((a, b) => a + b, 0))

// Tính phần trăm hoàn thành (Dựa trên Done / Total)
const progressPercent = computed(() => {
  if (totalTasks.value === 0) return 0
  const doneIndex = statistics.value.labels.indexOf("Done")
  return Math.round((statistics.value.data[doneIndex] / totalTasks.value) * 100)
})

// Hàm hỗ trợ màu sắc
const getColorForStatus = (status) => {
  if (status === 'To Do') return 'text-slate-600'
  if (status === 'In Progress') return 'text-orange-500'
  if (status === 'Done') return 'text-emerald-500'
}
const getBorderColorForStatus = (status) => {
  if (status === 'To Do') return 'border-slate-200 text-slate-600'
  if (status === 'In Progress') return 'border-orange-200 text-orange-500'
  if (status === 'Done') return 'border-emerald-200 text-emerald-500'
}

// --- DATA CHO BÌNH LUẬN ---
const comments = ref([
  { id: 1, user: 'Quản lý', content: 'Tiến độ dự án đang rất tốt, mọi người cố gắng nhé!', time: '10:00 AM' },
  { id: 2, user: 'Dev Team', content: 'Đã hoàn thành xong module Thống kê backend.', time: '10:15 AM' }
])
const newComment = ref('')

const submitComment = () => {
  if (!newComment.value.trim()) return
  
  // Lấy giờ hiện tại
  const now = new Date()
  const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  // Đẩy bình luận mới vào danh sách UI (Sau này sẽ gọi fetch/axios gửi về Java API ở đây)
  comments.value.push({
    id: Date.now(),
    user: currentUser.value,
    content: newComment.value,
    time: timeString
  })
  
  newComment.value = '' // Clear input
}

// --- KHỞI TẠO ---
onMounted(() => {
  const storedUser = localStorage.getItem('username')
  if (storedUser) {
    currentUser.value = storedUser
    firstLetter.value = storedUser.charAt(0).toUpperCase()
  }

  // TODO: Call API tới ApiServer.java để lấy số liệu thực tế thay vì dùng fake data
  // fetch('http://localhost:8080/api/statistics')
  //   .then(res => res.json())
  //   .then(data => statistics.value = data)
})

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn');
  localStorage.removeItem('username');
  alert("Đã đăng xuất khỏi hệ thống!");
  router.push('/');
}
</script>