import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthState, LoginResponse } from '../types/auth.types'

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (response: LoginResponse) =>
        set({
          user: {
            id: response.userId,
            nome: response.nome,
            cpf: response.cpf,
            tipo: response.tipo,
            status: 'ATIVO',
          },
          token: response.token,
          isAuthenticated: true,
        }),
      logout: () =>
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        }),
    }),
    { name: 'auth-storage' }
  )
)
