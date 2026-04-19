<template>
  <div class="min-h-screen bg-gray-50 flex flex-col md:flex-row font-sans overflow-hidden">
    <!-- Left: Visual Section -->
    <div class="hidden md:flex md:w-1/2 relative p-12 flex-col justify-between overflow-hidden transition-colors duration-500" :class="activeBg">
      <div class="absolute top-0 right-0 w-96 h-96 rounded-full -translate-y-1/2 translate-x-1/2 opacity-50 transition-colors duration-500" :class="activeAccent"></div>
      <div class="absolute bottom-0 left-0 w-64 h-64 rounded-full translate-y-1/2 -translate-x-1/2 opacity-50 transition-colors duration-500" :class="activeAccentDeep"></div>

      <div class="relative z-10 flex items-center gap-3">
        <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center shadow-xl">
          <ShoppingBagIcon class="text-green-600 w-7 h-7" />
        </div>
        <span class="text-2xl font-black text-white tracking-tight">城市临期食品分发系统</span>
      </div>

      <div class="relative z-10">
        <h1 class="text-5xl font-black text-white mb-8 leading-tight">
          加入我们<br />共建绿色未来
        </h1>
        <p class="text-lg text-green-100 leading-relaxed max-w-md">
          注册成为会员，享受专属临期食品推荐，积累绿色积分，参与社区互动，让健康饮食与环保理念融入日常生活。
        </p>
      </div>

      <div class="relative z-10 flex items-center gap-8 text-green-100 text-sm font-bold">
        <div class="flex items-center gap-2">
          <ShieldCheckIcon class="w-5 h-5" />
          安全合规
        </div>
        <div class="flex items-center gap-2">
          <UserIcon class="w-5 h-5" />
          个性化推荐
        </div>
      </div>
    </div>

    <!-- Right: Form Section -->
    <div class="flex-grow flex items-center justify-center p-8 bg-white relative">
      <div class="w-full max-w-md">
        <div class="mb-8">
          <h2 class="text-3xl font-black text-gray-900 mb-2">创建账号</h2>
          <p class="text-gray-400 text-sm">选择身份并填写注册信息</p>
        </div>

        <!-- Role Selector -->
        <div class="grid grid-cols-3 gap-3 mb-6">
          <button
            v-for="r in roles"
            :key="r.value"
            type="button"
            @click="formData.roleType = r.value"
            class="flex flex-col items-center gap-2 p-4 rounded-2xl border-2 transition-all"
            :class="formData.roleType === r.value
              ? `${r.activeBorder} ${r.activeBgLight} ${r.activeTextColor}`
              : 'border-gray-100 bg-gray-50 text-gray-400 hover:border-gray-200'"
          >
            <div class="w-10 h-10 rounded-xl flex items-center justify-center transition-colors"
              :class="formData.roleType === r.value ? r.activeBgSolid : 'bg-gray-100'">
              <component :is="r.icon" class="w-5 h-5" :class="formData.roleType === r.value ? 'text-white' : 'text-gray-400'" />
            </div>
            <span class="text-xs font-bold">{{ r.label }}</span>
          </button>
        </div>

        <form @submit.prevent="handleRegister" class="space-y-5">
          <div v-if="errorMsg" class="rounded-2xl border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-600">
            {{ errorMsg }}
          </div>
          <div class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">用户名</label>
            <div class="relative group">
              <UserIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5 transition-colors" :class="focusColor" />
              <input
                v-model="formData.username"
                type="text"
                required
                placeholder="请输入用户名"
                class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none transition-all"
                :class="inputFocusClass"
                @focus="focusedField = 'username'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">手机号</label>
            <div class="relative group">
              <PhoneIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5 transition-colors" :class="focusColor" />
              <input
                v-model="formData.phone"
                type="tel"
                required
                placeholder="请输入手机号"
                class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none transition-all"
                :class="inputFocusClass"
                @focus="focusedField = 'phone'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">登录密码</label>
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

          <div class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">确认密码</label>
            <div class="relative group">
              <LockIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5 transition-colors" :class="focusColor" />
              <input
                v-model="formData.confirmPassword"
                type="password"
                required
                placeholder="请再次输入登录密码"
                class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none transition-all"
                :class="inputFocusClass"
                @focus="focusedField = 'confirmPassword'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">昵称（可选）</label>
            <div class="relative group">
              <UserIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5 transition-colors" :class="focusColor" />
              <input
                v-model="formData.nickname"
                type="text"
                placeholder="设置您的昵称"
                class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl text-sm outline-none transition-all"
                :class="inputFocusClass"
                @focus="focusedField = 'nickname'"
                @blur="focusedField = ''"
              />
            </div>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-green-600 text-white py-4 rounded-2xl font-black shadow-xl shadow-green-100 hover:bg-green-700 transition-all active:scale-95 flex items-center justify-center gap-2 group disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <div v-if="loading" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            <template v-else>
              注册账号
              <ArrowRightIcon class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </template>
          </button>
        </form>

        <div class="mt-8 text-center">
          <p class="text-sm text-gray-400">
            已有账号？
            <router-link to="/login" class="text-green-600 font-bold hover:underline">立即登录</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import {
  ShoppingBag as ShoppingBagIcon,
  User as UserIcon,
  Lock as LockIcon,
  Phone as PhoneIcon,
  ArrowRight as ArrowRightIcon,
  ShieldCheck as ShieldCheckIcon,
  Bike as BikeIcon,
  Store as StoreIcon,
} from 'lucide-vue-next'

