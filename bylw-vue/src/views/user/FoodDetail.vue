<template>
  <div class="min-h-screen bg-gray-50 pb-20">
    <div class="max-w-7xl mx-auto px-4 py-8">
      <!-- Breadcrumbs -->
      <nav class="flex items-center gap-2 text-sm text-gray-400 mb-8">
        <router-link to="/" class="hover:text-green-600 transition-colors">首页</router-link>
        <ChevronRightIcon class="w-4 h-4" />
        <router-link to="/food-hall" class="hover:text-green-600 transition-colors">临期食品</router-link>
        <ChevronRightIcon class="w-4 h-4" />
        <span class="text-gray-900 font-medium">{{ food.name }}</span>
      </nav>

      <!-- Loading state -->
      <div v-if="loading" class="col-span-2 flex flex-col items-center justify-center py-32">
        <div class="h-12 w-12 border-4 border-green-600 border-t-transparent rounded-full animate-spin mb-4"></div>
        <p class="text-gray-400 font-bold text-sm">商品加载中...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="!foodData" class="col-span-2 flex flex-col items-center justify-center py-32">
        <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4">
          <ShoppingBagIcon class="w-10 h-10 text-gray-300" />
        </div>
        <p class="text-lg font-bold text-gray-900 mb-1">商品不存在或已下架</p>
        <p class="text-sm text-gray-400 mb-6">该商品可能已售罄或被商家下架</p>
        <router-link to="/food-hall" class="bg-green-600 text-white px-6 py-3 rounded-2xl font-bold text-sm hover:bg-green-700 transition-all">
          返回食品大厅
        </router-link>
      </div>

      <!-- Food content -->
      <template v-else>
        <!-- Left: Image Gallery -->
        <div class="space-y-4">
          <div class="aspect-square rounded-[2.5rem] overflow-hidden border border-gray-100 shadow-2xl shadow-black/5 bg-white group/img">
            <img
              :src="currentMainImage"
              :alt="food.name"
              class="w-full h-full object-cover group-hover/img:scale-110 transition-transform duration-700 ease-out"
              referrerPolicy="no-referrer"
              @error="handleMainImageError"
            />
          </div>
          <div class="grid grid-cols-4 gap-4">
            <div v-for="i in 4" :key="i" class="aspect-square rounded-2xl overflow-hidden border-2 border-transparent hover:border-green-600 cursor-pointer transition-all bg-white shadow-sm">
              <img :src="getThumbImage(i)" :alt="`Thumb ${i}`" class="w-full h-full object-cover opacity-60 hover:opacity-100 transition-opacity" referrerPolicy="no-referrer" @error="handleThumbError(i)" />
            </div>
          </div>
        </div>

        <!-- Right: Product Info -->
        <div class="flex flex-col">
          <div class="flex items-center gap-3 mb-6">
            <span class="bg-green-600/10 text-green-600 text-[10px] font-black px-3 py-1.5 rounded-xl uppercase tracking-widest border border-green-600/20">
              {{ food.category }}
            </span>
            <div class="flex items-center gap-1.5 text-xs font-bold text-gray-400">
              <StoreIcon class="w-4 h-4 text-green-600" />
              {{ food.store }}
            </div>
          </div>

          <h1 class="text-4xl font-black text-gray-900 mb-6 leading-tight tracking-tight">{{ food.name }}</h1>

          <div class="flex items-center gap-6 mb-10 bg-white p-6 rounded-[2rem] border border-gray-100 shadow-sm">
            <div class="flex flex-col">
              <span class="text-xs font-bold text-gray-400 line-through mb-1">原价 ¥{{ food.originalPrice.toFixed(2) }}</span>
              <div class="flex items-baseline gap-1">
                <span class="text-xl font-black text-orange-500">¥</span>
                <span class="text-5xl font-black text-orange-500 tracking-tighter">
                  {{ food.price.toFixed(2) }}
                </span>
              </div>
            </div>
            <div class="h-12 w-px bg-gray-100 mx-2"></div>
            <div class="flex flex-col">
              <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1.5">折扣力度</span>
              <div class="flex items-center gap-2">
                <span class="text-2xl font-black text-green-600">{{ discount }}</span>
                <span class="text-xs font-black text-green-600/60 uppercase tracking-widest">OFF</span>
              </div>
            </div>
          </div>

          <!-- Expiry Warning -->
          <div class="bg-red-50 border border-red-100 rounded-[2rem] p-6 mb-10 flex items-start gap-4">
            <div class="w-12 h-12 bg-red-500 rounded-2xl flex items-center justify-center shadow-lg shadow-red-500/20 shrink-0">
              <AlertTriangleIcon class="text-white w-6 h-6" />
            </div>
            <div>
              <p class="text-base font-black text-red-800 mb-1 tracking-tight">临期资产提醒</p>
              <p class="text-sm text-red-600/80 leading-relaxed font-medium">
                该商品保质期仅剩 <span class="font-black underline decoration-2 underline-offset-4">{{ food.expiryDays }} 天</span>。临期食品不退不换，请确认能在保质期内食用完毕，共同减少食物浪费。
              </p>
            </div>
          </div>

          <!-- Nutrition Tags -->
          <div class="mb-10">
            <p class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-4 block">营养标签与认证</p>
            <div class="flex flex-wrap gap-3">
              <span
                v-for="tag in food.tags"
                :key="tag"
                class="px-4 py-2 bg-white border border-gray-100 rounded-2xl text-sm font-black text-gray-600 flex items-center gap-2 shadow-sm hover:border-green-600 transition-all cursor-default"
              >
                <ShieldCheckIcon class="w-4 h-4 text-green-600" />
                {{ tag }}
              </span>
            </div>
          </div>

          <!-- Purchase Controls -->
          <div class="mt-auto space-y-8">
            <div class="flex items-center justify-between bg-gray-50 p-2 rounded-2xl border border-gray-100">
              <div class="flex items-center bg-white shadow-sm rounded-xl p-1 border border-gray-100">
                <button
                  @click="quantity = Math.max(1, quantity - 1)"
                  class="p-2.5 hover:bg-gray-50 text-gray-400 hover:text-green-600 transition-all rounded-lg active:scale-90"
                >
                  <MinusIcon class="w-5 h-5" />
                </button>
                <span class="w-14 text-center font-black text-lg text-gray-900">{{ quantity }}</span>
                <button
                  @click="quantity = Math.min(food.stock, quantity + 1)"
                  class="p-2.5 hover:bg-gray-50 text-gray-400 hover:text-green-600 transition-all rounded-lg active:scale-90"
                >
                  <PlusIcon class="w-5 h-5" />
                </button>
              </div>
              <div class="pr-4 flex flex-col items-end">
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">库存剩余</span>
                <span class="text-sm font-black text-gray-900">{{ food.stock }} <span class="text-xs font-medium text-gray-400">件</span></span>
              </div>
            </div>

            <div class="flex gap-4">
              <button
                :disabled="orderLoading"
                @click="openOrderConfirm"
                class="flex-grow bg-green-600 hover:bg-green-500 disabled:bg-green-300 text-white py-5 rounded-[1.5rem] font-black shadow-2xl shadow-green-600/20 transition-all active:scale-95 flex items-center justify-center gap-3 text-lg uppercase tracking-widest cursor-pointer"
              >
                <span v-if="orderLoading" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                <ShoppingCartIcon v-else class="w-6 h-6" />
                {{ orderLoading ? '下单中...' : '立即下单' }}
              </button>
              <button @click="handleFavorite" :class="[isFavorited ? 'text-red-500 border-red-200 bg-red-50' : 'text-gray-400 bg-white border-gray-200 hover:text-red-500 hover:border-red-100']" class="p-5 rounded-[1.5rem] border transition-all active:scale-95 shadow-sm cursor-pointer">
                <HeartIcon :class="[isFavorited ? 'fill-red-500' : '']" class="w-7 h-7" />
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- Tabs / Details Section -->
      <div class="mt-20">
        <div class="flex items-center gap-8 border-b border-gray-200 mb-8">
          <button
            v-for="(tab, idx) in tabs"
            :key="tab"
            :class="['pb-4 text-sm font-bold transition-all relative', activeTab === idx ? 'text-green-600' : 'text-gray-400 hover:text-gray-600']"
            @click="activeTab = idx"
          >
            {{ tab }}
            <span v-if="activeTab === idx" class="absolute bottom-0 left-0 w-full h-1 bg-green-600 rounded-full"></span>
          </button>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
          <div class="lg:col-span-2 space-y-8">
            <section v-if="activeTab === 0">
              <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                <InfoIcon class="w-5 h-5 text-green-600" />
                商品描述
              </h3>
              <p class="text-gray-600 leading-relaxed">{{ food.description }}</p>
            </section>
            <section v-else-if="activeTab === 1">
              <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                <ShieldCheckIcon class="w-5 h-5 text-green-600" />
                营养成分
              </h3>
              <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
                <p class="text-sm text-gray-600">{{ food.nutrition }}</p>
              </div>
            </section>
            <section v-else-if="activeTab === 2">
              <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                <UsersIcon class="w-5 h-5 text-green-600" />
                适合人群
              </h3>
              <p class="text-sm text-gray-600">{{ food.suitable }}</p>
            </section>
            <section v-else-if="activeTab === 3">
              <h3 class="text-lg font-bold text-gray-900 mb-4">用户评价</h3>

              <div v-if="reviews.length === 0" class="text-center py-12 text-gray-400">
                <p>暂无评价，快来发表第一条评论吧</p>
              </div>
              <div v-else class="space-y-4 mb-6">
                <div v-for="review in reviews" :key="review.reviewId" class="bg-gray-50 rounded-2xl p-5 border border-gray-100">
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-bold text-gray-900">用户{{ review.userId }}</span>
                      <div class="flex items-center gap-0.5">
                        <span v-for="s in 5" :key="s" class="text-xs" :class="s <= review.rating ? 'text-yellow-400' : 'text-gray-300'">&#9733;</span>
                      </div>
                    </div>
                    <span class="text-xs text-gray-400">{{ review.createTime?.replace('T', ' ').slice(0, 16) }}</span>
                  </div>
                  <p class="text-sm text-gray-600">{{ review.content }}</p>
                </div>
              </div>

              <div class="bg-white rounded-2xl border border-gray-100 p-5">
                <h4 class="text-sm font-bold text-gray-900 mb-3">发表评论</h4>
                <textarea v-model="reviewForm.content" rows="3" placeholder="分享您对这款食品的评价..."
                  class="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500 resize-none mb-3" />
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="text-sm text-gray-600">评分：</span>
                    <div class="flex gap-1">
                      <button v-for="s in 5" :key="s" @click="reviewForm.rating = s" class="text-lg"
                        :class="s <= reviewForm.rating ? 'text-yellow-400' : 'text-gray-300'">&#9733;</button>
                    </div>
                  </div>
                  <button class="px-4 py-2 rounded-xl bg-green-600 text-white text-sm font-bold hover:bg-green-700 transition-all disabled:bg-green-300"
                    :disabled="reviewSubmitting" @click="submitReview">
                    {{ reviewSubmitting ? '提交中...' : '提交评论' }}
                  </button>
                </div>
              </div>
            </section>
          </div>

          <!-- Related Recommendations -->
          <aside>
            <h3 class="text-lg font-bold text-gray-900 mb-6">相关推荐</h3>
            <div v-if="relatedLoading" class="flex items-center justify-center py-8 text-gray-400">
              <div class="w-5 h-5 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-2"></div>
              <span class="text-sm">加载中...</span>
            </div>
            <div v-else-if="relatedFoods.length === 0" class="space-y-6">
              <div class="flex items-center gap-4 text-sm text-gray-400">
                <ClockIcon class="w-4 h-4" />
                暂无相关推荐
              </div>
            </div>
            <div v-else class="space-y-6">
              <router-link
                v-for="(item, idx) in relatedFoods"
                :key="item.id"
                :to="`/food/${item.id}`"
                class="flex items-center gap-3 group"
              >
                <img :src="getRelatedImg(item, idx)" :alt="item.name" class="w-14 h-14 rounded-xl object-cover bg-gray-100" referrerPolicy="no-referrer" @error="handleRelatedImgError(idx)" />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-bold text-gray-900 group-hover:text-green-600 transition-colors truncate">{{ item.name }}</p>
                  <p class="text-sm font-black text-orange-500">¥{{ item.price.toFixed(2) }}</p>
                </div>
              </router-link>
            </div>
          </aside>
        </div>
      </div>
    </div>

    <!-- Order Confirmation Modal -->
    <Teleport to="body">
      <div v-if="showOrderConfirm" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="showOrderConfirm = false"></div>
        <div class="relative bg-white rounded-[2rem] shadow-2xl w-full max-w-md p-8 max-h-[90vh] overflow-y-auto">
          <h2 class="text-2xl font-black text-gray-900 mb-6">确认订单</h2>

          <!-- Order Items -->
          <div class="bg-gray-50 rounded-2xl p-4 mb-6">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-3">订单商品</p>
            <div class="flex items-center gap-3">
              <img :src="currentMainImage" :alt="food.name" class="w-14 h-14 rounded-xl object-cover bg-gray-200" referrerPolicy="no-referrer" />
              <div class="flex-1 min-w-0">
                <p class="font-bold text-gray-900 truncate">{{ food.name }}</p>
                <p class="text-sm text-gray-500">x{{ quantity }}</p>
              </div>
              <p class="font-black text-orange-500">¥{{ (food.price * quantity).toFixed(2) }}</p>
            </div>
          </div>

          <!-- Points Section -->
          <div class="bg-gradient-to-r from-orange-50 to-amber-50 rounded-2xl p-4 mb-6 border border-orange-100">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2">
                <GiftIcon class="w-5 h-5 text-orange-500" />
                <p class="text-sm font-bold text-gray-900">使用积分抵扣</p>
              </div>
              <p class="text-sm font-medium text-gray-500">可用: <span class="font-black text-orange-500">{{ pointsBalance.toLocaleString() }}</span> 积分</p>
            </div>

            <div v-if="pointsBalance > 0" class="space-y-3">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">选择使用积分</span>
                <span class="font-black text-orange-500">{{ pointsToUse.toLocaleString() }} 分</span>
              </div>
              <input
                type="range"
                v-model.number="pointsToUse"
                :min="0"
                :max="maxUsablePoints"
                step="100"
                class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-orange-500"
              />
              <div class="flex justify-between text-xs text-gray-400">
                <span>0</span>
                <span>{{ maxUsablePoints.toLocaleString() }}</span>
              </div>
              <div class="flex items-center justify-between text-sm bg-white/60 rounded-xl p-2">
                <span class="text-gray-500">抵扣金额</span>
                <span class="font-black text-green-600">-¥{{ pointsDeductedAmount }}</span>
              </div>
            </div>
            <div v-else class="text-center text-sm text-gray-500 py-2">
              暂无可用积分，登录后积累更多积分
            </div>
          </div>

          <!-- Price Summary -->
          <div class="space-y-2 mb-6">
            <div class="flex justify-between text-sm text-gray-500">
              <span>商品总价</span>
              <span>¥{{ (food.price * quantity).toFixed(2) }}</span>
            </div>
            <div class="flex justify-between text-sm text-green-600" v-if="pointsDeductedAmount > 0">
              <span>积分抵扣</span>
              <span>-¥{{ pointsDeductedAmount }}</span>
            </div>
            <div class="flex justify-between text-lg font-black text-gray-900 pt-2 border-t border-gray-100">
              <span>应付金额</span>
              <span class="text-orange-500">¥{{ finalAmount }}</span>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex gap-3">
            <button
              @click="showOrderConfirm = false"
              class="flex-1 py-3.5 rounded-xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all cursor-pointer"
            >
              取消
            </button>
            <button
              :disabled="orderLoading"
              @click="handleOrder"
              class="flex-1 py-3.5 rounded-xl bg-green-600 text-white font-black hover:bg-green-500 transition-all disabled:bg-gray-300 cursor-pointer flex items-center justify-center gap-2"
            >
              <span v-if="orderLoading" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              <span v-else>确认下单</span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { foodApi } from '@/api/food'
