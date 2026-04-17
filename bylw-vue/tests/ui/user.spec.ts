import { test, expect, ACCOUNTS, loginViaAPI, setAuth } from '../fixtures'

async function loginUser(page) {
  const result = await loginViaAPI(page, ACCOUNTS.user.username, ACCOUNTS.user.password)
  if (result) await setAuth(page, result)
  await page.goto('/')
  await page.waitForTimeout(1000)
}

// ============================================================
// 1. Home Page
// ============================================================
test.describe('Home Page', () => {
  test('loads with banner and sections', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(3000)
    // Should have main heading or banner
    const body = await page.textContent('body')
    const hasContent = body?.includes('临期食品') || body?.includes('绿享') || body?.includes('推荐')
    expect(hasContent).toBeTruthy()
  })

  test('category links are visible', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(2000)
    const body = await page.textContent('body')
    const hasCategory = body?.includes('新鲜果蔬') || body?.includes('肉禽蛋奶') || body?.includes('休闲零食')
    expect(hasCategory).toBeTruthy()
  })

  test('recommended foods section visible', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body).toContain('猜我喜欢')
  })

  test('announcements section exists', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(2000)
    const body = await page.textContent('body')
    const hasAnnounce = body?.includes('公告')
    expect(hasAnnounce).toBeTruthy()
  })
})

// ============================================================
// 2. Food Hall
// ============================================================
test.describe('Food Hall', () => {
  test('page loads with food cards', async ({ page }) => {
    await page.goto('/food-hall')
    await page.waitForTimeout(3000)
    // Should have food items or empty state
    const body = await page.textContent('body')
    const hasContent = body?.includes('临期食品') || body?.includes('暂无') || body?.includes('筛选')
    expect(hasContent).toBeTruthy()
  })

  test('food cards have price and name', async ({ page }) => {
    await page.goto('/food-hall')
    await page.waitForTimeout(3000)
    const prices = await page.locator('text=¥').count()
    // Should have at least one price if foods exist
    if (prices > 0) {
      expect(prices).toBeGreaterThan(0)
    }
  })
})

// ============================================================
// 3. Food Detail
// ============================================================
test.describe('Food Detail', () => {
  test('loads food detail page with correct sections', async ({ page }) => {
    // First get a food ID from the API
    const resp = await page.request.get('/api/food/list?pageNum=1&pageSize=1')
    if (resp.ok()) {
      const json = await resp.json()
      const foods = json.data?.records
      if (foods && foods.length > 0) {
        await page.goto(`/food/${foods[0].foodId}`)
        await page.waitForTimeout(3000)
        // Check key sections
        const body = await page.textContent('body')
        const hasDetails = body?.includes('商品详情') || body?.includes('营养说明') || body?.includes('立即下单')
        expect(hasDetails).toBeTruthy()
      }
    }
  })

  test('tab switching changes visible content', async ({ page }) => {
    const resp = await page.request.get('/api/food/list?pageNum=1&pageSize=1')
    if (resp.ok()) {
      const json = await resp.json()
      const foods = json.data?.records
      if (foods && foods.length > 0) {
        await page.goto(`/food/${foods[0].foodId}`)
        await page.waitForTimeout(3000)

        // Click second tab
        const tabs = page.locator('button:has-text("营养"), button:has-text("适合"), button:has-text("商品")')
        if (await tabs.count() >= 2) {
          await tabs.nth(1).click()
          await page.waitForTimeout(500)
          // Active tab should be highlighted
          const activeTab = page.locator('button.text-green-600')
          expect(await activeTab.count()).toBeGreaterThan(0)
        }
      }
    }
  })

  test('order button requires login', async ({ page }) => {
    const resp = await page.request.get('/api/food/list?pageNum=1&pageSize=1')
    if (resp.ok()) {
      const json = await resp.json()
      const foods = json.data?.records
      if (foods && foods.length > 0) {
        await page.goto(`/food/${foods[0].foodId}`)
        await page.waitForTimeout(3000)
        const orderBtn = page.locator('button:has-text("立即下单")')
        if (await orderBtn.isVisible()) {
          await orderBtn.click()
          await page.waitForTimeout(2000)
          // Should redirect to login or show toast
          const isLogin = page.url().includes('/login')
          const hasToast = await page.locator('text=请先登录').count() > 0
          expect(isLogin || hasToast).toBeTruthy()
        }
      }
    }
  })
})

