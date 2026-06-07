/**
* 菜品编辑/新增页
* 新增时自动创建菜品记录，编辑时回填已有数据
* 支持本地上传和 URL 下载两种图片方式
*/
<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {
  createFood,
  getCategories,
  getFood,
  updateFood,
  uploadFile,
  uploadFromUrl,
  uploadUrl
} from '@/api'

const route = useRoute()
const router = useRouter()
const foodId = computed(() => (route.params.id ? Number(route.params.id) : null))
const isEdit = computed(() => !!foodId.value)

const loading = ref(false)
const saving = ref(false)
const urlDialogVisible = ref(false)
const imageUrlInput = ref('')
const urlUploading = ref(false)
const categories = ref([])
const form = reactive({
  category_id: '',
  name: '',
  price: 0,
  status: 1,
  image_url: ''
})

/** 图片完整预览 URL */
const previewUrl = computed(() => uploadUrl(form.image_url))

/** 加载分类下拉选项 */
const loadCategories = async () => {
  categories.value = await getCategories()
}

/** 编辑模式：回填菜品已有数据 */
const loadFood = async () => {
  if (!foodId.value) return
  loading.value = true
  try {
    const data = await getFood(foodId.value)
    form.category_id = data.category_id
    form.name = data.name
    form.price = Number(data.price)
    form.status = data.status
    form.image_url = data.image_url || ''
  } finally {
    loading.value = false
  }
}

/** 本地上传图片 */
const handleUpload = async ({file}) => {
  const res = await uploadFile(file, 'food', foodId.value || undefined)
  form.image_url = res.path
  if (!foodId.value && res.relation_id) {
    router.replace(`/foods/edit/${res.relation_id}`)
  }
  ElMessage.success('上传成功')
}

/** 通过 URL 下载并保存图片 */
const handleUploadFromUrl = async () => {
  const url = imageUrlInput.value.trim()
  if (!url) {
    ElMessage.warning('请输入图片URL')
    return
  }
  if (!url.startsWith('http')) {
    ElMessage.warning('URL格式不正确，需以 http:// 或 https:// 开头')
    return
  }
  urlUploading.value = true
  try {
    const res = await uploadFromUrl(url, 'food', foodId.value || undefined)
    form.image_url = res.path
    if (!foodId.value && res.relation_id) {
      router.replace(`/foods/edit/${res.relation_id}`)
    }
    urlDialogVisible.value = false
    imageUrlInput.value = ''
    ElMessage.success('下载成功')
  } finally {
    urlUploading.value = false
  }
}

/** 创建或更新菜品 */
const handleSave = async () => {
  if (!form.category_id || !form.name) {
    ElMessage.warning('请填写分类和菜品名称')
    return
  }
  saving.value = true
  try {
    const payload = {...form}
    if (isEdit.value) {
      await updateFood(foodId.value, payload)
      ElMessage.success('保存成功')
    } else {
      const res = await createFood(payload)
      ElMessage.success('添加成功')
      if (res.id) {
        router.replace(`/foods/edit/${res.id}`)
      } else {
        router.push('/foods')
      }
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await loadFood()
})
</script>

<template>
  <div class="page-card" v-loading="loading">
    <h2 class="page-title">{{ isEdit ? '编辑菜品' : '新增菜品' }}</h2>

    <el-form label-width="100px" style="max-width: 560px">
      <el-form-item label="分类" required>
        <el-select v-model="form.category_id" placeholder="选择分类" style="width: 100%">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="名称" required>
        <el-input v-model="form.name" placeholder="菜品名称"/>
      </el-form-item>
      <el-form-item label="价格" required>
        <el-input-number v-model="form.price" :min="0" :precision="2" :step="1"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">上架</el-radio>
          <el-radio :label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="菜品图片">
        <div class="upload-area">
          <img v-if="form.image_url" :src="previewUrl" class="image-preview large" alt=""/>
          <div class="upload-btns">
            <el-upload :show-file-list="false" :http-request="handleUpload" accept="image/*">
              <el-button type="primary" plain>
                <el-icon>
                  <Plus/>
                </el-icon>
                上传图片
              </el-button>
            </el-upload>
            <el-button type="success" plain @click="urlDialogVisible = true">URL上传</el-button>
          </div>
        </div>
      </el-form-item>

      <!-- URL上传弹窗 -->
      <el-dialog v-model="urlDialogVisible" title="通过URL上传图片" width="480px" :close-on-click-modal="false">
        <el-input
            v-model="imageUrlInput"
            placeholder="请输入图片URL地址（http:// 或 https://）"
            size="large"
            @keyup.enter="handleUploadFromUrl"
        />
        <template #footer>
          <el-button @click="urlDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="urlUploading" @click="handleUploadFromUrl">下载并保存</el-button>
        </template>
      </el-dialog>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button @click="router.push('/foods')">返回列表</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.upload-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.upload-btns {
  display: flex;
  gap: 8px;
}

.image-preview.large {
  width: 120px;
  height: 120px;
}
</style>
