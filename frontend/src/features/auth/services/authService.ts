import api from '@/lib/api'
import type { LoginRequest, LoginResponse } from '../types/auth.types'

export const authService = {
  login: (data: LoginRequest): Promise<LoginResponse> =>
    api.post<LoginResponse>('/auth/login', data).then((r) => r.data),
}
