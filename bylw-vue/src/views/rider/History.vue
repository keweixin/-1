<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-black text-gray-900">历史记录</h1>
      <p class="text-sm text-gray-400 mt-1">{{ history.length }} 条配送记录</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- History List -->
    <div v-else-if="history.length > 0" class="space-y-4">
      <div v-for="item in history" :key="item.orderId"
        class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div class="flex items-center justify-between mb-4">
          <div>
            <p class="font-bold text-gray-900">{{ item.orderNo }}</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ item.createTime }}</p>
          </div>
          <span class="px-3 py-1.5 rounded-lg text-xs font-bold bg-green-50 text-green-600">
            已完成
          </span>
        </div>
        <div class="bg-gray-50 rounded-xl p-4 space-y-2 text-sm">
          <div class="flex items-start gap-2">
            <MapPinIcon class="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
            <span class="text-gray-600">{{ item.receiverAddress }}</span>
          </div>
          <div class="flex items-center gap-2">
            <UserIcon class="w-4 h-4 text-gray-400 flex-shrink-0" />
            <span class="text-gray-600">{{ item.receiverName }} {{ item.receiverPhone }}</span>
          </div>
        </div>
        <div class="flex items-center justify-between mt-4">
          <p class="text-lg font-black text-gray-900">&yen;{{ item.totalAmount?.toFixed(2) }}</p>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="bg-white rounded-2xl border border-gray-100 shadow-sm p-16 text-center">
      <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
        <HistoryIcon class="w-8 h-8 text-gray-300" />
      </div>
      <p class="text-gray-400 font-bold">暂无历史记录</p>
      <p class="text-gray-300 text-sm mt-2">完成配送后记录将显示在这里</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  History as HistoryIcon,
  MapPin as MapPinIcon,
  User as UserIcon,
} from 'lucide-vue-next'
import { courierApi } from '@/api/courier'
import type { CourierTask } from '@/api/courier'

const history = ref<CourierTask[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const tasks = await courierApi.getTasks('已完成')
    history.value = tasks ?? []
  } catch {
    /* load failed silently */
  } finally {
    loading.value = false
  }
})
</script>
