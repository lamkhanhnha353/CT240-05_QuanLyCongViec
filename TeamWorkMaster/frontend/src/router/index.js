import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    // ==========================================
    // KHU VỰC DÀNH CHO USER BÌNH THƯỜNG
    // ==========================================
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true } // Đánh dấu: Trang này cần phải có vé mới được vào
    },
    // 👇 THÊM TRANG QUẢN LÝ DỰ ÁN VÀO ĐÂY 👇
    {
      path: '/projects',
      name: 'projects',
      component: () => import('../views/ProjectsView.vue'),
      meta: { requiresAuth: true, role: 'MEMBER' }
    },

    {
      path: '/board/:projectId', 
      name: 'KanbanBoard',
      component: () => import('../views/KanbanBoardView.vue')
     },

    // ==========================================
    // KHU VỰC CẤM - CHỈ DÀNH CHO ADMIN
    // ==========================================
    {
      path: '/admin',
      component: () => import('../views/AdminView.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          // Mặc định khi vào /admin sẽ hiển thị Bảng điều khiển (Biểu đồ)
          path: '', 
          name: 'admin-dashboard',
          component: () => import('../views/AdminDashboardView.vue')
        },
        {
          // Khi vào /admin/users sẽ hiển thị Bảng quản lý Cư dân mạng
          path: 'users', 
          name: 'admin-users',
          component: () => import('../views/AdminUsersView.vue')
        }
      ]
    }
  ]
})

// Kiểm tra mọi lượt di chuyển trang
// ==========================================
// BÁC BẢO VỆ SIÊU CẤP (Navigation Guards)
// ==========================================
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
  const userRole = localStorage.getItem('role'); // Lấy quyền hạn từ vé

  // 1. Người dùng đã đăng nhập
  if (isLoggedIn) {
    // 1a. Nếu đang ở trang login/register, tự động chuyển theo role
    if (to.path === '/' || to.path === '/register') {
      if (userRole === 'ADMIN') {
        return next('/admin');
      } else {
        return next('/dashboard');
      }
    }
    // 1b. Nếu đi tới trang cần ADMIN nhưng user không phải ADMIN
    if (to.meta.role === 'ADMIN' && userRole !== 'ADMIN') {
      alert('Khu vực cấm! Bạn không phải là Quản trị viên.');
      return next('/dashboard');
    }
    // 1c. Nếu đi tới trang MEMBER nhưng user là ADMIN, đẩy sang admin
    if (to.meta.role === 'MEMBER' && userRole === 'ADMIN') {
      return next('/admin');
    }
    // 1d. Hợp lệ thì cho qua
    next();
  } else {
    // 2. Người dùng chưa đăng nhập
    // 2a. Nếu trang yêu cầu auth, đuổi về login
    if (to.meta.requiresAuth) {
      alert('Truy cập bị từ chối! Vui lòng đăng nhập.');
      return next('/');
    }
    // 2b. Nếu không yêu cầu auth (login/register), cho qua
    next();
  }
})  

export default router