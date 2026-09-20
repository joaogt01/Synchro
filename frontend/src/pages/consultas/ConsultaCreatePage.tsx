import { FormEvent, useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { ConsultaResponse, PacienteResponse, ProfissionalResponse } from "../../types/api"
import { ErrorMessage } from "../../components/common/ErrorMessage"
import { Loading } from "../../components/common/Loading"

export default function ConsultaCreatePage() {
    const navigate = useNavigate()

    const [profissionais, setProfissionais] = useState<ProfissionalResponse[] | null>(null)
    const [pacientes, setPacientes] = useState<PacienteResponse[] | null>(null)
    const [erroCarregando, setErroCarregando] = useState<string | null>(null)

    const [profissionalId, setProfissionalId] = useState("")
    const [pacienteId, setPacienteId] = useState("")
    const [inicio, setInicio] = useState("")
    const [fim, setFim] = useState("")

    const [erro, setErro] = useState<string | null>(null)
    const [detalhes, setDetalhes] = useState<string[]>([])
    const [enviando, setEnviando] = useState(false)

    useEffect(() => {
        Promise.all([
            api.get<ProfissionalResponse[]>("/api/profissionais"),
            api.get<PacienteResponse[]>("/api/pacientes"),
        ])
            .then(([profs, pacs]) => {
                setProfissionais(profs)
                setPacientes(pacs)
            })
            .catch((e) => setErroCarregando(e instanceof HttpError ? e.message : "Erro ao carregar dados"))
    }, [])

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setDetalhes([])
        setEnviando(true)
        try {
            const criada = await api.post<ConsultaResponse>("/api/consultas", {
                profissionalId: Number(profissionalId),
                pacienteId: Number(pacienteId),
                inicio: `${inicio}:00`,
                fim: `${fim}:00`,
            })
            navigate(`/consultas/${criada.id}`, { replace: true })
        } catch (err) {
            if (err instanceof HttpError) {
                setErro(err.message)
                setDetalhes(err.detalhes)
            } else {
                setErro("Erro ao agendar consulta")
            }
        } finally {
            setEnviando(false)
        }
    }

    if (erroCarregando) return <div style={{ padding: 32 }}><ErrorMessage mensagem={erroCarregando} /></div>
    if (!profissionais || !pacientes) return <Loading />

    return (
        <div style={{ padding: "28px 32px", maxWidth: 520 }}>
            <h1 style={{ fontSize: 19, fontWeight: 800, marginBottom: 20 }}>Agendar Consulta</h1>

            {erro && <ErrorMessage mensagem={erro} detalhes={detalhes} />}

            <form onSubmit={handleSubmit} className="card" style={{ padding: "24px 28px" }}>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">Profissional</label>
                    <select required value={profissionalId} onChange={(e) => setProfissionalId(e.target.value)}>
                        <option value="">Selecione...</option>
                        {profissionais.map((p) => (
                            <option key={p.id} value={p.id}>{p.nome} — {p.especialidade}</option>
                        ))}
                    </select>
                </div>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">Paciente</label>
                    <select required value={pacienteId} onChange={(e) => setPacienteId(e.target.value)}>
                        <option value="">Selecione...</option>
                        {pacientes.map((p) => (
                            <option key={p.id} value={p.id}>{p.nomeUsuario}</option>
                        ))}
                    </select>
                </div>
                <div style={{ display: "flex", gap: 12, marginBottom: 20 }}>
                    <div style={{ flex: 1 }}>
                        <label className="field-label">Início</label>
                        <input type="datetime-local" required value={inicio} onChange={(e) => setInicio(e.target.value)} />
                    </div>
                    <div style={{ flex: 1 }}>
                        <label className="field-label">Fim</label>
                        <input type="datetime-local" required value={fim} onChange={(e) => setFim(e.target.value)} />
                    </div>
                </div>
                <div style={{ display: "flex", gap: 10 }}>
                    <button type="button" className="btn-ghost" onClick={() => navigate(-1)}>Cancelar</button>
                    <button type="submit" className="btn-primary" disabled={enviando}>
                        {enviando ? "Agendando..." : "Agendar"}
                    </button>
                </div>
            </form>
        </div>
    )
}