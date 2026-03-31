import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useUsuario } from '@/features/usuarios/hooks/useUsuarios'
import { formatCPF, formatDate } from '@/lib/utils'

export function UsuarioDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { data: usuario, isLoading } = useUsuario(id ?? '')

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  if (!usuario) return <p>Usuário não encontrado.</p>

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link to="/usuarios"><ArrowLeft className="h-4 w-4" /></Link>
        </Button>
        <h1 className="text-2xl font-bold">{usuario.nome}</h1>
      </div>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Dados do Usuário
            <Badge>{usuario.tipo}</Badge>
            <Badge variant={usuario.status === 'ATIVO' ? 'outline' : 'destructive'}>
              {usuario.status}
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-2 gap-4 text-sm">
          <div><span className="font-medium">CPF:</span> {formatCPF(usuario.cpf)}</div>
          <div><span className="font-medium">Nascimento:</span> {formatDate(usuario.dataNascimento)}</div>
        </CardContent>
      </Card>
    </div>
  )
}
