<template>
  <div class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-gray-900">食材资源管理</h1>
        <p class="mt-2 text-sm text-gray-500">维护食品基础资料、库存、价格与上下架状态。</p>
      </div>
      <button
        class="bg-green-600 text-white px-6 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all shadow-lg shadow-green-100"
        @click="openCreateModal"
      >
        添加食材
      </button>
    </div>

    <div
      v-if="feedback"
      class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'"
    >
      {{ feedback.message }}
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="border-b border-gray-100 px-8 py-5 flex flex-col lg:flex-row lg:items-center gap-4">
        <div class="flex-1 grid grid-cols-1 md:grid-cols-3 gap-3">
          <input
            v-model.trim="filters.keyword"
            class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500"
            placeholder="搜索食品名称"
            @keyup.enter="loadFoods"
          />
          <select
            v-model.number="filters.categoryId"
            class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500"
          >
            <option :value="0">全部分类</option>
            <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">
              {{ category.categoryName }}
            </option>
          </select>
          <select
            v-model="filters.status"
            class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500"
          >
            <option value="">全部状态</option>
            <option value="1">上架中</option>
            <option value="0">已下架</option>
          </select>
        </div>
        <div class="flex items-center gap-3">
          <button
            class="px-4 py-3 rounded-2xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 transition-all"
            @click="resetFilters"
          >
            重置
          </button>
          <button
            class="px-4 py-3 rounded-2xl bg-green-600 text-sm font-bold text-white hover:bg-green-700 transition-all"
            @click="loadFoods"
          >
            查询
          </button>
        </div>
      </div>
      <table class="w-full">
        <thead>
            <tr class="bg-gray-50">
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">食品名称</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">分类</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">价格</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">库存</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">状态</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading">
            <td colspan="6" class="px-8 py-12 text-center text-gray-400">
              <div class="flex items-center justify-center">
                <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                加载中...
              </div>
            </td>
          </tr>
          <tr v-else-if="foods.length === 0">
            <td colspan="6" class="px-8 py-12 text-center text-gray-400">暂无数据</td>
          </tr>
          <tr v-else v-for="food in foods" :key="food.foodId" class="hover:bg-gray-50 transition-colors">
            <td class="px-8 py-6">
              <div class="flex items-center gap-4">
                <img :src="getFoodImage(food.coverImg, food.categoryName)" :alt="food.foodName" class="w-12 h-12 rounded-xl object-cover" referrerPolicy="no-referrer" />
                <div>
                  <span class="font-medium text-gray-900">{{ food.foodName }}</span>
                  <p class="text-xs text-gray-400 mt-1">保质期至 {{ formatDisplayTime(food.expireDate) }}</p>
                </div>
              </div>
            </td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ food.categoryName }}</td>
            <td class="px-8 py-6 text-sm font-bold text-orange-500">¥{{ food.discountPrice.toFixed(2) }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ food.stockQty }}</td>
            <td class="px-8 py-6">
              <StatusTag :status="food.status === 1 ? '上架中' : '已下架'" />
            </td>
            <td class="px-8 py-6">
              <div class="flex items-center gap-3">
                <button class="text-green-600 hover:underline text-sm font-medium" @click="openEditModal(food.foodId)">编辑</button>
                <button
                  class="text-red-600 hover:underline text-sm font-medium"
                  :disabled="busyId === food.foodId"
                  @click="toggleStatus(food)"
                >
                  {{ food.status === 1 ? '下架' : '上架' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modalVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4" @click.self="closeModal">
      <div class="w-full max-w-4xl bg-white rounded-[32px] shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">{{ form.foodId ? '编辑食材' : '添加食材' }}</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="closeModal">
            <XIcon class="w-5 h-5" />
          </button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">食品名称</span>
            <input v-model="form.foodName" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">食品分类</span>
            <select v-model.number="form.categoryId" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500">
              <option :value="0">请选择分类</option>
              <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">
                {{ category.categoryName }}
              </option>
            </select>
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">过期时间</span>
            <input v-model="form.expireDate" type="datetime-local" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">原价</span>
            <input v-model.number="form.originPrice" type="number" min="0" step="0.01" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">折后价</span>
            <input v-model.number="form.discountPrice" type="number" min="0" step="0.01" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">库存</span>
            <input v-model.number="form.stockQty" type="number" min="0" step="1" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">上架状态</span>
            <select v-model.number="form.status" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500">
              <option :value="1">上架中</option>
              <option :value="0">已下架</option>
            </select>
          </label>
          <label class="space-y-2 block md:col-span-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">营养描述</span>
            <input v-model="form.nutritionDesc" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block md:col-span-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">适宜人群</span>
            <input v-model="form.suitablePeople" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block md:col-span-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">过敏原信息</span>
            <input v-model="form.allergenInfo" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block md:col-span-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">封面图片</span>
            <ImageUpload v-model="form.coverImg" />
          </label>
          <label class="space-y-2 block md:col-span-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">食品简介</span>
            <textarea v-model="form.description" rows="4" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500 resize-none" />
          </label>
        </div>

        <div class="flex items-center justify-end gap-3 mt-8">
          <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="closeModal">
            取消
          </button>
          <button
            class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all disabled:bg-green-300"
            :disabled="submitting"
            @click="submitFood"
          >
            {{ submitting ? '保存中...' : '保存食材' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { foodApi, type FoodCategory, type FoodDTO } from '@/api/food'
import StatusTag from '@/components/ui/StatusTag.vue'
import { getFoodImage } from '@/utils/images'
import ImageUpload from '@/components/ui/ImageUpload.vue'
import { X } from 'lucide-vue-next'

interface FeedbackState {
  type: 'success' | 'error'
  message: string
}

type FoodForm = {
  foodId: number | null
  categoryId: number
  foodName: string
  originPrice: number
  discountPrice: number
  stockQty: number
  expireDate: string
  nutritionDesc: string
  suitablePeople: string
  allergenInfo: string
  coverImg: string
  description: string
  status: number
}

const XIcon = X
const foods = ref<FoodDTO[]>([])
const categories = ref<FoodCategory[]>([])
const loading = ref(true)
const modalVisible = ref(false)
const submitting = ref(false)
const busyId = ref<number | null>(null)
const feedback = ref<FeedbackState | null>(null)
const filters = reactive({
  keyword: '',
  categoryId: 0,
  status: '',
})
const form = reactive<FoodForm>(createEmptyForm())

function createEmptyForm(): FoodForm {
  return {
    foodId: null,
    categoryId: 0,
    foodName: '',
    originPrice: 0,
    discountPrice: 0,
    stockQty: 0,
    expireDate: '',
    nutritionDesc: '',
    suitablePeople: '',
    allergenInfo: '',
    coverImg: '',
    description: '',
    status: 1,
  }
}

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
}

async function loadFoods() {
  loading.value = true
  try {
    const result = await foodApi.adminList({
      pageNum: 1,
      pageSize: 100,
      keyword: filters.keyword || undefined,
      categoryId: filters.categoryId || undefined,
      status: filters.status === '' ? undefined : Number(filters.status),
    })
    foods.value = result.records
  } catch (error) {
    foods.value = []
    setFeedback('error', error instanceof Error ? error.message : '食品数据加载失败')
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    categories.value = await foodApi.getCategories()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '分类选项加载失败')
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.categoryId = 0
  filters.status = ''
  loadFoods()
}

function resetForm() {
  Object.assign(form, createEmptyForm())
}

function openCreateModal() {
  resetForm()
  modalVisible.value = true
}

async function openEditModal(foodId: number) {
  try {
    const detail = await foodApi.getById(foodId)
    Object.assign(form, {
      foodId: detail.foodId,
      categoryId: detail.categoryId,
      foodName: detail.foodName || '',
      originPrice: detail.originPrice ?? 0,
      discountPrice: detail.discountPrice ?? 0,
      stockQty: detail.stockQty ?? 0,
      expireDate: normalizeDateTimeLocal(detail.expireDate),
      nutritionDesc: detail.nutritionDesc || '',
      suitablePeople: detail.suitablePeople || '',
      allergenInfo: detail.allergenInfo || '',
      coverImg: detail.coverImg || '',
      description: detail.description || '',
      status: detail.status ?? 1,
    })
    modalVisible.value = true
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '食品详情加载失败')
  }
}

function closeModal() {
  modalVisible.value = false
  resetForm()
}

async function submitFood() {
  if (!form.foodName.trim() || !form.categoryId || !form.expireDate) {
    setFeedback('error', '请完整填写食品名称、食品分类和过期时间。')
    return
  }
  if (form.discountPrice > form.originPrice) {
    setFeedback('error', '折后价不能高于原价。')
    return
  }

  submitting.value = true
  try {
    const payload = {
      foodId: form.foodId ?? undefined,
      categoryId: form.categoryId,
      foodName: form.foodName.trim(),
      originPrice: form.originPrice,
      discountPrice: form.discountPrice,
      stockQty: form.stockQty,
      expireDate: withSeconds(form.expireDate),
      nutritionDesc: form.nutritionDesc.trim(),
      suitablePeople: form.suitablePeople.trim(),
      allergenInfo: form.allergenInfo.trim(),
      coverImg: form.coverImg.trim(),
      description: form.description.trim(),
      status: form.status,
    }

    if (form.foodId) {
      await foodApi.update(payload)
      setFeedback('success', '食品信息已更新。')
    } else {
      await foodApi.save(payload)
      setFeedback('success', '食品已成功添加。')
    }

    closeModal()
    await loadFoods()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '食品保存失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(food: FoodDTO) {
  busyId.value = food.foodId
  const nextStatus = food.status === 1 ? 0 : 1
  try {
    await foodApi.updateStatus(food.foodId, nextStatus)
    setFeedback('success', nextStatus === 1 ? '食品已恢复运营。' : '食品已停运，但仍可在后台继续编辑。')
    await loadFoods()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '食品状态更新失败')
  } finally {
    busyId.value = null
  }
}

function normalizeDateTimeLocal(value?: string) {
  if (!value) return ''
  return value.replace(' ', 'T').slice(0, 16)
}

function withSeconds(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function formatDisplayTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  await Promise.all([loadFoods(), loadOptions()])
})
</script>
