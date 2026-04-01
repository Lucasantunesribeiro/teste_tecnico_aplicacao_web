import { Loader2 } from 'lucide-react'
import { EnderecoCard } from './EnderecoCard'
import { useEnderecos } from '../hooks/useEnderecos'

interface Props {
  usuarioId: string
}

export function EnderecoList({ usuarioId }: Props) {
  const { data: enderecos, isLoading, isError } = useEnderecos(usuarioId)

  if (isLoading) {
    return (
      <div className="flex justify-center py-4">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    )
  }

  if (isError) {
    return (
      <p className="rounded-md border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
        Não foi possível carregar os endereços.
      </p>
    )
  }

  if (!enderecos?.length) {
    return (
      <p className="py-4 text-center text-sm text-muted-foreground">
        Nenhum endereço cadastrado.
      </p>
    )
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2">
      {enderecos.map((e) => (
        <EnderecoCard key={e.id} endereco={e} />
      ))}
    </div>
  )
}
