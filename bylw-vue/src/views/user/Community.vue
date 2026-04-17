<template>
  <div class="mx-auto max-w-7xl px-4 py-12">
    <div class="mb-12 flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <p class="text-sm font-bold uppercase tracking-[0.24em] text-green-600">Community</p>
        <h1 class="mt-3 text-4xl font-black text-gray-900">社区动态</h1>
        <p class="mt-3 text-sm text-gray-500">帖子支持真实跳转、点赞和评论，不再是摆设按钮。</p>
      </div>

      <button
        class="inline-flex items-center justify-center rounded-2xl bg-green-600 px-6 py-3 text-sm font-black text-white shadow-lg shadow-green-100 transition-all hover:bg-green-700"
        @click="showPublishModal = true"
      >
        发布动态
      </button>
    </div>

    <div class="grid grid-cols-1 gap-8 md:grid-cols-2">
      <div v-if="loading" class="col-span-2 flex items-center justify-center py-24 text-gray-400">
        <div class="mr-3 h-6 w-6 animate-spin rounded-full border-2 border-green-600 border-t-transparent"></div>
        社区内容加载中...
      </div>

      <template v-else-if="posts.length > 0">
        <article
          v-for="post in posts"
          :key="post.postId"
          class="cursor-pointer rounded-[2rem] border border-gray-100 bg-white p-6 shadow-sm transition-all hover:-translate-y-1 hover:shadow-md"
          @click="openPost(post.postId)"
        >
          <div class="mb-4 flex items-center gap-3">
            <img
              :src="getAvatarImage(post.userId, String(post.userId))"
              :alt="`用户${post.userId}`"
              class="h-11 w-11 rounded-full bg-gray-100"
              referrerpolicy="no-referrer"
            />
            <div>
              <p class="text-sm font-bold text-gray-900">用户 {{ post.userId }}</p>
              <p class="text-xs text-gray-400">{{ formatDisplayTime(post.createTime) }}</p>
            </div>
          </div>

          <h2 class="mb-3 text-xl font-black text-gray-900">{{ post.title }}</h2>
          <p class="mb-5 line-clamp-3 text-sm leading-7 text-gray-600">{{ post.content }}</p>

          <img
            v-if="post.imgList"
            :src="getCommunityImage(post.imgList)"
            alt="帖子配图"
            class="mb-5 h-52 w-full rounded-3xl object-cover"
            referrerpolicy="no-referrer"
          />

          <div class="flex items-center gap-6 border-t border-gray-100 pt-4 text-sm text-gray-500">
            <button
              class="inline-flex items-center gap-2 transition-colors"
              :class="post.liked ? 'text-red-500' : 'hover:text-red-500'"
              @click.stop="handleLike(post)"
            >
              <HeartIcon class="h-4 w-4" :fill="post.liked ? 'currentColor' : 'none'" />
              {{ post.likeCount }}
            </button>

            <button
              class="inline-flex items-center gap-2 transition-colors hover:text-green-600"
              @click.stop="openPost(post.postId)"
            >
              <MessageSquareIcon class="h-4 w-4" />
              {{ post.commentCount }}
            </button>
          </div>
        </article>
      </template>

      <div v-else class="col-span-2 flex flex-col items-center justify-center py-24 text-gray-400">
        <MessageSquareIcon class="mb-4 h-14 w-14 opacity-30" />
        <p class="text-base font-medium">当前还没有社区帖子</p>
      </div>
    </div>

    <div
      v-if="showPublishModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/55 px-4 backdrop-blur-sm"
      @click.self="showPublishModal = false"
    >
      <div class="w-full max-w-xl rounded-[2rem] bg-white p-8 shadow-2xl">
        <h2 class="text-2xl font-black text-gray-900">发布动态</h2>
        <p class="mt-2 text-sm text-gray-500">提交后将进入社区审核流程，通过后会展示给所有用户。</p>

        <div class="mt-6 space-y-5">
          <div>
            <label class="mb-2 block text-xs font-bold uppercase tracking-[0.24em] text-gray-400">标题</label>
            <input
              v-model="newPost.title"
              type="text"
              placeholder="请输入帖子标题"
              class="w-full rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm outline-none transition-all focus:border-green-600 focus:bg-white"
            />
          </div>

          <div>
            <label class="mb-2 block text-xs font-bold uppercase tracking-[0.24em] text-gray-400">内容</label>
            <textarea
              v-model="newPost.content"
              rows="5"
              placeholder="分享你的环保心得、食材做法或购物体验"
              class="w-full resize-none rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm outline-none transition-all focus:border-green-600 focus:bg-white"
            ></textarea>
          </div>

          <div>
            <label class="mb-2 block text-xs font-bold uppercase tracking-[0.24em] text-gray-400">配图</label>
            <ImageUpload v-model="newPost.imgList" />
          </div>
        </div>

        <div class="mt-7 flex gap-3">
          <button
            class="flex-1 rounded-2xl bg-gray-100 py-3 text-sm font-bold text-gray-600 transition-all hover:bg-gray-200"
            @click="showPublishModal = false"
          >
            取消
          </button>
          <button
            class="flex-1 rounded-2xl bg-green-600 py-3 text-sm font-black text-white transition-all hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-green-300"
            :disabled="publishing"
            @click="handlePublish"
          >
            {{ publishing ? '发布中...' : '提交发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Heart as HeartIcon, MessageSquare as MessageSquareIcon } from 'lucide-vue-next'
import { communityApi, type Post } from '@/api/community'
import { getLikedCommunityPostIds, hasLikedCommunityPost, markCommunityPostLiked, unmarkCommunityPostLiked } from '@/utils/communityLikes'
import { getAvatarImage, getCommunityImage } from '@/utils/images'
import ImageUpload from '@/components/ui/ImageUpload.vue'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

type CommunityPost = Post & { liked: boolean }

const router = useRouter()
const posts = ref<CommunityPost[]>([])
const loading = ref(true)
const showPublishModal = ref(false)
const publishing = ref(false)
const newPost = ref({
  title: '',
  content: '',
  imgList: '',
})

function mapPost(post: Post): CommunityPost {
  return {
    ...post,
    liked: hasLikedCommunityPost(post.postId),
  }
}

async function loadPosts() {
  loading.value = true
  try {
    const result = await communityApi.listPosts({ pageNum: 1, pageSize: 20 })
    posts.value = (result.records || []).map(mapPost)
  } catch {
    posts.value = []
  } finally {
    loading.value = false
  }
}

async function handlePublish() {
  if (!newPost.value.title.trim() || !newPost.value.content.trim()) {
    showToast('请填写标题和内容', 'warning')
    return
  }

  publishing.value = true
  try {
    await communityApi.publish({
      title: newPost.value.title.trim(),
      content: newPost.value.content.trim(),
      imgList: newPost.value.imgList.trim() || undefined,
    })

    showPublishModal.value = false
    newPost.value = { title: '', content: '', imgList: '' }
    await loadPosts()
    showToast('动态已提交，等待管理员审核后展示', 'success')
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : '发布失败，请先登录', 'error')
  } finally {
    publishing.value = false
  }
}

async function handleLike(post: CommunityPost) {
  try {
    if (post.liked) {
      await communityApi.unlikePost(post.postId)
      post.likeCount = Math.max(0, post.likeCount - 1)
      post.liked = false
      unmarkCommunityPostLiked(post.postId)
    } else {
      await communityApi.likePost(post.postId)
      post.likeCount += 1
      post.liked = true
      markCommunityPostLiked(post.postId)
    }
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : '操作失败，请先登录', 'error')
  }
}

function openPost(postId: number) {
  router.push(`/community/${postId}`)
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(() => {
  getLikedCommunityPostIds()
  loadPosts()
})
</script>
