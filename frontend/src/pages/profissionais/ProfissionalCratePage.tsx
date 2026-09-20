import { FormEvent, useState } from "react"
import { useNavigate } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { ProfissionalRequest, ProfissionalResponse } from "../../types/api"
import { ErrorMessage } from "../../components/common/ErrorMessage"

export default function ProfissionalCreatePage() {
    const navigate = useNavigate()
    const [form, setForm] = useState<ProfissionalRequest>({ nome: "", email: "", senha: "", especialidade: "" })
    const [erro, setErro] = useState<string | null>(null)
    const [detalhes, setDetalhes] = useState<string[]>([])
    const [enviando, setEnviando] = useState(false)

    function set<K extends keyof ProfissionalRequest>(campo: K, valor: ProfissionalRequest[K]) {
        setForm((f) => ({ ...f, [campo]: valor }))
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setDetalhes([])
        setEnviando(true)
        try {
            const criado = await api.post<ProfissionalResponse>("/api/profissionais", form)
            navigate(`/profissionais/${criado.id}`, { replace: true })
        } catch (err) {
            if (err instanceof HttpError) {
                setErro(err.message)
                setDetalhes(err.detalhes)
            } else {
                setErro("Erro ao cadastrar profissional")
            }
        } finally {
            setEnviando(false)
        }
    }

    return (
        <div style={{ padding: "28px 32px", maxWidth: 520 }}>
            <h1 style={{ fontSize: 19, fontWeight: 800, marginBottom: 20 }}>Novo Profissional</h1>

            {erro && <ErrorMessage mensagem={erro} detalhes={detalhes} />}

            <form onSubmit={handleSubmit} className="card" style={{ padding: "24px 28px" }}>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">Nome</label>
                    <input type="text" required value={form.nome} onChange={(e) => set("nome", e.target.value)} />
                </div>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">E-mail</label>
                    <input type="email" required value={form.email} onChange={(e) => set("email", e.target.value)} />
                </div>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">Senha (mínimo 8 caracteres)</label>
                    <input type="password" required minLength={8} value={form.senha} onChange={(e) => set("senha", e.target.value)} />
                </div>
                <div style={{ marginBottom: 20 }}>
                    <label className="field-label">Especialidade</label>
                    <input type="text" required value={form.especialidade} onChange={(e) => set("especialidade", e.target.value)} />
                </div>
                <div style={{ display: "flex", gap: 10 }}>
                    <button type="button" className="btn-ghost" onClick={() => navigate(-1)}>Cancelar</button>
                    <button type="submit" className="btn-primary" disabled={enviando}>
                        {enviando ? "Salvando..." : "Criar Profissional"}
                    </button>
                </div>
            </form>
        </div>
    )
}