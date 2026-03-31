import api from '@/lib/api'
import type { Usuario, UsuarioRequest, PaginatedUsuarios } from '../types/usuario.types'

interface ListParams {
  page?: number
  size?: number
  nome?: string
  cpf?: string
}

export const usuarioService = {
  listar: (params: ListParams = {}): Promise<PaginatedUsuarios> =>
    api.get('/usuarios', { params: { size: 10, ...params } }).then((r) => r.data),
  buscarPorId: (id: string): Promise<Usuario> =>
    api.get(`/usuarios/${id}`).then((r) => r.data),
  criar: (data: UsuarioRequest): Promise<Usuario> =>
    api.post('/usuarios', data).then((r) => r.data),
  atualizar: (id: string, data: Partial<UsuarioRequest>): Promise<Usuario> =>
    api.put(`/usuarios/${id}`, data).then((r) => r.data),
  deletar: (id: string): Promise<void> =>
    api.delete(`/usuarios/${id}`).then(() => undefined),
}
