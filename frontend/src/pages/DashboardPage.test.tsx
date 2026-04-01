import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { routerFuture } from '@/routes/routerFuture';
import { DashboardPage } from './DashboardPage';

const useAuthMock = vi.fn();
const useUsuariosMock = vi.fn();
const useEnderecosMock = vi.fn();
const useAdminEnderecosMock = vi.fn();

vi.mock('@/features/auth/hooks/useAuth', () => ({
  useAuth: () => useAuthMock(),
}));

vi.mock('@/features/usuarios/hooks/useUsuarios', () => ({
  useUsuarios: (...args: unknown[]) => useUsuariosMock(...args),
}));

vi.mock('@/features/enderecos/hooks/useEnderecos', () => ({
  useEnderecos: (...args: unknown[]) => useEnderecosMock(...args),
  useAdminEnderecos: (...args: unknown[]) => useAdminEnderecosMock(...args),
}));

describe('DashboardPage', () => {
  beforeEach(() => {
    useAuthMock.mockReset();
    useUsuariosMock.mockReset();
    useEnderecosMock.mockReset();
    useAdminEnderecosMock.mockReset();
  });

  it('nao consulta a lista de usuarios quando o usuario nao e admin', () => {
    useAuthMock.mockReturnValue({
      user: { id: 'user-1', nome: 'Usuario Comum', tipo: 'USER' },
      isAdmin: false,
    });
    useUsuariosMock.mockReturnValue({ data: undefined, isLoading: false, isError: false });
    useEnderecosMock.mockReturnValue({ data: [], isLoading: false, isError: false });
    useAdminEnderecosMock.mockReturnValue({ data: undefined, isLoading: false, isError: false });

    render(
      <MemoryRouter future={routerFuture}>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(useUsuariosMock).toHaveBeenCalledWith({}, { enabled: false });
    expect(useAdminEnderecosMock).toHaveBeenCalledWith({ page: 0, size: 1 }, { enabled: false });
  });

  it('consulta a lista de usuarios quando o usuario e admin', () => {
    useAuthMock.mockReturnValue({
      user: { id: 'admin-1', nome: 'Administrador', tipo: 'ADMIN' },
      isAdmin: true,
    });
    useUsuariosMock.mockReturnValue({
      data: { totalElements: 2 },
      isLoading: false,
      isError: false,
    });
    useEnderecosMock.mockReturnValue({ data: [], isLoading: false, isError: false });
    useAdminEnderecosMock.mockReturnValue({
      data: { totalElements: 5 },
      isLoading: false,
      isError: false,
    });

    render(
      <MemoryRouter future={routerFuture}>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(useUsuariosMock).toHaveBeenCalledWith({}, { enabled: true });
    expect(useAdminEnderecosMock).toHaveBeenCalledWith({ page: 0, size: 1 }, { enabled: true });
    expect(screen.getByText('Total de Usuarios')).toBeInTheDocument();
    expect(screen.getByText('Enderecos Globais')).toBeInTheDocument();
  });
});
