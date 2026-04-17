import { api } from './index'

export interface CourierTask {
  orderId: number
  orderNo: string
  courierName?: string
  courierPhone?: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  orderStatus: string
  payStatus: string
  totalAmount: number
  createTime: string
  finishTime?: string
  deliveryStatus?: string
}

export interface HallOrder {
  orderId: number
  orderNo: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  totalAmount: number
  createTime: string
  deliveryStatus?: string
  preAssigned?: boolean
}

export interface CourierProfile {
  nickname: string
  phone: string
  courierStatus?: number
}

export const courierApi = {
  getTasks(status?: string) {
    return api.get<CourierTask[]>('/delivery/courier/tasks', { status })
  },
  getHall() {
    return api.get<HallOrder[]>('/delivery/hall')
  },
  getProfile() {
    return api.get<CourierProfile>('/delivery/courier/profile')
  },
  claimOrder(orderId: number) {
    return api.post<boolean>(`/delivery/claim/${orderId}`)
  },
  updateTaskStatus(orderId: number, status: string) {
    return api.put<boolean>(`/delivery/courier/status/${orderId}?status=${encodeURIComponent(status)}`)
  },
}
