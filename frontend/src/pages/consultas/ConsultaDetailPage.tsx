import { useEffect, useState } from "react"
import { useParams } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { ConsultaResponse } from "../../types/api"
import { useAuth } from "../../lib/auth"
import { Loading } from "../../components/common/Loading"
import { ErrorMessage } from "../../components/common/ErrorMessage"
import { StatusBadge } from "../../components/common/StatusBadge"

export default function ConsultaDetailPage() {
    const { id } = useParams<{ id: string }>()
    const consultaId = Number(id)
    const { temRole } = useAuth()

    const [consulta, setConsulta] = useState<ConsultaResponse | null>(null)
    const [erro, setErro] = useState<string | null>(null)
    const [erroAcao, setErroAcao] = useState<string | null>(null)
    const [executando, setExecutando] = useState<string | null>(null)

    function carregar() {
        api
            .get<ConsultaResponse>(`/api/consultas/${consultaId}`)
            .then(setConsulta)
            .catch((e) => setErro(e instanceof HttpError ? e.message : "Erro ao carregar consulta"))
    }

    useEffect(() => {
        carregar()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [consultaId])

    async function executarAcao(acao: "confirmar" | "completar" | "cancelar") {
        setErroAcao(null)
        setExecutando(acao)
        try {
            const atualizada = await api.post<ConsultaResponse>(`/api/consultas/${consultaId}/${acao}`)
            setConsulta(atualizada)
        } catch (err) {
            setErroAcao(err instanceof HttpError ? err.message : `Erro ao executar ação "${acao}"`)
        } finally {
            setExecutando(null)
        }
    }

    if (erro) return <div style={{ padding: 32 }}><ErrorMessage mensagem={erro} /></div>
    if (!consulta) return <Loading />

    const podeConfirmar = temRole("ADMIN", "ATENDENTE", "PROFISSIONAL") && consulta.status === "AGENDADO"
    const podeCompletar = temRole("ADMIN", "PROFISSIONAL") && consulta.status === "CONFIRMADO"
    const podeCancelar = temRole("ADMIN", "PROFISSIONAL") && consulta.status !== "COMPLETO" && consulta.status !== "CANCELADO"

    return (
        <div style={{ padding: "28px 32px", maxWidth: 520 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                <h1 style={{ fontSize: 19, fontWeight: 800, margin: 0 }}>Consulta #{consulta.id}</h1>
                <StatusBadge status={consulta.status} />
            </div>

            {erroAcao && <ErrorMessage mensagem={erroAcao} />}

            <div className="card" style={{ padding: "20px 24px", marginBottom: 20 }}>
                {[
                    ["Profissional", consulta.profissional?.nome ?? "—"],
                    ["Paciente", consulta.paciente?.nome ?? "—"],
                    ["Início", consulta.inicio.replace("T", " ")],
                    ["Fim", consulta.fim.replace("T", " ")],
                ].map(([label, valor]) => (
                    <div key={label} style={{ marginBottom: 12 }}>
                        <div style={{ fontSize: 10.5, color: "#94a3b8", fontFamily: "var(--font-mono)", marginBottom: 2 }}>
                            {(label as string).toUpperCase()}
                        </div>
                        <div style={{ fontSize: 13.5, fontWeight: 500 }}>{valor}</div>
                    </div>
                ))}
            </div>

            {(podeConfirmar || podeCompletar || podeCancelar) && (
                <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
                    {podeConfirmar && (
                        <button className="btn-primary" disabled={executando !== null} onClick={() => executarAcao("confirmar")}>
                            {executando === "confirmar" ? "Confirmando..." : "Confirmar"}
                        </button>
                    )}
                    {podeCompletar && (
                        <button className="btn-primary" disabled={executando !== null} onClick={() => executarAcao("completar")}>
                            {executando === "completar" ? "Concluindo..." : "Marcar como Completa"}
                        </button>
                    )}
                    {podeCancelar && (
                        <button className="btn-danger" disabled={executando !== null} onClick={() => executarAcao("cancelar")}>
                            {executando === "cancelar" ? "Cancelando..." : "Cancelar Consulta"}
                        </button>
                    )}
                </div>
            )}
        </div>
    )
}