<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-gray-900">我的订单</h1>
        <p class="text-sm text-gray-400 mt-1">共 {{ total }} 条订单</p>
      </div>
      <select v-model="statusFilter" @change="loadOrders"
        class="rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm font-medium text-gray-600 outline-none focus:border-blue-500">
        <option value="">全部状态</option>
        <option value="待接单">待接单</option>
        <option value="待配送">待配送</option>
        <option value="配送中">配送中</option>
        <option value="已完成">已完成</option>
        <option value="已取消">已取消</option>
      </select>
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
                <div class="flex items-center gap-2">
                  <button
                    v-if="order.orderStatus === '待接单'"
                    @click="acceptOrder(order)"
                    :disabled="acceptingOrderId === order.orderId"
                    class="px-3 py-1.5 rounded-lg text-xs font-bold bg-blue-600 text-white hover:bg-blue-700 hover:scale-105 disabled:bg-gray-300 disabled:cursor-not-allowed transition-all"
                  >
                    {{ acceptingOrderId === order.orderId ? '处理中...' : '确认接单' }}
                  </button>
                  <button
                    v-if="order.orderStatus === '待接单'"
                    @click="rejectOrder(order)"
                    class="px-3 py-1.5 rounded-lg text-xs font-bold bg-red-50 text-red-600 border border-red-200 hover:bg-red-100 transition-all"
                  >
                    拒单
                  </button>
                  <span v-if="order.orderStatus !== '待接单'" class="text-xs text-gray-300">-</span>
                </div>
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
const statusFilter = ref('')

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
    let allOrders = result.records || []
    if (statusFilter.value) {
      allOrders = allOrders.filter((o: OrderDTO) => o.orderStatus === statusFilter.value)
    }
    orders.value = allOrders
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

async function rejectOrder(order: OrderDTO) {
  if (!confirm('确定要拒绝此订单吗？')) return
  try {
    await merchantApi.rejectOrder(order.orderId)
    toast.show('已拒绝订单', 'success')
    await loadOrders()
  } catch (e: unknown) {
    toast.show(e instanceof Error ? e.message : '拒单失败', 'error')
  }
}

watch(pageNum, loadOrders)
onMounted(loadOrders)
</script>
