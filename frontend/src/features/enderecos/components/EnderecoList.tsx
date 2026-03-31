import { Loader2 } from 'lucide-react'
import { EnderecoCard } from './EnderecoCard'
import { useEnderecos } from '../hooks/useEnderecos'

interface Props {
  usuarioId: string
}

export function EnderecoList({ usuarioId }: Props) {
  const { data: enderecos, isLoading } = useEnderecos(usuarioId)

  if (isLoading) {
    return (
      <div className="flex justify-center py-4">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    )
  }

  if (!enderecos?.length) {
    return <p className="text-sm text-muted-foreground text-center py-4">Nenhum endereço cadastrado.</p>
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2">
      {enderecos.map((e) => (
        <EnderecoCard key={e.id} endereco={e} />
      ))}
    </div>
  )
}
