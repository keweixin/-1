<template>
  <div class="space-y-8">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black text-gray-900">社区审核</h1>
        <p class="mt-2 text-sm text-gray-500">对用户发布的社区内容进行通过或驳回处理，管理帖子评论。</p>
      </div>
      <div class="flex items-center gap-3">
        <!-- Tab buttons -->
        <div class="flex rounded-2xl border border-gray-200 bg-gray-50 p-1">
          <button @click="activeTab = 'posts'" :class="['px-4 py-2 rounded-xl text-sm font-bold transition-all', activeTab === 'posts' ? 'bg-white text-green-600 shadow-sm' : 'text-gray-500']">帖子管理</button>
          <button @click="activeTab = 'comments'" :class="['px-4 py-2 rounded-xl text-sm font-bold transition-all', activeTab === 'comments' ? 'bg-white text-green-600 shadow-sm' : 'text-gray-500']">评论管理</button>
        </div>
      </div>
    </div>

    <div v-if="feedback" class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'">
      {{ feedback.message }}
    </div>

    <!-- Posts Tab -->
    <template v-if="activeTab === 'posts'">
      <div class="flex items-center gap-3">
        <select v-model="statusFilter" class="rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-medium text-gray-600 outline-none focus:border-green-500">
          <option value="">全部状态</option>
          <option value="待审核">待审核</option>
          <option value="已通过">已通过</option>
          <option value="已驳回">已驳回</option>
        </select>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="loadPosts">刷新</button>
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
              <span v-if="post.recommended === 1" class="px-2 py-0.5 rounded-lg text-[10px] font-bold bg-amber-50 text-amber-600 border border-amber-200">已推荐</span>
              <span class="text-xs text-gray-400">点赞 {{ post.likeCount }} · 评论 {{ post.commentCount }}</span>
            </div>
          </div>

          <h3 class="font-bold text-gray-900 mb-2">{{ post.title }}</h3>
          <p class="text-sm text-gray-600 mb-4 whitespace-pre-line">{{ post.content }}</p>
          <img v-if="post.imgList" :src="getCommunityImage(post.imgList)" alt="帖子配图" class="w-full max-w-md rounded-2xl object-cover mb-4" referrerPolicy="no-referrer" />

          <div class="flex items-center gap-4">
            <button class="px-4 py-2 bg-green-600 text-white rounded-xl text-sm font-bold hover:bg-green-700 transition-all disabled:bg-green-300"
              :disabled="busyId === post.postId || post.auditStatus === '已通过'" @click="auditPost(post, '已通过')">
              {{ busyId === post.postId && targetStatus === '已通过' ? '处理中...' : '通过' }}
            </button>
            <button class="px-4 py-2 bg-red-50 text-red-600 rounded-xl text-sm font-bold hover:bg-red-100 transition-all disabled:opacity-50"
              :disabled="busyId === post.postId || post.auditStatus === '已驳回'" @click="auditPost(post, '已驳回')">
              {{ busyId === post.postId && targetStatus === '已驳回' ? '处理中...' : '驳回' }}
            </button>
            <button class="px-4 py-2 rounded-xl text-sm font-bold transition-all"
              :class="post.recommended === 1 ? 'bg-amber-100 text-amber-700 hover:bg-amber-200' : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
              @click="toggleRecommend(post)">
              {{ post.recommended === 1 ? '取消推荐' : '推荐' }}
            </button>
            <button class="px-4 py-2 border border-red-200 text-red-600 rounded-xl text-sm font-bold hover:bg-red-50 transition-all" @click="deletePost(post.postId)">删除</button>
          </div>
        </div>
      </div>
    </template>

    <!-- Comments Tab -->
    <template v-if="activeTab === 'comments'">
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50">
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">帖子ID</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">评论内容</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">用户ID</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">时间</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-if="commentsLoading">
              <td colspan="5" class="px-8 py-12 text-center text-gray-400">
                <div class="flex items-center justify-center">
                  <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                  加载中...
                </div>
              </td>
            </tr>
            <tr v-else-if="comments.length === 0">
              <td colspan="5" class="px-8 py-12 text-center text-gray-400">暂无评论</td>
            </tr>
            <tr v-else v-for="comment in comments" :key="comment.commentId" class="hover:bg-gray-50 transition-colors">
              <td class="px-8 py-6 text-sm text-gray-900">{{ comment.postId }}</td>
              <td class="px-8 py-6 text-sm text-gray-600 max-w-xs truncate">{{ comment.content }}</td>
              <td class="px-8 py-6 text-sm text-gray-600">{{ comment.userId }}</td>
              <td class="px-8 py-6 text-sm text-gray-600">{{ formatDisplayTime(comment.createTime) }}</td>
              <td class="px-8 py-6">
                <button class="text-red-600 hover:underline text-sm font-medium" @click="deleteComment(comment.commentId)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { communityApi } from '@/api/community'
import type { Post, Comment } from '@/api/community'
import StatusTag from '@/components/ui/StatusTag.vue'
import { getAvatarImage, getCommunityImage } from '@/utils/images'

interface FeedbackState { type: 'success' | 'error'; message: string }

const activeTab = ref<'posts' | 'comments'>('posts')
const posts = ref<Post[]>([])
const comments = ref<Comment[]>([])
const loading = ref(true)
const commentsLoading = ref(false)
const busyId = ref<number | null>(null)
const targetStatus = ref('')
const statusFilter = ref('')
const feedback = ref<FeedbackState | null>(null)

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
  setTimeout(() => { feedback.value = null }, 3000)
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

async function loadComments() {
  commentsLoading.value = true
  try {
    const result = await communityApi.adminListComments({ pageNum: 1, pageSize: 100 })
    comments.value = result.records
  } catch (error) {
    comments.value = []
    setFeedback('error', error instanceof Error ? error.message : '评论加载失败')
  } finally {
    commentsLoading.value = false
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

async function deletePost(postId: number) {
  if (!confirm('确定要删除此帖子吗？')) return
  try {
    await communityApi.deletePost(postId)
    setFeedback('success', '帖子已删除')
    await loadPosts()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '删除失败')
  }
}

async function toggleRecommend(post: Post) {
  try {
    await communityApi.toggleRecommend(post.postId)
    setFeedback('success', post.recommended === 1 ? '已取消推荐' : '已设为推荐')
    await loadPosts()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '操作失败')
  }
}

async function deleteComment(commentId: number) {
  if (!confirm('确定要删除此评论吗？')) return
  try {
    await communityApi.deleteComment(commentId)
    setFeedback('success', '评论已删除')
    await loadComments()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '删除失败')
  }
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

watch(statusFilter, () => { loadPosts() })
watch(activeTab, (tab) => {
  if (tab === 'comments') loadComments()
})

onMounted(loadPosts)
</script>
