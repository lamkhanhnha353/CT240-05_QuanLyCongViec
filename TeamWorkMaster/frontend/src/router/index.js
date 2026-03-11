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
    // KHU VỰC DÀNH CHO USER BÌNH THƯỜNG
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true, role: 'MEMBER' } 
    },
    // KHU VỰC CẤM - CHỈ DÀNH CHO ADMIN
    {
      path: '/admin',
      component: () => import('../views/AdminView.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          path: '', // Mặc định khi vào /admin sẽ tự động nhảy sang /admin/users
          redirect: '/admin/users'
        },
        {
          path: 'users', // Đường dẫn sẽ là /admin/users
          name: 'admin-users',
          component: () => import('../views/AdminUsersView.vue')
        }
      ]
    }
  ]
})

// BÁC BẢO VỆ SIÊU CẤP
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
  const userRole = localStorage.getItem('role'); // Lấy quyền hạn từ vé

  // 1. Chưa đăng nhập mà đòi vào trang mật -> Đuổi ra cổng
  if (to.meta.requiresAuth && !isLoggedIn) {
    alert('Vui lòng đăng nhập!');
    return next('/');
  }

  // 2. Đã đăng nhập nhưng đi sai luồng
  if (isLoggedIn && to.meta.requiresAuth) {
    if (to.meta.role === 'ADMIN' && userRole !== 'ADMIN') {
      alert('Khu vực cấm! Bạn không phải là Quản trị viên.');
      return next('/dashboard'); // Đá về trang User
    }
    if (to.meta.role === 'MEMBER' && userRole === 'ADMIN') {
      return next('/admin'); // Admin thì ưu tiên đẩy thẳng vào Tổng hành dinh
    }
  }

  // 3. Đang ở màn hình Login mà có vé rồi -> Tự động chuyển trang
  if ((to.path === '/' || to.path === '/register') && isLoggedIn) {
    if (userRole === 'ADMIN') return next('/admin');
    return next('/dashboard');
  }

  // Hợp lệ hết thì cho đi
  next();
})  

export default router