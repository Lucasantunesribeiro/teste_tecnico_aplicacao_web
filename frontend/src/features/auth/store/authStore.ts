import { create } from 'zustand'
import type { AuthState, SessionResponse, User } from '../types/auth.types'

function toUser(response: SessionResponse): User {
  return {
    id: response.userId,
    nome: response.nome,
    cpf: response.cpf,
    tipo: response.tipo,
    status: 'ATIVO',
  }
}

export const useAuthStore = create<AuthState>()((set) => ({
  user: null,
  isAuthenticated: false,
  isBootstrapping: true,
  hasHydratedSession: false,
  setSession: (response) =>
    set({
      user: toUser(response),
      isAuthenticated: true,
      isBootstrapping: false,
      hasHydratedSession: true,
    }),
  hydrate: (response) =>
    set({
      user: response ? toUser(response) : null,
      isAuthenticated: !!response,
      isBootstrapping: false,
      hasHydratedSession: true,
    }),
  clearSession: () =>
    set({
      user: null,
      isAuthenticated: false,
      isBootstrapping: false,
      hasHydratedSession: true,
    }),
}))
