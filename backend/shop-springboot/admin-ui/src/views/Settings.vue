/**
* 系统设置页
* 配置微信小程序 AppID/AppSecret、满减促销规则、首页轮播图、今日推荐、店铺信息
*/
<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Plus, Delete} from '@element-plus/icons-vue'
import {getSettings, saveSettings, uploadFile, uploadUrl} from '@/api'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  appid: '',
  appsecret: '',
  promotion: [{k: 50, v: 10}],
  img_swiper: [''],
  recommend_img: '/static/uploads/foods/15.jpg',
  recommend_name: '烧味双拼饭',
  recommend_price: '68',
  store_name: '一口食堂',
  store_subtitle: '港式美味 · 用心烹饪',
  store_description: '正宗港式茶餐厅，传承香港饮食文化。我们坚持使用新鲜食材，为顾客提供地道的港式美食体验。',
  store_address: '香港九龙旺角弥敦道688号',
  store_phone: '852-1234-5678',
  store_hours: '07:00 - 22:00'
})

/** 从后端加载系统设置 */
const loadData = async () => {
  loading.value = true
  try {
    const data = await getSettings()
    form.appid = data.appid || ''
    form.appsecret = data.appsecret || ''
    form.promotion = data.promotion?.length ? data.promotion : [{k: 50, v: 10}]
    form.img_swiper = data.img_swiper?.length ? data.img_swiper : ['']
    form.recommend_img = data.recommend_img || ''
    form.recommend_name = data.recommend_name || ''
    form.recommend_price = data.recommend_price || ''
    form.store_name = data.store_name || ''
    form.store_subtitle = data.store_subtitle || ''
    form.store_description = data.store_description || ''
    form.store_address = data.store_address || ''
    form.store_phone = data.store_phone || ''
    form.store_hours = data.store_hours || ''
  } finally {
    loading.value = false
  }
}

/** 添加/删除满减规则行 */
const addPromotion = () => form.promotion.push({k: 0, v: 0})
const removePromotion = (idx) => form.promotion.splice(idx, 1)

/** 添加/删除轮播图行 */
const addSwiper = () => form.img_swiper.push('')
const removeSwiper = (idx) => form.img_swiper.splice(idx, 1)

/** 上传设置中的图片（轮播图/推荐菜） */
const uploadSettingImage = async (file, target, index) => {
  const res = await uploadFile(file, target)
  const url = res.url || `/static/uploads/${res.path}`
  if (target === 'swiper') {
    form.img_swiper[index] = url
  } else if (target === 'recommend') {
    form.recommend_img = url
  }
  ElMessage.success('上传成功')
}

/** 保存全部设置项 */
const handleSave = async () => {
  saving.value = true
  try {
    await saveSettings({
      ...form,
      img_swiper: form.img_swiper.filter(Boolean)
    })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-card" v-loading="loading">
    <h2 class="page-title">系统设置</h2>

    <el-form label-width="120px" style="max-width: 720px">
      <el-divider content-position="left">微信小程序</el-divider>
      <el-form-item label="AppID">
        <el-input v-model="form.appid" placeholder="微信小程序 AppID"/>
      </el-form-item>
      <el-form-item label="AppSecret">
        <el-input v-model="form.appsecret" placeholder="微信小程序 AppSecret" show-password/>
      </el-form-item>

      <el-divider content-position="left">满减促销</el-divider>
      <div v-for="(item, idx) in form.promotion" :key="idx" class="promo-row">
        <span>满</span>
        <el-input-number v-model="item.k" :min="0" size="small"/>
        <span>元减</span>
        <el-input-number v-model="item.v" :min="0" size="small"/>
        <span>元</span>
        <el-button type="danger" link @click="removePromotion(idx)">
          <el-icon>
            <Delete/>
          </el-icon>
        </el-button>
      </div>
      <el-button type="primary" plain size="small" @click="addPromotion">添加规则</el-button>

      <el-divider content-position="left">首页轮播图</el-divider>
      <div v-for="(url, idx) in form.img_swiper" :key="'s' + idx" class="img-row">
        <img v-if="url" :src="uploadUrl(url)" class="image-preview" alt=""/>
        <el-input v-model="form.img_swiper[idx]" placeholder="图片 URL 或上传"/>
        <el-upload :show-file-list="false" :http-request="({ file }) => uploadSettingImage(file, 'swiper', idx)"
                   accept="image/*">
          <el-button size="small">上传</el-button>
        </el-upload>
        <el-button type="danger" link @click="removeSwiper(idx)">
          <el-icon>
            <Delete/>
          </el-icon>
        </el-button>
      </div>
      <el-button type="primary" plain size="small" @click="addSwiper">
        <el-icon>
          <Plus/>
        </el-icon>
        添加轮播
      </el-button>

      <el-divider content-position="left">今日推荐</el-divider>
      <div class="img-row">
        <img v-if="form.recommend_img" :src="uploadUrl(form.recommend_img)" class="image-preview" alt=""/>
        <el-input v-model="form.recommend_img" placeholder="推荐菜品图片 URL 或上传"/>
        <el-upload :show-file-list="false" :http-request="({ file }) => uploadSettingImage(file, 'recommend')"
                   accept="image/*">
          <el-button size="small">上传</el-button>
        </el-upload>
      </div>
      <el-form-item label="菜品名称">
        <el-input v-model="form.recommend_name" placeholder="例如：烧味双拼饭"/>
      </el-form-item>
      <el-form-item label="菜品价格">
        <el-input v-model="form.recommend_price" placeholder="例如：68">
          <template #prepend>¥</template>
        </el-input>
      </el-form-item>

      <el-divider content-position="left">店铺信息</el-divider>
      <el-form-item label="店铺名称">
        <el-input v-model="form.store_name" placeholder="例如：一口食堂"/>
      </el-form-item>
      <el-form-item label="店铺副标题">
        <el-input v-model="form.store_subtitle" placeholder="例如：港式美味 · 用心烹饪"/>
      </el-form-item>
      <el-form-item label="店铺介绍">
        <el-input v-model="form.store_description" type="textarea" :rows="3" placeholder="店铺简介描述"/>
      </el-form-item>
      <el-form-item label="店铺地址">
        <el-input v-model="form.store_address" placeholder="店铺详细地址"/>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.store_phone" placeholder="客服电话"/>
      </el-form-item>
      <el-form-item label="营业时间">
        <el-input v-model="form.store_hours" placeholder="例如：07:00 - 22:00"/>
      </el-form-item>

      <el-form-item style="margin-top: 24px">
        <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.promo-row,
.img-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.img-row .el-input {
  flex: 1;
  min-width: 200px;
}

.image-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  flex-shrink: 0;
}
</style>
