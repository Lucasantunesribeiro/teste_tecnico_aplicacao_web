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

export interface LoginResponse {
  token: string
  expiresIn: number
  userId: string
  nome: string
  cpf: string
  tipo: 'ADMIN' | 'USER'
}

export interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (response: LoginResponse) => void
  logout: () => void
}
