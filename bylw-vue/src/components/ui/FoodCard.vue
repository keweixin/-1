<template>
  <div
    class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden group hover:shadow-xl hover:shadow-green-900/5 transition-all duration-300 flex flex-col h-full"
  >
    <router-link :to="`/food/${id}`" class="block relative aspect-[4/3] overflow-hidden bg-gray-100">
      <img
        :src="currentImage"
        :alt="name"
        class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700 ease-out"
        referrerpolicy="no-referrer"
        loading="lazy"
        @error="handleImageError"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

      <div class="absolute top-3 left-3 flex flex-col gap-1.5 z-10">
        <div class="bg-orange-500 text-white text-[10px] font-black px-2.5 py-1 rounded-lg shadow-lg shadow-orange-500/20 uppercase tracking-widest">
          {{ discount }}% OFF
        </div>
        <div
          v-if="expiryDays <= 2"
          class="bg-red-500 text-white text-[10px] font-black px-2.5 py-1 rounded-lg shadow-lg shadow-red-500/20 uppercase tracking-widest flex items-center gap-1"
        >
          <ClockIcon class="w-3 h-3" />
          仅剩 {{ expiryDays }} 天
        </div>
      </div>

      <button @click.stop="handleFavorite" class="absolute top-3 right-3 p-2 bg-white/80 backdrop-blur-md rounded-full text-gray-400 hover:text-red-500 transition-all opacity-0 group-hover:opacity-100 translate-y-2 group-hover:translate-y-0 z-10">
        <HeartIcon class="w-4 h-4" />
      </button>

      <div
        v-if="matchScore != null && matchScore > 0"
        class="absolute bottom-3 right-3 z-10"
      >
        <div class="bg-green-600 text-white text-[10px] font-black px-2 py-1 rounded-lg shadow-lg flex items-center gap-1">
          <SparklesIcon class="w-3 h-3" />
          {{ Math.round(matchScore * 100) }}% 匹配
        </div>
      </div>
    </router-link>

    <div class="p-5 flex flex-col flex-grow">
      <div class="flex flex-wrap gap-1.5 mb-3">
        <span
          v-for="tag in tags"
          :key="tag"
          class="text-[10px] font-bold text-green-600 bg-green-50 px-2 py-0.5 rounded-md border border-green-100/50"
        >
          {{ tag }}
        </span>
      </div>

      <router-link :to="`/food/${id}`" class="block text-base font-black text-gray-900 mb-1.5 hover:text-green-600 transition-colors line-clamp-1">
        {{ name }}
      </router-link>

      <div class="flex items-center gap-1.5 text-xs text-gray-400 mb-4">
        <StoreIcon class="w-3.5 h-3.5" />
        <span class="truncate font-medium">{{ store }}</span>
      </div>

      <div class="mt-auto pt-4 border-t border-gray-50 flex items-center justify-between">
        <div class="flex flex-col">
          <span class="text-xs text-gray-400 line-through font-medium">¥{{ originalPrice.toFixed(2) }}</span>
          <div class="flex items-baseline gap-0.5">
            <span class="text-xs font-black text-orange-500">¥</span>
            <span class="text-xl font-black text-orange-500 tracking-tight">
              {{ price.toFixed(2) }}
            </span>
          </div>
        </div>
        <button @click.stop="$router.push(`/food/${id}`)" class="bg-green-600 hover:bg-green-700 text-white p-2.5 rounded-xl transition-all shadow-lg shadow-green-600/10 active:scale-90 group/btn cursor-pointer">
          <ShoppingCartIcon class="w-5 h-5 group-hover/btn:scale-110 transition-transform" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ShoppingBag as ShoppingCartIcon, Heart as HeartIcon, Store as StoreIcon, Clock as ClockIcon, Sparkles as SparklesIcon } from 'lucide-vue-next'
import { recommendApi } from '@/api/recommend'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
import { getFoodImage } from '@/utils/images'

const { show: showToast } = useToast()
const authStore = useAuthStore()

const props = defineProps<{
  id: string | number
  name: string
  image: string
  price: number
  originalPrice: number
  expiryDays: number
  stock: number
  store: string
  tags: string[]
  matchScore?: number
}>()

const useFallback = ref(false)

const currentImage = computed(() => {
  if (useFallback.value) {
    return getFoodImage()
  }
  return getFoodImage(props.image)
})

function handleImageError() {
  useFallback.value = true
}

const discount = computed(() => Math.round((1 - props.price / props.originalPrice) * 100))

async function handleFavorite() {
  if (!authStore.userId) {
    showToast('请先登录', 'warning')
    return
  }
  try {
    await recommendApi.recordBehavior({ userId: authStore.userId, targetType: 'food', targetId: Number(props.id), behaviorType: 'favorite' })
    showToast('收藏成功', 'success')
  } catch (e: unknown) {
    showToast('收藏失败：' + (e instanceof Error ? e.message : '请先登录'), 'error')
  }
}
</script>
