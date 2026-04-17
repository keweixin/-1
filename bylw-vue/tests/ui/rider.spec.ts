import { test, expect, ACCOUNTS, loginViaAPI, setAuth } from '../fixtures'

async function loginRider(page) {
  const result = await loginViaAPI(page, ACCOUNTS.rider.username, ACCOUNTS.rider.password)
  if (result) await setAuth(page, result)
  await page.goto('/rider')
  await page.waitForTimeout(2000)
}

test.describe('Rider Home', () => {
  test('loads rider dashboard', async ({ page }) => {
    await loginRider(page)
    const body = await page.textContent('body')
    expect(body?.includes('今日') || body?.includes('完成') || body?.includes('配送')).toBeTruthy()
  })

  test('stats cards visible', async ({ page }) => {
    await loginRider(page)
    const body = await page.textContent('body')
    expect(body).toContain('今日完成')
    expect(body).toContain('待取货')
  })
})

test.describe('Rider Tasks', () => {
  test('loads task list with filters', async ({ page }) => {
    await loginRider(page)
    await page.goto('/rider/tasks')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('全部') || body?.includes('待取货') || body?.includes('暂无')).toBeTruthy()
  })

  test('no duplicate address lines', async ({ page }) => {
    await loginRider(page)
    await page.goto('/rider/tasks')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    const count = (body?.match(/买家收货地址/g) || []).length
    const tasks = await page.locator('.bg-white.rounded-2xl').count()
    if (tasks > 0) expect(count).toBeLessThanOrEqual(tasks)
  })
})

test.describe('Rider Hall', () => {
  test('loads delivery hall', async ({ page }) => {
    await loginRider(page)
    await page.goto('/rider/hall')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('接单') || body?.includes('大厅') || body?.includes('暂无')).toBeTruthy()
  })
})

test.describe('Rider History', () => {
  test('loads history page', async ({ page }) => {
    await loginRider(page)
    await page.goto('/rider/history')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('历史') || body?.includes('已完成') || body?.includes('暂无')).toBeTruthy()
  })
})

test.describe('Rider Profile', () => {
  test('loads profile page', async ({ page }) => {
    await loginRider(page)
    await page.goto('/rider/profile')
    await page.waitForTimeout(3000)
    const body = await page.textContent('body')
    expect(body?.includes('个人') || body?.includes('骑手') || body?.includes('昵称')).toBeTruthy()
  })
})
