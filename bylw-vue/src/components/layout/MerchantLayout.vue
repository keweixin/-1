<template>
  <div class="flex min-h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside class="hidden md:flex w-64 flex-col bg-white border-r border-gray-100 shadow-sm">
      <div class="p-6 border-b border-gray-50">
        <div class="flex items-center gap-3">
          <div class="w-11 h-11 bg-blue-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-600/20">
            <StorefrontIcon class="text-white w-6 h-6" />
          </div>
          <div>
            <p class="font-black text-gray-900 leading-tight">商户中心</p>
            <p class="text-[10px] text-blue-600 font-bold uppercase tracking-widest">Merchant</p>
          </div>
        </div>
      </div>

      <div class="p-4">
        <div class="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-4 text-white">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center text-lg">{{ profileInitial }}</div>
            <div class="min-w-0">
              <p class="font-bold truncate">{{ merchantName }}</p>
              <p class="text-xs opacity-80">ID: {{ userId }}</p>
            </div>
          </div>
        </div>
      </div>

      <nav class="flex-1 px-3 space-y-1">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path"
          class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all"
          :class="isActive(item.path) ? 'bg-blue-50 text-blue-600 shadow-sm' : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'"
        >
          <component :is="item.icon" class="w-5 h-5" />
          {{ item.name }}
        </router-link>
      </nav>

      <div class="p-4 border-t border-gray-50">
        <button @click="handleLogout" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold text-red-500 hover:bg-red-50 transition-all">
          <LogOutIcon class="w-5 h-5" />
          退出登录
        </button>
      </div>
    </aside>

    <!-- Mobile header -->
    <div class="md:hidden fixed top-0 left-0 right-0 z-40 bg-white border-b border-gray-100 shadow-sm">
      <div class="flex items-center justify-between px-4 h-14">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-blue-600 rounded-xl flex items-center justify-center">
            <StorefrontIcon class="text-white w-4 h-4" />
          </div>
          <span class="font-black text-gray-900">商户中心</span>
        </div>
        <button @click="handleLogout" class="text-xs text-red-500 font-bold">退出</button>
      </div>
      <div class="flex border-t border-gray-50">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path"
          class="flex-1 flex flex-col items-center gap-1 py-2 text-[10px] font-bold"
          :class="isActive(item.path) ? 'text-blue-600' : 'text-gray-400'"
        >
          <component :is="item.icon" class="w-4 h-4" />
          {{ item.name }}
        </router-link>
      </div>
    </div>

    <!-- Main content -->
    <main class="flex-1 md:ml-0 pt-[6.5rem] md:pt-0">
      <div class="max-w-5xl mx-auto p-6">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Store as StorefrontIcon,
  LayoutDashboard as HomeIcon,
  ShoppingBag as FoodIcon,
  ClipboardList as OrderIcon,
  LogOut as LogOutIcon,
} from 'lucide-vue-next'
import { authApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const merchantName = ref('商户')
const profileInitial = ref('商')

const navItems = [
  { name: '工作台', path: '/merchant', icon: HomeIcon },
  { name: '我的食品', path: '/merchant/foods', icon: FoodIcon },
  { name: '我的订单', path: '/merchant/orders', icon: OrderIcon },
]

const userId = computed(() => authStore.userId || '-')

const isActive = (path: string) => route.path === path

const handleLogout = () => {
  authStore.clearAuth()
  router.push('/login')
}

onMounted(async () => {
  try {
    const data = await authApi.getUserInfo() as any
    const name = data?.nickname || data?.username || '商户'
    merchantName.value = name
    profileInitial.value = name[0] || '商'
  } catch {}
})
</script>
