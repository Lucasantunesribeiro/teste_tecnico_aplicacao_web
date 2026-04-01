import { describe, it, expect } from 'vitest'
import {
  formatCPF,
  cleanCPF,
  isValidCPF,
  formatCEP,
  cleanCEP,
  isValidCEP,
  formatDate,
  formatDateForInput,
} from './utils'

describe('formatCPF', () => {
  it('retorna vazio para string vazia', () => {
    expect(formatCPF('')).toBe('')
  })

  it('formata parcialmente com 3 dígitos', () => {
    expect(formatCPF('529')).toBe('529')
  })

  it('formata com ponto após 3 dígitos', () => {
    expect(formatCPF('5299')).toBe('529.9')
  })

  it('formata parcialmente com 6 dígitos', () => {
    expect(formatCPF('529982')).toBe('529.982')
  })

  it('formata com segundo ponto após 6 dígitos', () => {
    expect(formatCPF('5299822')).toBe('529.982.2')
  })

  it('formata CPF completo com 11 dígitos', () => {
    expect(formatCPF('52998224725')).toBe('529.982.247-25')
  })

  it('ignora caracteres não numéricos na entrada', () => {
    expect(formatCPF('529.982.247-25')).toBe('529.982.247-25')
  })

  it('trunca para 11 dígitos', () => {
    expect(formatCPF('529982247251234')).toBe('529.982.247-25')
  })
})

describe('cleanCPF', () => {
  it('remove pontos e traço do CPF formatado', () => {
    expect(cleanCPF('529.982.247-25')).toBe('52998224725')
  })

  it('retorna o mesmo valor se já for somente dígitos', () => {
    expect(cleanCPF('52998224725')).toBe('52998224725')
  })

  it('retorna vazio para string vazia', () => {
    expect(cleanCPF('')).toBe('')
  })
})

describe('isValidCPF', () => {
  it('aceita CPF válido', () => {
    expect(isValidCPF('52998224725')).toBe(true)
  })

  it('rejeita CPF com todos os dígitos iguais', () => {
    expect(isValidCPF('11111111111')).toBe(false)
  })

  it('rejeita CPF inválido', () => {
    expect(isValidCPF('12345678900')).toBe(false)
  })
})

describe('formatCEP', () => {
  it('retorna vazio para string vazia', () => {
    expect(formatCEP('')).toBe('')
  })

  it('formata parcialmente com 5 dígitos', () => {
    expect(formatCEP('01001')).toBe('01001')
  })

  it('adiciona traço após 5 dígitos', () => {
    expect(formatCEP('010010')).toBe('01001-0')
  })

  it('formata CEP completo com 8 dígitos', () => {
    expect(formatCEP('01001000')).toBe('01001-000')
  })

  it('ignora caracteres não numéricos', () => {
    expect(formatCEP('01001-000')).toBe('01001-000')
  })

  it('trunca para 8 dígitos', () => {
    expect(formatCEP('010010001234')).toBe('01001-000')
  })
})

describe('cleanCEP', () => {
  it('remove traço do CEP formatado', () => {
    expect(cleanCEP('01001-000')).toBe('01001000')
  })

  it('retorna o mesmo valor se já for somente dígitos', () => {
    expect(cleanCEP('01001000')).toBe('01001000')
  })
})

describe('isValidCEP', () => {
  it('aceita CEP com 8 dígitos', () => {
    expect(isValidCEP('01001000')).toBe(true)
  })

  it('aceita CEP formatado com traço', () => {
    expect(isValidCEP('01001-000')).toBe(true)
  })

  it('rejeita CEP incompleto', () => {
    expect(isValidCEP('0100100')).toBe(false)
  })
})

describe('formatDate', () => {
  it('retorna vazio para string vazia', () => {
    expect(formatDate('')).toBe('')
  })

  it('converte formato ISO para formato brasileiro', () => {
    expect(formatDate('2024-01-15')).toBe('15/01/2024')
  })

  it('formata corretamente ano, mês e dia', () => {
    expect(formatDate('1990-12-31')).toBe('31/12/1990')
  })
})

describe('formatDateForInput', () => {
  it('retorna vazio para string vazia', () => {
    expect(formatDateForInput('')).toBe('')
  })

  it('extrai somente a data de um datetime ISO', () => {
    expect(formatDateForInput('2024-01-15T10:30:00')).toBe('2024-01-15')
  })

  it('retorna a data sem alteração se não houver T', () => {
    expect(formatDateForInput('2024-01-15')).toBe('2024-01-15')
  })
})
