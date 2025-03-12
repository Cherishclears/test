<template>
  <div class="borrow-history-container">
    <h1 class="page-title">我的借阅</h1>
    
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部借阅" name="all"></el-tab-pane>
      <el-tab-pane label="待审核" name="PENDING"></el-tab-pane>
      <el-tab-pane label="已批准" name="APPROVED"></el-tab-pane>
      <el-tab-pane label="已拒绝" name="REJECTED"></el-tab-pane>
      <el-tab-pane label="已归还" name="RETURNED"></el-tab-pane>
      <el-tab-pane label="已逾期" name="OVERDUE"></el-tab-pane>
    </el-tabs>
    
    <div class="borrow-list">
      <el-empty v-if="borrows.length === 0" description="暂无借阅记录" />
      
      <el-card v-for="borrow in borrows" :key="borrow.id" class="borrow-card">
        <div class="borrow-info">
          <div class="book-info">
            <h3 class="book-title" @click="viewBookDetail(borrow.bookId)">
              {{ borrow.bookTitle }}
            </h3>
            <p class="book-isbn">ISBN: {{ borrow.bookIsbn }}</p>
          </div>
          
          <div class="borrow-dates">
            <p>
              <span class="date-label">借阅日期:</span> 
              {{ formatDate(borrow.borrowDate) }}
            </p>
            <p>
              <span class="date-label">应还日期:</span> 
              {{ formatDate(borrow.dueDate) }}
            </p>
            <p v-if="borrow.returnDate">
              <span class="date-label">归还日期:</span> 
              {{ formatDate(borrow.returnDate) }}
            </p>
          </div>
          
          <div class="borrow-status">
            <el-tag :type="getStatusType(borrow.status)">
              {{ getStatusText(borrow.status) }}
            </el-tag>
            
            <el-button 
              v-if="borrow.status === 'APPROVED'" 
              type="primary" 
              size="small"
              @click="handleReturn(borrow.id)"
            >
              归还
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalBorrows"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 归还确认对话框 -->
    <el-dialog
      v-model="returnDialogVisible"
      title="确认归还"
      width="30%"
    >
      <p>您确定要归还这本书吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReturn" :loading="returning">
            确认归还
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const borrows = ref([])
const totalBorrows = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const activeTab = ref('all')
const loading = ref(false)
const returnDialogVisible = ref(false)
const returning = ref(false)
const currentBorrowId = ref(null)

// 获取借阅记录
const fetchBorrows = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: activeTab.value === 'all' ? null : activeTab.value
    }
    
    const response = await axios.get('/api/borrows/current', { params })
    borrows.value = response.data.data.content
    totalBorrows.value = response.data.data.totalElements
  } catch (error) {
    console.error('获取借阅记录失败:', error)
    ElMessage.error('获取借阅记录失败，请稍后重试')
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

// 处理标签页切换
const handleTabClick = () => {
  currentPage.value = 1
  fetchBorrows()
}

// 改变每页显示数量
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchBorrows()
}

// 改变页码
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchBorrows()
}

// 查看图书详情
const viewBookDetail = (bookId) => {
  router.push({ name: 'BookDetail', params: { id: bookId } })
}

// 处理归还
const handleReturn = (borrowId) => {
  currentBorrowId.value = borrowId
  returnDialogVisible.value = true
}

// 确认归还
const confirmReturn = async () => {
  if (!currentBorrowId.value) return
  
  returning.value = true
  try {
    await axios.put(`/api/borrows/${currentBorrowId.value}/return`)
    
    ElMessage.success('图书归还申请已提交')
    returnDialogVisible.value = false
    
    // 刷新借阅列表
    fetchBorrows()
  } catch (error) {
    console.error('归还图书失败:', error)
    ElMessage.error(error.response?.data?.message || '归还图书失败，请稍后重试')
  } finally {
    returning.value = false
  }
}

// 页面加载时获取借阅记录
onMounted(() => {
  fetchBorrows()
})
</script>

<style scoped>
.borrow-history-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}

.borrow-list {
  margin: 20px 0;
}

.borrow-card {
  margin-bottom: 15px;
}

.borrow-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

@media (max-width: 768px) {
  .borrow-info {
    flex-direction: column;
    align-items: flex-start;
  }
}

.book-info {
  flex: 1;
  min-width: 200px;
}

.book-title {
  margin: 0 0 5px;
  font-size: 16px;
  color: #409EFF;
  cursor: pointer;
}

.book-title:hover {
  text-decoration: underline;
}

.book-isbn {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.borrow-dates {
  flex: 1;
  min-width: 200px;
}

.borrow-dates p {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.date-label {
  font-weight: bold;
  color: #303133;
}

.borrow-status {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style> 