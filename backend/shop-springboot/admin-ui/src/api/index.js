/**
 * 管理后台 API 函数集合
 * 封装所有后端接口调用，统一使用 request 实例
 */
import request from './request'

// ==================== 认证 ====================
export const checkLogin = () => request.get('/checkLogin')
export const login = (data) => request.post('/login', data)
export const logout = () => request.post('/logout')
export const changePassword = (password) => request.post('/password', { password })

// ==================== 系统设置 ====================
export const getSettings = () => request.get('/settings')
export const saveSettings = (data) => request.post('/settings', data)

// ==================== 分类管理 ====================
export const getCategories = () => request.get('/categories')
export const addCategory = (data) => request.post('/categories', data)
export const updateCategory = (id, data) => request.put(`/categories/${id}`, data)
export const sortCategories = (data) => request.post('/categories/sort', data)
export const deleteCategory = (id) => request.delete(`/categories/${id}`)

// ==================== 菜品管理 ====================
export const getFoods = (params) => request.get('/foods', { params })
export const getFood = (id) => request.get(`/foods/${id}`)
export const createFood = (data) => request.post('/foods', data)
export const updateFood = (id, data) => request.put(`/foods/${id}`, data)
export const deleteFood = (id, recycle = false) => request.delete(`/foods/${id}`, { params: { recycle: recycle ? 1 : 0 } })

// ==================== 订单管理 ====================
export const getOrders = (params) => request.get('/orders', { params })
export const setOrderTaken = (id, taken) => request.post(`/orders/${id}/taken`, { taken })

// ==================== 用户管理 ====================
export const getUsers = (params) => request.get('/users', { params })
export const getUserById = (id) => request.get(`/users/${id}`)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)

// ==================== 文件上传 ====================

/** 本地上传文件 */
export const uploadFile = (file, relation, relationId) => {
  const form = new FormData()
  form.append('file', file)
  if (relation) form.append('relation', relation)
  if (relationId) form.append('relation_id', relationId)
  return request.post('/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 删除上传文件 */
export const deleteUpload = (relationId) => request.delete('/upload', { params: { relation_id: relationId } })

/** 通过网络 URL 下载图片并保存到本地 */
export const uploadFromUrl = (url, relation, relationId) => {
  const form = new FormData()
  form.append('url', url)
  if (relation) form.append('relation', relation)
  if (relationId) form.append('relation_id', relationId)
  return request.post('/upload/url', form)
}

/** 将相对路径补充为完整可访问图片 URL */
export const uploadUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/static/')) return path
  return `/static/uploads/${path}`
}

// ==================== 统计 ====================
export const getStatisticsOverview = () => request.get('/statistics/overview')
export const getStatisticsOrderTrend = (days = 7) => request.get('/statistics/order-trend', { params: { days } })
export const getStatisticsRevenue = (days = 7) => request.get('/statistics/revenue', { params: { days } })
export const getStatisticsCategoryDistribution = () => request.get('/statistics/category-distribution')
export const getStatisticsTopFoods = (limit = 10, days = 30) => request.get('/statistics/top-foods', { params: { limit, days } })
export const getStatisticsOrderStatus = () => request.get('/statistics/order-status')

// ==================== AI 助手 ====================

/** 获取菜品列表（供下拉选择） */
export const getAiFoods = () => request.get('/ai/foods')
/** AI 接口超时时间：120 秒（大模型生成内容较慢） */
const AI_TIMEOUT = 120000

/** 菜品描述生成 */
export const generateFoodDescription = (foodName, category, price) =>
  request.post('/ai/food-description', { foodName, category, price }, { timeout: AI_TIMEOUT })
/** 每日经营简报 */
export const generateDailyBrief = (date) =>
  request.post('/ai/daily-brief', { date }, { timeout: AI_TIMEOUT })
/** 智能套餐推荐 */
export const generateComboRecommend = (days) =>
  request.post('/ai/combo-recommend', { days }, { timeout: AI_TIMEOUT })
/** 营销活动参谋 */
export const generateMarketingAdvice = () =>
  request.post('/ai/marketing-advice', {}, { timeout: AI_TIMEOUT })
