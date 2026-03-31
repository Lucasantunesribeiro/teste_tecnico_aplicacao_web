import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { formatCPF, cleanCPF } from '@/lib/utils'
import { useCreateUsuario } from '../hooks/useUsuarios'

const schema = z.object({
  nome: z.string().min(2, 'Nome muito curto'),
  cpf: z.string().min(11, 'CPF inválido'),
  dataNascimento: z.string().min(1, 'Data obrigatória'),
  senha: z.string().min(6, 'Mínimo 6 caracteres'),
})

type FormData = z.infer<typeof schema>

interface Props {
  onSuccess?: () => void
}

export function UsuarioForm({ onSuccess }: Props) {
  const { mutate, isPending } = useCreateUsuario()
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const cpfValue = watch('cpf', '')

  const handleCpfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue('cpf', formatCPF(e.target.value))
  }

  const onSubmit = (data: FormData) => {
    mutate(
      { ...data, cpf: cleanCPF(data.cpf) },
      { onSuccess: () => { reset(); onSuccess?.() } }
    )
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label>Nome</Label>
        <Input placeholder="Nome completo" {...register('nome')} />
        {errors.nome && <p className="text-sm text-destructive">{errors.nome.message}</p>}
      </div>
      <div className="space-y-2">
        <Label>CPF</Label>
        <Input placeholder="000.000.000-00" value={cpfValue} onChange={handleCpfChange} />
        {errors.cpf && <p className="text-sm text-destructive">{errors.cpf.message}</p>}
      </div>
      <div className="space-y-2">
        <Label>Data de Nascimento</Label>
        <Input type="date" {...register('dataNascimento')} />
        {errors.dataNascimento && <p className="text-sm text-destructive">{errors.dataNascimento.message}</p>}
      </div>
      <div className="space-y-2">
        <Label>Senha</Label>
        <Input type="password" placeholder="••••••••" {...register('senha')} />
        {errors.senha && <p className="text-sm text-destructive">{errors.senha.message}</p>}
      </div>
      <Button type="submit" className="w-full" disabled={isPending}>
        {isPending ? 'Salvando...' : 'Criar Usuário'}
      </Button>
    </form>
  )
}