const router = useRouter()
const loading = ref(false)
const focusedField = ref('')
const errorMsg = ref('')

const roles = [
  { label: '普通用户', value: 1, icon: UserIcon, activeBorder: 'border-green-500', activeBgLight: 'bg-green-50', activeTextColor: 'text-green-700', activeBgSolid: 'bg-green-500' },
  { label: '骑手', value: 2, icon: BikeIcon, activeBorder: 'border-orange-500', activeBgLight: 'bg-orange-50', activeTextColor: 'text-orange-700', activeBgSolid: 'bg-orange-500' },
  { label: '商户', value: 3, icon: StoreIcon, activeBorder: 'border-blue-500', activeBgLight: 'bg-blue-50', activeTextColor: 'text-blue-700', activeBgSolid: 'bg-blue-500' },
]

const formData = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  roleType: 1,
})

const activeBg = computed(() => {
  if (formData.roleType === 2) return 'bg-orange-600'
  if (formData.roleType === 3) return 'bg-blue-600'
  return 'bg-green-600'
})
const activeAccent = computed(() => {
  if (formData.roleType === 2) return 'bg-orange-500'
  if (formData.roleType === 3) return 'bg-blue-500'
  return 'bg-green-500'
})
const activeAccentDeep = computed(() => {
  if (formData.roleType === 2) return 'bg-orange-700'
  if (formData.roleType === 3) return 'bg-blue-700'
  return 'bg-green-700'
})

const focusColor = computed(() => {
  if (formData.roleType === 2) return focusedField.value ? 'text-orange-600' : ''
  if (formData.roleType === 3) return focusedField.value ? 'text-blue-600' : ''
  return focusedField.value ? 'text-green-600' : ''
})

const inputFocusClass = computed(() => {
  if (formData.roleType === 2) return 'focus:border-orange-600 focus:bg-white focus:shadow-orange-100'
  if (formData.roleType === 3) return 'focus:border-blue-600 focus:bg-white focus:shadow-blue-100'
  return 'focus:border-green-600 focus:bg-white focus:shadow-green-100'
})

const handleRegister = async () => {
  errorMsg.value = ''

  if (!formData.username || formData.username.length < 3) {
    errorMsg.value = '用户名长度不能少于3位'
    return
  }
  if (!formData.password || formData.password.length < 6) {
    errorMsg.value = '密码长度不能少于6位'
    return
  }
  if (formData.password !== formData.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  if (formData.phone && !/^1[3-9]\d{9}$/.test(formData.phone)) {
    errorMsg.value = '手机号格式不正确'
    return
  }

  loading.value = true
  try {
    await authApi.register({
      username: formData.username,
      phone: formData.phone,
      password: formData.password,
      nickname: formData.nickname || undefined,
      roleType: formData.roleType,
    })
    router.push('/login')
  } catch (e: unknown) {
    errorMsg.value = e instanceof Error ? e.message : '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>
