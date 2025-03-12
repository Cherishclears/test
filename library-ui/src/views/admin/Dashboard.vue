<template>
  <div class="admin-dashboard">
    <h1 class="page-title">管理员仪表盘</h1>
    
    <!-- 调试信息区域 -->
    <el-card class="debug-card" v-if="showDebug">
      <template #header>
        <div class="card-header">
          <h3>调试信息</h3>
          <el-button type="primary" size="small" @click="refreshDebugInfo">刷新</el-button>
        </div>
      </template>
      <div class="debug-info">
        <p><strong>认证状态:</strong> {{ authStore.isAuthenticated ? '已登录' : '未登录' }}</p>
        <p><strong>管理员权限:</strong> {{ authStore.isAdmin ? '是' : '否' }}</p>
        <p><strong>用户信息:</strong> {{ JSON.stringify(authStore.user) }}</p>
        <p><strong>Token:</strong> {{ authStore.token ? authStore.token.substring(0, 20) + '...' : '无' }}</p>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        <el-button type="warning" size="small" @click="handleRelogin">重新登录</el-button>
      </div>
    </el-card>
    
    <div class="admin-nav">
      <router-link to="/admin/books" class="admin-nav-item">
        <el-button type="primary">图书管理</el-button>
      </router-link>
      <router-link to="/admin/users" class="admin-nav-item">
        <el-button type="primary">用户管理</el-button>
      </router-link>
      <router-link to="/admin/borrows" class="admin-nav-item">
        <el-button type="primary">借阅管理</el-button>
      </router-link>
      <el-button type="info" @click="showDebug = !showDebug">{{ showDebug ? '隐藏调试' : '显示调试' }}</el-button>
    </div>
    
    <div class="stat-cards">
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>图书总数</h3>
          </div>
        </template>
        <div class="stat-value">
          <span class="number">{{ stats.totalBooks }}</span>
          <el-icon class="icon"><Reading /></el-icon>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>用户总数</h3>
          </div>
        </template>
        <div class="stat-value">
          <span class="number">{{ stats.totalUsers }}</span>
          <el-icon class="icon"><User /></el-icon>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>借阅总数</h3>
          </div>
        </template>
        <div class="stat-value">
          <span class="number">{{ stats.totalBorrows }}</span>
          <el-icon class="icon"><List /></el-icon>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <h3>待处理借阅</h3>
          </div>
        </template>
        <div class="stat-value">
          <span class="number">{{ stats.pendingBorrows }}</span>
          <el-icon class="icon"><Timer /></el-icon>
        </div>
      </el-card>
    </div>
    
    <div class="recent-section">
      <el-card class="recent-card">
        <template #header>
          <div class="card-header">
            <h3>最近借阅</h3>
            <router-link to="/admin/borrows" class="view-all">查看全部</router-link>
          </div>
        </template>
        
        <el-table :data="recentBorrows" style="width: 100%" v-loading="loading.borrows">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userRealName" label="用户" width="120" />
          <el-table-column prop="bookTitle" label="图书" min-width="200" />
          <el-table-column prop="borrowDate" label="借阅日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.borrowDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                type="success" 
                size="small"
                @click="handleApprove(scope.row.id)"
              >
                批准
              </el-button>
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                type="danger" 
                size="small"
                @click="handleReject(scope.row.id)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <el-card class="recent-card">
        <template #header>
          <div class="card-header">
            <h3>逾期未还</h3>
            <router-link to="/admin/borrows" class="view-all">查看全部</router-link>
          </div>
        </template>
        
        <el-table :data="overdueBorrows" style="width: 100%" v-loading="loading.overdue">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userRealName" label="用户" width="120" />
          <el-table-column prop="bookTitle" label="图书" min-width="200" />
          <el-table-column prop="borrowDate" label="借阅日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.borrowDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="应还日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.dueDate) }}
            </template>
          </el-table-column>
          <el-table-column label="逾期天数" width="100">
            <template #default="scope">
              {{ calculateOverdueDays(scope.row.dueDate) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading, User, List, Timer } from '@element-plus/icons-vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const stats = reactive({
  totalBooks: 0,
  totalUsers: 0,
  totalBorrows: 0,
  pendingBorrows: 0
})
const recentBorrows = ref([])
const overdueBorrows = ref([])
const loading = reactive({
  stats: false,
  borrows: false,
  overdue: false
})
const showDebug = ref(false)

