import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ProtectedRoute } from '@/features/auth/components/ProtectedRoute'
import { MainLayout } from '@/components/layout/MainLayout'
import { LoginPage } from '@/pages/LoginPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { UsuariosPage } from '@/pages/UsuariosPage'
import { UsuarioDetailPage } from '@/pages/UsuarioDetailPage'
import { EnderecosPage } from '@/pages/EnderecosPage'

export function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<MainLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/enderecos" element={<EnderecosPage />} />
            <Route element={<ProtectedRoute requireAdmin />}>
              <Route path="/usuarios" element={<UsuariosPage />} />
              <Route path="/usuarios/:id" element={<UsuarioDetailPage />} />
            </Route>
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
