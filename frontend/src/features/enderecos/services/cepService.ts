import api from '@/lib/api'
import type { CepResponse } from '../types/endereco.types'

export const cepService = {
  consultar: (cep: string): Promise<CepResponse> =>
    api.get(`/cep/${cep}`).then((r) => r.data),
}
