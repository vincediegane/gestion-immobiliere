export type Page<T> = { content: T[]; totalElements: number }
const TOKEN_KEY = 'real-estate-token'
export const session = { token: () => sessionStorage.getItem(TOKEN_KEY), save: (token: string) => sessionStorage.setItem(TOKEN_KEY, token), clear: () => sessionStorage.removeItem(TOKEN_KEY) }
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
