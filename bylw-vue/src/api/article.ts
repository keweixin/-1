import { api } from './index'

export interface Article {
  articleId: number
  title: string
  summary: string
  content: string
  coverImg: string
  publisherId: number
  readCount: number
  status: number
  publishTime: string
}

export interface Recipe {
  recipeId: number
  title: string
  summary: string
  content: string
  suitablePeople: string
  coverImg: string
  publisherId: number
  collectCount: number
  status: number
  publishTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const articleApi = {
  listArticles: (params: { pageNum?: number; pageSize?: number; keyword?: string }) =>
    api.get<PageResult<Article>>('/article/list', params),
  getArticle: (id: number) => api.get<Article>(`/article/${id}`),
  saveArticle: (data: Partial<Article>) => api.post<Article>('/article', data),
  updateArticle: (data: Partial<Article>) => api.put<Article>('/article', data),
  deleteArticle: (id: number) => api.delete<boolean>(`/article/${id}`),
  listRecipes: (params: { pageNum?: number; pageSize?: number; keyword?: string; suitablePeople?: string }) =>
    api.get<PageResult<Recipe>>('/article/recipe/list', params),
  getRecipe: (id: number) => api.get<Recipe>(`/article/recipe/${id}`),
  saveRecipe: (data: Partial<Recipe>) => api.post<Recipe>('/article/recipe', data),
  updateRecipe: (data: Partial<Recipe>) => api.put<Recipe>('/article/recipe', data),
  deleteRecipe: (id: number) => api.delete<boolean>(`/article/recipe/${id}`),
  toggleFavorite: (recipeId: number) => api.post<boolean>(`/article/favorite/${recipeId}`),
  getMyFavorites: () => api.get<Recipe[]>('/article/favorite/my'),
}
