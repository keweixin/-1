const BLOCKED_REMOTE_HOSTS = new Set([
  'api.dicebear.com',
  'images.unsplash.com',
  'loremflickr.com',
  'picsum.photos',
])

function isBlockedRemote(url: string): boolean {
  try {
    const parsed = new URL(url, window.location.origin)
    return /^https?:$/i.test(parsed.protocol) && BLOCKED_REMOTE_HOSTS.has(parsed.hostname)
  } catch {
    return false
  }
}

export function resolveImage(url: string | null | undefined, fallback: string): string {
  const value = (url ?? '').trim()
  if (!value) return fallback
  if (value.startsWith('data:') || value.startsWith('blob:') || value.startsWith('/')) return value
  if (isBlockedRemote(value)) return fallback
  return value
}

/* ------------------------------------------------------------------ */
/*  Inline SVG fallback generators — no file dependency needed         */
/* ------------------------------------------------------------------ */

function makeSvg(w: number, h: number, bg: string, iconSvg: string, label: string): string {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${w} ${h}">
    <rect width="${w}" height="${h}" fill="${bg}"/>
    ${iconSvg}
    <text x="${w / 2}" y="${h / 2 + 50}" text-anchor="middle" font-size="22" font-family="Microsoft YaHei,sans-serif" fill="rgba(255,255,255,0.7)" font-weight="bold">${label}</text>
  </svg>`
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

const FOOD_ICON = '<circle cx="160" cy="110" r="50" fill="rgba(255,255,255,0.2)"/><path d="M145 95 h30 a5 5 0 0 1 0 10 H145 a5 5 0 0 1 0-10z M148 105 h24 v20 a4 4 0 0 1-4 4 h-16 a4 4 0 0 1-4-4z" fill="rgba(255,255,255,0.5)"/>'

const BANNER_SVG = (() => {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 500">
    <defs>
      <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#065f46"/>
        <stop offset="50%" stop-color="#059669"/>
        <stop offset="100%" stop-color="#10b981"/>
      </linearGradient>
    </defs>
    <rect width="1200" height="500" fill="url(#bg)"/>
    <circle cx="200" cy="380" r="120" fill="rgba(255,255,255,0.05)"/>
    <circle cx="900" cy="100" r="180" fill="rgba(255,255,255,0.04)"/>
    <circle cx="1050" cy="400" r="80" fill="rgba(255,255,255,0.06)"/>
    <text x="600" y="220" text-anchor="middle" font-size="56" font-family="Microsoft YaHei,sans-serif" fill="white" font-weight="900">城市临期食品分发系统</text>
    <text x="600" y="280" text-anchor="middle" font-size="24" font-family="Microsoft YaHei,sans-serif" fill="rgba(255,255,255,0.7)" font-weight="bold">让每一份食物都物尽其用</text>
    <rect x="520" y="310" width="160" height="44" rx="22" fill="rgba(255,255,255,0.2)"/>
    <text x="600" y="339" text-anchor="middle" font-size="18" font-family="Microsoft YaHei,sans-serif" fill="white" font-weight="bold">立即探索</text>
  </svg>`
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
})()

/* ------------------------------------------------------------------ */
/*  Banner variants — each banner gets a unique theme                 */
/* ------------------------------------------------------------------ */

