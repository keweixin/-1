// API service layer - connects Vue frontend to Spring Boot backend

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

interface ApiOptions {
  headers?: Record<string, string>
  params?: Record<string, string | number | undefined>
}

async function request<T>(
  method: string,
  url: string,
  data?: unknown,
  options: ApiOptions = {}
): Promise<T> {
  const token = localStorage.getItem('token')

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options.headers,
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  let fullUrl = `${BASE_URL}${url}`
  if (options.params) {
    const params = new URLSearchParams()
    for (const [k, v] of Object.entries(options.params)) {
      if (v !== undefined) params.append(k, String(v))
    }
    fullUrl += `?${params.toString()}`
  }

  const res = await fetch(fullUrl, {
    method,
    headers,
    body: data ? JSON.stringify(data) : undefined,
  })

  if (!res.ok) {
    if (res.status === 401) {
      localStorage.removeItem('token')
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
    const err = await res.json().catch(() => ({ message: '网络错误' }))
    throw new Error(err.message || `请求失败: ${res.status}`)
  }

  const json = await res.json()
  if (json.code !== 200) {
    if (json.code === 401) {
      localStorage.removeItem('token')
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
    throw new Error(json.message || '请求失败')
  }

  return json.data as T
}

export const api = {
  get<T>(url: string, params?: Record<string, string | number | undefined>, options?: ApiOptions): Promise<T> {
    return request<T>('GET', url, undefined, { ...options, params })
  },
  post<T>(url: string, data?: unknown, options?: ApiOptions): Promise<T> {
    return request<T>('POST', url, data, options)
  },
  put<T>(url: string, data?: unknown, options?: ApiOptions): Promise<T> {
    return request<T>('PUT', url, data, options)
  },
  delete<T>(url: string, options?: ApiOptions): Promise<T> {
    return request<T>('DELETE', url, undefined, options)
  },
}
