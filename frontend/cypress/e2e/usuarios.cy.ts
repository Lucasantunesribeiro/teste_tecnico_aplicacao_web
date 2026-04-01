const ADMIN_CPF = '52998224725'
const ADMIN_PASSWORD = 'Admin123!'
const USER_CPF = '39053344705'
const USER_PASSWORD = 'User123!'

describe('Fluxos administrativos', () => {
  beforeEach(() => {
    cy.login(ADMIN_CPF, ADMIN_PASSWORD)
    cy.intercept('GET', '/api/usuarios*').as('listUsuarios')
    cy.visit('/usuarios')
    cy.wait('@listUsuarios').its('response.statusCode').should('eq', 200)
  })

  it('deve exibir a lista de usuários', () => {
    cy.contains('Usuários').should('be.visible')
    cy.contains('Novo Usuário').should('be.visible')
    cy.contains('Ver detalhes').should('be.visible')
  })

  it('deve navegar para o detalhe do usuário', () => {
    cy.intercept('GET', '/api/usuarios/*').as('getUsuario')
    cy.intercept('GET', '/api/enderecos/usuario/*').as('getUsuarioEnderecos')

    cy.contains('Ver detalhes').first().click()

    cy.wait('@getUsuario').its('response.statusCode').should('eq', 200)
    cy.wait('@getUsuarioEnderecos').its('response.statusCode').should('eq', 200)
    cy.url().should('match', /\/usuarios\/.+/)
    cy.contains('Dados do Usuário').should('be.visible')
  })

  it('deve carregar a auditoria global de endereços', () => {
    cy.intercept('GET', '/api/enderecos*').as('listAdminEnderecos')
    cy.visit('/enderecos/admin')
    cy.wait('@listAdminEnderecos').its('response.statusCode').should('eq', 200)
    cy.contains('Auditoria de Enderecos').should('be.visible')
  })

  it('não deve ser acessível para usuário não-admin', () => {
    cy.login(USER_CPF, USER_PASSWORD)
    cy.visit('/usuarios')
    cy.url().should('include', '/dashboard')
  })
})
