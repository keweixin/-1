<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-black text-gray-900">个人信息</h1>
      <p class="text-sm text-gray-400 mt-1">管理你的骑手资料</p>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <template v-else>
      <!-- Avatar & Status Card -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <div class="flex items-center gap-5">
          <div class="w-16 h-16 bg-gradient-to-br from-green-500 to-emerald-600 rounded-2xl flex items-center justify-center text-white text-2xl font-black shadow-lg shadow-green-600/20">
            {{ form.nickname?.[0] || '骑' }}
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-lg font-black text-gray-900 truncate">{{ form.nickname || '骑手' }}</p>
            <p class="text-sm text-gray-400">{{ form.phone || '-' }}</p>
            <div class="flex items-center gap-2 mt-1">
              <span class="w-2 h-2 rounded-full" :class="courierStatus === 1 ? 'bg-green-500 animate-pulse' : 'bg-red-400'"></span>
              <span class="text-xs font-bold" :class="courierStatus === 1 ? 'text-green-600' : 'text-red-500'">
                {{ courierStatus === 1 ? '正常接单' : '暂停接单' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Status Toggle -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="font-bold text-gray-900 text-sm">接单状态</p>
            <p class="text-xs text-gray-400 mt-0.5">关闭后将不再收到新订单分配</p>
          </div>
          <button
            @click="toggleCourierStatus"
            :class="courierStatus === 1
              ? 'bg-green-600'
              : 'bg-gray-300'"
            class="relative w-12 h-7 rounded-full transition-colors duration-200"
          >
            <span
              :class="courierStatus === 1 ? 'translate-x-5.5' : 'translate-x-0.5'"
              class="absolute top-0.5 w-6 h-6 bg-white rounded-full shadow transition-transform duration-200"
            ></span>
          </button>
        </div>
      </div>

      <!-- Form -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm">
        <div class="p-5 border-b border-gray-50">
          <h2 class="text-base font-black text-gray-900">基本信息</h2>
        </div>
        <form @submit.prevent="handleSubmit" class="p-5 space-y-5">
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">用户名</label>
            <input
              v-model="form.username"
              type="text"
              disabled
              class="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl text-gray-400 text-sm cursor-not-allowed"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">昵称</label>
            <input
              v-model="form.nickname"
              type="text"
              placeholder="请输入昵称"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">手机号</label>
            <input
              v-model="form.phone"
              type="tel"
              placeholder="请输入手机号"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">性别</label>
            <select
              v-model="form.gender"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition"
            >
              <option value="">未设置</option>
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">邮箱</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">地址</label>
            <input
              v-model="form.address"
              type="text"
              placeholder="请输入地址"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition"
            />
          </div>

          <div class="flex gap-3 pt-2">
            <button
              type="submit"
              :disabled="saving"
              class="flex-1 bg-green-600 text-white py-3 rounded-xl font-bold text-sm hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
            <button
              type="button"
              @click="loadProfile"
              class="px-6 py-3 bg-gray-100 text-gray-600 rounded-xl font-bold text-sm hover:bg-gray-200 transition-colors"
            >
              重置
            </button>
          </div>

          <div v-if="message.text" class="text-sm text-center py-2 rounded-xl" :class="message.type === 'success' ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'">
            {{ message.text }}
          </div>
        </form>
      </div>

      <!-- Password Change -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <h2 class="text-base font-black text-gray-900 mb-4">安全设置</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">原密码</label>
            <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition" />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">新密码</label>
            <input v-model="pwdForm.newPassword" type="password" placeholder="��少8位，含大小写字母、数字、特殊字符"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition" />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">确认新密码</label>
            <input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition" />
          </div>
        </div>
        <div class="flex justify-end mt-4">
          <button @click="handlePasswordChange" :disabled="pwdSaving"
            class="px-6 py-3 bg-green-600 text-white rounded-xl font-bold text-sm hover:bg-green-700 disabled:opacity-50 transition-colors">
            {{ pwdSaving ? '修改中...' : '修改密码' }}
          </button>
        </div>
        <div v-if="pwdError" class="mt-3 text-sm text-red-600 font-medium">{{ pwdError }}</div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { authApi } from '@/api/auth'
import { courierApi } from '@/api/courier'

const loading = ref(true)
const saving = ref(false)
const courierStatus = ref(1)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  gender: '',
  email: '',
  address: '',
})

const message = reactive({ type: '', text: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdSaving = ref(false)
const pwdError = ref('')

const loadProfile = async () => {
  loading.value = true
  message.text = ''
  try {
    const data = await authApi.getUserInfo() as any
    form.username = data.username || ''
    form.nickname = data.nickname || ''
    form.phone = data.phone || ''
    form.gender = data.gender || ''
    form.email = data.email || ''
    form.address = data.address || ''

    const profileData = await courierApi.getProfile() as any
    courierStatus.value = profileData?.courierStatus ?? 1
  } catch (e: unknown) {
    message.text = e instanceof Error ? e.message : '加载失败'
    message.type = 'error'
  } finally {
    loading.value = false
  }
}

const toggleCourierStatus = async () => {
  const newStatus = courierStatus.value === 1 ? 0 : 1
  try {
    await courierApi.updateOnlineStatus(newStatus)
    courierStatus.value = newStatus
  } catch (e: unknown) {
    message.text = (e instanceof Error ? e.message : '状态切换失败')
    message.type = 'error'
  }
}

const handleSubmit = async () => {
  saving.value = true
  message.text = ''
  try {
    await authApi.updateUserInfo({
      nickname: form.nickname || undefined,
      phone: form.phone || undefined,
      gender: form.gender || undefined,
      email: form.email || undefined,
      address: form.address || undefined,
    } as any)
    message.text = '保存成功'
    message.type = 'success'
  } catch (e: unknown) {
    message.text = e instanceof Error ? e.message : '保存失败'
    message.type = 'error'
  } finally {
    saving.value = false
  }
}

const handlePasswordChange = async () => {
  pwdError.value = ''
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    pwdError.value = '请填写所有密码字段'
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    pwdError.value = '两次输入的新密码不一致'
    return
  }
  pwdSaving.value = true
  try {
    await authApi.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    message.text = '密码修改成功'
    message.type = 'success'
  } catch (e: unknown) {
    pwdError.value = e instanceof Error ? e.message : '密码修改失败'
  } finally {
    pwdSaving.value = false
  }
}

onMounted(loadProfile)
</script>