import { orderApi, type OrderCreateDTO } from '@/api/order'
import { recommendApi } from '@/api/recommend'
import { authApi } from '@/api/auth'
import { reviewApi, type FoodReview } from '@/api/review'
import type { FoodDTO } from '@/api/food'
import { mapFoodToCard, type FoodCardItem, getCategoryImage } from '@/utils/mapping'
import { getFoodImage } from '@/utils/images'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
const toast = useToast()
const authStore = useAuthStore()
import {
  ChevronRight as ChevronRightIcon,
  ShoppingCart as ShoppingCartIcon,
  ShoppingBag as ShoppingBagIcon,
  Heart as HeartIcon,

  Store as StoreIcon,
  AlertTriangle as AlertTriangleIcon,
  ShieldCheck as ShieldCheckIcon,
  Minus as MinusIcon,
  Plus as PlusIcon,
  Info as InfoIcon,
  Users as UsersIcon,
  Clock as ClockIcon,
  Gift as GiftIcon,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const quantity = ref(1)
const activeTab = ref(0)
const tabs = ['商品详情', '营养说明', '适合人群', '用户评价']
const relatedFoods = ref<FoodCardItem[]>([])
const relatedLoading = ref(false)
const orderLoading = ref(false)
const reviews = ref<FoodReview[]>([])
const reviewSubmitting = ref(false)
const reviewForm = ref({ content: '', rating: 5 })
const showOrderConfirm = ref(false)
const pointsBalance = ref(0)
const pointsToUse = ref(0)
const mainImageError = ref(false)
const thumbErrorFlags = ref([false, false, false, false])
const isFavorited = ref(false)

const currentMainImage = computed(() => {
  if (mainImageError.value) {
    return getFoodImage('', food.value.category)
  }
  return getFoodImage(food.value.image, food.value.category)
})

function handleMainImageError() {
  mainImageError.value = true
}

function handleThumbError(index: number) {
  thumbErrorFlags.value[index - 1] = true
}

function getThumbImage(index: number): string {
  if (thumbErrorFlags.value[index - 1]) {
    return getFoodImage('', food.value.category)
  }
  return getFoodImage(food.value.image, food.value.category)
}

const relatedImgErrorFlags = ref<Record<number, boolean>>({})

function handleRelatedImgError(idx: number) {
  relatedImgErrorFlags.value[idx] = true
}

function getRelatedImg(item: FoodCardItem, idx: number): string {
  if (relatedImgErrorFlags.value[idx]) {
    return getFoodImage()
  }
  return getFoodImage(item.image)
}

// API-backed food data
const foodData = ref<FoodDTO | null>(null)
const loading = ref(true)

async function loadFood() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    foodData.value = await foodApi.getById(id)
    // 记录浏览行为
    if (authStore.userId) {
      recommendApi.recordBehavior({ userId: authStore.userId, targetType: 'food', targetId: id, behaviorType: 'view' }).catch(() => {})
    }
  } catch {
    foodData.value = null
  } finally {
    loading.value = false
  }
}

