/**
 * Download REAL food images from Baidu Image Search.
 * Replaces small/wrong images with actual food photos.
 *
 * Run: node download_baidu_images.js [--all|--small|--range start end]
 *   --all     : redownload all 216 foods
 *   --small   : only replace images < 30KB (default)
 *   --range N M: only process food IDs N to M
 */
const fs = require('fs');
const path = require('path');
const https = require('https');
const http = require('http');
const { URL } = require('url');

const FOOD_FILE = 'E:/毕业论文/bylw-spring/sql/food_all.txt';
const IMG_DIR = 'E:/毕业论文/bylw-spring/src/main/resources/static/images/foods/';
const LOG_FILE = 'E:/毕业论文/bylw-spring/sql/baidu_download.log';
const FAILED_FILE = 'E:/毕业论文/bylw-spring/sql/baidu_failed.txt';

// Search keywords optimized for Baidu Image - append "食物" for better food results
function getSearchKeyword(foodName) {
  // Remove weight/quantity info for cleaner search
  const cleaned = foodName
    .replace(/\d+(\.\d+)?\s*(g|kg|ml|L|枚|个|根|节|穗|棵|包|盒|袋|瓶|杯|条|根|片|装|罐|只)/gi, '')
    .replace(/\s+/g, ' ')
    .trim();
  return cleaned + ' 实物图';
}

function cleanName(n) {
  return n.replace(/[\\/:*?"<>|（）()【】\[\]「」『』\u200b\ufeff\s\.\，,、:：\*\+\-\~]+/g, '').substring(0, 25);
}

function httpGet(url, timeout = 15000, headers = {}, maxRedirects = 3) {
  return new Promise((resolve, reject) => {
    const parsed = new URL(url);
    const options = {
      hostname: parsed.hostname,
      port: parsed.port || (parsed.protocol === 'https:' ? 443 : 80),
      path: parsed.pathname + parsed.search,
      method: 'GET',
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
        ...headers,
      },
    };
    const client = parsed.protocol === 'https:' ? https : http;
    const req = client.request(options, (res) => {
      // Follow redirects (max 5)
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location && maxRedirects > 0) {
        const followUrl = res.headers.location.startsWith('http')
          ? res.headers.location
          : new URL(res.headers.location, url).toString();
        httpGet(followUrl, timeout, headers, maxRedirects - 1).then(resolve).catch(reject);
        return;
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve({
        statusCode: res.statusCode,
        data: Buffer.concat(chunks),
        headers: res.headers,
      }));
    });
    req.on('error', reject);
    req.setTimeout(timeout, () => { req.destroy(); reject(new Error('timeout')); });
    req.end();
  });
}

async function downloadImage(url, filepath, minSize = 5000) {
  try {
    const res = await httpGet(url, 15000);
    if (res.statusCode !== 200) return false;
    // Check if it's actually an image (magic bytes)
    const data = res.data;
    if (data.length < minSize) return false;
    // Check JPEG/PNG/WEBP magic bytes
    const isJpeg = data[0] === 0xFF && data[1] === 0xD8;
    const isPng = data[0] === 0x89 && data[1] === 0x50;
    const isWebp = data.length > 11 && data.slice(8, 12).toString('ascii') === 'WEBP';
    if (!isJpeg && !isPng && !isWebp) return false;
    fs.writeFileSync(filepath, data);
    return true;
  } catch (e) {
    return false;
  }
}

// Search Baidu Images for a keyword and return image URLs
async function searchBaiduImages(keyword, maxResults = 5) {
  const searchUrl = `https://image.baidu.com/search/acjson?tn=resultjson_com&logid=1&word=${encodeURIComponent(keyword)}&pn=0&rn=${maxResults}&ie=utf-8&ipn=rj&fp=result&fr=&width=&height=&face=0&istype=2&qc=&nc=1&simic=&sid=&s=&se=&tab=&name=&gsm=1e&cardserver=&tabName=`;
  try {
    const res = await httpGet(searchUrl, 10000);
    if (res.statusCode !== 200) return [];
    const text = res.data.toString('utf8');
    const json = JSON.parse(text);
    const data = json.data || [];
    const urls = [];
    for (const item of data) {
      if (item.objURL) urls.push({ url: item.objURL, size: 'large' });
      if (item.middleURL) urls.push({ url: item.middleURL, size: 'medium' });
      if (item.thumbURL) urls.push({ url: item.thumbURL, size: 'thumb' });
    }
    return urls;
  } catch (e) {
    return [];
  }
}

// Alternative: Use Bing Image Search
async function searchBingImages(keyword, maxResults = 5) {
  const searchUrl = `https://www.bing.com/images/search?q=${encodeURIComponent(keyword + ' food photo')}&form=HDRSC2&first=1&count=${maxResults}`;
  try {
    const res = await httpGet(searchUrl, 10000);
    if (res.statusCode !== 200) return [];
    const text = res.data.toString('utf8');
    // Extract image URLs from Bing HTML response
    const murlRegex = /murl&quot;:&quot;(https?:[^&]+)&quot;/g;
    const urls = [];
    let match;
    while ((match = murlRegex.exec(text)) !== null && urls.length < maxResults) {
      urls.push(decodeURIComponent(match[1].replace(/&amp;/g, '&')));
    }
    return urls;
  } catch (e) {
    return [];
  }
}

// Try Sohu image search (another Chinese image source)
async function searchSogouImages(keyword, maxResults = 5) {
  const searchUrl = `https://pic.sogou.com/pics?query=${encodeURIComponent(keyword)}&mode=1&start=0&reqType=ajax&rn=${maxResults}`;
  try {
    const res = await httpGet(searchUrl, 10000);
    if (res.statusCode !== 200) return [];
    const text = res.data.toString('utf8');
    const json = JSON.parse(text);
    const items = json.data?.items || [];
    const urls = items.map(i => i.picUrl || i.thumbUrl).filter(Boolean);
    return urls;
  } catch (e) {
    return [];
  }
}

