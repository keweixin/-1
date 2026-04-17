<template>
  <Teleport to="body">
    <div class="fixed top-6 left-1/2 -translate-x-1/2 z-[9999] flex flex-col gap-3 pointer-events-none">
      <TransitionGroup name="toast">
        <div
          v-for="item in queue"
          :key="item.id"
          :class="[
            'flex items-center gap-3 rounded-2xl px-5 py-3 shadow-lg backdrop-blur-sm transition-all duration-200',
            typeClass(item.type),
            item.leaving ? 'opacity-0 translate-y-[-10px]' : 'opacity-100 translate-y-0'
          ]"
        >
          <span class="text-lg">{{ icon(item.type) }}</span>
          <span class="text-sm font-bold text-white">{{ item.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { queue } = useToast()

const typeClass = (type: string) => ({
  'bg-green-600/90': type === 'success',
  'bg-red-600/90': type === 'error',
  'bg-yellow-500/90': type === 'warning',
  'bg-gray-800/90': type === 'info',
})

const icon = (type: string) => ({
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ',
}[type] || 'ℹ')
</script>

<style scoped>
/* 入场动画 */
.toast-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}
.toast-enter-active {
  transition: opacity 0.3s, transform 0.3s;
}
/* 离场动画由 JS leaving 标记 + CSS opacity 控制，不再依赖 TransitionGroup leave */
</style>
