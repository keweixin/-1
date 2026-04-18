<template>
  <div class="space-y-8">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black text-gray-900">配送调度管理</h1>
        <p class="mt-2 text-sm text-gray-500">管理骑手团队，调度订单配送状态。</p>
      </div>
      <div class="flex items-center gap-3">
        <div class="flex bg-gray-100 rounded-2xl p-1">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            class="px-4 py-2 rounded-xl text-sm font-bold transition-all"
            :class="activeTab === tab.key ? 'bg-white text-green-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="activeTab === 'dispatch' ? loadDispatches() : loadCouriers()">
          刷新
        </button>
      </div>
    </div>

    <!-- 配送调度 tab -->
    <template v-if="activeTab === 'dispatch'">
      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
        <div v-for="item in statCards" :key="item.label" class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">{{ item.label }}</p>
          <div class="mt-3 flex items-end justify-between">
            <h3 class="text-3xl font-black text-gray-900">{{ item.value }}</h3>
            <component :is="item.icon" class="w-6 h-6" :class="item.iconClass" />
          </div>
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-3 mb-4">
        <select v-model="statusFilter" class="rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-medium text-gray-600 outline-none focus:border-green-500">
          <option value="">全部状态</option>
          <option value="待分配">待分配</option>
          <option value="待取货">待取货</option>
          <option value="配送中">配送中</option>
          <option value="已完成">已完成</option>
        </select>
      </div>

      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50">
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">订单号</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">收货人</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">骑手</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">状态</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">调度时间</th>
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
            <tr v-else-if="filteredDispatches.length === 0">
              <td colspan="6" class="px-6 py-12 text-center text-gray-400">暂无调度数据</td>
            </tr>
            <tr v-else v-for="item in filteredDispatches" :key="item.dispatchId" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-5">
                <div class="font-bold text-gray-900">{{ item.orderNo }}</div>
                <p class="text-xs text-gray-400">#{{ item.dispatchId }}</p>
              </td>
              <td class="px-6 py-5">
                <div class="font-medium text-gray-900">{{ item.receiverName }}</div>
                <p class="text-xs text-gray-400">{{ item.receiverPhone }}</p>
              </td>
              <td class="px-6 py-5">
                <div class="font-medium text-gray-900">{{ item.courierName || '未分配' }}</div>
                <p class="text-xs text-gray-400">{{ item.courierPhone || '-' }}</p>
              </td>
              <td class="px-6 py-5"><StatusTag :status="item.dispatchStatus" /></td>
              <td class="px-6 py-5 text-sm text-gray-500">{{ item.dispatchTime || '-' }}</td>
              <td class="px-6 py-5">
                <div class="flex flex-wrap items-center gap-3">
                  <template v-if="item.dispatchStatus === '待分配'">
                    <button class="text-green-600 font-bold hover:underline" @click="openAssign(item)">分配骑手</button>
                  </template>
                  <template v-else-if="item.dispatchStatus === '待取货'">
                    <span class="text-xs text-gray-400">骑手已接单，等待取货</span>
                  </template>
                  <template v-else-if="item.dispatchStatus === '配送中'">
                    <span class="text-xs text-blue-500">骑手配送中</span>
                  </template>
                  <template v-else-if="item.dispatchStatus === '已完成'">
                    <span class="text-xs text-green-500">已完成</span>
                  </template>
                  <template v-else-if="item.dispatchStatus === '已拒绝'">
                    <button class="text-orange-600 font-bold hover:underline" @click="openAssign(item)">重新分配</button>
                  </template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- 骑手管理 tab -->
    <template v-else>
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-3">
          <select v-model="courierStatusFilter" class="rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-medium text-gray-600 outline-none focus:border-green-500">
            <option value="">全部状态</option>
            <option value="1">在职</option>
            <option value="0">离职</option>
          </select>
        </div>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="openCourierForm()">
          + 新增骑手
        </button>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
        <div v-for="c in filteredCouriers" :key="c.courierId"
          class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6 hover:-translate-y-0.5 hover:shadow-md transition-all">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-2xl bg-green-100 flex items-center justify-center">
                <Bike class="w-6 h-6 text-green-600" />
              </div>
              <div>
                <h3 class="font-black text-gray-900">{{ c.courierName }}</h3>
                <p class="text-sm text-gray-500 mt-1">{{ c.courierPhone }}</p>
              </div>
            </div>
            <span class="inline-flex items-center gap-1 text-xs font-bold px-3 py-1 rounded-full"
              :class="c.status === 1 ? 'bg-green-50 text-green-600' : 'bg-gray-100 text-gray-400'">
              <span class="w-1.5 h-1.5 rounded-full" :class="c.status === 1 ? 'bg-green-500' : 'bg-gray-400'"></span>
              {{ c.status === 1 ? '在职' : '离职' }}
            </span>
          </div>
          <div class="flex items-center justify-end gap-3 mt-5 pt-4 border-t border-gray-100">
            <button class="text-gray-500 font-bold hover:text-gray-700 text-sm" @click="openCourierForm(c)">编辑</button>
            <button v-if="c.status === 1" class="text-red-500 font-bold hover:text-red-600 text-sm" @click="removeCourier(c)">离职</button>
            <button v-else class="text-green-600 font-bold hover:text-green-700 text-sm" @click="reactivateCourier(c)">复职</button>
          </div>
        </div>
        <div v-if="filteredCouriers.length === 0 && !courierLoading" class="col-span-full text-center py-12 text-gray-400">
          暂无骑手数据
        </div>
      </div>
    </template>

    <!-- 分配骑手弹窗 -->
    <div v-if="assignVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4">
      <div class="w-full max-w-xl bg-white rounded-[32px] shadow-2xl p-8">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">分配骑手</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="assignVisible = false">
            <XIcon class="w-5 h-5" />
          </button>
        </div>
        <div class="space-y-4">
          <p class="text-sm text-gray-500">订单号：{{ assignTarget?.orderNo }}</p>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">选择骑手</span>
            <select v-model="assignForm.courierId" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500">
              <option value="">-- 选择已有骑手 --</option>
              <option v-for="c in activeCouriers" :key="c.courierId" :value="c.courierId">
                {{ c.courierName }} ({{ c.courierPhone }})
              </option>
            </select>
          </label>
          <div class="text-center text-gray-400 text-sm">— 或手动填写 —</div>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">骑手姓名</span>
            <input v-model="assignForm.courierName" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500"
              placeholder="手动输入姓名" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">骑手电话</span>
            <input v-model="assignForm.courierPhone" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500"
              placeholder="手动输入电话" />
          </label>
          <div class="flex items-center justify-end gap-3 mt-6">
            <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="assignVisible = false">
              取消
            </button>
            <button class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all" @click="submitAssign">
              保存
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑骑手弹窗 -->
    <div v-if="courierFormVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4">
      <div class="w-full max-w-md bg-white rounded-[32px] shadow-2xl p-8">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">{{ courierEditTarget ? '编辑骑手' : '新增骑手' }}</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="courierFormVisible = false">
            <XIcon class="w-5 h-5" />
          </button>
        </div>
        <div class="space-y-4">
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">骑手姓名</span>
            <input v-model="courierForm.courierName" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500"
              placeholder="请输入骑手姓名" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">骑手电话</span>
            <input v-model="courierForm.courierPhone" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500"
              placeholder="请输入骑手电话" />
          </label>
          <div class="flex items-center justify-end gap-3 mt-6">
            <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="courierFormVisible = false">
              取消
            </button>
            <button class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all" @click="submitCourierForm">
              保存
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { adminApi, type CourierEntity, type DispatchDTO } from '@/api/admin'
import StatusTag from '@/components/ui/StatusTag.vue'
import { AlertTriangle, Bike, CheckCircle2, Clock3, Truck, X, XCircle } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const XIcon = X
const activeTab = ref('dispatch')
const tabs = [
  { key: 'dispatch', label: '配送调度' },
  { key: 'courier', label: '骑手管理' },
]

