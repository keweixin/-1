import { api } from './index'

export interface FoodProfile {
  profileId?: number
  userId?: number
  tastePreference?: string
  allergenInfo?: string
  chronicDisease?: string
  tabooInfo?: string
  remark?: string
  updateTime?: string
}

export interface FoodProfileDTO {
  tastePreference?: string
  allergenInfo?: string
  chronicDisease?: string
  tabooInfo?: string
  remark?: string
}

export const profileApi = {
  get: () => api.get<FoodProfile>('/profile'),
  save: (data: FoodProfileDTO) => api.post<FoodProfile>('/profile', data),
}
