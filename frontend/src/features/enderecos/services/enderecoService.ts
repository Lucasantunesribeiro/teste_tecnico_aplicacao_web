import api from '@/lib/api'
import type { Endereco, EnderecoRequest } from '../types/endereco.types'

export const enderecoService = {
  listar: (usuarioId: string): Promise<Endereco[]> =>
    api.get('/enderecos', { params: { usuarioId } }).then((r) => r.data),
  criar: (data: EnderecoRequest): Promise<Endereco> =>
    api.post('/enderecos', data).then((r) => r.data),
  deletar: (id: string): Promise<void> =>
    api.delete(`/enderecos/${id}`).then(() => undefined),
  tornarPrincipal: (id: string): Promise<Endereco> =>
    api.put(`/enderecos/${id}/principal`).then((r) => r.data),
}
