<template>
  <div class="space-y-8">
    <section class="flex flex-col gap-4 rounded-[2rem] bg-gradient-to-r from-emerald-600 via-green-600 to-lime-500 p-8 text-white shadow-xl shadow-green-200">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <p class="text-sm font-bold uppercase tracking-[0.28em] text-white/70">Admin Workspace</p>
          <h1 class="mt-3 text-3xl font-black">管理工作台</h1>
          <p class="mt-3 max-w-2xl text-sm leading-7 text-white/80">
            当前面板已接入后端实时统计，涵盖订单、内容、积分、申诉与运营待办，适合答辩演示和日常巡检。
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <button
            class="inline-flex items-center gap-2 rounded-2xl bg-white/15 px-4 py-3 text-sm font-bold text-white backdrop-blur transition-all hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="loading"
            @click="handleManualRefresh"
          >
            <CalendarIcon class="h-4 w-4" />
            {{ formattedNow }}
          </button>
          <button
            class="inline-flex items-center gap-2 rounded-2xl bg-white px-4 py-3 text-sm font-black text-green-700 shadow-lg transition-all hover:bg-green-50 disabled:cursor-not-allowed disabled:bg-white/70"
            :disabled="exporting"
            @click="exportReport"
          >
            <DownloadIcon class="h-4 w-4" />
            {{ exporting ? '导出中...' : '导出报告' }}
          </button>
        </div>
      </div>
    </section>

    <section class="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-5">
      <article
        v-for="stat in paperStats"
        :key="stat.label"
        class="group relative rounded-[1.75rem] border border-gray-100 bg-white p-6 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md cursor-default"
        :title="stat.tip"
      >
        <div :class="['mb-5 flex h-11 w-11 items-center justify-center rounded-2xl', stat.color]">
          <component :is="stat.icon" class="h-5 w-5" />
        </div>
        <p class="text-xs font-bold uppercase tracking-[0.24em] text-gray-400">{{ stat.label }}</p>
        <div class="mt-2 flex items-end justify-between gap-2">
          <strong class="text-2xl font-black text-gray-900">{{ stat.value }}</strong>
          <span
            :class="[
              'inline-flex items-center gap-1 text-xs font-bold',
              stat.trendUp ? 'text-green-600' : 'text-red-500',
            ]"
          >
            <component :is="stat.trendUp ? ArrowUpRightIcon : ArrowDownRightIcon" class="h-3.5 w-3.5" />
            {{ stat.trend }}
          </span>
        </div>
        <!-- 悬停说明 -->
        <div class="pointer-events-none absolute bottom-full left-1/2 z-10 mb-2 w-56 -translate-x-1/2 rounded-xl border border-gray-100 bg-white px-3 py-2 text-xs text-gray-500 opacity-0 shadow-lg transition-all group-hover:opacity-100">
          {{ stat.tip }}
          <div class="absolute bottom-0 left-1/2 h-2 w-2 -translate-x-1/2 translate-y-1/2 rotate-45 border-b border-r border-gray-100 bg-white"></div>
        </div>
      </article>
    </section>

    <div
      v-if="feedback"
      class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'"
    >
      {{ feedback.message }}
    </div>

    <section class="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-6">
      <article
        v-for="stat in stats"
        :key="stat.label"
        class="rounded-[1.75rem] border border-gray-100 bg-white p-6 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
      >
        <div :class="['mb-5 flex h-11 w-11 items-center justify-center rounded-2xl', stat.color]">
          <component :is="stat.icon" class="h-5 w-5" />
        </div>
        <p class="text-xs font-bold uppercase tracking-[0.24em] text-gray-400">{{ stat.label }}</p>
        <div class="mt-3 flex items-end justify-between gap-3">
          <strong class="text-3xl font-black text-gray-900">{{ stat.value }}</strong>
          <span
            :class="[
              'inline-flex items-center gap-1 text-xs font-bold',
              stat.up ? 'text-green-600' : 'text-red-500',
            ]"
          >
            <component :is="stat.up ? ArrowUpRightIcon : ArrowDownRightIcon" class="h-3.5 w-3.5" />
            {{ stat.trend }}
          </span>
        </div>
      </article>
    </section>

    <section class="grid grid-cols-1 gap-8 xl:grid-cols-[1.35fr_1fr]">
      <article class="rounded-[2rem] border border-gray-100 bg-white p-7 shadow-sm">
        <div class="mb-6 flex items-center justify-between gap-4">
          <div>
            <h2 class="text-xl font-black text-gray-900">近 7 日运营趋势</h2>
            <p class="mt-2 text-sm text-gray-500">绿色表示运营中食品，蓝色表示订单量。</p>
          </div>
        </div>
        <div class="h-[320px]">
          <VChart :option="areaChartOption" autoresize />
        </div>
      </article>

      <article class="rounded-[2rem] border border-gray-100 bg-white p-7 shadow-sm">
        <div class="mb-6">
          <h2 class="text-xl font-black text-gray-900">申诉类型分布</h2>
          <p class="mt-2 text-sm text-gray-500">以下数据来自当前后端售后统计。</p>
        </div>

        <div class="grid gap-6 lg:grid-cols-[1fr_220px] xl:grid-cols-1">
          <div class="h-[300px]">
            <VChart :option="pieChartOption" autoresize />
          </div>

          <div class="space-y-3">
            <div
              v-for="(item, index) in pieData"
              :key="item.name"
              class="flex items-center justify-between rounded-2xl bg-gray-50 px-4 py-3"
            >
              <div class="flex items-center gap-3">
                <span class="h-3 w-3 rounded-full" :style="{ backgroundColor: COLORS[index] || COLORS[COLORS.length - 1] }"></span>
                <span class="text-sm font-medium text-gray-700">{{ item.name }}</span>
              </div>
              <span class="text-sm font-black text-gray-900">{{ item.percent }}</span>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="grid grid-cols-1 gap-8 xl:grid-cols-[1.4fr_1fr]">
      <article class="overflow-hidden rounded-[2rem] border border-gray-100 bg-white shadow-sm">
        <div class="flex items-center justify-between border-b border-gray-100 px-7 py-6">
          <div>
            <h2 class="text-xl font-black text-gray-900">最近订单</h2>
            <p class="mt-2 text-sm text-gray-500">最新 5 条订单，便于管理员快速巡检。</p>
          </div>
          <router-link to="/admin/orders" class="text-sm font-bold text-green-600 transition-colors hover:text-green-700">
            查看全部
          </router-link>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-full text-left">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-7 py-4 text-xs font-bold uppercase tracking-[0.24em] text-gray-400">订单号</th>
                <th class="px-7 py-4 text-xs font-bold uppercase tracking-[0.24em] text-gray-400">收货人</th>
                <th class="px-7 py-4 text-xs font-bold uppercase tracking-[0.24em] text-gray-400">金额</th>
                <th class="px-7 py-4 text-xs font-bold uppercase tracking-[0.24em] text-gray-400">状态</th>
                <th class="px-7 py-4 text-xs font-bold uppercase tracking-[0.24em] text-gray-400">下单时间</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-if="loading">
                <td colspan="5" class="px-7 py-12 text-center text-sm text-gray-400">
                  <div class="inline-flex items-center gap-3">
                    <div class="h-5 w-5 animate-spin rounded-full border-2 border-green-600 border-t-transparent"></div>
                    工作台数据加载中...
                  </div>
                </td>
              </tr>
              <tr v-else-if="recentOrders.length === 0">
                <td colspan="5" class="px-7 py-12 text-center text-sm text-gray-400">暂无订单数据</td>
              </tr>
              <tr v-for="order in recentOrders" :key="order.orderId" class="transition-colors hover:bg-gray-50/70">
                <td class="px-7 py-4 text-sm font-bold text-gray-900">{{ order.orderNo }}</td>
                <td class="px-7 py-4 text-sm text-gray-600">{{ order.receiverName || order.username || '-' }}</td>
                <td class="px-7 py-4 text-sm font-black text-orange-500">￥{{ order.totalAmount.toFixed(2) }}</td>
                <td class="px-7 py-4">
                  <StatusTag :status="order.orderStatus" />
                </td>
                <td class="px-7 py-4 text-sm text-gray-400">{{ formatDisplayTime(order.createTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="rounded-[2rem] border border-gray-100 bg-white p-7 shadow-sm">
        <div class="mb-6">
          <h2 class="text-xl font-black text-gray-900">待办提醒</h2>
          <p class="mt-2 text-sm text-gray-500">点击卡片可直达对应后台页面。</p>
        </div>

        <div class="space-y-4">
          <button
            v-for="todo in todoCards"
            :key="todo.label"
            class="flex w-full items-center justify-between rounded-2xl border border-gray-100 bg-gray-50 px-5 py-4 text-left transition-all hover:border-green-200 hover:bg-green-50/40"
            @click="router.push(todo.path)"
          >
            <div class="flex items-center gap-4">
              <div :class="['flex h-11 w-11 items-center justify-center rounded-2xl', todo.color]">
                <component :is="todo.icon" class="h-5 w-5" />
              </div>
              <div>
                <p class="text-sm font-bold text-gray-900">{{ todo.label }}</p>
                <p class="mt-1 text-xs text-gray-500">当前共有 {{ todo.count }} 条待处理</p>
              </div>
            </div>
            <ChevronRightIcon class="h-4 w-4 text-gray-400" />
          </button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { recommendApi, type AdminStatsDTO } from '@/api/recommend'
import { orderApi, type OrderDTO } from '@/api/order'
import StatusTag from '@/components/ui/StatusTag.vue'
import {
  AlertCircle,
  ArrowDownRight as ArrowDownRightIcon,
  ArrowUpRight as ArrowUpRightIcon,
  Calendar as CalendarIcon,
  ChevronRight as ChevronRightIcon,
  ClipboardList,
  Download as DownloadIcon,
  Gift,
  Leaf,
  MessageSquare,
  Scale,
  ShoppingBag,
  Store,
  TrendingUp,
  Users,
} from 'lucide-vue-next'

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

interface TrendPoint {
  name: string
  food: number
  orders: number
}

interface PiePoint {
  name: string
  value: number
  percent: string
}

interface FeedbackState {
  type: 'success' | 'error'
  message: string
}

interface StatCard {
  label: string
  value: number
  icon: typeof ShoppingBag
  color: string
  trend: string
  up: boolean
}

const router = useRouter()
const loading = ref(true)
const exporting = ref(false)
const feedback = ref<FeedbackState | null>(null)
const now = ref(new Date())
const recentOrders = ref<OrderDTO[]>([])
const trendData = ref<TrendPoint[]>(defaultTrendData())
const pieData = ref<PiePoint[]>(defaultPieData())
const stats = ref<StatCard[]>(createInitialStats())
const paperStatsData = ref({
  foodSavedKg: 0,
  totalPointsEarned: 0,
  avgOrderValue: 0,
  completedOrders: 0,
  wasteIndex: 0,
  foodSavedTrendRate: 0,
  wasteIndexTrendRate: 0,
  pointsTrendRate: 0,
})
const todoCounts = ref({
  inactiveStores: 0,
  pendingAppeals: 0,
  lowStockFoods: 0,
  pendingPosts: 0,
})

const COLORS = ['#10B981', '#3B82F6', '#F59E0B', '#EF4444', '#8B5CF6']
let timerId: ReturnType<typeof setInterval> | null = null

const formattedNow = computed(() => formatDateTime(now.value))
const summaryDate = computed(() => formatDate(now.value))

const todoCards = computed(() => [
  { label: '闲置门店', count: todoCounts.value.inactiveStores, icon: Store, color: 'bg-orange-50 text-orange-600', path: '/admin/foods' },
  { label: '待处理申诉', count: todoCounts.value.pendingAppeals, icon: AlertCircle, color: 'bg-red-50 text-red-600', path: '/admin/appeals' },
  { label: '库存预警食品', count: todoCounts.value.lowStockFoods, icon: ShoppingBag, color: 'bg-yellow-50 text-yellow-600', path: '/admin/foods' },
  { label: '待审核帖子', count: todoCounts.value.pendingPosts, icon: MessageSquare, color: 'bg-blue-50 text-blue-600', path: '/admin/community' },
])

const paperStats = computed(() => [
  {
    label: '本周食品节约量',
    value: `${paperStatsData.value.foodSavedKg.toFixed(1)} kg`,
    icon: Leaf,
    color: 'bg-emerald-50 text-emerald-600',
    trend: `${paperStatsData.value.foodSavedTrendRate >= 0 ? '+' : ''}${paperStatsData.value.foodSavedTrendRate.toFixed(1)}%`,
    trendUp: paperStatsData.value.foodSavedTrendRate >= 0,
    tip: '本周用户购买临期食品节省的总量，较上周变化',
  },
  {
    label: '本周积分产出',
    value: paperStatsData.value.totalPointsEarned.toLocaleString(),
    icon: TrendingUp,
    color: 'bg-cyan-50 text-cyan-600',
    trend: `${paperStatsData.value.pointsTrendRate >= 0 ? '+' : ''}${paperStatsData.value.pointsTrendRate.toFixed(1)}%`,
    trendUp: paperStatsData.value.pointsTrendRate >= 0,
    tip: '本周签到、订单环保奖励等途径产出的积分总量',
  },
  {
    label: '本周浪费指数',
    value: `${paperStatsData.value.wasteIndex.toFixed(1)}%`,
    icon: AlertCircle,
    color: 'bg-red-50 text-red-600',
    trend: `${paperStatsData.value.wasteIndexTrendRate >= 0 ? '+' : ''}${paperStatsData.value.wasteIndexTrendRate.toFixed(1)}%`,
    trendUp: paperStatsData.value.wasteIndexTrendRate <= 0,
    tip: '取消+退款订单占总完成订单比例，负数表示改善',
  },
  {
    label: '本周完成订单',
    value: paperStatsData.value.completedOrders.toLocaleString(),
    icon: ClipboardList,
    color: 'bg-violet-50 text-violet-600',
    trend: '本周',
    trendUp: true,
    tip: '本周状态变为"已完成"的订单数量',
  },
  {
    label: '本周平均订单额',
    value: `￥${paperStatsData.value.avgOrderValue.toFixed(2)}`,
    icon: Scale,
    color: 'bg-amber-50 text-amber-600',
    trend: '本周',
    trendUp: true,
    tip: '本周已完成订单的平均金额',
  },
])

const areaChartOption = computed(() => ({
  color: ['#10B981', '#3B82F6'],
  grid: { left: 24, right: 16, top: 20, bottom: 24, containLabel: true },
  tooltip: {
    trigger: 'axis',
    backgroundColor: '#111827',
    borderWidth: 0,
    textStyle: { color: '#F9FAFB' },
  },
  legend: {
    top: 0,
    right: 0,
    textStyle: { color: '#6B7280' },
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendData.value.map((item) => item.name),
    axisLine: { lineStyle: { color: '#E5E7EB' } },
    axisTick: { show: false },
    axisLabel: { color: '#9CA3AF' },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#9CA3AF' },
    splitLine: { lineStyle: { color: '#F3F4F6' } },
  },
  series: [
    {
      name: '运营中食品',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: trendData.value.map((item) => item.food),
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(16, 185, 129, 0.22)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.02)' },
          ],
        },
      },
    },
    {
      name: '订单量',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: trendData.value.map((item) => item.orders),
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(59, 130, 246, 0.2)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.02)' },
          ],
        },
      },
    },
  ],
}))

const pieChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['42%', '72%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 14,
        borderColor: '#FFFFFF',
        borderWidth: 3,
      },
      label: { show: false },
      data: pieData.value.map((item, index) => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: COLORS[index] || COLORS[COLORS.length - 1] },
      })),
    },
  ],
}))

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
}

async function loadDashboard(showSuccessMessage = false) {
  loading.value = true
  try {
    const [statsData, ordersPage] = await Promise.all([
      recommendApi.getAdminStats(),
      orderApi.listAll({ pageNum: 1, pageSize: 5 }),
    ])

    stats.value = buildStats(statsData)
    trendData.value = statsData.trendData?.length ? statsData.trendData : defaultTrendData()
    pieData.value = buildPieData(statsData.categoryData)
    recentOrders.value = ordersPage.records || []
    todoCounts.value = {
      inactiveStores: statsData.inactiveStoreCount ?? 0,
      pendingAppeals: statsData.appealCount ?? 0,
      lowStockFoods: statsData.lowStockFoodCount ?? 0,
      pendingPosts: statsData.pendingPostCount ?? 0,
    }
    paperStatsData.value = {
      foodSavedKg: statsData.foodSavedKg ?? 0,
      totalPointsEarned: statsData.totalPointsEarned ?? 0,
      avgOrderValue: statsData.avgOrderValue ?? 0,
      completedOrders: statsData.completedOrders ?? 0,
      wasteIndex: statsData.wasteIndex ?? 0,
      foodSavedTrendRate: statsData.foodSavedTrendRate ?? 0,
      wasteIndexTrendRate: statsData.wasteIndexTrendRate ?? 0,
      pointsTrendRate: statsData.pointsTrendRate ?? 0,
    }

    if (showSuccessMessage) {
      setFeedback('success', `工作台已刷新，统计时间：${formattedNow.value}`)
    }
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '工作台数据加载失败')
  } finally {
    loading.value = false
  }
}

