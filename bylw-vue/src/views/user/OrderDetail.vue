<template>
  <div class="mx-auto max-w-6xl px-4 py-12">
    <div class="mb-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <div>
        <button class="text-sm font-bold text-green-600 transition-colors hover:text-green-700" @click="router.push('/orders')">
          返回订单列表
        </button>
        <h1 class="mt-4 text-4xl font-black text-gray-900">订单详情</h1>
        <p class="mt-3 text-sm text-gray-500">这里展示订单状态、商品明细、收货信息和配送进度。</p>
      </div>

      <div v-if="order" class="flex flex-col items-start gap-3 lg:items-end">
        <p class="text-sm font-bold uppercase tracking-[0.24em] text-gray-400">订单号</p>
        <p class="text-lg font-black text-gray-900">{{ order.orderNo }}</p>
        <StatusTag :status="order.orderStatus" />
        <button
          v-if="['待配送', '配送中', '已完成'].includes(order.orderStatus)"
          @click="router.push('/appeals')"
          class="text-xs text-orange-500 font-bold hover:underline"
        >
          申请售后
        </button>
        <button
          v-if="['待支付', '待接单', '待配送'].includes(order.orderStatus)"
          @click="handleCancelOrder"
          class="text-xs text-red-500 font-bold hover:underline"
        >
          取消订单
        </button>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center rounded-[2rem] border border-gray-100 bg-white py-24 text-gray-400 shadow-sm">
      <div class="mr-3 h-6 w-6 animate-spin rounded-full border-2 border-green-600 border-t-transparent"></div>
      订单详情加载中...
    </div>

    <div v-else-if="!order" class="rounded-[2rem] border border-gray-100 bg-white py-24 text-center text-gray-400 shadow-sm">
      未找到该订单，或你没有权限查看。
    </div>

    <div v-else class="space-y-8">
      <section class="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-4">
        <article class="rounded-[1.75rem] border border-gray-100 bg-white p-6 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-[0.22em] text-gray-400">订单金额</p>
          <p class="mt-3 text-3xl font-black text-orange-500">￥{{ order.totalAmount.toFixed(2) }}</p>
        </article>
        <article class="rounded-[1.75rem] border border-gray-100 bg-white p-6 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-[0.22em] text-gray-400">支付状态</p>
          <p class="mt-3 text-lg font-black text-gray-900">{{ order.payStatus || '-' }}</p>
        </article>
        <article class="rounded-[1.75rem] border border-gray-100 bg-white p-6 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-[0.22em] text-gray-400">创建时间</p>
          <p class="mt-3 text-sm font-bold text-gray-900">{{ formatDisplayTime(order.createTime) }}</p>
        </article>
        <article class="rounded-[1.75rem] border border-gray-100 bg-white p-6 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-[0.22em] text-gray-400">支付时间</p>
          <p class="mt-3 text-sm font-bold text-gray-900">{{ formatDisplayTime(order.payTime) }}</p>
        </article>
      </section>

      <!-- Cancel Order Button -->
      <div v-if="canCancelOrder" class="flex justify-end">
        <button
          @click="handleCancelOrder"
          :disabled="cancelling"
          class="px-6 py-3 bg-red-50 text-red-600 rounded-xl font-bold hover:bg-red-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="cancelling">取消中...</span>
          <span v-else>取消订单</span>
        </button>
      </div>

      <section class="grid grid-cols-1 gap-8 lg:grid-cols-2">
        <article class="rounded-[2rem] border border-gray-100 bg-white p-7 shadow-sm">
          <h2 class="text-xl font-black text-gray-900">收货信息</h2>
          <div class="mt-6 space-y-4 text-sm text-gray-600">
            <p><span class="font-bold text-gray-900">收货人：</span>{{ order.receiverName }}</p>
            <p><span class="font-bold text-gray-900">联系电话：</span>{{ maskPhone(order.receiverPhone) }}</p>
            <p><span class="font-bold text-gray-900">收货地址：</span>{{ order.receiverAddress }}</p>
            <p><span class="font-bold text-gray-900">订单备注：</span>{{ order.remark || '无' }}</p>
          </div>
        </article>

        <article class="rounded-[2rem] border border-gray-100 bg-white p-7 shadow-sm">
          <h2 class="text-xl font-black text-gray-900">配送信息</h2>
          <div v-if="order.delivery" class="mt-6 space-y-4 text-sm text-gray-600">
            <p><span class="font-bold text-gray-900">配送员：</span>{{ order.delivery.courierName || '待分配' }}</p>
            <p><span class="font-bold text-gray-900">联系电话：</span>{{ order.delivery?.courierPhone ? maskPhone(order.delivery.courierPhone) : '待补充' }}</p>
            <p><span class="font-bold text-gray-900">配送状态：</span>{{ order.delivery.deliveryStatus || '待分配' }}</p>
            <p><span class="font-bold text-gray-900">派送时间：</span>{{ formatDisplayTime(order.delivery.dispatchTime) }}</p>
            <p><span class="font-bold text-gray-900">完成时间：</span>{{ formatDisplayTime(order.delivery.finishTime) }}</p>
          </div>
          <p v-else class="mt-6 text-sm text-gray-400">该订单暂未进入配送流程。</p>
        </article>
      </section>

      <section class="overflow-hidden rounded-[2rem] border border-gray-100 bg-white shadow-sm">
        <div class="border-b border-gray-100 px-7 py-5">
          <h2 class="text-xl font-black text-gray-900">商品明细</h2>
        </div>
        <div class="divide-y divide-gray-100">
          <article
            v-for="item in order.items"
            :key="item.itemId"
            class="flex flex-col gap-4 px-7 py-5 sm:flex-row sm:items-center sm:justify-between"
          >
            <div>
              <p class="text-base font-black text-gray-900">{{ item.foodName }}</p>
              <p class="mt-1 text-sm text-gray-500">数量：{{ item.quantity }}</p>
            </div>
            <div class="flex items-center gap-8 text-sm">
              <p class="text-gray-500">单价：<span class="font-bold text-gray-900">￥{{ item.price.toFixed(2) }}</span></p>
              <p class="text-gray-500">小计：<span class="font-black text-orange-500">￥{{ item.subtotal.toFixed(2) }}</span></p>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, type OrderDTO } from '@/api/order'
import StatusTag from '@/components/ui/StatusTag.vue'
import { maskPhone } from '@/utils/cn'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const cancelling = ref(false)
const order = ref<OrderDTO | null>(null)

const canCancelOrder = computed(() => {
  if (!order.value) return false
  const status = order.value.orderStatus
  const cancellableStatuses = ['待支付', '待接单', '待配送']
  return cancellableStatuses.includes(status)
})

async function loadOrderDetail() {
  const id = Number(route.params.id)
  if (!Number.isFinite(id)) {
    order.value = null
    loading.value = false
    showToast('无效的订单ID', 'error')
    return
  }
  loading.value = true
  try {
    order.value = await orderApi.getById(id)
  } catch {
    order.value = null
    showToast('加载订单详情失败', 'error')
  } finally {
    loading.value = false
  }
}

async function handleCancelOrder() {
  if (!order.value || cancelling.value) return

  if (!confirm('确定要取消该订单吗？')) return

  cancelling.value = true
  try {
    await orderApi.cancel(order.value.orderId)
    showToast('订单已取消', 'success')
    await loadOrderDetail()
  } catch (error) {
    showToast('取消订单失败', 'error')
  } finally {
    cancelling.value = false
  }
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(loadOrderDetail)
</script>
