import { useEffect } from 'react'
import { Loader2 } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { formatCEP, cleanCEP } from '@/lib/utils'
import { useConsultaCep } from '../hooks/useEnderecos'
import type { CepResponse } from '../types/endereco.types'

interface Props {
  value: string
  onChange: (value: string) => void
  onCepFound: (data: CepResponse) => void
  error?: string
}

export function CepInput({ value, onChange, onCepFound, error }: Props) {
  const { mutate, isPending } = useConsultaCep()

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCEP(e.target.value)
    onChange(formatted)
  }

  useEffect(() => {
    const clean = cleanCEP(value)
    if (clean.length === 8) {
      mutate(clean, { onSuccess: onCepFound })
    }
  }, [value])

  return (
    <div className="space-y-2">
      <Label>CEP</Label>
      <div className="relative">
        <Input
          placeholder="00000-000"
          value={value}
          onChange={handleChange}
          maxLength={9}
        />
        {isPending && (
          <Loader2 className="absolute right-3 top-3 h-4 w-4 animate-spin text-muted-foreground" />
        )}
      </div>
      {error && <p className="text-sm text-destructive">{error}</p>}
    </div>
  )
}
