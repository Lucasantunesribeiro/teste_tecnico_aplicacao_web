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

export function isValidCPF(value: string): boolean {
  const cpf = cleanCPF(value)

  if (!/^\d{11}$/.test(cpf)) return false
  if (/^(\d)\1{10}$/.test(cpf)) return false

  const digits = cpf.split('').map(Number)

  const calcDigit = (sliceLength: number) => {
    const weightStart = sliceLength + 1
    const sum = digits.slice(0, sliceLength).reduce((acc, digit, index) => {
      return acc + digit * (weightStart - index)
    }, 0)
    const remainder = sum % 11
    return remainder < 2 ? 0 : 11 - remainder
  }

  const firstDigit = calcDigit(9)
  const secondDigit = calcDigit(10)

  return digits[9] === firstDigit && digits[10] === secondDigit
}

export function formatCEP(value: string): string {
  const digits = value.replace(/\D/g, '').slice(0, 8)
  if (digits.length <= 5) return digits
  return `${digits.slice(0, 5)}-${digits.slice(5)}`
}

export function cleanCEP(value: string): string {
  return value.replace(/\D/g, '')
}

export function isValidCEP(value: string): boolean {
  return /^\d{8}$/.test(cleanCEP(value))
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
