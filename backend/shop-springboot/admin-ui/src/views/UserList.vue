/**
* 客户管理页
* 展示微信小程序用户列表，支持双击/点击编辑用户信息（昵称、头像、手机号、性别）
*/
<script setup>
import {onMounted, reactive, ref, computed} from 'vue'
import {ElMessage} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {getUsers, getUserById, updateUser, uploadFile, uploadUrl} from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({page: 1, page_size: 10})

// 编辑弹窗状态
const dialogVisible = ref(false)
const editForm = reactive({id: null, nickname: '', avatarUrl: '', phone: '', gender: 0})
const dialogLoading = ref(false)
const genderOptions = [
  {label: '未知', value: 0},
  {label: '男', value: 1},
  {label: '女', value: 2}
]

/** 分页查询用户列表 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUsers(query)
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

/** 统一格式化时间 */
const formatTime = (val) => {
  if (!val) return '-'
  if (typeof val === 'number') return new Date(val).toLocaleString('zh-CN')
  return String(val).replace('T', ' ').substring(0, 19)
}

/** 性别数字 → 中文 */
const formatGender = (val) => {
  return genderOptions.find(g => g.value === val)?.label ?? '未知'
}

/** 打开编辑弹窗，加载用户详情 */
const handleEdit = async (row) => {
  dialogLoading.value = true
  dialogVisible.value = true
  try {
    const res = await getUserById(row.id)
    editForm.id = res.id
    editForm.nickname = res.nickname || ''
    editForm.avatarUrl = res.avatarUrl || ''
    editForm.phone = res.phone || ''
    editForm.gender = res.gender ?? 0
  } catch (e) {
    ElMessage.error('获取用户信息失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

/** 提交用户信息修改 */
const handleSave = async () => {
  dialogLoading.value = true
  try {
    await updateUser(editForm.id, {
      nickname: editForm.nickname,
      avatarUrl: editForm.avatarUrl,
      phone: editForm.phone,
      gender: editForm.gender
    })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    dialogLoading.value = false
  }
}

/** 本地上传用户头像 */
const handleAvatarUpload = async ({file}) => {
  try {
    const res = await uploadFile(file, 'avatar')
    editForm.avatarUrl = res.path
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

/** 头像路径转完整 URL */
const resolveAvatarUrl = (path) => {
  if (!path) return ''
  return uploadUrl(path)
}

const avatarPreviewUrl = computed(() => resolveAvatarUrl(editForm.avatarUrl))

onMounted(loadData)
</script>

<template>
  <div class="page-card">
    <h2 class="page-title">客户管理</h2>

    <el-table v-loading="loading" :data="list" border stripe @row-dblclick="handleEdit">
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-avatar :size="36" :src="resolveAvatarUrl(row.avatarUrl)" v-if="row.avatarUrl"/>
          <el-avatar :size="36" v-else>用</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" width="120">
        <template #default="{ row }">{{ row.nickname || '-' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column prop="gender" label="性别" width="80" align="center">
        <template #default="{ row }">{{ formatGender(row.gender) }}</template>
      </el-table-column>
      <el-table-column prop="openid" label="OpenID" min-width="200" show-overflow-tooltip/>
      <el-table-column prop="price" label="累计消费" width="110" align="right">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">{{ formatTime(row.create_time) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.page_size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
      />
    </div>

    <!-- 用户信息编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑用户信息" width="480px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="80px" v-loading="dialogLoading">
        <el-form-item label="用户头像">
          <div class="avatar-upload-area">
            <el-avatar :size="64" :src="avatarPreviewUrl" v-if="editForm.avatarUrl"/>
            <div class="avatar-placeholder" v-else>暂无</div>
            <div class="avatar-actions">
              <el-upload :show-file-list="false" :http-request="handleAvatarUpload" accept="image/*">
                <el-button type="primary" size="small" plain>
                  <el-icon>
                    <Plus/>
                  </el-icon>
                  本地上传
                </el-button>
              </el-upload>
              <el-input v-model="editForm.avatarUrl" placeholder="或输入图片URL" size="small"
                        style="width:200px;margin-left:8px;" clearable/>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20"/>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11"/>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave" :loading="dialogLoading">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.el-avatar {
  background-color: #f7982a;
}

.avatar-upload-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-placeholder {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  flex-shrink: 0;
}

.avatar-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
