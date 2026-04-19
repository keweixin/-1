const { chromium } = require('playwright');
const path = require('path');
const http = require('http');
const OUT_DIR = path.resolve('E:/毕业论文/图片');

function apiPost(url, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const p = JSON.stringify(body);
    const opts = { hostname: u.hostname, port: u.port, path: u.pathname, method: 'POST', headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(p), ...headers } };
    const req = http.request(opts, res => { let d = ''; res.on('data', c => d += c); res.on('end', () => { try { resolve(JSON.parse(d)); } catch { resolve(null); } }); });
    req.on('error', reject); req.write(p); req.end();
  });
}

function apiGet(url, headers = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    http.get({ hostname: u.hostname, port: u.port, path: u.pathname + u.search, headers }, res => {
      let d = ''; res.on('data', c => d += c); res.on('end', () => { try { resolve(JSON.parse(d)); } catch { resolve(null); } });
    }).on('error', reject);
  });
}

async function main() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();

  // ===================== 图6-1: 健康档案配置测试界面 =====================
  // 用 testrec36281 登录（已有饮食档案: 甜/酸, 花生过敏, 高血压, 不吃辣）
  console.log('\n=== 图6-1: 健康档案配置测试界面 ===');

  await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle' });
  await page.waitForTimeout(500);
  await page.locator('input[placeholder*="用户名"]').first().fill('testrec36281');
  await page.locator('input[type="password"]').first().fill('Test@1234');
  await page.locator('button[type="submit"]').click();
  await page.waitForTimeout(3000);

  // 进入个人中心，滚动到饮食档案区域
  await page.goto('http://localhost:5173/profile', { waitUntil: 'networkidle' });
  await page.waitForTimeout(1500);
  const dietHeading = page.locator('text=饮食档案').first();
  if (await dietHeading.isVisible()) {
    await dietHeading.scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
  }
  await page.screenshot({ path: path.join(OUT_DIR, '图6-1.png'), fullPage: true });
  console.log('  ✓ 图6-1 健康档案配置界面');

  // ===================== 图6-2: 特征匹配推荐测试界面 =====================
  // 同一用户查看推荐结果（有档案，无行为，FM特征匹配生效）
  console.log('\n=== 图6-2: 特征匹配推荐测试界面 ===');

  await page.goto('http://localhost:5173/recommendations', { waitUntil: 'networkidle' });
  await page.waitForTimeout(2500);
  await page.screenshot({ path: path.join(OUT_DIR, '图6-2.png'), fullPage: true });

  // API验证
  const login1 = await apiPost('http://localhost:8080/api/auth/login', { username: 'testrec36281', password: 'Test@1234' });
  const t1 = login1?.data?.token;
  if (t1) {
    const rec2 = await apiGet('http://localhost:8080/api/recommend/foods?limit=5', { Authorization: 'Bearer ' + t1 });
    const r2 = rec2?.data;
    if (r2) {
      console.log(`  候选:${r2.candidateCount} 过滤后:${r2.healthFilteredCount} 冷启动:${r2.coldStart}`);
      (r2.foods || []).slice(0, 3).forEach(f =>
        console.log(`    ${f.foodName} score=${(f.matchScore || 0).toFixed(3)} fm=${(f.featureScore || 0).toFixed(3)} reason=${f.matchReason || '-'}`)
      );
    }
  }
  console.log('  ✓ 图6-2 特征匹配推荐界面');

  // ===================== 图6-3: 协同过滤推荐测试界面 =====================
  // 切换 testuser（有档案 + 44条行为数据），展示 CF 协同过滤结果
  console.log('\n=== 图6-3: 协同过滤推荐测试界面 ===');

  // 模拟行为数据：给 testrec36281 添加一些购买行为
  console.log('  模拟购买行为...');
  const behaviors = [
    { targetType: 'food', targetId: 1, behaviorType: 'purchase' },
    { targetType: 'food', targetId: 3, behaviorType: 'purchase' },
    { targetType: 'food', targetId: 5, behaviorType: 'favorite' },
    { targetType: 'food', targetId: 7, behaviorType: 'purchase' },
    { targetType: 'food', targetId: 10, behaviorType: 'view' },
  ];
  for (const b of behaviors) {
    await apiPost('http://localhost:8080/api/recommend/behavior',
      { ...b, userId: 207 },
      { Authorization: 'Bearer ' + t1 }
    );
  }
  console.log('  已添加5条模拟行为数据');

  // 刷新推荐页面截图（现在有行为数据了）
  await page.goto('http://localhost:5173/recommendations', { waitUntil: 'networkidle' });
  await page.waitForTimeout(2500);
  await page.screenshot({ path: path.join(OUT_DIR, '图6-3.png'), fullPage: true });

  // API验证
  const rec3 = await apiGet('http://localhost:8080/api/recommend/foods?limit=5', { Authorization: 'Bearer ' + t1 });
  const r3 = rec3?.data;
  if (r3) {
    console.log(`  候选:${r3.candidateCount} 过滤后:${r3.healthFilteredCount} 冷启动:${r3.coldStart}`);
    (r3.foods || []).slice(0, 3).forEach(f =>
      console.log(`    ${f.foodName} score=${(f.matchScore || 0).toFixed(3)} cf=${(f.cfScore || 0).toFixed(3)} fm=${(f.featureScore || 0).toFixed(3)} reason=${f.matchReason || '-'}`)
    );
  }
  console.log('  ✓ 图6-3 协同过滤推荐界面');

  await browser.close();

  console.log('\n========================================');
  console.log('测试截图完成');
  console.log('========================================');
  console.log('图6-1 | 健康档案配置界面 | testrec用户饮食档案(甜/酸,花生过敏,高血压,不吃辣)');
  console.log('图6-2 | 特征匹配推荐     | 仅饮食档案无行为 → FM特征匹配分,冷启动模式');
  console.log('图6-3 | 协同过滤推荐     | 档案+模拟行为 → CF协同过滤+FM混合排序');
  console.log(`\n保存目录: ${OUT_DIR}`);
}

main().catch(err => { console.error('Error:', err.message); process.exit(1); });
