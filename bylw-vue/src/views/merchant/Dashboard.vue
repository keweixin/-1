<template>
  <div class="space-y-6">
    <!-- Header Banner -->
    <div class="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-6 text-white shadow-lg shadow-blue-600/10">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-black">商户工作台</h1>
          <p class="text-blue-200 text-sm mt-1">管理您的临期食品库存和订单</p>
        </div>
        <div class="text-right">
          <p class="text-lg font-black">{{ merchantName }}</p>
          <p class="text-xs text-blue-200">商户 ID: {{ merchantId }}</p>
        </div>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer group">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-10 h-10 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center group-hover:bg-blue-100 transition-colors">
            <ShoppingBagIcon class="w-5 h-5" />
          </div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">上架食品</p>
        </div>
        <div class="flex items-baseline gap-2">
          <p class="text-3xl font-black text-gray-900">{{ stats.totalFoods }}</p>
          <span class="text-xs text-green-600 font-bold flex items-center gap-1">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"/>
            </svg>
          </span>
        </div>
        <p class="text-xs text-gray-400 mt-1">当前在售：{{ stats.activeFoods }} 件</p>
      </div>
      <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer group">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-10 h-10 bg-green-50 text-green-600 rounded-xl flex items-center justify-center group-hover:bg-green-100 transition-colors">
            <ClipboardListIcon class="w-5 h-5" />
          </div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">相关订单</p>
        </div>
        <p class="text-3xl font-black text-gray-900">{{ recentOrders.length }}</p>
        <router-link to="/merchant/orders" class="text-xs font-bold text-blue-600 hover:underline mt-1 inline-block">查看全部订单</router-link>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-2 gap-3">
      <router-link to="/merchant/foods" class="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl p-4 border border-blue-100 hover:shadow-md transition-all group">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform">
            <ShoppingBagIcon class="w-5 h-5 text-white" />
          </div>
          <div>
            <p class="text-sm font-bold text-gray-900">食品管理</p>
            <p class="text-xs text-gray-500">新增/编辑食品</p>
          </div>
        </div>
      </router-link>
      <router-link to="/merchant/orders" class="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl p-4 border border-green-100 hover:shadow-md transition-all group">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-green-600 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform">
            <ClipboardListIcon class="w-5 h-5 text-white" />
          </div>
          <div>
            <p class="text-sm font-bold text-gray-900">订单管理</p>
            <p class="text-xs text-gray-500">处理待接单</p>
          </div>
        </div>
      </router-link>
    </div>

    <!-- Merchant Info -->
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm">
      <div class="p-5 border-b border-gray-50">
        <h2 class="text-base font-black text-gray-900">商户信息</h2>
      </div>
      <div v-if="loading" class="p-8 text-center text-gray-400 text-sm">加载中...</div>
      <div v-else class="p-5 grid grid-cols-2 sm:grid-cols-3 gap-6">
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">用户名</p>
          <p class="text-sm font-bold text-gray-900">{{ merchantInfo.username || '-' }}</p>
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">昵称</p>
          <p class="text-sm font-bold text-gray-900">{{ merchantInfo.nickname || '-' }}</p>
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">手机号</p>
          <p class="text-sm font-bold text-gray-900">{{ merchantInfo.phone || '-' }}</p>
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">邮箱</p>
          <p class="text-sm font-bold text-gray-900">{{ merchantInfo.email || '-' }}</p>
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">地址</p>
          <p class="text-sm font-bold text-gray-900">{{ merchantInfo.address || '-' }}</p>
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">注册时间</p>
          <p class="text-sm font-bold text-gray-900">{{ (merchantInfo.createTime || '').replace('T', ' ').slice(0, 10) }}</p>
        </div>
      </div>
    </div>

    <!-- Recent Orders -->
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm">
      <div class="p-5 border-b border-gray-50">
        <h2 class="text-base font-black text-gray-900">最近订单</h2>
      </div>
      <div v-if="loading" class="p-8 text-center text-gray-400 text-sm">加载中...</div>
      <div v-else-if="recentOrders.length === 0" class="p-12 text-center text-gray-400 text-sm">暂无订单</div>
      <div v-else class="overflow-x-auto">
        <table class="min-w-full text-left text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-5 py-3 font-bold text-gray-400 text-xs uppercase">订单号</th>
              <th class="px-5 py-3 font-bold text-gray-400 text-xs uppercase">金额</th>
              <th class="px-5 py-3 font-bold text-gray-400 text-xs uppercase">状态</th>
              <th class="px-5 py-3 font-bold text-gray-400 text-xs uppercase">时间</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="order in recentOrders" :key="order.orderId" class="hover:bg-gray-50">
              <td class="px-5 py-3 font-bold text-gray-900">{{ order.orderNo }}</td>
              <td class="px-5 py-3 text-orange-500 font-bold">&yen;{{ order.totalAmount?.toFixed(2) }}</td>
              <td class="px-5 py-3">
                <span :class="['px-2 py-1 rounded-lg text-xs font-bold', statusClass(order.orderStatus)]">
                  {{ order.orderStatus }}
                </span>
              </td>
              <td class="px-5 py-3 text-gray-400">{{ (order.createTime || '').replace('T', ' ').slice(0, 16) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { merchantApi, type MerchantStats } from '@/api/merchant'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import type { OrderDTO } from '@/api/order'
import { ShoppingBag as ShoppingBagIcon, ClipboardList as ClipboardListIcon } from 'lucide-vue-next'

const authStore = useAuthStore()
const loading = ref(true)
const stats = ref<MerchantStats>({ totalFoods: 0, activeFoods: 0, merchantId: 0 })
const recentOrders = ref<OrderDTO[]>([])
const merchantInfo = ref<Record<string, string>>({})

const merchantName = computed(() => merchantInfo.value.nickname || merchantInfo.value.username || localStorage.getItem('username') || '商户')
const merchantId = computed(() => authStore.userId || '-')

function statusClass(status: string): string {
  if (status === '已完成') return 'bg-green-50 text-green-600'
  if (status === '已取消' || status === '已退款') return 'bg-red-50 text-red-600'
  if (status === '配送中') return 'bg-blue-50 text-blue-600'
  return 'bg-yellow-50 text-yellow-600'
}

onMounted(async () => {
  try {
    const [statsData, ordersData, info] = await Promise.all([
      merchantApi.getStats(),
      merchantApi.getOrders({ pageNum: 1, pageSize: 5 }),
      authApi.getUserInfo(),
    ])
    stats.value = statsData
    recentOrders.value = ordersData.records || []
    merchantInfo.value = info as any
  } catch {
    // silently handle
  } finally {
    loading.value = false
  }
})
</script>