// Dispatch state
const dispatches = ref<DispatchDTO[]>([])
const loading = ref(true)
const statusFilter = ref('')
const assignVisible = ref(false)
const assignTarget = ref<DispatchDTO | null>(null)
const activeCouriers = ref<CourierEntity[]>([])
const assignForm = reactive({ courierId: '' as number | '', courierName: '', courierPhone: '' })

// Courier state
const couriers = ref<CourierEntity[]>([])
const courierLoading = ref(false)
const courierStatusFilter = ref('')
const courierFormVisible = ref(false)
const courierEditTarget = ref<CourierEntity | null>(null)
const courierForm = reactive({ courierName: '', courierPhone: '' })

const filteredDispatches = computed(() => {
  if (!statusFilter.value) return dispatches.value
  return dispatches.value.filter(item => item.dispatchStatus === statusFilter.value)
})

const filteredCouriers = computed(() => {
  if (!courierStatusFilter.value) return couriers.value
  return couriers.value.filter(c => c.status === Number(courierStatusFilter.value))
})

const statCards = computed(() => {
  const total = dispatches.value.length
  const pending = dispatches.value.filter(item => item.dispatchStatus === '待分配').length
  const pickedUp = dispatches.value.filter(item => item.dispatchStatus === '待取货').length
  const delivering = dispatches.value.filter(item => item.dispatchStatus === '配送中').length
  const finished = dispatches.value.filter(item => item.dispatchStatus === '已完成').length
  const rejected = dispatches.value.filter(item => item.dispatchStatus === '已拒绝').length
  return [
    { label: '总调度数', value: total, icon: Truck, iconClass: 'text-blue-600' },
    { label: '待分配', value: pending, icon: Clock3, iconClass: 'text-orange-600' },
    { label: '配送中', value: delivering, icon: Bike, iconClass: 'text-blue-600' },
    { label: '已完成', value: finished, icon: CheckCircle2, iconClass: 'text-green-600' },
    { label: '已拒绝', value: rejected, icon: XCircle, iconClass: 'text-red-600' },
  ]
})

