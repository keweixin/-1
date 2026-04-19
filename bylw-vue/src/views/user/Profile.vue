<template>
  <div class="max-w-6xl mx-auto px-4 py-12">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-12">
      <div>
        <h1 class="text-4xl font-black text-gray-900 mb-3">个人中心</h1>
        <p class="text-gray-500">维护账户资料、饮食档案与个性化推荐依据，让系统推荐更贴近真实需求。</p>
      </div>
      <div class="inline-flex items-center gap-3 px-5 py-3 bg-white rounded-2xl border border-gray-100 shadow-sm">
        <div class="w-12 h-12 rounded-2xl bg-green-50 flex items-center justify-center text-green-600">
          <LeafIcon class="w-6 h-6" />
        </div>
        <div>
          <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">档案完整度</p>
          <p class="text-lg font-black text-gray-900">{{ profileCompletion }}%</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-[320px_minmax(0,1fr)] gap-8">
      <aside class="space-y-6">
        <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm p-8 text-center">
          <div class="w-24 h-24 mx-auto mb-4 rounded-full overflow-hidden border-4 border-green-100">
            <img
              :src="avatarUrl"
              alt="Avatar"
              class="w-full h-full bg-gray-100"
              referrerPolicy="no-referrer"
            />
          </div>
          <h2 class="text-2xl font-black text-gray-900 mb-1">{{ user?.nickname || user?.username || '—' }}</h2>
          <p class="text-sm text-gray-400 mb-4">会员等级: {{ membershipLabel }}</p>
          <div class="grid grid-cols-2 gap-3 text-left">
            <div class="bg-gray-50 rounded-2xl p-4">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">积分</p>
              <p class="text-lg font-black text-orange-500">{{ loading ? '—' : pointsBalance.toLocaleString() }}</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-4">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">档案状态</p>
              <p class="text-lg font-black text-green-600">{{ profileCompletion >= 80 ? '优秀' : profileCompletion >= 50 ? '完善中' : '待补充' }}</p>
            </div>
          </div>
          <button
            class="w-full mt-3 bg-green-600 text-white py-3 rounded-2xl font-bold hover:bg-green-700 transition-all"
            @click="router.push('/points')"
          >
            前往积分商城
          </button>
          <button
            class="w-full mt-2 bg-gradient-to-r from-green-600 to-emerald-500 text-white py-3 rounded-2xl font-bold hover:from-green-700 hover:to-emerald-600 transition-all shadow-lg shadow-green-600/20"
            @click="router.push('/recommendations')"
          >
            个性化膳食推荐
          </button>
        </div>

        <div class="bg-gradient-to-br from-gray-900 to-gray-800 rounded-[2rem] p-6 text-white shadow-xl shadow-gray-900/10">
          <p class="text-xs font-bold text-white/50 uppercase tracking-widest mb-2">个性化说明</p>
          <p class="text-lg font-black mb-3">推荐引擎已读取您的膳食偏好</p>
          <p class="text-sm leading-6 text-white/70">
            系统会综合口味偏好、过敏原、慢病史和忌口信息，对临期食品与健康食谱进行二次筛选和排序。
          </p>
        </div>
      </aside>

      <div class="space-y-6">
        <section class="bg-white rounded-[2rem] border border-gray-100 shadow-sm p-8">
          <div class="flex items-center justify-between mb-6">
            <div>
              <h3 class="text-xl font-black text-gray-900">账户基本信息</h3>
              <p class="text-sm text-gray-500 mt-1">用于登录认证、联系与配送服务。</p>
            </div>
            <button
              class="text-sm text-green-600 hover:text-green-700 font-bold"
              @click="editModal = true"
            >
              编辑资料
            </button>
          </div>
          <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="h-3 w-16 bg-gray-200 rounded mb-2"></div>
              <div class="h-5 w-36 bg-gray-100 rounded"></div>
            </div>
          </div>
          <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
            <InfoItem label="用户名" :value="user?.username || '—'" />
            <InfoItem label="昵称" :value="user?.nickname || '未填写'" />
            <InfoItem label="手机号" :value="maskPhone(user?.phone)" />
            <InfoItem label="邮箱" :value="user?.email || '未填写'" />
            <InfoItem label="性别" :value="genderLabel" />
            <InfoItem label="收货地址" :value="user?.address || '未填写'" />
            <InfoItem label="注册时间" :value="createTimeLabel" />
            <InfoItem label="账户角色" :value="roleLabel" />
          </div>
        </section>

        <section class="bg-white rounded-[2rem] border border-gray-100 shadow-sm p-8">
          <div class="flex items-center justify-between mb-6">
            <div>
              <h3 class="text-xl font-black text-gray-900">饮食档案</h3>
              <p class="text-sm text-gray-500 mt-1">这是个性化推荐、健康食谱筛选和过敏风险过滤的核心依据。</p>
            </div>
            <button
              class="text-sm text-green-600 hover:text-green-700 font-bold"
              @click="dietModal = true"
            >
              维护档案
            </button>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="rounded-2xl bg-gray-50 p-5 border border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">口味偏好</p>
              <p class="text-gray-900 font-medium leading-7">{{ dietaryProfile.tastePreference || '未填写' }}</p>
            </div>
            <div class="rounded-2xl bg-gray-50 p-5 border border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">过敏原信息</p>
              <p class="text-gray-900 font-medium leading-7">{{ dietaryProfile.allergenInfo || '未填写' }}</p>
            </div>
            <div class="rounded-2xl bg-gray-50 p-5 border border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">慢病史</p>
              <p class="text-gray-900 font-medium leading-7">{{ dietaryProfile.chronicDisease || '未填写' }}</p>
            </div>
            <div class="rounded-2xl bg-gray-50 p-5 border border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">饮食忌口</p>
              <p class="text-gray-900 font-medium leading-7">{{ dietaryProfile.tabooInfo || '未填写' }}</p>
            </div>
            <div class="md:col-span-2 rounded-2xl bg-gray-50 p-5 border border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">补充说明</p>
              <p class="text-gray-900 font-medium leading-7">{{ dietaryProfile.remark || '未填写' }}</p>
            </div>
          </div>

          <div class="mt-6 flex items-center justify-between rounded-2xl border border-dashed border-green-200 bg-green-50/60 px-5 py-4">
            <div>
              <p class="text-sm font-bold text-green-700">最近一次档案更新时间</p>
              <p class="text-sm text-green-600/80 mt-1">{{ profileUpdateLabel }}</p>
            </div>
            <button
              class="px-5 py-2.5 rounded-xl bg-white text-green-700 font-bold border border-green-100 hover:border-green-300 transition-all"
              @click="dietModal = true"
            >
              立即完善
            </button>
          </div>
        </section>

        <section class="bg-white rounded-[2rem] border border-gray-100 shadow-sm p-8">
          <h3 class="text-xl font-black text-gray-900 mb-6">账户设置</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-2xl hover:bg-gray-100 transition-all" @click="openAddressEdit">
              <span class="font-medium text-gray-700">收货地址管理</span>
              <ChevronRightIcon class="w-5 h-5 text-gray-400" />
            </button>
            <button class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-2xl hover:bg-gray-100 transition-all" @click="showRecommendExplain">
              <span class="font-medium text-gray-700">推荐偏好说明</span>
              <ChevronRightIcon class="w-5 h-5 text-gray-400" />
            </button>
            <button class="w-full flex items-center justify-between p-4 bg-gray-50 rounded-2xl hover:bg-gray-100 transition-all" @click="pwdModal = true">
              <span class="font-medium text-gray-700">修改密码</span>
              <ChevronRightIcon class="w-5 h-5 text-gray-400" />
            </button>
            <button
              class="w-full flex items-center justify-between p-4 bg-red-50 rounded-2xl hover:bg-red-100 transition-all md:col-span-2"
              @click="handleLogout"
            >
              <span class="font-medium text-red-600">退出登录</span>
              <LogOutIcon class="w-5 h-5 text-red-500" />
            </button>
          </div>
        </section>
      </div>
    </div>

    <div
      v-if="editModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4"
      @click.self="editModal = false"
    >
      <div class="bg-white rounded-[2rem] p-8 w-full max-w-md shadow-2xl">
        <h3 class="text-xl font-black text-gray-900 mb-6">编辑账户资料</h3>
        <div class="space-y-4">
          <FormField label="昵称">
            <input
              v-model="editForm.nickname"
              type="text"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </FormField>
          <FormField label="手机号">
            <input
              v-model="editForm.phone"
              type="text"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </FormField>
          <FormField label="邮箱">
            <input
              v-model="editForm.email"
              type="email"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </FormField>
          <FormField label="性别">
            <select
              v-model="editForm.gender"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              <option value="">未选择</option>
              <option value="男">男</option>
              <option value="女">女</option>
              <option value="保密">保密</option>
            </select>
          </FormField>
          <FormField label="收货地址">
            <input
              v-model="editForm.address"
              type="text"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </FormField>
          <FormField label="头像">
            <ImageUpload v-model="editForm.avatar" />
          </FormField>
        </div>
        <div class="flex gap-3 mt-6">
          <button
            class="flex-1 py-3 rounded-xl font-bold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-all"
            @click="editModal = false"
          >
            取消
          </button>
          <button
            class="flex-1 py-3 rounded-xl font-bold bg-green-600 text-white hover:bg-green-700 transition-all disabled:bg-green-300"
            :disabled="saving"
            @click="handleSaveProfile"
          >
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="dietModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4"
      @click.self="dietModal = false"
    >
      <div class="bg-white rounded-[2rem] p-8 w-full max-w-2xl shadow-2xl">
        <h3 class="text-xl font-black text-gray-900 mb-6">维护饮食档案</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField label="口味偏好">
            <textarea
              v-model="dietForm.tastePreference"
              rows="3"
              placeholder="如：偏清淡、喜欢高蛋白、偏爱低糖早餐"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
            />
          </FormField>
          <FormField label="过敏原信息">
            <textarea
              v-model="dietForm.allergenInfo"
              rows="3"
              placeholder="如：花生、海鲜、乳糖不耐受"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
            />
          </FormField>
          <FormField label="慢病史">
            <textarea
              v-model="dietForm.chronicDisease"
              rows="3"
              placeholder="如：高血压、糖尿病、肠胃敏感"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
            />
          </FormField>
          <FormField label="饮食忌口">
            <textarea
              v-model="dietForm.tabooInfo"
              rows="3"
              placeholder="如：不吃辣、不吃生冷、不喝含糖饮料"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
            />
          </FormField>
          <div class="md:col-span-2">
            <FormField label="补充说明">
              <textarea
                v-model="dietForm.remark"
                rows="4"
                placeholder="如：近期在减脂，希望系统优先推荐高蛋白、低糖、临期较近的可即食食品"
                class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
              />
            </FormField>
          </div>
        </div>
        <div class="flex gap-3 mt-6">
          <button
            class="flex-1 py-3 rounded-xl font-bold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-all"
            @click="dietModal = false"
          >
            取消
          </button>
          <button
            class="flex-1 py-3 rounded-xl font-bold bg-green-600 text-white hover:bg-green-700 transition-all disabled:bg-green-300"
            :disabled="dietSaving"
            @click="handleSaveDiet"
          >
            {{ dietSaving ? '保存中...' : '保存档案' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Password Modal -->
    <div
      v-if="pwdModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4"
      @click.self="pwdModal = false"
    >
      <div class="bg-white rounded-[2rem] p-8 w-full max-w-md shadow-2xl">
        <h3 class="text-xl font-black text-gray-900 mb-6">修改密码</h3>
        <div class="space-y-4">
          <FormField label="原密码">
            <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500" />
          </FormField>
          <FormField label="新密码">
            <input v-model="pwdForm.newPassword" type="password" placeholder="至少8位，含大小写字母、数字、特殊字符"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500" />
          </FormField>
          <FormField label="确认新密码">
            <input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码"
              class="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-green-500" />
          </FormField>
        </div>
        <div v-if="pwdError" class="mt-3 text-sm text-red-600 font-medium">{{ pwdError }}</div>
        <div class="flex gap-3 mt-6">
          <button class="flex-1 py-3 rounded-xl font-bold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-all" @click="pwdModal = false">取消</button>
          <button class="flex-1 py-3 rounded-xl font-bold bg-green-600 text-white hover:bg-green-700 transition-all disabled:bg-green-300" :disabled="pwdSaving" @click="handlePasswordChange">
            {{ pwdSaving ? '修改中...' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ChevronRight as ChevronRightIcon,
  Leaf as LeafIcon,
  LogOut as LogOutIcon,
} from 'lucide-vue-next'
import { authApi, type User } from '@/api/auth'
import { pointsApi } from '@/api/points'
import { profileApi, type FoodProfileDTO, type FoodProfile } from '@/api/profile'
import { useAuthStore } from '@/stores/auth'
import { maskPhone } from '@/utils/cn'
import { getAvatarImage } from '@/utils/images'
import { useToast } from '@/composables/useToast'
import ImageUpload from '@/components/ui/ImageUpload.vue'

const router = useRouter()
const authStore = useAuthStore()
const { show: showToast } = useToast()

const user = ref<User | null>(null)
const dietaryProfile = ref<FoodProfile>({})
const pointsBalance = ref(0)
const loading = ref(true)
const saving = ref(false)
const dietSaving = ref(false)
const editModal = ref(false)
const dietModal = ref(false)
const pwdModal = ref(false)
const pwdSaving = ref(false)
const pwdError = ref('')
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const editForm = ref({
  nickname: '',
  phone: '',
  email: '',
  gender: '',
  address: '',
  avatar: '',
})

const dietForm = ref<FoodProfileDTO>({
  tastePreference: '',
  allergenInfo: '',
  chronicDisease: '',
  tabooInfo: '',
  remark: '',
})

function createEmptyDietProfile(): FoodProfile {
  return {
    tastePreference: '',
    allergenInfo: '',
    chronicDisease: '',
    tabooInfo: '',
    remark: '',
  }
}

const avatarUrl = computed(() => {
  if (user.value?.avatar) return user.value.avatar
  const seed = user.value?.userId || user.value?.username || 'default'
  return getAvatarImage(seed, 'U')
})

const membershipLabel = computed(() => {
  const rt = user.value?.roleType
  if (rt === 99) return '管理员'
  if (rt === 3) return '商户会员'
  if (rt === 2) return '骑手会员'
  if (pointsBalance.value >= 1000) return 'VIP 会员'
  return '普通会员'
})

const roleLabel = computed(() => {
  const rt = user.value?.roleType
  if (rt === 99) return '管理员'
  if (rt === 3) return '商户'
  if (rt === 2) return '骑手'
  return '普通用户'
})

const genderLabel = computed(() => {
  const map: Record<string, string> = { 男: '男', 女: '女', 保密: '保密' }
  return map[user.value?.gender || ''] || '未填写'
})

const createTimeLabel = computed(() => {
  if (!user.value?.createTime) return '—'
  return user.value.createTime.split('T')[0]
})

const profileUpdateLabel = computed(() => {
  if (!dietaryProfile.value.updateTime) return '尚未建立饮食档案'
  return dietaryProfile.value.updateTime.split('T')[0]
})

const profileCompletion = computed(() => {
  const fields = [
    user.value?.nickname,
    user.value?.phone,
    user.value?.email,
    user.value?.address,
    dietaryProfile.value.tastePreference,
    dietaryProfile.value.allergenInfo,
    dietaryProfile.value.chronicDisease,
    dietaryProfile.value.tabooInfo,
    dietaryProfile.value.remark,
  ]
  const filled = fields.filter((value) => value && String(value).trim()).length
  return Math.round((filled / fields.length) * 100)
})

async function loadProfile() {
  loading.value = true
  try {
    const [userInfo, balanceData, profile] = await Promise.all([
      authApi.getUserInfo(),
      pointsApi.getBalance(),
      profileApi.get().catch(() => createEmptyDietProfile()),
    ])
    user.value = userInfo
    pointsBalance.value = balanceData?.balance ?? 0
    authStore.setPointsBalance(pointsBalance.value)
    dietaryProfile.value = profile
    editForm.value = {
      nickname: userInfo.nickname || '',
      phone: userInfo.phone || '',
      email: userInfo.email || '',
      gender: userInfo.gender || '',
      address: userInfo.address || '',
      avatar: userInfo.avatar || '',
    }
    dietForm.value = {
      tastePreference: profile.tastePreference || '',
      allergenInfo: profile.allergenInfo || '',
      chronicDisease: profile.chronicDisease || '',
      tabooInfo: profile.tabooInfo || '',
      remark: profile.remark || '',
    }
  } catch {
    user.value = null
    dietaryProfile.value = createEmptyDietProfile()
    authStore.setPointsBalance(0)
  } finally {
    loading.value = false
  }
}

async function handleSaveProfile() {
  saving.value = true
  try {
    const updated = await authApi.updateUserInfo(editForm.value)
    user.value = updated
    editModal.value = false
    showToast('资料保存成功', 'success')
  } catch (e: unknown) {
    showToast('保存失败：' + (e instanceof Error ? e.message : '请先登录'), 'error')
  } finally {
    saving.value = false
  }
}

async function handleSaveDiet() {
  dietSaving.value = true
  try {
    const updated = await profileApi.save(dietForm.value)
    dietaryProfile.value = updated
    dietModal.value = false
    showToast('饮食档案保存成功', 'success')
  } catch (e: unknown) {
    showToast('保存失败：' + (e instanceof Error ? e.message : '请先登录'), 'error')
  } finally {
    dietSaving.value = false
  }
}

function handleLogout() {
  authStore.clearAuth()
  router.push('/login')
}

function openAddressEdit() {
  editModal.value = true
}

function showRecommendExplain() {
  showToast('系统根据您的口味偏好、过敏原、慢病史进行健康过滤和口味匹配排序，可在饮食档案中维护这些信息', 'info')
}

async function handlePasswordChange() {
  pwdError.value = ''
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword || !pwdForm.value.confirmPassword) {
    pwdError.value = '请填写所有密码字段'
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    pwdError.value = '两次输入的新密码不一致'
    return
  }
  pwdSaving.value = true
  try {
    await authApi.changePassword({ oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    pwdModal.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    showToast('密码修改成功', 'success')
  } catch (e: unknown) {
    pwdError.value = e instanceof Error ? e.message : '密码修改失败'
  } finally {
    pwdSaving.value = false
  }
}

const InfoItem = defineComponent({
  name: 'InfoItem',
  props: {
    label: { type: String, required: true },
    value: { type: String, required: true },
  },
  setup(props) {
    return () =>
      h('div', { class: 'rounded-2xl bg-gray-50 p-5 border border-gray-100' }, [
        h('p', { class: 'text-xs font-bold text-gray-400 uppercase tracking-widest mb-2' }, props.label),
        h('p', { class: 'text-gray-900 font-medium leading-7 break-all' }, props.value),
      ])
  },
})

const FormField = defineComponent({
  name: 'FormField',
  props: {
    label: { type: String, required: true },
  },
  setup(props, { slots }) {
    return () =>
      h('div', [
        h('label', { class: 'text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 block' }, props.label),
        slots.default?.(),
      ])
  },
})

onMounted(loadProfile)
</script>
