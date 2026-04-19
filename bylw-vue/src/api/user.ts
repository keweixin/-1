import { api } from './index'

export interface UserDTO {
  userId: number
  username: string
  phone: string
  email?: string
  role: string
  roleType?: number
  points?: number
  createTime?: string
  status?: number
}

export interface UpdateUserInfoDTO {
  nickname?: string
  gender?: string
  phone?: string
  email?: string
  avatar?: string
  address?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const userApi = {
  list: (params: { pageNum?: number; pageSize?: number; keyword?: string }) =>
    api.get<PageResult<UserDTO>>('/user/list', params),
  getById: (id: number) => api.get<UserDTO>(`/user/${id}`),
  update: (data: UpdateUserInfoDTO) => api.put<UserDTO>('/auth/userinfo', data),
  updateStatus: (id: number, status: number) => api.put<boolean>(`/user/status/${id}?status=${status}`),
  delete: (id: number) => api.delete<boolean>(`/user/${id}`),
  count: () => api.get<number>('/user/count'),
}
