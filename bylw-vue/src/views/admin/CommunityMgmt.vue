<template>
  <div class="space-y-8">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black text-gray-900">社区审核</h1>
        <p class="mt-2 text-sm text-gray-500">对用户发布的社区内容进行通过或驳回处理。</p>
      </div>
      <div class="flex items-center gap-3">
        <select v-model="statusFilter" class="rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-medium text-gray-600 outline-none focus:border-green-500">
          <option value="">全部状态</option>
          <option value="待审核">待审核</option>
          <option value="已通过">已通过</option>
          <option value="已驳回">已驳回</option>
        </select>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="loadPosts">
          刷新
        </button>
      </div>
    </div>

    <div
      v-if="feedback"
      class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'"
    >
      {{ feedback.message }}
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>
    <div v-else-if="posts.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
      <p>暂无帖子</p>
    </div>
    <div v-else class="space-y-6">
      <div v-for="post in posts" :key="post.postId" class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6">
        <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-4">
          <div class="flex items-center gap-3">
            <img :src="getAvatarImage(post.userId, String(post.userId))" :alt="String(post.userId)" class="w-10 h-10 rounded-full bg-gray-100" referrerPolicy="no-referrer" />
            <div>
              <p class="font-bold text-gray-900">用户{{ post.userId }}</p>
              <p class="text-xs text-gray-400">{{ formatDisplayTime(post.createTime) }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <StatusTag :status="post.auditStatus" />
            <span class="text-xs text-gray-400">点赞 {{ post.likeCount }} · 评论 {{ post.commentCount }}</span>
          </div>
        </div>

        <h3 class="font-bold text-gray-900 mb-2">{{ post.title }}</h3>
        <p class="text-sm text-gray-600 mb-4 whitespace-pre-line">{{ post.content }}</p>
        <img v-if="post.imgList" :src="getCommunityImage(post.imgList)" alt="帖子配图" class="w-full max-w-md rounded-2xl object-cover mb-4" referrerPolicy="no-referrer" />

        <div class="flex items-center gap-4">
          <button
            class="px-4 py-2 bg-green-600 text-white rounded-xl text-sm font-bold hover:bg-green-700 transition-all disabled:bg-green-300"
            :disabled="busyId === post.postId || post.auditStatus === '已通过'"
            @click="auditPost(post, '已通过')"
          >
            {{ busyId === post.postId && targetStatus === '已通过' ? '处理中...' : '通过' }}
          </button>
          <button
            class="px-4 py-2 bg-red-50 text-red-600 rounded-xl text-sm font-bold hover:bg-red-100 transition-all disabled:opacity-50"
            :disabled="busyId === post.postId || post.auditStatus === '已驳回'"
            @click="auditPost(post, '已驳回')"
          >
            {{ busyId === post.postId && targetStatus === '已驳回' ? '处理中...' : '驳回' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { communityApi, type Post } from '@/api/community'
import StatusTag from '@/components/ui/StatusTag.vue'
import { getAvatarImage, getCommunityImage } from '@/utils/images'

interface FeedbackState {
  type: 'success' | 'error'
  message: string
}

const posts = ref<Post[]>([])
const loading = ref(true)
const busyId = ref<number | null>(null)
const targetStatus = ref('')
const statusFilter = ref('')
const feedback = ref<FeedbackState | null>(null)

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
}

async function loadPosts() {
  loading.value = true
  try {
    const result = await communityApi.adminListPosts({
      pageNum: 1,
      pageSize: 100,
      auditStatus: statusFilter.value || undefined,
    })
    posts.value = result.records
  } catch (error) {
    posts.value = []
    setFeedback('error', error instanceof Error ? error.message : '社区帖子加载失败')
  } finally {
    loading.value = false
  }
}

async function auditPost(post: Post, status: '已通过' | '已驳回') {
  busyId.value = post.postId
  targetStatus.value = status
  try {
    await communityApi.auditPost(post.postId, status)
    setFeedback('success', `帖子已${status === '已通过' ? '审核通过' : '驳回'}。`)
    await loadPosts()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '审核操作失败')
  } finally {
    busyId.value = null
    targetStatus.value = ''
  }
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

watch(statusFilter, () => { loadPosts() })

onMounted(loadPosts)
</script>