async function processFood(foodId, foodName) {
  const safeName = cleanName(foodName);
  const filename = `${foodId}_${safeName}.jpg`;
  const filepath = path.join(IMG_DIR, filename);

  const keyword = getSearchKeyword(foodName);
  let downloaded = false;

  // Strategy 1: Baidu Image Search
  if (!downloaded) {
    try {
      const results = await searchBaiduImages(keyword, 8);
      for (const item of results) {
        // Try largest first (objURL), then medium, then thumb
        const url = typeof item === 'object' ? item.url : item;
        // Remove existing file before downloading new one
        if (fs.existsSync(filepath)) fs.unlinkSync(filepath);
        downloaded = await downloadImage(url, filepath, 8000);
        if (downloaded) {
          const stat = fs.statSync(filepath);
          return { foodId, foodName, filename, status: 'ok', source: 'baidu', size: stat.size };
        }
      }
    } catch (e) { /* continue */ }
  }

  // Strategy 2: Bing Image Search
  if (!downloaded) {
    try {
      const urls = await searchBingImages(foodName + ' 实物图', 5);
      for (const url of urls) {
        if (fs.existsSync(filepath)) fs.unlinkSync(filepath);
        downloaded = await downloadImage(url, filepath, 8000);
        if (downloaded) {
          const stat = fs.statSync(filepath);
          return { foodId, foodName, filename, status: 'ok', source: 'bing', size: stat.size };
        }
      }
    } catch (e) { /* continue */ }
  }

  // Strategy 3: Sogou Image Search
  if (!downloaded) {
    try {
      const urls = await searchSogouImages(foodName, 5);
      for (const url of urls) {
        if (fs.existsSync(filepath)) fs.unlinkSync(filepath);
        downloaded = await downloadImage(url, filepath, 8000);
        if (downloaded) {
          const stat = fs.statSync(filepath);
          return { foodId, foodName, filename, status: 'ok', source: 'sogou', size: stat.size };
        }
      }
    } catch (e) { /* continue */ }
  }

  // Strategy 4: Baidu with simpler keyword (just food name, no suffix)
  if (!downloaded) {
    try {
      const results = await searchBaiduImages(foodName.replace(/\d+(\.\d+)?\s*(g|kg|ml|L|枚|个|根|节|穗|棵|包|盒|袋|瓶|杯|条|根|片|装|罐|只)/gi, '').trim(), 8);
      for (const item of results) {
        const url = typeof item === 'object' ? item.url : item;
        if (fs.existsSync(filepath)) fs.unlinkSync(filepath);
        downloaded = await downloadImage(url, filepath, 5000);
        if (downloaded) {
          const stat = fs.statSync(filepath);
          return { foodId, foodName, filename, status: 'ok', source: 'baidu2', size: stat.size };
        }
      }
    } catch (e) { /* continue */ }
  }

  return { foodId, foodName, filename, status: 'failed', source: 'all_failed', size: 0 };
}

async function main() {
  const args = process.argv.slice(2);
  const mode = args[0] || '--small';

  // Load food list
  const content = fs.readFileSync(FOOD_FILE, 'utf8').trim();
  const allFoods = content.split('\n').filter(l => l.trim()).map(line => {
    const idx = line.indexOf('\t');
    return { foodId: parseInt(line.substring(0, idx)), foodName: line.substring(idx + 1).trim() };
  });

  let foods;
  if (mode === '--range') {
    const startId = parseInt(args[1]);
    const endId = parseInt(args[2]);
    foods = allFoods.filter(f => f.foodId >= startId && f.foodId <= endId);
  } else if (mode === '--all') {
    foods = allFoods;
  } else {
    // --small: only foods with images < 30KB
    foods = allFoods.filter(f => {
      const safeName = cleanName(f.foodName);
      const filename = `${f.foodId}_${safeName}.jpg`;
      const filepath = path.join(IMG_DIR, filename);
      if (!fs.existsSync(filepath)) return true;
      const stat = fs.statSync(filepath);
      return stat.size < 30000;
    });
  }

  console.log(`Processing ${foods.length} food images...`);

  const results = [];
  const failed = [];

  for (let i = 0; i < foods.length; i++) {
    const food = foods[i];
    process.stdout.write(`[${String(i + 1).padStart(3)}/${foods.length}] ${food.foodName.substring(0, 20).padEnd(20)} ... `);

    const result = await processFood(food.foodId, food.foodName);
    results.push(result);

    if (result.status === 'ok') {
      console.log(`OK [${result.source}] (${(result.size / 1024).toFixed(1)}KB)`);
    } else {
      console.log(`FAILED [${result.source}]`);
      failed.push(result);
    }

    // Delay to avoid rate limiting
    if (i < foods.length - 1) await new Promise(r => setTimeout(r, 800));
  }

  const okCount = results.filter(r => r.status === 'ok').length;
  console.log(`\n=== SUMMARY ===`);
  console.log(`OK: ${okCount} | Failed: ${failed.length}`);

  if (failed.length > 0) {
    fs.writeFileSync(FAILED_FILE, failed.map(f => `${f.foodId}\t${f.foodName}`).join('\n'), 'utf8');
    console.log(`Failed list: ${FAILED_FILE}`);
  }

  // Save full log
  const logLines = results.map(r =>
    `${r.foodId}\t${r.foodName}\t${r.status}\t${r.source}\t${r.size}`
  ).join('\n');
  fs.writeFileSync(LOG_FILE, logLines, 'utf8');
  console.log(`Log: ${LOG_FILE}`);
}

main().catch(console.error);
