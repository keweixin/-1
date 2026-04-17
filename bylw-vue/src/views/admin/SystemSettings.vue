<template>
  <div class="space-y-8">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
      <div>
        <h1 class="text-2xl font-black text-gray-900">系统设置</h1>
        <p class="mt-2 text-sm text-gray-500">维护系统公告、轮播图、友情链接以及前台关于我们内容。</p>
      </div>
      <button
        class="inline-flex items-center gap-2 bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all shadow-lg shadow-green-100"
        @click="saveAboutUs"
      >
        <SaveIcon class="w-4 h-4" />
        保存关于我们
      </button>
    </div>

    <div v-if="feedback" class="rounded-2xl px-5 py-4 text-sm font-medium" :class="feedback.type === 'success' ? 'bg-green-50 text-green-700 border border-green-100' : 'bg-red-50 text-red-700 border border-red-100'">
      {{ feedback.message }}
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6">
      <div v-for="item in overviewCards" :key="item.label" class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6">
        <p class="text-xs font-bold text-gray-400 uppercase tracking-wider">{{ item.label }}</p>
        <div class="mt-3 flex items-end justify-between">
          <h3 class="text-3xl font-black text-gray-900">{{ item.value }}</h3>
          <component :is="item.icon" class="w-6 h-6 text-green-600" />
        </div>
      </div>
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-2 flex flex-wrap gap-2">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="px-5 py-3 rounded-2xl text-sm font-bold transition-all"
        :class="activeTab === tab.key ? 'bg-green-600 text-white shadow-lg shadow-green-100' : 'text-gray-500 hover:bg-gray-50'"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="activeTab === 'announcement'" class="space-y-6">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-bold text-gray-900">公告管理</h2>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="openAnnouncementModal()">
          新增公告
        </button>
      </div>
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <div v-for="item in announcements" :key="item.id" class="bg-white rounded-3xl border border-gray-100 shadow-sm p-6">
          <div class="flex items-start justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-2">
                <span class="text-sm font-black text-gray-900">{{ item.title }}</span>
              </div>
              <p class="text-sm text-gray-500 leading-6">{{ item.content }}</p>
            </div>
            <StatusTag :status="item.status === 1 ? '已发布' : '已停用'" />
          </div>
          <div class="mt-6 flex items-center justify-between text-xs text-gray-400">
            <span>{{ item.publishTime || '刚刚更新' }}</span>
            <div class="flex items-center gap-3">
              <button class="text-green-600 font-bold hover:underline" @click="openAnnouncementModal(item)">编辑</button>
              <button class="text-red-600 font-bold hover:underline" @click="removeAnnouncement(item.id)">删除</button>
            </div>
          </div>
        </div>
      </div>
      <div v-if="announcements.length === 0" class="bg-white rounded-3xl border border-dashed border-gray-200 py-16 text-center text-gray-400">
        暂无公告，点击右上角新增第一条公告。
      </div>
    </div>

    <div v-else-if="activeTab === 'banner'" class="space-y-6">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-bold text-gray-900">轮播图管理</h2>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="openBannerModal()">
          新增轮播图
        </button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        <div v-for="item in banners" :key="item.id" class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
          <img :src="getBannerImage(item.imageUrl)" :alt="item.title" class="h-44 w-full object-cover" referrerPolicy="no-referrer" />
          <div class="p-6">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h3 class="font-bold text-gray-900">{{ item.title }}</h3>
                <p class="mt-1 text-sm text-gray-500 break-all">{{ item.targetUrl }}</p>
              </div>
              <StatusTag :status="item.status === 1 ? '已发布' : '已停用'" />
            </div>
            <div class="mt-5 flex items-center justify-between text-xs text-gray-400">
              <span>排序 {{ item.sortNo }}</span>
              <div class="flex items-center gap-3">
                <button class="text-green-600 font-bold hover:underline" @click="openBannerModal(item)">编辑</button>
                <button class="text-red-600 font-bold hover:underline" @click="removeBanner(item.id)">删除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="banners.length === 0" class="bg-white rounded-3xl border border-dashed border-gray-200 py-16 text-center text-gray-400">
        暂无轮播图，点击右上角新增轮播图。
      </div>
    </div>

    <div v-else-if="activeTab === 'link'" class="space-y-6">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-bold text-gray-900">友情链接管理</h2>
        <button class="bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all" @click="openLinkModal()">
          新增友情链接
        </button>
      </div>
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50">
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">名称</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">链接</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">排序</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">状态</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="item in friendlyLinks" :key="item.id" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4 font-bold text-gray-900">{{ item.name }}</td>
              <td class="px-6 py-4 text-sm text-gray-500 break-all">{{ item.url }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ item.sortNo }}</td>
              <td class="px-6 py-4"><StatusTag :status="item.status === 1 ? '已发布' : '已停用'" /></td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <button class="text-green-600 font-bold hover:underline" @click="openLinkModal(item)">编辑</button>
                  <button class="text-red-600 font-bold hover:underline" @click="removeLink(item.id)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="friendlyLinks.length === 0" class="py-16 text-center text-gray-400">
          暂无友情链接，点击右上角新增友情链接。
        </div>
      </div>
    </div>

    <div v-else-if="activeTab === 'recommend'" class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-bold text-gray-900">推荐算法权重配置</h2>
          <p class="mt-1 text-sm text-gray-500">调整协同过滤与特征匹配的融合权重，实时影响前台推荐结果。</p>
        </div>
        <button
          class="inline-flex items-center gap-2 bg-green-600 text-white px-5 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all shadow-lg shadow-green-100"
          @click="saveRecommendConfig"
          :disabled="savingRecommend"
        >
          <SaveIcon class="w-4 h-4" />
          {{ savingRecommend ? '保存中...' : '保存配置' }}
        </button>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-8 space-y-6">
          <h3 class="font-bold text-gray-900">算法权重</h3>
          <div class="space-y-6">
            <div>
              <div class="flex items-center justify-between mb-2">
                <label class="text-sm font-medium text-gray-700">协同过滤权重 (α)</label>
                <span class="text-sm font-bold text-green-600">{{ recommendConfig.alpha.toFixed(2) }}</span>
              </div>
              <input
                v-model.number="recommendConfig.alpha"
                type="range"
                min="0" max="1" step="0.05"
                class="w-full h-2 bg-gray-100 rounded-lg appearance-none cursor-pointer accent-green-600"
              />
              <p class="mt-1 text-xs text-gray-400">数值越大，推荐结果越依赖用户行为相似度</p>
            </div>
            <div>
              <div class="flex items-center justify-between mb-2">
                <label class="text-sm font-medium text-gray-700">特征匹配权重 (β)</label>
                <span class="text-sm font-bold text-green-600">{{ recommendConfig.beta.toFixed(2) }}</span>
              </div>
              <input
                v-model.number="recommendConfig.beta"
                type="range"
                min="0" max="1" step="0.05"
                class="w-full h-2 bg-gray-100 rounded-lg appearance-none cursor-pointer accent-green-600"
              />
              <p class="mt-1 text-xs text-gray-400">数值越大，推荐结果越依赖口味/标签/疾病匹配</p>
            </div>
            <div>
              <div class="flex items-center justify-between mb-2">
                <label class="text-sm font-medium text-gray-700">时间衰减因子 (λ)</label>
                <span class="text-sm font-bold text-green-600">{{ recommendConfig.lambda.toFixed(2) }}</span>
              </div>
              <input
                v-model.number="recommendConfig.lambda"
                type="range"
                min="0" max="1" step="0.05"
                class="w-full h-2 bg-gray-100 rounded-lg appearance-none cursor-pointer accent-green-600"
              />
              <p class="mt-1 text-xs text-gray-400">数值越大，近期行为的权重越高</p>
            </div>
            <div>
              <div class="flex items-center justify-between mb-2">
                <label class="text-sm font-medium text-gray-700">行为窗口天数</label>
                <span class="text-sm font-bold text-green-600">{{ recommendConfig.behaviorWindowDays }}天</span>
              </div>
              <input
                v-model.number="recommendConfig.behaviorWindowDays"
                type="range"
                min="7" max="90" step="1"
                class="w-full h-2 bg-gray-100 rounded-lg appearance-none cursor-pointer accent-green-600"
              />
              <p class="mt-1 text-xs text-gray-400">参与协同过滤计算的历史行为天数范围</p>
            </div>
          </div>
          <div class="rounded-2xl bg-gray-50 border border-gray-100 p-4">
            <p class="text-xs font-medium text-gray-500">推荐得分公式：Score = α × CF_score + β × FM_score + λ × recency_decay</p>
            <p class="mt-1 text-xs text-gray-400">α + β = 1（系统自动归一化）</p>
          </div>
        </div>

        <div class="space-y-6">
          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-8">
            <h3 class="font-bold text-gray-900 mb-4">快捷预设方案</h3>
            <div class="grid grid-cols-1 gap-3">
              <button
                v-for="preset in presetSchemes"
                :key="preset.name"
                class="flex items-center justify-between p-4 rounded-2xl border border-gray-100 hover:border-green-400 hover:bg-green-50 transition-all text-left"
                @click="applyPreset(preset)"
              >
                <div>
                  <p class="font-bold text-gray-900">{{ preset.name }}</p>
                  <p class="mt-0.5 text-xs text-gray-500">{{ preset.desc }}</p>
                </div>
                <div class="text-right text-xs text-gray-400 shrink-0 ml-4">
                  <p>α={{ preset.alpha }}</p>
                  <p>β={{ preset.beta }}</p>
                  <p>λ={{ preset.lambda }}</p>
                </div>
              </button>
            </div>
          </div>

          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-8">
            <h3 class="font-bold text-gray-900 mb-4">当前配置预览</h3>
            <div class="space-y-3 text-sm">
              <div class="flex items-center justify-between">
                <span class="text-gray-500">协同过滤权重 α</span>
                <span class="font-bold text-gray-900">{{ recommendConfig.alpha.toFixed(2) }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-gray-500">特征匹配权重 β</span>
                <span class="font-bold text-gray-900">{{ recommendConfig.beta.toFixed(2) }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-gray-500">时间衰减因子 λ</span>
                <span class="font-bold text-gray-900">{{ recommendConfig.lambda.toFixed(2) }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-gray-500">行为窗口天数</span>
                <span class="font-bold text-gray-900">{{ recommendConfig.behaviorWindowDays }}天</span>
              </div>
              <div class="flex items-center justify-between pt-3 border-t border-gray-100">
                <span class="text-gray-500">α + β 总计</span>
                <span class="font-bold" :class="Math.abs((recommendConfig.alpha + recommendConfig.beta) - 1) < 0.01 ? 'text-green-600' : 'text-red-600'">
                  {{ (recommendConfig.alpha + recommendConfig.beta).toFixed(2) }}
                </span>
              </div>
            </div>
            <div v-if="Math.abs((recommendConfig.alpha + recommendConfig.beta) - 1) >= 0.01" class="mt-3 rounded-xl bg-red-50 border border-red-100 p-3 text-xs text-red-600">
              警告：α + β 必须等于 1，系统将自动归一化
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="grid grid-cols-1 xl:grid-cols-[1.2fr_0.8fr] gap-8">
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-8 space-y-6">
        <div>
          <h2 class="text-lg font-bold text-gray-900">关于我们</h2>
          <p class="mt-2 text-sm text-gray-500">这里会同步到前台“关于我们”和联系方式相关区域。</p>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <label class="space-y-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">平台名称</span>
            <input v-model="aboutForm.platformName" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">联系电话</span>
            <input v-model="aboutForm.phone" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">邮箱</span>
            <input v-model="aboutForm.email" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">地址</span>
            <input v-model="aboutForm.address" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 md:col-span-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">简介</span>
            <textarea v-model="aboutForm.introText" rows="6" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500 resize-none" />
          </label>
          <label class="space-y-2 md:col-span-2">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">封面图</span>
            <ImageUpload v-model="aboutForm.coverImg" />
          </label>
        </div>
      </div>
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-8">
        <h3 class="font-bold text-gray-900 mb-4">内容预览</h3>
        <div class="space-y-3 text-sm text-gray-600">
          <p><span class="font-bold text-gray-900">平台：</span>{{ aboutForm.platformName }}</p>
          <p><span class="font-bold text-gray-900">电话：</span>{{ aboutForm.phone }}</p>
          <p><span class="font-bold text-gray-900">邮箱：</span>{{ aboutForm.email }}</p>
          <p><span class="font-bold text-gray-900">地址：</span>{{ aboutForm.address }}</p>
          <p><span class="font-bold text-gray-900">简介：</span>{{ aboutForm.introText || '暂无简介' }}</p>
        </div>
        <img
          v-if="aboutForm.coverImg"
          :src="getBannerImage(aboutForm.coverImg)"
          alt="关于我们封面"
          class="mt-6 h-48 w-full rounded-2xl object-cover"
          referrerPolicy="no-referrer"
        />
      </div>
    </div>

    <div v-if="modalVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4">
      <div class="w-full max-w-2xl bg-white rounded-[32px] shadow-2xl p-8 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">{{ modalTitle }}</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="closeModal">
            <XIcon class="w-5 h-5" />
          </button>
        </div>

        <div v-if="modalType === 'announcement'" class="space-y-4">
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">标题</span>
            <input v-model="announcementForm.title" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">内容</span>
            <textarea v-model="announcementForm.content" rows="6" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500 resize-none" />
          </label>
          <label class="flex items-center gap-2 text-sm font-medium text-gray-600">
            <input v-model.number="announcementForm.status" type="checkbox" class="rounded border-gray-300 text-green-600 focus:ring-green-500" :true-value="1" :false-value="0" />
            启用
          </label>
        </div>

        <div v-else-if="modalType === 'banner'" class="space-y-4">
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">标题</span>
            <input v-model="bannerForm.title" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">轮播图片</span>
            <ImageUpload v-model="bannerForm.imageUrl" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">跳转地址</span>
            <input v-model="bannerForm.targetUrl" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <div class="grid grid-cols-2 gap-4">
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">排序</span>
              <input v-model.number="bannerForm.sortNo" type="number" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
            </label>
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">状态</span>
              <select v-model.number="bannerForm.status" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500">
                <option :value="1">启用</option>
                <option :value="0">停用</option>
              </select>
            </label>
          </div>
        </div>

        <div v-else class="space-y-4">
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">名称</span>
            <input v-model="linkForm.name" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">链接</span>
            <input v-model="linkForm.url" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
          </label>
          <div class="grid grid-cols-2 gap-4">
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">排序</span>
              <input v-model.number="linkForm.sortNo" type="number" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
            </label>
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">状态</span>
              <select v-model.number="linkForm.status" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500">
                <option :value="1">启用</option>
                <option :value="0">停用</option>
              </select>
            </label>
          </div>
        </div>

        <div class="flex items-center justify-end gap-3 mt-8">
          <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="closeModal">
            取消
          </button>
          <button class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all" @click="submitModal">
            保存
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { adminApi, type AboutUsDTO, type AnnouncementDTO, type BannerDTO, type FriendlyLinkDTO } from '@/api/admin'
import StatusTag from '@/components/ui/StatusTag.vue'
import { Bell, Image as ImageIcon, Link2, Megaphone, Save, X } from 'lucide-vue-next'
import { getBannerImage } from '@/utils/images'
import ImageUpload from '@/components/ui/ImageUpload.vue'

const SaveIcon = Save
const XIcon = X
const activeTab = ref<'announcement' | 'banner' | 'link' | 'about' | 'recommend'>('announcement')
const tabs = [
  { key: 'announcement', label: '公告' },
  { key: 'banner', label: '轮播图' },
  { key: 'link', label: '友情链接' },
  { key: 'about', label: '关于我们' },
  { key: 'recommend', label: '推荐配置' },
] as const

const announcements = ref<AnnouncementDTO[]>([])
const banners = ref<BannerDTO[]>([])
const friendlyLinks = ref<FriendlyLinkDTO[]>([])
const feedback = ref<{ type: 'success' | 'error'; message: string } | null>(null)

const recommendConfig = reactive({
  alpha: 0.6,
  beta: 0.4,
  lambda: 0.1,
  behaviorWindowDays: 30,
})
const savingRecommend = ref(false)

const presetSchemes = [
  { name: '默认平衡方案', desc: '适合大多数场景', alpha: 0.6, beta: 0.4, lambda: 0.1, behaviorWindowDays: 30 },
  { name: '侧重协同过滤', desc: '依赖用户行为数据', alpha: 0.8, beta: 0.2, lambda: 0.1, behaviorWindowDays: 30 },
  { name: '侧重特征匹配', desc: '依赖口味/标签匹配', alpha: 0.2, beta: 0.8, lambda: 0.1, behaviorWindowDays: 30 },
  { name: '短期行为敏感', desc: '强调近期购买', alpha: 0.6, beta: 0.4, lambda: 0.3, behaviorWindowDays: 14 },
  { name: '长期兴趣', desc: '覆盖更长时间', alpha: 0.6, beta: 0.4, lambda: 0.05, behaviorWindowDays: 60 },
]

const aboutForm = reactive<AboutUsDTO>({
  platformName: '',
  introText: '',
  phone: '',
  email: '',
  address: '',
  coverImg: '',
})

const overviewCards = computed(() => [
  { label: '公告数量', value: announcements.value.length, icon: Megaphone },
  { label: '轮播图数量', value: banners.value.length, icon: ImageIcon },
  { label: '友情链接数量', value: friendlyLinks.value.length, icon: Link2 },
  { label: '系统状态', value: '在线', icon: Bell },
])

const modalVisible = ref(false)
const modalType = ref<'announcement' | 'banner' | 'link'>('announcement')
const editingId = ref<number | null>(null)
const announcementForm = reactive<Partial<AnnouncementDTO>>({ title: '', content: '', status: 1 })
const bannerForm = reactive<Partial<BannerDTO>>({ title: '', imageUrl: '', targetUrl: '', sortNo: 1, status: 1 })
const linkForm = reactive<Partial<FriendlyLinkDTO>>({ name: '', url: '', sortNo: 1, status: 1 })

const modalTitle = computed(() => {
  if (modalType.value === 'announcement') return editingId.value ? '编辑公告' : '新增公告'
  if (modalType.value === 'banner') return editingId.value ? '编辑轮播图' : '新增轮播图'
  return editingId.value ? '编辑友情链接' : '新增友情链接'
})

function setFeedback(type: 'success' | 'error', message: string) {
  feedback.value = { type, message }
  window.setTimeout(() => {
    feedback.value = null
  }, 3000)
}

async function loadData() {
  try {
    const settings = await adminApi.getSettings()
    announcements.value = settings.announcements
    banners.value = settings.banners
    friendlyLinks.value = settings.friendlyLinks
    Object.assign(aboutForm, settings.aboutUs)
  } catch {
    announcements.value = []
    banners.value = []
    friendlyLinks.value = []
  }
  await loadRecommendConfig()
}

async function loadRecommendConfig() {
  try {
    const config = await adminApi.getRecommendConfig()
    Object.assign(recommendConfig, config)
  } catch {
    // ignore
  }
}

async function saveRecommendConfig() {
  savingRecommend.value = true
  try {
    await adminApi.saveRecommendConfig({ ...recommendConfig })
    setFeedback('success', '推荐配置已保存，实时生效')
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '保存失败')
  } finally {
    savingRecommend.value = false
  }
}

function applyPreset(preset: typeof presetSchemes[number]) {
  recommendConfig.alpha = preset.alpha
  recommendConfig.beta = preset.beta
  recommendConfig.lambda = preset.lambda
  recommendConfig.behaviorWindowDays = preset.behaviorWindowDays
}

function openAnnouncementModal(item?: AnnouncementDTO) {
  modalType.value = 'announcement'
  editingId.value = item?.id ?? null
  announcementForm.title = item?.title ?? ''
  announcementForm.content = item?.content ?? ''
  announcementForm.status = item?.status ?? 1
  modalVisible.value = true
}

function openBannerModal(item?: BannerDTO) {
  modalType.value = 'banner'
  editingId.value = item?.id ?? null
  bannerForm.title = item?.title ?? ''
  bannerForm.imageUrl = item?.imageUrl ?? ''
  bannerForm.targetUrl = item?.targetUrl ?? ''
  bannerForm.sortNo = item?.sortNo ?? 1
  bannerForm.status = item?.status ?? 1
  modalVisible.value = true
}

function openLinkModal(item?: FriendlyLinkDTO) {
  modalType.value = 'link'
  editingId.value = item?.id ?? null
  linkForm.name = item?.name ?? ''
  linkForm.url = item?.url ?? ''
  linkForm.sortNo = item?.sortNo ?? 1
  linkForm.status = item?.status ?? 1
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
  editingId.value = null
}

async function submitModal() {
  try {
    if (modalType.value === 'announcement') {
      const payload = { ...announcementForm, id: editingId.value ?? undefined }
      if (editingId.value) {
        await adminApi.updateAnnouncement(payload)
      } else {
        await adminApi.saveAnnouncement(payload)
      }
      setFeedback('success', '公告已保存')
    } else if (modalType.value === 'banner') {
      const payload = { ...bannerForm, id: editingId.value ?? undefined }
      if (editingId.value) {
        await adminApi.updateBanner(payload)
      } else {
        await adminApi.saveBanner(payload)
      }
      setFeedback('success', '轮播图已保存')
    } else {
      const payload = { ...linkForm, id: editingId.value ?? undefined }
      if (editingId.value) {
        await adminApi.updateFriendlyLink(payload)
      } else {
        await adminApi.saveFriendlyLink(payload)
      }
      setFeedback('success', '友情链接已保存')
    }
    closeModal()
    await loadData()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '保存失败')
  }
}

async function saveAboutUs() {
  try {
    await adminApi.saveAboutUs({ ...aboutForm })
    setFeedback('success', '关于我们已保存')
    await loadData()
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '保存失败')
  }
}

async function removeAnnouncement(id: number) {
  try {
    await adminApi.deleteAnnouncement(id)
    announcements.value = announcements.value.filter((item) => item.id !== id)
    setFeedback('success', '公告已删除')
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '删除失败')
  }
}

async function removeBanner(id: number) {
  try {
    await adminApi.deleteBanner(id)
    banners.value = banners.value.filter((item) => item.id !== id)
    setFeedback('success', '轮播图已删除')
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '删除失败')
  }
}

async function removeLink(id: number) {
  try {
    await adminApi.deleteFriendlyLink(id)
    friendlyLinks.value = friendlyLinks.value.filter((item) => item.id !== id)
    setFeedback('success', '友情链接已删除')
  } catch (error) {
    setFeedback('error', error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(loadData)
</script>
