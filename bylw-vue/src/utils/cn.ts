import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/** 手机号脱敏：138****1234 */
export function maskPhone(phone: string | null | undefined): string {
  if (!phone || phone.length < 11) return phone ?? '—'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
