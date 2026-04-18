<template>
  <div class="min-h-screen bg-gray-50 pb-20">
    <!-- Search & Filter Header -->
    <div class="bg-white border-b border-gray-200 sticky top-16 z-30 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <div class="flex flex-col md:flex-row gap-6 items-center justify-between">
          <div class="relative w-full md:w-[500px]">
            <SearchIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索临期食品、品牌、门店..."
              class="w-full pl-12 pr-4 py-3.5 bg-gray-50 border-gray-100 focus:bg-white focus:border-green-600 focus:ring-4 focus:ring-green-600/5 rounded-2xl transition-all text-sm font-medium outline-none border"
            />
          </div>

          <div class="flex items-center gap-3 w-full md:w-auto">
            <button
              @click="showFilters = !showFilters"
              :class="[
                'flex items-center gap-2 px-6 py-3.5 rounded-2xl border transition-all text-sm font-black uppercase tracking-widest',
                showFilters
                  ? 'bg-green-600 text-white border-green-600 shadow-lg shadow-green-600/20'
                  : 'bg-white text-gray-600 border-gray-200 hover:border-green-600 hover:text-green-600'
              ]"
            >
              <SlidersHorizontalIcon class="w-4 h-4" />
              高级筛选
            </button>
            <div class="relative group">
              <button class="flex items-center gap-2 px-6 py-3.5 bg-white border border-gray-200 rounded-2xl text-sm font-black text-gray-600 hover:border-green-600 hover:text-green-600 transition-all uppercase tracking-widest">
                {{ sortBy }}
                <ChevronDownIcon class="w-4 h-4" />
              </button>
              <div class="absolute right-0 top-full mt-2 w-48 bg-white border border-gray-100 rounded-2xl shadow-2xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50 p-1.5">
                <button
                  v-for="opt in sortOptions"
                  :key="opt"
                  @click="sortBy = opt"
                  class="w-full text-left px-4 py-2.5 text-sm font-bold text-gray-500 hover:bg-green-50 hover:text-green-600 rounded-xl transition-colors"
                >
                  {{ opt }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Category Tabs -->
        <div class="flex items-center gap-3 mt-8 overflow-x-auto pb-2 scrollbar-hide">
          <button
            v-for="cat in categoryTabs"
            :key="cat"
            @click="selectedCategory = cat"
            :class="[
              'px-6 py-2 rounded-xl text-xs font-black whitespace-nowrap transition-all uppercase tracking-widest border',
              selectedCategory === cat
                ? 'bg-green-600 text-white border-green-600 shadow-lg shadow-green-600/20'
                : 'bg-white text-gray-400 border-gray-100 hover:border-green-600 hover:text-green-600'
            ]"
          >
            {{ cat }}
          </button>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 mt-10">
      <div class="flex flex-col lg:flex-row gap-10">
        <!-- Sidebar Filters (Desktop) -->
        <Transition
          enter-active-class="transition-all duration-300 ease-out"
          enter-from-class="opacity-0 -translate-x-4"
          enter-to-class="opacity-100 translate-x-0"
          leave-active-class="transition-all duration-200 ease-in"
          leave-from-class="opacity-100 translate-x-0"
          leave-to-class="opacity-0 -translate-x-4"
        >
          <aside v-if="showFilters" class="hidden lg:block flex-shrink-0 overflow-hidden">
            <div class="bg-white p-8 rounded-[2.5rem] border border-gray-100 shadow-sm sticky top-48">
              <div class="flex items-center justify-between mb-8">
                <h3 class="font-black text-gray-900 text-lg tracking-tight">筛选条件</h3>
                <button @click="showFilters = false" class="w-8 h-8 flex items-center justify-center bg-gray-50 rounded-full text-gray-400 hover:text-red-500 transition-colors">
                  <XIcon class="w-4 h-4" />
                </button>
              </div>

              <div class="space-y-10">
                <div>
                  <label class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-4 block">价格区间 (¥)</label>
                  <div class="flex items-center gap-3">
                    <input
                      v-model.number="priceRange[0]"
                      type="number"
                      placeholder="0"
                      class="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl text-sm font-bold outline-none focus:border-green-600 focus:bg-white transition-all"
                    />
                    <span class="text-gray-300 font-black">-</span>
                    <input
                      v-model.number="priceRange[1]"
                      type="number"
                      placeholder="100"
                      class="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl text-sm font-bold outline-none focus:border-green-600 focus:bg-white transition-all"
                    />
                  </div>
                </div>

                <div>
                  <label class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-4 block">保质期筛选</label>
                  <div class="space-y-3">
                    <label v-for="range in expiryRanges" :key="range" class="flex items-center gap-3 cursor-pointer group">
                      <input type="checkbox" :value="range" v-model="selectedExpiryRanges" class="peer w-5 h-5 rounded-lg border-gray-200 text-green-600 focus:ring-green-600/20 transition-all" />
                      <span class="text-sm font-bold text-gray-500 group-hover:text-green-600 transition-colors">{{ range }}</span>
                    </label>
                  </div>
                </div>

                <div>
                  <label class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-4 block">营养标签</label>
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="tag in nutritionTags"
                      :key="tag"
                      @click="toggleNutritionTag(tag)"
                      :class="[
                        'px-3 py-1.5 border rounded-xl text-[10px] font-black uppercase tracking-widest transition-all',
                        selectedNutritionTags.includes(tag)
                          ? 'bg-green-600 text-white border-green-600 shadow-lg shadow-green-600/20'
                          : 'bg-gray-50 border-gray-100 text-gray-400 hover:bg-green-50 hover:text-green-600 hover:border-green-600'
                      ]"
                    >
                      {{ tag }}
                    </button>
                  </div>
                </div>
              </div>

              <button class="w-full mt-10 bg-gray-900 text-white py-4 rounded-2xl font-black shadow-xl shadow-gray-900/10 hover:bg-green-600 transition-all active:scale-95 uppercase tracking-widest" @click="applyFilters">
                应用筛选
              </button>
            </div>
          </aside>
        </Transition>

        <!-- Main List Area -->
        <div class="flex-grow">
          <div class="flex items-center justify-between mb-6">
            <p class="text-sm text-gray-500">
              共找到 <span class="text-green-600 font-bold">{{ total }}</span> 件临期食品
            </p>
            <div class="flex items-center bg-white border border-gray-200 rounded-lg p-1">
              <button class="p-1.5 bg-gray-100 text-green-600 rounded-md shadow-sm">
                <LayoutGridIcon class="w-4 h-4" />
              </button>
              <button class="p-1.5 text-gray-400 hover:text-green-600 transition-colors">
                <ListIcon class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Loading skeleton -->
          <div v-if="loading && apiFoods.length === 0" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
            <div v-for="i in 6" :key="i" class="bg-white rounded-3xl border border-gray-100 p-5 animate-pulse">
              <div class="h-48 bg-gray-100 rounded-2xl mb-4"></div>
              <div class="h-5 bg-gray-100 rounded-xl w-3/4 mb-3"></div>
              <div class="h-4 bg-gray-100 rounded-xl w-1/2 mb-4"></div>
              <div class="flex justify-between items-center">
                <div class="h-6 bg-gray-100 rounded-xl w-20"></div>
                <div class="h-10 bg-gray-100 rounded-2xl w-28"></div>
              </div>
            </div>
          </div>

          <!-- Reloading indicator (already has data, refreshing) -->
          <div v-else-if="loading && apiFoods.length > 0" class="mb-4 flex items-center justify-center gap-3 py-3 text-sm text-green-600">
            <div class="h-4 w-4 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
            刷新中...
          </div>

          <div v-else-if="filteredItems.length > 0" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
            <FoodCard
              v-for="food in filteredItems"
              :key="food.id"
              v-bind="food"
            />
          </div>

          <div v-else class="flex flex-col items-center justify-center py-32 bg-white rounded-3xl border border-dashed border-gray-200">
            <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mb-4">
              <SearchIcon class="text-gray-300 w-10 h-10" />
            </div>
            <h3 class="text-lg font-bold text-gray-900 mb-1">未找到相关食品</h3>
            <p class="text-sm text-gray-400">尝试更换关键词或筛选条件吧</p>
            <button
              @click="resetFilters"
              class="mt-6 text-green-600 font-bold hover:underline"
            >
              重置所有条件
            </button>
          </div>

          <!-- Pagination -->
          <div v-if="filteredItems.length > 0" class="mt-12 flex items-center justify-center gap-2">
            <button
              :disabled="pageNum <= 1"
              @click="pageNum--"
              class="w-10 h-10 flex items-center justify-center rounded-xl border border-gray-200 text-gray-400 hover:border-green-600 hover:text-green-600 transition-all disabled:opacity-30 disabled:cursor-not-allowed"
            >
              <ChevronLeftIcon class="w-5 h-5" />
            </button>
            <button
              v-for="p in totalPages"
              :key="p"
              @click="pageNum = p"
              :class="[
                'w-10 h-10 flex items-center justify-center rounded-xl font-bold transition-all',
                p === pageNum
                  ? 'bg-green-600 text-white shadow-lg shadow-green-100'
                  : 'bg-white text-gray-500 border border-gray-200 hover:border-green-600 hover:text-green-600'
              ]"
            >
              {{ p }}
            </button>
            <button
              :disabled="pageNum >= totalPages"
              @click="pageNum++"
              class="w-10 h-10 flex items-center justify-center rounded-xl border border-gray-200 text-gray-400 hover:border-green-600 hover:text-green-600 transition-all disabled:opacity-30 disabled:cursor-not-allowed"
            >
              <ChevronRightIcon class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { foodApi, type FoodDTO } from '@/api/food'
