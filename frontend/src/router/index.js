import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue')
      },
      {
        path: 'exercises',
        name: 'Exercises',
        component: () => import('../views/ExerciseView.vue')
      },
      {
        path: 'goals',
        name: 'Goals',
        component: () => import('../views/GoalsView.vue')
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/StatsView.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.guest && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
