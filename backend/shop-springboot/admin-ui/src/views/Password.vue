/**
 * 修改密码页
 * 输入新密码 → 调用 changePassword API → 登出跳转登录页
 */
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword, logout } from '@/api'

const router = useRouter()
const loading = ref(false)
const password = ref('')

/** 保存新密码：校验长度 → 调用接口 → 自动登出 */
const handleSave = async () => {
  if (!password.value || password.value.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  loading.value = true
  try {
    await changePassword(password.value)
    ElMessage.success('密码修改成功，请重新登录')
    await logout()
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-card">
    <h2 class="page-title">修改密码</h2>
    <el-form label-width="100px" style="max-width: 420px">
      <el-form-item label="新密码">
        <el-input v-model="password" type="password" show-password placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
