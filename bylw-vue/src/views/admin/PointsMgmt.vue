<template>
  <div class="space-y-8">
    <h1 class="text-2xl font-black text-gray-900">积分管理</h1>

    <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
      <div class="bg-gradient-to-br from-orange-500 to-amber-500 rounded-3xl p-8 text-white">
        <p class="text-sm opacity-80 mb-2">今日发放</p>
        <p class="text-3xl font-black">+{{ todayEarned.toLocaleString() }}</p>
      </div>
      <div class="bg-gradient-to-br from-green-600 to-emerald-600 rounded-3xl p-8 text-white">
        <p class="text-sm opacity-80 mb-2">今日兑换</p>
        <p class="text-3xl font-black">{{ todaySpent.toLocaleString() }}</p>
      </div>
      <div class="bg-gradient-to-br from-purple-600 to-pink-600 rounded-3xl p-8 text-white">
        <p class="text-sm opacity-80 mb-2">用户总数</p>
        <p class="text-3xl font-black">{{ totalUsers.toLocaleString() }}</p>
      </div>
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">用户</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">类型</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">积分</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">时间</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading">
            <td colspan="4" class="px-8 py-12 text-center text-gray-400">
              <div class="flex items-center justify-center">
                <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                加载中...
              </div>
            </td>
          </tr>
          <tr v-else-if="history.length === 0">
            <td colspan="4" class="px-8 py-12 text-center text-gray-400">暂无记录</td>
          </tr>
          <tr v-else v-for="record in history" :key="record.recordId" class="hover:bg-gray-50 transition-colors">
            <td class="px-8 py-4 text-sm text-gray-900">用户{{ record.userId }}</td>
            <td class="px-8 py-4 text-sm text-gray-600">{{ record.remark || record.sourceType }}</td>
            <td class="px-8 py-4 text-sm font-bold" :class="record.changeValue >= 0 ? 'text-green-600' : 'text-red-600'">
              {{ record.changeValue >= 0 ? '+' : '' }}{{ record.changeValue }}
            </td>
            <td class="px-8 py-4 text-sm text-gray-400">{{ record.createTime }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from '@/api/index'
import { userApi } from '@/api/user'
import type { PointsRecord } from '@/api/points'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const history = ref<PointsRecord[]>([])
const totalUsers = ref(0)
const loading = ref(true)

const todayEarned = computed(() => {
  const today = new Date().toDateString()
  return history.value
    .filter(r => new Date(r.createTime).toDateString() === today && r.changeValue > 0)
    .reduce((sum, r) => sum + r.changeValue, 0)
})

const todaySpent = computed(() => {
  const today = new Date().toDateString()
  return history.value
    .filter(r => new Date(r.createTime).toDateString() === today && r.changeValue < 0)
    .reduce((sum, r) => sum + r.changeValue, 0)
})

async function loadHistory() {
  loading.value = true
  try {
    const [historyData, userCount] = await Promise.all([
      api.get<PointsRecord[]>('/points/history/all'),
      userApi.count(),
    ])
    history.value = historyData
    totalUsers.value = userCount ?? 0
  } catch (e: any) {
    history.value = []
    showToast('加载失败：' + (e?.message || '请检���网络连接'), 'error')
  } finally {
    loading.value = false
  }
}

onMounted(loadHistory)
</script>
