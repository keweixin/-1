<template>
  <div class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-gray-900">用户管理</h1>
        <p class="mt-2 text-sm text-gray-500">管理平台用户账号、角色与状态。</p>
      </div>
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <div class="border-b border-gray-100 px-8 py-5 flex flex-col lg:flex-row lg:items-center gap-4">
        <div class="flex-1 grid grid-cols-1 md:grid-cols-3 gap-3">
          <input v-model.trim="searchKeyword" class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500"
            placeholder="搜索用户名" @keyup.enter="loadUsers" />
          <select v-model="roleFilter" class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500">
            <option value="">全部角色</option>
            <option value="1">普通用户</option>
            <option value="2">骑手</option>
            <option value="3">商户</option>
          </select>
          <select v-model="statusFilter" class="w-full rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-green-500">
            <option value="">全部状态</option>
            <option value="1">正常</option>
            <option value="0">已禁用</option>
          </select>
        </div>
        <div class="flex items-center gap-3">
          <button class="px-4 py-3 rounded-2xl bg-green-600 text-sm font-bold text-white hover:bg-green-700 transition-all" @click="loadUsers">查询</button>
          <button class="px-4 py-3 rounded-2xl border border-gray-200 text-sm font-bold text-gray-600 hover:bg-gray-50 transition-all" @click="resetFilters">重置</button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
      加载中...
    </div>
    <div v-else-if="filteredUsers.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
      <p>暂无匹配用户</p>
    </div>
    <div v-else class="space-y-4">
      <div v-for="user in filteredUsers" :key="user.userId" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 flex items-center justify-between gap-4">
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
          <button @click="toggleUserStatus(user)"
            :class="['px-4 py-2 rounded-xl text-xs font-bold border transition-colors', user.status === 0 ? 'border-green-200 text-green-600 hover:bg-green-50' : 'border-red-200 text-red-500 hover:bg-red-50']">
            {{ user.status === 0 ? '启用' : '禁用' }}
          </button>
          <button @click="deleteUser(user)" class="px-4 py-2 rounded-xl text-xs font-bold border border-gray-200 text-gray-500 hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors">
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { userApi, type UserDTO } from '@/api/user'
import { getAvatarImage } from '@/utils/images'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const users = ref<UserDTO[]>([])
const loading = ref(true)
const searchKeyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')

const filteredUsers = computed(() => {
  return users.value.filter(u => {
    if (roleFilter.value && String(u.roleType) !== roleFilter.value) return false
    if (statusFilter.value && String(u.status) !== statusFilter.value) return false
    return true
  })
})

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
    const result = await userApi.list({
      pageNum: 1,
      pageSize: 100,
      keyword: searchKeyword.value || undefined,
    })
    users.value = result.records.filter(u => u.role !== 'admin' && u.roleType !== 99)
  } catch (e: unknown) {
    users.value = []
    showToast(e instanceof Error ? e.message : '加载失败', 'error')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  searchKeyword.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
  loadUsers()
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

async function deleteUser(user: UserDTO) {
  if (!confirm(`确定要删除用户 "${user.username}" 吗？此操作不可恢复。`)) return
  try {
    await userApi.delete(user.userId)
    showToast('用户已删除', 'success')
    await loadUsers()
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '删除失败', 'error')
  }
}

onMounted(loadUsers)
</script>