async function loadDispatches() {
  loading.value = true
  try {
    const result = await adminApi.listDispatches({ pageNum: 1, pageSize: 50 })
    dispatches.value = result.records
  } catch {
    dispatches.value = []
  } finally {
    loading.value = false
  }
}

async function loadCouriers() {
  courierLoading.value = true
  try {
    couriers.value = await adminApi.listCouriers(true)
  } catch {
    couriers.value = []
  } finally {
    courierLoading.value = false
  }
}

async function loadActiveCouriers() {
  try {
    activeCouriers.value = await adminApi.listCouriers(false)
  } catch {
    activeCouriers.value = []
  }
}

function openAssign(item: DispatchDTO) {
  assignTarget.value = item
  if (item.dispatchStatus === '已拒绝') {
    assignForm.courierId = ''
    assignForm.courierName = ''
    assignForm.courierPhone = ''
  } else {
    assignForm.courierId = ''
    assignForm.courierName = item.courierName ?? ''
    assignForm.courierPhone = item.courierPhone ?? ''
  }
  assignVisible.value = true
}

function openCourierForm(c?: CourierEntity) {
  courierEditTarget.value = c ?? null
  courierForm.courierName = c?.courierName ?? ''
  courierForm.courierPhone = c?.courierPhone ?? ''
  courierFormVisible.value = true
}

async function submitAssign() {
  if (!assignTarget.value) return
  let courierName = assignForm.courierName
  let courierPhone = assignForm.courierPhone
  if (assignForm.courierId) {
    const selected = activeCouriers.value.find(c => c.courierId === assignForm.courierId)
    if (selected) {
      courierName = selected.courierName
      courierPhone = selected.courierPhone
    }
  }
  if (!courierName || !courierPhone) {
    showToast('请选择骑手或填写骑手信息', 'warning')
    return
  }
  try {
    await adminApi.dispatchAssign(assignTarget.value.orderId, courierName, courierPhone)
    assignVisible.value = false
    await loadDispatches()
  } catch (e: unknown) {
    showToast('分配失败：' + (e instanceof Error ? e.message : '请重试'), 'error')
  }
}

async function setStatus(item: DispatchDTO, status: string) {
  if (status === '已完成') {
    if (!confirm('确认将配送状态改为已完成？')) return
  }
  try {
    await adminApi.updateDispatchStatus(item.dispatchId, status)
    await loadDispatches()
  } catch (e: unknown) {
    showToast('状态更新失败：' + (e instanceof Error ? e.message : '请重试'), 'error')
  }
}



async function submitCourierForm() {
  if (!courierForm.courierName.trim()) {
    showToast('请输入骑手姓名', 'warning')
    return
  }
  if (!courierForm.courierPhone.trim()) {
    showToast('请输入骑手电话', 'warning')
    return
  }
  try {
    if (courierEditTarget.value) {
      await adminApi.updateCourier(courierEditTarget.value.courierId, {
        courierName: courierForm.courierName,
        courierPhone: courierForm.courierPhone,
      })
    } else {
      await adminApi.addCourier({
        courierName: courierForm.courierName,
        courierPhone: courierForm.courierPhone,
      })
    }
    courierFormVisible.value = false
    await loadCouriers()
    await loadActiveCouriers()
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '保存失败', 'error')
  }
}

async function removeCourier(c: CourierEntity) {
  if (!confirm(`确认将骑手"${c.courierName}"设为离职？`)) return
  try {
    await adminApi.deleteCourier(c.courierId)
    await loadCouriers()
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '操作失败', 'error')
  }
}

async function reactivateCourier(c: CourierEntity) {
  try {
    await adminApi.updateCourier(c.courierId, { status: 1 })
    await loadCouriers()
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '操作失败', 'error')
  }
}

onMounted(async () => {
  await Promise.all([loadDispatches(), loadCouriers(), loadActiveCouriers()])
})
</script>
