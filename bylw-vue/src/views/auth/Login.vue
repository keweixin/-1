<template>
  <div class="min-h-screen bg-gray-50 flex flex-col md:flex-row font-sans overflow-hidden">
    <!-- Left: Visual Section -->
    <div class="hidden md:flex md:w-1/2 relative p-12 flex-col justify-between overflow-hidden transition-colors duration-500" :class="activeBg">
      <div class="absolute top-0 right-0 w-96 h-96 rounded-full -translate-y-1/2 translate-x-1/2 opacity-50 transition-colors duration-500" :class="activeAccent"></div>
      <div class="absolute bottom-0 left-0 w-64 h-64 rounded-full translate-y-1/2 -translate-x-1/2 opacity-50 transition-colors duration-500" :class="activeAccentDeep"></div>

      <div class="relative z-10 flex items-center gap-3">
        <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center shadow-xl">
          <component :is="activeRoleIcon" class="w-7 h-7" :class="activeIconColor" />
        </div>
        <span class="text-2xl font-black text-white tracking-tight">城市临期食品分发系统</span>
      </div>

      <div class="relative z-10">
        <h1 class="text-5xl font-black text-white mb-8 leading-tight">
          {{ activeRole.title }}
        </h1>
        <p class="text-lg leading-relaxed max-w-md" :class="activeTextLight">
          {{ activeRole.desc }}
        </p>
      </div>

      <div class="relative z-10 flex items-center gap-8 text-sm font-bold" :class="activeTextLight">
        <div class="flex items-center gap-2">
          <ShieldCheckIcon class="w-5 h-5" />
          安全合规
        </div>
        <div class="flex items-center gap-2">
          <component :is="activeRoleIcon" class="w-5 h-5" />
          {{ activeRole.badge }}
        </div>
      </div>
    </div>

    <!-- Right: Form Section -->
    <div class="flex-grow flex items-center justify-center p-8 bg-white relative">
      <div class="w-full max-w-md">
        <div class="mb-8">
          <h2 class="text-3xl font-black text-gray-900 mb-2">欢迎回来</h2>
          <p class="text-gray-400 text-sm">选择身份并登录您的账号</p>
        </div>

        <!-- Role Selector -->
        <div class="grid grid-cols-3 gap-3 mb-8">
          <button
            v-for="r in roles"
            :key="r.value"
            type="button"
            @click="selectedRole = r.value"
            class="flex flex-col items-center gap-2 p-4 rounded-2xl border-2 transition-all"
            :class="selectedRole === r.value
              ? `${r.activeBorder} ${r.activeBgLight} ${r.activeTextColor}`
              : 'border-gray-100 bg-gray-50 text-gray-400 hover:border-gray-200'"
          >
            <div class="w-10 h-10 rounded-xl flex items-center justify-center transition-colors"
              :class="selectedRole === r.value ? r.activeBgSolid : 'bg-gray-100'">
              <component :is="r.icon" class="w-5 h-5" :class="selectedRole === r.value ? 'text-white' : 'text-gray-400'" />
            </div>
            <span class="text-xs font-bold">{{ r.label }}</span>
          </button>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-5">
          <div v-if="errorMsg" class="rounded-2xl border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-600">
            {{ errorMsg }}
          </div>
          <div class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">用户名 / 手机号</label>
            <div class="relative group">
              <UserIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5 transition-colors" :class="focusColor" />
              <input
                v-model="formData.username"
                type="text"
                required
                placeholder="请输入用户名或手机号"
                class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none transition-all"
                :class="inputFocusClass"
                @focus="focusedField = 'username'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <div class="space-y-2">
            <div class="flex items-center justify-between ml-1">
              <label class="text-xs font-bold text-gray-400 uppercase tracking-wider">登录密码</label>
              <span class="text-xs font-bold cursor-pointer hover:underline transition-colors" :class="activeBtnColor" @click="showResetModal = true">忘记密码？</span>
            </div>
            <div class="relative group">
              <LockIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5 transition-colors" :class="focusColor" />
              <input
                v-model="formData.password"
                type="password"
                required
                placeholder="请输入登录密码"
                class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none transition-all"
                :class="inputFocusClass"
                @focus="focusedField = 'password'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full text-white py-4 rounded-2xl font-black shadow-xl transition-all active:scale-95 flex items-center justify-center gap-2 group disabled:opacity-50 disabled:cursor-not-allowed"
            :class="activeMainBtn"
          >
            <div v-if="loading" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            <template v-else>
              登录{{ activeRole.label }}
              <ArrowRightIcon class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </template>
          </button>
        </form>

        <div class="mt-8 text-center">
          <p class="text-sm text-gray-400">
            还没有账号？
            <router-link to="/register" class="font-bold hover:underline transition-colors" :class="activeBtnColor">立即注册</router-link>
          </p>
        </div>

        <div class="mt-6 pt-6 border-t border-gray-50 text-center">
          <router-link to="/admin-login" class="text-xs font-bold text-gray-300 hover:text-gray-500 transition-colors">
            后台管理入口
          </router-link>
        </div>
      </div>

      <!-- Reset Password Modal -->
      <div v-if="showResetModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/55 backdrop-blur-sm" @click.self="showResetModal = false">
        <div class="w-full max-w-md bg-white rounded-[2rem] p-8 shadow-2xl">
          <h2 class="text-2xl font-black text-gray-900 mb-2">重置密码</h2>
          <p class="text-sm text-gray-500 mb-6">请输入用户名和注册时的手机号来验证身份</p>
          <div v-if="resetMsg" class="rounded-2xl border px-4 py-3 text-sm font-medium mb-4" :class="resetSuccess ? 'border-green-100 bg-green-50 text-green-600' : 'border-red-100 bg-red-50 text-red-600'">{{ resetMsg }}</div>
          <form @submit.prevent="handleReset" class="space-y-4">
            <input v-model="resetForm.username" type="text" required placeholder="用户名" class="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none focus:border-green-600 focus:bg-white transition-all" />
            <input v-model="resetForm.phone" type="tel" required placeholder="注册时的手机号" class="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none focus:border-green-600 focus:bg-white transition-all" />
            <input v-model="resetForm.newPassword" type="password" required placeholder="新密码（至少6位）" class="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none focus:border-green-600 focus:bg-white transition-all" />
            <div class="flex gap-3 pt-2">
              <button type="button" class="flex-1 rounded-2xl bg-gray-100 py-3 text-sm font-bold text-gray-600 hover:bg-gray-200 transition-all" @click="showResetModal = false">取消</button>
              <button type="submit" :disabled="resetLoading" class="flex-1 rounded-2xl bg-green-600 py-3 text-sm font-black text-white hover:bg-green-700 disabled:bg-green-300 transition-all">{{ resetLoading ? '提交中...' : '确认重置' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import {
  User as UserIcon,
  Bike as BikeIcon,
  Store as StoreIcon,
  Lock as LockIcon,
  ArrowRight as ArrowRightIcon,
  ShieldCheck as ShieldCheckIcon,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const focusedField = ref('')
const errorMsg = ref('')
const selectedRole = ref(1)
const showResetModal = ref(false)
const resetLoading = ref(false)
const resetMsg = ref('')
const resetSuccess = ref(false)
const resetForm = reactive({ username: '', phone: '', newPassword: '' })

const formData = reactive({
  username: '',
  password: '',
})

const roles = [
  {
    label: '用户',
    value: 1,
    icon: UserIcon,
    redirect: '/',
    title: '开启您的\n绿色健康生活',
    desc: '通过科学分发临期食品，不仅为您节省开支，更在共同守护地球资源，推动可持续发展的城市生活方式。',
    badge: '个性化推荐',
    bg: 'bg-green-600',
    accent: 'bg-green-500',
    accentDeep: 'bg-green-700',
    textLight: 'text-green-100',
    activeBorder: 'border-green-500',
    activeBgLight: 'bg-green-50',
    activeTextColor: 'text-green-700',
    activeBgSolid: 'bg-green-500',
    activeBtnColor: 'text-green-600',
    activeMainBtn: 'bg-green-600 hover:bg-green-700 shadow-green-100',
    focusBorderColor: 'focus:border-green-600',
  },
  {
    label: '骑手',
    value: 2,
    icon: BikeIcon,
    redirect: '/rider',
    title: '高效配送\n使命必达',
    desc: '骑手工作台为您提供实时订单推送、智能路线规划，让每一份临期食品都能准时送达用户手中。',
    badge: '智能调度',
    bg: 'bg-emerald-600',
    accent: 'bg-emerald-500',
    accentDeep: 'bg-emerald-700',
    textLight: 'text-emerald-100',
    activeBorder: 'border-emerald-500',
    activeBgLight: 'bg-emerald-50',
    activeTextColor: 'text-emerald-700',
    activeBgSolid: 'bg-emerald-500',
    activeBtnColor: 'text-emerald-600',
    activeMainBtn: 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-100',
    focusBorderColor: 'focus:border-emerald-600',
  },
  {
    label: '商户',
    value: 3,
    icon: StoreIcon,
    redirect: '/merchant',
    title: '入驻平台\n共创价值',
    desc: '商户管理中心帮助您轻松管理临期食品库存、处理订单，减少浪费的同时提升经营效益。',
    badge: '库存管理',
    bg: 'bg-blue-600',
    accent: 'bg-blue-500',
    accentDeep: 'bg-blue-700',
    textLight: 'text-blue-100',
    activeBorder: 'border-blue-500',
    activeBgLight: 'bg-blue-50',
    activeTextColor: 'text-blue-700',
    activeBgSolid: 'bg-blue-500',
    activeBtnColor: 'text-blue-600',
    activeMainBtn: 'bg-blue-600 hover:bg-blue-700 shadow-blue-100',
    focusBorderColor: 'focus:border-blue-600',
  },
]

const activeRole = computed(() => roles.find(r => r.value === selectedRole.value) || roles[0])

const activeBg = computed(() => activeRole.value.bg)
const activeAccent = computed(() => activeRole.value.accent)
const activeAccentDeep = computed(() => activeRole.value.accentDeep)
const activeTextLight = computed(() => activeRole.value.textLight)
const activeRoleIcon = computed(() => activeRole.value.icon)
const activeBtnColor = computed(() => activeRole.value.activeBtnColor)
const activeMainBtn = computed(() => activeRole.value.activeMainBtn)
const activeIconColor = computed(() => {
  const v = selectedRole.value
  if (v === 2) return 'text-emerald-600'
  if (v === 3) return 'text-blue-600'
  return 'text-green-600'
})

const focusColor = computed(() => {
  const v = selectedRole.value
  if (v === 2) return focusedField.value ? 'text-emerald-600' : ''
  if (v === 3) return focusedField.value ? 'text-blue-600' : ''
  return focusedField.value ? 'text-green-600' : ''
})

const inputFocusClass = computed(() => {
  const v = selectedRole.value
  if (v === 2) return 'focus:border-emerald-600 focus:bg-white focus:shadow-emerald-100'
  if (v === 3) return 'focus:border-blue-600 focus:bg-white focus:shadow-blue-100'
  return 'focus:border-green-600 focus:bg-white focus:shadow-green-100'
})

const handleLogin = async () => {
  errorMsg.value = ''
  loading.value = true
  try {
    const data = await authApi.login({ username: formData.username, password: formData.password })
    if (!data.token) {
      throw new Error('登录失败：服务器未返回token')
    }

    // 如果选择的身份与账号实际身份不匹配，给出提示
    if (data.roleType !== selectedRole.value) {
      const selectedLabel = roles.find(r => r.value === selectedRole.value)?.label || ''
      const actualLabel = roles.find(r => r.value === data.roleType)?.label || ''
      if (data.roleType === 99) {
        errorMsg.value = '该账号为管理员账号，请使用后台管理入口登录'
      } else {
        errorMsg.value = `该账号为「${actualLabel}」身份，请选择正确的身份登录`
      }
      return
    }

    let userId: number | undefined
    try {
      const payload = JSON.parse(atob(data.token.split('.')[1]))
      userId = payload.sub ? Number(payload.sub) : undefined
    } catch {
      // JWT 解析失败不影响登录
    }
    authStore.setAuth(data.token, data.role, data.roleType, userId)
    const redirect = (route.query.redirect as string) || authStore.getDefaultRoute()
    router.push(redirect)
  } catch (e: unknown) {
    if (e instanceof SyntaxError) {
      errorMsg.value = '登录失败：token解析错误，请重新登录'
    } else {
      errorMsg.value = e instanceof Error ? e.message : '登录失败，请检查用户名和密码'
    }
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  resetMsg.value = ''
  resetSuccess.value = false
  resetLoading.value = true
  try {
    await authApi.resetPassword(resetForm)
    resetSuccess.value = true
    resetMsg.value = '密码重置成功，请使用新密码登录'
    setTimeout(() => {
      showResetModal.value = false
      resetForm.username = ''
      resetForm.phone = ''
      resetForm.newPassword = ''
      resetMsg.value = ''
    }, 2000)
  } catch (e: unknown) {
    resetMsg.value = e instanceof Error ? e.message : '重置失败，请检查信息'
  } finally {
    resetLoading.value = false
  }
}
</script>
