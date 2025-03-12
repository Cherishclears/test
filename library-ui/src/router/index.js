import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// 页面组件
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import BookList from '../views/BookList.vue'
import BookDetail from '../views/BookDetail.vue'
import UserProfile from '../views/UserProfile.vue'
import BorrowHistory from '../views/BorrowHistory.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'
import AdminBookManage from '../views/admin/BookManage.vue'
import AdminUserManage from '../views/admin/UserManage.vue'
import AdminBorrowManage from '../views/admin/BorrowManage.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/books',
    name: 'BookList',
    component: BookList
  },
  {
    path: '/books/:id',
    name: 'BookDetail',
    component: BookDetail,
    props: true
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/borrows',
    name: 'BorrowHistory',
    component: BorrowHistory,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/books',
    name: 'AdminBookManage',
    component: AdminBookManage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUserManage',
    component: AdminUserManage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/borrows',
    name: 'AdminBorrowManage',
    component: AdminBorrowManage,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  
  // 检查用户是否已登录
  if (requiresAuth && !authStore.isAuthenticated) {
    console.log('需要登录权限，重定向到登录页面')
    next('/login')
  } 
  // 检查用户是否有管理员权限
  else if (requiresAdmin && !authStore.isAdmin) {
    console.log('需要管理员权限，重定向到首页')
    next('/')
  } 
  else {
    next()
  }
})

export default router 