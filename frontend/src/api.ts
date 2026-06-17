export type Page<T> = { content: T[]; totalElements: number }
const TOKEN_KEY = 'real-estate-token'
const NAME_KEY = 'real-estate-user-name'
const ROLE_KEY = 'real-estate-user-role'
export const session = {
  token: () => sessionStorage.getItem(TOKEN_KEY),
  fullName: () => sessionStorage.getItem(NAME_KEY),
  role: () => sessionStorage.getItem(ROLE_KEY),
  save: (token: string, fullName?: string, role?: string) => {
    sessionStorage.setItem(TOKEN_KEY, token)
    if (fullName) sessionStorage.setItem(NAME_KEY, fullName)
    if (role) sessionStorage.setItem(ROLE_KEY, role)
  },
  clear: () => {
    sessionStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(NAME_KEY)
    sessionStorage.removeItem(ROLE_KEY)
  },
}
export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body) headers.set('Content-Type', 'application/json; charset=utf-8')
  const token = session.token(); if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(path, { ...options, headers })
  if (response.status === 401) session.clear()
  if (!response.ok) { const problem = await response.json().catch(() => ({ detail: 'Erreur serveur' })); throw new Error(problem.detail ?? problem.title ?? 'Erreur serveur') }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
export const xof = (value: number) => `${new Intl.NumberFormat('fr-FR').format(value)} XOF`
