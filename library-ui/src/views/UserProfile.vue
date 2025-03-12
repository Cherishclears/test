<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
        </div>
      </template>
      
      <el-form 
        ref="profileForm"
        :model="profileData"
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileData.username" />
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input v-model="profileData.name" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileData.email" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileData.phone" />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-tag :type="profileData.role === 'ADMIN' ? 'danger' : 'success'">
            {{ profileData.role === 'ADMIN' ? '管理员' : '读者' }}
          </el-tag>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="updateProfile" :loading="updating">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <h2>修改密码</h2>
        </div>
      </template>
      
      <el-form 
        ref="passwordForm"
        :model="passwordData"
        :rules="passwordRules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input 
            v-model="passwordData.oldPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordData.newPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordData.confirmPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="updatePassword" :loading="updatingPassword">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import axios from 'axios'

const authStore = useAuthStore()
const profileForm = ref(null)
const passwordForm = ref(null)
const updating = ref(false)
const updatingPassword = ref(false)

// 个人资料数据
const profileData = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  role: ''
})

// 密码修改数据
const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
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
  ]
}

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordData.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const response = await axios.get('/api/users/profile')
    const userData = response.data.data
    
    profileData.username = userData.username
    profileData.name = userData.name
    profileData.email = userData.email
    profileData.phone = userData.phone
    profileData.role = userData.role
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败，请稍后重试')
  }
}

// 更新个人资料
const updateProfile = async () => {
  await profileForm.value.validate(async (valid) => {
    if (!valid) return
    
    updating.value = true
    try {
      const response = await axios.put('/api/users/profile', {
        username: profileData.username,
        name: profileData.name,
        email: profileData.email,
        phone: profileData.phone
      })
      
      ElMessage.success('个人资料更新成功')
      
      // 更新存储的用户信息
      authStore.updateUserInfo({
        username: profileData.username,
        name: profileData.name
      })
    } catch (error) {
      console.error('更新个人资料失败:', error)
      ElMessage.error(error.response?.data?.message || '更新个人资料失败，请稍后重试')
    } finally {
      updating.value = false
    }
  })
}

// 修改密码
const updatePassword = async () => {
  await passwordForm.value.validate(async (valid) => {
    if (!valid) return
    
    updatingPassword.value = true
    try {
      const response = await axios.put('/api/users/password', {
        oldPassword: passwordData.oldPassword,
        newPassword: passwordData.newPassword
      })
      
      ElMessage.success('密码修改成功')
      
      // 清空表单
      passwordData.oldPassword = ''
      passwordData.newPassword = ''
      passwordData.confirmPassword = ''
      passwordForm.value.resetFields()
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.response?.data?.message || '修改密码失败，请稍后重试')
    } finally {
      updatingPassword.value = false
    }
  })
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card, .password-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 0 10px;
  }
}
</style> 