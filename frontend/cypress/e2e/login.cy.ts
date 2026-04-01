const ADMIN_CPF = '52998224725'
const ADMIN_PASSWORD = 'Admin123!'
const USER_CPF = '39053344705'
const USER_PASSWORD = 'User123!'

function resolveApiUrl(path: string) {
  const configuredApiBaseUrl = Cypress.env('apiBaseUrl') as string | undefined
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (configuredApiBaseUrl) {
    return `${configuredApiBaseUrl}${normalizedPath}`
  }

  return normalizedPath
}

describe('Login', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('deve exibir o formulario de login', () => {
    cy.contains(/Gerenciamento de Usu/i).should('be.visible')
    cy.get('input[id="cpf"]').should('be.visible')
    cy.get('input[id="senha"]').should('be.visible')
    cy.get('button[type="submit"]').should('contain', 'Entrar')
  })

  it('deve criar uma sessao admin valida no backend e acessar o dashboard', () => {
    cy.login(ADMIN_CPF, ADMIN_PASSWORD)
    cy.visit('/dashboard')

    cy.url().should('include', '/dashboard')
    cy.contains('Dashboard').should('be.visible')
  })

  it('deve criar uma sessao de usuario comum valida no backend', () => {
    cy.login(USER_CPF, USER_PASSWORD)
    cy.visit('/dashboard')

    cy.url().should('include', '/dashboard')
    cy.contains('Bem-vindo').should('be.visible')
  })

  it('deve validar CPF invalido no formulario antes de enviar', () => {
    cy.get('input[id="cpf"]').type('111.222.333-44')
    cy.get('input[id="senha"]').type('senhaErrada', { log: false })
    cy.get('button[type="submit"]').click()

    cy.contains('CPF invalido').should('be.visible')
    cy.url().should('include', '/login')
  })

  it('deve redirecionar para /login quando nao autenticado', () => {
    cy.visit('/dashboard')
    cy.url().should('include', '/login')
  })

  it('deve fazer logout corretamente', () => {
    cy.login(ADMIN_CPF, ADMIN_PASSWORD)
    cy.visit('/dashboard')

    cy.getByTestId('user-menu-trigger').click()
    cy.getByTestId('logout-button').click()

    cy.url().should('include', '/login')
    cy.request({ url: resolveApiUrl('/api/auth/me'), failOnStatusCode: false })
      .its('status')
      .should('eq', 401)
  })
})
