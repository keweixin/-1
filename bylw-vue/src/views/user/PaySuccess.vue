<template>
  <div class="flex min-h-screen items-center justify-center bg-gradient-to-br from-green-50 via-emerald-50 to-teal-50 p-4">
    <div class="w-full max-w-lg rounded-[3rem] border border-green-100 bg-white p-12 text-center shadow-2xl shadow-green-900/5">
      <div class="mx-auto mb-8 flex h-28 w-28 items-center justify-center rounded-full bg-green-500 shadow-2xl shadow-green-500/30">
        <CheckCircleIcon class="h-16 w-16 text-white" />
      </div>

      <h1 class="mb-3 text-3xl font-black text-gray-900">支付成功</h1>
      <p class="mb-2 text-sm font-medium text-gray-500">你的订单已支付成功，系统会继续推进配送流程。</p>
      <p class="mb-8 text-sm text-gray-400">离开当前页面后，仍然可以从订单列表重新进入订单详情。</p>

      <div v-if="orderInfo" class="mb-8 rounded-2xl bg-gray-50 p-5 text-left">
        <p class="mb-3 text-sm font-bold text-gray-700">订单信息</p>
        <div class="space-y-2 text-sm text-gray-500">
          <p>订单号：<span class="font-bold text-gray-700">{{ orderInfo.orderNo }}</span></p>
          <p>订单金额：<span class="font-black text-orange-500">￥{{ orderInfo.totalAmount?.toFixed(2) }}</span></p>
          <p>下单时间：<span class="font-medium text-gray-700">{{ formatDisplayTime(orderInfo.createTime) }}</span></p>
        </div>
      </div>

      <div class="mb-8 rounded-2xl border border-orange-100 bg-orange-50 p-5">
        <div class="mb-2 flex items-center gap-3">
          <GiftIcon class="h-5 w-5 text-orange-500" />
          <span class="text-sm font-black text-orange-600">绿色积分奖励</span>
        </div>
        <p class="text-xs leading-6 text-orange-500/80">完成配送后，系统会自动发放积分到你的账户。</p>
      </div>

      <div class="flex flex-col gap-3">
        <router-link
          v-if="orderInfo?.orderId"
          :to="`/orders/${orderInfo.orderId}`"
          class="flex items-center justify-center gap-2 rounded-2xl bg-green-600 px-8 py-4 font-black text-white shadow-xl shadow-green-600/20 transition-all hover:bg-green-500"
        >
          <ClipboardListIcon class="h-5 w-5" />
          查看订单详情
        </router-link>
        <router-link
          to="/orders"
          class="flex items-center justify-center gap-2 rounded-2xl border border-gray-200 bg-white px-8 py-4 font-black text-gray-600 transition-all hover:bg-gray-50"
        >
          <ClipboardListIcon class="h-5 w-5" />
          查看我的订单
        </router-link>
        <router-link
          to="/food-hall"
          class="flex items-center justify-center gap-2 rounded-2xl border border-gray-200 bg-white px-8 py-4 font-black text-gray-600 transition-all hover:bg-gray-50"
        >
          <ShoppingBagIcon class="h-5 w-5" />
          继续逛临期食品
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi, type OrderDTO } from '@/api/order'
import {
  CheckCircle as CheckCircleIcon,
  ClipboardList as ClipboardListIcon,
  Gift as GiftIcon,
  ShoppingBag as ShoppingBagIcon,
} from 'lucide-vue-next'

const route = useRoute()
const orderInfo = ref<OrderDTO | null>(null)

async function loadOrderInfo() {
  const orderId = Number(route.query.orderId)
  if (!orderId) return

  try {
    orderInfo.value = await orderApi.getById(orderId)
  } catch {
    orderInfo.value = null
  }
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(loadOrderInfo)
</script>
