import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { routerFuture } from '@/routes/routerFuture'
import { UsuarioDetailPage } from './UsuarioDetailPage'

const useUsuarioMock = vi.fn()
const useEnderecosMock = vi.fn()

vi.mock('@/features/usuarios/hooks/useUsuarios', () => ({
  useUsuario: (...args: unknown[]) => useUsuarioMock(...args),
}))

vi.mock('@/features/enderecos/hooks/useEnderecos', () => ({
  useEnderecos: (...args: unknown[]) => useEnderecosMock(...args),
  useDeleteEndereco: () => ({
    mutate: vi.fn(),
    isPending: false,
  }),
  useSetPrincipal: () => ({
    mutate: vi.fn(),
    isPending: false,
  }),
}))

describe('UsuarioDetailPage', () => {
  beforeEach(() => {
    useUsuarioMock.mockReset()
    useEnderecosMock.mockReset()
  })

  it('exibe os dados do usuário e sua lista de endereços', () => {
    useUsuarioMock.mockReturnValue({
      data: {
        id: 'user-1',
        nome: 'João Silva',
        cpf: '52998224725',
        dataNascimento: '1990-01-01',
        tipo: 'USER',
        status: 'ATIVO',
      },
      isLoading: false,
      isError: false,
    })
    useEnderecosMock.mockReturnValue({
      data: [
        {
          id: 'address-1',
          cep: '01310100',
          logradouro: 'Avenida Paulista',
          numero: '1000',
          complemento: 'Sala 100',
          bairro: 'Bela Vista',
          cidade: 'São Paulo',
          estado: 'SP',
          principal: true,
          usuarioId: 'user-1',
        },
      ],
      isLoading: false,
      isError: false,
    })

    render(
      <MemoryRouter future={routerFuture} initialEntries={['/usuarios/user-1']}>
        <Routes>
          <Route path="/usuarios/:id" element={<UsuarioDetailPage />} />
        </Routes>
      </MemoryRouter>
    )

    expect(screen.getByText('Dados do Usuário')).toBeInTheDocument()
    expect(screen.getByText('Endereços')).toBeInTheDocument()
    expect(screen.getByText('Novo endereço')).toBeInTheDocument()
    expect(screen.getByText(/Avenida Paulista, 1000, Sala 100/i)).toBeInTheDocument()
  })
})
