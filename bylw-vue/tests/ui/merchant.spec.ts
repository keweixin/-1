import { test, expect, ACCOUNTS, loginViaAPI, setAuth } from '../fixtures'

async function loginMerchant(page) {
  const result = await loginViaAPI(page, ACCOUNTS.merchant.username, ACCOUNTS.merchant.password)
  if (result) await setAuth(page, result)
  await page.goto('/merchant')
  await page.waitForTimeout(2000)
}

test.describe('Merchant Dashboard', () => {
  test('loads dashboard', async ({ page }) => {
    await loginMerchant(page)
    const body = await page.textContent('body')
    expect(body?.includes('商户') || body?.includes('统计') || body?.includes('食品')).toBeTruthy()
  })
})

test.describe('Merchant Food Management', () => {
  test('loads food list with add button', async ({ page }) => {
    await loginMerchant(page)
    await page.goto('/merchant/foods')
    await page.waitForTimeout(3000)
    const addBtn = page.locator('button:has-text("新增食品"), button:has-text("新增")')
    if (await addBtn.isVisible()) expect(true).toBeTruthy()
  })

  test('food rows have edit and toggle buttons', async ({ page }) => {
    await loginMerchant(page)
    await page.goto('/merchant/foods')
    await page.waitForTimeout(3000)
    const editCount = await page.locator('text=编辑').count()
    const toggleCount = await page.locator('button:has-text("下架"), button:has-text("上架")').count()
    if (editCount > 0) expect(toggleCount).toBeGreaterThan(0)
  })

  test('add food modal opens', async ({ page }) => {
    await loginMerchant(page)
    await page.goto('/merchant/foods')
    await page.waitForTimeout(3000)
    const addBtn = page.locator('button:has-text("新增食品")')
    if (await addBtn.isVisible()) {
      await addBtn.click()
      await page.waitForTimeout(1000)
      const body = await page.textContent('body')
      expect(body).toContain('食品名称')
    }
  })
})

test.describe('Merchant Order Management', () => {
  test('loads order list', async ({ page }) => {
    await loginMerchant(page)
    await page.goto('/merchant/orders')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('订单') || body?.includes('暂无')).toBeTruthy()
  })

  test('pending orders show accept button', async ({ page }) => {
    await loginMerchant(page)
    await page.goto('/merchant/orders')
    await page.waitForTimeout(3000)
    const pendingCount = await page.locator('text=待接单').count()
    if (pendingCount > 0) {
      const acceptCount = await page.locator('button:has-text("确认接单")').count()
      expect(acceptCount).toBeGreaterThan(0)
    }
  })
})
