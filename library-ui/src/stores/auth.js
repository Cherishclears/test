import { defineStore } from 'pinia'
import axios from 'axios'
import router from '../router'

export const useAuthStore = defineStore('auth', {
  state: () => {
    console.log('初始化认证存储')
    const user = localStorage.getItem('user')
    const token = localStorage.getItem('token')
    console.log('从localStorage读取用户信息:', user)
    console.log('从localStorage读取token:', token ? token.substring(0, 20) + '...' : '无')
    
    return {
      user: user ? JSON.parse(user) : null,
      token: token || null
    }
  },
  
  getters: {
    isAuthenticated: (state) => {
      const result = !!state.token
      console.log('检查认证状态:', result)
      return result
    },
    isAdmin: (state) => {
      const result = state.user?.role === 'ADMIN'
      console.log('检查管理员权限:', result, '用户角色:', state.user?.role)
      return result
    }
  },
  
  actions: {
    async login(username, password) {
      try {
        console.log('尝试登录用户:', username)
        
        const response = await axios.post('/api/auth/login', { username, password })
        console.log('登录响应:', response.data)
        
        // 确保从正确的响应结构中获取数据
        const responseData = response.data
        
        // 检查响应是否成功
        if (!responseData.success) {
          console.error('登录失败:', responseData.message)
          return false
        }
        
        const userData = responseData.data
        console.log('用户数据:', userData)
        
        this.user = {
          id: userData.id,
          username: userData.username,
          name: userData.name,
          role: userData.role
        }
        
        this.token = userData.token
        
        console.log('设置用户信息:', this.user)
        console.log('设置token:', this.token ? this.token.substring(0, 20) + '...' : '无')
        
        localStorage.setItem('user', JSON.stringify(this.user))
        localStorage.setItem('token', this.token)
        
        // 设置 axios 默认 headers
        axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
        console.log('设置axios请求头:', `Bearer ${this.token.substring(0, 20)}...`)
        
        return true
      } catch (error) {
        console.error('登录错误:', error)
        console.error('错误详情:', error.response ? error.response.data : '无响应数据')
        console.error('错误状态码:', error.response ? error.response.status : '无状态码')
        return false
      }
    },
    
    async register(userData) {
      try {
        console.log('尝试注册用户:', userData.username)
        
        const response = await axios.post('/api/auth/register', userData)
        console.log('注册响应:', response.data)
        
        return response.data.success
      } catch (error) {
        console.error('注册错误:', error)
        console.error('错误详情:', error.response ? error.response.data : '无响应数据')
        return false
      }
    },
    
    logout() {
      console.log('执行登出操作')
      
      this.user = null
      this.token = null
      
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      
      // 清除 axios 默认 headers
      delete axios.defaults.headers.common['Authorization']
      console.log('清除axios请求头')
      
      router.push('/login')
    },
    
    init() {
      console.log('初始化认证状态')
      
      if (this.token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
        console.log('设置axios请求头:', `Bearer ${this.token.substring(0, 20)}...`)
        
        // 检查认证状态
        this.checkAuth()
      } else {
        console.log('无token，跳过初始化')
      }
    },
    
    async checkAuth() {
      try {
        console.log('检查认证状态')
        
        const response = await axios.get('/api/auth/check')
        console.log('认证检查响应:', response.data)
        
        if (!response.data.success) {
          console.warn('认证已失效，执行登出')
          this.logout()
        }
        
        return response.data.success
      } catch (error) {
        console.error('认证检查错误:', error)
        console.error('错误详情:', error.response ? error.response.data : '无响应数据')
        
        // 如果是401或403错误，执行登出
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
          console.warn('认证已失效，执行登出')
          this.logout()
        }
        
        return false
      }
    }
  }
}) 