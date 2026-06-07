<script setup>
import {reactive} from 'vue'
import {useAuthStore} from '@/stores/auth'

const authStore = useAuthStore()
const form = reactive({
  username: '',
  password: ''
})
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <img src="/logo.jpg" alt="Logo" class="login-logo"/>
      <h1>一口食堂管理中心</h1>
      <p class="subtitle">点餐系统后台管理</p>
      <el-form @submit.prevent="authStore.login(form)">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large"/>
        </el-form-item>
        <el-form-item>
          <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="authStore.login(form)"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="authStore.loading"
                   @click="authStore.login(form)">
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('/login-bj.jpg') center / cover no-repeat;
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.login-logo {
  display: block;
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  object-fit: contain;
  border-radius: 8px;
}

h1 {
  margin: 0 0 8px;
  text-align: center;
  color: #303133;
  font-size: 24px;
}

.subtitle {
  margin: 0 0 32px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
}
</style>
