import axios from 'axios'
import { toast } from 'sonner'
import { useAuthStore } from '@/features/auth/store/authStore'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem('auth-storage')
  if (stored) {
    try {
      const { state } = JSON.parse(stored)
      if (state?.token) {
        config.headers.Authorization = `Bearer ${state.token}`
      }
    } catch {}
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      useAuthStore.getState().logout()
    } else if (status === 403) {
      toast.error('Acesso negado')
    } else if (status === 404) {
      toast.error('Recurso não encontrado')
    } else if (status === 400) {
      const msg = error.response?.data?.message || 'Dados inválidos'
      toast.error(msg)
    } else if (status >= 500) {
      toast.error('Erro interno do servidor')
    }
    return Promise.reject(error)
  }
)

export default api

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors?: { field: string; message: string }[]
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
