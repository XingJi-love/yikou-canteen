<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="admin-aside">
      <div class="logo">
        <img class="logo-img" src="/logo.jpg" alt="logo" />
        <span v-show="!collapsed">一口食堂</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        background-color="#2c3e50"
        text-color="#bfcbd9"
        active-text-color="#ff9c35"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/orders/pending">
          <el-icon><Bell /></el-icon>
          <template #title>待取餐</template>
        </el-menu-item>
        <el-menu-item index="/foods">
          <el-icon><Goods /></el-icon>
          <template #title>菜品管理</template>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>客户管理</template>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
        <el-menu-item index="/ai">
          <el-icon><MagicStick /></el-icon>
          <template #title>AI 助手</template>
        </el-menu-item>
        <el-menu-item index="/password">
          <el-icon><Lock /></el-icon>
          <template #title>修改密码</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="20"
            @click="collapsed = !collapsed"
          >
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <span class="header-title">{{ route.meta.title || '首页' }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleLogout">
            <div class="user-dropdown">
              <el-avatar :size="28" icon="UserFilled" />
              <span>{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
  import {computed, onMounted, ref} from 'vue'
  import {useRoute} from 'vue-router'
  import {checkLogin} from '@/api'
  import {useAuthStore} from '@/stores/auth'
  // Element Plus 图标组件
  import {
    Odometer,
    Document,
    Bell,
    Goods,
    Menu as MenuIcon,
    User,
    Setting,
    Lock,
    Fold,
    Expand,
    ArrowDown,
    UserFilled,
    MagicStick
  } from '@element-plus/icons-vue'

  const route = useRoute()
  const authStore = useAuthStore()
  const username = ref('admin')
  const collapsed = ref(false)

  const activeMenu = computed(() => route.path)

  // 初始化加载时获取当前管理员用户名
  onMounted(async () => {
    try {
      const res = await checkLogin()
      if (res.username) username.value = res.username
    } catch {
      /* ignore */
    }
  })

  /** 退出登录：通过 AuthStore 统一处理 */
  const handleLogout = () => {
    authStore.logout()
  }
</script>

<style scoped>
  .admin-layout {
    height: 100vh;
  }

  .admin-aside {
    background: #2c3e50;
    transition: width 0.2s;
    overflow: hidden;
  }

  .logo {
    height: 60px;
    line-height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #fff;
    font-size: 20px;
    font-weight: 700;
    background: rgba(0, 0, 0, 0.15);
  }

  .logo-img {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    object-fit: contain;
  }

  .logo-text-short {
    font-size: 16px;
  }

  .admin-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    border-bottom: 1px solid #ebeef5;
    padding: 0 20px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .header-right {
    display: flex;
    align-items: center;
  }

  .user-dropdown {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    color: #606266;
  }

  .admin-main {
    padding: 20px;
    background: #f5f7fa;
  }

  .el-menu {
    border-right: none;
  }
</style>
