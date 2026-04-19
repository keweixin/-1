<template>
  <div class="space-y-8">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-black text-gray-900">积分管理</h1>
      <div class="flex rounded-2xl border border-gray-200 bg-gray-50 p-1">
        <button @click="activeTab = 'records'" :class="['px-4 py-2 rounded-xl text-sm font-bold transition-all', activeTab === 'records' ? 'bg-white text-green-600 shadow-sm' : 'text-gray-500']">积分记录</button>
        <button @click="activeTab = 'goods'" :class="['px-4 py-2 rounded-xl text-sm font-bold transition-all', activeTab === 'goods' ? 'bg-white text-green-600 shadow-sm' : 'text-gray-500']">商品管理</button>
      </div>
    </div>

    <div v-if="feedback" class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'">
      {{ feedback.message }}
    </div>

    <!-- Records Tab -->
    <template v-if="activeTab === 'records'">
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <div class="bg-gradient-to-br from-orange-500 to-amber-500 rounded-3xl p-8 text-white">
          <p class="text-sm opacity-80 mb-2">今日发放</p>
          <p class="text-3xl font-black">+{{ todayEarned.toLocaleString() }}</p>
        </div>
        <div class="bg-gradient-to-br from-green-600 to-emerald-600 rounded-3xl p-8 text-white">
          <p class="text-sm opacity-80 mb-2">今日兑换</p>
          <p class="text-3xl font-black">{{ todaySpent.toLocaleString() }}</p>
        </div>
        <div class="bg-gradient-to-br from-purple-600 to-pink-600 rounded-3xl p-8 text-white">
          <p class="text-sm opacity-80 mb-2">用户总数</p>
          <p class="text-3xl font-black">{{ totalUsers.toLocaleString() }}</p>
        </div>
      </div>

      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50">
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">用户</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">类型</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">积分</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">时间</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-if="loading">
              <td colspan="4" class="px-8 py-12 text-center text-gray-400">
                <div class="flex items-center justify-center">
                  <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                  加载中...
                </div>
              </td>
            </tr>
            <tr v-else-if="history.length === 0">
              <td colspan="4" class="px-8 py-12 text-center text-gray-400">暂无记录</td>
            </tr>
            <tr v-else v-for="record in history" :key="record.recordId" class="hover:bg-gray-50 transition-colors">
              <td class="px-8 py-4 text-sm text-gray-900">用户{{ record.userId }}</td>
              <td class="px-8 py-4 text-sm text-gray-600">{{ record.remark || record.sourceType }}</td>
              <td class="px-8 py-4 text-sm font-bold" :class="record.changeValue >= 0 ? 'text-green-600' : 'text-red-600'">
                {{ record.changeValue >= 0 ? '+' : '' }}{{ record.changeValue }}
              </td>
              <td class="px-8 py-4 text-sm text-gray-400">{{ record.createTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- Goods Tab -->
    <template v-if="activeTab === 'goods'">
      <div class="flex justify-end">
        <button class="bg-green-600 text-white px-6 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all shadow-lg shadow-green-100" @click="openCreateGoodsModal">添加商品</button>
      </div>

      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50">
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">商品名称</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">积分</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">库存</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">状态</th>
              <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-if="goodsLoading">
              <td colspan="5" class="px-8 py-12 text-center text-gray-400">
                <div class="flex items-center justify-center">
                  <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                  加载中...
                </div>
              </td>
            </tr>
            <tr v-else-if="goods.length === 0">
              <td colspan="5" class="px-8 py-12 text-center text-gray-400">暂无商品</td>
            </tr>
            <tr v-else v-for="item in goods" :key="item.goodsId" class="hover:bg-gray-50 transition-colors">
              <td class="px-8 py-6 text-sm font-medium text-gray-900">{{ item.goodsName }}</td>
              <td class="px-8 py-6 text-sm font-bold text-orange-500">{{ item.pointCost }}</td>
              <td class="px-8 py-6 text-sm text-gray-600">{{ item.stockQty }}</td>
              <td class="px-8 py-6">
                <StatusTag :status="item.status === 1 ? '上架' : '下架'" />
              </td>
              <td class="px-8 py-6">
                <div class="flex items-center gap-3">
                  <button class="text-green-600 hover:underline text-sm font-medium" @click="openEditGoodsModal(item)">编辑</button>
                  <button class="text-red-600 hover:underline text-sm font-medium" @click="handleDeleteGoods(item.goodsId)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Goods Modal -->
      <div v-if="goodsModalVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4" @click.self="goodsModalVisible = false">
        <div class="w-full max-w-lg bg-white rounded-[32px] shadow-2xl p-8">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-black text-gray-900">{{ goodsForm.goodsId ? '编辑商品' : '添加商品' }}</h3>
            <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="goodsModalVisible = false">
              <XIcon class="w-5 h-5" />
            </button>
          </div>
          <div class="space-y-4">
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">商品名称</span>
              <input v-model="goodsForm.goodsName" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
            </label>
            <div class="grid grid-cols-2 gap-4">
              <label class="space-y-2 block">
                <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">所需积分</span>
                <input v-model.number="goodsForm.pointCost" type="number" min="1" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
              </label>
              <label class="space-y-2 block">
                <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">库存</span>
                <input v-model.number="goodsForm.stockQty" type="number" min="0" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
              </label>
            </div>
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">状态</span>
              <select v-model.number="goodsForm.status" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500">
                <option :value="1">上架</option>
                <option :value="0">下架</option>
              </select>
            </label>
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">描述</span>
              <textarea v-model="goodsForm.description" rows="3" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500 resize-none" />
            </label>
          </div>
          <div class="flex items-center justify-end gap-3 mt-8">
            <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="goodsModalVisible = false">取消</button>
            <button class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all disabled:bg-green-300" :disabled="goodsSubmitting" @click="submitGoods">
              {{ goodsSubmitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { api } from '@/api/index'
import { userApi } from '@/api/user'
import { pointsApi, type PointsRecord, type PointsGoods } from '@/api/points'
import StatusTag from '@/components/ui/StatusTag.vue'
import { X } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const XIcon = X
const { show: showToast } = useToast()

interface FeedbackState { type: 'success' | 'error'; message: string }

const activeTab = ref<'records' | 'goods'>('records')
const feedback = ref<FeedbackState | null>(null)

// Records
const history = ref<PointsRecord[]>([])
const totalUsers = ref(0)
const loading = ref(true)

const todayEarned = computed(() => {
  const today = new Date().toDateString()
  return history.value
    .filter(r => new Date(r.createTime).toDateString() === today && r.changeValue > 0)
    .reduce((sum, r) => sum + r.changeValue, 0)
})

const todaySpent = computed(() => {
  const today = new Date().toDateString()
  return history.value
    .filter(r => new Date(r.createTime).toDateString() === today && r.changeValue < 0)
    .reduce((sum, r) => sum + r.changeValue, 0)
})

async function loadHistory() {
  loading.value = true
  try {
    const [historyData, userCount] = await Promise.all([
      api.get<PointsRecord[]>('/points/history/all'),
      userApi.count(),
    ])
    history.value = historyData
    totalUsers.value = userCount ?? 0
  } catch (e: unknown) {
    history.value = []
    showToast('加载失败：' + (e instanceof Error ? e.message : '请检查网络连接'), 'error')
  } finally {
    loading.value = false
  }
}

// Goods
const goods = ref<PointsGoods[]>([])
const goodsLoading = ref(false)
const goodsModalVisible = ref(false)
const goodsSubmitting = ref(false)
const goodsForm = reactive({ goodsId: null as number | null, goodsName: '', pointCost: 100, stockQty: 10, description: '', status: 1 })

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
  setTimeout(() => { feedback.value = null }, 3000)
}

async function loadGoods() {
  goodsLoading.value = true
  try {
    const result = await pointsApi.listGoods({ pageNum: 1, pageSize: 100 })
    goods.value = result.records
  } catch (e: unknown) {
    goods.value = []
    setFeedback('error', e instanceof Error ? e.message : '加载商品失败')
  } finally {
    goodsLoading.value = false
  }
}

function openCreateGoodsModal() {
  Object.assign(goodsForm, { goodsId: null, goodsName: '', pointCost: 100, stockQty: 10, description: '', status: 1 })
  goodsModalVisible.value = true
}

function openEditGoodsModal(item: PointsGoods) {
  Object.assign(goodsForm, {
    goodsId: item.goodsId,
    goodsName: item.goodsName || '',
    pointCost: item.pointCost ?? 100,
    stockQty: item.stockQty ?? 0,
    description: item.description || '',
    status: item.status ?? 1,
  })
  goodsModalVisible.value = true
}

async function submitGoods() {
  if (!goodsForm.goodsName.trim()) {
    setFeedback('error', '商品名称不能为空')
    return
  }
  goodsSubmitting.value = true
  try {
    if (goodsForm.goodsId) {
      await pointsApi.updateGoods(goodsForm)
      setFeedback('success', '商品已更新')
    } else {
      await pointsApi.saveGoods(goodsForm)
      setFeedback('success', '商品已添加')
    }
    goodsModalVisible.value = false
    await loadGoods()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '保存失败')
  } finally {
    goodsSubmitting.value = false
  }
}

async function handleDeleteGoods(id: number) {
  if (!confirm('确定要删除此商品吗？')) return
  try {
    await pointsApi.deleteGoods(id)
    setFeedback('success', '商品已删除')
    await loadGoods()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(() => {
  loadHistory()
})
</script>
