<template>
  <div class="max-w-7xl mx-auto px-4 py-12">
    <div class="text-center mb-12">
      <h1 class="text-4xl font-black text-gray-900 mb-4">个性化推荐</h1>
      <p class="text-gray-500">基于您的饮食偏好和购买历史，为您推荐最适合的商品</p>
    </div>

    <!-- 登录提示 -->
    <div v-if="!isLoggedIn" class="bg-amber-50 border border-amber-200 rounded-2xl p-6 mb-8 flex items-center gap-4">
      <div class="w-10 h-10 bg-amber-100 rounded-xl flex items-center justify-center shrink-0">
        <svg class="w-5 h-5 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
      </div>
      <div>
        <p class="font-bold text-amber-800">登录后可获得个性化推荐</p>
        <p class="text-sm text-amber-600 mt-0.5">当前为游客模式，显示热门食品而非基于您口味的推荐</p>
      </div>
      <button class="ml-auto shrink-0 bg-amber-600 text-white px-5 py-2.5 rounded-xl font-bold hover:bg-amber-700 transition-all" @click="router.push('/login')">
        登录
      </button>
    </div>

    <!-- 饮食档案状态 -->
    <div v-else-if="!hasProfile" class="bg-blue-50 border border-blue-200 rounded-2xl p-6 mb-8 flex items-center gap-4">
      <div class="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center shrink-0">
        <svg class="w-5 h-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
      </div>
      <div>
        <p class="font-bold text-blue-800">完善饮食档案，让推荐更精准</p>
        <p class="text-sm text-blue-600 mt-0.5">系统将根据您的口味偏好、过敏原和慢病史进行个性化筛选</p>
      </div>
      <button class="ml-auto shrink-0 bg-blue-600 text-white px-5 py-2.5 rounded-xl font-bold hover:bg-blue-700 transition-all" @click="router.push('/profile')">
        完善档案
      </button>
    </div>

    <!-- 个性化激活状态 -->
    <div v-else class="bg-gradient-to-r from-green-600 to-emerald-600 rounded-[2rem] p-8 mb-8 text-white">
      <div class="flex items-center gap-6">
        <div class="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center">
          <SparklesIcon class="w-8 h-8" />
        </div>
        <div>
          <p class="font-bold text-lg mb-1">个性化推荐已激活</p>
          <p class="opacity-80 text-sm">口味偏好：<span class="font-semibold">{{ tastePreference || '未设置' }}</span>
            <span v-if="funnel.coldStart" class="ml-2 text-xs bg-white/20 px-2 py-0.5 rounded-full">冷启动模式</span>
          </p>
        </div>
      </div>

      <!-- 匹配统计 - 真实漏斗数据 -->
      <div v-if="funnel.candidateCount > 0" class="mt-6">
        <div class="grid grid-cols-4 gap-3">
          <div class="bg-white/15 rounded-xl p-4 text-center">
            <p class="text-2xl font-black">{{ funnel.candidateCount }}</p>
            <p class="text-xs opacity-70 mt-1">候选食品</p>
          </div>
          <div class="bg-white/15 rounded-xl p-4 text-center">
            <p class="text-2xl font-black text-yellow-300">{{ funnel.healthFilteredCount }}</p>
            <p class="text-xs opacity-70 mt-1">健康过滤后</p>
          </div>
          <div class="bg-white/15 rounded-xl p-4 text-center">
            <p class="text-2xl font-black text-green-300">{{ funnel.tasteMatchedCount }}</p>
            <p class="text-xs opacity-70 mt-1">口味匹配加分</p>
          </div>
          <div class="bg-white/15 rounded-xl p-4 text-center">
            <p class="text-2xl font-black text-white">{{ funnel.finalCount }}</p>
            <p class="text-xs opacity-70 mt-1">最终推荐</p>
          </div>
        </div>
        <!-- 漏斗进度条 -->
        <div class="mt-4 flex items-center gap-2 text-xs opacity-60">
          <div class="flex-1 bg-white/20 rounded-full h-2 overflow-hidden flex">
            <div class="bg-yellow-300 h-full transition-all duration-500" :style="{ width: (funnel.candidateCount > 0 ? (funnel.healthFilteredCount / funnel.candidateCount) * 100 : 0) + '%' }"></div>
          </div>
          <span>{{ funnel.candidateCount > 0 ? Math.round((funnel.healthFilteredCount / funnel.candidateCount) * 100) : 0 }}%通过健康筛选</span>
        </div>
      </div>
    </div>

    <h2 class="text-2xl font-black text-gray-900 mb-8">为您精选</h2>
    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>
    <div v-else-if="recommendedFoods.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
      <p>暂无推荐</p>
    </div>
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <FoodCard
        v-for="food in recommendedFoods"
        :key="food.id"
        v-bind="food"
      />
    </div>

    <div class="mt-12 text-center">
      <button @click="loadMore" :disabled="loadingMore" class="bg-gray-100 text-gray-600 px-8 py-4 rounded-2xl font-bold hover:bg-gray-200 transition-all disabled:opacity-50 cursor-pointer">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { recommendApi, type RecommendResult } from '@/api/recommend'