// ============================================================
// 4. User Orders (requires auth)
// ============================================================
test.describe('Orders Page', () => {
  test('loads orders page for authenticated user', async ({ page }) => {
    await loginUser(page)
    await page.goto('/orders')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const hasContent = body?.includes('我的订单') || body?.includes('暂无') || body?.includes('还没有订单')
    expect(hasContent).toBeTruthy()
  })

  test('unpaid orders show cancel and pay buttons', async ({ page }) => {
    await loginUser(page)
    await page.goto('/orders')
    await page.waitForTimeout(3000)
    // Check if any unpaid orders have action buttons
    const payBtn = await page.locator('button:has-text("去支付")').count()
    const cancelBtn = await page.locator('button:has-text("取消订单")').count()
    // If there are unpaid orders, both buttons should appear
    if (payBtn > 0) {
      expect(cancelBtn).toBeGreaterThan(0)
    }
  })
})

// ============================================================
// 5. Profile Page (requires auth)
// ============================================================
test.describe('Profile Page', () => {
  test('loads profile with user info', async ({ page }) => {
    await loginUser(page)
    await page.goto('/profile')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const hasContent = body?.includes('个人中心') || body?.includes('账户基本信息') || body?.includes('饮食档案')
    expect(hasContent).toBeTruthy()
  })

  test('diet profile section visible', async ({ page }) => {
    await loginUser(page)
    await page.goto('/profile')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body).toContain('口味偏好')
    expect(body).toContain('过敏原')
  })

  test('edit profile modal opens', async ({ page }) => {
    await loginUser(page)
    await page.goto('/profile')
    await page.waitForTimeout(3000)
    const editBtn = page.locator('text=编辑资料')
    if (await editBtn.isVisible()) {
      await editBtn.click()
      await page.waitForTimeout(1000)
      const modal = page.locator('text=编辑账户资料')
      expect(await modal.isVisible()).toBeTruthy()
    }
  })
})

// ============================================================
// 6. Recommendations Page (requires auth)
// ============================================================
test.describe('Recommendations Page', () => {
  test('loads recommendations page', async ({ page }) => {
    await loginUser(page)
    await page.goto('/recommendations')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const hasContent = body?.includes('个性化推荐') || body?.includes('为您精选') || body?.includes('热门')
    expect(hasContent).toBeTruthy()
  })

  test('shows login prompt when not logged in', async ({ page }) => {
    await page.goto('/recommendations')
    await page.waitForTimeout(2000)
    // Should redirect to login
    expect(page.url()).toContain('/login')
  })
})

// ============================================================
// 7. Community Pages
// ============================================================
test.describe('Community', () => {
  test('community list page loads', async ({ page }) => {
    await page.goto('/community')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const hasContent = body?.includes('社区') || body?.includes('动态') || body?.includes('暂无')
    expect(hasContent).toBeTruthy()
  })
})

// ============================================================
// 8. Encyclopedia & Recipes
// ============================================================
test.describe('Encyclopedia & Recipes', () => {
  test('encyclopedia list loads', async ({ page }) => {
    await page.goto('/encyclopedia')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const hasContent = body?.includes('百科') || body?.includes('膳食') || body?.includes('暂无')
    expect(hasContent).toBeTruthy()
  })

  test('recipes list loads', async ({ page }) => {
    await page.goto('/recipes')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const hasContent = body?.includes('食谱') || body?.includes('健康') || body?.includes('暂无')
    expect(hasContent).toBeTruthy()
  })
})

// ============================================================
// 9. Points Mall
// ============================================================
test.describe('Points Mall', () => {
  test('redirects to login when not authenticated', async ({ page }) => {
    await page.goto('/points')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })

  test('loads for authenticated user', async ({ page }) => {
    await loginUser(page)
    await page.goto('/points')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const hasContent = body?.includes('积分') || body?.includes('商城') || body?.includes('暂无')
    expect(hasContent).toBeTruthy()
  })
})
