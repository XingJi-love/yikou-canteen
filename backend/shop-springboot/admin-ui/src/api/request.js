/**
 * Axios HTTP 请求封装
 * - 统一前缀 /admin/api
 * - 响应拦截器：将业务错误（code=0）转为弹窗提示，
 *   "管理员未登录" 时联动 AuthStore 清除认证状态并跳转登录页
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/admin/api',
  withCredentials: true,
  timeout: 30000
})

// 响应拦截器：后端返回 {code: 0, msg: "..." } 视为业务错误
request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data && data.code === 0) {
      ElMessage.error(data.msg || '请求失败')
      if (data.msg === '管理员未登录') {
        // 动态导入避免循环依赖
        import('@/stores/auth').then(({ useAuthStore }) => {
          useAuthStore().forceLogout()
        })
      }
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
