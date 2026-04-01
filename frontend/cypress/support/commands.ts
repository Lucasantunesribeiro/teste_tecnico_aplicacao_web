/* eslint-disable @typescript-eslint/no-namespace */
/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      login(cpf: string, senha: string): Chainable<void>
      getByTestId(testId: string): Chainable<JQuery<HTMLElement>>
    }
  }
}

type CookieOptions = Partial<Cypress.SetCookieOptions>

function resolveApiUrl(path: string) {
  const configuredApiBaseUrl = Cypress.env('apiBaseUrl') as string | undefined
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (configuredApiBaseUrl) {
    return `${configuredApiBaseUrl}${normalizedPath}`
  }

  return normalizedPath
}

function extractCookie(setCookieHeader: string[] | string | undefined, cookieName: string) {
  const cookieLines = Array.isArray(setCookieHeader) ? setCookieHeader : [setCookieHeader]

  for (const cookieLine of cookieLines) {
    if (!cookieLine?.startsWith(`${cookieName}=`)) {
      continue
    }

    const [nameValue, ...attributes] = cookieLine.split(';').map((part) => part.trim())
    const value = nameValue.slice(cookieName.length + 1)
    const options: CookieOptions = {}

    for (const attribute of attributes) {
      const [key, rawValue] = attribute.split('=')
      const normalizedKey = key.toLowerCase()
      const normalizedValue = rawValue?.trim()

      if (normalizedKey === 'path' && normalizedValue) {
        options.path = normalizedValue
      } else if (normalizedKey === 'httponly') {
        options.httpOnly = true
      } else if (normalizedKey === 'secure') {
        options.secure = true
      } else if (normalizedKey === 'samesite' && normalizedValue) {
        options.sameSite = normalizedValue.toLowerCase() as Cypress.SameSiteStatus
      }
    }

    return {
      value: decodeURIComponent(value),
      options,
    }
  }

  return null
}

function applySessionCookies(setCookieHeader: string[] | string | undefined) {
  const accessCookie = extractCookie(setCookieHeader, 'ACCESS_TOKEN')
  const refreshCookie = extractCookie(setCookieHeader, 'REFRESH_TOKEN')
  const xsrfCookie = extractCookie(setCookieHeader, 'XSRF-TOKEN')

  if (accessCookie) {
    cy.setCookie('ACCESS_TOKEN', accessCookie.value, accessCookie.options)
  }

  if (refreshCookie) {
    cy.setCookie('REFRESH_TOKEN', refreshCookie.value, refreshCookie.options)
  }

  if (xsrfCookie) {
    cy.setCookie('XSRF-TOKEN', xsrfCookie.value, xsrfCookie.options)
  }
}

Cypress.Commands.add('login', (cpf: string, senha: string) => {
  cy.session(
    [cpf, senha],
    () => {
      cy.clearCookies()
      cy.request('POST', resolveApiUrl('/api/auth/login'), { cpf, senha }).then((response) => {
        expect(response.status).to.eq(200)
        applySessionCookies(response.headers['set-cookie'])
      })

      cy.visit('/dashboard')
      cy.url().should('include', '/dashboard')
      cy.window().then((win) =>
        win.fetch('/api/auth/me', { credentials: 'include' }).then((response) => response.status)
      ).should('eq', 200)
      cy.request(resolveApiUrl('/api/auth/me')).its('status').should('eq', 200)
    },
    {
      cacheAcrossSpecs: true,
      validate: () => {
        cy.request(resolveApiUrl('/api/auth/me')).its('status').should('eq', 200)
      },
    }
  )
})

Cypress.Commands.add('getByTestId', (testId: string) => cy.get(`[data-testid="${testId}"]`))

export {}
