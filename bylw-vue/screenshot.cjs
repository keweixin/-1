const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const BASE = 'http://localhost:5173';
const OUT_DIR = path.resolve('E:/毕业论文/图片');
const WIDTH = 1440;
const HEIGHT = 900;

if (!fs.existsSync(OUT_DIR)) fs.mkdirSync(OUT_DIR, { recursive: true });

async function screenshot(page, name) {
  const filePath = path.join(OUT_DIR, `${name}.png`);
  await page.screenshot({ path: filePath, fullPage: true });
  console.log(`  ✓ ${name}`);
}

async function nav(page, url, name, wait = 1500) {
  await page.goto(`${BASE}${url}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(wait);
  await screenshot(page, name);
}

async function main() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: WIDTH, height: HEIGHT } });
  const page = await context.newPage();

  // ========== 5.1 Admin (图5-1 ~ 图5-16) ==========
  console.log('\n=== 5.1 管理员模块 ===');

  await page.goto(`${BASE}/admin-login`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(800);
  await page.locator('input[placeholder="请输入管理员账号"]').fill('admin');
  await page.locator('input[placeholder="请输入管理员密码"]').fill('123456');
  await page.locator('button[type="submit"]').click();
  await page.waitForTimeout(3000);
  console.log('  Admin logged in');

  await nav(page, '/admin/profile', '图5-1');
  await nav(page, '/admin/users', '图5-2');
  await nav(page, '/admin/categories', '图5-3');
  await nav(page, '/admin/foods', '图5-4');
  await nav(page, '/admin/orders', '图5-5');
  await nav(page, '/admin/dispatch', '图5-6');
  await nav(page, '/admin/community', '图5-7');

  // 图5-8: 评论管理 tab
  await page.goto(`${BASE}/admin/community`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await page.locator('button:has-text("评论管理")').click();
  await page.waitForTimeout(1500);
  await screenshot(page, '图5-8');

  // 图5-9: 积分商品管理 tab
  await page.goto(`${BASE}/admin/points`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  const goodsBtn = page.locator('button:has-text("商品管理")');
  if (await goodsBtn.isVisible()) {
    await goodsBtn.click();
    await page.waitForTimeout(1500);
  }
  await screenshot(page, '图5-9');

  await nav(page, '/admin/appeals', '图5-10');
  await nav(page, '/admin/content', '图5-11');

  // 图5-12: 健康食谱 tab
  await page.goto(`${BASE}/admin/content`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  const recipeBtn = page.locator('button:has-text("食谱"), button:has-text("健康食谱")');
  if (await recipeBtn.count() > 0) {
    await recipeBtn.first().click();
    await page.waitForTimeout(1500);
  }
  await screenshot(page, '图5-12');

  // 图5-13: 轮播图管理 (settings banner tab)
  await page.goto(`${BASE}/admin/settings`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  const bannerBtn = page.locator('button, [role="tab"], [class*="tab"]').filter({ hasText: '轮播图' });
  if (await bannerBtn.count() > 0) {
    await bannerBtn.first().click();
    await page.waitForTimeout(1500);
  }
  await screenshot(page, '图5-13');

  await nav(page, '/admin/favorites', '图5-14');
  await nav(page, '/admin/reviews', '图5-15');
  await nav(page, '/admin/settings', '图5-16');

  await context.clearCookies();
  await page.evaluate(() => localStorage.clear());
  await page.waitForTimeout(500);

  // ========== 5.2 Merchant (图5-17 ~ 图5-21) ==========
  console.log('\n=== 5.2 商家模块 ===');

  // 图5-17: 商家登录
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(800);
  const merchantRole = page.locator('button:has-text("商户")');
  if (await merchantRole.isVisible()) { await merchantRole.click(); await page.waitForTimeout(300); }
  await page.locator('input[placeholder*="用户名"]').first().fill('merchant001');
  await page.locator('input[type="password"]').first().fill('123456');
  await page.waitForTimeout(300);
  await screenshot(page, '图5-17');

  await page.locator('button[type="submit"]').click();
  await page.waitForTimeout(3000);
  console.log('  Merchant logged in');

  await nav(page, '/merchant', '图5-18');
  await nav(page, '/merchant/foods', '图5-19');
  await nav(page, '/merchant/orders', '图5-20');
  await nav(page, '/merchant/profile', '图5-21');

  await context.clearCookies();
  await page.evaluate(() => localStorage.clear());
  await page.waitForTimeout(500);

  // ========== 5.3 Rider (图5-22 ~ 图5-25) ==========
  console.log('\n=== 5.3 骑手模块 ===');

  // 图5-22: 骑手登录
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(800);
  const riderRole = page.locator('button:has-text("骑手")');
  if (await riderRole.isVisible()) { await riderRole.click(); await page.waitForTimeout(300); }
  await page.locator('input[placeholder*="用户名"]').first().fill('courier001');
  await page.locator('input[type="password"]').first().fill('123456');
  await page.waitForTimeout(300);
  await screenshot(page, '图5-22');

  await page.locator('button[type="submit"]').click();
  await page.waitForTimeout(3000);
  console.log('  Rider logged in');

  await nav(page, '/rider/profile', '图5-23');
  await nav(page, '/rider/hall', '图5-24');
  await nav(page, '/rider/history', '图5-25');

  await context.clearCookies();
  await page.evaluate(() => localStorage.clear());
  await page.waitForTimeout(500);

  // ========== 5.4 User (图5-26 ~ 图5-41) ==========
  console.log('\n=== 5.4 用户模块 ===');

  // 图5-26: 用户注册
  await page.goto(`${BASE}/register`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(800);
  const regFields = {
    '请输入用户名': 'newuser2026',
    '请输入手机号': '13800138000',
    '请输入登录密码': 'Test1234',
    '请再次输入登录密码': 'Test1234',
  };
  for (const [ph, val] of Object.entries(regFields)) {
    const inp = page.locator(`input[placeholder="${ph}"]`).first();
    if (await inp.isVisible()) await inp.fill(val);
  }
  await page.waitForTimeout(300);
  await screenshot(page, '图5-26');

  // 图5-27: 用户登录
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(800);
  await page.locator('input[placeholder*="用户名"]').first().fill('testuser');
  await page.locator('input[type="password"]').first().fill('123456');
  await page.waitForTimeout(300);
  await screenshot(page, '图5-27');

  await page.locator('button[type="submit"]').click();
  await page.waitForTimeout(3000);
  console.log('  User logged in');

  await nav(page, '/', '图5-28', 2500);
  await nav(page, '/food-hall', '图5-29', 2000);

  // Get a real food ID
  let foodId = 1;
  try {
    const resp = await page.request.get('http://localhost:8080/api/food/list?pageNum=1&pageSize=1');
    const data = await resp.json();
    foodId = data?.data?.records?.[0]?.foodId || 1;
  } catch {}
  await nav(page, `/food/${foodId}`, '图5-30', 2000);
  await nav(page, '/recommendations', '图5-31', 2500);

  // 图5-32: 下单购买
  await page.goto(`${BASE}/food/${foodId}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(2000);
  const buyBtn = page.locator('button:has-text("立即购买"), button:has-text("购买")');
  if (await buyBtn.count() > 0) {
    await buyBtn.first().scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
  }
  await screenshot(page, '图5-32');

  await nav(page, '/orders', '图5-33');
  await nav(page, '/points', '图5-34', 2000);
  await nav(page, '/community', '图5-35');
  await nav(page, '/encyclopedia', '图5-36');
  await nav(page, '/recipes', '图5-37');

  // 图5-38: 饮食档案
  await page.goto(`${BASE}/profile`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1500);
  const diet = page.locator('text=饮食档案').first();
  if (await diet.isVisible()) { await diet.scrollIntoViewIfNeeded(); await page.waitForTimeout(500); }
  await screenshot(page, '图5-38');

  // 图5-39: 个人中心
  await page.goto(`${BASE}/profile`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1500);
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(300);
  await screenshot(page, '图5-39');

  await nav(page, '/appeals', '图5-40');
  await nav(page, '/favorites', '图5-41');

  await browser.close();
  console.log('\n✅ 全部截图完成！');
  console.log(`保存目录: ${OUT_DIR}`);
}

main().catch(err => {
  console.error('Error:', err.message);
  process.exit(1);
});
