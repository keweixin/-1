<template>
  <div class="space-y-8">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black text-gray-900">订单服务管理</h1>
        <p class="mt-2 text-sm text-gray-500">查看订单详情、商品明细与配送信息，追踪整个履约过程。</p>
      </div>
      <div class="flex items-center gap-3">
        <select v-model="statusFilter" class="rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-medium text-gray-600 outline-none focus:border-green-500">
          <option value="">全部状态</option>
          <option value="待支付">待支付</option>
          <option value="待接单">待接单</option>
          <option value="待配送">待配送</option>
          <option value="配送中">配送中</option>
          <option value="已完成">已完成</option>
          <option value="已取消">已取消</option>
        </select>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="loadOrders">
          刷新
        </button>
      </div>
    </div>

    <div
      v-if="feedback"
      class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'"
    >
      {{ feedback.message }}
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">订单号</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">用户</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">金额</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">状态</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">时间</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading">
            <td colspan="6" class="px-8 py-12 text-center text-gray-400">
              <div class="flex items-center justify-center">
                <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                加载中...
              </div>
            </td>
          </tr>
          <tr v-else-if="orders.length === 0">
            <td colspan="6" class="px-8 py-12 text-center text-gray-400">暂无订单</td>
          </tr>
          <tr v-else v-for="order in orders" :key="order.orderId" class="hover:bg-gray-50 transition-colors">
            <td class="px-8 py-6 text-sm font-bold text-gray-900">{{ order.orderNo }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ order.username || order.receiverName }}</td>
            <td class="px-8 py-6 text-sm font-black text-orange-500">¥{{ order.totalAmount.toFixed(2) }}</td>
            <td class="px-8 py-6">
              <StatusTag :status="order.orderStatus" />
            </td>
            <td class="px-8 py-6 text-sm text-gray-400">{{ formatDisplayTime(order.createTime) }}</td>
            <td class="px-8 py-6">
              <button
                class="text-green-600 hover:underline text-sm font-medium"
                :disabled="detailLoading && currentOrderId === order.orderId"
                @click="openDetail(order.orderId)"
              >
                {{ detailLoading && currentOrderId === order.orderId ? '加载中...' : '详情' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="detailVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4" @click.self="detailVisible = false">
      <div class="w-full max-w-4xl bg-white rounded-[32px] shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">订单详情</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="detailVisible = false">
            <XIcon class="w-5 h-5" />
          </button>
        </div>

        <div v-if="detailLoading" class="flex items-center justify-center py-16 text-gray-400">
          <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
          加载订单详情...
        </div>

        <div v-else-if="currentOrder" class="space-y-8">
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
            <div class="rounded-2xl bg-gray-50 border border-gray-100 p-5">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">订单号</p>
              <p class="mt-2 font-black text-gray-900 break-all">{{ currentOrder.orderNo }}</p>
            </div>
            <div class="rounded-2xl bg-gray-50 border border-gray-100 p-5">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">订单状态</p>
              <div class="mt-2"><StatusTag :status="currentOrder.orderStatus" /></div>
            </div>
            <div class="rounded-2xl bg-gray-50 border border-gray-100 p-5">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">支付状态</p>
              <p class="mt-2 font-black text-gray-900">{{ currentOrder.payStatus }}</p>
            </div>
            <div class="rounded-2xl bg-gray-50 border border-gray-100 p-5">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">订单金额</p>
              <p class="mt-2 font-black text-orange-500">¥{{ currentOrder.totalAmount.toFixed(2) }}</p>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="rounded-3xl border border-gray-100 p-6">
              <h4 class="text-lg font-bold text-gray-900 mb-4">收货信息</h4>
              <div class="space-y-3 text-sm text-gray-600">
                <p><span class="font-bold text-gray-900">收货人：</span>{{ currentOrder.receiverName }}</p>
                <p><span class="font-bold text-gray-900">联系电话：</span>{{ currentOrder.receiverPhone }}</p>
                <p><span class="font-bold text-gray-900">收货地址：</span>{{ currentOrder.receiverAddress }}</p>
                <p><span class="font-bold text-gray-900">订单备注：</span>{{ currentOrder.remark || '无' }}</p>
                <p><span class="font-bold text-gray-900">创建时间：</span>{{ formatDisplayTime(currentOrder.createTime) }}</p>
                <p><span class="font-bold text-gray-900">支付时间：</span>{{ formatDisplayTime(currentOrder.payTime) }}</p>
              </div>
            </div>

            <div class="rounded-3xl border border-gray-100 p-6">
              <h4 class="text-lg font-bold text-gray-900 mb-4">配送信息</h4>
              <div v-if="currentOrder.delivery" class="space-y-3 text-sm text-gray-600">
                <p><span class="font-bold text-gray-900">配送员：</span>{{ currentOrder.delivery.courierName || '未分配' }}</p>
                <p><span class="font-bold text-gray-900">联系电话：</span>{{ currentOrder.delivery.courierPhone || '未填写' }}</p>
                <p><span class="font-bold text-gray-900">配送状态：</span>{{ currentOrder.delivery.deliveryStatus || '未分配' }}</p>
                <p><span class="font-bold text-gray-900">派送时间：</span>{{ formatDisplayTime(currentOrder.delivery.dispatchTime) }}</p>
                <p><span class="font-bold text-gray-900">完成时间：</span>{{ formatDisplayTime(currentOrder.delivery.finishTime) }}</p>
              </div>
              <div v-else class="text-sm text-gray-400">该订单暂未进入配送流程。</div>
            </div>
          </div>

          <div class="rounded-3xl border border-gray-100 overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-100">
              <h4 class="text-lg font-bold text-gray-900">商品明细</h4>
            </div>
            <table class="w-full">
              <thead>
                <tr class="bg-gray-50">
                  <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">食品</th>
                  <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">单价</th>
                  <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">数量</th>
                  <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">小计</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100">
                <tr v-for="item in currentOrder.items" :key="item.itemId">
                  <td class="px-6 py-4 font-medium text-gray-900">{{ item.foodName }}</td>
                  <td class="px-6 py-4 text-sm text-gray-600">¥{{ item.price.toFixed(2) }}</td>
                  <td class="px-6 py-4 text-sm text-gray-600">{{ item.quantity }}</td>
                  <td class="px-6 py-4 text-sm font-bold text-orange-500">¥{{ item.subtotal.toFixed(2) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { orderApi, type OrderDTO } from '@/api/order'
import StatusTag from '@/components/ui/StatusTag.vue'
import { X } from 'lucide-vue-next'

interface FeedbackState {
  type: 'success' | 'error'
  message: string
}

const XIcon = X
const orders = ref<OrderDTO[]>([])
const loading = ref(true)
const detailLoading = ref(false)
const detailVisible = ref(false)
const currentOrder = ref<OrderDTO | null>(null)
const currentOrderId = ref<number | null>(null)
const statusFilter = ref('')
const feedback = ref<FeedbackState | null>(null)

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
}

async function loadOrders() {
  loading.value = true
  try {
    const result = await orderApi.listAll({
      pageNum: 1,
      pageSize: 100,
      status: statusFilter.value || undefined,
    })
    orders.value = result.records
  } catch (error) {
    orders.value = []
    setFeedback('error', error instanceof Error ? error.message : '订单数据加载失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(orderId: number) {
  detailVisible.value = true
  detailLoading.value = true
  currentOrderId.value = orderId
  try {
    currentOrder.value = await orderApi.getById(orderId)
  } catch (error) {
    currentOrder.value = null
    detailVisible.value = false
    setFeedback('error', error instanceof Error ? error.message : '订单详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

watch(statusFilter, () => { loadOrders() })
watch(detailVisible, (val) => { if (!val) currentOrderId.value = null })

onMounted(loadOrders)
</script>
