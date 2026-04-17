<template>
  <div class="max-w-4xl mx-auto px-4 py-12">
    <div class="flex items-center gap-2 text-sm text-gray-400 mb-8">
      <router-link to="/orders" class="hover:text-green-600 transition-colors">我的订单</router-link>
      <ChevronRightIcon class="w-4 h-4" />
      <span class="text-gray-900 font-medium">售后申诉</span>
    </div>

    <div class="mb-8 flex items-end justify-between">
      <div>
        <h1 class="text-4xl font-black text-gray-900 mb-2">售后申诉</h1>
        <p class="text-gray-500">对订单问题进行申诉，处理结果可在申诉记录中查看</p>
      </div>
      <button
        v-if="!showForm"
        @click="openForm"
        class="bg-green-600 text-white px-6 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all"
      >
        新建申诉
      </button>
    </div>

    <!-- Appeal Form Modal -->
    <Teleport to="body">
      <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
        <div class="bg-white rounded-[2rem] shadow-2xl w-full max-w-lg p-8 max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-black text-gray-900">提交售后申诉</h2>
            <button @click="showForm = false" class="p-2 rounded-xl hover:bg-gray-100 text-gray-400">
              <XIcon class="w-5 h-5" />
            </button>
          </div>

          <div class="space-y-5">
            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 block">选择订单</label>
              <select
                v-model="form.orderId"
                class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm font-medium outline-none focus:border-green-500"
              >
                <option value="">请选择订单</option>
                <option v-for="o in eligibleOrders" :key="o.orderId" :value="o.orderId">
                  {{ o.orderNo }} - {{ o.items?.[0]?.foodName || '商品' }} ×{{ o.items?.[0]?.quantity }}
                </option>
              </select>
            </div>

            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 block">申诉类型</label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  v-for="t in reasonTypes"
                  :key="t"
                  @click="form.reasonType = t"
                  :class="['py-3 rounded-xl text-sm font-bold border transition-all', form.reasonType === t ? 'bg-green-600 text-white border-green-600' : 'bg-white text-gray-500 border-gray-200 hover:border-green-400']"
                >
                  {{ t }}
                </button>
              </div>
            </div>

            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 block">详细描述</label>
              <textarea
                v-model="form.reasonDesc"
                rows="4"
                placeholder="请详细描述您遇到的问题..."
                class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm font-medium outline-none focus:border-green-500 resize-none"
              ></textarea>
            </div>

            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 block">凭证图片（选填）</label>
              <ImageUpload v-model="form.evidenceImg" />
            </div>
          </div>

          <div class="flex gap-3 mt-8">
            <button @click="showForm = false" class="flex-1 py-3.5 rounded-xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all">
              取消
            </button>
            <button
              @click="submitAppeal"
              :disabled="submitting"
              class="flex-1 py-3.5 rounded-xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all disabled:bg-gray-300 flex items-center justify-center gap-2"
            >
              <span v-if="submitting" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              {{ submitting ? '提交中...' : '提交申诉' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Appeal Detail Modal -->
    <Teleport to="body">
      <div v-if="detailAppeal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
        <div class="bg-white rounded-[2rem] shadow-2xl w-full max-w-lg p-8">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-black text-gray-900">申诉详情</h2>
            <button @click="detailAppeal = null" class="p-2 rounded-xl hover:bg-gray-100 text-gray-400">
              <XIcon class="w-5 h-5" />
            </button>
          </div>

          <div class="space-y-4">
            <div class="flex justify-between">
              <span class="text-sm text-gray-400">订单号</span>
              <span class="text-sm font-bold text-gray-900">{{ detailAppeal.orderNo || detailAppeal.orderId }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-400">申诉类型</span>
              <span class="text-sm font-bold text-gray-900">{{ detailAppeal.reasonType }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-400">状态</span>
              <StatusTag :status="detailAppeal.handleStatus" />
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-400">申请时间</span>
              <span class="text-sm font-medium text-gray-600">{{ formatTime(detailAppeal.applyTime) }}</span>
            </div>
            <div v-if="detailAppeal.handleTime" class="flex justify-between">
              <span class="text-sm text-gray-400">处理时间</span>
              <span class="text-sm font-medium text-gray-600">{{ formatTime(detailAppeal.handleTime) }}</span>
            </div>
            <div v-if="detailAppeal.reasonDesc" class="pt-4 border-t border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">问题描述</p>
              <p class="text-sm text-gray-600 leading-relaxed">{{ detailAppeal.reasonDesc }}</p>
            </div>
            <div v-if="detailAppeal.handleResult" class="pt-4 border-t border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">处理结果</p>
              <p class="text-sm text-gray-600 leading-relaxed">{{ detailAppeal.handleResult }}</p>
            </div>
          </div>

          <button @click="detailAppeal = null" class="w-full mt-6 py-3.5 rounded-xl bg-gray-100 text-gray-600 font-bold hover:bg-gray-200 transition-all">
            关闭
          </button>
        </div>
      </div>
    </Teleport>

    <!-- Appeal List -->
    <div v-if="loading" class="flex items-center justify-center py-24 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>

    <div v-else-if="appeals.length === 0" class="flex flex-col items-center justify-center py-24 text-gray-400">
      <svg class="w-16 h-16 mb-4 opacity-30" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25zM6.75 12h.008v.008H6.75V12zm0 3h.008v.008H6.75V15zm0 3h.008v.008H6.75V18z" />
      </svg>
      <p class="text-base font-medium mb-4">暂无申诉记录</p>
      <router-link to="/orders" class="text-green-600 font-bold hover:underline">前往订单页面</router-link>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="appeal in appeals"
        :key="appeal.afterSaleId"
        class="bg-white rounded-[1.5rem] border border-gray-100 shadow-sm p-6 flex flex-col sm:flex-row sm:items-center gap-4 cursor-pointer hover:shadow-md transition-all"
        @click="openDetail(appeal)"
      >
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-3 mb-2">
            <StatusTag :status="appeal.handleStatus" />
            <span class="text-xs text-gray-400">{{ formatTime(appeal.applyTime) }}</span>
          </div>
          <p class="font-bold text-gray-900 truncate">{{ appeal.reasonType }}</p>
          <p class="text-sm text-gray-500 mt-1 line-clamp-1">{{ appeal.reasonDesc }}</p>
        </div>
        <div class="text-right">
          <p class="text-xs text-gray-400 mb-1">订单 #{{ appeal.orderId }}</p>
          <ChevronRightIcon class="w-5 h-5 text-gray-300 ml-auto" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { appealApi, type AppealDTO, type AppealCreateDTO } from '@/api/appeal'
import { orderApi, type OrderDTO } from '@/api/order'
import StatusTag from '@/components/ui/StatusTag.vue'
import { ChevronRight as ChevronRightIcon, X as XIcon } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import ImageUpload from '@/components/ui/ImageUpload.vue'

const { show: showToast } = useToast()

const appeals = ref<AppealDTO[]>([])
const loading = ref(true)
const showForm = ref(false)
const submitting = ref(false)
const detailAppeal = ref<AppealDTO | null>(null)
const eligibleOrders = ref<OrderDTO[]>([])

const reasonTypes = ['质量问题', '包装破损', '商品不符', '配送延迟', '其他']

const form = ref<AppealCreateDTO>({
  orderId: 0,
  reasonType: '',
  reasonDesc: '',
  evidenceImg: '',
})

async function loadAppeals() {
  loading.value = true
  try {
    const result = await appealApi.listMy({ pageNum: 1, pageSize: 50 })
    appeals.value = result.records
  } catch {
    appeals.value = []
  } finally {
    loading.value = false
  }
}

async function loadEligibleOrders() {
  try {
    const result = await orderApi.listMy({ pageNum: 1, pageSize: 100 })
    eligibleOrders.value = result.records.filter(o =>
      !['待支付', '已取消', '已退款'].includes(o.orderStatus)
    )
  } catch {
    eligibleOrders.value = []
  }
}

function openForm() {
  form.value = { orderId: 0, reasonType: '', reasonDesc: '', evidenceImg: '' }
  loadEligibleOrders()
  showForm.value = true
}

async function submitAppeal() {
  if (!form.value.orderId) {
    showToast('请选择要申诉的订单', 'warning')
    return
  }
  if (!form.value.reasonType) {
    showToast('请选择申诉类型', 'warning')
    return
  }
  if (!form.value.reasonDesc || form.value.reasonDesc.trim().length < 5) {
    showToast('请详细描述问题（至少5个字）', 'warning')
    return
  }
  submitting.value = true
  try {
    await appealApi.submit({
      orderId: Number(form.value.orderId),
      reasonType: form.value.reasonType,
      reasonDesc: form.value.reasonDesc,
      evidenceImg: form.value.evidenceImg || undefined,
    })
    showToast('申诉提交成功，请等待处理', 'success')
    showForm.value = false
    loadAppeals()
  } catch (e: any) {
    showToast('提交失败：' + (e?.message || '请重试'), 'error')
  } finally {
    submitting.value = false
  }
}

async function openDetail(appeal: AppealDTO) {
  try {
    const full = await appealApi.getById(appeal.afterSaleId)
    detailAppeal.value = full
  } catch {
    detailAppeal.value = appeal
  }
}

function formatTime(time?: string): string {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 19)
}

onMounted(loadAppeals)
</script>
