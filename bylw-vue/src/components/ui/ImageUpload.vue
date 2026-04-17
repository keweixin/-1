<template>
  <div class="space-y-3">
    <div
      v-if="previewUrl"
      class="relative group rounded-xl overflow-hidden border border-gray-100"
    >
      <img :src="previewUrl" alt="预览" class="w-full h-40 object-cover" referrerpolicy="no-referrer" />
      <button
        @click="removeImage"
        class="absolute top-2 right-2 w-7 h-7 bg-red-500 text-white rounded-lg flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <XIcon class="w-4 h-4" />
      </button>
    </div>

    <label
      v-else
      class="flex flex-col items-center justify-center h-32 border-2 border-dashed border-gray-200 rounded-xl cursor-pointer hover:border-green-400 hover:bg-green-50/30 transition-all"
      :class="{ 'opacity-50 pointer-events-none': uploading }"
    >
      <div v-if="uploading" class="flex items-center gap-2 text-gray-400">
        <div class="w-5 h-5 border-2 border-green-600 border-t-transparent rounded-full animate-spin"></div>
        <span class="text-sm font-medium">上传中...</span>
      </div>
      <template v-else>
        <UploadIcon class="w-6 h-6 text-gray-300 mb-2" />
        <span class="text-sm font-bold text-gray-400">点击上传图片</span>
        <span class="text-xs text-gray-300 mt-1">JPG / PNG / WEBP，最大 10MB</span>
      </template>
      <input type="file" accept="image/*" class="hidden" @change="handleUpload" />
    </label>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { api } from '@/api'
import { Upload as UploadIcon, X as XIcon } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const { show: showToast } = useToast()

const props = defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const previewUrl = ref(props.modelValue || '')
const uploading = ref(false)

watch(() => props.modelValue, (val) => {
  previewUrl.value = val || ''
})

async function handleUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.size > 10 * 1024 * 1024) {
    showToast('文件大小不能超过10MB', 'error')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)

    const token = localStorage.getItem('token')
    const res = await fetch('/api/upload/image', {
      method: 'POST',
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
      body: formData,
    })

    const json = await res.json()
    if (json.code !== 200) {
      throw new Error(json.message || '上传失败')
    }

    const url = json.data as string
    previewUrl.value = url
    emit('update:modelValue', url)
  } catch (e: unknown) {
    showToast(e instanceof Error ? e.message : '上传失败', 'error')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

function removeImage() {
  previewUrl.value = ''
  emit('update:modelValue', '')
}
</script>
