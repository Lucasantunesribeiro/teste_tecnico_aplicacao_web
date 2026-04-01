import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LoginForm } from './LoginForm'

const mockMutate = vi.fn()

vi.mock('../hooks/useLogin', () => ({
  useLogin: () => ({
    mutate: mockMutate,
    isPending: false,
  }),
}))

describe('LoginForm', () => {
  beforeEach(() => {
    mockMutate.mockClear()
  })

  it('renderiza campos CPF, senha e botao de submit', () => {
    render(<LoginForm />)

    expect(screen.getByLabelText('CPF')).toBeInTheDocument()
    expect(screen.getByLabelText('Senha')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument()
  })

  it('formata CPF automaticamente enquanto o usuario digita', async () => {
    render(<LoginForm />)

    const cpfInput = screen.getByLabelText('CPF')
    fireEvent.change(cpfInput, { target: { value: '52998224725' } })

    await waitFor(() => {
      expect(cpfInput).toHaveValue('529.982.247-25')
    })
  })

  it('exibe erro de validacao para CPF invalido', async () => {
    render(<LoginForm />)

    await userEvent.type(screen.getByLabelText('CPF'), '12345678900')
    await userEvent.type(screen.getByLabelText('Senha'), 'Senha123')
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }))

    await waitFor(() => {
      expect(screen.getByText('CPF invalido')).toBeInTheDocument()
    })
  })

  it('exibe erro de validacao quando senha nao atende ao minimo', async () => {
    render(<LoginForm />)

    fireEvent.change(screen.getByLabelText('CPF'), {
      target: { value: '52998224725' },
    })
    fireEvent.change(screen.getByLabelText('Senha'), {
      target: { value: '1234567' },
    })
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }))

    await waitFor(() => {
      expect(screen.getByText('Minimo 8 caracteres')).toBeInTheDocument()
    })
  })

  it('chama mutate com CPF limpo e senha ao submeter formulario valido', async () => {
    render(<LoginForm />)

    await userEvent.type(screen.getByLabelText('CPF'), '52998224725')
    await userEvent.type(screen.getByLabelText('Senha'), 'MinhaS3nha')
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }))

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith({
        cpf: '52998224725',
        senha: 'MinhaS3nha',
      })
    })
  })
})
