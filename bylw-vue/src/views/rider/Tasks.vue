<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-black text-gray-900">配送任务</h1>
      <p class="text-sm text-gray-400 mt-1">管理和处理你的配送订单</p>
    </div>

    <!-- Filter Tabs -->
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-1.5">
      <div class="flex gap-1">
        <button
          v-for="status in statuses"
          :key="status.value"
          @click="filterStatus = status.value"
          class="flex-1 px-4 py-2.5 rounded-xl text-sm font-bold transition-all"
          :class="filterStatus === status.value
            ? 'bg-green-600 text-white shadow-sm'
            : 'text-gray-500 hover:bg-gray-50'"
        >
          {{ status.label }}
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Task List -->
    <div v-else-if="filteredTasks.length > 0" class="space-y-4">
      <div v-for="task in filteredTasks" :key="task.orderId"
        class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <div class="p-5">
          <div class="flex items-center justify-between mb-4">
            <div>
              <p class="font-bold text-gray-900">{{ task.orderNo }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ task.createTime }}</p>
            </div>
            <span class="px-3 py-1.5 rounded-lg text-xs font-bold" :class="getStatusClass(task.deliveryStatus || '')">
              {{ task.deliveryStatus }}
            </span>
          </div>

          <div class="bg-gray-50 rounded-xl p-4 space-y-3 text-sm">
            <div class="flex items-start gap-2">
              <MapPinIcon class="w-4 h-4 text-emerald-500 mt-0.5 flex-shrink-0" />
              <div>
                <span class="text-gray-400 text-xs block mb-1">收货地址</span>
                <span class="text-gray-900 font-medium">{{ task.receiverAddress || '暂无' }}</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <UserIcon class="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span class="text-gray-600">{{ task.receiverName }} {{ task.receiverPhone }}</span>
            </div>
          </div>

          <div class="flex items-center justify-between mt-4">
            <p class="text-lg font-black text-orange-500">&yen;{{ task.totalAmount?.toFixed(2) }}</p>
          </div>
        </div>

        <div v-if="task.deliveryStatus === '待取货' || task.deliveryStatus === '配送中' || task.deliveryStatus === '待分配'"
          class="border-t border-gray-50 px-5 py-3 bg-gray-50/50 flex gap-3">
          <button
            v-if="task.deliveryStatus === '待取货'"
            @click="startDelivery(task)"
            class="flex-1 bg-green-600 text-white py-2.5 rounded-xl font-bold text-sm hover:bg-green-700 transition-colors"
          >
            开始配送
          </button>
          <button
            v-if="task.deliveryStatus === '配送中'"
            @click="confirmDelivery(task)"
            class="flex-1 bg-blue-600 text-white py-2.5 rounded-xl font-bold text-sm hover:bg-blue-700 transition-colors"
          >
            确认送达
          </button>
          <button
            v-if="task.deliveryStatus === '待分配'"
            @click="acceptTask(task)"
            class="flex-1 bg-green-600 text-white py-2.5 rounded-xl font-bold text-sm hover:bg-green-700 transition-colors"
          >
            接受任务
          </button>
          <button
            v-if="task.deliveryStatus === '待分配'"
            @click="openRejectModal(task)"
            class="flex-1 bg-white border border-gray-200 text-gray-600 py-2.5 rounded-xl font-bold text-sm hover:bg-gray-50 transition-colors"
          >
            拒单
          </button>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="bg-white rounded-2xl border border-gray-100 shadow-sm p-16 text-center">
      <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
        <PackageIcon class="w-8 h-8 text-gray-300" />
      </div>
      <p class="text-gray-400 font-bold">暂无配送任务</p>
    </div>

    <!-- Reject Modal -->
    <div v-if="rejectModalVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4" @click.self="rejectModalVisible = false">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md">
        <h3 class="text-lg font-black text-gray-900 mb-2">确认拒单</h3>
        <p class="text-sm text-gray-500 mb-4">请输入拒单原因（可选）：</p>
        <textarea
          v-model="rejectReason"
          rows="3"
          class="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500 resize-none"
          placeholder="例如：地址超出配送范围"
        ></textarea>
        <div class="flex gap-3 mt-5">
          <button @click="rejectModalVisible = false"
            class="flex-1 py-2.5 rounded-xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-colors">
            取消
          </button>
          <button @click="confirmReject"
            class="flex-1 py-2.5 rounded-xl bg-red-600 text-white font-bold hover:bg-red-700 transition-colors">
            确认拒单
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Package as PackageIcon,
  MapPin as MapPinIcon,
  User as UserIcon,
} from 'lucide-vue-next'
import { courierApi } from '@/api/courier'
import type { CourierTask } from '@/api/courier'

const filterStatus = ref('all')
const statuses = [
  { label: '全部', value: 'all' },
  { label: '待分配', value: '待分配' },
  { label: '待取货', value: '待取货' },
  { label: '配送中', value: '配送中' },
]

const tasks = ref<CourierTask[]>([])
const loading = ref(false)
const rejectModalVisible = ref(false)
const rejectReason = ref('')
const currentRejectTask = ref<CourierTask | null>(null)

const filteredTasks = computed(() => {
  if (filterStatus.value === 'all') {
    return tasks.value.filter(t => t.deliveryStatus !== '已完成')
  }
  return tasks.value.filter(t => t.deliveryStatus === filterStatus.value)
})

const getStatusClass = (status: string) => {
  switch (status) {
    case '待分配': return 'bg-gray-100 text-gray-600'
    case '待取货': return 'bg-orange-100 text-orange-700'
    case '配送中': return 'bg-blue-100 text-blue-700'
    case '已完成': return 'bg-green-100 text-green-700'
    default: return 'bg-gray-100 text-gray-600'
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await courierApi.getTasks()
    tasks.value = res ?? []
  } catch {
    /* load failed silently */
  } finally {
    loading.value = false
  }
}

const acceptTask = async (task: CourierTask) => {
  await courierApi.updateTaskStatus(task.orderId, '待取货')
  await loadTasks()
}

const openRejectModal = (task: CourierTask) => {
  currentRejectTask.value = task
  rejectReason.value = ''
  rejectModalVisible.value = true
}

const confirmReject = async () => {
  if (!currentRejectTask.value) return
  // Note: rejectReason is collected but backend API doesn't support it yet
  // TODO: Pass rejectReason to API when backend adds support
  await courierApi.updateTaskStatus(currentRejectTask.value.orderId, '已拒绝')
  rejectModalVisible.value = false
  await loadTasks()
}

const startDelivery = async (task: CourierTask) => {
  await courierApi.updateTaskStatus(task.orderId, '配送中')
  await loadTasks()
}

const confirmDelivery = async (task: CourierTask) => {
  await courierApi.updateTaskStatus(task.orderId, '已完成')
  await loadTasks()
}

onMounted(loadTasks)
</script>
