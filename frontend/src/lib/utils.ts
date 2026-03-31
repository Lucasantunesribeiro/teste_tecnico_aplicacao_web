import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatCPF(value: string): string {
  const digits = value.replace(/\D/g, '').slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 6) return `${digits.slice(0, 3)}.${digits.slice(3)}`
  if (digits.length <= 9) return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`
}

export function cleanCPF(value: string): string {
  return value.replace(/\D/g, '')
}

export function formatCEP(value: string): string {
  const digits = value.replace(/\D/g, '').slice(0, 8)
  if (digits.length <= 5) return digits
  return `${digits.slice(0, 5)}-${digits.slice(5)}`
}

export function cleanCEP(value: string): string {
  return value.replace(/\D/g, '')
}

export function formatDate(value: string): string {
  if (!value) return ''
  const [year, month, day] = value.split('-')
  return `${day}/${month}/${year}`
}

export function formatDateForInput(value: string): string {
  if (!value) return ''
  return value.split('T')[0]
}
