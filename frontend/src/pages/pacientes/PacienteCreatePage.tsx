import { FormEvent, useState } from "react"
import { useNavigate } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { PacienteRequest, PacienteResponse } from "../../types/api"
import { ErrorMessage } from "../../components/common/ErrorMessage"

export default function PacienteCreatePage() {
    const navigate = useNavigate()
    const [form, setForm] = useState<PacienteRequest>({ usuarioId: 0, telefone: "" })
    const [erro, setErro] = useState<string | null>(null)
    const [detalhes, setDetalhes] = useState<string[]>([])
    const [enviando, setEnviando] = useState(false)

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setDetalhes([])
        setEnviando(true)
        try {
            const criado = await api.post<PacienteResponse>("/api/pacientes", form)
            navigate(`/pacientes/${criado.id}`, { replace: true })
        } catch (err) {
            if (err instanceof HttpError) {
                setErro(err.message)
                setDetalhes(err.detalhes)
            } else {
                setErro("Erro ao cadastrar paciente")
            }
        } finally {
            setEnviando(false)
        }
    }

    return (
        <div style={{ padding: "28px 32px", maxWidth: 480 }}>
            <h1 style={{ fontSize: 19, fontWeight: 800, marginBottom: 8 }}>Novo Paciente</h1>
            <p style={{ fontSize: 12.5, color: "#64748b", marginBottom: 20 }}>
                O paciente precisa já ter uma conta de usuário criada (ex.: via cadastro público em
                <code style={{ margin: "0 4px" }}>/registrar</code>). Informe o ID desse usuário abaixo.
            </p>

            {erro && <ErrorMessage mensagem={erro} detalhes={detalhes} />}

            <form onSubmit={handleSubmit} className="card" style={{ padding: "24px 28px" }}>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">ID do Usuário</label>
                    <input
                        type="number"
                        required
                        min={1}
                        value={form.usuarioId || ""}
                        onChange={(e) => setForm((f) => ({ ...f, usuarioId: Number(e.target.value) }))}
                    />
                </div>
                <div style={{ marginBottom: 20 }}>
                    <label className="field-label">Telefone</label>
                    <input
                        type="text"
                        required
                        placeholder="81999999999"
                        value={form.telefone}
                        onChange={(e) => setForm((f) => ({ ...f, telefone: e.target.value }))}
                    />
                </div>
                <div style={{ display: "flex", gap: 10 }}>
                    <button type="button" className="btn-ghost" onClick={() => navigate(-1)}>Cancelar</button>
                    <button type="submit" className="btn-primary" disabled={enviando}>
                        {enviando ? "Salvando..." : "Criar Paciente"}
                    </button>
                </div>
            </form>
        </div>
    )
}