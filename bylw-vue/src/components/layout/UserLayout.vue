<template>
  <div class="flex min-h-screen flex-col bg-gray-50">
    <LoadingBar />
    <header class="sticky top-0 z-50 border-b border-gray-200 bg-white/95 backdrop-blur">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
        <router-link to="/" class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-green-600 shadow-lg shadow-green-600/20">
            <ShoppingBagIcon class="h-6 w-6 text-white" />
          </div>
          <div>
            <p class="text-lg font-black leading-none text-gray-900">绿享膳食</p>
            <p class="mt-1 text-[10px] font-bold uppercase tracking-[0.22em] text-green-600">Smart Food Rescue</p>
          </div>
        </router-link>

        <nav class="hidden items-center gap-1 rounded-2xl border border-gray-100 bg-gray-50 p-1.5 lg:flex">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            :class="[
              'inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-bold transition-all',
              route.path === item.path
                ? 'bg-white text-green-600 shadow-sm'
                : 'text-gray-500 hover:bg-white hover:text-green-600',
            ]"
          >
            <component :is="item.icon" class="h-4 w-4" />
            {{ item.name }}
          </router-link>
        </nav>

        <div class="flex items-center gap-3">
          <template v-if="authStore.isAuthenticated()">
            <div class="hidden items-center gap-2 rounded-2xl border border-orange-100 bg-orange-50 px-3 py-2 sm:flex">
              <GiftIcon class="h-4 w-4 text-orange-500" />
              <span class="text-xs font-black text-orange-600">{{ authStore.pointsBalance.toLocaleString() }} 积分</span>
            </div>

            <NotificationPanel />

            <router-link
              to="/profile"
              class="h-10 w-10 overflow-hidden rounded-xl border-2 border-white bg-gray-100 shadow-sm transition-all hover:border-green-600"
            >
              <img :src="avatarUrl" alt="用户头像" class="h-full w-full" referrerpolicy="no-referrer" />
            </router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="px-4 py-2 text-sm font-bold text-gray-600 hover:text-green-600 transition-colors">
              登录
            </router-link>
            <router-link to="/register" class="px-4 py-2 text-sm font-bold text-white bg-green-600 rounded-xl hover:bg-green-700 transition-all">
              注册
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <router-view v-slot="{ Component, route }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </router-view>
    </main>

    <footer class="mt-auto border-t border-gray-200 bg-white py-12">
      <div class="mx-auto grid max-w-7xl gap-8 px-4 sm:px-6 md:grid-cols-4 lg:px-8">
        <div>
          <div class="mb-4 flex items-center gap-3">
            <div class="flex h-9 w-9 items-center justify-center rounded-xl bg-green-600">
              <ShoppingBagIcon class="h-5 w-5 text-white" />
            </div>
            <span class="text-lg font-black text-gray-900">城市临期食品分发系统</span>
          </div>
          <p class="max-w-md text-sm leading-7 text-gray-500">
            聚焦临期食品、社区互助与个性化推荐，帮助用户省钱、省心，也让可利用食材得到更高效的流转。
          </p>
        </div>

        <div>
          <h3 class="mb-4 text-sm font-bold uppercase tracking-[0.24em] text-gray-900">快速入口</h3>
          <ul class="space-y-2 text-sm text-gray-500">
            <li><router-link to="/" class="transition-colors hover:text-green-600">首页</router-link></li>
            <li><router-link to="/food-hall" class="transition-colors hover:text-green-600">临期食品</router-link></li>
            <li><router-link to="/community" class="transition-colors hover:text-green-600">社区动态</router-link></li>
          </ul>
        </div>

        <div>
          <h3 class="mb-4 text-sm font-bold uppercase tracking-[0.24em] text-gray-900">联系我们</h3>
          <ul class="space-y-2 text-sm text-gray-500">
            <li>电话：400-123-4567</li>
            <li>邮箱：contact@foodsave.com</li>
            <li>地址：绿色科技园 1 号楼</li>
          </ul>
        </div>

        <div v-if="friendLinks.length > 0">
          <h3 class="mb-4 text-sm font-bold uppercase tracking-[0.24em] text-gray-900">友情链接</h3>
          <ul class="space-y-2 text-sm">
            <li v-for="link in friendLinks" :key="link.id">
              <a :href="link.url" target="_blank" rel="noopener noreferrer" class="text-gray-500 hover:text-green-600 transition-colors">{{ link.name }}</a>
            </li>
          </ul>
        </div>
      </div>

      <div class="mx-auto mt-8 max-w-7xl border-t border-gray-100 px-4 pt-8 text-center text-xs text-gray-400 sm:px-6 lg:px-8">
        © 2026 城市临期食品分发与膳食推荐系统
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { pointsApi } from '@/api/points'
import { useAuthStore } from '@/stores/auth'
import { getAvatarImage } from '@/utils/images'
import { api } from '@/api'
import NotificationPanel from '@/components/ui/NotificationPanel.vue'
import LoadingBar from '@/components/ui/LoadingBar.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'

useScrollReveal()
import {
  BookOpen,
  ClipboardList,
  Gift as GiftIcon,
  Headphones as HeadphonesIcon,
  Home,
  ShoppingBag as ShoppingBagIcon,
  User,
  Users,
  UtensilsCrossed as UtensilsIcon,
} from 'lucide-vue-next'

const route = useRoute()
const authStore = useAuthStore()

const friendLinks = ref<{ id: number; name: string; url: string }[]>([])

async function loadFriendLinks() {
  try {
    const list = await api.get<Array<{ linkId: number; linkName: string; linkUrl: string; status: number }>>('/system/friend-link/list')
    friendLinks.value = (list || [])
      .filter((l) => l.status === 1)
      .map((l) => ({ id: l.linkId, name: l.linkName, url: l.linkUrl }))
  } catch {
    friendLinks.value = []
  }
}

const navItems = [
  { name: '首页', path: '/', icon: Home },
  { name: '临期食品', path: '/food-hall', icon: ShoppingBagIcon },
  { name: '膳食百科', path: '/encyclopedia', icon: BookOpen },
  { name: '健康食谱', path: '/recipes', icon: UtensilsIcon },
  { name: '社区', path: '/community', icon: Users },
  { name: '积分商城', path: '/points', icon: GiftIcon },
  { name: '我的订单', path: '/orders', icon: ClipboardList },
  { name: '售后申诉', path: '/appeals', icon: HeadphonesIcon },
  { name: '个人中心', path: '/profile', icon: User },
]

const avatarUrl = computed(() => {
  const seed = localStorage.getItem('token') || 'user'
  return getAvatarImage(seed, 'U')
})

async function loadPointsBalance() {
  if (!authStore.isAuthenticated() || authStore.isAdmin()) return

  try {
    const result = await pointsApi.getBalance()
    authStore.setPointsBalance(result.balance ?? 0)
  } catch {
    authStore.setPointsBalance(0)
  }
}

onMounted(() => {
  loadPointsBalance()
  loadFriendLinks()
})
</script>
