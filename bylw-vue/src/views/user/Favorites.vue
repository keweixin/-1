<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
    <div class="mb-8">
      <h1 class="text-2xl font-black text-gray-900">我的收藏</h1>
      <p class="mt-2 text-sm text-gray-500">共 {{ total }} 件收藏食品</p>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <div v-else-if="favoriteItems.length === 0" class="text-center py-20">
      <p class="text-gray-400 text-lg">暂无收藏</p>
      <router-link to="/food-hall" class="mt-4 inline-block text-green-600 font-bold hover:underline">去逛逛</router-link>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div v-for="item in favoriteItems" :key="item.behaviorId"
        class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden hover:shadow-lg transition-all group">
        <router-link :to="`/food/${item.targetId}`" class="block">
          <div class="aspect-[4/3] bg-gray-100 overflow-hidden">
            <img :src="getFoodImage(item.food?.coverImg || '')" alt="" class="w-full h-full object-cover group-hover:scale-105 transition-transform" referrerpolicy="no-referrer" />
          </div>
          <div class="p-4">
            <p class="font-bold text-gray-900 truncate">{{ item.food?.foodName || '食品 #' + item.targetId }}</p>
            <div class="flex items-baseline gap-1 mt-1">
              <span class="text-sm text-gray-400 line-through">&yen;{{ (item.food?.originPrice || 0).toFixed(2) }}</span>
              <span class="text-base font-black text-orange-500">&yen;{{ (item.food?.discountPrice || 0).toFixed(2) }}</span>
            </div>
            <p class="text-xs text-gray-400 mt-1">{{ formatTime(item.createTime) }}</p>
          </div>
        </router-link>
        <div class="px-4 pb-4 flex gap-2">
          <router-link :to="`/food/${item.targetId}`"
            class="flex-1 py-2 rounded-xl text-sm font-bold text-green-600 border border-green-200 hover:bg-green-50 transition-all text-center">
            去下单
          </router-link>
          <button @click="removeFavorite(item.targetId)"
            class="flex-1 py-2 rounded-xl text-sm font-bold text-red-500 border border-red-200 hover:bg-red-50 transition-all">
            取消收藏
          </button>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex items-center justify-center gap-3 mt-8">
      <button :disabled="page <= 1" @click="page--; loadFavorites()"
        class="px-4 py-2 rounded-xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 disabled:opacity-30 transition-colors">
        上一页
      </button>
      <span class="text-sm text-gray-500 font-bold">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="page++; loadFavorites()"
        class="px-4 py-2 rounded-xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 disabled:opacity-30 transition-colors">
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { recommendApi } from '@/api/recommend'
import { foodApi, type FoodDTO } from '@/api/food'
import { getFoodImage } from '@/utils/images'

interface FavoriteItem {
  behaviorId: number
  userId: number
  targetType: string
  targetId: number
  behaviorType: string
  createTime: string
  food?: FoodDTO | null
}

const favoriteItems = ref<FavoriteItem[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = 12
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

async function loadFavorites() {
  loading.value = true
  try {
    const result = await recommendApi.listMyFavorites({ pageNum: page.value, pageSize }) as any
    const records = result.records || []
    total.value = result.total || 0

    // Fetch food details for each favorite
    const foodMap = new Map<number, FoodDTO>()
    const foodIds = [...new Set(records.map((r: any) => r.targetId))]
    await Promise.all(foodIds.map(async (id: number) => {
      try {
        const food = await foodApi.getById(id) as any
        foodMap.set(id, food)
      } catch { /* food may be deleted */ }
    }))

    favoriteItems.value = records.map((r: any) => ({
      ...r,
      food: foodMap.get(r.targetId) || null,
    }))
  } catch {
    favoriteItems.value = []
  } finally {
    loading.value = false
  }
}

async function removeFavorite(targetId: number) {
  try {
    await recommendApi.toggleFavorite('food', targetId)
    await loadFavorites()
  } catch { /* ignore */ }
}

onMounted(loadFavorites)
</script>
