describe('Usuários (Admin)', () => {
  beforeEach(() => {
    cy.login('11122233344', 'admin123')
    cy.visit('/usuarios')
  })

  it('deve exibir a lista de usuários', () => {
    cy.contains('Usuários').should('be.visible')
    cy.contains('Novo Usuário').should('be.visible')
  })

  it('deve abrir o dialog de criação de usuário', () => {
    cy.contains('Novo Usuário').click()
    cy.contains('Criar Usuário').should('be.visible')
    cy.get('input[placeholder="Nome completo"]').should('be.visible')
  })

  it('deve criar um novo usuário', () => {
    cy.contains('Novo Usuário').click()

    cy.get('input[placeholder="Nome completo"]').type('Teste Cypress')
    cy.get('input[placeholder="000.000.000-00"]').type('529.982.247-25')
    cy.get('input[type="date"]').type('1995-03-15')
    cy.get('input[type="password"]').type('senha123')
    cy.get('button[type="submit"]').contains('Criar Usuário').click()

    cy.contains('Usuário criado com sucesso!').should('be.visible')
  })

  it('não deve ser acessível para usuário não-admin', () => {
    cy.login('55566677788', 'user123')
    cy.visit('/usuarios')

    // Should redirect to dashboard
    cy.url().should('include', '/dashboard')
  })
})
