import { api } from './index'

export interface PointsGoods {
  goodsId: number
  goodsName: string
  pointCost: number
  stockQty: number
  coverImg: string
  description: string
  status: number
}

export interface PointsRecord {
  recordId: number
  userId: number
  changeType: string
  changeValue: number
  sourceType: string
  remark: string
  createTime: string
}

export interface PointsExchange {
  exchangeId: number
  goodsId: number
  userId: number
  exchangeStatus: string
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const pointsApi = {
  getBalance: () => api.get<{ balance: number }>('/points/balance'),
  getHistory: () => api.get<PointsRecord[]>('/points/history'),
  listAllHistory: (params?: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<PointsRecord>>('/points/history/all', params),
  listGoods: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<PointsGoods>>('/points/goods', params),
  saveGoods: (data: Partial<PointsGoods>) => api.post<PointsGoods>('/points/goods', data),
  updateGoods: (data: Partial<PointsGoods>) => api.put<PointsGoods>('/points/goods', data),
  deleteGoods: (id: number) => api.delete<boolean>(`/points/goods/${id}`),
  exchange: (goodsId: number) => api.post<PointsExchange>('/points/exchange', { goodsId }),
  signIn: () => api.post<{ success: boolean; points: number }>('/points/sign-in', {}),
  listExchanges: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<PointsExchange>>('/points/exchange/list', params),
}
