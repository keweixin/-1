<template>
  <div class="mx-auto max-w-5xl px-4 py-12">
    <div class="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <button class="w-fit text-sm font-bold text-green-600 transition-colors hover:text-green-700" @click="router.back()">
        返回社区
      </button>
      <div class="flex items-center gap-5 text-sm text-gray-400">
        <span>点赞 {{ post?.likeCount ?? 0 }}</span>
        <span>评论 {{ post?.commentCount ?? 0 }}</span>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-24 text-gray-400">
      <div class="mr-3 h-6 w-6 animate-spin rounded-full border-2 border-green-600 border-t-transparent"></div>
      帖子加载中...
    </div>

    <template v-else-if="post">
      <article class="rounded-[2rem] border border-gray-100 bg-white p-8 shadow-sm">
        <div class="mb-6 flex items-center gap-3">
          <img
            :src="getAvatarImage(post.userId, String(post.userId))"
            :alt="`用户${post.userId}`"
            class="h-12 w-12 rounded-full bg-gray-100"
            referrerpolicy="no-referrer"
          />
          <div>
            <p class="font-bold text-gray-900">用户 {{ post.userId }}</p>
            <p class="text-sm text-gray-400">{{ formatDisplayTime(post.createTime) }}</p>
          </div>
        </div>

        <h1 class="mb-4 text-3xl font-black text-gray-900">{{ post.title }}</h1>
        <p class="whitespace-pre-wrap text-sm leading-8 text-gray-600">{{ post.content }}</p>

        <img
          v-if="post.imgList"
          :src="getCommunityImage(post.imgList)"
          alt="帖子配图"
          class="mt-6 max-h-[440px] w-full rounded-3xl object-cover"
          referrerpolicy="no-referrer"
        />

        <div class="mt-8 flex flex-wrap items-center gap-4 border-t border-gray-100 pt-6">
          <button
            class="inline-flex items-center gap-2 rounded-2xl px-4 py-2 text-sm font-bold transition-all"
            :class="post.liked ? 'bg-red-50 text-red-500' : 'bg-gray-50 text-gray-600 hover:bg-red-50 hover:text-red-500'"
            @click="handleLike"
          >
            <HeartIcon class="h-4 w-4" :fill="post.liked ? 'currentColor' : 'none'" />
            {{ post.likeCount }}
          </button>

          <span class="rounded-2xl bg-gray-50 px-4 py-2 text-sm font-bold text-gray-600">
            评论 {{ post.commentCount }}
          </span>
        </div>
      </article>

      <section class="mt-8 rounded-[2rem] border border-gray-100 bg-white p-8 shadow-sm">
        <div class="mb-6">
          <h2 class="text-2xl font-black text-gray-900">评论区</h2>
          <p class="mt-2 text-sm text-gray-500">评论提交后会立即调用后端接口保存。</p>
        </div>

        <div class="rounded-[1.75rem] border border-gray-100 bg-gray-50 p-5">
          <textarea
            v-model="commentForm"
            rows="4"
            placeholder="写下你的评论..."
            class="w-full resize-none rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm outline-none transition-all focus:border-green-600"
          ></textarea>

          <div class="mt-4 flex justify-end">
            <button
              class="rounded-2xl bg-green-600 px-5 py-3 text-sm font-black text-white transition-all hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-green-300"
              :disabled="commentSubmitting"
              @click="submitComment"
            >
              {{ commentSubmitting ? '提交中...' : '发表评论' }}
            </button>
          </div>
        </div>

        <div v-if="commentsLoading" class="flex items-center justify-center py-12 text-gray-400">
          <div class="mr-3 h-5 w-5 animate-spin rounded-full border-2 border-green-600 border-t-transparent"></div>
          评论加载中...
        </div>
        <div v-else-if="comments.length === 0" class="py-12 text-center text-gray-400">
          暂时还没有评论，来抢沙发吧。
        </div>
        <div v-else class="mt-6 space-y-4">
          <article
            v-for="comment in comments"
            :key="comment.commentId"
            class="rounded-2xl border border-gray-100 bg-gray-50 p-5"
          >
            <div class="mb-3 flex items-center justify-between gap-3">
              <span class="text-sm font-bold text-gray-900">用户 {{ comment.userId }}</span>
              <span class="text-xs text-gray-400">{{ formatDisplayTime(comment.createTime) }}</span>
            </div>
            <p class="whitespace-pre-wrap text-sm leading-7 text-gray-600">{{ comment.content }}</p>
          </article>
        </div>
      </section>
    </template>

    <div v-else class="rounded-[2rem] border border-gray-100 bg-white py-24 text-center text-gray-400 shadow-sm">
      当前帖子不存在或已被删除。
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Heart as HeartIcon } from 'lucide-vue-next'
import { communityApi, type Comment, type Post } from '@/api/community'
import { hasLikedCommunityPost, markCommunityPostLiked, unmarkCommunityPostLiked } from '@/utils/communityLikes'
import { getAvatarImage, getCommunityImage } from '@/utils/images'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

type CommunityPost = Post & { liked: boolean }

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const commentsLoading = ref(false)
const commentSubmitting = ref(false)
const post = ref<CommunityPost | null>(null)
const comments = ref<Comment[]>([])
const commentForm = ref('')

function getPostId() {
  return Number(route.params.id)
}

async function loadPost() {
  loading.value = true
  try {
    const data = await communityApi.getPost(getPostId())
    post.value = data
      ? {
          ...data,
          liked: hasLikedCommunityPost(data.postId),
        }
      : null
  } catch {
    post.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  commentsLoading.value = true
  try {
    const result = await communityApi.getComments(getPostId(), { pageNum: 1, pageSize: 100 })
    comments.value = result.records || []
  } catch {
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

async function handleLike() {
  if (!post.value) return

  try {
    if (post.value.liked) {
      await communityApi.unlikePost(post.value.postId)
      post.value.likeCount = Math.max(0, post.value.likeCount - 1)
      post.value.liked = false
      unmarkCommunityPostLiked(post.value.postId)
    } else {
      await communityApi.likePost(post.value.postId)
      post.value.likeCount += 1
      post.value.liked = true
      markCommunityPostLiked(post.value.postId)
    }
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : '操作失败，请先登录', 'error')
  }
}

async function submitComment() {
  if (!post.value) return

  const content = commentForm.value.trim()
  if (!content) {
    showToast('请输入评论内容', 'warning')
    return
  }

  commentSubmitting.value = true
  try {
    const created = await communityApi.comment({
      postId: post.value.postId,
      content,
    })

    comments.value.unshift(created)
    post.value.commentCount += 1
    commentForm.value = ''
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : '评论失败，请先登录', 'error')
  } finally {
    commentSubmitting.value = false
  }
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(async () => {
  await Promise.all([loadPost(), loadComments()])
})
</script>
