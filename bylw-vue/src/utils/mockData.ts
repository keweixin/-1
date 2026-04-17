// Mock data for the City Near-Expiry Food Distribution System

export const FOOD_CATEGORIES = [
  { id: '1', name: '新鲜果蔬', icon: 'Apple' },
  { id: '2', name: '肉禽蛋奶', icon: 'Beef' },
  { id: '3', name: '休闲零食', icon: 'Candy' },
  { id: '4', name: '粮油调味', icon: 'Wheat' },
  { id: '5', name: '速食冷冻', icon: 'IceCream' },
  { id: '6', name: '酒水饮料', icon: 'Coffee' },
]

export interface FoodItem {
  id: string
  name: string
  category: string
  price: number
  originalPrice: number
  expiryDays: number
  stock: number
  image: string
  tags: string[]
  store: string
  description: string
  nutrition: string
  suitable: string
  allergens: string
}

export const FOOD_ITEMS: FoodItem[] = [
  {
    id: '1',
    name: '有机红富士苹果 1kg',
    category: '新鲜果蔬',
    price: 9.9,
    originalPrice: 19.8,
    expiryDays: 3,
    stock: 25,
    image: '/images/placeholders/food-default.jpg',
    tags: ['低脂', '高纤维'],
    store: '绿色生活超市',
    description: '产自烟台，口感脆甜，富含维生素C。',
    nutrition: '热量: 52kcal/100g, 纤维素: 2.4g',
    suitable: '适合所有人，尤其是追求健康饮食的人群。',
    allergens: '无',
  },
  {
    id: '2',
    name: '全脂纯牛奶 1L',
    category: '肉禽蛋奶',
    price: 4.5,
    originalPrice: 12.0,
    expiryDays: 2,
    stock: 15,
    image: '/images/placeholders/food-default.jpg',
    tags: ['高钙', '优质蛋白'],
    store: '晨曦乳业门店',
    description: '优质牧场奶源，巴氏杀菌，保留更多营养。',
    nutrition: '蛋白质: 3.2g/100ml, 钙: 110mg/100ml',
    suitable: '青少年、成年人。',
    allergens: '乳制品',
  },
  {
    id: '3',
    name: '全麦吐司面包 400g',
    category: '速食冷冻',
    price: 6.8,
    originalPrice: 15.0,
    expiryDays: 1,
    stock: 8,
    image: '/images/placeholders/food-default.jpg',
    tags: ['低糖', '粗粮'],
    store: '麦香园面包店',
    description: '全麦粉含量>50%，无添加蔗糖，饱腹感强。',
    nutrition: '热量: 245kcal/100g, 膳食纤维: 6.5g',
    suitable: '健身人群、控糖人群。',
    allergens: '麸质',
  },
  {
    id: '4',
    name: '冷冻鸡胸肉 500g',
    category: '肉禽蛋奶',
    price: 12.0,
    originalPrice: 22.0,
    expiryDays: 15,
    stock: 40,
    image: '/images/placeholders/food-default.jpg',
    tags: ['高蛋白', '低卡'],
    store: '生鲜大本营',
    description: '去皮去油脂，肉质鲜嫩，适合多种烹饪方式。',
    nutrition: '蛋白质: 24g/100g, 脂肪: 1.9g',
    suitable: '减脂健身人群。',
    allergens: '无',
  },
  {
    id: '5',
    name: '新鲜草莓 500g',
    category: '新鲜果蔬',
    price: 15.9,
    originalPrice: 29.9,
    expiryDays: 1,
    stock: 10,
    image: '/images/placeholders/food-default.jpg',
    tags: ['低脂', '高维C'],
    store: '鲜果时光',
    description: '当季采摘，酸甜可口，富含维生素C。',
    nutrition: '热量: 32kcal/100g, 维生素C: 58.8mg/100g',
    suitable: '适合所有人。',
    allergens: '无',
  },
  {
    id: '6',
    name: '无糖燕麦酸奶 200g',
    category: '肉禽蛋奶',
    price: 3.5,
    originalPrice: 8.0,
    expiryDays: 3,
    stock: 30,
    image: '/images/placeholders/food-default.jpg',
    tags: ['低糖', '益生菌'],
    store: '晨曦乳业门店',
    description: '添加真实燕麦颗粒，饱腹感强，促进肠道蠕动。',
    nutrition: '蛋白质: 4.5g/100g, 碳水: 8g/100g',
    suitable: '减脂人群、肠胃不佳者。',
    allergens: '乳制品',
  },
  {
    id: '7',
    name: '混合坚果仁 250g',
    category: '休闲零食',
    price: 19.9,
    originalPrice: 39.9,
    expiryDays: 30,
    stock: 50,
    image: '/images/placeholders/food-default.jpg',
    tags: ['健康脂肪', '高蛋白'],
    store: '良品铺子',
    description: '包含核桃、腰果、巴旦木等多种坚果，营养丰富。',
    nutrition: '热量: 600kcal/100g, 脂肪: 50g/100g',
    suitable: '学生、脑力劳动者。',
    allergens: '坚果',
  },
  {
    id: '8',
    name: '冷冻三文鱼排 200g',
    category: '速食冷冻',
    price: 25.0,
    originalPrice: 45.0,
    expiryDays: 5,
    stock: 12,
    image: '/images/placeholders/food-default.jpg',
    tags: ['高蛋白', 'Omega-3'],
    store: '深海渔场',
    description: '富含Omega-3不饱和脂肪酸，肉质鲜美。',
    nutrition: '蛋白质: 20g/100g, 脂肪: 13g',
    suitable: '健身人群、孕妇、儿童。',
    allergens: '鱼类',
  },
  {
    id: '9',
    name: '特级初榨橄榄油 500ml',
    category: '粮油调味',
    price: 29.9,
    originalPrice: 59.9,
    expiryDays: 60,
    stock: 20,
    image: '/images/placeholders/food-default.jpg',
    tags: ['健康脂肪', '抗氧化'],
    store: '绿色生活超市',
    description: '冷压初榨，保留更多营养，适合凉拌和轻度烹饪。',
    nutrition: '脂肪: 99g/100ml',
    suitable: '追求健康饮食的人群。',
    allergens: '无',
  },
  {
    id: '10',
    name: '0卡气泡水(白桃味) 330ml',
    category: '酒水饮料',
    price: 1.9,
    originalPrice: 4.5,
    expiryDays: 15,
    stock: 100,
    image: '/images/placeholders/food-default.jpg',
    tags: ['0糖', '0卡'],
    store: '便利蜂',
    description: '清爽解渴，无糖无负担，淡淡白桃香气。',
    nutrition: '热量: 0kcal, 糖: 0g',
    suitable: '控糖人群。',
    allergens: '无',
  },
]

