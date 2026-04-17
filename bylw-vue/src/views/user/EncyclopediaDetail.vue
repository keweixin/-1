<template>
  <div class="max-w-4xl mx-auto px-4 py-12">
    <nav class="flex items-center gap-2 text-sm text-gray-400 mb-8">
      <router-link to="/" class="hover:text-green-600 transition-colors">首页</router-link>
      <ChevronRightIcon class="w-4 h-4" />
      <router-link to="/encyclopedia" class="hover:text-green-600 transition-colors">膳食百科</router-link>
      <ChevronRightIcon class="w-4 h-4" />
      <span class="text-gray-900 font-medium">{{ article.title }}</span>
    </nav>

    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>

    <article v-else class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="aspect-video overflow-hidden">
        <img :src="currentImage" :alt="article.title" class="w-full h-full object-cover" referrerPolicy="no-referrer" @error="handleImageError" />
      </div>
      <div class="p-12">
        <h1 class="text-3xl font-black text-gray-900 mb-6">{{ article.title }}</h1>
        <div class="flex items-center gap-6 text-sm text-gray-400 mb-8 pb-8 border-b border-gray-100">
          <span>{{ article.date }}</span>
          <span>{{ article.views }} 阅读</span>
        </div>
        <div class="prose prose-lg max-w-none">
          <p class="text-gray-600 leading-relaxed">{{ article.content }}</p>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '@/api/article'
import { recommendApi } from '@/api/recommend'
import { ChevronRight as ChevronRightIcon } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { getArticleImage } from '@/utils/images'

const route = useRoute()
const authStore = useAuthStore()
const loading = ref(true)
const imgError = ref(false)
const article = ref<{ title: string; image: string; date: string; views: number; content: string }>({
  title: '',
  image: '',
  date: '',
  views: 0,
  content: '',
})

const currentImage = computed(() => {
  if (imgError.value) {
    return getArticleImage()
  }
  return getArticleImage(article.value.image)
})

function handleImageError() {
  imgError.value = true
}

async function loadArticle() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const data = await articleApi.getArticle(id)
    article.value = {
      title: data.title || '',
      image: getArticleImage(data.coverImg),
      date: data.publishTime || '',
      views: data.readCount || 0,
      content: data.content || '',
    }
    // 记录浏览行为
    if (authStore.userId) {
      recommendApi.recordBehavior({ userId: authStore.userId, targetType: 'article', targetId: id, behaviorType: 'read' }).catch(() => {})
    }
  } catch {
    article.value = { title: '加载失败', image: '', date: '', views: 0, content: '文章加载失败，请返回重试。' }
  } finally {
    loading.value = false
  }
}

onMounted(loadArticle)
</script>
