<template>
  <div class="space-y-8">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black text-gray-900">售后申诉管理</h1>
        <p class="mt-2 text-sm text-gray-500">集中处理用户对订单、配送和商品状态的申诉请求。</p>
      </div>
      <div class="flex items-center gap-3">
        <select v-model="statusFilter" class="rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-medium text-gray-600 outline-none focus:border-green-500">
          <option value="">全部状态</option>
          <option value="待处理">待处理</option>
          <option value="处理中">处理中</option>
          <option value="已通过">已通过</option>
          <option value="已驳回">已驳回</option>
        </select>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="loadAppeals">
          刷新
        </button>
      </div>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
      <div v-for="item in statCards" :key="item.label" class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6">
        <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">{{ item.label }}</p>
        <div class="mt-3 flex items-end justify-between">
          <h3 class="text-3xl font-black text-gray-900">{{ item.value }}</h3>
          <component :is="item.icon" class="w-6 h-6" :class="item.iconClass" />
        </div>
      </div>
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">申诉单</th>
            <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">用户</th>
            <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">类型</th>
            <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">状态</th>
            <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">提交时间</th>
            <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading">
            <td colspan="6" class="px-6 py-12 text-center text-gray-400">
              <div class="flex items-center justify-center">
                <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                加载中...
              </div>
            </td>
          </tr>
          <tr v-else-if="filteredAppeals.length === 0">
            <td colspan="6" class="px-6 py-12 text-center text-gray-400">暂无申诉数据</td>
          </tr>
          <tr v-else v-for="item in filteredAppeals" :key="item.appealId" class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-5">
              <div class="font-bold text-gray-900">{{ item.orderNo }}</div>
              <p class="text-xs text-gray-400">#{{ item.appealId }}</p>
            </td>
            <td class="px-6 py-5">
              <div class="font-medium text-gray-900">{{ item.userName }}</div>
              <p class="text-xs text-gray-400">用户ID {{ item.userId }}</p>
            </td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ item.type }}</td>
            <td class="px-6 py-5"><StatusTag :status="item.status" /></td>
            <td class="px-6 py-5 text-sm text-gray-500">{{ item.createTime }}</td>
            <td class="px-6 py-5">
              <div class="flex items-center gap-3">
                <button class="text-green-600 font-bold hover:underline" @click="openReview(item)">审核</button>
                <button class="text-gray-500 font-bold hover:underline" @click="openDetail(item)">查看</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="detailVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4">
      <div class="w-full max-w-2xl bg-white rounded-[32px] shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">申诉详情</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="detailVisible = false">
            <XIcon class="w-5 h-5" />
          </button>
        </div>
        <div v-if="currentAppeal" class="space-y-4">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <p><span class="font-bold text-gray-900">订单号：</span>{{ currentAppeal.orderNo }}</p>
            <p><span class="font-bold text-gray-900">类型：</span>{{ currentAppeal.type }}</p>
            <p><span class="font-bold text-gray-900">用户：</span>{{ currentAppeal.userName }}</p>
            <p><span class="font-bold text-gray-900">状态：</span>{{ currentAppeal.status }}</p>
          </div>
          <div>
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">申诉原因</p>
            <p class="text-sm text-gray-600 leading-7">{{ currentAppeal.reason }}</p>
          </div>
          <div>
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">管理员备注</p>
            <textarea v-model="reviewRemark" rows="5" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500 resize-none" />
          </div>
          <div class="flex items-center justify-end gap-3">
            <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="detailVisible = false">关闭</button>
            <button class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all" @click="approveAppeal">通过</button>
            <button class="px-5 py-3 rounded-2xl bg-red-500 text-white font-bold hover:bg-red-600 transition-all" @click="rejectAppeal">驳回</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminApi, type AppealDTO } from '@/api/admin'
import StatusTag from '@/components/ui/StatusTag.vue'
import { AlertCircle, CheckCircle2, Clock3, X, XCircle } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const XIcon = X
const appeals = ref<AppealDTO[]>([])
const loading = ref(true)
const statusFilter = ref('')
const detailVisible = ref(false)
const currentAppeal = ref<AppealDTO | null>(null)
const reviewRemark = ref('')

const filteredAppeals = computed(() => {
  if (!statusFilter.value) return appeals.value
  return appeals.value.filter(item => item.status === statusFilter.value)
})

const statCards = computed(() => {
  const total = appeals.value.length
  const pending = appeals.value.filter(item => item.status === '待处理').length
  const passed = appeals.value.filter(item => item.status === '已通过').length
  const rejected = appeals.value.filter(item => item.status === '已驳回').length
  return [
    { label: '总申诉数', value: total, icon: AlertCircle, iconClass: 'text-blue-600' },
    { label: '待处理', value: pending, icon: Clock3, iconClass: 'text-orange-600' },
    { label: '已通过', value: passed, icon: CheckCircle2, iconClass: 'text-green-600' },
    { label: '已驳回', value: rejected, icon: XCircle, iconClass: 'text-red-600' },
  ]
})

async function loadAppeals() {
  loading.value = true
  try {
    const result = await adminApi.listAppeals({ pageNum: 1, pageSize: 50, status: statusFilter.value || undefined })
    appeals.value = result.records
  } catch {
    appeals.value = []
  } finally {
    loading.value = false
  }
}

function openDetail(item: AppealDTO) {
  currentAppeal.value = item
  reviewRemark.value = item.adminRemark ?? ''
  detailVisible.value = true
}

function openReview(item: AppealDTO) {
  openDetail(item)
}

async function approveAppeal() {
  if (!currentAppeal.value) return
  try {
    await adminApi.updateAppealStatus(currentAppeal.value.appealId, '已通过', reviewRemark.value)
    currentAppeal.value.status = '已通过'
    currentAppeal.value.adminRemark = reviewRemark.value
    detailVisible.value = false
    await loadAppeals()
  } catch (e: unknown) {
    showToast('处理失败：' + (e instanceof Error ? e.message : '请重试'), 'error')
  }
}

async function rejectAppeal() {
  if (!currentAppeal.value) return
  try {
    await adminApi.updateAppealStatus(currentAppeal.value.appealId, '已驳回', reviewRemark.value)
    currentAppeal.value.status = '已驳回'
    currentAppeal.value.adminRemark = reviewRemark.value
    detailVisible.value = false
    await loadAppeals()
  } catch (e: unknown) {
    showToast('处理失败：' + (e instanceof Error ? e.message : '请重试'), 'error')
  }
}

onMounted(loadAppeals)
</script>
