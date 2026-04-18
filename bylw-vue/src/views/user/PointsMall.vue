<template>
  <div class="max-w-7xl mx-auto px-4 py-12">
    <div class="text-center mb-12">
      <h1 class="text-4xl font-black text-gray-900 mb-4">积分商城</h1>
      <p class="text-gray-500">用积分兑换精美礼品和优惠券</p>
    </div>

    <div class="bg-gradient-to-r from-orange-500 to-amber-500 rounded-[2rem] p-8 mb-8 text-white">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm opacity-80 mb-2">我的积分</p>
          <p class="text-4xl font-black">{{ loadingBalance ? '-' : balance.toLocaleString() }}</p>
        </div>
        <button
          class="bg-white text-orange-600 px-6 py-3 rounded-2xl font-bold hover:bg-gray-100 transition-all cursor-pointer"
          @click="scrollToHistory"
        >
          积分明细
        </button>
      </div>
    </div>

    <!-- Daily Sign-in Card -->
    <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm p-8 mb-8 flex flex-col sm:flex-row items-center justify-between gap-6">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 bg-gradient-to-br from-green-400 to-emerald-500 rounded-2xl flex items-center justify-center shadow-lg shadow-green-500/20">
          <svg class="w-7 h-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <h3 class="text-lg font-black text-gray-900">每日签到</h3>
          <p class="text-sm text-gray-500 mt-0.5">签到即得 <span class="text-orange-500 font-black">10 绿色积分</span>，连续签到更有额外奖励</p>
        </div>
      </div>
      <button
        v-if="!signedIn"
        @click="handleSignIn"
        :disabled="signInLoading"
        class="bg-green-600 hover:bg-green-700 disabled:bg-green-300 text-white px-8 py-3.5 rounded-2xl font-black text-sm transition-all cursor-pointer flex items-center gap-2 shadow-lg shadow-green-600/20"
      >
        <span v-if="signInLoading" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
        <CalendarCheckIcon v-else class="w-5 h-5" />
        {{ signInLoading ? '签到中...' : '立即签到' }}
      </button>
      <div v-else class="flex items-center gap-2 text-green-600 font-black text-sm">
        <CheckCircleIcon class="w-5 h-5" />
        今日已签到
      </div>
    </div>

    <h2 class="text-2xl font-black text-gray-900 mb-8">可兑换礼品</h2>
    <div v-if="loadingGoods" class="flex items-center justify-center py-10 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>
    <div v-else-if="goods.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div
        v-for="(item, idx) in goods"
        :key="item.goodsId"
        class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden hover:shadow-xl transition-all"
      >
        <div class="aspect-square overflow-hidden bg-gray-100">
          <img :src="getGoodsImage(item, idx)" :alt="item.goodsName" class="w-full h-full object-cover" referrerPolicy="no-referrer" @error="handleGoodsImgError(idx)" />
        </div>
        <div class="p-6">
          <h3 class="font-bold text-gray-900 mb-2">{{ item.goodsName }}</h3>
          <div class="flex items-center justify-between">
            <span class="text-lg font-black text-orange-500">{{ item.pointCost }} 积分</span>
            <button
              @click="handleExchange(item)"
              :disabled="exchangeLoading === item.goodsId"
              class="bg-green-600 text-white px-4 py-2 rounded-xl text-sm font-bold hover:bg-green-700 transition-all disabled:bg-green-300 cursor-pointer"
            >
              <span v-if="exchangeLoading === item.goodsId">兑换中...</span>
              <span v-else>立即兑换</span>
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="flex flex-col items-center justify-center py-10 text-gray-400">
      <p>暂无可兑换礼品</p>
    </div>

    <h2 id="points-history" class="text-2xl font-black text-gray-900 mb-8 mt-16">积分记录</h2>
    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <div v-if="loadingHistory" class="flex items-center justify-center py-10 text-gray-400">
        <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
        加载中...
      </div>
      <table v-else-if="history.length > 0" class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">描述</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">积分</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">时间</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="record in history" :key="record.recordId">
            <td class="px-8 py-4 text-sm text-gray-600">{{ record.remark || record.sourceType }}</td>
            <td class="px-8 py-4 text-sm font-bold" :class="record.changeValue >= 0 ? 'text-green-600' : 'text-red-600'">
              {{ record.changeValue >= 0 ? '+' : '' }}{{ record.changeValue }}
            </td>
            <td class="px-8 py-4 text-sm text-gray-400">{{ record.createTime }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="flex flex-col items-center justify-center py-10 text-gray-400">
        <p>暂无积分记录</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { pointsApi, type PointsGoods, type PointsRecord } from '@/api/points'
import { useAuthStore } from '@/stores/auth'
import { CalendarCheck, CheckCircle } from 'lucide-vue-next'
import { getPointsImage } from '@/utils/images'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const CalendarCheckIcon = CalendarCheck
const CheckCircleIcon = CheckCircle

const authStore = useAuthStore()
const balance = ref(0)
const goods = ref<PointsGoods[]>([])
const history = ref<PointsRecord[]>([])
const loadingBalance = ref(true)
const loadingGoods = ref(true)
const loadingHistory = ref(true)
const exchangeLoading = ref<number | null>(null)
const signInLoading = ref(false)
const goodsImgErrorFlags = ref<Record<number, boolean>>({})

const signedIn = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return history.value.some(r =>
    r.sourceType === '每日签到' && r.createTime && r.createTime.slice(0, 10) === today
  )
})

