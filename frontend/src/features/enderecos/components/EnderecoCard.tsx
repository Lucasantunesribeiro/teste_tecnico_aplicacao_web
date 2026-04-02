import { useState } from 'react'
import { Link } from 'react-router-dom'
import { MapPin, Pencil, Star, Trash2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { formatCEP, formatCPF } from '@/lib/utils'
import { useDeleteEndereco, useSetPrincipal } from '../hooks/useEnderecos'
import type { Endereco } from '../types/endereco.types'
import { EnderecoForm } from './EnderecoForm'

interface Props {
  endereco: Endereco
  showOwner?: boolean
}

export function EnderecoCard({ endereco, showOwner = false }: Props) {
  const [editOpen, setEditOpen] = useState(false)
  const deletarEndereco = useDeleteEndereco()
  const setPrincipalEndereco = useSetPrincipal()

  return (
    <Card className={endereco.principal ? 'border-primary' : ''}>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="flex items-center gap-2 text-sm font-semibold">
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
              onClick={() => setPrincipalEndereco.mutate(endereco.id)}
              disabled={setPrincipalEndereco.isPending}
              aria-label="Definir como principal"
            >
              <Star className="h-3 w-3" />
            </Button>
          )}
          <Dialog open={editOpen} onOpenChange={setEditOpen}>
            <DialogTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="h-7 w-7"
                aria-label="Editar endereço"
              >
                <Pencil className="h-3 w-3" />
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Editar endereço</DialogTitle>
              </DialogHeader>
              <EnderecoForm
                usuarioId={endereco.usuarioId}
                endereco={endereco}
                onSuccess={() => setEditOpen(false)}
              />
            </DialogContent>
          </Dialog>
          <Button
            variant="ghost"
            size="icon"
            className="h-7 w-7 text-destructive"
            onClick={() => deletarEndereco.mutate(endereco.id)}
            disabled={deletarEndereco.isPending}
            aria-label="Excluir endereco"
          >
            <Trash2 className="h-3 w-3" />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-0.5 text-xs text-muted-foreground">
        <p>
          {endereco.logradouro}, {endereco.numero}
          {endereco.complemento ? `, ${endereco.complemento}` : ''}
        </p>
        <p>
          {endereco.bairro} - {endereco.cidade}/{endereco.estado}
        </p>
        {showOwner && endereco.usuarioNome && (
          <div className="pt-2">
            <p className="font-medium text-foreground">{endereco.usuarioNome}</p>
            {endereco.usuarioCpf && <p>CPF: {formatCPF(endereco.usuarioCpf)}</p>}
            <Button variant="link" className="h-auto px-0 text-xs" asChild>
              <Link to={`/usuarios/${endereco.usuarioId}`}>Abrir usuario</Link>
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
