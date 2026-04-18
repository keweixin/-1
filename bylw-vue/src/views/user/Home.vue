<template>
  <div class="pb-20">
    <!-- Banner Carousel -->
    <section class="relative h-[500px] md:h-[600px] overflow-hidden">
      <div class="absolute inset-0">
        <img
          v-if="bannerImages.length > 0"
          :src="getBannerImage(bannerImages[bannerIndex].imageUrl, bannerImages[bannerIndex].title)"
          :alt="bannerImages[bannerIndex].title"
          class="w-full h-full object-cover scale-105 transition-opacity duration-700"
          referrerPolicy="no-referrer"
        />
        <img
          v-else
          :src="getBannerImage()"
          alt="Banner"
          class="w-full h-full object-cover scale-105 animate-slow-zoom"
          referrerPolicy="no-referrer"
        />
        <div class="absolute inset-0 bg-gradient-to-r from-black/80 via-black/40 to-transparent"></div>
        <!-- Carousel dots -->
        <div v-if="bannerImages.length > 1" class="absolute bottom-20 left-1/2 -translate-x-1/2 flex gap-2 z-10">
          <button
            v-for="(_, idx) in bannerImages"
            :key="idx"
            @click="bannerIndex = idx"
            class="w-2.5 h-2.5 rounded-full transition-all"
            :class="bannerIndex === idx ? 'bg-white w-8' : 'bg-white/40 hover:bg-white/60'"
          ></button>
        </div>
      </div>
      <div class="relative max-w-7xl mx-auto px-4 h-full flex flex-col justify-center items-start">
        <div class="max-w-2xl animate-fade-in-up">
          <div class="inline-flex items-center gap-2 bg-green-600/20 backdrop-blur-md border border-green-500/30 text-green-400 text-[10px] font-black px-4 py-1.5 rounded-full mb-6 uppercase tracking-[0.2em]">
            <div class="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse" />
            绿色消费 · 拯救食物 · 守护地球
          </div>
          <h1 class="text-5xl md:text-7xl font-black text-white mb-8 leading-[1.1] tracking-tight">
            让临期食品<br />
            <span class="text-transparent bg-clip-text bg-gradient-to-r from-green-400 to-emerald-500">
              焕发新生
            </span>
          </h1>
          <p class="text-xl text-gray-300 mb-10 leading-relaxed max-w-lg font-medium">
            城市临期食品智能分发系统，为您精准推荐高性价比健康美食，让每一份资源都物尽其用。
          </p>
          <div class="flex flex-wrap gap-5">
            <router-link to="/food-hall" class="group bg-green-600 hover:bg-green-500 text-white px-10 py-4.5 rounded-2xl font-black transition-all shadow-2xl shadow-green-600/20 active:scale-95 flex items-center gap-2">
              立即探索
              <ChevronRightIcon class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </router-link>
            <router-link to="/encyclopedia" class="bg-white/10 hover:bg-white/20 backdrop-blur-md text-white border border-white/20 px-10 py-4.5 rounded-2xl font-black transition-all active:scale-95">
              膳食百科
            </router-link>
          </div>
        </div>
      </div>

      <!-- Decorative Elements -->
      <div class="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-gray-50 to-transparent" />
    </section>

    <!-- 新用户专享 Banner (only show when not logged in) -->
    <div v-if="!authStore.isAuthenticated()" class="bg-gradient-to-r from-green-600 via-emerald-500 to-teal-500 relative overflow-hidden" id="new-user-banner">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-0 left-1/4 w-64 h-64 bg-white rounded-full blur-3xl"></div>
        <div class="absolute bottom-0 right-1/4 w-48 h-48 bg-yellow-300 rounded-full blur-3xl"></div>
      </div>
      <div class="relative max-w-7xl mx-auto px-4 py-6 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center shadow-lg shrink-0">
            <svg class="w-7 h-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v13m0-13V6a2 2 0 112 2h-2zm0 0V5.5A2.5 2.5 0 109.5 8H12zm-7 4h14M5 12a2 2 0 110-4h14a2 2 0 110 4M5 12v7a2 2 0 002 2h10a2 2 0 002-2v-7" />
            </svg>
          </div>
          <div>
            <div class="flex items-center gap-2 mb-0.5">
              <span class="bg-white text-green-700 text-[10px] font-black px-2 py-0.5 rounded-full uppercase tracking-widest">新用户专享</span>
            </div>
            <p class="text-white font-black text-lg leading-tight">注册即领 <span class="text-yellow-300">500 绿色积分</span>，首单立减 <span class="text-yellow-300">5 元</span>！</p>
          </div>
        </div>
        <div class="flex items-center gap-3 shrink-0">
          <router-link to="/register" class="bg-white text-green-600 px-6 py-3 rounded-xl font-black text-sm hover:bg-yellow-300 hover:text-green-800 transition-all shadow-lg active:scale-95">
            立即注册
          </router-link>
          <router-link to="/food-hall" class="bg-green-700/40 backdrop-blur-sm text-white border border-white/30 px-6 py-3 rounded-xl font-black text-sm hover:bg-white/10 transition-all active:scale-95">
            抢购临期好物
          </router-link>
        </div>
      </div>
    </div>

    <!-- Announcement Bar -->
    <div class="bg-white border-b border-gray-100 py-4 overflow-hidden">
      <div class="max-w-7xl mx-auto px-4 flex items-center gap-4">
        <div class="flex items-center gap-2 px-3 py-1 bg-orange-500 text-white rounded-lg text-[10px] font-black uppercase tracking-widest shrink-0">
          <BellIcon class="w-3.5 h-3.5" />
          公告
        </div>
        <div class="flex-grow overflow-hidden relative h-6">
          <div class="absolute whitespace-nowrap text-sm font-bold text-gray-600 flex items-center gap-12 animate-marquee">
            <span>欢迎访问城市临期食品分发系统，绿色消费从今天开始！</span>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 mt-16">
      <!-- Quick Access Categories -->
      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-6 mb-12">
        <router-link
          v-for="cat in categories"
          :key="cat.id"
          :to="`/food-hall?category=${cat.name}`"
          class="flex flex-col items-center group"
        >
          <div
            :class="['w-20 h-20 rounded-[2rem] shadow-sm border border-gray-100 flex items-center justify-center mb-4 transition-all duration-500 group-hover:shadow-xl group-hover:-translate-y-2', cat.color]"
          >
            <span class="text-3xl group-hover:scale-125 transition-transform duration-500">{{ cat.icon }}</span>
          </div>
          <span class="text-sm font-black text-gray-900 group-hover:text-green-600 transition-colors uppercase tracking-widest">{{ cat.name }}</span>
        </router-link>
      </div>

      <!-- Daily Sign-in Card -->
      <div v-if="authStore.isAuthenticated()" class="bg-gradient-to-r from-green-600 to-emerald-600 rounded-[2rem] p-6 mb-12 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
            <CalendarCheckIcon class="w-6 h-6 text-white" />
          </div>
          <div>
            <h3 class="text-lg font-black text-white">每日签到</h3>
            <p class="text-sm text-green-100">签到即得 <span class="text-yellow-300 font-black">10 绿色积分</span></p>
          </div>
        </div>
        <button
          v-if="!signedInToday"
          @click="handleSignIn"
          :disabled="signInLoading"
          class="bg-white text-green-600 px-6 py-3 rounded-2xl font-black text-sm hover:bg-yellow-300 hover:text-green-800 transition-all shadow-lg"
        >
          <span v-if="signInLoading" class="w-5 h-5 border-2 border-green-600 border-t-transparent rounded-full animate-spin inline-block mr-2"></span>
          {{ signInLoading ? '签到中...' : '立即签到' }}
        </button>
        <div v-else class="flex items-center gap-2 text-white font-black text-sm bg-white/20 px-4 py-2 rounded-2xl">
          <CheckCircleIcon class="w-5 h-5" />
          今日已签到 +10积分
        </div>
      </div>

      <!-- Expiry Reminder Card -->
      <div class="relative bg-gray-900 rounded-[3rem] p-10 md:p-14 mb-24 overflow-hidden group">
        <div class="absolute top-0 right-0 w-96 h-96 bg-green-600/20 rounded-full blur-[100px] -translate-y-1/2 translate-x-1/2" />
        <div class="absolute bottom-0 left-0 w-64 h-64 bg-orange-600/20 rounded-full blur-[80px] translate-y-1/2 -translate-x-1/2" />

        <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-10">
          <div class="flex flex-col md:flex-row items-center gap-8 text-center md:text-left">
            <div class="w-20 h-20 bg-orange-500 rounded-[2rem] flex items-center justify-center shadow-2xl shadow-orange-500/40 animate-bounce-slow">
              <ClockIcon class="w-10 h-10 text-white" />
            </div>
            <div>
              <h2 class="text-3xl font-black text-white mb-3 tracking-tight">临期资产提醒</h2>
              <p v-if="expiryReminders.length > 0" class="text-gray-400 text-lg font-medium max-w-md">
                您有 <span class="text-orange-500 font-black">{{ expiryReminders.length }}</span> 件已购商品即将在 <span class="text-white font-black">24 小时</span> 内到期，请尽快食用以保证最佳口感。
              </p>
              <p v-else class="text-gray-400 text-lg font-medium max-w-md">
                您暂无即将到期的已购商品，膳食安排十分合理！
              </p>
            </div>
          </div>
          <router-link to="/orders" class="bg-white text-gray-900 px-10 py-4.5 rounded-2xl font-black hover:bg-green-500 hover:text-white transition-all shadow-xl active:scale-95 whitespace-nowrap">
            立即处理
          </router-link>
        </div>
      </div>

      <!-- Recommended Food Section -->
      <section class="mb-16">
        <div class="flex items-end justify-between mb-8">
          <div>
            <h2 class="text-3xl font-black text-gray-900 mb-2">猜我喜欢</h2>
            <p class="text-gray-500">基于您的饮食档案与购买习惯为您推荐</p>
          </div>
          <router-link to="/food-hall" class="text-green-600 font-bold flex items-center gap-1 hover:gap-2 transition-all">
            查看全部 <ChevronRightIcon class="w-5 h-5" />
          </router-link>
        </div>

        <!-- Food skeleton loading -->
        <div v-if="foodsLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="i in 4" :key="i" class="bg-white rounded-3xl border border-gray-100 p-5 animate-pulse">
            <div class="h-44 bg-gray-100 rounded-2xl mb-4"></div>
            <div class="h-5 bg-gray-100 rounded-xl w-3/4 mb-3"></div>
            <div class="h-4 bg-gray-100 rounded-xl w-1/2 mb-4"></div>
            <div class="flex justify-between items-center">
              <div class="h-6 bg-gray-100 rounded-xl w-20"></div>
              <div class="h-10 bg-gray-100 rounded-2xl w-28"></div>
            </div>
          </div>
        </div>
        <div v-else-if="recommendedFoods.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <FoodCard
            v-for="food in recommendedFoods"
            :key="food.id"
            v-bind="food"
          />
        </div>
        <div v-else class="flex flex-col items-center justify-center py-16 bg-white rounded-3xl border border-dashed border-gray-200">
          <ShoppingBagIcon class="w-12 h-12 text-gray-300 mb-3" />
          <p class="text-sm font-bold text-gray-400">暂无推荐食品</p>
          <router-link to="/food-hall" class="mt-3 text-sm font-bold text-green-600 hover:underline">去逛逛食品大厅</router-link>
        </div>
      </section>

      <!-- Recommended Recipes Section -->
      <section class="mb-16">
        <div class="flex items-end justify-between mb-8">
          <div>
            <h2 class="text-3xl font-black text-gray-900 mb-2">健康食谱</h2>
            <p class="text-gray-500">科学膳食，让每一口都充满营养</p>
          </div>
          <router-link to="/recipes" class="text-green-600 font-bold flex items-center gap-1 hover:gap-2 transition-all">
            查看全部 <ChevronRightIcon class="w-5 h-5" />
          </router-link>
        </div>

        <!-- Recipe skeleton loading -->
        <div v-if="recipesLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="i in 4" :key="i" class="bg-white rounded-3xl border border-gray-100 overflow-hidden animate-pulse">
            <div class="h-40 bg-gray-100"></div>
            <div class="p-5">
              <div class="h-5 bg-gray-100 rounded-xl w-3/4 mb-3"></div>
              <div class="h-3 bg-gray-100 rounded-xl w-full mb-2"></div>
              <div class="h-3 bg-gray-100 rounded-xl w-2/3"></div>
            </div>
          </div>
        </div>
        <div v-else-if="recipes.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <RecipeCard
            v-for="recipe in recipes"
            :key="recipe.id"
            v-bind="recipe"
          />
        </div>
        <div v-else class="flex flex-col items-center justify-center py-16 bg-white rounded-3xl border border-dashed border-gray-200">
          <UtensilsIcon class="w-12 h-12 text-gray-300 mb-3" />
          <p class="text-sm font-bold text-gray-400">暂无食谱推荐</p>
          <router-link to="/recipes" class="mt-3 text-sm font-bold text-green-600 hover:underline">去看看食谱大全</router-link>
        </div>
      </section>

      <!-- Community Preview Section -->
      <section>
        <div class="flex items-end justify-between mb-8">
          <div>
            <h2 class="text-3xl font-black text-gray-900 mb-2">社区动态</h2>
            <p class="text-gray-500">分享您的绿色生活与美食创意</p>
          </div>
          <router-link to="/community" class="text-green-600 font-bold flex items-center gap-1 hover:gap-2 transition-all">
            进入社区 <ChevronRightIcon class="w-5 h-5" />
          </router-link>
        </div>

        <!-- Community skeleton loading -->
        <div v-if="postsLoading" class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div v-for="i in 2" :key="i" class="rounded-3xl border border-gray-100 bg-white p-6 animate-pulse">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-full bg-gray-100"></div>
              <div>
                <div class="h-4 bg-gray-100 rounded-xl w-20 mb-1"></div>
                <div class="h-3 bg-gray-100 rounded-xl w-28"></div>
              </div>
            </div>
            <div class="h-5 bg-gray-100 rounded-xl w-3/4 mb-3"></div>
            <div class="h-4 bg-gray-100 rounded-xl w-full mb-1"></div>
            <div class="h-4 bg-gray-100 rounded-xl w-2/3 mb-4"></div>
            <div class="h-48 bg-gray-100 rounded-2xl"></div>
          </div>
        </div>
        <div v-else-if="communityPosts.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <router-link
            v-for="(post, index) in communityPosts"
            :key="post.id"
            :to="`/community/${post.id}`"
            class="block rounded-3xl border border-gray-100 bg-white p-6 shadow-sm transition-all hover:-translate-y-1 hover:shadow-md"
          >
            <div class="flex items-center gap-3 mb-4">
              <img :src="post.avatar" :alt="post.user" class="w-10 h-10 rounded-full bg-gray-100" referrerPolicy="no-referrer" />
              <div>
                <p class="text-sm font-bold text-gray-900">{{ post.user }}</p>
                <p class="text-xs text-gray-400">{{ post.time }}</p>
              </div>
            </div>
            <h3 class="font-bold text-gray-900 mb-2">{{ post.title }}</h3>
            <p class="text-sm text-gray-600 line-clamp-2 mb-4">{{ post.content }}</p>
            <img v-if="post.images && post.images[0]" :src="getPostImage(post, index)" alt="Post" class="w-full h-48 object-cover rounded-2xl mb-4" referrerPolicy="no-referrer" @error="handlePostImageError(index)" />
            <div class="flex items-center gap-6 text-gray-400 text-sm">
              <span class="flex items-center gap-1.5 hover:text-red-500 cursor-pointer transition-colors">
                <UsersIcon class="w-4 h-4" /> {{ post.likes }}
              </span>
              <span class="flex items-center gap-1.5 hover:text-green-600 cursor-pointer transition-colors">
                <MessageSquareIcon class="w-4 h-4" /> {{ post.comments }}
              </span>
            </div>
          </router-link>
        </div>
        <div v-else class="flex flex-col items-center justify-center py-16 bg-white rounded-3xl border border-dashed border-gray-200">
          <MessageSquareIcon class="w-12 h-12 text-gray-300 mb-3" />
          <p class="text-sm font-bold text-gray-400">暂无社区动态</p>
          <router-link to="/community" class="mt-3 text-sm font-bold text-green-600 hover:underline">去社区看看吧</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { recommendApi } from '@/api/recommend'
