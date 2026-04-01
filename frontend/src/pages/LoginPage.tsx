import { Navigate } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { LoginForm } from '@/features/auth/components/LoginForm'
import { useAuthStore } from '@/features/auth/store/authStore'

export function LoginPage() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.18),_transparent_34%),linear-gradient(180deg,_#f8fafc_0%,_#eef2ff_100%)]">
      <div className="mx-auto grid min-h-screen max-w-6xl lg:grid-cols-[1.1fr_0.9fr]">
        <section className="hidden flex-col justify-between p-12 lg:flex">
          <div className="max-w-lg space-y-6">
            <p className="text-sm font-semibold uppercase tracking-[0.3em] text-slate-500">
              Solution TI
            </p>
            <h1 className="text-5xl font-semibold tracking-tight text-slate-900">
              Cadastro de usuários e endereços com autenticação segura.
            </h1>
            <p className="text-lg text-slate-600">
              Fluxo completo com cookies httpOnly, RBAC, ViaCEP e interface responsiva para
              controle de usuários e múltiplos endereços.
            </p>
          </div>

          <div className="grid grid-cols-3 gap-4 text-sm text-slate-600">
            <div className="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm backdrop-blur">
              <p className="font-semibold text-slate-900">Sessão</p>
              <p>Cookies seguros com renovação automática</p>
            </div>
            <div className="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm backdrop-blur">
              <p className="font-semibold text-slate-900">ViaCEP</p>
              <p>Busca automática de endereço</p>
            </div>
            <div className="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm backdrop-blur">
              <p className="font-semibold text-slate-900">RBAC</p>
              <p>Controle por perfil</p>
            </div>
          </div>
        </section>

        <main className="flex items-center justify-center px-4 py-10 sm:px-6 lg:px-12">
          <Card className="w-full max-w-md border-white/70 bg-white/90 shadow-2xl backdrop-blur">
            <CardHeader className="space-y-1">
              <CardTitle className="text-center text-2xl font-bold">
                Gerenciamento de Usuários
              </CardTitle>
              <CardDescription className="text-center">
                Faça login para acessar o sistema
              </CardDescription>
            </CardHeader>
            <CardContent>
              <LoginForm />
            </CardContent>
          </Card>
        </main>
      </div>
    </div>
  )
}
