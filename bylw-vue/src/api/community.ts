import { api } from './index'

export interface Post {
  postId: number
  userId: number
  title: string
  content: string
  imgList: string
  likeCount: number
  commentCount: number
  auditStatus: string
  recommended?: number
  createTime: string
}

export interface Comment {
  commentId: number
  postId: number
  userId: number
  content: string
  status: number
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const communityApi = {
  publish: (data: Partial<Post>) => api.post<Post>('/community/post', data),
  getPost: (id: number) => api.get<Post>(`/community/post/${id}`),
  listPosts: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<Post>>('/community/post/list', params),
  adminListPosts: (params: { pageNum?: number; pageSize?: number; auditStatus?: string }) =>
    api.get<PageResult<Post>>('/community/post/admin/list', params),
  auditPost: (id: number, status: string) =>
    api.put<boolean>(`/community/post/audit/${id}?status=${encodeURIComponent(status)}`),
  deletePost: (id: number) => api.delete<boolean>(`/community/post/${id}`),
  toggleRecommend: (id: number) => api.put<boolean>(`/community/post/recommend/${id}`),
  likePost: (id: number) => api.post<boolean>(`/community/post/like/${id}`),
  unlikePost: (id: number) => api.delete<boolean>(`/community/post/like/${id}`),
  comment: (data: Partial<Comment>) => api.post<Comment>('/community/comment', data),
  getComments: (postId: number, params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<Comment>>(`/community/comment/${postId}`, params),
  adminListComments: (params: { pageNum?: number; pageSize?: number }) =>
    api.get<PageResult<Comment>>('/community/comment/admin/list', params),
  deleteComment: (id: number) => api.delete<boolean>(`/community/comment/${id}`),
}
