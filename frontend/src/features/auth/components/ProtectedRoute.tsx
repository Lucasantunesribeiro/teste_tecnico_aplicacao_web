import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

interface Props {
  requireAdmin?: boolean
}

export function ProtectedRoute({ requireAdmin = false }: Props) {
  const { isAuthenticated, user } = useAuthStore()

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (requireAdmin && user?.tipo !== 'ADMIN') return <Navigate to="/dashboard" replace />

  return <Outlet />
}
