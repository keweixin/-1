<template>
  <Transition name="loading-bar">
    <div v-if="loading" class="loading-bar" :style="{ width: progress + '%' }"></div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const progress = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

function start() {
  loading.value = true
  progress.value = 0
  let tick = 0
  timer = setInterval(() => {
    tick++
    // Ease out: fast at start, slow near end
    progress.value = Math.min(90, progress.value + (90 - progress.value) * 0.08 + 1)
    if (tick > 200) clearInterval(timer!)
  }, 50)
}

function finish() {
  if (timer) clearInterval(timer)
  progress.value = 100
  setTimeout(() => {
    loading.value = false
    progress.value = 0
  }, 500)
}

watch(() => router.currentRoute.value.path, () => {
  start()
  // Finish after a short delay to allow the new page to render
  setTimeout(finish, 300)
})
</script>

<style scoped>
.loading-bar-enter-from {
  opacity: 0;
}
.loading-bar-leave-active {
  transition: opacity 0.4s ease 0.2s;
}
.loading-bar-leave-to {
  opacity: 0;
}
</style>
