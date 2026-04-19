<template>
  <div class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-gray-900">内容管理</h1>
        <p class="mt-2 text-sm text-gray-500">统一维护膳食百科文章与健康食谱内容。</p>
      </div>
      <button
        class="bg-green-600 text-white px-6 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all shadow-lg shadow-green-100"
        @click="openCreateModal"
      >
        添加内容
      </button>
    </div>

    <div
      v-if="feedback"
      class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'"
    >
      {{ feedback.message }}
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-2 flex flex-wrap gap-2">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="px-5 py-3 rounded-2xl text-sm font-bold transition-all"
        :class="activeTab === tab.key ? 'bg-green-600 text-white shadow-lg shadow-green-100' : 'text-gray-500 hover:bg-gray-50'"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>
    <div v-else-if="items.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
      <p>暂无内容</p>
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div v-for="item in items" :key="getItemId(item)" class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <div class="aspect-video overflow-hidden">
          <img :src="activeTab === 'article' ? getArticleImage(item.coverImg) : getRecipeImage(item.coverImg)" :alt="item.title" class="w-full h-full object-cover" referrerPolicy="no-referrer" />
        </div>
        <div class="p-6">
          <div class="flex items-start justify-between gap-4">
            <div>
              <h3 class="font-bold text-gray-900 mb-2">{{ item.title }}</h3>
              <p class="text-sm text-gray-500 mb-3">{{ item.summary }}</p>
            </div>
            <StatusTag :status="'已发布'" />
          </div>
          <p v-if="activeTab === 'recipe'" class="text-xs text-green-600 bg-green-50 border border-green-100 rounded-xl px-3 py-2 inline-flex">
            适宜人群：{{ getRecipeSuitablePeople(item) }}
          </p>
          <div class="mt-5 flex items-center justify-between">
            <span class="text-xs text-gray-400">
              {{ formatDisplayTime(item.publishTime) }}
              · {{ getItemMetric(item) }}
            </span>
            <div class="flex items-center gap-3">
              <button class="text-green-600 hover:underline text-sm font-medium" @click="openEditModal(item)">编辑</button>
              <button class="text-red-600 hover:underline text-sm font-medium" @click="removeItem(item)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="modalVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4" @click.self="closeModal">
      <div class="w-full max-w-3xl bg-white rounded-[32px] shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">{{ form.id ? `编辑${currentLabel}` : `添加${currentLabel}` }}</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="closeModal">
            <XIcon class="w-5 h-5" />
          </button>
        </div>

        <div class="space-y-4">
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">标题</span>
            <input v-model="form.title" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">摘要</span>
            <textarea v-model="form.summary" rows="3" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500 resize-none" />
          </label>
          <label v-if="activeTab === 'recipe'" class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">适宜人群</span>
            <input v-model="form.suitablePeople" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">封面图片</span>
            <ImageUpload v-model="form.coverImg" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">正文内容</span>
            <textarea v-model="form.content" rows="10" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500 resize-none" />
          </label>
        </div>

        <div class="flex items-center justify-end gap-3 mt-8">
          <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="closeModal">
            取消
          </button>
          <button
            class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all disabled:bg-green-300"
            :disabled="submitting"
            @click="submitContent"
          >
            {{ submitting ? '保存中...' : '保存内容' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { articleApi, type Article, type Recipe } from '@/api/article'
import StatusTag from '@/components/ui/StatusTag.vue'
import { getArticleImage, getRecipeImage } from '@/utils/images'
import ImageUpload from '@/components/ui/ImageUpload.vue'
import { X } from 'lucide-vue-next'

type ContentType = 'article' | 'recipe'

interface FeedbackState {
  type: 'success' | 'error'
  message: string
}

type ContentItem = Article | Recipe

type ContentForm = {
  id: number | null
  title: string
  summary: string
  content: string
  coverImg: string
  suitablePeople: string
}

const XIcon = X
const tabs: Array<{ key: ContentType; label: string }> = [
  { key: 'article', label: '百科文章' },
  { key: 'recipe', label: '健康食谱' },
]
const activeTab = ref<ContentType>('article')
const items = ref<ContentItem[]>([])
const loading = ref(true)
const modalVisible = ref(false)
const submitting = ref(false)
const feedback = ref<FeedbackState | null>(null)
const form = reactive<ContentForm>(createEmptyForm())

const currentLabel = computed(() => (activeTab.value === 'article' ? '百科文章' : '健康食谱'))

function createEmptyForm(): ContentForm {
  return {
    id: null,
    title: '',
    summary: '',
    content: '',
    coverImg: '',
    suitablePeople: '',
  }
}

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
}

async function loadItems() {
  loading.value = true
  try {
    if (activeTab.value === 'article') {
      const result = await articleApi.listArticles({ pageNum: 1, pageSize: 50 })
      items.value = result.records
    } else {
      const result = await articleApi.listRecipes({ pageNum: 1, pageSize: 50 })
      items.value = result.records
    }
  } catch (error) {
    items.value = []
    setFeedback('error', error instanceof Error ? error.message : '内容数据加载失败')
  } finally {
    loading.value = false
  }
}

async function switchTab(tab: ContentType) {
  activeTab.value = tab
  closeModal()
  await loadItems()
}

function openCreateModal() {
  Object.assign(form, createEmptyForm())
  modalVisible.value = true
}

function openEditModal(item: ContentItem) {
  Object.assign(form, {
    id: getItemId(item),
    title: item.title || '',
    summary: item.summary || '',
    content: item.content || '',
    coverImg: item.coverImg || '',
    suitablePeople: 'suitablePeople' in item ? item.suitablePeople || '' : '',
  })
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
  Object.assign(form, createEmptyForm())
}

async function submitContent() {
  if (!form.title.trim() || !form.summary.trim() || !form.content.trim()) {
    setFeedback('error', '请完整填写标题、摘要和正文内容。')
    return
  }

  submitting.value = true
  try {
    if (activeTab.value === 'article') {
      const articleData = {
        articleId: form.id ?? undefined,
        title: form.title.trim(),
        summary: form.summary.trim(),
        content: form.content.trim(),
        coverImg: form.coverImg.trim(),
        status: 1,
      }
      if (form.id) {
        await articleApi.updateArticle(articleData)
      } else {
        await articleApi.saveArticle(articleData)
      }
    } else {
      const recipeData = {
        recipeId: form.id ?? undefined,
        title: form.title.trim(),
        summary: form.summary.trim(),
        content: form.content.trim(),
        coverImg: form.coverImg.trim(),
        suitablePeople: form.suitablePeople.trim(),
        status: 1,
      }
      if (form.id) {
        await articleApi.updateRecipe(recipeData)
      } else {
        await articleApi.saveRecipe(recipeData)
      }
    }

    setFeedback('success', `${currentLabel.value}已成功保存。`)
    closeModal()
    await loadItems()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '内容保存失败')
  } finally {
    submitting.value = false
  }
}

async function removeItem(item: ContentItem) {
  if (!window.confirm(`确定删除“${item.title}”吗？`)) {
    return
  }

  try {
    if (activeTab.value === 'article') {
      await articleApi.deleteArticle((item as Article).articleId)
    } else {
      await articleApi.deleteRecipe((item as Recipe).recipeId)
    }
    setFeedback('success', `${currentLabel.value}已删除。`)
    await loadItems()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '内容删除失败')
  }
}

function getItemId(item: ContentItem) {
  return activeTab.value === 'article' ? (item as Article).articleId : (item as Recipe).recipeId
}

function getRecipeSuitablePeople(item: ContentItem) {
  return 'suitablePeople' in item ? item.suitablePeople || '不限' : '不限'
}

function getItemMetric(item: ContentItem) {
  return activeTab.value === 'article'
    ? `${('readCount' in item ? item.readCount : 0) || 0} 阅读`
    : `${('collectCount' in item ? item.collectCount : 0) || 0} 收藏`
}

function formatDisplayTime(value?: string) {
  if (!value) return '刚刚发布'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(loadItems)
</script>
