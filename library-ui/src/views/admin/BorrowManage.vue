<template>
  <div class="borrow-manage">
    <div class="page-header">
      <h1 class="page-title">借阅管理</h1>
    </div>
    
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索借阅（用户名、图书标题）"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已批准" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
        <el-option label="已归还" value="RETURNED" />
        <el-option label="已逾期" value="OVERDUE" />
      </el-select>
    </div>
    
    <el-table
      :data="borrows"
      style="width: 100%"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userRealName" label="用户" width="120" />
      <el-table-column prop="bookTitle" label="图书" min-width="200" />
      <el-table-column prop="bookIsbn" label="ISBN" width="150" />
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
      <el-table-column prop="returnDate" label="归还日期" width="120">
        <template #default="scope">
          {{ formatDate(scope.row.returnDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
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
          <el-button 
            v-if="scope.row.status === 'APPROVED'" 
            type="primary" 
            size="small"
            @click="handleReturn(scope.row.id)"
          >
            归还
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
        :total="totalBorrows"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

const borrows = ref([])
const totalBorrows = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const statusFilter = ref('')
const loading = ref(false)

// 获取借阅列表
const fetchBorrows = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value,
      status: statusFilter.value
    }
    
    const response = await axios.get('/api/admin/borrows', { params })
    borrows.value = response.data.data.content
    totalBorrows.value = response.data.data.totalElements
  } catch (error) {
    console.error('获取借阅列表失败:', error)
    ElMessage.error('获取借阅列表失败，请稍后重试')
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

// 搜索借阅
const handleSearch = () => {
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
    fetchBorrows()
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
    fetchBorrows()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝借阅失败:', error)
      ElMessage.error('拒绝借阅失败，请稍后重试')
    }
  }
}

// 处理归还图书
const handleReturn = async (borrowId) => {
  try {
    await ElMessageBox.confirm('确定将这本书标记为已归还吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await axios.put(`/api/borrows/${borrowId}/return`)
    ElMessage.success('图书已标记为归还')
    fetchBorrows()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('归还图书失败:', error)
      ElMessage.error('归还图书失败，请稍后重试')
    }
  }
}

// 页面加载时获取借阅列表
onMounted(() => {
  fetchBorrows()
})
</script>

<style scoped>
.borrow-manage {
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
</style> 