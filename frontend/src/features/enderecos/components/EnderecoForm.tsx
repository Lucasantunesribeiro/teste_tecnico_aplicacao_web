import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { CepInput } from './CepInput'
import { useCreateEndereco } from '../hooks/useEnderecos'
import type { CepResponse } from '../types/endereco.types'

const schema = z.object({
  cep: z.string().min(8, 'CEP inválido'),
  numero: z.string().min(1, 'Número obrigatório'),
  complemento: z.string().optional(),
  principal: z.boolean().default(false),
})

type FormData = z.infer<typeof schema>

interface Props {
  usuarioId: string
  onSuccess?: () => void
}

export function EnderecoForm({ usuarioId, onSuccess }: Props) {
  const { mutate, isPending } = useCreateEndereco()
  const [cepData, setCepData] = useState<CepResponse | null>(null)
  const { register, handleSubmit, setValue, watch, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { principal: false },
  })

  const cepValue = watch('cep', '')
  const principalValue = watch('principal', false)

  const handleCepFound = (data: CepResponse) => {
    setCepData(data)
  }

  const onSubmit = (data: FormData) => {
    mutate(
      { usuarioId, ...data, cep: data.cep.replace(/\D/g, '') },
      { onSuccess: () => { reset(); setCepData(null); onSuccess?.() } }
    )
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <CepInput
        value={cepValue}
        onChange={(v) => setValue('cep', v)}
        onCepFound={handleCepFound}
        error={errors.cep?.message}
      />
      {cepData && (
        <div className="grid grid-cols-2 gap-3 p-3 bg-muted rounded-md text-sm">
          <div><span className="font-medium">Logradouro:</span> {cepData.logradouro}</div>
          <div><span className="font-medium">Bairro:</span> {cepData.bairro}</div>
          <div><span className="font-medium">Cidade:</span> {cepData.localidade}</div>
          <div><span className="font-medium">Estado:</span> {cepData.uf}</div>
        </div>
      )}
      <div className="space-y-2">
        <Label>Número</Label>
        <Input placeholder="123" {...register('numero')} />
        {errors.numero && <p className="text-sm text-destructive">{errors.numero.message}</p>}
      </div>
      <div className="space-y-2">
        <Label>Complemento (opcional)</Label>
        <Input placeholder="Apto 42" {...register('complemento')} />
      </div>
      <div className="flex items-center gap-3">
        <Switch
          checked={principalValue}
          onCheckedChange={(v) => setValue('principal', v)}
        />
        <Label>Endereço principal</Label>
      </div>
      <Button type="submit" className="w-full" disabled={isPending || !cepData}>
        {isPending ? 'Salvando...' : 'Adicionar Endereço'}
      </Button>
    </form>
  )
}
