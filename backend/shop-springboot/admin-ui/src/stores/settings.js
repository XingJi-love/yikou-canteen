/**
 * 系统设置状态管理（SettingsStore）
 * - 缓存系统设置数据，避免多页面重复请求
 * - State: settings 对象、加载状态、缓存标记
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSettings, saveSettings as saveSettingsApi } from '@/api'

export const useSettingsStore = defineStore('settings', () => {
  const settings = ref({})
  const loaded = ref(false)
  const saving = ref(false)

  /** 获取系统设置（带缓存） */
  async function fetchSettings(force = false) {
    if (loaded.value && !force) return settings.value
    const res = await getSettings()
    settings.value = res.data ?? res
    loaded.value = true
    return settings.value
  }

  /** 保存系统设置 */
  async function saveSettings(data) {
    saving.value = true
    try {
      await saveSettingsApi(data)
      // 保存成功后刷新缓存
      loaded.value = false
      await fetchSettings(true)
      return true
    } finally {
      saving.value = false
    }
  }

  return { settings, loaded, saving, fetchSettings, saveSettings }
})
