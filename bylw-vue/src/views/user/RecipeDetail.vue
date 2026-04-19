<template>
  <div class="max-w-4xl mx-auto px-4 py-12">
    <nav class="flex items-center gap-2 text-sm text-gray-400 mb-8">
      <router-link to="/" class="hover:text-green-600 transition-colors">首页</router-link>
      <ChevronRightIcon class="w-4 h-4" />
      <router-link to="/recipes" class="hover:text-green-600 transition-colors">健康食谱</router-link>
      <ChevronRightIcon class="w-4 h-4" />
      <span class="text-gray-900 font-medium">{{ recipe.name }}</span>
    </nav>

    <div v-if="loading" class="flex items-center justify-center py-24">
      <div class="w-8 h-8 border-3 border-green-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <div v-else-if="!recipeData" class="text-center py-24 text-gray-400">食谱不存在</div>

    <div v-else class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="aspect-video overflow-hidden">
        <img :src="currentRecipeImage" :alt="recipe.name" class="w-full h-full object-cover" referrerPolicy="no-referrer" @error="handleImageError" />
      </div>
      <div class="p-12">
        <div class="flex flex-wrap gap-2 mb-6">
          <span v-for="tag in recipe.tags" :key="tag" class="text-[10px] font-bold text-orange-600 bg-orange-50 px-3 py-1 rounded-xl border border-orange-100 uppercase tracking-widest">
            {{ tag }}
          </span>
        </div>
        <h1 class="text-3xl font-black text-gray-900 mb-6">{{ recipe.name }}</h1>

        <div class="space-y-8">
          <section>
            <h3 class="text-lg font-bold text-gray-900 mb-3">简介</h3>
            <p class="text-gray-600 leading-relaxed">{{ recipe.summary }}</p>
          </section>
          <section>
            <h3 class="text-lg font-bold text-gray-900 mb-3">制作步骤</h3>
            <p class="text-gray-600 leading-relaxed whitespace-pre-line">{{ recipe.content }}</p>
          </section>
          <section>
            <h3 class="text-lg font-bold text-gray-900 mb-3">适合人群</h3>
            <p class="text-gray-600 leading-relaxed">{{ recipe.suitable }}</p>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi, type Recipe } from '@/api/article'
import { recommendApi } from '@/api/recommend'
import { getRecipeImage } from '@/utils/images'
import { ChevronRight as ChevronRightIcon } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const recipeData = ref<Recipe | null>(null)
const loading = ref(true)
const imageError = ref(false)

const currentRecipeImage = computed(() => {
  if (imageError.value) {
    return getRecipeImage()
  }
  return recipe.value.image
})

function handleImageError() {
  imageError.value = true
}

async function loadRecipe() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    recipeData.value = await articleApi.getRecipe(id)
    // 记录浏览行为
    if (authStore.userId) {
      recommendApi.recordBehavior({ userId: authStore.userId, targetType: 'recipe', targetId: id, behaviorType: 'view' }).catch(() => {})
    }
  } catch {
    recipeData.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadRecipe)

// Map API response to template's expected shape
const recipe = computed(() => {
  if (!recipeData.value) {
    return {
      name: '加载中...',
      image: getRecipeImage(),
      tags: [],
      summary: '',
      content: '',
      suitable: '',
    }
  }
  const r = recipeData.value
  return {
    name: r.title,
    image: getRecipeImage(r.coverImg),
    tags: r.collectCount > 0 ? ['精选'] : [],
    summary: r.summary,
    content: r.content,
    suitable: r.suitablePeople,
  }
})
</script>
