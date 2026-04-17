<template>
  <span
    class="text-[10px] font-black px-2.5 py-1 rounded-lg border uppercase tracking-widest inline-flex items-center justify-center"
    :class="[style.bg, style.border, style.color]"
  >
    {{ status }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
}>()

const style = computed(() => {
  switch (props.status) {
    case '待接单':
    case '待支付':
    case '待分配':
    case '待审核':
    case '待处理':
    case '待调度':
      return { color: 'text-orange-600', bg: 'bg-orange-50', border: 'border-orange-200' }
    case '待配送':
    case '处理中':
    case '已调度':
    case '配送中':
    case '已支付':
      return { color: 'text-blue-600', bg: 'bg-blue-50', border: 'border-blue-200' }
    case '已完成':
    case '已通过':
    case '已发布':
    case '运营中':
    case '上架中':
      return { color: 'text-green-600', bg: 'bg-green-50', border: 'border-green-200' }
    case '已取消':
    case '已驳回':
    case '已异常':
    case '已停用':
    case '已停运':
    case '已下架':
      return { color: 'text-red-600', bg: 'bg-red-50', border: 'border-red-200' }
    case '审核中':
      return { color: 'text-gray-600', bg: 'bg-gray-50', border: 'border-gray-200' }
    default:
      return { color: 'text-gray-600', bg: 'bg-gray-50', border: 'border-gray-200' }
  }
})
</script>
