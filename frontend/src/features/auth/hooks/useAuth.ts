import { useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authService } from '../services/authService'
import { useAuthStore } from '../store/authStore'

export function useAuth() {
  const { user, isBootstrapping, clearSession } = useAuthStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const isAdmin = user?.tipo === 'ADMIN'

  const logout = async () => {
    try {
      await authService.logout()
    } catch {
      // A limpeza local precisa acontecer mesmo se o backend não responder.
    } finally {
      clearSession()
      queryClient.clear()
      toast.info('Sessão encerrada')
      navigate('/login', { replace: true })
    }
  }

  return { user, isAdmin, isBootstrapping, logout }
}
