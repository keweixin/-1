import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const role = ref<string | null>(localStorage.getItem('role'))
  const roleType = ref<number | null>(parseInt(localStorage.getItem('roleType') || '0') || null)
  const userId = ref<number | null>(parseInt(localStorage.getItem('userId') || '0') || null)
  const pointsBalance = ref<number>(Number(localStorage.getItem('pointsBalance') || '0'))

  function setAuth(newToken: string, newRole: string, newRoleType: number, newUserId?: number) {
    token.value = newToken
    role.value = newRole
    roleType.value = newRoleType
    localStorage.setItem('token', newToken)
    localStorage.setItem('role', newRole)
    localStorage.setItem('roleType', String(newRoleType))
    if (newUserId !== undefined) {
      userId.value = newUserId
      localStorage.setItem('userId', String(newUserId))
    }
  }

  function setPointsBalance(balance: number) {
    pointsBalance.value = Number.isFinite(balance) ? balance : 0
    localStorage.setItem('pointsBalance', String(pointsBalance.value))
  }

  function clearAuth() {
    if (token.value) {
      authApi.logout().catch(() => {})
    }
    token.value = null
    role.value = null
    roleType.value = null
    userId.value = null
    pointsBalance.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('roleType')
    localStorage.removeItem('userId')
    localStorage.removeItem('pointsBalance')
  }

  function isAuthenticated(): boolean {
    return !!token.value
  }

  function isAdmin(): boolean {
    return role.value === 'admin' || roleType.value === 99
  }

  function isCourier(): boolean {
    return roleType.value === 2
  }

  function isNormalUser(): boolean {
    return roleType.value === 1
  }

  function isMerchant(): boolean {
    return roleType.value === 3
  }

  function getDefaultRoute(): string {
    if (roleType.value === 99) return '/admin'
    if (roleType.value === 3) return '/merchant'
    if (roleType.value === 2) return '/rider'
    return '/'
  }

  return {
    token, role, roleType, userId, pointsBalance,
    setAuth, setPointsBalance, clearAuth,
    isAuthenticated, isAdmin, isCourier, isMerchant, isNormalUser, getDefaultRoute
  }
})
