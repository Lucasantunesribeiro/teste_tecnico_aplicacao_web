import { User, Trash2 } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatCPF, formatDate } from '@/lib/utils'
import type { Usuario } from '../types/usuario.types'

interface Props {
  usuario: Usuario
  onDelete?: (id: string) => void
  canDelete?: boolean
}

export function UsuarioCard({ usuario, onDelete, canDelete }: Props) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-base font-semibold flex items-center gap-2">
          <User className="h-4 w-4" />
          {usuario.nome}
        </CardTitle>
        <div className="flex items-center gap-2">
          <Badge variant={usuario.tipo === 'ADMIN' ? 'default' : 'secondary'}>
            {usuario.tipo}
          </Badge>
          <Badge variant={usuario.status === 'ATIVO' ? 'outline' : 'destructive'}>
            {usuario.status}
          </Badge>
          {canDelete && onDelete && (
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-destructive"
              onClick={() => onDelete(usuario.id)}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="text-sm text-muted-foreground space-y-1">
        <p>CPF: {formatCPF(usuario.cpf)}</p>
        <p>Nascimento: {formatDate(usuario.dataNascimento)}</p>
      </CardContent>
    </Card>
  )
}
