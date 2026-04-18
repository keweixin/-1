import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

export function useScrollReveal() {
  let observer: IntersectionObserver | null = null
  const router = useRouter()

  function observe() {
    // Reset: make all previous elements visible (cleanup for route changes)
    document.querySelectorAll('.reveal-section:not(.visible)').forEach((el) => {
      observer?.observe(el)
    })
  }

  onMounted(() => {
    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible')
            observer?.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.05, rootMargin: '0px 0px -20px 0px' },
    )

    // Initial observation
    requestAnimationFrame(() => {
      observe()
    })

    // Safety: ensure everything visible after 3 seconds
    setTimeout(() => {
      document.querySelectorAll('.reveal-section:not(.visible)').forEach((el) => {
        el.classList.add('visible')
      })
    }, 3000)
  })

  // Re-observe on route change
  router.afterEach(() => {
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        observe()
      })
    })
  })

  onUnmounted(() => {
    observer?.disconnect()
  })
}
