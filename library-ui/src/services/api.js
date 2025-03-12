import axios from 'axios'

// 设置基础URL
axios.defaults.baseURL = 'http://localhost:8080'

// 创建API服务
const api = {
  // 图书相关API
  books: {
    // 获取所有图书
    getAll: (page = 0, size = 10, sortBy = 'id', direction = 'asc') => {
      return axios.get(`/api/books?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`)
    },
    
    // 搜索图书
    search: (keyword, page = 0, size = 10) => {
      return axios.get(`/api/books/search?keyword=${keyword}&page=${page}&size=${size}`)
    },
    
    // 按分类获取图书
    getByCategory: (category, page = 0, size = 10) => {
      return axios.get(`/api/books/category/${category}?page=${page}&size=${size}`)
    },
    
    // 按作者获取图书
    getByAuthor: (author, page = 0, size = 10) => {
      return axios.get(`/api/books/author/${author}?page=${page}&size=${size}`)
    },
    
    // 获取可借阅的图书
    getAvailable: () => {
      return axios.get('/api/books/available')
    },
    
    // 获取图书详情
    getById: (id) => {
      return axios.get(`/api/books/${id}`)
    },
    
    // 添加图书（管理员）
    add: (bookData) => {
      return axios.post('/api/books', bookData)
    },
    
    // 更新图书（管理员）
    update: (id, bookData) => {
      return axios.put(`/api/books/${id}`, bookData)
    },
    
    // 删除图书（管理员）
    delete: (id) => {
      return axios.delete(`/api/books/${id}`)
    }
  },
  
  // 用户相关API
  users: {
    // 获取所有用户（管理员）
    getAll: () => {
      return axios.get('/api/users')
    },
    
    // 获取用户详情
    getById: (id) => {
      return axios.get(`/api/users/${id}`)
    },
    
    // 更新用户信息
    update: (id, userData) => {
      return axios.put(`/api/users/${id}`, userData)
    },
    
    // 删除用户（管理员）
    delete: (id) => {
      return axios.delete(`/api/users/${id}`)
    }
  },
  
  // 借阅相关API
  borrows: {
    // 借阅图书
    borrow: (bookId, dueDate) => {
      return axios.post('/api/borrows', { bookId, dueDate })
    },
    
    // 批准借阅（管理员）
    approve: (id) => {
      return axios.put(`/api/borrows/${id}/approve`)
    },
    
    // 拒绝借阅（管理员）
    reject: (id) => {
      return axios.put(`/api/borrows/${id}/reject`)
    },
    
    // 归还图书（管理员）
    return: (id) => {
      return axios.put(`/api/borrows/${id}/return`)
    },
    
    // 获取借阅详情
    getById: (id) => {
      return axios.get(`/api/borrows/${id}`)
    },
    
    // 获取所有借阅记录（管理员）
    getAll: (page = 0, size = 10) => {
      return axios.get(`/api/borrows?page=${page}&size=${size}`)
    },
    
    // 获取用户的借阅记录
    getByUser: (userId, page = 0, size = 10) => {
      return axios.get(`/api/borrows/user/${userId}?page=${page}&size=${size}`)
    },
    
    // 获取图书的借阅记录（管理员）
    getByBook: (bookId, page = 0, size = 10) => {
      return axios.get(`/api/borrows/book/${bookId}?page=${page}&size=${size}`)
    },
    
    // 获取特定状态的借阅记录（管理员）
    getByStatus: (status, page = 0, size = 10) => {
      return axios.get(`/api/borrows/status/${status}?page=${page}&size=${size}`)
    },
    
    // 获取逾期的借阅记录（管理员）
    getOverdue: () => {
      return axios.get('/api/borrows/overdue')
    },
    
    // 获取当前用户的借阅记录
    getCurrent: () => {
      return axios.get('/api/borrows/current')
    }
  }
}

export default api 