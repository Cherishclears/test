<template>
  <div class="user-manage">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <el-button type="primary" @click="openAddDialog">添加用户</el-button>
    </div>
    
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户（用户名、姓名、邮箱）"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <el-select v-model="roleFilter" placeholder="角色筛选" clearable @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="管理员" value="ADMIN" />
        <el-option label="读者" value="READER" />
      </el-select>
      
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="激活" value="ACTIVE" />
        <el-option label="禁用" value="INACTIVE" />
      </el-select>
    </div>
    
    <el-table
      :data="users"
      style="width: 100%"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="name" label="姓名" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'success'">
            {{ scope.row.role === 'ADMIN' ? '管理员' : '读者' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ scope.row.status === 'ACTIVE' ? '激活' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button 
            type="warning" 
            size="small" 
            @click="handleToggleStatus(scope.row)"
          >
            {{ scope.row.status === 'ACTIVE' ? '禁用' : '激活' }}
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDelete(scope.row)"
            :disabled="scope.row.role === 'ADMIN' && scope.row.username === 'admin'"
          >
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
        :total="totalUsers"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="50%"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="读者" value="READER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
          <el-input v-model="userForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '保存修改' : '添加用户' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

const users = ref([])
const totalUsers = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const userForm = ref({
  id: null,
  username: '',
  name: '',
  email: '',
  phone: '',
  role: 'READER',
  status: 'ACTIVE',
  password: '',
  confirmPassword: ''
})
const userFormRef = ref(null)

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (isEdit.value) {
    callback()
  } else if (!value) {
    callback(new Error('请输入密码'))
  } else {
    if (userForm.value.confirmPassword !== '') {
      userFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (isEdit.value) {
    callback()
  } else if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== userForm.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value,
      role: roleFilter.value,
      status: statusFilter.value
    }
    
    const response = await axios.get('/api/admin/users', { params })
    users.value = response.data.data.content
    totalUsers.value = response.data.data.totalElements
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '未知'
  const date = new Date(dateTimeString)
  return date.toLocaleString()
}

// 搜索用户
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 改变每页显示数量
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

// 改变页码
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUsers()
}

// 打开添加对话框
const openAddDialog = () => {
  isEdit.value = false
  userForm.value = {
    id: null,
    username: '',
    name: '',
    email: '',
    phone: '',
    role: 'READER',
    status: 'ACTIVE',
    password: '',
    confirmPassword: ''
  }
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  isEdit.value = true
  userForm.value = { 
    ...row,
    password: '',
    confirmPassword: ''
  }
  dialogVisible.value = true
}

// 处理切换状态
const handleToggleStatus = async (row) => {
  // 防止禁用超级管理员
  if (row.role === 'ADMIN' && row.username === 'admin' && row.status === 'ACTIVE') {
    ElMessage.warning('不能禁用超级管理员账户')
    return
  }
  
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const actionText = newStatus === 'ACTIVE' ? '激活' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户 ${row.username} 吗？`, '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.put(`/api/admin/users/${row.id}/status`, { status: newStatus })
    ElMessage.success(`用户${actionText}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}用户失败:`, error)
      ElMessage.error(`${actionText}用户失败，请稍后重试`)
    }
  }
}

// 处理删除
const handleDelete = async (row) => {
  // 防止删除超级管理员
  if (row.role === 'ADMIN' && row.username === 'admin') {
    ElMessage.warning('不能删除超级管理员账户')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`/api/admin/users/${row.id}`)
    ElMessage.success('用户删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败，请稍后重试')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑用户
        const { password, confirmPassword, ...userData } = userForm.value
        await axios.put(`/api/admin/users/${userData.id}`, userData)
        ElMessage.success('用户更新成功')
      } else {
        // 添加用户
        const { confirmPassword, ...userData } = userForm.value
        await axios.post('/api/admin/users', userData)
        ElMessage.success('用户添加成功')
      }
      
      dialogVisible.value = false
      fetchUsers()
    } catch (error) {
      console.error(isEdit.value ? '更新用户失败:' : '添加用户失败:', error)
      ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新用户失败' : '添加用户失败'))
    } finally {
      submitting.value = false
    }
  })
}

// 页面加载时获取用户列表
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-manage {
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