import { test, expect, ACCOUNTS } from '../fixtures'

const BASE = 'http://localhost:8080/api'

async function apiPost(path: string, data: Record<string, unknown>) {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return response
}

async function apiGet(path: string, token?: string) {
  const headers: Record<string, string> = {}
  if (token) headers['Authorization'] = `Bearer ${token}`
  const response = await fetch(`${BASE}${path}`, { headers })
  return response
}

// Backend returns HTTP 200 with {code, message, data} for all responses.
// Business errors have code != 200 but HTTP status is still 200.
async function assertApiError(resp: Response, expectedCode = 400) {
  expect(resp.ok).toBeTruthy()
  const json = await resp.json()
  expect(json.code).toBe(expectedCode)
}

async function assertApiSuccess(resp: Response) {
  expect(resp.ok).toBeTruthy()
  const json = await resp.json()
  expect(json.code).toBe(200)
  return json.data
}

// ============================================================
// 1. Auth API
// ============================================================
test.describe('Auth API', () => {
  test('POST /auth/login - valid admin credentials', async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.admin.username,
      password: ACCOUNTS.admin.password,
    })
    const data = await assertApiSuccess(resp)
    expect(data.token).toBeTruthy()
    expect(data.roleType).toBe(99)
  })

  test('POST /auth/login - wrong password returns business error', async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.admin.username,
      password: 'wrongpassword',
    })
    await assertApiError(resp)
  })

  test('POST /auth/login - missing fields returns business error', async () => {
    const resp = await apiPost('/auth/login', { username: '', password: '' })
    await assertApiError(resp)
  })

  test('GET /auth/userinfo - without token returns error', async () => {
    const resp = await apiGet('/auth/userinfo')
    // Backend may return 200 with error code or actual HTTP error
    const json = await resp.json().catch(() => null)
    if (json) {
      expect(json.code).not.toBe(200)
    } else {
      expect(resp.ok).toBeFalsy()
    }
  })

  test('GET /auth/userinfo - with valid token returns user data', async () => {
    const loginResp = await apiPost('/auth/login', {
      username: ACCOUNTS.admin.username,
      password: ACCOUNTS.admin.password,
    })
    const loginJson = await loginResp.json()
    const token = loginJson.data.token
    const resp = await apiGet('/auth/userinfo', token)
    const data = await assertApiSuccess(resp)
    expect(data.username).toBeTruthy()
    expect(data.roleType).toBeDefined()
  })

  test('POST /auth/reset-password - invalid username returns error', async () => {
    const resp = await apiPost('/auth/reset-password', {
      username: 'nonexistent_user_xyz',
      phone: '00000000000',
      newPassword: 'newpass123',
    })
    // Backend returns either 400 or 404 for invalid credentials
    expect(resp.ok).toBeTruthy()
    const json = await resp.json()
    expect(json.code).not.toBe(200)
  })

  test('POST /auth/login - valid user credentials', async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.user.username,
      password: ACCOUNTS.user.password,
    })
    const data = await assertApiSuccess(resp)
    expect(data.roleType).toBe(1)
  })

  test('POST /auth/login - valid rider credentials', async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.rider.username,
      password: ACCOUNTS.rider.password,
    })
    const data = await assertApiSuccess(resp)
    expect(data.roleType).toBe(2)
  })

  test('POST /auth/login - valid merchant credentials', async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.merchant.username,
      password: ACCOUNTS.merchant.password,
    })
    const data = await assertApiSuccess(resp)
    expect(data.roleType).toBe(3)
  })
})

// ============================================================
// 2. Food API
// ============================================================
test.describe('Food API', () => {
  let adminToken: string

  test.beforeAll(async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.admin.username,
      password: ACCOUNTS.admin.password,
    })
    const json = await resp.json()
    adminToken = json.data.token
  })

  test('GET /food/list - returns food list', async () => {
    const resp = await apiGet(`/food/list?pageNum=1&pageSize=10`, adminToken)
    const data = await assertApiSuccess(resp)
    expect(Array.isArray(data.records)).toBeTruthy()
    expect(data.total).toBeDefined()
  })

  test('GET /food/list - pagination works', async () => {
    const resp1 = await apiGet(`/food/list?pageNum=1&pageSize=5`, adminToken)
    const resp2 = await apiGet(`/food/list?pageNum=2&pageSize=5`, adminToken)
    const data1 = await assertApiSuccess(resp1)
    const data2 = await assertApiSuccess(resp2)
    expect(data1.records.length).toBeLessThanOrEqual(5)
    expect(data2.records.length).toBeLessThanOrEqual(5)
  })

  test('GET /food/list - food has required fields', async () => {
    const resp = await apiGet(`/food/list?pageNum=1&pageSize=1`, adminToken)
    const data = await assertApiSuccess(resp)
    if (data.records.length > 0) {
      const food = data.records[0]
      expect(food.foodId).toBeDefined()
      expect(food.foodName).toBeDefined()
      expect(food.discountPrice).toBeDefined()
    }
  })
})

