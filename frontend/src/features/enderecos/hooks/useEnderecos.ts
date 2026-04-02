import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { cepService } from '../services/cepService'
import { enderecoService } from '../services/enderecoService'
import type {
  AtualizarEnderecoRequest,
  EnderecoRequest,
  ListarEnderecosParams,
} from '../types/endereco.types'

interface QueryOptions {
  enabled?: boolean
}

export function useEnderecos(usuarioId: string) {
  return useQuery({
    queryKey: ['enderecos', usuarioId],
    queryFn: () => enderecoService.listar(usuarioId),
    enabled: !!usuarioId,
  })
}

export function useAdminEnderecos(params: ListarEnderecosParams = {}, options: QueryOptions = {}) {
  return useQuery({
    queryKey: ['enderecos-admin', params],
    queryFn: () => enderecoService.listarTodos(params),
    enabled: options.enabled ?? true,
  })
}

export function useCreateEndereco() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: EnderecoRequest) => enderecoService.criar(data),
    onSuccess: () => {
      toast.success('Endereço adicionado!')
      queryClient.invalidateQueries({ queryKey: ['enderecos'] })
      queryClient.invalidateQueries({ queryKey: ['enderecos-admin'] })
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })
}

export function useUpdateEndereco() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: AtualizarEnderecoRequest }) =>
      enderecoService.atualizar(id, data),
    onSuccess: () => {
      toast.success('Endereço atualizado!')
      queryClient.invalidateQueries({ queryKey: ['enderecos'] })
      queryClient.invalidateQueries({ queryKey: ['enderecos-admin'] })
    },
  })
}

export function useDeleteEndereco() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => enderecoService.deletar(id),
    onSuccess: () => {
      toast.success('Endereco removido!')
      queryClient.invalidateQueries({ queryKey: ['enderecos'] })
      queryClient.invalidateQueries({ queryKey: ['enderecos-admin'] })
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    },
  })
}

export function useSetPrincipal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => enderecoService.tornarPrincipal(id),
    onSuccess: () => {
      toast.success('Endereco principal atualizado!')
      queryClient.invalidateQueries({ queryKey: ['enderecos'] })
      queryClient.invalidateQueries({ queryKey: ['enderecos-admin'] })
    },
  })
}

export function useConsultaCep() {
  return useMutation({
    mutationFn: (cep: string) => cepService.consultar(cep),
  })
}
