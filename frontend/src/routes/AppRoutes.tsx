import { lazy, Suspense } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { MainLayout } from '@/components/layout/MainLayout'
import { AuthBootstrap } from '@/features/auth/components/AuthBootstrap'
import { ProtectedRoute } from '@/features/auth/components/ProtectedRoute'
import { routerFuture } from './routerFuture'

const LoginPage = lazy(() => import('@/pages/LoginPage').then((module) => ({ default: module.LoginPage })))
const DashboardPage = lazy(() =>
  import('@/pages/DashboardPage').then((module) => ({ default: module.DashboardPage }))
)
const UsuariosPage = lazy(() =>
  import('@/pages/UsuariosPage').then((module) => ({ default: module.UsuariosPage }))
)
const UsuarioDetailPage = lazy(() =>
  import('@/pages/UsuarioDetailPage').then((module) => ({ default: module.UsuarioDetailPage }))
)
const EnderecosPage = lazy(() =>
  import('@/pages/EnderecosPage').then((module) => ({ default: module.EnderecosPage }))
)
const AdminEnderecosPage = lazy(() =>
  import('@/pages/AdminEnderecosPage').then((module) => ({ default: module.AdminEnderecosPage }))
)

function RouteFallback() {
  return (
    <div className="flex min-h-[50vh] items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
    </div>
  )
}

export function AppRoutes() {
  return (
    <BrowserRouter future={routerFuture}>
      <AuthBootstrap>
        <Suspense fallback={<RouteFallback />}>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route element={<ProtectedRoute />}>
              <Route element={<MainLayout />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/enderecos" element={<EnderecosPage />} />
                <Route element={<ProtectedRoute requireAdmin />}>
                  <Route path="/enderecos/admin" element={<AdminEnderecosPage />} />
                  <Route path="/usuarios" element={<UsuariosPage />} />
                  <Route path="/usuarios/:id" element={<UsuarioDetailPage />} />
                </Route>
              </Route>
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Suspense>
      </AuthBootstrap>
    </BrowserRouter>
  )
}