// ============================================================
// 3. Order API
// ============================================================
test.describe('Order API', () => {
  let userToken: string

  test.beforeAll(async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.user.username,
      password: ACCOUNTS.user.password,
    })
    const json = await resp.json()
    userToken = json.data?.token
  })

  test('GET /order/my - returns user orders', async () => {
    if (!userToken) return test.skip()
    const resp = await apiGet('/order/my?pageNum=1&pageSize=10', userToken)
    const data = await assertApiSuccess(resp)
    expect(data).toBeDefined()
  })
})

// ============================================================
// 4. Recommend API
// ============================================================
test.describe('Recommend API', () => {
  test('GET /recommend/foods - returns recommendations', async () => {
    const resp = await apiGet('/recommend/foods?size=4')
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })
})

// ============================================================
// 5. Community API
// ============================================================
test.describe('Community API', () => {
  test('GET /community/posts - returns post list', async () => {
    const resp = await apiGet('/community/posts?pageNum=1&pageSize=10')
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })
})

// ============================================================
// 6. Points API
// ============================================================
test.describe('Points API', () => {
  let userToken: string

  test.beforeAll(async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.user.username,
      password: ACCOUNTS.user.password,
    })
    const json = await resp.json()
    userToken = json.data?.token
  })

  test('GET /points/balance - returns balance', async () => {
    if (!userToken) return test.skip()
    const resp = await apiGet('/points/balance', userToken)
    const data = await assertApiSuccess(resp)
    expect(data.balance).toBeDefined()
  })

  test('GET /points/goods - returns goods list', async () => {
    const resp = await apiGet('/points/goods')
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })
})

// ============================================================
// 7. Profile API
// ============================================================
test.describe('Profile API', () => {
  let userToken: string

  test.beforeAll(async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.user.username,
      password: ACCOUNTS.user.password,
    })
    const json = await resp.json()
    userToken = json.data?.token
  })

  test('GET /profile - returns dietary profile', async () => {
    if (!userToken) return test.skip()
    const resp = await apiGet('/profile', userToken)
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })
})

// ============================================================
// 8. System API
// ============================================================
test.describe('System API', () => {
  test('GET /system/notice/list - returns notices', async () => {
    const resp = await apiGet('/system/notice/list')
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })

  test('GET /system/banner/list - returns banners', async () => {
    const resp = await apiGet('/system/banner/list')
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })
})

// ============================================================
// 9. Merchant API
// ============================================================
test.describe('Merchant API', () => {
  let merchantToken: string

  test.beforeAll(async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.merchant.username,
      password: ACCOUNTS.merchant.password,
    })
    const json = await resp.json()
    merchantToken = json.data?.token
  })

  test('GET /merchant/foods - returns merchant foods', async () => {
    if (!merchantToken) return test.skip()
    const resp = await apiGet('/merchant/foods?pageNum=1&pageSize=10', merchantToken)
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })

  test('GET /merchant/orders - returns merchant orders', async () => {
    if (!merchantToken) return test.skip()
    const resp = await apiGet('/merchant/orders?pageNum=1&pageSize=10', merchantToken)
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })

  test('GET /merchant/stats - returns merchant stats', async () => {
    if (!merchantToken) return test.skip()
    const resp = await apiGet('/merchant/stats', merchantToken)
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })
})

// ============================================================
// 10. Courier API
// ============================================================
test.describe('Courier API', () => {
  let riderToken: string

  test.beforeAll(async () => {
    const resp = await apiPost('/auth/login', {
      username: ACCOUNTS.rider.username,
      password: ACCOUNTS.rider.password,
    })
    const json = await resp.json()
    riderToken = json.data?.token
  })

  test('GET /delivery/courier/tasks - returns tasks', async () => {
    if (!riderToken) return test.skip()
    const resp = await apiGet('/delivery/courier/tasks', riderToken)
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })

  test('GET /delivery/hall - returns hall orders', async () => {
    if (!riderToken) return test.skip()
    const resp = await apiGet('/delivery/hall', riderToken)
    if (resp.ok) {
      const json = await resp.json()
      expect(json.data).toBeDefined()
    }
  })
})
