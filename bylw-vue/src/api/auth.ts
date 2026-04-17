import { api } from './index'

export interface LoginDTO {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  role: string
  roleType: number
}

export interface RegisterDTO {
  username: string
  password: string
  nickname?: string
  phone?: string
  email?: string
  roleType?: number
}

export interface User {
  userId: number
  username: string
  nickname: string
  gender: string
  phone: string
  email: string
  avatar: string
  address: string
  role: string
  roleType: number
  createTime?: string
}

export interface UpdateUserInfoDTO {
  nickname?: string
  gender?: string
  phone?: string
  email?: string
  avatar?: string
  address?: string
}

export const authApi = {
  login: (data: LoginDTO) => api.post<LoginResponse>('/auth/login', data),
  register: (data: RegisterDTO) => api.post<User>('/auth/register', data),
  getUserInfo: () => api.get<User>('/auth/userinfo'),
  updateUserInfo: (data: UpdateUserInfoDTO) => api.put<User>('/auth/userinfo', data),
  resetPassword: (data: { username: string; phone: string; newPassword: string }) =>
    api.post<boolean>('/auth/reset-password', data),
  logout: () => api.post<void>('/auth/logout'),
}
