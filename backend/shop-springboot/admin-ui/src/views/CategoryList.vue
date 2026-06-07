/**
* 分类管理页
* 支持分类的增删改查，行内拖拽排序 / input-number
*/
<script setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {addCategory, deleteCategory, getCategories, sortCategories, updateCategory} from '@/api'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editing = ref(null)
const form = ref({name: '', sort: 0})

/** 加载全部分类 */
const loadData = async () => {
  loading.value = true
  try {
    list.value = await getCategories()
  } finally {
    loading.value = false
  }
}

/** 打开新增对话框 */
const openAdd = () => {
  editing.value = null
  form.value = {name: '', sort: list.value.length + 1}
  dialogVisible.value = true
}

/** 打开编辑对话框 */
const openEdit = (row) => {
  editing.value = row
  form.value = {name: row.name, sort: row.sort}
  dialogVisible.value = true
}

/** 新增或更新分类 */
const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (editing.value) {
    await updateCategory(editing.value.id, form.value)
    ElMessage.success('修改成功')
  } else {
    await addCategory(form.value)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadData()
}

/** 删除分类（二次确认） */
const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', {type: 'warning'})
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  loadData()
}

/** 保存排序 */
const handleSortChange = async () => {
  const sortMap = {}
  list.value.forEach((item, index) => {
    sortMap[item.id] = index + 1
  })
  await sortCategories(sortMap)
  ElMessage.success('排序已保存')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-card">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <h2 class="page-title" style="margin: 0">分类管理</h2>
      <div>
        <el-button @click="handleSortChange">保存排序</el-button>
        <el-button type="primary" @click="openAdd">添加分类</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="list" border stripe row-key="id">
      <el-table-column type="index" label="#" width="60"/>
      <el-table-column prop="name" label="分类名称" min-width="200"/>
      <el-table-column prop="sort" label="排序" width="100">
        <template #default="{ row }">
          <el-input-number v-model="row.sort" :min="0" size="small"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑分类' : '添加分类'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>