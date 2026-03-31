import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authService } from '../services/authService'
import { useAuthStore } from '../store/authStore'

export function useLogin() {
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  return useMutation({
    mutationFn: authService.login,
    onSuccess: (data) => {
      login(data)
      toast.success(`Bem-vindo, ${data.nome}!`)
      navigate('/dashboard')
    },
  })
}
