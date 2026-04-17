import { api } from './index'
import type { PageResult } from './order'

export interface AppealDTO {
  afterSaleId: number
  orderId: number
  orderNo?: string
  userId: number
  reasonType: string
  reasonDesc: string
  evidenceImg?: string
  handleStatus: string
  handleResult?: string
  applyTime?: string
  handleTime?: string
}

export interface AppealCreateDTO {
  orderId: number
  reasonType: string
  reasonDesc: string
  evidenceImg?: string
}

export const appealApi = {
  submit: (data: AppealCreateDTO) =>
    api.post<AppealDTO>('/after-sale', data),

  getById: (id: number) =>
    api.get<AppealDTO>(`/after-sale/${id}`),

  listMy: (params?: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<AppealDTO>>('/after-sale/my', params || {}),
}
