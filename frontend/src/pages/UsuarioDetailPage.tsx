import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, Loader2, PlusCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { useUsuario } from '@/features/usuarios/hooks/useUsuarios'
import { useEnderecos } from '@/features/enderecos/hooks/useEnderecos'
import { EnderecoForm } from '@/features/enderecos/components/EnderecoForm'
import { EnderecoList } from '@/features/enderecos/components/EnderecoList'
import { formatCPF, formatDate } from '@/lib/utils'

export function UsuarioDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [open, setOpen] = useState(false)
  const { data: usuario, isLoading, isError } = useUsuario(id ?? '')
  const { data: enderecos } = useEnderecos(id ?? '')

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  if (isError || !usuario) {
    return (
      <div className="space-y-4">
        <Button variant="ghost" asChild>
          <Link to="/usuarios">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Voltar
          </Link>
        </Button>
        <p className="rounded-md border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
          Não foi possível carregar os dados do usuário.
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link to="/usuarios">
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>
        <div className="min-w-0 flex-1">
          <h1 className="truncate text-2xl font-bold">{usuario.nome}</h1>
          <p className="text-sm text-muted-foreground">Visualização e gestão de endereços do usuário</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex flex-wrap items-center gap-2">
            Dados do Usuário
            <Badge>{usuario.tipo}</Badge>
            <Badge variant={usuario.status === 'ATIVO' ? 'outline' : 'destructive'}>
              {usuario.status}
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 text-sm md:grid-cols-2">
          <div>
            <span className="font-medium">CPF:</span> {formatCPF(usuario.cpf)}
          </div>
          <div>
            <span className="font-medium">Nascimento:</span> {formatDate(usuario.dataNascimento)}
          </div>
          <div>
            <span className="font-medium">Endereços:</span> {enderecos?.length ?? 0}
          </div>
        </CardContent>
      </Card>

      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold">Endereços</h2>
          <p className="text-sm text-muted-foreground">Adicione, edite, remova ou defina o principal.</p>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>
              <PlusCircle className="mr-2 h-4 w-4" />
              Novo endereço
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Adicionar endereço</DialogTitle>
            </DialogHeader>
            <EnderecoForm usuarioId={usuario.id} onSuccess={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>

      <EnderecoList usuarioId={usuario.id} />
    </div>
  )
}
