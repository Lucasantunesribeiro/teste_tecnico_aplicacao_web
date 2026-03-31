import { Users, MapPin, ShieldCheck } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { useUsuarios } from '@/features/usuarios/hooks/useUsuarios'
import { useEnderecos } from '@/features/enderecos/hooks/useEnderecos'

export function DashboardPage() {
  const { user, isAdmin } = useAuth()
  const { data: usuariosData } = useUsuarios()
  const { data: enderecos } = useEnderecos(user?.id ?? '')

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">
          Bem-vindo, {user?.nome}!{' '}
          <Badge variant={isAdmin ? 'default' : 'secondary'}>{user?.tipo}</Badge>
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Meus Endereços</CardTitle>
            <MapPin className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{enderecos?.length ?? 0}</div>
            <Button variant="link" className="px-0 text-sm" asChild>
              <Link to="/enderecos">Gerenciar endereços</Link>
            </Button>
          </CardContent>
        </Card>

        {isAdmin && (
          <>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Total de Usuários</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{usuariosData?.totalElements ?? 0}</div>
                <Button variant="link" className="px-0 text-sm" asChild>
                  <Link to="/usuarios">Ver usuários</Link>
                </Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Acesso Admin</CardTitle>
                <ShieldCheck className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Você tem acesso total ao sistema
                </p>
              </CardContent>
            </Card>
          </>
        )}
      </div>
    </div>
  )
}
