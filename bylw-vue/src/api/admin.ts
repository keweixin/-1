import { api } from './index'
import { orderApi, type OrderDTO } from './order'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

interface NoticeEntity {
  noticeId: number
  title: string
  content: string
  publisherId?: number
  status: number
  publishTime?: string
}

interface BannerEntity {
  bannerId: number
  title: string
  imgUrl: string
  linkUrl: string
  sortNo: number
  status: number
}

interface FriendLinkEntity {
  linkId: number
  linkName: string
  linkUrl: string
  sortNo: number
  status: number
}

interface AboutUsEntity {
  aboutId?: number
  platformName?: string
  phone?: string
  email?: string
  address?: string
  introText?: string
  coverImg?: string
  updateTime?: string
}

interface AfterSaleEntity {
  afterSaleId: number
  orderId: number
  userId: number
  reasonType: string
  reasonDesc: string
  evidenceImg?: string
  handleStatus: string
  handleResult?: string
  applyTime?: string
  handleTime?: string
}

interface DeliveryEntity {
  deliveryId?: number
  orderId: number
  courierName?: string
  courierPhone?: string
  deliveryStatus?: string
  dispatchTime?: string
  finishTime?: string
  remark?: string
}

export interface AnnouncementDTO {
  id: number
  title: string
  content: string
  status: number
  publishTime: string
}

export interface BannerDTO {
  id: number
  title: string
  imageUrl: string
  targetUrl: string
  sortNo: number
  status: number
}

export interface FriendlyLinkDTO {
  id: number
  name: string
  url: string
  sortNo: number
  status: number
}

export interface AboutUsDTO {
  platformName: string
  introText: string
  phone: string
  email: string
  address: string
  coverImg?: string
}

export interface AppealDTO {
  appealId: number
  orderId: number
  orderNo: string
  userId: number
  userName: string
  type: string
  reason: string
  attachmentUrl?: string
  status: string
  adminRemark?: string
  createTime: string
  handleTime?: string
}

export interface DispatchDTO {
  dispatchId: number
  orderId: number
  orderNo: string
  userName: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  courierName?: string
  courierPhone?: string
  dispatchStatus: string
  dispatchTime?: string
  finishTime?: string
  remark?: string
}

function toAnnouncement(entity: NoticeEntity): AnnouncementDTO {
  return {
    id: entity.noticeId,
    title: entity.title,
    content: entity.content,
    status: entity.status ?? 1,
    publishTime: entity.publishTime || '',
  }
}

function toBanner(entity: BannerEntity): BannerDTO {
  return {
    id: entity.bannerId,
    title: entity.title,
    imageUrl: entity.imgUrl,
    targetUrl: entity.linkUrl,
    sortNo: entity.sortNo,
    status: entity.status ?? 1,
  }
}

function toLink(entity: FriendLinkEntity): FriendlyLinkDTO {
  return {
    id: entity.linkId,
    name: entity.linkName,
    url: entity.linkUrl,
    sortNo: entity.sortNo,
    status: entity.status ?? 1,
  }
}

function toAboutUs(entity?: AboutUsEntity | null): AboutUsDTO {
  return {
    platformName: entity?.platformName || '城市临期食品分发系统',
    introText: entity?.introText || '',
    phone: entity?.phone || '',
    email: entity?.email || '',
    address: entity?.address || '',
    coverImg: entity?.coverImg || '',
  }
}

function mapDispatchStatus(status?: string, hasDelivery = false): string {
  if (!hasDelivery) {
    return '待分配'
  }
  if (status === '待分配') return '待分配'
  if (status === '待取货') return '待取货'
  if (status === '配送中') return '配送中'
  if (status === '已完成') return '已完成'
  if (status === '已拒绝') return '已拒绝'
  return '待分配'
}

async function buildAppealDTO(entity: AfterSaleEntity): Promise<AppealDTO> {
  const order = await orderApi.getById(entity.orderId).catch(() => null)
  return {
    appealId: entity.afterSaleId,
    orderId: entity.orderId,
    orderNo: order?.orderNo || `订单${entity.orderId}`,
    userId: entity.userId,
    userName: order?.username || `用户${entity.userId}`,
    type: entity.reasonType,
    reason: entity.reasonDesc,
    attachmentUrl: entity.evidenceImg,
    status: entity.handleStatus,
    adminRemark: entity.handleResult,
    createTime: entity.applyTime || '',
    handleTime: entity.handleTime,
  }
}

