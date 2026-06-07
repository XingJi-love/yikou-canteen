/**
 * 订单管理/待取餐列表页
 * 支持支付状态、取餐状态筛选，以及搜索订单号/取餐码
 * 路由 meta.pending 控制待取餐视图
 */
<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrders, setOrderTaken } from '@/api'

const route = useRoute()
const isPending = computed(() => route.meta.pending === true)

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  is_pay: isPending.value ? 1 : -1,
  is_taken: isPending.value ? 0 : -1,
  search: '',
  page: 1,
  page_size: 10
})

/** 查询订单列表 */
const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    if (params.is_pay < 0) delete params.is_pay
    if (params.is_taken < 0) delete params.is_taken
    if (!params.search) delete params.search
    const res = await getOrders(params)
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

/** 标记/取消取餐 */
const handleTaken = async (row, taken) => {
  await setOrderTaken(row.id, taken)
  ElMessage.success(taken ? '已标记取餐' : '已取消取餐')
  loadData()
}

/** 统一格式化时间戳/字符串 → 本地时间 */
const formatTime = (val) => {
  if (!val) return '-'
  if (typeof val === 'number') {
    return new Date(val).toLocaleString('zh-CN')
  }
  return String(val).replace('T', ' ').substring(0, 19)
}

// 当路由切换（订单管理 ↔ 待取餐）时重置查询条件
watch(isPending, (val) => {
  query.is_pay = val ? 1 : -1
  query.is_taken = val ? 0 : -1
  query.page = 1
  loadData()
})

onMounted(loadData)
</script>

<template>
  <div class="page-card">
    <h2 class="page-title">{{ isPending ? '待取餐订单' : '订单管理' }}</h2>

    <div class="page-toolbar">
      <el-input
        v-model="query.search"
        placeholder="取餐码 / 订单 ID（如 A01）"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      />
      <template v-if="!isPending">
        <el-select v-model="query.is_pay" placeholder="支付状态" style="width: 120px" @change="handleSearch">
          <el-option label="全部" :value="-1" />
          <el-option label="已支付" :value="1" />
          <el-option label="未支付" :value="0" />
        </el-select>
        <el-select v-model="query.is_taken" placeholder="取餐状态" style="width: 120px" @change="handleSearch">
          <el-option label="全部" :value="-1" />
          <el-option label="已取餐" :value="1" />
          <el-option label="未取餐" :value="0" />
        </el-select>
      </template>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="code" label="取餐码" width="90" />
      <el-table-column prop="sn" label="订单号" min-width="160" />
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div v-for="(item, idx) in row.order_food" :key="idx" class="food-line">
            {{ item.name }} x{{ item.number }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="金额" width="90">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="支付" width="80">
        <template #default="{ row }">
          <el-tag :type="row.is_pay ? 'success' : 'info'" size="small">{{ row.is_pay ? '已付' : '未付' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="取餐" width="80">
        <template #default="{ row }">
          <el-tag :type="row.is_taken ? 'success' : 'warning'" size="small">{{ row.is_taken ? '已取' : '待取' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }">{{ formatTime(row.create_time) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.is_pay && !row.is_taken"
            type="primary"
            link
            @click="handleTaken(row, true)"
          >标记取餐</el-button>
          <el-button
            v-if="row.is_taken"
            type="warning"
            link
            @click="handleTaken(row, false)"
          >取消取餐</el-button>
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

<style scoped>
.food-line {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
</style>
