import { test as base, expect, type Page } from '@playwright/test'

export const ACCOUNTS = {
  admin: { username: 'admin', password: '123456' },
  user: { username: 'testuser', password: '123456' },
  rider: { username: 'courier001', password: '123456' },
  merchant: { username: 'merchant001', password: '123456' },
}

interface LoginResult {
  token: string
  role: string
  roleType: number
  userId?: number
}

async function loginViaAPI(page: Page, username: string, password: string): Promise<LoginResult | null> {
  const resp = await page.request.post('/api/auth/login', {
    data: { username, password },
  })
  if (resp.ok()) {
    const json = await resp.json()
    if (json.data?.token) {
      return {
        token: json.data.token,
        role: json.data.role,
        roleType: json.data.roleType,
      }
    }
  }
  return null
}

async function setAuth(page: Page, loginResult: LoginResult) {
  await page.goto('/')
  await page.evaluate((data) => {
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('roleType', String(data.roleType))
  }, loginResult)
}

async function loginViaUI(page: Page, username: string, password: string) {
  await page.goto('/login')
  await page.locator('input[type="text"], input[placeholder*="用户名"]').first().fill(username)
  await page.locator('input[type="password"]').first().fill(password)
  await page.locator('button[type="submit"], button:has-text("登录")').first().click()
  await page.waitForURL(/^(?!.*\/login).*$/, { timeout: 10000 }).catch(() => {})
}

export const test = base.extend<{ authenticatedPage: Page }>({
  authenticatedPage: async ({ page }, use) => {
    const result = await loginViaAPI(page, ACCOUNTS.user.username, ACCOUNTS.user.password)
    if (result) await setAuth(page, result)
    await page.goto('/')
    await use(page)
  },
})

export { expect, loginViaAPI, setAuth, loginViaUI }
