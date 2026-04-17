<template>
  <div class="relative">
    <button
      @click="toggle"
      class="relative rounded-xl p-2.5 text-gray-400 transition-all hover:bg-green-50 hover:text-green-600"
    >
      <BellIcon class="h-5 w-5" />
      <span
        v-if="unreadCount > 0"
        class="absolute right-1.5 top-1.5 min-w-[18px] h-[18px] flex items-center justify-center rounded-full bg-red-500 text-white text-[10px] font-black border-2 border-white"
      >
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
      <span v-else class="absolute right-2.5 top-2.5 h-2 w-2 rounded-full border-2 border-white bg-gray-300"></span>
    </button>

    <div
      v-if="open"
      class="absolute right-0 top-full mt-2 w-80 bg-white border border-gray-100 rounded-2xl shadow-2xl z-50 overflow-hidden"
    >
      <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100">
        <h3 class="text-sm font-black text-gray-900">通知</h3>
        <div class="flex items-center gap-3">
          <button v-if="unreadCount > 0" @click="notifStore.markAllRead()" class="text-xs font-bold text-green-600 hover:underline">
            全部已读
          </button>
          <button @click="open = false" class="text-gray-400 hover:text-gray-600">
            <XIcon class="w-4 h-4" />
          </button>
        </div>
      </div>

      <div class="max-h-80 overflow-y-auto">
        <div v-if="notifications.length === 0" class="py-12 text-center text-gray-400 text-sm">
          暂无通知
        </div>
        <div
          v-for="n in notifications"
          :key="n.id"
          :class="['px-5 py-3.5 border-b border-gray-50 hover:bg-gray-50 transition-colors', !n.read && 'bg-green-50/50']"
        >
          <div class="flex items-start gap-3">
            <div :class="['mt-0.5 w-8 h-8 rounded-lg flex items-center justify-center shrink-0', n.type === 'order' ? 'bg-blue-50 text-blue-600' : 'bg-orange-50 text-orange-600']">
              <component :is="n.type === 'order' ? PackageIcon : ClockIcon" class="w-4 h-4" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-gray-900 truncate">{{ n.title }}</p>
              <p class="text-xs text-gray-500 mt-0.5 truncate">{{ n.message }}</p>
            </div>
            <span v-if="!n.read" class="mt-1.5 w-2 h-2 bg-green-500 rounded-full shrink-0"></span>
          </div>
        </div>
      </div>

      <div v-if="notifications.length > 0" class="px-5 py-3 border-t border-gray-100">
        <button @click="notifStore.clearAll(); open = false" class="text-xs font-bold text-gray-400 hover:text-red-500 transition-colors">
          清空通知
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import {
  Bell as BellIcon,
  Clock as ClockIcon,
  Package as PackageIcon,
  X as XIcon,
} from 'lucide-vue-next'

const notifStore = useNotificationStore()
const open = ref(false)
const panelRef = ref<HTMLElement | null>(null)
const unreadCount = computed(() => notifStore.unreadCount)
const notifications = computed(() => notifStore.notifications)

let pollTimer: ReturnType<typeof setInterval> | null = null

function toggle() {
  open.value = !open.value
  if (open.value) {
    notifStore.fetchNotifications()
  }
}

function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (open.value && !target.closest('.relative')) {
    open.value = false
  }
}

onMounted(() => {
  notifStore.fetchNotifications()
  pollTimer = setInterval(() => notifStore.fetchNotifications(), 60000)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  document.removeEventListener('click', handleClickOutside)
})
</script>
