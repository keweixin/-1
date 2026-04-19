import { api } from './index'

export interface FoodDTO {
  foodId: number
  merchantId: number
  categoryId: number
  categoryName: string
  foodName: string
  originPrice: number
  discountPrice: number
  stockQty: number
  expireDate: string
  nutritionDesc: string
  tasteDesc: string
  suitablePeople: string
  allergenInfo: string
  coverImg: string
  description: string
  status: number
  createTime: string
  matchScore?: number
  cfScore?: number
  featureScore?: number
  matchReason?: string
  matchedTags?: string[]
}

export interface FoodCategory {
  categoryId: number
  categoryName: string
  description: string
  sortNo: number
  status: number
}

export interface FoodTag {
  tagId: number
  tagName: string
  tagType: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface FoodListParams {
  [key: string]: string | number | undefined
  pageNum?: number
  pageSize?: number
  keyword?: string
  categoryId?: number
  status?: number
  minPrice?: number
  maxPrice?: number
  minExpiryDays?: number
  maxExpiryDays?: number
}

export const foodApi = {
  list: (params: FoodListParams) => api.get<PageResult<FoodDTO>>('/food/list', params),
  adminList: (params: FoodListParams) => api.get<PageResult<FoodDTO>>('/food/admin/list', params),
  getById: (id: number) => api.get<FoodDTO>(`/food/${id}`),
  getCategories: () => api.get<FoodCategory[]>('/food/categories'),
  getTags: () => api.get<FoodTag[]>('/food/tags'),
  save: (data: Partial<FoodDTO>) => api.post<FoodDTO>('/food', data),
  update: (data: Partial<FoodDTO>) => api.put<FoodDTO>('/food', data),
  delete: (id: number) => api.delete<boolean>(`/food/${id}`),
  updateStatus: (id: number, status: number) => api.put<boolean>(`/food/status/${id}?status=${status}`),
  saveCategory: (data: Partial<FoodCategory>) => api.post<FoodCategory>('/food/categories', data),
  updateCategory: (data: Partial<FoodCategory>) => api.put<FoodCategory>('/food/categories', data),
  deleteCategory: (id: number) => api.delete<boolean>(`/food/categories/${id}`),
}
