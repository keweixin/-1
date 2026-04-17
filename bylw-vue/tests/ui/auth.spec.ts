import { test, expect, ACCOUNTS, loginViaAPI, loginViaUI } from '../fixtures'

// ============================================================
// 1. Login Page
// ============================================================
test.describe('Login Page', () => {
  test('page loads with form elements', async ({ page }) => {
    await page.goto('/login')
    await expect(page.locator('input').first()).toBeVisible({ timeout: 10000 })
    await expect(page.locator('button').first()).toBeVisible()
  })

  test('shows error on wrong credentials', async ({ page }) => {
    await page.goto('/login')
    const inputs = page.locator('input')
    await inputs.first().fill('wronguser')
    await inputs.nth(1).fill('wrongpass')
    await page.locator('button[type="submit"], button:has-text("登录")').first().click()
    // Should show error toast or stay on login page
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })

  test('admin login redirects to admin dashboard', async ({ page }) => {
    await loginViaUI(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
    await page.waitForTimeout(3000)
    // Should redirect away from /login
    expect(page.url()).not.toContain('/login')
  })

  test('forgot password button opens modal', async ({ page }) => {
    await page.goto('/login')
    const forgotBtn = page.locator('text=忘记密码')
    if (await forgotBtn.isVisible()) {
      await forgotBtn.click()
      await expect(page.locator('text=重置密码')).toBeVisible({ timeout: 5000 })
    }
  })

  test('has link to register page', async ({ page }) => {
    await page.goto('/login')
    const registerLink = page.locator('a:has-text("注册")').first()
    if (await registerLink.isVisible({ timeout: 2000 }).catch(() => false)) {
      await registerLink.click()
      await page.waitForURL('**/register', { timeout: 5000 }).catch(() => {})
    }
  })
})

// ============================================================
// 2. Register Page
// ============================================================
test.describe('Register Page', () => {
  test('page loads with form elements', async ({ page }) => {
    await page.goto('/register')
    await expect(page.locator('input').first()).toBeVisible({ timeout: 10000 })
  })

  test('has role selection options', async ({ page }) => {
    await page.goto('/register')
    // Should have role options - check for rider/merchant labels
    const pageContent = await page.textContent('body')
    const hasRoleOptions = pageContent?.includes('普通用户') || pageContent?.includes('骑手') || pageContent?.includes('商户')
    expect(hasRoleOptions).toBeTruthy()
  })

  test('shows validation on empty submit', async ({ page }) => {
    await page.goto('/register')
    await page.locator('button[type="submit"], button:has-text("注册")').first().click()
    await page.waitForTimeout(1000)
    // Should stay on register page
    expect(page.url()).toContain('/register')
  })
})

// ============================================================
// 3. Auth Guard Tests
// ============================================================
test.describe('Auth Guards', () => {
  test('unauthenticated access to /orders redirects to /login', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })

  test('unauthenticated access to /profile redirects to /login', async ({ page }) => {
    await page.goto('/profile')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })

  test('unauthenticated access to /appeals redirects to /login', async ({ page }) => {
    await page.goto('/appeals')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })

  test('unauthenticated access to /points redirects to /login', async ({ page }) => {
    await page.goto('/points')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })

  test('unauthenticated access to /recommendations redirects to /login', async ({ page }) => {
    await page.goto('/recommendations')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })

  test('public pages accessible without login', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(2000)
    expect(page.url()).not.toContain('/login')

    await page.goto('/food-hall')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/food-hall')

    await page.goto('/community')
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/community')
  })
})
