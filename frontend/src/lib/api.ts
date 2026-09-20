import type { ApiError } from '../types/api'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

const TOKEN_KEY = 'agendamento_token'

export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
    localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
    localStorage.removeItem(TOKEN_KEY)
}

type UnauthorizedListener = () => void
let unauthorizedListener: UnauthorizedListener | null = null
export function onUnauthorized(listener: UnauthorizedListener) {
    unauthorizedListener = listener
}

export class HttpError extends Error {
    status: number
    detalhes: string[]
    constructor(status: number, message: string, detalhes: string[] = []) {
        super(message)
        this.status = status
        this.detalhes = detalhes
    }
}

interface RequestOptions {
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
    body?: unknown
    query?: Record<string, string | number | undefined | null>
}

function buildQuery(query?: RequestOptions['query']): string {
    if (!query) return ''
    const params = new URLSearchParams()
    for (const [key, value] of Object.entries(query)) {
        if (value !== undefined && value !== null && value !== '') {
            params.set(key, String(value))
        }
    }
    const qs = params.toString()
    return qs ? `?${qs}` : ''
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const token = getToken()
    const headers: Record<string, string> = {}
    if (options.body !== undefined) {
        headers['Content-Type'] = 'application/json'
    }
    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    const response = await fetch(`${API_URL}${path}${buildQuery(options.query)}`, {
        method: options.method ?? 'GET',
        headers,
        body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    })

    if (response.status === 401) {
        clearToken()
        unauthorizedListener?.()
        throw new HttpError(401, 'Sessão expirada ou inválida. Faça login novamente.')
    }

    if (response.status === 204) {
        return undefined as T
    }

    const isJson = response.headers.get('content-type')?.includes('application/json')
    const data = isJson ? await response.json() : undefined

    if (!response.ok) {
        const apiError = data as ApiError | undefined
        throw new HttpError(
            response.status,
            apiError?.mensagem ?? `Erro ${response.status} ao chamar ${path}`,
            apiError?.detalhes ?? [],
        )
    }

    return data as T
}

export const api = {
    get: <T>(path: string, query?: RequestOptions['query']) => request<T>(path, { method: 'GET', query }),
    post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
    put: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PUT', body }),
    delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}