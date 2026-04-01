import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { formatCPF, cleanCPF, isValidCPF } from '@/lib/utils'
import { useLogin } from '../hooks/useLogin'

const schema = z.object({
  cpf: z.string().refine(isValidCPF, 'CPF invalido'),
  senha: z.string().min(8, 'Minimo 8 caracteres'),
})

type FormData = z.infer<typeof schema>

export function LoginForm() {
  const { mutate, isPending } = useLogin()
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    register('cpf')
  }, [register])

  const cpfValue = watch('cpf', '')

  const handleCpfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCPF(e.target.value)
    setValue('cpf', formatted, { shouldDirty: true, shouldValidate: true })
  }

  const onSubmit = (data: FormData) => {
    mutate({ cpf: cleanCPF(data.cpf), senha: data.senha })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="cpf">CPF</Label>
        <Input
          id="cpf"
          placeholder="000.000.000-00"
          value={cpfValue}
          onChange={handleCpfChange}
          autoComplete="username"
          inputMode="numeric"
        />
        {errors.cpf && <p className="text-sm text-destructive">{errors.cpf.message}</p>}
      </div>
      <div className="space-y-2">
        <Label htmlFor="senha">Senha</Label>
        <Input
          id="senha"
          type="password"
          placeholder="********"
          autoComplete="current-password"
          {...register('senha')}
        />
        {errors.senha && <p className="text-sm text-destructive">{errors.senha.message}</p>}
      </div>
      <Button type="submit" className="w-full" disabled={isPending}>
        {isPending ? 'Entrando...' : 'Entrar'}
      </Button>
    </form>
  )
}
