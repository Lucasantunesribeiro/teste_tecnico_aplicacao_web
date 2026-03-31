import { MapPin, Trash2, Star } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatCEP } from '@/lib/utils'
import { useDeleteEndereco, useSetPrincipal } from '../hooks/useEnderecos'
import type { Endereco } from '../types/endereco.types'

interface Props {
  endereco: Endereco
}

export function EnderecoCard({ endereco }: Props) {
  const { mutate: deletar, isPending: deleting } = useDeleteEndereco(endereco.usuarioId)
  const { mutate: setPrincipal, isPending: setting } = useSetPrincipal(endereco.usuarioId)

  return (
    <Card className={endereco.principal ? 'border-primary' : ''}>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-semibold flex items-center gap-2">
          <MapPin className="h-4 w-4" />
          {formatCEP(endereco.cep)}
        </CardTitle>
        <div className="flex items-center gap-1">
          {endereco.principal && <Badge>Principal</Badge>}
          {!endereco.principal && (
            <Button
              variant="ghost"
              size="icon"
              className="h-7 w-7"
              onClick={() => setPrincipal(endereco.id)}
              disabled={setting}
            >
              <Star className="h-3 w-3" />
            </Button>
          )}
          <Button
            variant="ghost"
            size="icon"
            className="h-7 w-7 text-destructive"
            onClick={() => deletar(endereco.id)}
            disabled={deleting}
          >
            <Trash2 className="h-3 w-3" />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="text-xs text-muted-foreground space-y-0.5">
        <p>{endereco.logradouro}, {endereco.numero}{endereco.complemento ? `, ${endereco.complemento}` : ''}</p>
        <p>{endereco.bairro} — {endereco.cidade}/{endereco.estado}</p>
      </CardContent>
    </Card>
  )
}
