import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { orderApi, type OrderDTO, type FoodDTO, type PageResult } from '@/api/order'

export interface Notification {
  id: string
  type: 'order' | 'expiry'
  title: string
  message: string
  time: string
  read: boolean
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const lastOrderIds = ref<Set<string>>(new Set())

  const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

  async function fetchNotifications() {
    try {
      const ordersPage = await orderApi.listMy({ pageNum: 1, pageSize: 5 }).catch(() => ({ records: [] as OrderDTO[], total: 0, size: 5, current: 1 }))
      const expiryItems = await orderApi.getExpiryReminder(48).catch(() => [] as FoodDTO[])

      const newNotifications: Notification[] = []

      // Order status changes
      const currentIds = new Set(lastOrderIds.value)
      const statusMap: Record<string, string> = {
        '待支付': '待支付',
        '待接单': '商家已确认',
        '待配送': '配送准备中',
        '配送中': '骑手配送中',
        '已完成': '订单已完成',
        '已取消': '订单已取消',
        '已退款': '已退款',
      }
      for (const order of (ordersPage.records ?? [])) {
        const id = `order-${order.orderId}`
        if (!currentIds.has(id) && order.orderStatus) {
          newNotifications.push({
            id,
            type: 'order',
            title: `订单 ${order.orderNo?.slice(-6) || order.orderId}`,
            message: statusMap[order.orderStatus] || order.orderStatus,
            time: order.createTime || '',
            read: currentIds.size > 0,
          })
        }
        currentIds.add(id)
      }
      lastOrderIds.value = currentIds

      // Expiry reminders
      for (const item of (expiryItems ?? []).slice(0, 3)) {
        newNotifications.push({
          id: `expiry-${item.foodName}`,
          type: 'expiry',
          title: '临期提醒',
          message: `${item.foodName} 即将到期`,
          time: item.expireDate || '',
          read: false,
        })
      }

      // Merge: keep existing read state, add new ones at top
      const existingMap = new Map(notifications.value.map(n => [n.id, n]))
      for (const n of newNotifications) {
        if (!existingMap.has(n.id)) {
          notifications.value.unshift(n)
        }
      }
      // Keep only last 20
      if (notifications.value.length > 20) {
        notifications.value = notifications.value.slice(0, 20)
      }
    } catch {
      // Silently fail - notifications are non-critical
    }
  }

  function markAllRead() {
    for (const n of notifications.value) {
      n.read = true
    }
  }

  function clearAll() {
    notifications.value = []
  }

  return { notifications, unreadCount, fetchNotifications, markAllRead, clearAll }
})
