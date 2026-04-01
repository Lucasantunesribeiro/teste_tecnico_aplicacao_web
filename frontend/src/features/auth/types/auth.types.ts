export interface User {
  id: string
  nome: string
  cpf: string
  tipo: 'ADMIN' | 'USER'
  status: 'ATIVO' | 'INATIVO'
}

export interface LoginRequest {
  cpf: string
  senha: string
}

export interface SessionResponse {
  expiresIn: number
  userId: string
  nome: string
  cpf: string
  tipo: 'ADMIN' | 'USER'
}

export type LoginResponse = SessionResponse

export interface AuthState {
  user: User | null
  isAuthenticated: boolean
  isBootstrapping: boolean
  hasHydratedSession: boolean
  setSession: (response: SessionResponse) => void
  hydrate: (response: SessionResponse | null) => void
  clearSession: () => void
}