function handleGoodsImgError(idx: number) {
  goodsImgErrorFlags.value[idx] = true
}

function getGoodsImage(item: PointsGoods, idx: number): string {
  if (goodsImgErrorFlags.value[idx]) {
    return getPointsImage()
  }
  return getPointsImage(item.coverImg)
}

async function loadBalance() {
  loadingBalance.value = true
  try {
    const result = await pointsApi.getBalance()
    balance.value = result.balance
    authStore.setPointsBalance(result.balance ?? 0)
  } catch {
    balance.value = 0
    authStore.setPointsBalance(0)
  } finally {
    loadingBalance.value = false
  }
}

async function loadGoods() {
  loadingGoods.value = true
  try {
    const result = await pointsApi.listGoods({ pageNum: 1, pageSize: 20 })
    goods.value = result.records
  } catch {
    goods.value = []
  } finally {
    loadingGoods.value = false
  }
}

async function loadHistory() {
  loadingHistory.value = true
  try {
    history.value = await pointsApi.getHistory()
  } catch {
    history.value = []
  } finally {
    loadingHistory.value = false
  }
}

async function handleSignIn() {
  signInLoading.value = true
  try {
    await pointsApi.signIn()
    showToast('签到成功！获得 10 绿色积分', 'success')
    await Promise.all([loadBalance(), loadHistory()])
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '签到失败，请重试', 'error')
  } finally {
    signInLoading.value = false
  }
}

async function handleExchange(item: PointsGoods) {
  if (item.stockQty <= 0) {
    showToast('库存不足，兑换失败', 'warning')
    return
  }
  if (balance.value < item.pointCost) {
    showToast('积分不足，无法兑换', 'warning')
    return
  }
  exchangeLoading.value = item.goodsId
  try {
    await pointsApi.exchange(item.goodsId)
    showToast('兑换成功！', 'success')
    await Promise.all([loadBalance(), loadGoods(), loadHistory()])
  } catch (e: unknown) {
    showToast('兑换失败：' + (e instanceof Error ? e.message : '请先登录'), 'error')
  } finally {
    exchangeLoading.value = null
  }
}

function scrollToHistory() {
  document.getElementById('points-history')?.scrollIntoView({ behavior: 'smooth' })
}

onMounted(() => {
  loadBalance()
  loadGoods()
  loadHistory()
})
</script>
