<template>
  <div class="page-card">
    <h1 class="page-title">AI 智能助手</h1>

    <el-tabs v-model="activeTab" tab-position="top">
      <!-- ==================== Tab1: 菜品描述生成 ==================== -->
      <el-tab-pane label="菜品描述" name="desc">
        <div class="page-toolbar">
          <el-select
            v-model="selectedFood"
            filterable
            placeholder="搜索并选择菜品"
            clearable
            style="width: 320px"
            @change="onFoodSelect"
          >
            <el-option
              v-for="food in foodList"
              :key="food.id"
              :label="food.name + ' (￥' + food.price + ')'"
              :value="food.id"
            />
          </el-select>
          <el-button
            type="primary"
            :loading="descLoading"
            :disabled="!selectedFood"
            @click="generateDesc"
          >
            <el-icon><MagicStick /></el-icon>
            生成描述
          </el-button>
        </div>

        <div v-if="currentFood" class="food-info-card">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="菜品名称">{{ currentFood.name }}</el-descriptions-item>
            <el-descriptions-item label="所属分类">{{ currentFood.category_name }}</el-descriptions-item>
            <el-descriptions-item label="价格">￥{{ currentFood.price }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-loading="descLoading" class="result-card">
          <el-card v-if="descResult" shadow="hover">
            <template #header>
              <div class="result-header">
                <span>生成结果</span>
                <el-tag size="small" type="info">{{ descModel }}</el-tag>
              </div>
            </template>
            <div class="result-content markdown-body" v-html="renderMarkdown(descResult)"></div>
            <template v-if="descResult && currentFood" #footer>
              <el-button type="success" size="small" @click="updateFoodDesc">
                <el-icon><Check /></el-icon>
                填入今日推荐描述
              </el-button>
            </template>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- ==================== Tab2: 每日经营简报 ==================== -->
      <el-tab-pane label="经营简报" name="brief">
        <div class="page-toolbar">
          <el-date-picker
            v-model="briefDate"
            type="date"
            placeholder="选择日期（默认昨天）"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
          <el-button
            type="primary"
            :loading="briefLoading"
            @click="generateBrief"
          >
            <el-icon><DataAnalysis /></el-icon>
            生成简报
          </el-button>
        </div>

        <div v-loading="briefLoading" class="result-card">
          <el-alert
            v-if="briefResult"
            type="info"
            :closable="false"
            show-icon
          >
            <template #title>
              <div class="result-header">每日经营简报</div>
            </template>
            <div class="result-content markdown-body" v-html="renderMarkdown(briefResult)"></div>
          </el-alert>
        </div>
      </el-tab-pane>

      <!-- ==================== Tab3: 智能套餐推荐 ==================== -->
      <el-tab-pane label="套餐推荐" name="combo">
        <div class="page-toolbar">
          <span style="margin-right: 8px; color: #606266">分析天数：</span>
          <el-input-number
            v-model="comboDays"
            :min="7"
            :max="365"
            :step="1"
            style="width: 140px"
          />
          <el-button
            type="primary"
            :loading="comboLoading"
            @click="generateCombo"
          >
            <el-icon><Connection /></el-icon>
            分析推荐
          </el-button>
        </div>

        <div v-loading="comboLoading" class="result-card">
          <el-card v-if="comboResult" shadow="hover">
            <template #header>
              <div class="result-header">
                <span>套餐推荐结果</span>
                <el-tag size="small" type="info">{{ comboModel }}</el-tag>
              </div>
            </template>
            <div class="result-content markdown-body" v-html="renderMarkdown(comboResult)"></div>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- ==================== Tab4: 营销活动参谋 ==================== -->
      <el-tab-pane label="营销参谋" name="marketing">
        <div class="page-toolbar">
          <el-button
            type="primary"
            :loading="marketingLoading"
            @click="generateMarketing"
          >
            <el-icon><TrendCharts /></el-icon>
            生成营销方案
          </el-button>
        </div>

        <div v-loading="marketingLoading" class="result-card">
          <el-alert
            v-if="marketingResult"
            type="warning"
            :closable="false"
            show-icon
          >
            <template #title>
              <div class="result-header">营销活动建议</div>
            </template>
            <div class="result-content markdown-body" v-html="renderMarkdown(marketingResult)"></div>
          </el-alert>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, DataAnalysis, Connection, TrendCharts, Check } from '@element-plus/icons-vue'