export interface Recipe {
  id: string
  name: string
  image: string
  tags: string[]
  summary: string
  content: string
  suitable: string
}

export const RECIPES: Recipe[] = [
  {
    id: '1',
    name: '牛油果鸡肉沙拉',
    image: '/images/placeholders/food-default.jpg',
    tags: ['减脂', '高蛋白'],
    summary: '一道清爽可口的减脂餐，营养均衡。',
    content: '1. 鸡胸肉煮熟切块；2. 牛油果切片；3. 加入生菜、圣女果；4. 淋上橄榄油和柠檬汁。',
    suitable: '追求减脂、健康饮食的人群。',
  },
  {
    id: '2',
    name: '燕麦牛奶粥',
    image: '/images/placeholders/food-default.jpg',
    tags: ['早餐', '高纤维'],
    summary: '简单快捷的营养早餐，开启活力一天。',
    content: '1. 燕麦加入牛奶中；2. 小火煮至粘稠；3. 加入蓝莓或坚果装饰。',
    suitable: '上班族、学生。',
  },
]

export interface Encyclopedia {
  id: string
  title: string
  summary: string
  image: string
  date: string
  views: number
  content: string
}

export const ENCYCLOPEDIA: Encyclopedia[] = [
  {
    id: '1',
    title: '如何科学识别临期食品？',
    summary: '临期食品不等于过期食品，科学识别可以既省钱又环保。',
    image: '/images/placeholders/food-default.jpg',
    date: '2024-03-20',
    views: 1250,
    content: '临期食品是指即将到达保质期但仍在保质期内的食品。国家对不同保质期的食品有明确的临期界定标准...',
  },
  {
    id: '2',
    title: '低糖饮食的五大误区',
    summary: '控糖不代表完全不吃碳水，避开这些坑。',
    image: '/images/placeholders/food-default.jpg',
    date: '2024-03-18',
    views: 890,
    content: '误区一：水果可以随便吃；误区二：不吃主食就行；误区三：无糖饼干可以多吃...',
  },
]

export interface Post {
  id: string
  user: string
  avatar: string
  title: string
  content: string
  images?: string[]
  likes: number
  comments: number
  time: string
}

