import { resolveImage, getFoodCategoryImage } from '@/utils/images'

/**
 * 图片加载错误处理 composable
 * 在 <img @error="onImageError"> 中使用，加载失败时自动切换到 fallback
 */
export function useImageError() {
  /**
   * 处理图片加载错误事件，将 img.src 替换为 fallback
   */
  function onImageError(event: Event, fallback?: string) {
    const img = event.target as HTMLImageElement
    if (img && img.src !== fallback) {
      img.src = fallback || getFoodCategoryImage()
    }
  }

  /**
   * 安全获取图片 URL，优先使用原始 URL，失败时返回 fallback
   */
  function safeImageUrl(url: string | null | undefined, fallback: string): string {
    return resolveImage(url, fallback)
  }

  return { onImageError, safeImageUrl }
}
