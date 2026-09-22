import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { api, clearToken, getToken, onUnauthorized, setToken } from './api'
import type { LoginRequest, LoginResponse, RegistroRequest, UserRole, UsuarioResponse, PacienteResponse, ProfissionalResponse } from '../types/api'

interface AuthContextValue {
    usuario: UsuarioResponse | null
    meuPaciente: PacienteResponse | null
    meuProfissional: ProfissionalResponse | null
    carregando: boolean
    autenticado: boolean
    login: (dados: LoginRequest) => Promise<void>
    registrar: (dados: RegistroRequest) => Promise<void>
    logout: () => void
    temRole: (...roles: UserRole[]) => boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
    const [usuario, setUsuario] = useState<UsuarioResponse | null>(null)
    const [meuPaciente, setMeuPaciente] = useState<PacienteResponse | null>(null)
    const [meuProfissional, setMeuProfissional] = useState<ProfissionalResponse | null>(null)
    const [carregando, setCarregando] = useState(true)

    async function carregarPerfil() {
        try {
            const perfil = await api.get<UsuarioResponse>('/api/me')
            setUsuario(perfil)

            if (perfil.role === 'PACIENTE') {
                api.get<PacienteResponse>('/api/me/paciente')
                    .then(setMeuPaciente)
                    .catch(() => setMeuPaciente(null))
            } else if (perfil.role === 'PROFISSIONAL') {
                api.get<ProfissionalResponse>('/api/me/profissional')
                    .then(setMeuProfissional)
                    .catch(() => setMeuProfissional(null))
            }
        } catch {
            setUsuario(null)
            clearToken()
        } finally {
            setCarregando(false)
        }
    }

    useEffect(() => {
        onUnauthorized(() => {
            setUsuario(null)
            setMeuPaciente(null)
            setMeuProfissional(null)
        })
        if (getToken()) {
            carregarPerfil()
        } else {
            setCarregando(false)
        }
    }, [])

    async function login(dados: LoginRequest) {
        const resposta = await api.post<LoginResponse>('/api/auth/login', dados)
        setToken(resposta.token)
        await carregarPerfil()
    }

    async function registrar(dados: RegistroRequest) {
        const resposta = await api.post<LoginResponse>('/api/auth/registrar', dados)
        setToken(resposta.token)
        await carregarPerfil()
    }

    function logout() {
        clearToken()
        setUsuario(null)
        setMeuPaciente(null)
        setMeuProfissional(null)
    }

    function temRole(...roles: UserRole[]) {
        return !!usuario && roles.includes(usuario.role)
    }

    return (
        <AuthContext.Provider
            value={{ usuario, meuPaciente, meuProfissional, carregando, autenticado: !!usuario, login, registrar, logout, temRole }}
        >
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    const ctx = useContext(AuthContext)
    if (!ctx) throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
    return ctx
}