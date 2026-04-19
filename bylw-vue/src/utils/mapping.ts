import type { FoodDTO } from '@/api/food'
import type { OrderDTO } from '@/api/order'
import { getFoodCategoryImage } from '@/utils/images'

// FoodCard props shape (matches FoodCard.vue defineProps)
export interface FoodCardItem {
  id: string | number
  name: string
  image: string
  price: number
  originalPrice: number
  expiryDays: number
  stock: number
  store: string
  tags: string[]
  matchScore?: number
  matchReason?: string
  matchedTags?: string[]
}

// Map API FoodDTO to FoodCardItem
export function mapFoodToCard(dto: FoodDTO): FoodCardItem {
  const daysLeft = calcExpiryDays(dto.expireDate)
  const tags = buildFoodTags(dto)
  return {
    id: dto.foodId,
    name: dto.foodName,
    image: dto.coverImg || getCategoryImage(dto.categoryName),
    price: dto.discountPrice,
    originalPrice: dto.originPrice,
    expiryDays: daysLeft,
    stock: dto.stockQty,
    store: '官方商城',
    tags,
    matchScore: dto.matchScore,
    matchReason: dto.matchReason,
    matchedTags: dto.matchedTags,
  }
}

// Category-based local placeholder images
export function getCategoryImage(categoryName?: string): string {
  return getFoodCategoryImage(categoryName)
}

function calcExpiryDays(expireDate: string): number {
  if (!expireDate) return 0
  const exp = new Date(expireDate)
  const now = new Date()
  const diff = exp.getTime() - now.getTime()
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

function buildFoodTags(dto: FoodDTO): string[] {
  const tags: string[] = []
  if (dto.nutritionDesc) {
    const nutrition = dto.nutritionDesc.toLowerCase()
    if (nutrition.includes('蛋白')) tags.push('高蛋白')
    if (nutrition.includes('低脂') || nutrition.includes('低卡')) tags.push('低脂')
    if (nutrition.includes('纤维')) tags.push('高纤维')
    if (nutrition.includes('钙')) tags.push('高钙')
    if (nutrition.includes('omega') || nutrition.includes('omega-3')) tags.push('Omega-3')
  }
  if (dto.allergenInfo && dto.allergenInfo !== '无') {
    // Skip allergens from tags to keep UI clean
  }
  return tags.length > 0 ? tags : ['优质']
}

// Order row for Orders.vue table
export interface OrderRow {
  id: string
  totalAmount: number
  orderStatus: string
  payStatus: string
  createTime: string
  firstItemName: string
  firstItemImg: string
  itemCount: number
}

export function mapOrderToRow(dto: OrderDTO): OrderRow {
  const firstItem = dto.items?.[0]
  return {
    id: dto.orderNo || String(dto.orderId),
    totalAmount: dto.totalAmount,
    orderStatus: dto.orderStatus,
    payStatus: dto.payStatus,
    createTime: dto.createTime,
    firstItemName: firstItem?.foodName || '-',
    firstItemImg: '',
    itemCount: dto.items?.length || 0,
  }
}