export const POSTS: Post[] = [
  {
    id: '1',
    user: '健康达人小王',
    avatar: '/images/placeholders/avatar.svg',
    title: '今天在临期大厅捡漏成功！',
    content: '只花了10块钱就买到了原价30的进口牛奶，日期还有3天，完全来得及喝完！大家冲鸭！',
    images: ['/images/placeholders/food-default.jpg'],
    likes: 45,
    comments: 12,
    time: '2小时前',
  },
  {
    id: '2',
    user: '爱做饭的琳琳',
    avatar: '/images/placeholders/avatar.svg',
    title: '分享一个用临期吐司做的法式西多士',
    content: '吐司快到期了口感变干？没关系，裹上蛋液煎一下，瞬间变美味！',
    images: ['/images/placeholders/food-default.jpg'],
    likes: 128,
    comments: 34,
    time: '5小时前',
  },
]

export const POINTS_ITEMS = [
  { id: '1', name: '环保帆布袋', points: 500, stock: 100, image: '/images/placeholders/food-default.jpg' },
  { id: '2', name: '5元无门槛优惠券', points: 200, stock: 999, image: '/images/placeholders/food-default.jpg' },
]

export const POINTS_HISTORY = [
  { id: '1', type: 'earn', description: '每日签到', points: '+10', time: '2024-03-20 08:00' },
  { id: '2', type: 'earn', description: '发布社区帖子', points: '+50', time: '2024-03-19 14:30' },
  { id: '3', type: 'spend', description: '兑换5元无门槛优惠券', points: '-200', time: '2024-03-18 10:15' },
  { id: '4', type: 'earn', description: '购买临期食品奖励', points: '+35', time: '2024-03-17 18:20' },
]

export const ADMIN_STATS = {
  foodCount: 1250,
  orderCount: 450,
  userCount: 3200,
  appealCount: 12,
  postCount: 850,
  pointsExchange: 120,
}

export interface OrderItem {
  name: string
  image: string
  quantity: number
  price: number
}

export interface Order {
  id: string
  user: string
  amount: number
  status: string
  time: string
  items: OrderItem[]
}

export const RECENT_ORDERS: Order[] = [
  {
    id: 'ORD20240320001',
    user: '张三',
    amount: 45.5,
    status: '待配送',
    time: '2024-03-20 10:30',
    items: [
      { name: '有机红富士苹果 1kg', image: '/images/placeholders/food-default.jpg', quantity: 2, price: 9.9 },
      { name: '全脂纯牛奶 1L', image: '/images/placeholders/food-default.jpg', quantity: 1, price: 4.5 }
    ]
  },
  {
    id: 'ORD20240320002',
    user: '李四',
    amount: 12.8,
    status: '已完成',
    time: '2024-03-20 09:15',
    items: [
      { name: '全麦吐司面包 400g', image: '/images/placeholders/food-default.jpg', quantity: 1, price: 6.8 },
      { name: '无糖燕麦酸奶 200g', image: '/images/placeholders/food-default.jpg', quantity: 2, price: 3.0 }
    ]
  },
  {
    id: 'ORD20240320003',
    user: '王五',
    amount: 88.0,
    status: '待接单',
    time: '2024-03-20 08:45',
    items: [
      { name: '冷冻鸡胸肉 500g', image: '/images/placeholders/food-default.jpg', quantity: 3, price: 12.0 },
      { name: '混合坚果仁 250g', image: '/images/placeholders/food-default.jpg', quantity: 2, price: 19.9 },
      { name: '冷冻三文鱼排 200g', image: '/images/placeholders/food-default.jpg', quantity: 1, price: 25.0 }
    ]
  },
  {
    id: 'ORD20240319004',
    user: '赵六',
    amount: 35.8,
    status: '已完成',
    time: '2024-03-19 18:20',
    items: [
      { name: '新鲜草莓 500g', image: '/images/placeholders/food-default.jpg', quantity: 2, price: 15.9 },
      { name: '0卡气泡水(白桃味) 330ml', image: '/images/placeholders/food-default.jpg', quantity: 2, price: 1.9 }
    ]
  },
  {
    id: 'ORD20240319005',
    user: '孙七',
    amount: 29.9,
    status: '已取消',
    time: '2024-03-19 14:10',
    items: [
      { name: '特级初榨橄榄油 500ml', image: '/images/placeholders/food-default.jpg', quantity: 1, price: 29.9 }
    ]
  },
]
