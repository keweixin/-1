<template>
  <div class="max-w-7xl mx-auto px-4 py-12">
    <div class="text-center mb-16">
      <h1 class="text-4xl font-black text-gray-900 mb-4">健康食谱</h1>
      <p class="text-gray-500">科学膳食，让每一口都充满营养</p>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>
    <div v-else-if="recipes.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <RecipeCard
        v-for="recipe in recipes"
        :key="recipe.id"
        v-bind="recipe"
      />
    </div>
    <div v-else class="flex flex-col items-center justify-center py-20 text-gray-400">
      <p>暂无食谱</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import type { Recipe } from '@/api/article'
import RecipeCard from '@/components/ui/RecipeCard.vue'
import { getRecipeImage } from '@/utils/images'

const recipes = ref<{ id: number; name: string; image: string; tags: string[]; summary: string }[]>([])
const loading = ref(true)

async function loadRecipes() {
  loading.value = true
  try {
    const result = await articleApi.listRecipes({ pageNum: 1, pageSize: 20 })
    recipes.value = result.records.map((r: Recipe) => ({
      id: r.recipeId,
      name: r.title,
      image: getRecipeImage(r.coverImg),
      tags: [],
      summary: r.summary,
    }))
  } catch {
    recipes.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadRecipes)
</script>
