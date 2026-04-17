<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-gray-900">我的食品</h1>
        <p class="text-sm text-gray-400 mt-1">共 {{ total }} 件食品</p>
      </div>
      <button @click="openAddModal"
        class="flex items-center gap-2 bg-blue-600 text-white px-5 py-2.5 rounded-xl text-sm font-bold hover:bg-blue-700 transition-colors shadow-sm">
        <PlusIcon class="w-4 h-4" />
        新增食品
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="foods.length === 0" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-16 text-center">
      <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
        <ShoppingBagIcon class="w-8 h-8 text-gray-300" />
      </div>
      <p class="text-gray-400 font-bold">暂无食品数据</p>
      <p class="text-gray-300 text-sm mt-2">点击上方"新增食品"开始</p>
    </div>

    <!-- Table -->
    <div v-else class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full text-left text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">食品名称</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">原价</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">折扣价</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">库存</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">到期日</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">状态</th>
              <th class="px-5 py-4 font-bold text-gray-400 text-xs uppercase">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="food in foods" :key="food.foodId" class="hover:bg-blue-50/30 transition-colors group">
              <td class="px-5 py-4 font-bold text-gray-900">
                <div class="flex items-center gap-2">
                  {{ food.foodName }}
                  <span v-if="food.stockQty < 10" class="px-2 py-0.5 bg-orange-100 text-orange-600 text-[10px] font-bold rounded">
                    库存预警
                  </span>
                </div>
              </td>
              <td class="px-5 py-4 text-gray-400 line-through">&yen;{{ food.originPrice?.toFixed(2) }}</td>
              <td class="px-5 py-4 text-orange-500 font-bold">&yen;{{ food.discountPrice?.toFixed(2) }}</td>
              <td class="px-5 py-4">
                <span :class="['font-bold', food.stockQty < 10 ? 'text-orange-500' : 'text-gray-600']">
                  {{ food.stockQty }}
                </span>
              </td>
              <td class="px-5 py-4 text-gray-500">{{ (food.expireDate || '').replace('T', ' ').slice(0, 10) }}</td>
              <td class="px-5 py-4">
                <span :class="['px-2.5 py-1 rounded-lg text-xs font-bold', food.status === 1 ? 'bg-green-50 text-green-600' : 'bg-gray-100 text-gray-400']">
                  {{ food.status === 1 ? '在售' : '下架' }}
                </span>
              </td>
              <td class="px-5 py-4">
                <div class="flex gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button @click="openEditModal(food)" class="text-xs text-blue-600 font-bold hover:underline">编辑</button>
                  <button @click="toggleStatus(food)" class="text-xs font-bold hover:underline" :class="food.status === 1 ? 'text-red-500' : 'text-green-500'">
                    {{ food.status === 1 ? '下架' : '上架' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex items-center justify-center gap-3">
      <button :disabled="pageNum <= 1" @click="pageNum--"
        class="px-4 py-2 rounded-xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 disabled:opacity-30 transition-colors">
        上一页
      </button>
      <span class="text-sm text-gray-500 font-bold">{{ pageNum }} / {{ totalPages }}</span>
      <button :disabled="pageNum >= totalPages" @click="pageNum++"
        class="px-4 py-2 rounded-xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 disabled:opacity-30 transition-colors">
        下一页
      </button>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4" @click.self="showModal = false">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <h3 class="text-lg font-black text-gray-900 mb-5">{{ editingFood ? '编辑食品' : '新增食品' }}</h3>
        <div class="space-y-4">
          <div>
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">食品名称</label>
            <input v-model="form.foodName" type="text"
              class="w-full mt-1.5 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue-500 transition"
              placeholder="请输入食品名称" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">原价</label>
              <input v-model.number="form.originPrice" type="number" step="0.01"
                class="w-full mt-1.5 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue-500 transition"
                placeholder="0.00" />
            </div>
            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">折扣价</label>
              <input v-model.number="form.discountPrice" type="number" step="0.01"
                class="w-full mt-1.5 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue-500 transition"
                placeholder="0.00" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">库存</label>
              <input v-model.number="form.stockQty" type="number"
                class="w-full mt-1.5 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue-500 transition"
                placeholder="0" />
            </div>
            <div>
              <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">到期日期</label>
              <input v-model="form.expireDate" type="date"
                class="w-full mt-1.5 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue-500 transition" />
            </div>
          </div>
          <div>
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">描述</label>
            <textarea v-model="form.description" rows="3"
              class="w-full mt-1.5 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue-500 resize-none transition"
              placeholder="食品描述"></textarea>
          </div>
          <div>
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">营养成分</label>
            <input v-model="form.nutritionDesc" type="text"
              class="w-full mt-1.5 rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-blue-500 transition"
              placeholder="如：高蛋白、低脂" />
          </div>
          <div>
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">封面图片</label>
            <ImageUpload v-model="form.coverImg" />
          </div>
        </div>
        <div class="flex gap-3 mt-6">
          <button @click="showModal = false"
            class="flex-1 py-3 rounded-xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-colors">
            取消
          </button>
          <button @click="handleSave" :disabled="saving"
            class="flex-1 py-3 rounded-xl bg-blue-600 text-white font-bold hover:bg-blue-700 transition-colors disabled:bg-blue-300">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { Plus as PlusIcon, ShoppingBag as ShoppingBagIcon } from 'lucide-vue-next'
import { merchantApi } from '@/api/merchant'
import { foodApi, type FoodDTO } from '@/api/food'
import { useToast } from '@/composables/useToast'
import ImageUpload from '@/components/ui/ImageUpload.vue'

const toast = useToast()
const loading = ref(true)
const foods = ref<FoodDTO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

const showModal = ref(false)
const saving = ref(false)
const editingFood = ref<FoodDTO | null>(null)
const form = ref({
  foodName: '',
  originPrice: 0,
  discountPrice: 0,
  stockQty: 0,
  expireDate: '',
  description: '',
  nutritionDesc: '',
  coverImg: '',
})

async function loadFoods() {
  loading.value = true
  try {
    const result = await merchantApi.getFoods({ pageNum: pageNum.value, pageSize })
    foods.value = result.records || []
    total.value = result.total
  } catch {
    foods.value = []
  } finally {
    loading.value = false
  }
}

function openAddModal() {
  editingFood.value = null
  form.value = { foodName: '', originPrice: 0, discountPrice: 0, stockQty: 0, expireDate: '', description: '', nutritionDesc: '', coverImg: '' }
  showModal.value = true
}

function openEditModal(food: FoodDTO) {
  editingFood.value = food
  form.value = {
    foodName: food.foodName,
    originPrice: food.originPrice,
    discountPrice: food.discountPrice,
    stockQty: food.stockQty,
    expireDate: (food.expireDate || '').slice(0, 10),
    description: food.description,
    nutritionDesc: food.nutritionDesc,
    coverImg: food.coverImg,
  }
  showModal.value = true
}

async function handleSave() {
  if (!form.value.foodName.trim()) {
    toast.show('请输入食品名称', 'warning')
    return
  }
  saving.value = true
  try {
    const payload: Partial<FoodDTO> = {
      ...form.value,
      status: editingFood.value ? editingFood.value.status : 1,
    }
    if (editingFood.value) {
      payload.foodId = editingFood.value.foodId
      await merchantApi.updateFood(payload)
      toast.show('食品已更新', 'success')
    } else {
      await merchantApi.saveFood(payload)
      toast.show('食品已新增', 'success')
    }
    showModal.value = false
    await loadFoods()
  } catch (e: unknown) {
    toast.show(e instanceof Error ? e.message : '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(food: FoodDTO) {
  try {
    await merchantApi.updateFood({
      foodId: food.foodId,
      status: food.status === 1 ? 0 : 1,
    })
    toast.show(food.status === 1 ? '已下架' : '已上架', 'success')
    await loadFoods()
  } catch (e: unknown) {
    toast.show(e instanceof Error ? e.message : '操作失败', 'error')
  }
}

watch(pageNum, loadFoods)
onMounted(loadFoods)
</script>
