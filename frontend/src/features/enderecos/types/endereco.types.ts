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
}

export interface EnderecoRequest {
  usuarioId: string
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