import { mapFoodToCard, type FoodCardItem } from '@/utils/mapping'
import { debounce } from '@/utils/debounce'
import FoodCard from '@/components/ui/FoodCard.vue'
import { useToast } from '@/composables/useToast'
import {
  Search as SearchIcon,
  SlidersHorizontal as SlidersHorizontalIcon,
  ChevronDown as ChevronDownIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon,
  X as XIcon,
  LayoutGrid as LayoutGridIcon,
  List as ListIcon,
} from 'lucide-vue-next'

const { show: showToast } = useToast()

const searchQuery = ref('')
const selectedCategory = ref('全部')
const showFilters = ref(false)
const priceRange = ref([0, 100])
const sortBy = ref('默认排序')
const selectedExpiryRanges = ref<string[]>([])
const selectedNutritionTags = ref<string[]>([])

const sortOptions = ['默认排序', '价格最低', '保质期最长', '库存最多']
const expiryRanges = ['1-3天', '3-7天', '7天以上']
const nutritionTags = ['高蛋白', '低脂', '无糖', '高纤维', '低钠']

function toggleNutritionTag(tag: string) {
  const idx = selectedNutritionTags.value.indexOf(tag)
  if (idx === -1) {
    selectedNutritionTags.value.push(tag)
  } else {
    selectedNutritionTags.value.splice(idx, 1)
  }
}

