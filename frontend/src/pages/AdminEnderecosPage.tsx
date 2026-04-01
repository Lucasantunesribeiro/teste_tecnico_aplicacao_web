import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowLeft, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { EnderecoCard } from '@/features/enderecos/components/EnderecoCard'
import { useAdminEnderecos } from '@/features/enderecos/hooks/useEnderecos'

export function AdminEnderecosPage() {
  const [cidade, setCidade] = useState('')
  const [estado, setEstado] = useState('')
  const [principalFilter, setPrincipalFilter] = useState<'all' | 'true' | 'false'>('all')
  const [page, setPage] = useState(0)

  const params = useMemo(
    () => ({
      page,
      size: 12,
      cidade: cidade.trim() || undefined,
      estado: estado.trim() || undefined,
      principal: principalFilter === 'all' ? undefined : principalFilter === 'true',
    }),
    [cidade, estado, principalFilter, page]
  )

  const query = useAdminEnderecos(params)
  const enderecos = query.data?.content ?? []

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link to="/dashboard">
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl font-bold">Auditoria de Enderecos</h1>
          <p className="text-muted-foreground">
            Consulte enderecos de todo o sistema com filtros e paginacao.
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Filtros</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-4">
          <div className="space-y-2">
            <Label htmlFor="cidade">Cidade</Label>
            <Input
              id="cidade"
              value={cidade}
              onChange={(event) => {
                setCidade(event.target.value)
                setPage(0)
              }}
              placeholder="Sao Paulo"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="estado">Estado</Label>
            <Input
              id="estado"
              value={estado}
              onChange={(event) => {
                setEstado(event.target.value.toUpperCase())
                setPage(0)
              }}
              maxLength={2}
              placeholder="SP"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="principal">Tipo</Label>
            <select
              id="principal"
              value={principalFilter}
              onChange={(event) => {
                setPrincipalFilter(event.target.value as 'all' | 'true' | 'false')
                setPage(0)
              }}
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
            >
              <option value="all">Todos</option>
              <option value="true">Somente principal</option>
              <option value="false">Somente secundario</option>
            </select>
          </div>
          <div className="flex items-end">
            <Button
              variant="outline"
              className="w-full"
              onClick={() => {
                setCidade('')
                setEstado('')
                setPrincipalFilter('all')
                setPage(0)
              }}
            >
              Limpar filtros
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold">Resultados</h2>
          <p className="text-sm text-muted-foreground">
            {query.data ? `${query.data.totalElements} enderecos encontrados` : 'Carregando enderecos'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            onClick={() => setPage((current) => Math.max(0, current - 1))}
            disabled={page === 0 || query.isLoading}
          >
            Anterior
          </Button>
          <Button
            variant="outline"
            onClick={() => setPage((current) => current + 1)}
            disabled={query.isLoading || !query.data || query.data.last}
          >
            Proxima
          </Button>
        </div>
      </div>

      {query.isLoading && (
        <div className="flex justify-center py-8">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      )}

      {query.isError && (
        <p className="rounded-md border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
          Nao foi possivel carregar a auditoria de enderecos.
        </p>
      )}

      {!query.isLoading && !query.isError && enderecos.length === 0 && (
        <p className="py-8 text-center text-muted-foreground">Nenhum endereco encontrado.</p>
      )}

      {!query.isLoading && !query.isError && enderecos.length > 0 && (
        <div className="grid gap-3 lg:grid-cols-2">
          {enderecos.map((endereco) => (
            <EnderecoCard key={endereco.id} endereco={endereco} showOwner />
          ))}
        </div>
      )}
    </div>
  )
}
