const USER_CPF = '39053344705'
const USER_PASSWORD = 'User123!'

function fillAddressForm(uniqueSuffix: string) {
  cy.intercept('GET', '/api/cep/01310100').as('cepLookup')
  cy.get('input[placeholder="00000-000"]').clear().type('01310100')
  cy.wait('@cepLookup').its('response.statusCode').should('eq', 200)
  cy.get('input[placeholder="123"]').clear().type(uniqueSuffix)
  cy.get('input[placeholder="Apto 42"]').clear().type(`Sala ${uniqueSuffix}`)
}

function ensureAddressExists(uniqueSuffix: string) {
  cy.get('body').then(($body) => {
    if ($body.find('[aria-label="Editar endereço"]').length > 0) {
      return
    }

    cy.contains('Novo Endereço').click()
    fillAddressForm(uniqueSuffix)
    cy.intercept('POST', '/api/enderecos').as('createEndereco')
    cy.get('button[type="submit"]').contains('Adicionar Endereço').click()
    cy.wait('@createEndereco').its('response.statusCode').should('eq', 201)
    cy.contains('Endereço adicionado!').should('be.visible')
  })
}

describe('Endereços', () => {
  beforeEach(() => {
    cy.login(USER_CPF, USER_PASSWORD)
    cy.intercept('GET', '/api/enderecos/usuario/*').as('listEnderecos')
    cy.visit('/enderecos')
    cy.wait('@listEnderecos').its('response.statusCode').should('eq', 200)
  })

  it('deve exibir a página de endereços', () => {
    cy.contains('Meus Endereços').should('be.visible')
    cy.contains('Novo Endereço').should('be.visible')
  })

  it('deve preencher endereço automaticamente ao digitar CEP válido', () => {
    cy.contains('Novo Endereço').click()
    fillAddressForm(`${Date.now()}`.slice(-4))
    cy.contains('Logradouro:').should('be.visible')
    cy.contains('Avenida Paulista').should('be.visible')
  })

  it('deve adicionar novo endereço', () => {
    const uniqueSuffix = `${Date.now()}`.slice(-4)

    cy.contains('Novo Endereço').click()
    fillAddressForm(uniqueSuffix)
    cy.get('button[type="submit"]').contains('Adicionar Endereço').click()

    cy.contains('Endereço adicionado!').should('be.visible')
    cy.contains(`Sala ${uniqueSuffix}`).should('be.visible')
  })

  it('deve editar um endereço existente', () => {
    const uniqueSuffix = `${Date.now()}`.slice(-4)

    ensureAddressExists(uniqueSuffix)

    cy.get('[aria-label="Editar endereço"]').first().click()
    cy.contains('Editar endereço').should('be.visible')
    fillAddressForm(`${uniqueSuffix}9`.slice(-4))
    cy.get('button[type="submit"]').contains('Salvar alterações').click()

    cy.contains('Endereço atualizado!').should('be.visible')
  })
})