import { mapFoodToCard, type FoodCardItem } from '@/utils/mapping'
import { communityApi } from '@/api/community'
import { orderApi } from '@/api/order'
import { pointsApi } from '@/api/points'
import { api } from '@/api'
import { useAuthStore } from '@/stores/auth'
import FoodCard from '@/components/ui/FoodCard.vue'
import RecipeCard from '@/components/ui/RecipeCard.vue'
import { useToast } from '@/composables/useToast'
import { getAvatarImage, getBannerImage, getCommunityImage, getRecipeImage } from '@/utils/images'
import {
  ChevronRight as ChevronRightIcon,
  Bell as BellIcon,
  Users as UsersIcon,
  MessageSquare as MessageSquareIcon,
  Clock as ClockIcon,
  CalendarCheck as CalendarCheckIcon,
  CheckCircle as CheckCircleIcon,
  ShoppingBag as ShoppingBagIcon,
  UtensilsCrossed as UtensilsIcon,
} from 'lucide-vue-next'

const authStore = useAuthStore()
const toast = useToast()
const recommendedFoods = ref<FoodCardItem[]>([])
const recipes = ref<{ id: number; name: string; image: string; tags: string[]; summary: string }[]>([])
const communityPosts = ref<{ id: number; user: string; avatar: string; title: string; content: string; images: string[]; likes: number; comments: number; time: string }[]>([])
const postImgErrorFlags = ref<Record<number, boolean>>({})
const expiryReminders = ref<{ foodName: string; expireDate: string }[]>([])
const signedInToday = ref(false)
const signInLoading = ref(false)
const bannerImages = ref<{ id: number; title: string; imageUrl: string }[]>([])
const bannerIndex = ref(0)
let bannerTimer: ReturnType<typeof setInterval> | null = null

