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
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true } // Đánh dấu: Trang này cần phải có vé mới được vào
    }
  ]
})

//  Kiểm tra mọi lượt di chuyển trang
router.beforeEach((to, from, next) => {
  // Kiểm tra xem trong túi (localStorage) có vé không
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';

  if (to.meta.requiresAuth && !isLoggedIn) {
   
    alert('Truy cập bị từ chối! Vui lòng đăng nhập.');
    next('/');
  } else if ((to.path === '/' || to.path === '/register') && isLoggedIn) {
    next('/dashboard');
  } else {

    next();
  }
})

export default router