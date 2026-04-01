import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { usuarioService } from '../services/usuarioService'
import type { UsuarioRequest } from '../types/usuario.types'

interface UseUsuariosOptions {
  enabled?: boolean
}

export function useUsuarios(params = {}, options: UseUsuariosOptions = {}) {
  return useQuery({
    queryKey: ['usuarios', params],
    queryFn: () => usuarioService.listar(params),
    enabled: options.enabled ?? true,
  })
}

export function useUsuario(id: string) {
  return useQuery({
    queryKey: ['usuarios', id],
    queryFn: () => usuarioService.buscarPorId(id),
    enabled: !!id,
  })
}

export function useCreateUsuario() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: UsuarioRequest) => usuarioService.criar(data),
    onSuccess: () => {
      toast.success('Usuário criado com sucesso!')
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })
}

export function useUpdateUsuario() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<UsuarioRequest> }) =>
      usuarioService.atualizar(id, data),
    onSuccess: () => {
      toast.success('Usuário atualizado!')
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })
}

export function useDeleteUsuario() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => usuarioService.deletar(id),
    onSuccess: () => {
      toast.success('Usuário removido!')
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })
}
