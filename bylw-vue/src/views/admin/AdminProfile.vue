<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-black text-gray-900">个人中心</h1>
      <p class="mt-2 text-sm text-gray-500">管理您的账号信息与安全设置。</p>
    </div>

    <div v-if="feedback" class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'">
      {{ feedback.message }}
    </div>

    <!-- Basic Info -->
    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-8">
      <h2 class="text-lg font-bold text-gray-900 mb-6">基本信息</h2>
      <div class="flex items-center gap-6 mb-6">
        <div class="h-20 w-20 overflow-hidden rounded-2xl border-2 border-gray-200 bg-gray-100">
          <img :src="avatarUrl" alt="头像" class="h-full w-full object-cover" referrerpolicy="no-referrer" />
        </div>
        <div>
          <p class="text-lg font-bold text-gray-900">{{ userInfo.nickname || userInfo.username }}</p>
          <p class="text-sm text-gray-500">{{ userInfo.email || '未设置邮箱' }}</p>
          <p class="text-xs text-green-600 font-medium mt-1">超级管理员</p>
        </div>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="space-y-2 block">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">用户名</span>
          <input :value="userInfo.username" disabled class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm bg-gray-50 text-gray-500" />
        </label>
        <label class="space-y-2 block">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">昵称</span>
          <input v-model="profileForm.nickname" class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500" />
        </label>
        <label class="space-y-2 block">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">手机号</span>
          <input v-model="profileForm.phone" class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500" />
        </label>
        <label class="space-y-2 block">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">邮箱</span>
          <input v-model="profileForm.email" class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500" />
        </label>
      </div>
      <div class="flex justify-end mt-6">
        <button class="px-6 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all disabled:bg-green-300"
          :disabled="profileSubmitting" @click="saveProfile">
          {{ profileSubmitting ? '保存中...' : '保存信息' }}
        </button>
      </div>
    </div>

    <!-- Password Change -->
    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-8">
      <h2 class="text-lg font-bold text-gray-900 mb-6">安全设置</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <label class="space-y-2 block">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">原密码</span>
          <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码"
            class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500" />
        </label>
        <label class="space-y-2 block">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">新密码</span>
          <input v-model="pwdForm.newPassword" type="password" placeholder="至少8位，含大小写字母、数字、特殊字符"
            class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500" />
        </label>
        <label class="space-y-2 block">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">确认新密码</span>
          <input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码"
            class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500" />
        </label>
      </div>
      <div class="flex justify-end mt-6">
        <button class="px-6 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all disabled:bg-green-300"
          :disabled="pwdSubmitting" @click="changePassword">
          {{ pwdSubmitting ? '修改中...' : '修改密码' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { authApi, type User } from '@/api/auth'
import { getAvatarImage } from '@/utils/images'
import { useAuthStore } from '@/stores/auth'

interface FeedbackState { type: 'success' | 'error'; message: string }

const authStore = useAuthStore()
const feedback = ref<FeedbackState | null>(null)
const userInfo = ref<User>({ userId: 0, username: '', nickname: '', gender: '', phone: '', email: '', avatar: '', address: '', role: '', roleType: 0 })
const profileSubmitting = ref(false)
const pwdSubmitting = ref(false)

const profileForm = reactive({ nickname: '', phone: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const avatarUrl = ref('')

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
  setTimeout(() => { feedback.value = null }, 3000)
}

async function loadProfile() {
  try {
    const info = await authApi.getUserInfo()
    userInfo.value = info
    profileForm.nickname = info.nickname || ''
    profileForm.phone = info.phone || ''
    profileForm.email = info.email || ''
    avatarUrl.value = info.avatar || getAvatarImage(String(info.userId), info.nickname || 'AD')
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '加载用户信息失败')
  }
}

async function saveProfile() {
  profileSubmitting.value = true
  try {
    await authApi.updateUserInfo({
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      email: profileForm.email,
    })
    setFeedback('success', '个人信息已更新')
    await loadProfile()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '更新失败')
  } finally {
    profileSubmitting.value = false
  }
}

async function changePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    setFeedback('error', '请填写所有密码字段')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    setFeedback('error', '两次输入的新密码不一致')
    return
  }
  pwdSubmitting.value = true
  try {
    await authApi.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    setFeedback('success', '密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '密码修改失败')
  } finally {
    pwdSubmitting.value = false
  }
}

onMounted(loadProfile)
</script>
