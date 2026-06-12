import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import KnowledgeView from '../views/KnowledgeView.vue'
import ChatView from '../views/ChatView.vue'
import StatsView from '../views/StatsView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/knowledge',
    name: 'knowledge',
    component: KnowledgeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'chat',
    component: ChatView,
    meta: { requiresAuth: true }
  },
  {
    path: '/stats',
    name: 'stats',
    component: StatsView,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  const isAdmin = localStorage.getItem('isAdmin') === 'true'
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
