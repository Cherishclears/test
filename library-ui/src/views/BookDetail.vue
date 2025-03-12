<template>
  <div class="book-detail-container">
    <el-card v-if="book" class="book-detail-card">
      <div class="book-detail-content">
        <div class="book-cover">
          <img :src="book.cover || '/default-book-cover.jpg'" :alt="book.title">
        </div>
        
        <div class="book-info">
          <h1 class="book-title">{{ book.title }}</h1>
          
          <div class="book-meta">
            <p><strong>作者：</strong>{{ book.author }}</p>
            <p><strong>ISBN：</strong>{{ book.isbn }}</p>
            <p><strong>出版社：</strong>{{ book.publisher }}</p>
            <p><strong>出版日期：</strong>{{ formatDate(book.publishDate) }}</p>
            <p><strong>分类：</strong>{{ book.category }}</p>
            <p><strong>馆藏位置：</strong>{{ book.location }}</p>
            <p><strong>状态：</strong>
              <el-tag :type="book.status === 'AVAILABLE' ? 'success' : 'danger'">
                {{ book.status === 'AVAILABLE' ? '可借阅' : '已借出' }}
              </el-tag>
            </p>
            <p><strong>可借阅数量：</strong>{{ book.availableCopies }}/{{ book.totalCopies }}</p>
          </div>
          
          <div class="book-actions">
            <el-button 
              type="primary" 
              :disabled="!canBorrow || !isAuthenticated" 
              @click="handleBorrow"
            >
              借阅
            </el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
          
          <div v-if="!isAuthenticated" class="login-tip">
            请<router-link to="/login">登录</router-link>后借阅图书
          </div>
        </div>
      </div>
      
      <div class="book-description">
        <h2>图书简介</h2>
        <p>{{ book.description }}</p>
      </div>
    </el-card>
    
    <el-skeleton v-else :rows="10" animated />
    
    <!-- 借阅确认对话框 -->
    <el-dialog
      v-model="borrowDialogVisible"
      title="确认借阅"
      width="30%"
    >
      <p>您确定要借阅《{{ book?.title }}》吗？</p>
      <p>借阅期限：{{ borrowDays }}天</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="borrowDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBorrow" :loading="borrowing">
            确认借阅
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const bookId = route.params.id
const book = ref(null)
const borrowDialogVisible = ref(false)
const borrowing = ref(false)
const borrowDays = 30 // 默认借阅天数

// 计算属性：是否可以借阅
const canBorrow = computed(() => {
  return book.value && book.value.status === 'AVAILABLE' && book.value.availableCopies > 0
})

// 计算属性：是否已登录
const isAuthenticated = computed(() => {
  return authStore.isAuthenticated
})

// 获取图书详情
const fetchBookDetail = async () => {
  try {
    const response = await axios.get(`/api/books/${bookId}`)
    book.value = response.data.data
  } catch (error) {
    console.error('获取图书详情失败:', error)
    ElMessage.error('获取图书详情失败，请稍后重试')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理借阅
const handleBorrow = () => {
  if (!isAuthenticated.value) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  
  if (!canBorrow.value) {
    ElMessage.warning('该图书当前无法借阅')
    return
  }
  
  borrowDialogVisible.value = true
}

// 确认借阅
const confirmBorrow = async () => {
  borrowing.value = true
  try {
    const response = await axios.post('/api/borrows', {
      bookId: book.value.id
    })
    
    ElMessage.success('借阅申请已提交，请等待管理员审核')
    borrowDialogVisible.value = false
    
    // 更新图书状态
    await fetchBookDetail()
  } catch (error) {
    console.error('借阅失败:', error)
    ElMessage.error(error.response?.data?.message || '借阅失败，请稍后重试')
  } finally {
    borrowing.value = false
  }
}

// 页面加载时获取图书详情
onMounted(() => {
  fetchBookDetail()
})
</script>

<style scoped>
.book-detail-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}

.book-detail-card {
  margin-bottom: 30px;
}

.book-detail-content {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

@media (max-width: 768px) {
  .book-detail-content {
    flex-direction: column;
  }
}

.book-cover {
  flex: 0 0 250px;
}

.book-cover img {
  width: 100%;
  height: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.book-info {
  flex: 1;
}

.book-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}

.book-meta p {
  margin: 10px 0;
  color: #606266;
}

.book-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
}

.login-tip {
  margin-top: 15px;
  font-size: 14px;
  color: #909399;
}

.login-tip a {
  color: #409EFF;
  text-decoration: none;
}

.book-description {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

.book-description h2 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #303133;
}

.book-description p {
  line-height: 1.6;
  color: #606266;
  white-space: pre-line;
}
</style> 