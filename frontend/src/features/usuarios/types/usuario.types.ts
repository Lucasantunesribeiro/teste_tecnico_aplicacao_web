import type { PaginatedResponse } from '@/lib/api'

export interface Usuario {
  id: string
  nome: string
  cpf: string
  dataNascimento: string
  tipo: 'ADMIN' | 'USER'
  status: 'ATIVO' | 'INATIVO'
}

export interface UsuarioRequest {
  nome: string
  cpf: string
  dataNascimento: string
  senha: string
}

export type PaginatedUsuarios = PaginatedResponse<Usuario>