async function buildDispatchDTO(order: OrderDTO): Promise<DispatchDTO> {
  const delivery = await api.get<DeliveryEntity | null>(`/delivery/order/${order.orderId}`).catch(() => null)
  const hasDelivery = Boolean(delivery)
  return {
    dispatchId: order.orderId,
    orderId: order.orderId,
    orderNo: order.orderNo,
    userName: order.username || order.receiverName || `用户${order.userId}`,
    receiverName: order.receiverName,
    receiverPhone: order.receiverPhone,
    receiverAddress: order.receiverAddress,
    courierName: delivery?.courierName,
    courierPhone: delivery?.courierPhone,
    dispatchStatus: mapDispatchStatus(delivery?.deliveryStatus, hasDelivery),
    dispatchTime: delivery?.dispatchTime || order.payTime || order.createTime,
    finishTime: delivery?.finishTime,
    remark: delivery?.remark || order.remark,
  }
}

function mapDispatchStatusToApi(status: string): string {
  return status
}

export const adminApi = {
  async getSettings() {
    const [announcements, banners, friendlyLinks, aboutUs] = await Promise.all([
      this.listAnnouncements(),
      this.listBanners(),
      this.listFriendlyLinks().catch(() => []),
      this.getAboutUs().catch(() => ({})),
    ])
    return { announcements, banners, friendlyLinks, aboutUs }
  },

  async listAnnouncements() {
    const list = await api.get<NoticeEntity[]>('/system/notice/list')
    return list.map(toAnnouncement)
  },

  async saveAnnouncement(data: Partial<AnnouncementDTO>) {
    const saved = await api.post<NoticeEntity>('/system/notice', {
      noticeId: data.id,
      title: data.title,
      content: data.content,
      status: data.status ?? 1,
    })
    return toAnnouncement(saved)
  },

  updateAnnouncement: (data: Partial<AnnouncementDTO>) =>
    api.put<NoticeEntity>(`/system/notice/${data.id}`, {
      noticeId: data.id,
      title: data.title,
      content: data.content,
      status: data.status ?? 1,
    }),

  deleteAnnouncement: (id: number) => api.delete<boolean>(`/system/notice/${id}`),

  async listBanners() {
    const list = await api.get<BannerEntity[]>('/system/banner/list')
    return list.map(toBanner)
  },

  async saveBanner(data: Partial<BannerDTO>) {
    const saved = await api.post<BannerEntity>('/system/banner', {
      bannerId: data.id,
      title: data.title,
      imgUrl: data.imageUrl,
      linkUrl: data.targetUrl,
      sortNo: data.sortNo ?? 1,
      status: data.status ?? 1,
    })
    return toBanner(saved)
  },

  updateBanner: (data: Partial<BannerDTO>) =>
    api.put<BannerEntity>(`/system/banner/${data.id}`, {
      bannerId: data.id,
      title: data.title,
      imgUrl: data.imageUrl,
      linkUrl: data.targetUrl,
      sortNo: data.sortNo ?? 1,
      status: data.status ?? 1,
    }),

  deleteBanner: (id: number) => api.delete<boolean>(`/system/banner/${id}`),

  async listFriendlyLinks() {
    const list = await api.get<FriendLinkEntity[]>('/system/friend-link/list')
    return (list || []).map(toLink)
  },
  async saveFriendlyLink(data: Partial<FriendlyLinkDTO>) {
    const saved = await api.post<FriendLinkEntity>('/system/friend-link', {
      linkName: data.name,
      linkUrl: data.url,
      sortNo: data.sortNo ?? 1,
      status: data.status ?? 1,
    })
    return toLink(saved)
  },
  async updateFriendlyLink(data: Partial<FriendlyLinkDTO>) {
    const saved = await api.put<FriendLinkEntity>(`/system/friend-link/${data.id}`, {
      linkId: data.id,
      linkName: data.name,
      linkUrl: data.url,
      sortNo: data.sortNo ?? 1,
      status: data.status ?? 1,
    })
    return toLink(saved)
  },
  async deleteFriendlyLink(id: number) {
    await api.delete<boolean>(`/system/friend-link/${id}`)
    return true
  },
  async getAboutUs() {
    const entity = await api.get<AboutUsEntity>('/system/about')
    return toAboutUs(entity)
  },
  async saveAboutUs(data: Partial<AboutUsDTO>) {
    const saved = await api.put<AboutUsEntity>('/system/about', {
      platformName: data.platformName,
      phone: data.phone,
      email: data.email,
      address: data.address,
      introText: data.introText,
      coverImg: data.coverImg,
    })
    return toAboutUs(saved)
  },

  async listAppeals(params?: { pageNum?: number; pageSize?: number; status?: string }) {
    const page = await api.get<PageResult<AfterSaleEntity>>('/after-sale/list', {
      pageNum: params?.pageNum,
      pageSize: params?.pageSize,
      status: params?.status,
    })
    const records = await Promise.all(page.records.map(buildAppealDTO))
    return { ...page, records }
  },

  async getAppealById(id: number) {
    const entity = await api.get<AfterSaleEntity>(`/after-sale/${id}`)
    return buildAppealDTO(entity)
  },

  updateAppealStatus: (id: number, status: string, adminRemark?: string) =>
    api.put<boolean>(
      `/after-sale/handle/${id}?handleStatus=${encodeURIComponent(status)}${adminRemark ? `&handleResult=${encodeURIComponent(adminRemark)}` : ''}`
    ),

  async listDispatches(params?: { pageNum?: number; pageSize?: number; status?: string }) {
    // 查询所有非待支付状态的订单用于配送调度
    const page = await orderApi.listAll({ pageNum: params?.pageNum ?? 1, pageSize: params?.pageSize ?? 50 })
    const records = await Promise.all(page.records.map(buildDispatchDTO))
    const filtered = params?.status
      ? records.filter((item) => item.dispatchStatus === params.status)
      : records
    return {
      ...page,
      total: filtered.length,
      records: filtered,
    }
  },

  async getDispatchById(id: number) {
    const order = await orderApi.getById(id)
    return buildDispatchDTO(order)
  },

  updateDispatchStatus: (orderId: number, status: string) =>
    api.put<boolean>(`/delivery/status/${orderId}?status=${encodeURIComponent(mapDispatchStatusToApi(status))}`),

  assignCourier: (orderId: number, courierName: string, courierPhone: string) =>
    api.post<boolean>(
      `/delivery/assign?orderId=${orderId}&courierName=${encodeURIComponent(courierName)}&courierPhone=${encodeURIComponent(courierPhone)}`
    ),

  dispatchAssign: (orderId: number, courierName: string, courierPhone: string) =>
    api.put<boolean>(
      `/order/dispatch-assign/${orderId}?courierName=${encodeURIComponent(courierName)}&courierPhone=${encodeURIComponent(courierPhone)}`
    ),

  acceptOrder: (orderId: number) =>
    api.put<boolean>(`/order/accept/${orderId}`),

  rejectOrder: (orderId: number, reason?: string) =>
    api.put<boolean>(`/order/reject/${orderId}${reason ? `?reason=${encodeURIComponent(reason)}` : ''}`),

  getRecommendConfig() {
    return api.get<{ alpha: number; beta: number; lambda: number; behaviorWindowDays: number }>('/recommend/config')
  },

  saveRecommendConfig(data: { alpha: number; beta: number; lambda: number; behaviorWindowDays: number }) {
    return api.put('/recommend/config', data)
  },

  listCouriers(all?: boolean) {
    return api.get<CourierEntity[]>('/courier/list', { all: all ? 'true' : undefined })
  },

  addCourier(data: { courierName: string; courierPhone: string; status?: number }) {
    return api.post<CourierEntity>('/courier', data)
  },

  updateCourier(id: number, data: { courierName?: string; courierPhone?: string; status?: number }) {
    return api.put<CourierEntity>(`/courier/${id}`, data)
  },

  deleteCourier(id: number) {
    return api.delete<boolean>(`/courier/${id}`)
  },
}

export interface CourierEntity {
  courierId: number
  userId?: number
  courierName: string
  courierPhone: string
  status: number
  createTime?: string
}
