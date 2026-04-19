<template>
  <div class="flex min-h-screen bg-gray-100">
    <aside class="fixed inset-y-0 left-0 z-50 flex w-72 flex-col bg-gray-950 text-white shadow-2xl">
      <div class="flex items-center gap-4 border-b border-white/10 px-7 py-7">
        <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-green-600 shadow-lg shadow-green-600/20">
          <LayoutDashboardIcon class="h-6 w-6 text-white" />
        </div>
        <div>
          <p class="text-xl font-black">管理后台</p>
          <p class="mt-1 text-[10px] font-bold uppercase tracking-[0.24em] text-green-400">Admin Portal</p>
        </div>
      </div>

      <nav class="flex-1 space-y-1 overflow-y-auto px-4 py-6">
        <router-link
          v-for="item in adminNavItems"
          :key="item.path"
          :to="item.path"
          :class="[
            'group flex items-center gap-3 rounded-2xl px-4 py-3.5 text-sm font-bold transition-all',
            isActive(item.path)
              ? 'bg-green-600 text-white shadow-lg shadow-green-600/20'
              : 'text-gray-400 hover:bg-white/5 hover:text-white',
          ]"
        >
          <component :is="item.icon" class="h-5 w-5" />
          {{ item.name }}
          <ChevronRightIcon v-if="isActive(item.path)" class="ml-auto h-4 w-4" />
        </router-link>
      </nav>

      <div class="border-t border-white/10 p-5">
        <button
          class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-sm font-bold text-gray-400 transition-all hover:bg-red-500/10 hover:text-red-400"
          @click="handleLogout"
        >
          <LogOutIcon class="h-5 w-5" />
          退出登录
        </button>
      </div>
    </aside>

    <div class="ml-72 flex min-h-screen flex-1 flex-col">
      <header class="sticky top-0 z-40 flex h-20 items-center justify-between border-b border-gray-200 bg-white/90 px-8 backdrop-blur">
        <div>
          <p class="text-xs font-bold uppercase tracking-[0.24em] text-gray-400">Admin Console</p>
          <h1 class="mt-2 text-2xl font-black text-gray-900">{{ currentPageName }}</h1>
        </div>

        <div class="flex items-center gap-6">
          <button class="relative rounded-xl p-2.5 text-gray-400 transition-all hover:bg-green-50 hover:text-green-600">
            <BellIcon class="h-5 w-5" />
            <span class="absolute right-2.5 top-2.5 h-2 w-2 rounded-full border-2 border-white bg-red-500"></span>
          </button>

          <div class="flex items-center gap-4 border-l border-gray-200 pl-6">
            <div class="hidden text-right sm:block">
              <p class="text-sm font-black text-gray-900">{{ adminDisplayName }}</p>
              <p class="mt-1 text-[10px] font-bold uppercase tracking-[0.22em] text-green-600">Super Admin</p>
            </div>
            <div class="h-11 w-11 overflow-hidden rounded-2xl border-2 border-white bg-gray-100 shadow-sm">
              <img
                :src="adminAvatar"
                alt="管理员头像"
                class="h-full w-full"
                referrerpolicy="no-referrer"
              />
            </div>
          </div>
        </div>
      </header>

      <main class="flex-1 p-8">
        <router-view v-slot="{ Component, route }">
          <Transition name="page" mode="out-in">
            <component :is="Component" :key="route.path" />
          </Transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getAvatarImage } from '@/utils/images'
import {
  Bell as BellIcon,
  BookOpen,
  ChevronRight as ChevronRightIcon,
  ClipboardList,
  FileWarning,
  Gift,
  Heart,
  LayoutDashboard as LayoutDashboardIcon,
  LogOut as LogOutIcon,
  MessageSquare,
  Settings,
  ShoppingBag,
  Star,
  Tags,
  Truck,
  UserCircle,
  Users,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const adminNavItems = [
  { name: '工作台', path: '/admin', icon: LayoutDashboardIcon },
  { name: '个人中心', path: '/admin/profile', icon: UserCircle },
  { name: '用户管理', path: '/admin/users', icon: Users },
  { name: '食材资源', path: '/admin/foods', icon: ShoppingBag },
  { name: '分类管理', path: '/admin/categories', icon: Tags },
  { name: '订单服务', path: '/admin/orders', icon: ClipboardList },
  { name: '内容管理', path: '/admin/content', icon: BookOpen },
  { name: '社区审核', path: '/admin/community', icon: MessageSquare },
  { name: '积分管理', path: '/admin/points', icon: Gift },
  { name: '售后申诉', path: '/admin/appeals', icon: FileWarning },
  { name: '配送调度', path: '/admin/dispatch', icon: Truck },
  { name: '收藏管理', path: '/admin/favorites', icon: Heart },
  { name: '评论管理', path: '/admin/reviews', icon: Star },
  { name: '系统设置', path: '/admin/settings', icon: Settings },
]

function isActive(path: string) {
  return route.path === path
}

const currentPageName = computed(() => {
  const current = adminNavItems.find((item) => item.path === route.path)
  return current?.name || '管理后台'
})

const adminAvatar = computed(() => {
  const uid = authStore.userId
  return uid ? getAvatarImage(uid, String(uid)) : getAvatarImage('admin', 'AD')
})

const adminDisplayName = computed(() => {
  return authStore.role || '系统管理员'
})

function handleLogout() {
  authStore.clearAuth()
  router.push('/admin')
}
</script>
