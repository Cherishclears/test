<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(true)
const books = ref([])
const searchKeyword = ref('')

onMounted(async () => {
  await fetchBooks()
})

const fetchBooks = async () => {
  loading.value = true
  try {
    const response = await api.books.getAll(0, 6)
    books.value = response.data.data.content
  } catch (error) {
    console.error('Error fetching books:', error)
    ElMessage.error('获取图书列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/books',
      query: { keyword: searchKeyword.value }
    })
  }
}

const viewBookDetail = (id) => {
  router.push(`/books/${id}`)
}

const viewAllBooks = () => {
  router.push('/books')
}
</script>

<template>
  <div class="home-container">
    <section class="hero-section">
      <div class="hero-content">
        <h1>欢迎使用图书馆管理系统</h1>
        <p>探索丰富的图书资源，享受便捷的借阅服务</p>
        
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索图书..."
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><el-icon-search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </section>
    
    <section class="featured-books">
      <div class="section-header">
        <h2>精选图书</h2>
        <el-button type="primary" text @click="viewAllBooks">查看全部</el-button>
      </div>
      
      <el-row :gutter="20">
        <el-col
          v-for="book in books"
          :key="book.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="8"
          :xl="8"
        >
          <el-card class="book-card" @click="viewBookDetail(book.id)">
            <div class="book-cover">
              <img :src="book.cover || '/default-book-cover.png'" :alt="book.title" />
            </div>
            <div class="book-info">
              <h3 class="book-title">{{ book.title }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-category">分类：{{ book.category }}</p>
              <p class="book-status" :class="{ 'available': book.status === 'AVAILABLE' }">
                {{ book.status === 'AVAILABLE' ? '可借阅' : '已借出' }}
              </p>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="books.length === 0 && !loading" description="暂无图书" />
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
    </section>
    
    <section class="features-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <div class="feature-card">
            <el-icon><el-icon-collection /></el-icon>
            <h3>丰富的图书资源</h3>
            <p>提供各类图书资源，满足不同读者的阅读需求</p>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <div class="feature-card">
            <el-icon><el-icon-reading /></el-icon>
            <h3>便捷的借阅服务</h3>
            <p>简化借阅流程，提供在线借阅和归还服务</p>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <div class="feature-card">
            <el-icon><el-icon-user /></el-icon>
            <h3>个性化用户体验</h3>
            <p>根据用户借阅历史和偏好，推荐相关图书</p>
          </div>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<style scoped>
.home-container {
  padding-bottom: 40px;
}

.hero-section {
  background-color: #409EFF;
  color: white;
  padding: 60px 20px;
  text-align: center;
  margin-bottom: 40px;
  border-radius: 8px;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-content h1 {
  font-size: 2.5rem;
  margin-bottom: 20px;
}

.hero-content p {
  font-size: 1.2rem;
  margin-bottom: 30px;
}

.search-box {
  max-width: 500px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.book-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
}

.book-card:hover {
  transform: translateY(-5px);
}

.book-cover {
  height: 200px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
}

.book-cover img {
  max-height: 100%;
  max-width: 100%;
  object-fit: contain;
}

.book-info {
  padding: 0 10px;
}

.book-title {
  font-size: 1.1rem;
  margin: 0 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author, .book-category {
  color: #666;
  margin: 5px 0;
  font-size: 0.9rem;
}

.book-status {
  color: #f56c6c;
  font-weight: bold;
  margin: 10px 0 0;
}

.book-status.available {
  color: #67c23a;
}

.loading-container {
  padding: 20px;
}

.features-section {
  margin-top: 60px;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
  height: 100%;
}

.feature-card .el-icon {
  font-size: 3rem;
  color: #409EFF;
  margin-bottom: 20px;
}

.feature-card h3 {
  margin: 0 0 15px;
  font-size: 1.3rem;
}

.feature-card p {
  color: #666;
  margin: 0;
}
</style> 