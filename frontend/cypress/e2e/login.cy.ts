describe('Login', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('deve exibir o formulário de login', () => {
    cy.contains('Gerenciamento de Usuários').should('be.visible')
    cy.get('input[id="cpf"]').should('be.visible')
    cy.get('input[id="senha"]').should('be.visible')
    cy.get('button[type="submit"]').should('contain', 'Entrar')
  })

  it('deve fazer login como admin com sucesso', () => {
    cy.get('input[id="cpf"]').type('111.222.333-44')
    cy.get('input[id="senha"]').type('admin123')
    cy.get('button[type="submit"]').click()

    cy.url().should('include', '/dashboard')
    cy.contains('Dashboard').should('be.visible')
  })

  it('deve fazer login como usuário comum com sucesso', () => {
    cy.get('input[id="cpf"]').type('555.666.777-88')
    cy.get('input[id="senha"]').type('user123')
    cy.get('button[type="submit"]').click()

    cy.url().should('include', '/dashboard')
  })

  it('deve mostrar erro para credenciais inválidas', () => {
    cy.get('input[id="cpf"]').type('111.222.333-44')
    cy.get('input[id="senha"]').type('senhaErrada')
    cy.get('button[type="submit"]').click()

    // Should stay on login page
    cy.url().should('include', '/login')
  })

  it('deve redirecionar para /login quando não autenticado', () => {
    cy.visit('/dashboard')
    cy.url().should('include', '/login')
  })

  it('deve fazer logout corretamente', () => {
    cy.login('11122233344', 'admin123')
    cy.visit('/dashboard')

    // Open user menu and logout
    cy.get('[data-radix-collection-item]').first().click()
    cy.contains('Sair').click()

    cy.url().should('include', '/login')
  })
})
