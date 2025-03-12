<template>
  <div class="book-manage">
    <div class="page-header">
      <h1 class="page-title">图书管理</h1>
      <el-button type="primary" @click="openAddDialog">添加图书</el-button>
    </div>
    
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
    
    <el-table
      :data="books"
      style="width: 100%"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="author" label="作者" width="150" />
      <el-table-column prop="isbn" label="ISBN" width="150" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="publisher" label="出版社" width="150" />
      <el-table-column prop="publishDate" label="出版日期" width="120">
        <template #default="scope">
          {{ formatDate(scope.row.publishDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : 'danger'">
            {{ scope.row.status === 'AVAILABLE' ? '可借阅' : '已借出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="库存" width="120">
        <template #default="scope">
          {{ scope.row.availableCopies }}/{{ scope.row.totalCopies }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalBooks"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 添加/编辑图书对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑图书' : '添加图书'"
      width="60%"
    >
      <el-form
        ref="bookFormRef"
        :model="bookForm"
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" :disabled="isEdit" />
        </el-form-item>
        
        <el-form-item label="标题" prop="title">
          <el-input v-model="bookForm.title" />
        </el-form-item>
        
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" />
        </el-form-item>
        
        <el-form-item label="分类" prop="category">
          <el-select v-model="bookForm.category" placeholder="选择分类">
            <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="bookForm.publisher" />
        </el-form-item>
        
        <el-form-item label="出版日期" prop="publishDate">
          <el-date-picker
            v-model="bookForm.publishDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="封面URL" prop="cover">
          <div class="cover-upload">
            <el-input v-model="bookForm.cover" placeholder="输入图片URL或上传图片" />
            <el-upload
              class="cover-uploader"
              action="#"
              :http-request="uploadCover"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary">上传图片</el-button>
            </el-upload>
          </div>
          <div v-if="bookForm.cover" class="cover-preview">
            <img :src="bookForm.cover" alt="封面预览" />
          </div>
        </el-form-item>
        
        <el-form-item label="馆藏位置" prop="location">
          <el-input v-model="bookForm.location" />
        </el-form-item>
        
        <el-form-item label="总数量" prop="totalCopies">
          <el-input-number v-model="bookForm.totalCopies" :min="1" />
        </el-form-item>
        
        <el-form-item label="可借数量" prop="availableCopies">
          <el-input-number 
            v-model="bookForm.availableCopies" 
            :min="0" 
            :max="bookForm.totalCopies" 
          />
        </el-form-item>
        
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="bookForm.description"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '保存修改' : '添加图书' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

const books = ref([])
const totalBooks = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const categoryFilter = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const bookForm = ref({
  id: null,
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishDate: '',
  category: '',
  description: '',
  cover: '',
  location: '',
  status: 'AVAILABLE',
  totalCopies: 1,
  availableCopies: 1
})
const bookFormRef = ref(null)

// 分类列表
const categories = ref([
  '古典文学', '当代文学', '外国文学', '计算机', '科学', '历史', '哲学', '艺术', '经济', '教育'
])

// 表单验证规则
const rules = {
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { pattern: /^[0-9-]{10,17}$/, message: 'ISBN格式不正确', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  publisher: [
    { required: true, message: '请输入出版社', trigger: 'blur' }
  ],
  totalCopies: [
    { required: true, message: '请输入总数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '总数量必须大于0', trigger: 'blur' }
  ],
  availableCopies: [
    { required: true, message: '请输入可借数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '可借数量不能小于0', trigger: 'blur' }
  ]
}

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

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString()
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

// 打开添加对话框
const openAddDialog = () => {
  isEdit.value = false
  bookForm.value = {
    id: null,
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: '',
    category: '',
    description: '',
    cover: '',
    location: '',
    status: 'AVAILABLE',
    totalCopies: 1,
    availableCopies: 1
  }
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  isEdit.value = true
  bookForm.value = { ...row }
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除《${row.title}》吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`/api/books/${row.id}`)
    ElMessage.success('图书删除成功')
    fetchBooks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除图书失败:', error)
      ElMessage.error('删除图书失败，请稍后重试')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!bookFormRef.value) return
  
  await bookFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑图书
        await axios.put(`/api/books/${bookForm.value.id}`, bookForm.value)
        ElMessage.success('图书更新成功')
      } else {
        // 添加图书
        await axios.post('/api/books', bookForm.value)
        ElMessage.success('图书添加成功')
      }
      
      dialogVisible.value = false
      fetchBooks()
    } catch (error) {
      console.error(isEdit.value ? '更新图书失败:' : '添加图书失败:', error)
      ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新图书失败' : '添加图书失败'))
    } finally {
      submitting.value = false
    }
  })
}

// 上传图书封面
const uploadCover = async (options) => {
  const { file } = options
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  
  // 检查文件大小（限制为5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await axios.post('/api/books/upload-cover', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.success) {
      bookForm.value.cover = response.data.data
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error(response.data.message || '封面上传失败')
    }
  } catch (error) {
    console.error('上传封面失败:', error)
    ElMessage.error('封面上传失败，请稍后重试')
  }
}

// 页面加载时获取图书列表
onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.book-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.cover-upload {
  display: flex;
  gap: 10px;
  align-items: center;
}

.cover-preview {
  margin-top: 10px;
  max-width: 200px;
}

.cover-preview img {
  width: 100%;
  height: auto;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
</style> 