async function loadRelatedFoods() {
  relatedLoading.value = true
  try {
    const result = await recommendApi.getFoods(4)
    const id = Number(route.params.id)
    relatedFoods.value = (result.foods || []).map((r: FoodDTO) => mapFoodToCard(r)).filter((f: FoodCardItem) => f.id !== id).slice(0, 4)
  } catch {
    relatedFoods.value = []
  } finally {
    relatedLoading.value = false
  }
}

async function openOrderConfirm() {
  if (!foodData.value) return
  if (quantity.value > (foodData.value.stockQty || 0)) {
    toast.show('库存不足，当前库存：' + foodData.value.stockQty, 'warning')
    return
  }
  const user = await authApi.getUserInfo().catch(() => null)
  if (!user) {
    toast.show('请先登录！', 'warning')
    router.push('/login')
    return
  }
  if (!user.address || user.address.trim() === '') {
    toast.show('请先在个人中心填写收货地址！', 'warning')
    router.push('/profile')
    return
  }
  if (!user.phone || user.phone.trim() === '') {
    toast.show('请先在个人中心填写手机号码！', 'warning')
    router.push('/profile')
    return
  }
  try {
    const { pointsApi: pApi } = await import('@/api/points')
    const balanceResult = await pApi.getBalance().catch(() => ({ balance: 0 }))
    pointsBalance.value = balanceResult.balance ?? 0
  } catch {
    pointsBalance.value = 0
  }
  pointsToUse.value = 0
  showOrderConfirm.value = true
}

