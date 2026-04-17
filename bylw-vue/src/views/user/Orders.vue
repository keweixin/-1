<template>
  <div class="mx-auto max-w-7xl px-4 py-12">
    <div class="mb-8 flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <p class="text-sm font-bold uppercase tracking-[0.24em] text-green-600">Orders</p>
        <h1 class="mt-3 text-4xl font-black text-gray-900">我的订单</h1>
        <p class="mt-3 text-sm text-gray-500">点击订单卡片可查看详情；未支付订单可在支付页里完成支付。</p>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="payModalVisible"
        class="fixed inset-0 z-[200] flex items-center justify-center bg-black/55 px-4 backdrop-blur-sm"
        @click.self="closePayModal"
      >
        <div class="w-full max-w-md overflow-hidden rounded-[2.25rem] bg-white shadow-2xl">
          <div class="bg-gradient-to-r from-green-600 to-emerald-500 p-7 text-center text-white">
            <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/20">
              <QrCodeIcon class="h-8 w-8" />
            </div>
            <h2 class="text-2xl font-black">支付页面</h2>
            <p class="mt-2 text-sm text-white/75">扫码仅用于演示，点击“我已完成支付”后会直接按支付成功处理。</p>
          </div>

          <div class="space-y-5 bg-gray-50 p-8">
            <div class="mx-auto w-fit rounded-3xl border border-gray-100 bg-white p-4 shadow-sm">
              <canvas ref="qrCanvas" class="h-48 w-48"></canvas>
            </div>

            <div class="rounded-2xl bg-white px-5 py-4 text-center shadow-sm">
              <p class="text-xs font-bold uppercase tracking-[0.22em] text-gray-400">当前应付</p>
              <p class="mt-2 text-3xl font-black text-orange-500">￥{{ currentPayOrder?.totalAmount?.toFixed(2) }}</p>
            </div>

            <div v-if="payingOrderId === currentPayOrder?.orderId" class="flex items-center justify-center gap-3 py-2 text-green-600">
              <div class="h-5 w-5 animate-spin rounded-full border-2 border-green-600 border-t-transparent"></div>
              <span class="text-sm font-bold">支付处理中...</span>
            </div>

            <div class="space-y-3">
              <button
                v-if="payingOrderId !== currentPayOrder?.orderId"
                class="w-full rounded-2xl bg-green-600 py-4 text-base font-black text-white shadow-lg shadow-green-200 transition-all hover:bg-green-700"
                @click="confirmMockPay"
              >
                我已完成支付
              </button>
              <button
                v-if="payingOrderId !== currentPayOrder?.orderId"
                class="w-full rounded-2xl bg-white py-3 text-sm font-bold text-gray-500 transition-all hover:bg-gray-100"
                @click="closePayModal"
              >
                暂不支付
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <section class="overflow-hidden rounded-[2rem] border border-gray-100 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-24 text-gray-400">
        <div class="mr-3 h-6 w-6 animate-spin rounded-full border-2 border-green-600 border-t-transparent"></div>
        订单加载中...
      </div>

      <div v-else-if="orders.length === 0" class="flex flex-col items-center justify-center py-24 text-gray-400">
        <ShoppingBagIcon class="mb-4 h-14 w-14 opacity-30" />
        <p class="text-base font-medium">当前还没有订单</p>
        <router-link
          to="/food-hall"
          class="mt-5 inline-flex items-center rounded-2xl bg-green-600 px-5 py-3 text-sm font-bold text-white transition-all hover:bg-green-700"
        >
          去逛临期食品
        </router-link>
      </div>

      <div v-else class="divide-y divide-gray-100">
        <article
          v-for="order in orders"
          :key="order.orderId"
          class="cursor-pointer p-6 transition-colors hover:bg-gray-50/70"
          role="button"
          tabindex="0"
          @click="openOrderDetail(order.orderId)"
          @keydown.enter.prevent="openOrderDetail(order.orderId)"
          @keydown.space.prevent="openOrderDetail(order.orderId)"
        >
          <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
            <div class="min-w-0 flex-1">
              <div class="mb-4 flex flex-wrap items-center gap-3">
                <span class="rounded-full bg-gray-100 px-3 py-1 text-xs font-bold text-gray-500">{{ order.orderNo }}</span>
                <span class="text-xs text-gray-400">{{ formatDisplayTime(order.createTime) }}</span>
              </div>

              <div class="flex items-start gap-4">
                <div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-green-50 text-green-600">
                  <ShoppingBagIcon class="h-6 w-6" />
                </div>
                <div class="min-w-0 flex-1">
                  <p class="truncate text-base font-black text-gray-900">{{ order.items?.[0]?.foodName || '订单商品' }}</p>
                  <p v-if="(order.items?.length ?? 0) > 1" class="mt-1 text-sm text-gray-400">
                    共 {{ order.items?.length }} 件商品
                  </p>
                  <p class="mt-2 text-sm text-gray-500">
                    收货信息：{{ order.receiverName }} / {{ maskPhone(order.receiverPhone) }}
                  </p>
                  <p class="mt-1 line-clamp-2 text-sm text-gray-400">{{ order.receiverAddress }}</p>
                </div>
              </div>
            </div>

            <div class="flex flex-col items-start gap-3 lg:min-w-[220px] lg:items-end">
              <p class="text-2xl font-black text-orange-500">￥{{ order.totalAmount.toFixed(2) }}</p>
              <StatusTag :status="order.orderStatus" />
              <div class="flex items-center gap-3">
                <button
                  v-if="isUnpaid(order)"
                  class="rounded-2xl bg-green-600 px-5 py-3 text-sm font-black text-white transition-all hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-green-300"
                  :disabled="payingOrderId === order.orderId"
                  @click.stop="openPayModal(order)"
                >
                  {{ payingOrderId === order.orderId ? '支付处理中...' : '去支付' }}
                </button>
                <button
                  class="rounded-2xl border border-gray-200 bg-white px-5 py-3 text-sm font-bold text-gray-600 transition-all hover:border-green-600 hover:text-green-600"
                  @click.stop="openOrderDetail(order.orderId)"
                >
                  查看详情
                </button>
                <button
                  v-if="canCancelOrder(order)"
                  class="rounded-2xl border border-red-200 bg-white px-5 py-3 text-sm font-bold text-red-500 transition-all hover:bg-red-50"
                  @click.stop="cancelOrder(order)"
                >
                  取消订单
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { QrCode as QrCodeIcon, ShoppingBag as ShoppingBagIcon } from 'lucide-vue-next'
import { orderApi, type OrderDTO } from '@/api/order'
import { maskPhone } from '@/utils/cn'
import StatusTag from '@/components/ui/StatusTag.vue'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const router = useRouter()
const orders = ref<OrderDTO[]>([])
const loading = ref(true)
const payingOrderId = ref<number | null>(null)
const payModalVisible = ref(false)
const currentPayOrder = ref<OrderDTO | null>(null)
const qrCanvas = ref<HTMLCanvasElement | null>(null)

