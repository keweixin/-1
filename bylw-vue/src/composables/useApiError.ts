import { useToast } from './useToast'

/**
 * 统一 API 错误处理 composable
 * 提供一致的错误消息提取和用户提示逻辑
 */
export function useApiError() {
  const { show } = useToast()

  /**
   * 处理 API 错误，提取用户友好的错误消息并弹出 Toast 提示
   * @returns 错误消息字符串
   */
  function handleError(error: unknown, fallbackMsg = '操作失败，请稍后重试'): string {
    const message = extractErrorMessage(error, fallbackMsg)
    show(message, 'error')
    return message
  }

  /**
   * 静默提取错误消息（不弹 Toast）
   */
  function extractErrorMessage(error: unknown, fallback: string = '操作失败，请稍后重试'): string {
    if (error instanceof Error) {
      return error.message || fallback
    }
    if (typeof error === 'string') {
      return error
    }
    return fallback
  }

  return { handleError, extractErrorMessage }
}
