import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { formatCEP, cleanCEP, isValidCEP } from '@/lib/utils'
import { CepInput } from './CepInput'
import { useCreateEndereco, useUpdateEndereco } from '../hooks/useEnderecos'
import type { CepResponse, Endereco, EnderecoFormValues } from '../types/endereco.types'

const schema = z.object({
  cep: z.string().refine(isValidCEP, 'CEP inválido'),
  numero: z.string().trim().min(1, 'Número obrigatório'),
  complemento: z.string().optional(),
  principal: z.boolean().default(false),
})

type FormData = z.infer<typeof schema>

interface Props {
  usuarioId: string
  endereco?: Endereco
  onSuccess?: () => void
}

function toCepResponse(endereco: Endereco): CepResponse {
  return {
    cep: endereco.cep,
    logradouro: endereco.logradouro,
    complemento: endereco.complemento ?? '',
    bairro: endereco.bairro,
    localidade: endereco.cidade,
    uf: endereco.estado,
  }
}

function buildDefaultValues(endereco?: Endereco): EnderecoFormValues {
  if (!endereco) {
    return {
      cep: '',
      numero: '',
      complemento: '',
      principal: false,
    }
  }

  return {
    cep: formatCEP(endereco.cep),
    numero: endereco.numero,
    complemento: endereco.complemento ?? '',
    principal: endereco.principal,
  }
}

export function EnderecoForm({ usuarioId, endereco, onSuccess }: Props) {
  const createEndereco = useCreateEndereco()
  const updateEndereco = useUpdateEndereco()
  const isEditMode = !!endereco
  const isPending = isEditMode ? updateEndereco.isPending : createEndereco.isPending
  const [cepData, setCepData] = useState<CepResponse | null>(endereco ? toCepResponse(endereco) : null)

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: buildDefaultValues(endereco),
  })

  useEffect(() => {
    register('cep')
    register('principal')
  }, [register])

  useEffect(() => {
    reset(buildDefaultValues(endereco))
    setCepData(endereco ? toCepResponse(endereco) : null)
  }, [endereco, reset])

  const cepValue = watch('cep', '')
  const principalValue = watch('principal', false)

  const handleCepFound = (data: CepResponse) => {
    setCepData(data)
  }

  useEffect(() => {
    if (cleanCEP(cepValue).length !== 8) {
      setCepData(null)
    }
  }, [cepValue])

  const onSubmit = (data: FormData) => {
    const payload = {
      cep: cleanCEP(data.cep),
      numero: data.numero.trim(),
      complemento: data.complemento?.trim() || undefined,
    }

    if (isEditMode && endereco) {
      updateEndereco.mutate(
        {
          id: endereco.id,
          data: payload,
        },
        {
          onSuccess: () => {
            onSuccess?.()
          },
        }
      )
      return
    }

    createEndereco.mutate(
      {
        usuarioId,
        ...payload,
        principal: principalValue,
      },
      {
        onSuccess: () => {
          reset(buildDefaultValues())
          setCepData(null)
          onSuccess?.()
        },
      }
    )
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <CepInput
        value={cepValue}
        onChange={(v) => setValue('cep', v, { shouldDirty: true, shouldValidate: true })}
        onCepFound={handleCepFound}
        error={errors.cep?.message}
      />
      {cepData && (
        <div className="grid grid-cols-2 gap-3 rounded-md bg-muted p-3 text-sm">
          <div>
            <span className="font-medium">Logradouro:</span> {cepData.logradouro}
          </div>
          <div>
            <span className="font-medium">Bairro:</span> {cepData.bairro}
          </div>
          <div>
            <span className="font-medium">Cidade:</span> {cepData.localidade}
          </div>
          <div>
            <span className="font-medium">Estado:</span> {cepData.uf}
          </div>
        </div>
      )}
      <div className="space-y-2">
        <Label>Número</Label>
        <Input
          placeholder="123"
          {...register('numero')}
        />
        {errors.numero && <p className="text-sm text-destructive">{errors.numero.message}</p>}
      </div>
      <div className="space-y-2">
        <Label>Complemento (opcional)</Label>
        <Input placeholder="Apto 42" {...register('complemento')} />
      </div>
      {!isEditMode && (
        <div className="flex items-center gap-3">
          <Switch
            checked={principalValue}
            onCheckedChange={(v) =>
              setValue('principal', v, { shouldDirty: true, shouldValidate: true })
            }
          />
          <Label>Endereço principal</Label>
        </div>
      )}
      <Button type="submit" className="w-full" disabled={isPending || !cepData}>
        {isPending ? 'Salvando...' : isEditMode ? 'Salvar alterações' : 'Adicionar Endereço'}
      </Button>
    </form>
  )
}
