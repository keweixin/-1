<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-950 px-6 py-12">
    <div class="grid w-full max-w-5xl overflow-hidden rounded-[2rem] border border-white/10 bg-white shadow-2xl md:grid-cols-[1.05fr_minmax(420px,520px)]">
      <section class="hidden bg-gradient-to-br from-slate-950 via-slate-900 to-green-950 p-12 text-white md:flex md:flex-col md:justify-between">
        <div class="flex items-center gap-3">
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-green-500 shadow-lg shadow-green-500/30">
            <ShieldCheckIcon class="h-7 w-7 text-white" />
          </div>
          <div>
            <p class="text-xl font-black">管理员入口</p>
            <p class="mt-1 text-xs font-bold uppercase tracking-[0.28em] text-green-300">Admin Portal</p>
          </div>
        </div>

        <div>
          <h1 class="mb-6 text-5xl font-black leading-tight">后台独立登录页</h1>
          <p class="max-w-md text-base leading-8 text-white/70">
            该入口仅供管理员使用，登录成功后将直接进入后台工作区；普通用户账号无法通过这里进入后台页面。
          </p>
        </div>

        <router-link to="/login" class="text-sm font-bold text-green-300 transition-colors hover:text-green-200">
          切换到用户登录
        </router-link>
      </section>

      <section class="p-8 md:p-12">
        <div class="mb-10">
          <p class="mb-3 text-sm font-bold uppercase tracking-[0.25em] text-green-600">Admin Access</p>
          <h2 class="text-3xl font-black text-gray-900">登录管理后台</h2>
          <p class="mt-2 text-sm text-gray-500">请输入管理员账号和密码</p>
        </div>

        <form class="space-y-6" @submit.prevent="handleLogin">
          <div v-if="errorMsg" class="rounded-2xl border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-600">
            {{ errorMsg }}
          </div>

          <div class="space-y-2">
            <label class="block text-xs font-bold uppercase tracking-[0.22em] text-gray-400">账号</label>
            <div class="relative">
              <UserIcon class="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
              <input
                v-model="form.username"
                type="text"
                required
                placeholder="请输入管理员账号"
                class="w-full rounded-2xl border border-gray-200 bg-gray-50 py-4 pl-12 pr-4 text-sm outline-none transition-all focus:border-green-600 focus:bg-white"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="block text-xs font-bold uppercase tracking-[0.22em] text-gray-400">密码</label>
            <div class="relative">
              <LockIcon class="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
              <input
                v-model="form.password"
                type="password"
                required
                placeholder="请输入管理员密码"
                class="w-full rounded-2xl border border-gray-200 bg-gray-50 py-4 pl-12 pr-4 text-sm outline-none transition-all focus:border-green-600 focus:bg-white"
              />
            </div>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="flex w-full items-center justify-center gap-2 rounded-2xl bg-green-600 py-4 text-sm font-black text-white shadow-xl shadow-green-100 transition-all hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-green-300"
          >
            <div v-if="loading" class="h-5 w-5 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
            <template v-else>
              进入后台
              <ArrowRightIcon class="h-5 w-5" />
            </template>
          </button>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight as ArrowRightIcon, Lock as LockIcon, ShieldCheck as ShieldCheckIcon, User as UserIcon } from 'lucide-vue-next'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: '',
})

function parseRoleFromToken(token: string) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      role: payload.role ?? 'user',
      roleType: (payload.roleType as number) ?? 99,
      userId: (payload.userId as number) ?? 0,
    }
  } catch {
    return { role: 'user', roleType: 99, userId: 0 }
  }
}

async function handleLogin() {
  errorMsg.value = ''
  loading.value = true

  try {
    const data = await authApi.login({
      username: form.username,
      password: form.password,
    })

    if (!data || !data.token) {
      throw new Error('登录失败：未收到服务器令牌')
    }

    const { role, roleType, userId } = parseRoleFromToken(data.token)

    if (role !== 'admin') {
      errorMsg.value = '当前账号不是管理员，不能进入后台。'
      return
    }

    // 验证通过后再清理旧会话
    const authKeys = ['token', 'role', 'roleType', 'userId', 'pointsBalance']
    authKeys.forEach(key => localStorage.removeItem(key))
    authStore.clearAuth()

    const finalRoleType = roleType ?? 99
    const finalUserId = userId ?? 0

    authStore.setAuth(data.token, role, finalRoleType, finalUserId)

    window.location.href = '/admin'
  } catch (error: unknown) {
    errorMsg.value = error instanceof Error ? error.message : '登录失败，请检查账号和密码'
  } finally {
    loading.value = false
  }
}
</script>
