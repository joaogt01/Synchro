import { FormEvent, useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { ConsultaResponse } from "../../types/api"
import { useAuth } from "../../lib/auth"
import { Loading } from "../../components/common/Loading"
import { ErrorMessage } from "../../components/common/ErrorMessage"
import { EmptyState } from "../../components/common/EmptyState"
import { StatusBadge } from "../../components/common/StatusBadge"

export default function ConsultasListPage() {
    const { temRole } = useAuth()
    const navigate = useNavigate()

    const [lista, setLista] = useState<ConsultaResponse[] | null>(null)
    const [erro, setErro] = useState<string | null>(null)

    const [profissionalId, setProfissionalId] = useState("")
    const [pacienteId, setPacienteId] = useState("")
    const [inicio, setInicio] = useState("")
    const [fim, setFim] = useState("")

    const podeFiltrarPorTerceiros = temRole("ADMIN", "ATENDENTE")

    function carregar(e?: FormEvent) {
        e?.preventDefault()
        setErro(null)
        api
            .get<ConsultaResponse[]>("/api/consultas", {
                profissionalId: podeFiltrarPorTerceiros ? profissionalId : undefined,
                pacienteId: podeFiltrarPorTerceiros ? pacienteId : undefined,
                inicio: inicio ? `${inicio}:00` : undefined,
                fim: fim ? `${fim}:00` : undefined,
            })
            .then(setLista)
            .catch((e) => setErro(e instanceof HttpError ? e.message : "Erro ao carregar consultas"))
    }

    useEffect(() => {
        carregar()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const podeCriar = temRole("ADMIN", "ATENDENTE", "PROFISSIONAL")

    return (
        <div style={{ padding: "28px 32px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                <h1 style={{ fontSize: 19, fontWeight: 800, margin: 0 }}>Consultas</h1>
                {podeCriar && (
                    <Link to="/consultas/nova" className="btn-primary" style={{ textDecoration: "none" }}>
                        + Agendar Consulta
                    </Link>
                )}
            </div>

            <form onSubmit={carregar} className="card" style={{ padding: "14px 18px", marginBottom: 20, display: "flex", gap: 12, flexWrap: "wrap", alignItems: "flex-end" }}>
                {podeFiltrarPorTerceiros && (
                    <>
                        <div>
                            <label className="field-label">ID Profissional</label>
                            <input type="number" value={profissionalId} onChange={(e) => setProfissionalId(e.target.value)} style={{ width: 140 }} />
                        </div>
                        <div>
                            <label className="field-label">ID Paciente</label>
                            <input type="number" value={pacienteId} onChange={(e) => setPacienteId(e.target.value)} style={{ width: 140 }} />
                        </div>
                    </>
                )}
                <div>
                    <label className="field-label">De</label>
                    <input type="datetime-local" value={inicio} onChange={(e) => setInicio(e.target.value)} />
                </div>
                <div>
                    <label className="field-label">Até</label>
                    <input type="datetime-local" value={fim} onChange={(e) => setFim(e.target.value)} />
                </div>
                <button className="btn-ghost" type="submit">Filtrar</button>
                {!podeFiltrarPorTerceiros && (
                    <span style={{ fontSize: 11.5, color: "#94a3b8" }}>
            A lista já é restrita automaticamente às suas próprias consultas.
          </span>
                )}
            </form>

            {erro && <ErrorMessage mensagem={erro} />}
            {!lista && !erro && <Loading />}

            {lista && (
                <div className="card" style={{ overflow: "hidden" }}>
                    {lista.length === 0 ? (
                        <EmptyState mensagem="Nenhuma consulta encontrada." />
                    ) : (
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Profissional</th>
                                <th>Paciente</th>
                                <th>Início</th>
                                <th>Fim</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            {lista.map((c) => (
                                <tr key={c.id} className="clickable" onClick={() => navigate(`/consultas/${c.id}`)}>
                                    <td className="mono">{c.id}</td>
                                    <td>{c.profissional?.nome ?? "—"}</td>
                                    <td>{c.paciente?.nome ?? "—"}</td>
                                    <td className="mono">{c.inicio.replace("T", " ")}</td>
                                    <td className="mono">{c.fim.replace("T", " ")}</td>
                                    <td><StatusBadge status={c.status} /></td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            )}
        </div>
    )
}