async function handleOrder() {
  if (!foodData.value) return
  orderLoading.value = true
  try {
    const id = Number(route.params.id)
    if (authStore.userId) {
      recommendApi.recordBehavior({ userId: authStore.userId, targetType: 'food', targetId: id, behaviorType: 'purchase' }).catch(() => {})
    }
    const user = await authApi.getUserInfo()
    const orderData: OrderCreateDTO = {
      receiverName: user.nickname || user.username || '顾客',
      receiverPhone: user.phone || '',
      receiverAddress: user.address || '',
      items: [{ foodId: id, quantity: quantity.value, price: foodData.value.discountPrice, foodName: foodData.value.foodName, subtotal: Number((foodData.value.discountPrice * quantity.value).toFixed(2)) }],
    }
    if (pointsToUse.value > 0 && pointsBalance.value > 0) {
      orderData.pointsToUse = pointsToUse.value
    }
    await orderApi.create(orderData)
    showOrderConfirm.value = false
    toast.show('下单成功！即将跳转到支付页面...', 'success')
    router.push('/orders')
  } catch (e: unknown) {
    toast.show('下单失败：' + (e instanceof Error ? e.message : '请先登录'), 'error')
  } finally {
    orderLoading.value = false
  }
}

async function loadFavoriteStatus() {
  if (!authStore.userId) return
  const id = Number(route.params.id)
  try {
    const res = await recommendApi.checkFavorite('food', id)
    isFavorited.value = res === true
  } catch {
    isFavorited.value = false
  }
}