import { profileApi, type FoodProfile } from '@/api/profile'
import { mapFoodToCard, type FoodCardItem } from '@/utils/mapping'
import FoodCard from '@/components/ui/FoodCard.vue'
import { Sparkles as SparklesIcon } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

import { useAuthStore } from '@/stores/auth'

const { show: showToast } = useToast()

const router = useRouter()
const recommendedFoods = ref<FoodCardItem[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const pageSize = ref(8)
const userProfile = ref<FoodProfile | null>(null)
const authStore = useAuthStore()

const isLoggedIn = computed(() => authStore.isAuthenticated())
const hasProfile = computed(() => {
  const p = userProfile.value
  return !!(p?.tastePreference || p?.allergenInfo || p?.chronicDisease || p?.tabooInfo)
})
const tastePreference = computed(() => userProfile.value?.tastePreference || '')

const funnel = ref<{ candidateCount: number; healthFilteredCount: number; tasteMatchedCount: number; finalCount: number; coldStart: boolean }>({
  candidateCount: 0, healthFilteredCount: 0, tasteMatchedCount: 0, finalCount: 0, coldStart: true
})

async function loadProfile() {
  if (!isLoggedIn.value) return
  try {
    userProfile.value = await profileApi.get()
  } catch {
    userProfile.value = null
  }
}

async function loadFoods() {
  loading.value = true
  try {
    const result = await recommendApi.getFoods(pageSize.value)
    funnel.value = {
      candidateCount: result.candidateCount,
      healthFilteredCount: result.healthFilteredCount,
      tasteMatchedCount: result.tasteMatchedCount,
      finalCount: result.finalCount,
      coldStart: result.coldStart,
    }
    recommendedFoods.value = (result.foods || []).map(r => mapFoodToCard(r))
  } catch (e: unknown) {
    recommendedFoods.value = []
    showToast('加载推荐失败：' + (e instanceof Error ? e.message : '请稍后重试'), 'error')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  try {
    pageSize.value += 8
    const result = await recommendApi.getFoods(pageSize.value)
    funnel.value = {
      candidateCount: result.candidateCount,
      healthFilteredCount: result.healthFilteredCount,
      tasteMatchedCount: result.tasteMatchedCount,
      finalCount: result.finalCount,
      coldStart: result.coldStart,
    }
    recommendedFoods.value = (result.foods || []).map(r => mapFoodToCard(r))
  } catch {
    pageSize.value -= 8
    showToast('加载更多失败', 'error')
  } finally {
    loadingMore.value = false
  }
}

onMounted(async () => {
  await loadProfile()
  await loadFoods()
})
</script>
