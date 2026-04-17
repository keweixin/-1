<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-black text-gray-900">工作台</h1>
      <p class="text-sm text-gray-400 mt-1">今日配送概况</p>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-2 gap-4">
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer group">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-10 h-10 bg-green-50 text-green-600 rounded-xl flex items-center justify-center group-hover:bg-green-100 transition-colors">
            <CheckCircleIcon class="w-5 h-5" />
          </div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">今日完成</p>
        </div>
        <p class="text-3xl font-black text-gray-900">{{ stats.todayCompleted }}</p>
      </div>
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer group">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-10 h-10 bg-orange-50 text-orange-600 rounded-xl flex items-center justify-center group-hover:bg-orange-100 transition-colors">
            <ClockIcon class="w-5 h-5" />
          </div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">待取货</p>
        </div>
        <p class="text-3xl font-black text-gray-900">{{ stats.pending }}</p>
      </div>
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer group">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-10 h-10 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center group-hover:bg-blue-100 transition-colors">
            <TruckIcon class="w-5 h-5" />
          </div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">配送中</p>
        </div>
        <p class="text-3xl font-black text-gray-900">{{ stats.delivering }}</p>
      </div>
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer group">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-10 h-10 bg-purple-50 text-purple-600 rounded-xl flex items-center justify-center group-hover:bg-purple-100 transition-colors">
            <AwardIcon class="w-5 h-5" />
          </div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">累计完成</p>
        </div>
        <p class="text-3xl font-black text-gray-900">{{ stats.totalCompleted }}</p>
      </div>
    </div>

    <!-- Current Task -->
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm">
      <div class="p-5 border-b border-gray-50">
        <h2 class="text-base font-black text-gray-900">当前任务</h2>
      </div>
      <div v-if="currentTask" class="p-5">
        <div class="flex items-center justify-between mb-4">
          <div>
            <p class="font-bold text-gray-900">{{ currentTask.orderNo }}</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ currentTask.createTime }}</p>
          </div>
          <span class="px-3 py-1.5 rounded-lg text-xs font-bold" :class="getStatusClass(currentTask.deliveryStatus || '')">
            {{ currentTask.deliveryStatus }}
          </span>
        </div>
        <div class="bg-gray-50 rounded-xl p-4 space-y-2 text-sm">
          <div class="flex items-start gap-2">
            <MapPinIcon class="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
            <span class="text-gray-600">{{ currentTask.receiverAddress }}</span>
          </div>
          <div class="flex items-center gap-2">
            <UserIcon class="w-4 h-4 text-gray-400 flex-shrink-0" />
            <span class="text-gray-600">{{ currentTask.receiverName }} {{ currentTask.receiverPhone }}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-gray-400 text-lg leading-none font-bold w-4 text-center flex-shrink-0">&yen;</span>
            <span class="text-gray-600 font-bold">{{ currentTask.totalAmount?.toFixed(2) }}</span>
          </div>
        </div>
        <div class="flex gap-3 mt-4">
          <button
            v-if="currentTask.deliveryStatus === '待取货'"
            @click="startDeliver"
            class="flex-1 bg-green-600 text-white py-2.5 rounded-xl font-bold text-sm hover:bg-green-700 transition-colors"
          >
            开始配送
          </button>
          <button
            v-if="currentTask.deliveryStatus === '配送中'"
            @click="confirmDeliver"
            class="flex-1 bg-blue-600 text-white py-2.5 rounded-xl font-bold text-sm hover:bg-blue-700 transition-colors"
          >
            确认送达
          </button>
        </div>
      </div>
      <div v-else class="p-10 text-center">
        <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center mx-auto mb-3">
          <BikeIcon class="w-8 h-8 text-gray-300" />
        </div>
        <p class="text-gray-400 font-medium text-sm">暂无进行中的任务</p>
        <p class="text-gray-300 text-xs mt-1">前往接单大厅领取新订单</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  Bike as BikeIcon,
  CheckCircle as CheckCircleIcon,
  Clock as ClockIcon,
  Truck as TruckIcon,
  Award as AwardIcon,
  MapPin as MapPinIcon,
  User as UserIcon,
} from 'lucide-vue-next'
import { courierApi } from '@/api/courier'
import type { CourierTask } from '@/api/courier'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const stats = ref({
  todayCompleted: 0,
  pending: 0,
  delivering: 0,
  totalCompleted: 0,
})

const currentTask = ref<CourierTask | null>(null)

const getStatusClass = (status: string) => {
  switch (status) {
    case '待分配': return 'bg-gray-100 text-gray-600'
    case '待取货': return 'bg-orange-100 text-orange-700'
    case '配送中': return 'bg-blue-100 text-blue-700'
    case '已完成': return 'bg-green-100 text-green-700'
    default: return 'bg-gray-100 text-gray-600'
  }
}

const computeStats = (tasks: CourierTask[]) => {
  const today = new Date().toDateString()
  const todayCompleted = tasks.filter(t =>
    t.deliveryStatus === '已完成' && t.finishTime && new Date(t.finishTime).toDateString() === today
  ).length
  const pending = tasks.filter(t => t.deliveryStatus === '待取货').length
  const delivering = tasks.filter(t => t.deliveryStatus === '配送中').length
  const totalCompleted = tasks.filter(t => t.deliveryStatus === '已完成').length
  stats.value = { todayCompleted, pending, delivering, totalCompleted }
}

const findCurrentTask = (tasks: CourierTask[]) => {
  const inProgress = tasks.find(t =>
    t.deliveryStatus === '待取货' || t.deliveryStatus === '配送中'
  )
  currentTask.value = inProgress ?? null
}

const refreshTasks = async () => {
  const tasks = await courierApi.getTasks()
  const list = tasks ?? []
  computeStats(list)
  findCurrentTask(list)
}

onMounted(async () => {
  try {
    await refreshTasks()
  } catch {
    showToast('任务加载失败，请刷新重试', 'error')
  }
})

const startDeliver = async () => {
  if (!currentTask.value) return
  try {
    await courierApi.updateTaskStatus(currentTask.value.orderId, '配送中')
    await refreshTasks()
    showToast('已开始配送', 'success')
  } catch {
    showToast('操作失败，请重试', 'error')
  }
}

const confirmDeliver = async () => {
  if (!currentTask.value) return
  try {
    await courierApi.updateTaskStatus(currentTask.value.orderId, '已完成')
    await refreshTasks()
    showToast('配送完成', 'success')
  } catch {
    showToast('操作失败，请重试', 'error')
  }
}
</script>
