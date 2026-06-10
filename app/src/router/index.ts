import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue')
    },
    {
      path: '/',
      component: () => import('@/views/LayoutView.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        },
        {
          path: 'tickets',
          name: 'TicketList',
          component: () => import('@/views/TicketListView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'tickets/create',
          name: 'TicketCreate',
          component: () => import('@/views/TicketFormView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'tickets/:id',
          name: 'TicketDetail',
          component: () => import('@/views/TicketDetailView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'tickets/:id/edit',
          name: 'TicketEdit',
          component: () => import('@/views/TicketFormView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'admin/users',
          name: 'UserManage',
          component: () => import('@/views/UserManageView.vue'),
          meta: { requiresAuth: true, requiresAdmin: true }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/tickets')
    return
  }

  // 已登录访问登录页跳转首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/tickets')
    return
  }

  next()
})

export default router