// Loading states
const foodsLoading = ref(true)
const recipesLoading = ref(true)
const postsLoading = ref(true)

function handlePostImageError(index: number) {
  postImgErrorFlags.value[index] = true
}

function getPostImage(post: typeof communityPosts.value[0], index: number): string {
  if (postImgErrorFlags.value[index]) {
    return getCommunityImage()
  }
  return getCommunityImage(post.images?.[0])
}


async function loadData() {
  foodsLoading.value = true
  recipesLoading.value = true
  postsLoading.value = true

  try {
    const [foodsResult, recipesResult, postsResult, expiryResult] = await Promise.allSettled([
      recommendApi.getFoods(4),
      recommendApi.getRecipes(4),
      communityApi.listPosts({ pageNum: 1, pageSize: 2 }),
      orderApi.getExpiryReminder(24).catch(() => []),
    ])

    if (foodsResult.status === 'fulfilled') {
      recommendedFoods.value = (foodsResult.value.foods || []).map((r: unknown) => mapFoodToCard(r as Parameters<typeof mapFoodToCard>[0]))
    } else {
      recommendedFoods.value = []
    }
    foodsLoading.value = false

    if (recipesResult.status === 'fulfilled') {
      const val = recipesResult.value
      recipes.value = ((Array.isArray(val) ? val : []) as unknown[]).map((r: unknown) => {
        const rec = r as Record<string, unknown>
        return {
          id: rec.recipeId || rec.articleId || rec.id,
          name: rec.title,
          image: getRecipeImage(rec.coverImg as string | undefined),
          tags: (rec.tags as string[]) || [],
          summary: (rec.summary as string) || '',
        }
      })
    } else {
      recipes.value = []
    }
    recipesLoading.value = false

    if (postsResult.status === 'fulfilled') {
      communityPosts.value = ((postsResult.value.records as unknown[]) || []).map((p: unknown) => {
        const post = p as Record<string, unknown>
        return {
          id: post.postId,
          user: `用户${post.userId}`,
          avatar: getAvatarImage(post.userId as number, String(post.userId)),
          title: post.title as string,
          content: post.content as string,
          images: post.imgList ? [post.imgList as string] : [],
          likes: (post.likeCount as number) || 0,
          comments: (post.commentCount as number) || 0,
          time: post.createTime as string,
        }
      })
    } else {
      communityPosts.value = []
    }
    postsLoading.value = false

    if (expiryResult.status === 'fulfilled') {
      expiryReminders.value = ((expiryResult.value as unknown[]) || []).map((f: unknown) => {
        const rec = f as Record<string, unknown>
        return { foodName: rec.foodName as string, expireDate: rec.expireDate as string }
      })
    } else {
      expiryReminders.value = []
    }

    if (authStore.isAuthenticated()) {
      await checkSignedIn()
    }
  } catch (e: unknown) {
    foodsLoading.value = false
    recipesLoading.value = false
    postsLoading.value = false
    toast.show('数据加载失败，请检查网络连接', 'error')
  }
}

