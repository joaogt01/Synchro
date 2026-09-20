import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './auth'
import type { UserRole } from '../types/api'
import { Loading } from '../components/common/Loading'

export function RequireAuth({ children }: { children: ReactNode }) {
    const { autenticado, carregando } = useAuth()
    const location = useLocation()

    if (carregando) return <Loading />
    if (!autenticado) return <Navigate to="/login" state={{ from: location }} replace />
    return <>{children}</>
}

export function RequireRole({ roles, children }: { roles: UserRole[]; children: ReactNode }) {
    const { temRole, carregando } = useAuth()

    if (carregando) return <Loading />
    if (!temRole(...roles)) {
        return (
            <div style={{ padding: 32 }}>
                <div className="card" style={{ padding: '24px 28px', maxWidth: 520 }}>
                    <h2 style={{ marginTop: 0 }}>Acesso restrito</h2>
                    <p style={{ color: '#64748b' }}>
                        Seu perfil não tem permissão para acessar esta funcionalidade.
                    </p>
                </div>
            </div>
        )
    }
    return <>{children}</>
}