import api from '@/lib/api'
import type { LoginRequest, LoginResponse } from '../types/auth.types'

export const authService = {
  login: (data: LoginRequest): Promise<LoginResponse> =>
    api.post<LoginResponse>('/auth/login', data).then((r) => r.data),
  me: (): Promise<LoginResponse> => api.get<LoginResponse>('/auth/me').then((r) => r.data),
  refresh: (): Promise<LoginResponse> =>
    api.post<LoginResponse>('/auth/refresh').then((r) => r.data),
  logout: (): Promise<void> => api.post('/auth/logout').then(() => undefined),
}
