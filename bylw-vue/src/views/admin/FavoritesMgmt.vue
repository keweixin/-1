<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-black text-gray-900">食品收藏管理</h1>
      <p class="mt-2 text-sm text-gray-500">查看用户的食品收藏记录与统计。</p>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6">
        <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">总收藏数</p>
        <p class="text-3xl font-black text-gray-900 mt-2">{{ stats.totalFavorites }}</p>
      </div>
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6">
        <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">今日新增</p>
        <p class="text-3xl font-black text-green-600 mt-2">{{ stats.todayFavorites }}</p>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">用户ID</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">目标类型</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">目标ID</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">收藏时间</th>
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
          <tr v-else-if="favorites.length === 0">
            <td colspan="4" class="px-8 py-12 text-center text-gray-400">暂无收藏记录</td>
          </tr>
          <tr v-else v-for="item in favorites" :key="item.behaviorId" class="hover:bg-gray-50 transition-colors">
            <td class="px-8 py-6 text-sm text-gray-900">{{ item.userId }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ item.targetType }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ item.targetId }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ formatTime(item.createTime) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="flex justify-center gap-2">
      <button v-for="p in totalPages" :key="p" @click="page = p"
        class="w-10 h-10 rounded-xl text-sm font-bold transition-all"
        :class="page === p ? 'bg-green-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'">
        {{ p }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { recommendApi } from '@/api/recommend'

interface FavoriteItem {
  behaviorId: number
  userId: number
  targetType: string
  targetId: number
  behaviorType: string
  createTime: string
}

const favorites = ref<FavoriteItem[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const stats = ref({ totalFavorites: 0, todayFavorites: 0 })

const totalPages = computed(() => Math.ceil(total.value / pageSize))

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

async function loadFavorites() {
  loading.value = true
  try {
    const result = await recommendApi.listAdminFavorites({ pageNum: page.value, pageSize })
    favorites.value = result.records as FavoriteItem[]
    total.value = result.total
  } catch (_e: unknown) {
    favorites.value = []
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    stats.value = await recommendApi.getFavoritesStats()
  } catch (_e: unknown) { /* ignore */ }
}

onMounted(async () => {
  await Promise.all([loadFavorites(), loadStats()])
})
</script>
