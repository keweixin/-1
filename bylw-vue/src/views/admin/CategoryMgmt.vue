<template>
  <div class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-gray-900">食品分类管理</h1>
        <p class="mt-2 text-sm text-gray-500">管理食品分类体系，包括分类名称、描述与排序。</p>
      </div>
      <button class="bg-green-600 text-white px-6 py-3 rounded-2xl font-bold hover:bg-green-700 transition-all shadow-lg shadow-green-100" @click="openCreateModal">
        添加分类
      </button>
    </div>

    <div v-if="feedback" class="rounded-2xl border px-5 py-4 text-sm font-medium"
      :class="feedback.type === 'success' ? 'border-green-100 bg-green-50 text-green-700' : 'border-red-100 bg-red-50 text-red-700'">
      {{ feedback.message }}
    </div>

    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">分类名称</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">描述</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">排序</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">状态</th>
            <th class="px-8 py-4 text-xs font-bold text-gray-400 uppercase tracking-wider text-left">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading">
            <td colspan="5" class="px-8 py-12 text-center text-gray-400">
              <div class="flex items-center justify-center">
                <div class="w-6 h-6 border-2 border-green-600 border-t-transparent rounded-full animate-spin mr-3"></div>
                加载中...
              </div>
            </td>
          </tr>
          <tr v-else-if="categories.length === 0">
            <td colspan="5" class="px-8 py-12 text-center text-gray-400">暂无分类数据</td>
          </tr>
          <tr v-else v-for="cat in categories" :key="cat.categoryId" class="hover:bg-gray-50 transition-colors">
            <td class="px-8 py-6 text-sm font-medium text-gray-900">{{ cat.categoryName }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ cat.description || '-' }}</td>
            <td class="px-8 py-6 text-sm text-gray-600">{{ cat.sortNo ?? '-' }}</td>
            <td class="px-8 py-6">
              <StatusTag :status="cat.status === 1 ? '启用' : '禁用'" />
            </td>
            <td class="px-8 py-6">
              <div class="flex items-center gap-3">
                <button class="text-green-600 hover:underline text-sm font-medium" @click="openEditModal(cat)">编辑</button>
                <button class="text-red-600 hover:underline text-sm font-medium" @click="handleDelete(cat.categoryId)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="modalVisible" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-center justify-center px-4" @click.self="closeModal">
      <div class="w-full max-w-lg bg-white rounded-[32px] shadow-2xl p-8">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-black text-gray-900">{{ form.categoryId ? '编辑分类' : '添加分类' }}</h3>
          <button class="p-2 rounded-xl hover:bg-gray-100 text-gray-400" @click="closeModal">
            <XIcon class="w-5 h-5" />
          </button>
        </div>
        <div class="space-y-4">
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">分类名称</span>
            <input v-model="form.categoryName" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" placeholder="请输入分类名称" />
          </label>
          <label class="space-y-2 block">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">描述</span>
            <input v-model="form.description" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" placeholder="分类描述（可选）" />
          </label>
          <div class="grid grid-cols-2 gap-4">
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">排序号</span>
              <input v-model.number="form.sortNo" type="number" min="0" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500" />
            </label>
            <label class="space-y-2 block">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">状态</span>
              <select v-model.number="form.status" class="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-green-500">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
          </div>
        </div>
        <div class="flex items-center justify-end gap-3 mt-8">
          <button class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-bold hover:bg-gray-50 transition-all" @click="closeModal">取消</button>
          <button class="px-5 py-3 rounded-2xl bg-green-600 text-white font-bold hover:bg-green-700 transition-all disabled:bg-green-300" :disabled="submitting" @click="submitForm">
            {{ submitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { foodApi, type FoodCategory } from '@/api/food'
import StatusTag from '@/components/ui/StatusTag.vue'
import { X } from 'lucide-vue-next'

const XIcon = X

interface FeedbackState { type: 'success' | 'error'; message: string }

const categories = ref<FoodCategory[]>([])
const loading = ref(true)
const modalVisible = ref(false)
const submitting = ref(false)
const feedback = ref<FeedbackState | null>(null)

const form = reactive({ categoryId: null as number | null, categoryName: '', description: '', sortNo: 0, status: 1 })

function setFeedback(type: FeedbackState['type'], message: string) {
  feedback.value = { type, message }
  setTimeout(() => { feedback.value = null }, 3000)
}

async function loadCategories() {
  loading.value = true
  try {
    categories.value = await foodApi.getCategories()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '加载分类失败')
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  Object.assign(form, { categoryId: null, categoryName: '', description: '', sortNo: 0, status: 1 })
  modalVisible.value = true
}

function openEditModal(cat: FoodCategory) {
  Object.assign(form, {
    categoryId: cat.categoryId,
    categoryName: cat.categoryName,
    description: cat.description || '',
    sortNo: cat.sortNo ?? 0,
    status: cat.status ?? 1,
  })
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
}

async function submitForm() {
  if (!form.categoryName.trim()) {
    setFeedback('error', '分类名称不能为空')
    return
  }
  submitting.value = true
  try {
    if (form.categoryId) {
      await foodApi.updateCategory(form)
      setFeedback('success', '分类已更新')
    } else {
      await foodApi.saveCategory(form)
      setFeedback('success', '分类已添加')
    }
    closeModal()
    await loadCategories()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定要删除此分类吗？')) return
  try {
    await foodApi.deleteCategory(id)
    setFeedback('success', '分类已删除')
    await loadCategories()
  } catch (e: unknown) {
    setFeedback('error', e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(loadCategories)
</script>
