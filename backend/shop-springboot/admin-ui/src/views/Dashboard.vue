/**
* 管理后台首页（仪表盘）
* 展示统计卡片 + 4 个 ECharts 图表（订单趋势、营收统计、分类分布、热销商品）
*/
<script setup>
import {onMounted, ref, nextTick} from 'vue'
import {useRouter} from 'vue-router'
import {
  getSettings,
  getStatisticsOverview,
  getStatisticsOrderTrend,
  getStatisticsRevenue,
  getStatisticsCategoryDistribution,
  getStatisticsTopFoods
} from '@/api'
import * as echarts from 'echarts'

const router = useRouter()
const settingsOk = ref(true)
const pendingCount = ref(0)
const totalOrders = ref(0)
const totalFoods = ref(0)
const totalUsers = ref(0)
const totalRevenue = ref(0)

// 图表 DOM 引用
const orderTrendChart = ref(null)
const categoryChart = ref(null)
const revenueChart = ref(null)
const topFoodsChart = ref(null)

const loading = ref(true)

/** 初始化订单趋势折线图 */
const initOrderTrendChart = (data) => {
  if (!orderTrendChart.value) return

  const chart = echarts.init(orderTrendChart.value)
  const option = {
    title: {
      text: '订单趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 600
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '订单数'
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: data.counts,
        itemStyle: {
          color: '#ff9c35'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {offset: 0, color: 'rgba(255, 156, 53, 0.3)'},
            {offset: 1, color: 'rgba(255, 156, 53, 0.05)'}
          ])
        }
      }
    ]
  }
  chart.setOption(option)

  // 窗口 resize 时自适应
  window.addEventListener('resize', () => chart.resize())
}

