<template>
  <div class="book-list-container">
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索图书（标题、作者、ISBN）"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <el-select v-model="categoryFilter" placeholder="分类筛选" clearable @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
      </el-select>
    </div>

    <div class="books-grid">
      <el-empty v-if="books.length === 0" description="暂无图书" />
      
      <el-card v-for="book in books" :key="book.id" class="book-card" @click="viewBookDetail(book.id)">
        <div class="book-cover">
          <img :src="book.cover || '/default-book-cover.jpg'" :alt="book.title">
          <div class="book-status" :class="{ 'available': book.status === 'AVAILABLE', 'borrowed': book.status === 'BORROWED' }">
            {{ book.status === 'AVAILABLE' ? '可借阅' : '已借出' }}
          </div>
        </div>
        <div class="book-info">
          <h3 class="book-title">{{ book.title }}</h3>
          <p class="book-author">{{ book.author }}</p>
          <p class="book-category">{{ book.category }}</p>
          <p class="book-copies">可借阅: {{ book.availableCopies }}/{{ book.totalCopies }}</p>
        </div>
      </el-card>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalBooks"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const books = ref([])
const totalBooks = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const searchKeyword = ref('')
const categoryFilter = ref('')
const categories = ref([
  '古典文学', '当代文学', '外国文学', '计算机', '科学', '历史', '哲学', '艺术', '经济', '教育'
])
const loading = ref(false)

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value,
      category: categoryFilter.value
    }
    
    const response = await axios.get('/api/books', { params })
    books.value = response.data.data.content
    totalBooks.value = response.data.data.totalElements
  } catch (error) {
    console.error('获取图书列表失败:', error)
    ElMessage.error('获取图书列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索图书
const handleSearch = () => {
  currentPage.value = 1
  fetchBooks()
}

// 改变每页显示数量
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchBooks()
}

// 改变页码
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchBooks()
}

// 查看图书详情
const viewBookDetail = (bookId) => {
  router.push({ name: 'BookDetail', params: { id: bookId } })
}

// 页面加载时获取图书列表
onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.book-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-section {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.search-input {
  flex: 1;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.book-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  height: 100%;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.book-cover {
  position: relative;
  height: 220px;
  overflow: hidden;
  margin-bottom: 10px;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.book-status.available {
  background-color: #67c23a;
  color: white;
}

.book-status.borrowed {
  background-color: #f56c6c;
  color: white;
}

.book-info {
  padding: 0 10px 10px;
}

.book-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author, .book-category, .book-copies {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style> 