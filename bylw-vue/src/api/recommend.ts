import { api } from './index'
import type { FoodDTO } from './food'

export interface AdminStatsDTO {
  foodCount: number
  orderCount: number
  userCount: number
  appealCount: number
  postCount: number
  pointsExchange: number
  inactiveStoreCount: number
  lowStockFoodCount: number
  pendingPostCount: number
  foodTrendRate: number
  orderTrendRate: number
  userTrendRate: number
  appealTrendRate: number
  postTrendRate: number
  pointsExchangeTrendRate: number
  trendData: Array<{ name: string; food: number; orders: number }>
  categoryData: Array<{ name: string; value: number }>
  foodSavedKg: number
  totalPointsEarned: number
  avgOrderValue: number
  completedOrders: number
  wasteIndex: number
  foodSavedTrendRate: number
  wasteIndexTrendRate: number
  pointsTrendRate: number
}

export interface RecommendResult {
  foods: FoodDTO[]
  candidateCount: number
  healthFilteredCount: number
  tasteMatchedCount: number
  finalCount: number
  coldStart: boolean
}

export const recommendApi = {
  getAdminStats: () => api.get<AdminStatsDTO>('/recommend/stats'),
  getFoods: (limit?: number) => api.get<RecommendResult>('/recommend/foods', { limit }),
  getArticles: (limit?: number) => api.get<unknown[]>('/recommend/articles', { limit }),
  getRecipes: (limit?: number) => api.get<unknown[]>('/recommend/recipes', { limit }),
  recordBehavior: (data: { userId: number; targetType: string; targetId: number; behaviorType: string }) =>
    api.post('/recommend/behavior', data),
  checkFavorite: (targetType: string, targetId: number) =>
    api.get<boolean>('/recommend/favorite/check', { targetType, targetId }),
  toggleFavorite: (targetType: string, targetId: number) =>
    api.post<boolean>('/recommend/favorite/toggle', { targetType, targetId }),
}