/** 初始化分类分布饼图 */
const initCategoryChart = (data) => {
  if (!categoryChart.value) return

  const chart = echarts.init(categoryChart.value)
  const option = {
    title: {
      text: '菜品分类分布',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 600
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [
      {
        name: '菜品数量',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data,
        color: ['#ff9c35', '#67c23a', '#409eff', '#e6a23c', '#f56c6c', '#909399']
      }
    ]
  }
  chart.setOption(option)

  window.addEventListener('resize', () => chart.resize())
}

/** 初始化营收统计柱状图 */
const initRevenueChart = (data) => {
  if (!revenueChart.value) return

  const chart = echarts.init(revenueChart.value)
  const option = {
    title: {
      text: '营收统计',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 600
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        return `${params[0].name}<br/>${params[0].marker}营收: ¥${params[0].value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '金额(元)',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '营收',
        type: 'bar',
        data: data.revenues,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {offset: 0, color: '#ff9c35'},
            {offset: 1, color: '#e8851f'}
          ]),
          borderRadius: [5, 5, 0, 0]
        }
      }
    ]
  }
  chart.setOption(option)

  window.addEventListener('resize', () => chart.resize())
}

/** 初始化热销商品排行条形图 */
const initTopFoodsChart = (data) => {
  if (!topFoodsChart.value) return

  // 反转数组：ECharts category 轴从下到上排列，反转后销量高的显示在顶部
  const reversedNames = [...data.names].reverse()
  const reversedSales = [...data.sales].reverse()

  const chart = echarts.init(topFoodsChart.value)
  const option = {
    title: {
      text: '热销菜品 TOP 10',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 600
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '12%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '销量'
    },
    yAxis: {
      type: 'category',
      data: reversedNames,
      axisLabel: {
        interval: 0,
        formatter: (value) => {
          return value.length > 8 ? value.substring(0, 8) + '...' : value
        }
      }
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        data: reversedSales,
        itemStyle: {
          color: '#67c23a',
          borderRadius: [0, 5, 5, 0]
        },
        label: {
          show: true,
          position: 'right'
        }
      }
    ]
  }
  chart.setOption(option)

  window.addEventListener('resize', () => chart.resize())
}

/** 生成模拟统计（API 失败时降级使用） */
const generateMockData = () => {
  const dates = []
  const orderCounts = []
  const revenues = []

  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
    orderCounts.push(Math.floor(Math.random() * 50) + 10)
    revenues.push((Math.random() * 1000 + 500).toFixed(2))
  }

  return {
    orderTrend: {dates, counts: orderCounts},
    revenue: {dates, revenues}
  }
}

/** 并发加载所有统计数据，然后渲染图表 */
const loadStatistics = async () => {
  loading.value = true

  try {
    const settings = await getSettings()
    settingsOk.value = !!(settings.appid && settings.appsecret)

    const overview = await getStatisticsOverview()
    totalOrders.value = overview.totalOrders || 0
    pendingCount.value = overview.pendingOrders || 0
    totalFoods.value = overview.totalFoods || 0
    totalUsers.value = overview.totalUsers || 0
    totalRevenue.value = parseFloat(overview.totalRevenue) || 0

    const orderTrend = await getStatisticsOrderTrend(7)
    const revenue = await getStatisticsRevenue(7)
    const categoryDist = await getStatisticsCategoryDistribution()
    const categoryData = (categoryDist.data || []).map(item => ({
      name: item.category_name || item.categoryName,
      value: item.food_count || item.foodCount
    }))
    const topFoods = await getStatisticsTopFoods(10, 30)
    const topFoodsData = {
      names: (topFoods.data || []).map(f => f.food_name || f.foodName),
      sales: (topFoods.data || []).map(f => f.sales)
    }

    await nextTick()
    initOrderTrendChart(orderTrend)
    initCategoryChart(categoryData)
    initRevenueChart(revenue)
    initTopFoodsChart(topFoodsData)

  } catch (error) {
    console.error('加载统计数据失败:', error)
    const mockData = generateMockData()
    await nextTick()
    initOrderTrendChart(mockData.orderTrend)
    initRevenueChart(mockData.revenue)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div class="page-card">
    <h2 class="page-title">欢迎使用一口食堂管理中心</h2>

    <el-alert
        v-if="!settingsOk"
        title="您还没有配置 AppID 或 AppSecret，请前往系统设置进行配置。"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
    />

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card" @click="router.push('/orders')">
          <div class="stat-value">{{ totalOrders }}</div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-card-pending" @click="router.push('/orders')">
          <div class="stat-value">{{ pendingCount }}</div>
          <div class="stat-label">待取餐订单</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card" @click="router.push('/foods')">
          <div class="stat-value">{{ totalFoods }}</div>
          <div class="stat-label">菜品总数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">¥{{ totalRevenue.toFixed(2) }}</div>
          <div class="stat-label">总营收</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" v-loading="loading">
      <!-- 订单趋势图 -->
      <el-col :xs="24" :lg="12" style="margin-bottom: 20px">
        <el-card shadow="hover">
          <div ref="orderTrendChart" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 营收统计图 -->
      <el-col :xs="24" :lg="12" style="margin-bottom: 20px">
        <el-card shadow="hover">
          <div ref="revenueChart" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 分类分布图 -->
      <el-col :xs="24" :lg="12" style="margin-bottom: 20px">
        <el-card shadow="hover">
          <div ref="categoryChart" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 热销商品图 -->
      <el-col :xs="24" :lg="12" style="margin-bottom: 20px">
        <el-card shadow="hover">
          <div ref="topFoodsChart" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-divider content-position="left">快捷操作</el-divider>

    <el-row :gutter="20">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="quick-card" @click="router.push('/foods')">
          <el-icon class="quick-icon">
            <Goods/>
          </el-icon>
          <div class="quick-label">菜品管理</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="quick-card" @click="router.push('/categories')">
          <el-icon class="quick-icon">
            <Menu/>
          </el-icon>
          <div class="quick-label">分类管理</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="quick-card" @click="router.push('/orders')">
          <el-icon class="quick-icon">
            <Document/>
          </el-icon>
          <div class="quick-label">订单管理</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="quick-card" @click="router.push('/users')">
          <el-icon class="quick-icon">
            <User/>
          </el-icon>
          <div class="quick-label">用户管理</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="quick-card" @click="router.push('/settings')">
          <el-icon class="quick-icon">
            <Setting/>
          </el-icon>
          <div class="quick-label">系统设置</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="quick-card" @click="router.push('/password')">
          <el-icon class="quick-icon">
            <Lock/>
          </el-icon>
          <div class="quick-label">修改密码</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(255, 156, 53, 0.3);
}

.stat-card-pending {
  background: linear-gradient(135deg, #fff4e8 0%, #ffffff 100%);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #ff9c35;
  margin-bottom: 8px;
  line-height: 1.2;
}

.stat-label {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.quick-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  padding: 10px 0;
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-icon {
  font-size: 32px;
  color: #ff9c35;
  margin-bottom: 8px;
}

.quick-label {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .stat-value {
    font-size: 24px;
  }

  .quick-icon {
    font-size: 28px;
  }
}
</style>