async function loadOrders() {
  loading.value = true
  try {
    const result = await orderApi.listMy({ pageNum: 1, pageSize: 50 })
    orders.value = result.records || []
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : '订单加载失败', 'error')
    orders.value = []
  } finally {
    loading.value = false
  }
}

function isUnpaid(order: OrderDTO) {
  if (['已取消', '已退款', '已完成', '配送中'].includes(order.orderStatus)) return false
  const unpaidStatuses = ['待支付', '未支付', '待付款']
  return unpaidStatuses.includes(order.orderStatus) || unpaidStatuses.includes(order.payStatus)
}

function canCancelOrder(order: OrderDTO) {
  const cancellableStatuses = ['待支付', '待接单', '待配送']
  return cancellableStatuses.includes(order.orderStatus)
}

function openOrderDetail(orderId: number) {
  router.push(`/orders/${orderId}`)
}

async function openPayModal(order: OrderDTO) {
  currentPayOrder.value = order
  payModalVisible.value = true
  await nextTick()

  if (!qrCanvas.value) return

  const payload = `ORDER_PAY:${order.orderId}:${order.orderNo}:${order.totalAmount}`
  await QRCode.toCanvas(qrCanvas.value, payload, {
    width: 192,
    margin: 2,
    color: {
      dark: '#111827',
      light: '#FFFFFF',
    },
  })
}

function closePayModal() {
  if (payingOrderId.value) return
  payModalVisible.value = false
  currentPayOrder.value = null
}

async function confirmMockPay() {
  if (!currentPayOrder.value) return

  const orderId = currentPayOrder.value.orderId
  payingOrderId.value = orderId
  try {
    await orderApi.mockPay(orderId)
    payModalVisible.value = false
    currentPayOrder.value = null
    await loadOrders()
    await router.push(`/orders/pay-success?orderId=${orderId}`)
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : '支付失败', 'error')
  } finally {
    payingOrderId.value = null
  }
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

async function cancelOrder(order: OrderDTO) {
  try {
    await orderApi.cancel(order.orderId)
    showToast('订单已取消', 'success')
    await loadOrders()
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '取消失败', 'error')
  }
}

onMounted(loadOrders)
</script>
