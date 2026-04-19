import { api } from './index'

export interface FoodReview {
  reviewId: number
  foodId: number
  userId: number
  content: string
  rating: number
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const reviewApi = {
  addReview: (data: { foodId: number; content: string; rating: number }) =>
    api.post<FoodReview>('/review', data),
  getReviewsByFoodId: (foodId: number, params?: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<FoodReview>>(`/review/food/${foodId}`, params),
  listAllReviews: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<FoodReview>>('/review/list', params),
  deleteReview: (id: number) => api.delete<boolean>(`/review/${id}`),
}
