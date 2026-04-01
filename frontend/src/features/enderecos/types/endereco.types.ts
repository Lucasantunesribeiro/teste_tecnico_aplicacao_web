import type { PaginatedResponse } from '@/lib/api'

export interface Endereco {
  id: string
  cep: string
  logradouro: string
  numero: string
  complemento?: string
  bairro: string
  cidade: string
  estado: string
  principal: boolean
  usuarioId: string
  usuarioNome?: string
  usuarioCpf?: string
}

export interface EnderecoRequest {
  usuarioId: string
  cep: string
  numero: string
  complemento?: string
  principal: boolean
}

export interface AtualizarEnderecoRequest {
  cep?: string
  numero?: string
  complemento?: string
}

export interface ListarEnderecosParams {
  page?: number
  size?: number
  usuarioId?: string
  principal?: boolean
  cep?: string
  cidade?: string
  estado?: string
}

export interface EnderecoFormValues {
  cep: string
  numero: string
  complemento?: string
  principal: boolean
}

export interface CepResponse {
  cep: string
  logradouro: string
  complemento: string
  bairro: string
  localidade: string
  uf: string
}

export type PaginatedEnderecos = PaginatedResponse<Endereco>
