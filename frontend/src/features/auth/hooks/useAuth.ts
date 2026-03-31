import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useAuthStore } from '../store/authStore'

export function useAuth() {
  const { user, token, isAuthenticated, logout: storeLogout } = useAuthStore()
  const navigate = useNavigate()

  const isAdmin = user?.tipo === 'ADMIN'

  const logout = () => {
    storeLogout()
    toast.info('Sessão encerrada')
    navigate('/login')
  }

  return { user, token, isAuthenticated, isAdmin, logout }
}
