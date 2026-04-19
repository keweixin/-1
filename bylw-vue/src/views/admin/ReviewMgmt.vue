<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-black text-gray-900">食品评论管理</h1>
      <p class="mt-2 text-sm text-gray-500">查看与管理用户对食品的评价。</p>
    </div>

    <div v-if="feedback" class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'">
      {{ feedback.message }}
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">食品ID</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">评论内容</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">用户ID</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">评分</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">时间</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading">
            <td colspan="6" class="px-8 py-12 text-center text-gray-400">
              <div class="flex items-center justify-center">
                <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                加载中...
              </div>
            </td>
          </tr>
          <tr v-else-if="reviews.length === 0">
            <td colspan="6" class="px-8 py-12 text-center text-gray-400">暂无评论数据</td>
          </tr>
          <tr v-else v-for="review in reviews" :key="review.reviewId" class="hover:bg-gray-50 transition-colors">
            <td class="px-8 py-6 text-sm text-gray-900">{{ review.foodId }}</td>
            <td class="px-8 py-6 text-sm text-gray-600 max-w-xs truncate">{{ review.content }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ review.userId }}</td>
            <td class="px-8 py-6">
              <div class="flex items-center gap-0.5">
                <span v-for="s in 5" :key="s" class="text-sm" :class="s <= review.rating ? 'text-yellow-400' : 'text-gray-300'">&#9733;</span>
              </div>
            </td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ formatTime(review.createTime) }}</td>
            <td class="px-8 py-6">
              <button class="text-red-600 hover:underline text-sm font-medium" @click="handleDelete(review.reviewId)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="flex justify-center gap-2">
      <button v-for="p in totalPages" :key="p" @click="page = p; loadReviews()"
        class="w-10 h-10 rounded-xl text-sm font-bold transition-all"
        :class="page === p ? 'bg-green-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'">
        {{ p }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { reviewApi, type FoodReview } from '@/api/review'

interface FeedbackState { type: 'success' | 'error'; message: string }

const reviews = ref<FoodReview[]>([])
const loading = ref(true)
const feedback = ref<FeedbackState | null>(null)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize))

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
  setTimeout(() => { feedback.value = null }, 3000)
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

async function loadReviews() {
  loading.value = true
  try {
    const result = await reviewApi.listAllReviews({ pageNum: page.value, pageSize })
    reviews.value = result.records
    total.value = result.total
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '加载评论失败')
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定要删除此评论吗？')) return
  try {
    await reviewApi.deleteReview(id)
    setFeedback('success', '评论已删除')
    await loadReviews()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(loadReviews)
</script>
