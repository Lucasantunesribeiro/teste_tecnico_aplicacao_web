/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      login(cpf: string, senha: string): Chainable<void>
      getByTestId(testId: string): Chainable<JQuery<HTMLElement>>
    }
  }
}

/**
 * Login via API (bypasses UI, faster than form-based login)
 */
Cypress.Commands.add('login', (cpf: string, senha: string) => {
  cy.session(
    [cpf, senha],
    () => {
      cy.request('POST', '/api/auth/login', { cpf, senha }).then((response) => {
        expect(response.status).to.eq(200)
        const { token, userId, nome, tipo } = response.body
        window.localStorage.setItem(
          'auth-storage',
          JSON.stringify({
            state: { user: { id: userId, nome, cpf, tipo, status: 'ATIVO' }, token, isAuthenticated: true },
            version: 0,
          })
        )
      })
    },
    {
      cacheAcrossSpecs: true,
    }
  )
})

/**
 * Get element by data-testid attribute
 */
Cypress.Commands.add('getByTestId', (testId: string) => {
  return cy.get(`[data-testid="${testId}"]`)
})

export {}
