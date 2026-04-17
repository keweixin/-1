import { api } from './index'
import type { PageResult, FoodDTO } from './food'
import type { OrderDTO } from './order'

export interface MerchantStats {
  totalFoods: number
  activeFoods: number
  merchantId: number
}

export const merchantApi = {
  getFoods: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<FoodDTO>>('/merchant/foods', params),
  getOrders: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<OrderDTO>>('/merchant/orders', params),
  getStats: () => api.get<MerchantStats>('/merchant/stats'),
  saveFood: (data: Partial<FoodDTO>) => api.post<FoodDTO>('/food', data),
  updateFood: (data: Partial<FoodDTO>) => api.put<FoodDTO>('/food', data),
  acceptOrder: (orderId: number) => api.put<boolean>(`/merchant/orders/accept/${orderId}`),
}
