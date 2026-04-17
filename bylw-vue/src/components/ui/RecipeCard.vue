<template>
  <div
    class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden group hover:shadow-xl hover:shadow-orange-900/5 transition-all duration-300 flex flex-col h-full"
  >
    <router-link :to="`/recipe/${id}`" class="block relative aspect-video overflow-hidden bg-gray-100">
      <img
        :src="currentImage"
        :alt="name"
        class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700 ease-out"
        referrerPolicy="no-referrer"
        loading="lazy"
        @error="handleImageError"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end p-5">
        <span class="text-white text-xs font-black flex items-center gap-1.5 uppercase tracking-widest">
          查看详情 <ChevronRightIcon class="w-4 h-4" />
        </span>
      </div>
    </router-link>
    <div class="p-5 flex flex-col flex-grow">
      <div class="flex flex-wrap gap-1.5 mb-3">
        <span
          v-for="tag in tags"
          :key="tag"
          class="text-[10px] font-bold text-orange-600 bg-orange-50 px-2 py-0.5 rounded-md border border-orange-100/50"
        >
          {{ tag }}
        </span>
      </div>
      <router-link :to="`/recipe/${id}`" class="block text-base font-black text-gray-900 mb-2 hover:text-green-600 transition-colors">
        {{ name }}
      </router-link>
      <p class="text-xs text-gray-500 line-clamp-2 leading-relaxed font-medium">
        {{ summary }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChevronRight as ChevronRightIcon } from 'lucide-vue-next'
import { getRecipeImage } from '@/utils/images'

const props = defineProps<{
  id: string | number
  name: string
  image: string
  tags: string[]
  summary: string
}>()

const useFallback = ref(false)

const currentImage = computed(() => {
  if (useFallback.value) {
    return getRecipeImage()
  }
  return getRecipeImage(props.image)
})

function handleImageError() {
  useFallback.value = true
}
</script>