async function handleFavorite() {
  if (!authStore.userId) {
    toast.show('请先登录', 'warning')
    return
  }
  const id = Number(route.params.id)
  try {
    const res = await recommendApi.toggleFavorite('food', id)
    isFavorited.value = res === true
    toast.show(isFavorited.value ? '已收藏' : '已取消收藏', 'success')
  } catch (e: unknown) {
    toast.show('操作失败：' + (e instanceof Error ? e.message : '请先登录'), 'error')
  }
}

async function loadReviews() {
  try {
    const id = Number(route.params.id)
    const result = await reviewApi.getReviewsByFoodId(id, { pageNum: 1, pageSize: 50 })
    reviews.value = result.records
  } catch {
    reviews.value = []
  }
}

async function submitReview() {
  if (!reviewForm.value.content.trim()) {
    toast.show('请输入评论内容', 'error')
    return
  }
  reviewSubmitting.value = true
  try {
    const id = Number(route.params.id)
    await reviewApi.addReview({ foodId: id, content: reviewForm.value.content.trim(), rating: reviewForm.value.rating })
    reviewForm.value = { content: '', rating: 5 }
    toast.show('评论发表成功', 'success')
    // 记录评论行为
    if (authStore.userId) {
      recommendApi.recordBehavior({ userId: authStore.userId, targetType: 'food', targetId: id, behaviorType: 'comment' }).catch(() => {})
    }
    await loadReviews()
  } catch (e: unknown) {
    toast.show(e instanceof Error ? e.message : '评论失败', 'error')
  } finally {
    reviewSubmitting.value = false
  }
}


