<template>
  <div class="space-y-8">
    <h1 class="text-2xl font-black text-gray-900">用户管理</h1>

    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>
    <div v-else-if="users.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
      <p>暂无用户</p>
    </div>
    <div v-else class="space-y-4">
      <div v-for="user in users" :key="user.userId" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 flex items-center justify-between gap-4">
        <div class="flex items-center gap-4 min-w-0 flex-1">
          <img :src="getAvatarImage(user.userId, user.username)" :alt="user.username" class="w-12 h-12 rounded-full bg-gray-100 shrink-0" referrerPolicy="no-referrer" />
          <div class="min-w-0">
            <div class="flex items-center gap-2">
              <p class="font-bold text-gray-900 truncate">{{ user.username }}</p>
              <span :class="['px-2 py-0.5 rounded-lg text-[10px] font-bold uppercase', roleClass(user)]">
                {{ roleLabel(user) }}
              </span>
              <span v-if="user.status === 0" class="px-2 py-0.5 rounded-lg text-[10px] font-bold bg-red-100 text-red-600">已禁用</span>
            </div>
            <p class="text-xs text-gray-400 mt-1">{{ user.phone || '未设置手机号' }}</p>
            <p class="text-xs text-gray-400">积分: <span class="font-bold text-orange-500">{{ user.points ?? 0 }}</span> | 注册: {{ (user.createTime || '').slice(0, 10) }}</p>
          </div>
        </div>
        <div class="flex items-center gap-2 shrink-0">
          <button
            @click="toggleUserStatus(user)"
            :class="['px-4 py-2 rounded-xl text-xs font-bold border transition-colors', user.status === 0 ? 'border-green-200 text-green-600 hover:bg-green-50' : 'border-red-200 text-red-500 hover:bg-red-50']"
          >
            {{ user.status === 0 ? '启用' : '禁用' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi, type UserDTO } from '@/api/user'
import { getAvatarImage } from '@/utils/images'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const users = ref<UserDTO[]>([])
const loading = ref(true)

function roleLabel(user: UserDTO): string {
  if (user.roleType === 2) return '骑手'
  if (user.roleType === 3) return '商户'
  if ((user.points ?? 0) >= 1000) return 'VIP'
  return '普通'
}

function roleClass(user: UserDTO): string {
  if (user.roleType === 2) return 'bg-blue-100 text-blue-600'
  if (user.roleType === 3) return 'bg-purple-100 text-purple-600'
  if ((user.points ?? 0) >= 1000) return 'bg-yellow-100 text-yellow-600'
  return 'bg-gray-100 text-gray-600'
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await userApi.list({ pageNum: 1, pageSize: 100 })
    users.value = result.records.filter(u => u.role !== 'admin' && u.roleType !== 99)
  } catch (e: unknown) {
    users.value = []
    showToast(e instanceof Error ? e.message : '加载失败', 'error')
  } finally {
    loading.value = false
  }
}

async function toggleUserStatus(user: UserDTO) {
  const newStatus = user.status === 0 ? 1 : 0
  try {
    await userApi.updateStatus(user.userId, newStatus)
    user.status = newStatus
    showToast(newStatus === 1 ? '用户已启用' : '用户已禁用', 'success')
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '操作失败', 'error')
  }
}

onMounted(loadUsers)
</script>
