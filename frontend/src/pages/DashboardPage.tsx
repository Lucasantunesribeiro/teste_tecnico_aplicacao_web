import { MapPin, ShieldCheck, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { useAdminEnderecos, useEnderecos } from '@/features/enderecos/hooks/useEnderecos'
import { useUsuarios } from '@/features/usuarios/hooks/useUsuarios'

export function DashboardPage() {
  const { user, isAdmin } = useAuth()
  const usuariosQuery = useUsuarios({}, { enabled: isAdmin })
  const enderecosQuery = useEnderecos(user?.id ?? '')
  const enderecosAdminQuery = useAdminEnderecos({ page: 0, size: 1 }, { enabled: isAdmin })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <div className="flex flex-wrap items-center gap-2 text-muted-foreground">
          <span>Bem-vindo, {user?.nome}!</span>
          <Badge variant={isAdmin ? 'default' : 'secondary'}>{user?.tipo}</Badge>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Meus Enderecos</CardTitle>
            <MapPin className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {enderecosQuery.isLoading ? '--' : enderecosQuery.data?.length ?? 0}
            </div>
            {enderecosQuery.isError ? (
              <p className="text-sm text-destructive">Nao foi possivel carregar seus enderecos.</p>
            ) : (
              <Button variant="link" className="px-0 text-sm" asChild>
                <Link to="/enderecos">Gerenciar enderecos</Link>
              </Button>
            )}
          </CardContent>
        </Card>

        {isAdmin && (
          <>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Total de Usuarios</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {usuariosQuery.isLoading ? '--' : usuariosQuery.data?.totalElements ?? 0}
                </div>
                {usuariosQuery.isError ? (
                  <p className="text-sm text-destructive">Nao foi possivel carregar os usuarios.</p>
                ) : (
                  <Button variant="link" className="px-0 text-sm" asChild>
                    <Link to="/usuarios">Ver usuarios</Link>
                  </Button>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Acesso Admin</CardTitle>
                <ShieldCheck className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Voce pode auditar usuarios e enderecos em todo o sistema.
                </p>
                <Button variant="link" className="px-0 text-sm" asChild>
                  <Link to="/enderecos/admin">Ver enderecos globais</Link>
                </Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Enderecos Globais</CardTitle>
                <MapPin className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {enderecosAdminQuery.isLoading ? '--' : enderecosAdminQuery.data?.totalElements ?? 0}
                </div>
                {enderecosAdminQuery.isError ? (
                  <p className="text-sm text-destructive">Nao foi possivel carregar os enderecos.</p>
                ) : (
                  <Button variant="link" className="px-0 text-sm" asChild>
                    <Link to="/enderecos/admin">Abrir auditoria administrativa</Link>
                  </Button>
                )}
              </CardContent>
            </Card>
          </>
        )}
      </div>
    </div>
  )
}
