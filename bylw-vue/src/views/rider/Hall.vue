<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-gray-900">接单大厅</h1>
        <p class="text-sm text-gray-400 mt-1">{{ hallOrders.length }} 个订单可接</p>
      </div>
      <button @click="loadHall" :disabled="loading"
        class="flex items-center gap-2 px-4 py-2 rounded-xl bg-white border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-50">
        <RefreshCwIcon class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        刷新
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="bg-red-50 rounded-2xl border border-red-100 p-8 text-center">
      <AlertCircleIcon class="w-10 h-10 text-red-300 mx-auto mb-3" />
      <p class="text-red-500 font-bold">加载失败</p>
      <p class="text-red-400 text-sm mt-1">{{ loadError }}</p>
      <button @click="loadHall" class="mt-4 text-sm text-green-600 font-bold hover:underline">重试</button>
    </div>

    <!-- Empty -->
    <div v-else-if="hallOrders.length === 0" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-16 text-center">
      <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
        <InboxIcon class="w-8 h-8 text-gray-300" />
      </div>
      <p class="text-gray-400 font-bold">暂无可接订单</p>
      <p class="text-gray-300 text-sm mt-2">请先以普通用户身份下单并完成支付</p>
    </div>

    <!-- Order List -->
    <div v-else class="space-y-4">
      <div v-for="order in hallOrders" :key="order.orderId"
        class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden hover:shadow-lg hover:border-emerald-200 transition-all duration-300 relative group">
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-emerald-400 to-emerald-600 opacity-0 group-hover:opacity-100 transition-opacity"></div>
        <div class="p-5">
          <div class="flex items-center justify-between mb-4">
            <div>
              <p class="font-bold text-gray-900">{{ order.orderNo }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ order.createTime }}</p>
            </div>
            <span v-if="order.preAssigned" class="px-3 py-1.5 rounded-lg text-xs font-bold bg-blue-50 text-blue-600">
              已分配给你
            </span>
            <span v-else class="px-3 py-1.5 rounded-lg text-xs font-bold bg-orange-50 text-orange-600 animate-pulse">
              可抢单
            </span>
          </div>

          <div class="bg-gray-50 rounded-xl p-4 space-y-2 text-sm">
            <div class="flex items-start gap-2">
              <MapPinIcon class="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
              <span class="text-gray-600">{{ order.receiverAddress }}</span>
            </div>
            <div class="flex items-center gap-2">
              <UserIcon class="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span class="text-gray-600">{{ order.receiverName }} {{ order.receiverPhone }}</span>
            </div>
          </div>

          <div class="flex items-center justify-between mt-4">
            <p class="text-xl font-black text-orange-500">&yen;{{ order.totalAmount?.toFixed(2) }}</p>
            <p class="text-xs text-gray-400">{{ order.deliveryStatus }}</p>
          </div>
        </div>

        <div class="border-t border-gray-50 px-5 py-3 bg-gray-50/50">
          <button
            v-if="order.preAssigned"
            @click="confirmClaim(order)"
            :disabled="claimingOrderId === order.orderId"
            class="w-full bg-blue-600 text-white py-2.5 rounded-xl font-bold text-sm hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ claimingOrderId === order.orderId ? '确认领取中...' : '确认领取' }}
          </button>
          <button
            v-else
            @click="claimOrder(order)"
            :disabled="claimingOrderId === order.orderId"
            class="w-full bg-green-600 text-white py-2.5 rounded-xl font-bold text-sm hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ claimingOrderId === order.orderId ? '抢单中...' : '抢单' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  Inbox as InboxIcon,
  RefreshCw as RefreshCwIcon,
  AlertCircle as AlertCircleIcon,
  MapPin as MapPinIcon,
  User as UserIcon,
} from 'lucide-vue-next'
import { courierApi, type HallOrder } from '@/api/courier'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const hallOrders = ref<HallOrder[]>([])
const loading = ref(false)
const claimingOrderId = ref<number | null>(null)
const loadError = ref('')

const loadHall = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const orders = await courierApi.getHall()
    hallOrders.value = orders ?? []
  } catch (e: unknown) {
    showToast('加载接单大厅失败', 'error')
    loadError.value = e instanceof Error ? e.message : '未知错误'
  } finally {
    loading.value = false
  }
}

const claimOrder = async (order: HallOrder) => {
  if (claimingOrderId.value !== null) return
  claimingOrderId.value = order.orderId
  try {
    await courierApi.claimOrder(order.orderId)
    showToast('抢单成功', 'success')
    await loadHall()
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '操作失败，请重试', 'error')
    await loadHall()
  } finally {
    claimingOrderId.value = null
  }
}

const confirmClaim = async (order: HallOrder) => {
  if (claimingOrderId.value !== null) return
  claimingOrderId.value = order.orderId
  try {
    await courierApi.claimOrder(order.orderId)
    showToast('已确认领取', 'success')
    await loadHall()
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '操作失败，请重试', 'error')
    await loadHall()
  } finally {
    claimingOrderId.value = null
  }
}

onMounted(loadHall)
</script>
