import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authService } from '../services/authService'
import { useAuthStore } from '../store/authStore'

export function useLogin() {
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)

  return useMutation({
    mutationFn: authService.login,
    onSuccess: (data) => {
      setSession(data)
      toast.success(`Bem-vindo, ${data.nome}!`)
      navigate('/dashboard')
    },
    onError: () => {
      toast.error('Não foi possível realizar o login. Verifique suas credenciais.')
    },
  })
}