async function handleManualRefresh() {
  now.value = new Date()
  await loadDashboard(true)
}

async function exportReport() {
  exporting.value = true
  try {
    const [statsData, ordersPage] = await Promise.all([
      recommendApi.getAdminStats(),
      orderApi.listAll({ pageNum: 1, pageSize: 50 }),
    ])

    const BOM = '\uFEFF'
    const rows: string[] = []

    rows.push('类别,指标,数值')
    rows.push(`核心指标,运营中食品,${statsData.foodCount}`)
    rows.push(`核心指标,订单总数,${statsData.orderCount}`)
    rows.push(`核心指标,用户总数,${statsData.userCount}`)
    rows.push(`核心指标,待处理申诉,${statsData.appealCount}`)
    rows.push(`核心指标,社区帖子,${statsData.postCount}`)
    rows.push(`核心指标,积分兑换,${statsData.pointsExchange}`)
    rows.push(`论文指标,食品节约量(kg),${(statsData.foodSavedKg ?? 0).toFixed(1)}`)
    rows.push(`论文指标,浪费指数(%),${(statsData.wasteIndex ?? 0).toFixed(1)}`)
    rows.push(`论文指标,积分总产出,${statsData.totalPointsEarned ?? 0}`)
    rows.push(`论文指标,已完成订单,${statsData.completedOrders ?? 0}`)
    rows.push(`论文指标,平均订单额,${(statsData.avgOrderValue ?? 0).toFixed(2)}`)
    rows.push(`待办,库存预警食品,${statsData.lowStockFoodCount ?? 0}`)
    rows.push(`待办,待审核帖子,${statsData.pendingPostCount ?? 0}`)
    rows.push('')
    rows.push('订单号,收货人,金额,状态,下单时间')
    for (const item of ordersPage.records) {
      rows.push(`${item.orderNo},${item.receiverName ?? '-'},${item.totalAmount.toFixed(2)},${item.orderStatus},${formatDisplayTime(item.createTime)}`)
    }

    downloadTextFile(`运营报告-${formatDate(now.value)}.csv`, BOM + rows.join('\n'))
    setFeedback('success', 'CSV报告已导出，可用 Excel 打开。')
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '报告导出失败')
  } finally {
    exporting.value = false
  }
}