import { marked } from 'marked'
import {
  getAiFoods,
  generateFoodDescription,
  generateDailyBrief,
  generateComboRecommend,
  generateMarketingAdvice,
  getSettings,
  saveSettings
} from '@/api'

const renderMarkdown = (text) => {
  if (!text) return ''
  try {
    const html = marked.parse(text, { async: false })
    return typeof html === 'string' ? html : String(html)
  } catch (e) {
    console.error('Markdown 解析失败:', e)
    return text.replace(/\n/g, '<br>')
  }
}

// Tab 状态
const activeTab = ref('desc')

// ===== Tab1: 菜品描述 =====
const foodList = ref([])
const selectedFood = ref(null)
const currentFood = ref(null)
const descLoading = ref(false)
const descResult = ref('')
const descModel = ref('')

const loadFoods = async () => {
  try {
    const res = await getAiFoods()
    foodList.value = res.data || []
  } catch {
    /* ignore */
  }
}

const onFoodSelect = (id) => {
  if (id) {
    currentFood.value = foodList.value.find(f => f.id === id) || null
  } else {
    currentFood.value = null
  }
  descResult.value = ''
}

const generateDesc = async () => {
  if (!currentFood.value) return
  const food = currentFood.value
  descLoading.value = true
  descResult.value = ''
  descModel.value = ''

  try {
    const res = await generateFoodDescription(food.name, food.category_name, food.price)
    descResult.value = res.content || ''
    descModel.value = res.model || ''
  } catch {
    /* error handled by interceptor */
  } finally {
    descLoading.value = false
  }
}

const updateFoodDesc = async () => {
  if (!currentFood.value || !descResult.value) return
  try {
    const settings = await getSettings()
    settings.recommend_desc = descResult.value
    await saveSettings(settings)
    ElMessage.success('描述已填入今日推荐！前往【系统设置】查看')
  } catch {
    /* error handled by interceptor */
  }
}

// ===== Tab2: 经营简报 =====
const briefDate = ref('')
const briefLoading = ref(false)
const briefResult = ref('')

const generateBrief = async () => {
  briefLoading.value = true
  briefResult.value = ''

  try {
    const res = await generateDailyBrief(briefDate.value || null)
    briefResult.value = res.content || ''
  } catch {
    /* error handled by interceptor */
  } finally {
    briefLoading.value = false
  }
}

// ===== Tab3: 套餐推荐 =====
const comboDays = ref(30)
const comboLoading = ref(false)
const comboResult = ref('')
const comboModel = ref('')

const generateCombo = async () => {
  comboLoading.value = true
  comboResult.value = ''
  comboModel.value = ''

  try {
    const res = await generateComboRecommend(comboDays.value)
    comboResult.value = res.content || ''
    comboModel.value = res.model || ''
  } catch {
    /* error handled by interceptor */
  } finally {
    comboLoading.value = false
  }
}

// ===== Tab4: 营销参谋 =====
const marketingLoading = ref(false)
const marketingResult = ref('')

const generateMarketing = async () => {
  marketingLoading.value = true
  marketingResult.value = ''

  try {
    const res = await generateMarketingAdvice()
    marketingResult.value = res.content || ''
  } catch {
    /* error handled by interceptor */
  } finally {
    marketingLoading.value = false
  }
}

onMounted(() => {
  loadFoods()
})
</script>

<style scoped>
.food-info-card {
  margin: 16px 0;
}

.result-card {
  margin-top: 16px;
  min-height: 60px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 15px;
}

.result-content {
  margin-top: 12px;
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
}


.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 12px 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
}

.markdown-body :deep(h1) { font-size: 17px; }
.markdown-body :deep(h3) { font-size: 14px; }

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 6px 0;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin: 3px 0;
  line-height: 1.7;
}

.markdown-body :deep(p) {
  margin: 6px 0;
}

.markdown-body :deep(strong) {
  color: #e54d42;
  font-weight: 600;
}

.markdown-body :deep(blockquote) {
  margin: 8px 0;
  padding: 4px 14px;
  border-left: 3px solid #409eff;
  background: #f0f5ff;
  color: #666;
}

.markdown-body :deep(code) {
  padding: 1px 5px;
  background: #f5f5f5;
  border-radius: 3px;
  font-size: 13px;
}

.markdown-body :deep(hr) {
  margin: 12px 0;
  border: none;
  border-top: 1px solid #ebeef5;
}
</style>
