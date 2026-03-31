import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { enderecoService } from '../services/enderecoService'
import { cepService } from '../services/cepService'
import type { EnderecoRequest } from '../types/endereco.types'

export function useEnderecos(usuarioId: string) {
  return useQuery({
    queryKey: ['enderecos', usuarioId],
    queryFn: () => enderecoService.listar(usuarioId),
    enabled: !!usuarioId,
  })
}

export function useCreateEndereco() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: EnderecoRequest) => enderecoService.criar(data),
    onSuccess: (_, variables) => {
      toast.success('Endereço adicionado!')
      queryClient.invalidateQueries({ queryKey: ['enderecos', variables.usuarioId] })
    },
  })
}

export function useDeleteEndereco(usuarioId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => enderecoService.deletar(id),
    onSuccess: () => {
      toast.success('Endereço removido!')
      queryClient.invalidateQueries({ queryKey: ['enderecos', usuarioId] })
    },
  })
}

export function useSetPrincipal(usuarioId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => enderecoService.tornarPrincipal(id),
    onSuccess: () => {
      toast.success('Endereço principal atualizado!')
      queryClient.invalidateQueries({ queryKey: ['enderecos', usuarioId] })
    },
  })
}

export function useConsultaCep() {
  return useMutation({
    mutationFn: (cep: string) => cepService.consultar(cep),
  })
}
