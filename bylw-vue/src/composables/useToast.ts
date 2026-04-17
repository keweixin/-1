import { ref } from 'vue'

interface ToastItem {
  id: number
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
  leaving?: boolean
}

const queue = ref<ToastItem[]>([])
let nextId = 0
let timer: ReturnType<typeof setTimeout> | null = null

export function useToast() {
  function show(msg: string, toastType: 'success' | 'error' | 'warning' | 'info' = 'info', duration = 3000) {
    const id = nextId++
    queue.value.push({ id, message: msg, type: toastType })
    scheduleNext(duration)
  }

  function scheduleNext(duration: number) {
    if (timer) clearTimeout(timer)
    // duration: 显示时长；+300ms: 入场动画结束后开始离场；+200ms: 离场动画时长
    timer = setTimeout(() => {
      if (queue.value.length === 0) return
      // 标记离开（触发 CSS leave 动画），等动画完成后再从数组移除
      queue.value[0].leaving = true
      setTimeout(() => {
        queue.value.shift()
        if (queue.value.length > 0) {
          scheduleNext(duration)
        }
      }, 250) // 与 .toast-leave-active transition 时间一致
    }, duration + 300)
  }

  return { queue, show }
}