function makeBannerSvg(colors: [string, string, string], title: string, subtitle: string, btn: string): string {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 500">
    <defs>
      <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="${colors[0]}"/>
        <stop offset="50%" stop-color="${colors[1]}"/>
        <stop offset="100%" stop-color="${colors[2]}"/>
      </linearGradient>
    </defs>
    <rect width="1200" height="500" fill="url(#bg)"/>
    <circle cx="200" cy="380" r="120" fill="rgba(255,255,255,0.05)"/>
    <circle cx="900" cy="100" r="180" fill="rgba(255,255,255,0.04)"/>
    <circle cx="1050" cy="400" r="80" fill="rgba(255,255,255,0.06)"/>
    <text x="600" y="220" text-anchor="middle" font-size="52" font-family="Microsoft YaHei,sans-serif" fill="white" font-weight="900">${title}</text>
    <text x="600" y="280" text-anchor="middle" font-size="24" font-family="Microsoft YaHei,sans-serif" fill="rgba(255,255,255,0.75)" font-weight="bold">${subtitle}</text>
    <rect x="520" y="310" width="160" height="44" rx="22" fill="rgba(255,255,255,0.2)"/>
    <text x="600" y="339" text-anchor="middle" font-size="18" font-family="Microsoft YaHei,sans-serif" fill="white" font-weight="bold">${btn}</text>
  </svg>`
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

const BANNER_MAP: Record<string, string> = {
  '临期食品专区': makeBannerSvg(['#065f46', '#059669', '#10b981'], '🥬 临期食品专区', '优质食品 · 低价好物 · 环保行动', '立即探索'),
  '健康食谱推荐': makeBannerSvg(['#9a3412', '#ea580c', '#f97316'], '🍳 健康食谱推荐', '营养均衡 · 科学食谱 · 健康生活', '查看食谱'),
  '零浪费社区': makeBannerSvg(['#5815c7', '#7c3aed', '#a78bfa'], '💬 零浪费社区', '分享经验 · 交流心得 · 共同成长', '进入社区'),
}

const BANNER_LIST = [
  BANNER_MAP['临期食品专区'],
  BANNER_MAP['健康食谱推荐'],
  BANNER_MAP['零浪费社区'],
]

const RECIPE_SVG = makeSvg(320, 200, '#ea580c',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">🍳</text>',
  '健康食谱')

const ARTICLE_SVG = makeSvg(320, 200, '#2563eb',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">📖</text>',
  '膳食百科')

const COMMUNITY_SVG = makeSvg(320, 200, '#7c3aed',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">💬</text>',
  '社区动态')

const POINTS_SVG = makeSvg(320, 200, '#d97706',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">🎁</text>',
  '积分商品')

const FOOD_DEFAULT_SVG = makeSvg(320, 200, '#16a34a',
  FOOD_ICON, '临期食品')

const FOOD_FRESH_SVG = makeSvg(320, 200, '#15803d',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">🥬</text>',
  '新鲜果蔬')

const FOOD_MEAT_SVG = makeSvg(320, 200, '#dc2626',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">🥩</text>',
  '肉禽蛋奶')

const FOOD_SNACK_SVG = makeSvg(320, 200, '#ca8a04',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">🍪</text>',
  '休闲零食')

const FOOD_GRAIN_SVG = makeSvg(320, 200, '#b45309',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">🌾</text>',
  '粮油调味')

const FOOD_FROZEN_SVG = makeSvg(320, 200, '#0284c7',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">🍦</text>',
  '速食冷冻')

const FOOD_DRINK_SVG = makeSvg(320, 200, '#0d9488',
  '<circle cx="160" cy="90" r="40" fill="rgba(255,255,255,0.15)"/><text x="160" y="100" text-anchor="middle" font-size="40" fill="rgba(255,255,255,0.5)">☕</text>',
  '酒水饮料')

/* ------------------------------------------------------------------ */
/*  Public API — keep same function signatures                        */
/* ------------------------------------------------------------------ */

export function getFoodCategoryImage(categoryName?: string): string {
  switch ((categoryName ?? '').trim()) {
    case '新鲜果蔬':
      return FOOD_FRESH_SVG
    case '肉禽蛋奶':
      return FOOD_MEAT_SVG
    case '休闲零食':
      return FOOD_SNACK_SVG
    case '米面粮油':
    case '调味品':
    case '粮油调味':
      return FOOD_GRAIN_SVG
    case '速食冻品':
    case '速食冷冻':
      return FOOD_FROZEN_SVG
    case '饮料冲剂':
    case '酒水饮料':
      return FOOD_DRINK_SVG
    default:
      return FOOD_DEFAULT_SVG
  }
}

export function getFoodImage(url?: string | null, categoryName?: string): string {
  return resolveImage(url, getFoodCategoryImage(categoryName))
}

export function getRecipeImage(url?: string | null): string {
  return resolveImage(url, RECIPE_SVG)
}

export function getArticleImage(url?: string | null): string {
  return resolveImage(url, ARTICLE_SVG)
}

export function getCommunityImage(url?: string | null): string {
  return resolveImage(url, COMMUNITY_SVG)
}

export function getPointsImage(url?: string | null): string {
  return resolveImage(url, POINTS_SVG)
}

export function getBannerImage(url?: string | null, title?: string | null): string {
  // If url is valid, use it; otherwise pick banner by title
  const resolved = url ? resolveImage(url, '') : ''
  if (resolved && !resolved.startsWith('data:image/svg+xml')) return resolved
  if (title && BANNER_MAP[title]) return BANNER_MAP[title]
  return BANNER_SVG
}

function getAvatarLabel(seed?: string | number, label?: string): string {
  const source = String(label ?? seed ?? '用户').trim()
  if (!source) return '用户'
  return source.length <= 2 ? source : source.slice(-2)
}

export function getAvatarImage(seed?: string | number, label?: string): string {
  const text = getAvatarLabel(seed, label)
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128">
      <defs>
        <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#1f8f62" />
          <stop offset="100%" stop-color="#69c39a" />
        </linearGradient>
      </defs>
      <rect width="128" height="128" rx="28" fill="url(#g)" />
      <circle cx="64" cy="46" r="24" fill="#f4fffa" opacity="0.95" />
      <path d="M28 108c8-22 22-34 36-34s28 12 36 34" fill="#f4fffa" opacity="0.95" />
      <text x="64" y="114" text-anchor="middle" font-size="18" font-family="Microsoft YaHei, sans-serif" fill="#0f5132">${text}</text>
    </svg>
  `.trim()
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}
