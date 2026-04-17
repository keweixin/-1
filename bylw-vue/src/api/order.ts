import { api } from './index'

export interface FoodDTO {
  foodId: number
  foodName: string
  originPrice: number
  discountPrice: number
  expireDate: string
  nutritionDesc: string
  coverImg: string
}

export interface OrderItemDTO {
  itemId: number
  foodId: number
  foodName: string
  price: number
  quantity: number
  subtotal: number
}

export interface DeliveryDTO {
  deliveryId: number
  courierName: string
  courierPhone: string
  deliveryStatus: string
  dispatchTime: string
  finishTime: string
}

export interface OrderDTO {
  orderId: number
  orderNo: string
  userId: number
  username?: string
  totalAmount: number
  orderStatus: string
  payStatus: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
  createTime: string
  payTime: string
  pointsUsed?: number
  pointsDeducted?: number
  items: OrderItemDTO[]
  delivery?: DeliveryDTO
}

export interface OrderItemCreateDTO {
  foodId: number
  quantity: number
  price: number
  foodName: string
  subtotal: number
}

export interface OrderCreateDTO {
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
  pointsToUse?: number
  items: OrderItemCreateDTO[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const orderApi = {
  create: (data: OrderCreateDTO) => api.post<OrderDTO>('/order', data),
  getById: (id: number) => api.get<OrderDTO>(`/order/${id}`),
  listMy: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<OrderDTO>>('/order/my', params),
  listAll: (params: { pageNum?: number; pageSize?: number; status?: string }) =>
    api.get<PageResult<OrderDTO>>('/order/list', params),
  updateStatus: (id: number, status: string) =>
    api.put<boolean>(`/order/status/${id}?status=${status}`),
  pay: (id: number) => api.put<boolean>(`/order/pay/${id}`),
  mockPay: async (id: number) => {
    try {
      return await api.put<boolean>(`/order/mock-pay/${id}`)
    } catch (error: unknown) {
      if (error instanceof Error && error.message.includes('No static resource')) {
        return api.put<boolean>(`/order/pay/${id}`)
      }
      throw error
    }
  },
  cancel: (id: number) => api.put<boolean>(`/order/cancel/${id}`),
  getRecent: () => api.get<OrderDTO[]>('/order/recent'),
  getExpiryReminder: (hoursAhead = 24) =>
    api.get<FoodDTO[]>(`/order/expiry-reminder?hoursAhead=${hoursAhead}`),
}
