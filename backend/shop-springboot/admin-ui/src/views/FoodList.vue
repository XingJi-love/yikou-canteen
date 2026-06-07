/**
* 菜品管理列表页
* 支持分类筛选、名称搜索、回收站切换、分页浏览
*/
<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {deleteFood, getCategories, getFoods, uploadUrl} from '@/api'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const query = reactive({
  category_id: '',
  search: '',
  recycle: 0,
  page: 1,
  page_size: 10
})

/** 加载分类下拉选项 */
const loadCategories = async () => {
  categories.value = await getCategories()
}

/** 查询菜品（分页 + 筛选） */
const loadData = async () => {
  loading.value = true
  try {
    const params = {...query}
    if (!params.category_id) delete params.category_id
    if (!params.search) delete params.search
    const res = await getFoods(params)
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

/** 搜索（重置到第一页） */
const handleSearch = () => {
  query.page = 1
  loadData()
}

/** 删除/回收菜品：二次确认后执行 */
const handleDelete = async (row) => {
  const isRecycle = query.recycle === 1
  await ElMessageBox.confirm(
      isRecycle ? '永久删除后无法恢复，确定吗？' : '确定移入回收站吗？',
      '提示',
      {type: 'warning'}
  )
  await deleteFood(row.id, isRecycle)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  await loadCategories()
  await loadData()
})
</script>

<template>
  <div class="page-card">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <h2 class="page-title" style="margin: 0">菜品管理</h2>
      <el-button type="primary" @click="router.push('/foods/edit')">新增菜品</el-button>
    </div>

    <div class="page-toolbar">
      <el-select v-model="query.category_id" placeholder="全部分类" clearable style="width: 160px"
                 @change="handleSearch">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id"/>
      </el-select>
      <el-input v-model="query.search" placeholder="搜索菜品名" clearable style="width: 200px"
                @keyup.enter="handleSearch"/>
      <el-radio-group v-model="query.recycle" @change="handleSearch">
        <el-radio-button :label="0">正常</el-radio-button>
        <el-radio-button :label="1">回收站</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <img v-if="row.image_url" :src="uploadUrl(row.image_url)" class="image-preview" alt=""/>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="160"/>
      <el-table-column prop="category_name" label="分类" width="120"/>
      <el-table-column prop="price" label="价格" width="90">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'info'" size="small">{{ row.status ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="router.push(`/foods/edit/${row.id}`)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 16px; display: flex; justify-content: flex-end">
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.page_size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
      />
    </div>
  </div>
</template>
