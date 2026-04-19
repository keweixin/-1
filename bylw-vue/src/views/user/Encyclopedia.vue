<template>
  <div class="max-w-7xl mx-auto px-4 py-12">
    <div class="text-center mb-16">
      <h1 class="text-4xl font-black text-gray-900 mb-4">膳食百科</h1>
      <p class="text-gray-500">了解临期食品知识，科学健康饮食</p>
      <div class="mt-6 max-w-md mx-auto">
        <input
          v-model.trim="searchKeyword"
          class="w-full rounded-2xl border border-gray-200 bg-white px-5 py-3 text-sm outline-none focus:border-green-500 shadow-sm transition"
          placeholder="搜索文章标题..."
          @keyup.enter="loadArticles"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
      <div v-if="loading" class="col-span-2 flex items-center justify-center py-20 text-gray-400">
        <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
        加载中...
      </div>
      <router-link
        v-else-if="articles.length > 0"
        v-for="(item, idx) in articles"
        :key="item.articleId"
        :to="`/encyclopedia/${item.articleId}`"
        class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden hover:shadow-xl transition-all group"
      >
        <div class="aspect-video overflow-hidden">
          <img :src="getArticleImage(item, idx)" :alt="item.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" referrerPolicy="no-referrer" @error="handleArticleImgError(idx)" />
        </div>
        <div class="p-8">
          <h2 class="text-xl font-black text-gray-900 mb-3 group-hover:text-green-600 transition-colors">{{ item.title }}</h2>
          <p class="text-gray-500 leading-relaxed mb-4">{{ item.summary }}</p>
          <div class="flex items-center justify-between text-sm text-gray-400">
            <span>{{ item.publishTime }}</span>
            <span>{{ item.readCount }} 阅读</span>
          </div>
        </div>
      </router-link>
      <div v-else class="col-span-2 flex flex-col items-center justify-center py-20 text-gray-400">
        <p>暂无文章</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import type { Article } from '@/api/article'
import { getArticleImage as getArticleCover } from '@/utils/images'

const articles = ref<Article[]>([])
const loading = ref(true)
const searchKeyword = ref('')
const articleImgErrorFlags = ref<Record<number, boolean>>({})

function handleArticleImgError(idx: number) {
  articleImgErrorFlags.value[idx] = true
}

function getArticleImage(item: Article, idx: number): string {
  if (articleImgErrorFlags.value[idx]) {
    return getArticleCover()
  }
  return getArticleCover(item.coverImg)
}

async function loadArticles() {
  loading.value = true
  try {
    const result = await articleApi.listArticles({ pageNum: 1, pageSize: 20, keyword: searchKeyword.value || undefined })
    articles.value = result.records
  } catch {
    articles.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadArticles)
</script>
