import { FormEvent, useState } from "react"
import { api, HttpError } from "../../lib/api"
import type { NovoUsuarioPrivilegiadoRequest, UserRole, UsuarioResponse } from "../../types/api"
import { ErrorMessage } from "../../components/common/ErrorMessage"

const ROLES_PERMITIDAS: UserRole[] = ["ADMIN", "ATENDENTE"]

export default function AdminUsuariosPage() {
    const [form, setForm] = useState<NovoUsuarioPrivilegiadoRequest>({ nome: "", email: "", senha: "", role: "ATENDENTE" })
    const [erro, setErro] = useState<string | null>(null)
    const [detalhes, setDetalhes] = useState<string[]>([])
    const [sucesso, setSucesso] = useState<UsuarioResponse | null>(null)
    const [enviando, setEnviando] = useState(false)

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setDetalhes([])
        setSucesso(null)
        setEnviando(true)
        try {
            const criado = await api.post<UsuarioResponse>("/api/admin/usuarios", form)
            setSucesso(criado)
            setForm({ nome: "", email: "", senha: "", role: "ATENDENTE" })
        } catch (err) {
            if (err instanceof HttpError) {
                setErro(err.message)
                setDetalhes(err.detalhes)
            } else {
                setErro("Erro ao criar usuário")
            }
        } finally {
            setEnviando(false)
        }
    }

    return (
        <div style={{ padding: "28px 32px", maxWidth: 480 }}>
            <h1 style={{ fontSize: 19, fontWeight: 800, marginBottom: 8 }}>Criar Usuário Privilegiado</h1>
            <p style={{ fontSize: 12.5, color: "#64748b", marginBottom: 20 }}>
                Este endpoint só cria usuários com papel <strong>ADMIN</strong> ou <strong>ATENDENTE</strong>.
                Contas PROFISSIONAL são criadas em "Profissionais → Novo Profissional", e contas PACIENTE
                via cadastro público.
            </p>

            {erro && <ErrorMessage mensagem={erro} detalhes={detalhes} />}
            {sucesso && (
                <div style={{ background: "#f0fdf4", border: "1px solid #bbf7d0", color: "#15803d", borderRadius: 10, padding: "12px 16px", fontSize: 13, marginBottom: 16 }}>
                    Usuário <strong>{sucesso.nome}</strong> ({sucesso.role}) criado com sucesso — ID {sucesso.id}.
                </div>
            )}

            <form onSubmit={handleSubmit} className="card" style={{ padding: "24px 28px" }}>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">Nome</label>
                    <input type="text" required value={form.nome} onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))} />
                </div>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">E-mail</label>
                    <input type="email" required value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} />
                </div>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">Senha (mínimo 8 caracteres)</label>
                    <input type="password" required minLength={8} value={form.senha} onChange={(e) => setForm((f) => ({ ...f, senha: e.target.value }))} />
                </div>
                <div style={{ marginBottom: 20 }}>
                    <label className="field-label">Papel</label>
                    <select value={form.role} onChange={(e) => setForm((f) => ({ ...f, role: e.target.value as UserRole }))}>
                        {ROLES_PERMITIDAS.map((r) => (
                            <option key={r} value={r}>{r}</option>
                        ))}
                    </select>
                </div>
                <button className="btn-primary" type="submit" disabled={enviando}>
                    {enviando ? "Criando..." : "Criar Usuário"}
                </button>
            </form>
        </div>
    )
}