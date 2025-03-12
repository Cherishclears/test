<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => authStore.isAdmin)
const username = computed(() => authStore.user?.name || authStore.user?.username)

const logout = () => {
  authStore.logout()
}

const goToHome = () => {
  router.push('/')
}

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToAdminDashboard = () => {
  router.push('/admin')
}

const goToBorrowHistory = () => {
  router.push('/borrows')
}
</script>

<template>
  <header class="app-header">
    <div class="logo" @click="goToHome">
      <h1>图书馆管理系统</h1>
    </div>
    <div class="nav-links">
      <el-menu mode="horizontal" :router="true" :default-active="$route.path">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/books">图书浏览</el-menu-item>
        
        <template v-if="isAuthenticated">
          <el-sub-menu index="user">
            <template #title>
              <el-icon><el-icon-user /></el-icon>
              <span>{{ username }}</span>
            </template>
            
            <el-menu-item index="/profile" @click="goToProfile">个人信息</el-menu-item>
            <el-menu-item index="/borrows" @click="goToBorrowHistory">借阅历史</el-menu-item>
            
            <template v-if="isAdmin">
              <el-menu-item index="/admin" @click="goToAdminDashboard">管理后台</el-menu-item>
            </template>
            
            <el-menu-item index="logout" @click="logout">退出登录</el-menu-item>
          </el-sub-menu>
        </template>
        
        <template v-else>
          <el-menu-item index="/login" @click="goToLogin">登录</el-menu-item>
          <el-menu-item index="/register" @click="goToRegister">注册</el-menu-item>
        </template>
      </el-menu>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  cursor: pointer;
}

.logo h1 {
  font-size: 1.5rem;
  color: #409EFF;
  margin: 0;
}

.nav-links {
  display: flex;
  align-items: center;
}
</style> 