// 获取统计数据
const fetchStats = async () => {
  loading.stats = true
  try {
    console.log('开始获取统计数据')
    console.log('当前认证状态:', authStore.isAuthenticated)
    console.log('当前用户角色:', authStore.user?.role)
    console.log('当前token:', localStorage.getItem('token'))
    
    // 确保token已设置
    const token = localStorage.getItem('token')
    if (!token) {
      console.error('Token不存在，请重新登录')
      ElMessage.error('登录信息已失效，请重新登录')
      authStore.logout()
      return
    }
    
    // 手动设置请求头
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
    console.log('请求头:', headers)
    
    console.log('发送请求到:', '/api/admin/stats')
    const response = await axios.get('/api/admin/stats', { headers })
    
    console.log('获取统计数据响应:', response)
    if (response.data && response.data.data) {
      const data = response.data.data
      stats.totalBooks = data.totalBooks || 0
      stats.totalUsers = data.totalUsers || 0
      stats.totalBorrows = data.totalBorrows || 0
      stats.pendingBorrows = data.pendingBorrows || 0
    } else {
      console.error('响应数据格式不正确:', response.data)
      ElMessage.warning('获取数据格式不正确')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    console.error('错误详情:', error.response ? error.response.data : '无响应数据')
    console.error('错误状态码:', error.response ? error.response.status : '无状态码')
    console.error('请求配置:', error.config)
    
    if (error.response) {
      if (error.response.status === 403) {
        console.error('权限错误: 403 Forbidden')
        ElMessage.error('您没有权限访问此资源，请确认您是管理员')
        router.push('/')
      } else if (error.response.status === 401) {
        console.error('认证错误: 401 Unauthorized')
        ElMessage.error('认证已过期，请重新登录')
        authStore.logout()
      } else {
        console.error('服务器错误:', error.response.status)
        ElMessage.error(`服务器错误 (${error.response.status})，请稍后重试`)
      }
    } else if (error.request) {
      console.error('未收到响应:', error.request)
      ElMessage.error('服务器未响应，请检查网络连接')
    } else {
      console.error('请求配置错误:', error.message)
      ElMessage.error('请求错误，请稍后重试')
    }
  } finally {
    loading.stats = false
  }
}

// 获取最近借阅
const fetchRecentBorrows = async () => {
  loading.borrows = true
  try {
    const response = await axios.get('/api/admin/borrows/recent')
    recentBorrows.value = response.data.data
  } catch (error) {
    console.error('获取最近借阅失败:', error)
    ElMessage.error('获取最近借阅失败，请稍后重试')
  } finally {
    loading.borrows = false
  }
}

// 获取逾期借阅
const fetchOverdueBorrows = async () => {
  loading.overdue = true
  try {
    const response = await axios.get('/api/borrows/overdue')
    overdueBorrows.value = response.data.data
  } catch (error) {
    console.error('获取逾期借阅失败:', error)
    ElMessage.error('获取逾期借阅失败，请稍后重试')
  } finally {
    loading.overdue = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

// 计算逾期天数
const calculateOverdueDays = (dueDate) => {
  if (!dueDate) return 0
  
  const today = new Date()
  const due = new Date(dueDate)
  const diffTime = today - due
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays > 0 ? diffDays : 0
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'info',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'RETURNED': '',
    'OVERDUE': 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'RETURNED': '已归还',
    'OVERDUE': '已逾期'
  }
  return statusMap[status] || status
}

// 处理批准借阅
const handleApprove = async (borrowId) => {
  try {
    await ElMessageBox.confirm('确定批准这个借阅申请吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await axios.put(`/api/borrows/${borrowId}/approve`)
    ElMessage.success('借阅申请已批准')
    
    // 刷新数据
    fetchRecentBorrows()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批准借阅失败:', error)
      ElMessage.error('批准借阅失败，请稍后重试')
    }
  }
}

// 处理拒绝借阅
const handleReject = async (borrowId) => {
  try {
    await ElMessageBox.confirm('确定拒绝这个借阅申请吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.put(`/api/borrows/${borrowId}/reject`)
    ElMessage.success('借阅申请已拒绝')
    
    // 刷新数据
    fetchRecentBorrows()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝借阅失败:', error)
      ElMessage.error('拒绝借阅失败，请稍后重试')
    }
  }
}

// 刷新调试信息
const refreshDebugInfo = () => {
  console.log('刷新调试信息')
  console.log('认证状态:', authStore.isAuthenticated)
  console.log('管理员权限:', authStore.isAdmin)
  console.log('用户信息:', authStore.user)
  console.log('Token:', authStore.token)
}

// 处理退出登录
const handleLogout = () => {
  authStore.logout()
  ElMessage.success('已退出登录')
}

// 处理重新登录
const handleRelogin = () => {
  router.push('/login')
}

// 页面加载时获取数据
onMounted(() => {
  // 检查用户是否有管理员权限
  const authStore = useAuthStore()
  if (!authStore.isAuthenticated) {
    console.error('用户未登录，无法访问管理员仪表盘')
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  
  if (!authStore.isAdmin) {
    console.error('用户没有管理员权限，无法访问管理员仪表盘')
    ElMessage.error('您没有权限访问此页面')
    router.push('/')
    return
  }
  
  console.log('当前用户信息:', authStore.user)
  console.log('当前token:', authStore.token)
  
  fetchStats()
  fetchRecentBorrows()
  fetchOverdueBorrows()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #303133;
}

.admin-nav {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.admin-nav-item {
  text-decoration: none;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.stat-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}

.icon {
  font-size: 40px;
  color: #909399;
}

.recent-section {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

@media (min-width: 1200px) {
  .recent-section {
    grid-template-columns: 1fr 1fr;
  }
}

.recent-card {
  margin-bottom: 20px;
}

.view-all {
  font-size: 14px;
  color: #409EFF;
  text-decoration: none;
}

.view-all:hover {
  text-decoration: underline;
}

.debug-card {
  margin-bottom: 20px;
  background-color: #f8f8f8;
}

.debug-info {
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}

.debug-info p {
  margin: 5px 0;
}
</style> 