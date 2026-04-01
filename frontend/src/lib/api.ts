import axios, { AxiosHeaders, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { toast } from 'sonner'
import { useAuthStore } from '@/features/auth/store/authStore'
import type { SessionResponse } from '@/features/auth/types/auth.types'
import { queryClient } from './queryClient'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
  _csrfRetry?: boolean
}

let refreshPromise: Promise<SessionResponse> | null = null

function readCookie(name: string) {
  if (typeof document === 'undefined') {
    return null
  }

  const prefix = `${name}=`
  return document.cookie
    .split(';')
    .map((segment) => segment.trim())
    .find((segment) => segment.startsWith(prefix))
    ?.slice(prefix.length) ?? null
}

api.interceptors.request.use((request) => {
  const method = request.method?.toLowerCase()
  const requiresCsrf = ['post', 'put', 'patch', 'delete'].includes(method ?? '')

  if (requiresCsrf) {
    const csrfToken = readCookie('XSRF-TOKEN')
    if (csrfToken) {
      const headerValue = decodeURIComponent(csrfToken)
      if (typeof request.headers?.set === 'function') {
        request.headers.set('X-XSRF-TOKEN', headerValue)
      } else {
        request.headers = AxiosHeaders.from(request.headers ?? {})
        request.headers.set('X-XSRF-TOKEN', headerValue)
      }
    }
  }

  return request
})

function isAuthSessionEndpoint(url?: string) {
  return ['/auth/login', '/auth/logout', '/auth/me', '/auth/refresh'].some((path) =>
    url?.includes(path)
  )
}

function isMutatingRequest(method?: string) {
  return ['post', 'put', 'patch', 'delete'].includes(method?.toLowerCase() ?? '')
}

async function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post<SessionResponse>(
        '/api/auth/refresh',
        undefined,
        {
          headers: { 'Content-Type': 'application/json' },
          withCredentials: true,
          xsrfCookieName: 'XSRF-TOKEN',
          xsrfHeaderName: 'X-XSRF-TOKEN',
        }
      )
      .then((response) => {
        useAuthStore.getState().setSession(response.data)
        return response.data
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiError>) => {
    const status = error.response?.status
    const payload = error.response?.data
    const request = error.config as RetryableRequestConfig | undefined
    const url = request?.url

    if (status === 401 && request && !request._retry && !isAuthSessionEndpoint(url)) {
      request._retry = true

      try {
        await refreshSession()
        return api(request)
      } catch {
        useAuthStore.getState().clearSession()
        queryClient.clear()
      }
    } else if (
      status === 403 &&
      request &&
      isMutatingRequest(request.method) &&
      !request._csrfRetry &&
      !isAuthSessionEndpoint(url)
    ) {
      request._csrfRetry = true

      try {
        await api.get<SessionResponse>('/auth/me')
        return api(request)
      } catch {
        // Se a sessao nao puder ser reidratada, cai no tratamento padrao abaixo.
      }
    } else if (status === 401) {
      useAuthStore.getState().clearSession()
      queryClient.clear()
    } else if (status === 403) {
      toast.error('Acesso negado')
    } else if (status === 404) {
      toast.error('Recurso não encontrado')
    } else if (status === 400) {
      const msg = payload?.fieldErrors?.[0]?.message || payload?.message || 'Dados inválidos'
      toast.error(msg)
    } else if (status === 429) {
      const msg =
        payload?.message || 'Muitas tentativas. Aguarde antes de tentar novamente.'
      toast.error(msg)
    } else if (status && status >= 500) {
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
  requestId?: string
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