onMounted(() => { loadFood(); loadRelatedFoods(); loadFavoriteStatus(); loadReviews() })
watch(() => route.params.id, () => { loadFood(); loadRelatedFoods(); loadFavoriteStatus(); loadReviews() })

function calcExpiryDays(expireDate: string): number {
  if (!expireDate) return 0
  const exp = new Date(expireDate)
  const now = new Date()
  const diff = exp.getTime() - now.getTime()
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

const food = computed(() => {
  if (!foodData.value) {
    return {
      name: '加载中...',
      category: '-',
      store: '-',
      price: 0,
      originalPrice: 0,
      expiryDays: 0,
      stock: 0,
      image: getFoodImage(getCategoryImage()),
      tags: [],
      description: '',
      nutrition: '',
      suitable: '',
    }
  }
  const dto = foodData.value
  const days = calcExpiryDays(dto.expireDate)
  const tags: string[] = []
  if (dto.nutritionDesc) {
    const n = dto.nutritionDesc.toLowerCase()
    if (n.includes('蛋白')) tags.push('高蛋白')
    if (n.includes('低脂') || n.includes('低卡')) tags.push('低脂')
    if (n.includes('纤维')) tags.push('高纤维')
  }
  return {
    name: dto.foodName,
    category: dto.categoryName,
    store: '官方商城',
    price: dto.discountPrice,
    originalPrice: dto.originPrice,
    expiryDays: days,
    stock: dto.stockQty,
    image: getFoodImage(dto.coverImg, dto.categoryName),
    tags,
    description: dto.description,
    nutrition: dto.nutritionDesc,
    suitable: dto.suitablePeople,
  }
})

const discount = computed(() => {
  if (!food.value.originalPrice) return 0
  return Math.round((1 - food.value.price / food.value.originalPrice) * 100)
})

const maxUsablePoints = computed(() => {
  const maxByAmount = Math.floor(food.value.price * quantity.value * 100)
  return Math.min(pointsBalance.value, maxByAmount)
})

const pointsDeductedAmount = computed(() => {
  return (pointsToUse.value * 0.01)
})

const finalAmount = computed(() => {
  const total = food.value.price * quantity.value
  const deducted = pointsToUse.value * 0.01
  return Math.max(0, total - deducted).toFixed(2)
})
</script>