function downloadTextFile(filename: string, content: string) {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

function createInitialStats(): StatCard[] {
  return [
    { label: '运营食品', value: 0, icon: ShoppingBag, color: 'bg-green-50 text-green-600', trend: '+0.0%', up: true },
    { label: '订单总数', value: 0, icon: ClipboardList, color: 'bg-blue-50 text-blue-600', trend: '+0.0%', up: true },
    { label: '用户总数', value: 0, icon: Users, color: 'bg-purple-50 text-purple-600', trend: '+0.0%', up: true },
    { label: '售后申诉', value: 0, icon: AlertCircle, color: 'bg-red-50 text-red-600', trend: '+0.0%', up: true },
    { label: '社区帖子', value: 0, icon: MessageSquare, color: 'bg-orange-50 text-orange-600', trend: '+0.0%', up: true },
    { label: '积分兑换', value: 0, icon: Gift, color: 'bg-pink-50 text-pink-600', trend: '+0.0%', up: true },
  ]
}

function buildStats(statsData: AdminStatsDTO): StatCard[] {
  return [
    { label: '运营食品', value: statsData.foodCount, icon: ShoppingBag, color: 'bg-green-50 text-green-600', ...buildTrendState(statsData.foodTrendRate) },
    { label: '订单总数', value: statsData.orderCount, icon: ClipboardList, color: 'bg-blue-50 text-blue-600', ...buildTrendState(statsData.orderTrendRate) },
    { label: '用户总数', value: statsData.userCount, icon: Users, color: 'bg-purple-50 text-purple-600', ...buildTrendState(statsData.userTrendRate) },
    { label: '售后申诉', value: statsData.appealCount, icon: AlertCircle, color: 'bg-red-50 text-red-600', ...buildTrendState(statsData.appealTrendRate) },
    { label: '社区帖子', value: statsData.postCount, icon: MessageSquare, color: 'bg-orange-50 text-orange-600', ...buildTrendState(statsData.postTrendRate) },
    { label: '积分兑换', value: statsData.pointsExchange, icon: Gift, color: 'bg-pink-50 text-pink-600', ...buildTrendState(statsData.pointsExchangeTrendRate) },
  ]
}

function buildTrendState(rate?: number) {
  const value = Number.isFinite(rate) ? Number(rate) : 0
  return {
    trend: `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`,
    up: value >= 0,
  }
}

function buildPieData(source?: Array<{ name: string; value: number }>) {
  const baseData = source?.length ? source : defaultPieData().map((item) => ({ name: item.name, value: item.value }))
  const total = baseData.reduce((sum, item) => sum + item.value, 0) || 1
  return baseData.map((item) => ({
    name: item.name,
    value: item.value,
    percent: `${((item.value / total) * 100).toFixed(1)}%`,
  }))
}

function defaultTrendData(): TrendPoint[] {
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(now.value)
    date.setDate(date.getDate() - (6 - index))
    return {
      name: formatMonthDay(date),
      food: 0,
      orders: 0,
    }
  })
}

function defaultPieData(): PiePoint[] {
  return [
    { name: '质量问题', value: 0, percent: '0.0%' },
    { name: '配送延迟', value: 0, percent: '0.0%' },
    { name: '包装破损', value: 0, percent: '0.0%' },
    { name: '其他', value: 0, percent: '0.0%' },
  ]
}

function formatMonthDay(date: Date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDateTime(date: Date) {
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${formatDate(date)} ${hours}:${minutes}:${seconds}`
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(async () => {
  await loadDashboard()
  timerId = setInterval(() => {
    now.value = new Date()
  }, 60000)
})

onUnmounted(() => {
  if (timerId) {
    clearInterval(timerId)
  }
})
</script>
