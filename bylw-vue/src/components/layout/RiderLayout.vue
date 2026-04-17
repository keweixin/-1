<template>
  <div class="flex min-h-screen bg-gray-50">
    <!-- Sidebar -->
    <aside class="hidden md:flex w-64 flex-col bg-white border-r border-gray-100 shadow-sm">
      <div class="p-6 border-b border-gray-50">
        <div class="flex items-center gap-3">
          <div class="w-11 h-11 bg-green-600 rounded-2xl flex items-center justify-center shadow-lg shadow-green-600/20">
            <BikeIcon class="text-white w-6 h-6" />
          </div>
          <div>
            <p class="font-black text-gray-900 leading-tight">骑手工作台</p>
            <p class="text-[10px] text-green-600 font-bold uppercase tracking-widest">Courier</p>
          </div>
        </div>
      </div>

      <div class="p-4">
        <div class="bg-gradient-to-r from-green-600 to-emerald-600 rounded-2xl p-4 text-white">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center text-lg">{{ profile.nickname?.[0] || '骑' }}</div>
            <div class="min-w-0">
              <p class="font-bold truncate">{{ profile.nickname || '骑手' }}</p>
              <div class="flex items-center gap-1 mt-0.5">
                <span class="w-1.5 h-1.5 rounded-full" :class="profile.courierStatus === 1 ? 'bg-green-300 animate-pulse' : 'bg-red-300'"></span>
                <span class="text-xs opacity-80">{{ profile.courierStatus === 1 ? '在线接单' : '暂停接单' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <nav class="flex-1 px-3 space-y-1">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path"
          class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all"
          :class="isActive(item.path) ? 'bg-green-50 text-green-600 shadow-sm' : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'"
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
          <div class="w-8 h-8 bg-green-600 rounded-xl flex items-center justify-center">
            <BikeIcon class="text-white w-4 h-4" />
          </div>
          <span class="font-black text-gray-900">骑手工作台</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="w-2 h-2 rounded-full" :class="profile.courierStatus === 1 ? 'bg-green-500' : 'bg-red-400'"></span>
          <button @click="handleLogout" class="text-xs text-red-500 font-bold">退出</button>
        </div>
      </div>
      <div class="flex border-t border-gray-50">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path"
          class="flex-1 flex flex-col items-center gap-1 py-2 text-[10px] font-bold"
          :class="isActive(item.path) ? 'text-green-600' : 'text-gray-400'"
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { courierApi } from '@/api/courier'
import {
  Bike as BikeIcon,
  LayoutDashboard as HomeIcon,
  Inbox as HallIcon,
  Package as TasksIcon,
  History as HistoryIcon,
  User as ProfileIcon,
  LogOut as LogOutIcon,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const profile = ref({ nickname: '', phone: '', courierStatus: 1 as number })

const navItems = [
  { name: '工作台', path: '/rider', icon: HomeIcon },
  { name: '接单大厅', path: '/rider/hall', icon: HallIcon },
  { name: '配送任务', path: '/rider/tasks', icon: TasksIcon },
  { name: '历史记录', path: '/rider/history', icon: HistoryIcon },
  { name: '个人信息', path: '/rider/profile', icon: ProfileIcon },
]

const isActive = (path: string) => route.path === path

const handleLogout = () => {
  authStore.clearAuth()
  router.push('/login')
}

onMounted(async () => {
  try {
    const data = await courierApi.getProfile() as any
    if (data) {
      profile.value.nickname = data.nickname || ''
      profile.value.phone = data.phone || ''
      profile.value.courierStatus = data.courierStatus ?? 1
    }
  } catch {}
})
</script>
