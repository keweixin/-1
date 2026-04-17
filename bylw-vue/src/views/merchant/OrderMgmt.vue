<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-black text-gray-900">我的订单</h1>
      <p class="text-sm text-gray-400 mt-1">共 {{ total }} 条订单</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="orders.length === 0" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-16 text-center">
      <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
        <ClipboardListIcon class="w-8 h-8 text-gray-300" />
      </div>
      <p class="text-gray-400 font-bold">暂无订单数据</p>
    </div>

    <!-- Table -->
    <div v-else class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full text-left text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">订单号</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">收货人</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">金额</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">状态</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">下单时间</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="order in orders" :key="order.orderId"
              :class="['hover:bg-blue-50/30 transition-colors group', order.orderStatus === '待接单' ? 'bg-yellow-50/30' : '']">
              <td class="px-5 py-4 font-bold text-gray-900">
                <div class="flex items-center gap-2">
                  {{ order.orderNo }}
                  <span v-if="order.orderStatus === '待接单'" class="w-2 h-2 bg-orange-500 rounded-full animate-pulse"></span>
                </div>
              </td>
              <td class="px-5 py-4 text-gray-600">{{ order.receiverName || '-' }}</td>
              <td class="px-5 py-4 text-orange-500 font-bold">&yen;{{ order.totalAmount?.toFixed(2) }}</td>
              <td class="px-5 py-4">
                <span :class="['px-2.5 py-1 rounded-lg text-xs font-bold', statusClass(order.orderStatus)]">
                  {{ order.orderStatus }}
                </span>
              </td>
              <td class="px-5 py-4 text-gray-400">{{ (order.createTime || '').replace('T', ' ').slice(0, 16) }}</td>
              <td class="px-5 py-4">
                <button
                  v-if="order.orderStatus === '待接单'"
                  @click="acceptOrder(order)"
                  :disabled="acceptingOrderId === order.orderId"
                  class="px-3 py-1.5 rounded-lg text-xs font-bold bg-blue-600 text-white hover:bg-blue-700 hover:scale-105 disabled:bg-gray-300 disabled:cursor-not-allowed transition-all"
                >
                  {{ acceptingOrderId === order.orderId ? '处理中...' : '确认接单' }}
                </button>
                <span v-else class="text-xs text-gray-300">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex items-center justify-center gap-3">
      <button :disabled="pageNum <= 1" @click="pageNum--"
        class="px-4 py-2 rounded-xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 disabled:opacity-30 transition-colors">
        上一页
      </button>
      <span class="text-sm text-gray-500 font-bold">{{ pageNum }} / {{ totalPages }}</span>
      <button :disabled="pageNum >= totalPages" @click="pageNum++"
        class="px-4 py-2 rounded-xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 disabled:opacity-30 transition-colors">
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { ClipboardList as ClipboardListIcon } from 'lucide-vue-next'
import { merchantApi } from '@/api/merchant'
import type { OrderDTO } from '@/api/order'
import { useToast } from '@/composables/useToast'

const toast = useToast()
const loading = ref(true)
const orders = ref<OrderDTO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)
const acceptingOrderId = ref<number | null>(null)

function statusClass(status: string): string {
  if (status === '已完成') return 'bg-green-50 text-green-600'
  if (status === '已取消' || status === '已退款') return 'bg-red-50 text-red-600'
  if (status === '配送中') return 'bg-blue-50 text-blue-600'
  return 'bg-yellow-50 text-yellow-600'
}

async function loadOrders() {
  loading.value = true
  try {
    const result = await merchantApi.getOrders({ pageNum: pageNum.value, pageSize })
    orders.value = result.records || []
    total.value = result.total
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function acceptOrder(order: OrderDTO) {
  acceptingOrderId.value = order.orderId
  try {
    await merchantApi.acceptOrder(order.orderId)
    toast.show('已接单，等待配送', 'success')
    await loadOrders()
  } catch (e: unknown) {
    toast.show(e instanceof Error ? e.message : '接单失败', 'error')
  } finally {
    acceptingOrderId.value = null
  }
}

watch(pageNum, loadOrders)
onMounted(loadOrders)
</script>
