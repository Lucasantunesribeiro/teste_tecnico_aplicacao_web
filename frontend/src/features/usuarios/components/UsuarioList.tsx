import { Loader2 } from 'lucide-react'
import { UsuarioCard } from './UsuarioCard'
import { useUsuarios, useDeleteUsuario } from '../hooks/useUsuarios'
import { useAuth } from '@/features/auth/hooks/useAuth'

export function UsuarioList() {
  const { data, isLoading } = useUsuarios()
  const { mutate: deleteUsuario } = useDeleteUsuario()
  const { isAdmin } = useAuth()

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  const usuarios = data?.content ?? []

  if (usuarios.length === 0) {
    return (
      <p className="text-center text-muted-foreground py-8">
        Nenhum usuário encontrado.
      </p>
    )
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {usuarios.map((u) => (
        <UsuarioCard
          key={u.id}
          usuario={u}
          canDelete={isAdmin}
          onDelete={deleteUsuario}
        />
      ))}
    </div>
  )
}
