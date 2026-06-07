/**
 * 认证状态管理（AuthStore）
 * - State: 登录状态、用户名、加载状态
 * - Actions: login() / logout() / checkSession()
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as loginApi, checkLogin as checkLoginApi, logout as logoutApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const isLoggedIn = ref(false)
  const username = ref('')
  const loading = ref(false)
  const router = useRouter()

  let _sessionChecked = false

  /** 登录 */
  async function login(credentials) {
    if (!credentials.username || !credentials.password) {
      ElMessage.warning('请输入用户名和密码')
      return false
    }
    loading.value = true
    try {
      const res = await loginApi(credentials)
      if (res.isLogin) {
        isLoggedIn.value = true
        username.value = credentials.username
        _sessionChecked = true
        ElMessage.success('登录成功')
        router.push('/dashboard')
        return true
      } else {
        ElMessage.error('登录失败')
        return false
      }
    } finally {
      loading.value = false
    }
  }

  /** 登出 */
  async function logout() {
    try {
      await logoutApi()
    } finally {
      isLoggedIn.value = false
      username.value = ''
      _sessionChecked = false
      router.push('/login')
    }
  }

  /** 校验当前会话是否有效（幂等，避免重复请求） */
  async function checkSession() {
    if (_sessionChecked && isLoggedIn.value) return true
    try {
      const res = await checkLoginApi()
      isLoggedIn.value = !!res.isLogin
      if (!isLoggedIn.value) _sessionChecked = false
      else _sessionChecked = true
      return isLoggedIn.value
    } catch {
      isLoggedIn.value = false
      _sessionChecked = false
      return false
    }
  }

  /** 强制清除认证状态（用于 Axios 拦截器等外部调用） */
  function forceLogout() {
    isLoggedIn.value = false
    username.value = ''
    _sessionChecked = false
    router.push('/login')
  }

  return { isLoggedIn, username, loading, login, logout, checkSession, forceLogout }
})