async function checkSignedIn() {
  try {
    const history = await pointsApi.getHistory()
    const today = new Date().toISOString().slice(0, 10)
    signedInToday.value = history.some((r: { sourceType: string; createTime: string }) =>
      r.sourceType === '每日签到' && r.createTime && r.createTime.slice(0, 10) === today
    )
  } catch {
    signedInToday.value = false
  }
}

async function handleSignIn() {
  signInLoading.value = true
  try {
    await pointsApi.signIn()
    signedInToday.value = true
    toast.show('签到成功！获得 10 绿色积分', 'success')
  } catch (e: unknown) {
    toast.show(e instanceof Error ? e.message : '签到失败，请重试', 'error')
  } finally {
    signInLoading.value = false
  }
}

onMounted(() => {
  loadData()
  // Load banners
  api.get<Array<{ bannerId: number; title: string; imgUrl: string; status: number }>>('/system/banner/list')
    .then((list) => {
      bannerImages.value = (list || [])
        .filter((b) => b.status === 1)
        .map((b) => ({ id: b.bannerId, title: b.title, imageUrl: b.imgUrl }))
      if (bannerImages.value.length > 1) {
        bannerTimer = setInterval(() => {
          bannerIndex.value = (bannerIndex.value + 1) % bannerImages.value.length
        }, 5000)
      }
    })
    .catch(() => { bannerImages.value = [] })
})

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
})

const categories = [
  { id: '1', name: '新鲜果蔬', icon: '🍎', color: 'bg-red-50 text-red-600' },
  { id: '2', name: '肉禽蛋奶', icon: '🥩', color: 'bg-orange-50 text-orange-600' },
  { id: '3', name: '休闲零食', icon: '🍪', color: 'bg-yellow-50 text-yellow-600' },
  { id: '4', name: '粮油调味', icon: '🌾', color: 'bg-amber-50 text-amber-600' },
  { id: '5', name: '速食冷冻', icon: '🍦', color: 'bg-blue-50 text-blue-600' },
  { id: '6', name: '酒水饮料', icon: '☕', color: 'bg-emerald-50 text-emerald-600' },
]
</script>