function expiryRangeToDays(range: string): { min: number; max: number } | null {
  if (range === '1-3天') return { min: 1, max: 3 }
  if (range === '3-7天') return { min: 4, max: 7 }
  if (range === '7天以上') return { min: 8, max: 9999 }
  return null
}

// API data
const apiFoods = ref<FoodCardItem[]>([])
const categories = ref<{ id: number; name: string }[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12
const loading = ref(false)

const categoryTabs = computed(() => {
  if (categories.value.length > 0) {
    return ['全部', ...categories.value.map(c => c.name)]
  }
  return ['全部', '新鲜果蔬', '肉禽蛋奶', '休闲零食', '粮油调味', '速食冷冻', '酒水饮料']
})

async function loadFoods() {
  loading.value = true
  try {
    const catId = selectedCategory.value === '全部'
      ? undefined
      : categories.value.find(c => c.name === selectedCategory.value)?.id

    // Compute expiry days range from selected checkboxes
    let minExpiryDays: number | undefined
    let maxExpiryDays: number | undefined
    if (selectedExpiryRanges.value.length > 0) {
      const allDays = selectedExpiryRanges.value
        .map(r => expiryRangeToDays(r))
        .filter((d): d is { min: number; max: number } => d !== null)
      if (allDays.length > 0) {
        minExpiryDays = Math.min(...allDays.map(d => d.min))
        maxExpiryDays = Math.max(...allDays.map(d => d.max))
      }
    }

    const result = await foodApi.list({
      pageNum: pageNum.value,
      pageSize,
      keyword: searchQuery.value || undefined,
      categoryId: catId,
      minPrice: priceRange.value[0] > 0 ? priceRange.value[0] : undefined,
      maxPrice: priceRange.value[1] > 0 ? priceRange.value[1] : undefined,
      minExpiryDays,
      maxExpiryDays,
    })
    apiFoods.value = result.records.map(r => mapFoodToCard(r))
    total.value = result.total
  } catch {
    apiFoods.value = []
    total.value = 0
    showToast('加载食品列表失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const cats = await foodApi.getCategories()
    categories.value = cats.map(c => ({ id: c.categoryId, name: c.categoryName }))
  } catch {
    categories.value = []
  }
}

const filteredItems = computed(() => {
  let items = apiFoods.value
  if (selectedNutritionTags.value.length > 0) {
    items = items.filter(item =>
      selectedNutritionTags.value.every(tag => item.tags && item.tags.includes(tag))
    )
  }
  switch (sortBy.value) {
    case '价格最低':
      items = [...items].sort((a, b) => a.price - b.price)
      break
    case '保质期最长':
      items = [...items].sort((a, b) => b.expiryDays - a.expiryDays)
      break
    case '库存最多':
      items = [...items].sort((a, b) => b.stock - a.stock)
      break
    default:
      break
  }
  return items
})

const applyFilters = () => {
  if (priceRange.value[0] > priceRange.value[1] && priceRange.value[1] > 0) {
    showToast('最低价格不能大于最高价格', 'warning')
    return
  }
  showFilters.value = false
  pageNum.value = 1
  loadFoods()
}

const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

const resetFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = '全部'
  priceRange.value = [0, 100]
  selectedExpiryRanges.value = []
  selectedNutritionTags.value = []
  pageNum.value = 1
  loadFoods()
}

// Debounced search: wait 400ms after user stops typing before API call
const debouncedLoadFoods = debounce(loadFoods, 400)

// Reload on search (debounced) and other filters (immediate)
watch(searchQuery, () => {
  pageNum.value = 1
  debouncedLoadFoods()
})
watch([selectedCategory, () => [...priceRange.value], selectedExpiryRanges], () => {
  pageNum.value = 1
  loadFoods()
})
watch(pageNum, () => loadFoods())

onMounted(() => {
  loadCategories()
  loadFoods()
})
</script>
