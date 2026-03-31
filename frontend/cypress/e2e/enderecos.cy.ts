describe('Endereços', () => {
  beforeEach(() => {
    cy.login('55566677788', 'user123')
    cy.visit('/enderecos')
  })

  it('deve exibir a página de endereços', () => {
    cy.contains('Meus Endereços').should('be.visible')
    cy.contains('Novo Endereço').should('be.visible')
  })

  it('deve abrir o dialog de novo endereço', () => {
    cy.contains('Novo Endereço').click()
    cy.contains('Adicionar Endereço').should('be.visible')
    cy.get('input[placeholder="00000-000"]').should('be.visible')
  })

  it('deve preencher endereço automaticamente ao digitar CEP válido', () => {
    cy.contains('Novo Endereço').click()

    // Type a valid CEP
    cy.get('input[placeholder="00000-000"]').type('01310100')

    // Wait for CEP lookup (API call)
    cy.wait(2000)

    // Fields should be filled (logradouro visible)
    cy.contains('Av. Paulista').should('be.visible')
  })

  it('deve adicionar novo endereço', () => {
    cy.contains('Novo Endereço').click()

    cy.get('input[placeholder="00000-000"]').type('01310100')
    cy.wait(2000) // Wait for CEP lookup

    cy.get('input[placeholder="123"]').type('1000')
    cy.get('input[placeholder="Apto 42"]').type('Sala 5')

    cy.get('button[type="submit"]').contains('Adicionar Endereço').click()

    cy.contains('Endereço adicionado!').should('be.visible')
  })
})
