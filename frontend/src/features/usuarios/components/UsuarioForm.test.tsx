import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { UsuarioForm } from './UsuarioForm'

const mockMutate = vi.fn()

vi.mock('../hooks/useUsuarios', () => ({
  useCreateUsuario: () => ({
    mutate: mockMutate,
    isPending: false,
  }),
}))

describe('UsuarioForm', () => {
  beforeEach(() => {
    mockMutate.mockClear()
  })

  it('renderiza todos os campos obrigatórios', () => {
    render(<UsuarioForm />)

    expect(screen.getByPlaceholderText('Nome completo')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('000.000.000-00')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /criar usuário/i })).toBeInTheDocument()
  })

  it('exibe erro quando CPF é inválido', async () => {
    render(<UsuarioForm />)

    await userEvent.type(screen.getByPlaceholderText('Nome completo'), 'João Silva')
    await userEvent.type(screen.getByPlaceholderText('000.000.000-00'), '12345678900')
    fireEvent.change(screen.getByLabelText('Data de Nascimento'), {
      target: { value: '1995-03-15' },
    })
    await userEvent.type(screen.getByPlaceholderText('••••••••'), 'Senha123')
    fireEvent.click(screen.getByRole('button', { name: /criar usuário/i }))

    await waitFor(() => {
      expect(screen.getByText('CPF inválido')).toBeInTheDocument()
    })
  })

  it('exibe erro quando senha não atende a política', async () => {
    render(<UsuarioForm />)

    await userEvent.type(screen.getByPlaceholderText('Nome completo'), 'João Silva')
    fireEvent.change(screen.getByPlaceholderText('000.000.000-00'), {
      target: { value: '52998224725' },
    })
    fireEvent.change(screen.getByLabelText('Data de Nascimento'), {
      target: { value: '1995-03-15' },
    })
    await userEvent.type(screen.getByPlaceholderText('••••••••'), 'senhasemmaius')

    fireEvent.click(screen.getByRole('button', { name: /criar usuário/i }))

    await waitFor(() => {
      expect(
        screen.getByText('Senha deve ter maiúscula, minúscula e número')
      ).toBeInTheDocument()
    })
  })

  it('chama mutate com dados corretos para senha válida', async () => {
    render(<UsuarioForm />)

    await userEvent.type(screen.getByPlaceholderText('Nome completo'), 'João Silva')
    fireEvent.change(screen.getByPlaceholderText('000.000.000-00'), {
      target: { value: '52998224725' },
    })
    fireEvent.change(screen.getByLabelText('Data de Nascimento'), {
      target: { value: '1995-03-15' },
    })
    await userEvent.type(screen.getByPlaceholderText('••••••••'), 'MinhaS3nha')
    fireEvent.click(screen.getByRole('button', { name: /criar usuário/i }))

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        expect.objectContaining({
          nome: 'João Silva',
          cpf: '52998224725',
          dataNascimento: '1995-03-15',
          senha: 'MinhaS3nha',
        }),
        expect.any(Object)
      )
    })
  })
})
