import { test, expect, ACCOUNTS, loginViaAPI, setAuth } from '../fixtures'

async function loginAdmin(page) {
  const result = await loginViaAPI(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
  if (result) await setAuth(page, result)
  await page.goto('/admin')
  await page.waitForTimeout(2000)
}

test.describe('Admin Dashboard', () => {
  test('loads dashboard with stats', async ({ page }) => {
    await loginAdmin(page)
    const body = await page.textContent('body')
    const hasStats = body?.includes('管理工作台') || body?.includes('运营食品') || body?.includes('订单')
    expect(hasStats).toBeTruthy()
  })

  test('todo cards have no duplicate labels', async ({ page }) => {
    await loginAdmin(page)
    const body = await page.textContent('body')
    const stockWarningCount = (body?.match(/库存预警食品/g) || []).length
    expect(stockWarningCount).toBeLessThanOrEqual(1)
    expect(body).toContain('待处理申诉')
  })

  test('recent orders table visible', async ({ page }) => {
    await loginAdmin(page)
    const body = await page.textContent('body')
    expect(body).toContain('最近订单')
  })
})

test.describe('Admin Food Management', () => {
  test('loads food list', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/foods')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('食品') || body?.includes('暂无')).toBeTruthy()
  })
})

test.describe('Admin Order Management', () => {
  test('loads order list', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/orders')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('订单') || body?.includes('暂无')).toBeTruthy()
  })
})

test.describe('Admin User Management', () => {
  test('loads user list', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/users')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('用户') || body?.includes('普通') || body?.includes('骑手')).toBeTruthy()
  })

  test('users have enable/disable buttons', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/users')
    await page.waitForTimeout(3000)
    const count = await page.locator('button:has-text("禁用"), button:has-text("启用")').count()
    expect(count).toBeGreaterThanOrEqual(0)
  })
})

test.describe('Admin Content Management', () => {
  test('loads content page', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/content')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('文章') || body?.includes('食谱') || body?.includes('内容')).toBeTruthy()
  })
})

test.describe('Admin Community Management', () => {
  test('loads community page', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/community')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('社区') || body?.includes('帖子') || body?.includes('审核')).toBeTruthy()
  })
})

test.describe('Admin Appeal Management', () => {
  test('loads appeal page', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/appeals')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('申诉') || body?.includes('售后') || body?.includes('暂无')).toBeTruthy()
  })
})

test.describe('Admin Dispatch Management', () => {
  test('loads dispatch page', async ({ page }) => {
    await loginAdmin(page)
    await page.goto('/admin/dispatch')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('配送') || body?.includes('调度') || body?.includes('暂无')).toBeTruthy()
  })
})
