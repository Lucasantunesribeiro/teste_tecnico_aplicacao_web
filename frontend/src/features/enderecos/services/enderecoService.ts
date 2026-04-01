import api from '@/lib/api'
import type {
  AtualizarEnderecoRequest,
  Endereco,
  EnderecoRequest,
  ListarEnderecosParams,
  PaginatedEnderecos,
} from '../types/endereco.types'

export const enderecoService = {
  listar: (usuarioId: string): Promise<Endereco[]> =>
    api.get(`/enderecos/usuario/${usuarioId}`).then((r) => r.data),
  listarTodos: (params: ListarEnderecosParams = {}): Promise<PaginatedEnderecos> =>
    api.get('/enderecos', { params: { size: 12, ...params } }).then((r) => r.data),
  criar: (data: EnderecoRequest): Promise<Endereco> =>
    api.post('/enderecos', data).then((r) => r.data),
  atualizar: (id: string, data: AtualizarEnderecoRequest): Promise<Endereco> =>
    api.put(`/enderecos/${id}`, data).then((r) => r.data),
  deletar: (id: string): Promise<void> =>
    api.delete(`/enderecos/${id}`).then(() => undefined),
  tornarPrincipal: (id: string): Promise<Endereco> =>
    api.patch(`/enderecos/${id}/principal`).then((r) => r.data),
}
