import { FormEvent, useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type {
    DisponibilidadeRequest,
    DisponibilidadeResponse,
    ProfissionalResponse,
    SlotBloqueadoRequest,
    SlotBloqueadoResponse,
} from "../../types/api"
import { useAuth } from "../../lib/auth"
import { Loading } from "../../components/common/Loading"
import { ErrorMessage } from "../../components/common/ErrorMessage"
import { EmptyState } from "../../components/common/EmptyState"

const DIAS_SEMANA: { valor: number; label: string }[] = [
    { valor: 1, label: "Segunda-feira" },
    { valor: 2, label: "Terça-feira" },
    { valor: 3, label: "Quarta-feira" },
    { valor: 4, label: "Quinta-feira" },
    { valor: 5, label: "Sexta-feira" },
    { valor: 6, label: "Sábado" },
    { valor: 7, label: "Domingo" },
]

function nomeDia(valor: number) {
    return DIAS_SEMANA.find((d) => d.valor === valor)?.label ?? String(valor)
}

type Aba = "perfil" | "disponibilidade" | "bloqueios"

export default function ProfissionalDetailPage() {
    const { id } = useParams<{ id: string }>()
    const profissionalId = Number(id)
    const navigate = useNavigate()
    const { usuario, temRole } = useAuth()

    const [profissional, setProfissional] = useState<ProfissionalResponse | null>(null)
    const [erro, setErro] = useState<string | null>(null)
    const [aba, setAba] = useState<Aba>("perfil")

    const [especialidadeForm, setEspecialidadeForm] = useState("")
    const [salvandoEspecialidade, setSalvandoEspecialidade] = useState(false)
    const [erroEspecialidade, setErroEspecialidade] = useState<string | null>(null)

    const [disponibilidades, setDisponibilidades] = useState<DisponibilidadeResponse[] | null>(null)
    const [slots, setSlots] = useState<SlotBloqueadoResponse[] | null>(null)

    function carregarProfissional() {
        api
            .get<ProfissionalResponse>(`/api/profissionais/${profissionalId}`)
            .then((p) => {
                setProfissional(p)
                setEspecialidadeForm(p.especialidade)
            })
            .catch((e) => setErro(e instanceof HttpError ? e.message : "Erro ao carregar profissional"))
    }

    function carregarDisponibilidades() {
        api
            .get<DisponibilidadeResponse[]>(`/api/profissionais/${profissionalId}/disponibilidade`)
            .then(setDisponibilidades)
            .catch(() => setDisponibilidades([]))
    }

    function carregarSlots() {
        api
            .get<SlotBloqueadoResponse[]>(`/api/profissionais/${profissionalId}/slots-bloqueados`)
            .then(setSlots)
            .catch(() => setSlots([]))
    }

    useEffect(() => {
        carregarProfissional()
        carregarDisponibilidades()
        carregarSlots()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [profissionalId])

    const ehDono = !!profissional && !!usuario && usuario.id === profissional.idUsuario
    const podeGerenciar = temRole("ADMIN") || (temRole("PROFISSIONAL") && ehDono)

    async function salvarEspecialidade(e: FormEvent) {
        e.preventDefault()
        setErroEspecialidade(null)
        setSalvandoEspecialidade(true)
        try {
            const atualizado = await api.put<ProfissionalResponse>(`/api/profissionais/${profissionalId}`, {
                especialidade: especialidadeForm,
            })
            setProfissional(atualizado)
        } catch (err) {
            setErroEspecialidade(err instanceof HttpError ? err.message : "Erro ao salvar")
        } finally {
            setSalvandoEspecialidade(false)
        }
    }

    async function excluirProfissional() {
        if (!confirm("Excluir este profissional? Esta ação não pode ser desfeita.")) return
        try {
            await api.delete(`/api/profissionais/${profissionalId}`)
            navigate("/profissionais", { replace: true })
        } catch (err) {
            alert(err instanceof HttpError ? err.message : "Erro ao excluir profissional")
        }
    }

    if (erro) return <div style={{ padding: 32 }}><ErrorMessage mensagem={erro} /></div>
    if (!profissional) return <Loading />

    return (
        <div style={{ padding: "28px 32px", maxWidth: 820 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 20 }}>
                <div>
                    <h1 style={{ fontSize: 19, fontWeight: 800, margin: "0 0 4px" }}>{profissional.nome}</h1>
                    <p style={{ color: "#64748b", fontSize: 13, margin: 0 }}>
                        {profissional.especialidade} · <span className={`badge ${profissional.ativo ? "status-livre" : "status-cancelado"}`}>{profissional.ativo ? "Ativo" : "Inativo"}</span>
                    </p>
                </div>
                {temRole("ADMIN") && (
                    <button className="btn-danger" onClick={excluirProfissional}>Excluir</button>
                )}
            </div>

            <div style={{ display: "flex", gap: 4, borderBottom: "1px solid #e2e8f0", marginBottom: 20 }}>
                {([
                    ["perfil", "Perfil"],
                    ["disponibilidade", "Disponibilidade"],
                    ["bloqueios", "Horários Bloqueados"],
                ] as [Aba, string][]).map(([valor, label]) => (
                    <button
                        key={valor}
                        onClick={() => setAba(valor)}
                        style={{
                            padding: "8px 14px",
                            border: "none",
                            background: "none",
                            cursor: "pointer",
                            fontSize: 13,
                            fontWeight: 600,
                            color: aba === valor ? "#0f766e" : "#94a3b8",
                            borderBottom: aba === valor ? "2px solid #0f766e" : "2px solid transparent",
                        }}
                    >
                        {label}
                    </button>
                ))}
            </div>

            {aba === "perfil" && (
                <div className="card" style={{ padding: "20px 24px", maxWidth: 420 }}>
                    {!podeGerenciar ? (
                        <p style={{ fontSize: 13, color: "#64748b" }}>
                            Apenas o próprio profissional ou um administrador podem editar a especialidade.
                        </p>
                    ) : (
                        <form onSubmit={salvarEspecialidade}>
                            {erroEspecialidade && <ErrorMessage mensagem={erroEspecialidade} />}
                            <label className="field-label">Especialidade</label>
                            <input
                                type="text"
                                required
                                value={especialidadeForm}
                                onChange={(e) => setEspecialidadeForm(e.target.value)}
                                style={{ marginBottom: 14 }}
                            />
                            <button className="btn-primary" type="submit" disabled={salvandoEspecialidade}>
                                {salvandoEspecialidade ? "Salvando..." : "Salvar"}
                            </button>
                        </form>
                    )}
                </div>
            )}

            {aba === "disponibilidade" && (
                <DisponibilidadeTab
                    profissionalId={profissionalId}
                    disponibilidades={disponibilidades}
                    podeGerenciar={podeGerenciar}
                    onCriada={carregarDisponibilidades}
                />
            )}

            {aba === "bloqueios" && (
                <SlotsBloqueadosTab
                    profissionalId={profissionalId}
                    slots={slots}
                    podeGerenciar={podeGerenciar}
                    onAtualizar={carregarSlots}
                />
            )}
        </div>
    )
}

function DisponibilidadeTab({
                                profissionalId,
                                disponibilidades,
                                podeGerenciar,
                                onCriada,
                            }: {
    profissionalId: number
    disponibilidades: DisponibilidadeResponse[] | null
    podeGerenciar: boolean
    onCriada: () => void
}) {
    const [form, setForm] = useState<DisponibilidadeRequest>({ diaDaSemana: 1, horaInicio: "08:00", horaFim: "12:00" })
    const [erro, setErro] = useState<string | null>(null)
    const [enviando, setEnviando] = useState(false)

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setEnviando(true)
        try {
            await api.post(`/api/profissionais/${profissionalId}/disponibilidade`, form)
            onCriada()
        } catch (err) {
            setErro(err instanceof HttpError ? err.message : "Erro ao cadastrar disponibilidade")
        } finally {
            setEnviando(false)
        }
    }

    return (
        <div>
            {podeGerenciar && (
                <form onSubmit={handleSubmit} className="card" style={{ padding: "16px 20px", marginBottom: 20, display: "flex", gap: 12, alignItems: "flex-end", flexWrap: "wrap" }}>
                    {erro && <div style={{ width: "100%" }}><ErrorMessage mensagem={erro} /></div>}
                    <div>
                        <label className="field-label">Dia da semana</label>
                        <select value={form.diaDaSemana} onChange={(e) => setForm((f) => ({ ...f, diaDaSemana: Number(e.target.value) }))}>
                            {DIAS_SEMANA.map((d) => (
                                <option key={d.valor} value={d.valor}>{d.label}</option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="field-label">Início</label>
                        <input type="time" value={form.horaInicio} onChange={(e) => setForm((f) => ({ ...f, horaInicio: e.target.value }))} />
                    </div>
                    <div>
                        <label className="field-label">Fim</label>
                        <input type="time" value={form.horaFim} onChange={(e) => setForm((f) => ({ ...f, horaFim: e.target.value }))} />
                    </div>
                    <button className="btn-primary" type="submit" disabled={enviando}>
                        {enviando ? "Salvando..." : "+ Adicionar"}
                    </button>
                </form>
            )}

            <div className="card" style={{ overflow: "hidden" }}>
                {disponibilidades === null ? (
                    <Loading />
                ) : disponibilidades.length === 0 ? (
                    <EmptyState mensagem="Nenhuma disponibilidade cadastrada." />
                ) : (
                    <table>
                        <thead>
                        <tr>
                            <th>Dia</th>
                            <th>Início</th>
                            <th>Fim</th>
                        </tr>
                        </thead>
                        <tbody>
                        {disponibilidades.map((d) => (
                            <tr key={d.id}>
                                <td>{nomeDia(d.diaDaSemana)}</td>
                                <td className="mono">{d.horaInicio}</td>
                                <td className="mono">{d.horaFim}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    )
}

function SlotsBloqueadosTab({
                                profissionalId,
                                slots,
                                podeGerenciar,
                                onAtualizar,
                            }: {
    profissionalId: number
    slots: SlotBloqueadoResponse[] | null
    podeGerenciar: boolean
    onAtualizar: () => void
}) {
    const [form, setForm] = useState<SlotBloqueadoRequest>({ inicio: "", fim: "" })
    const [erro, setErro] = useState<string | null>(null)
    const [enviando, setEnviando] = useState(false)

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setEnviando(true)
        try {
            await api.post(`/api/profissionais/${profissionalId}/slots-bloqueados`, form)
            setForm({ inicio: "", fim: "" })
            onAtualizar()
        } catch (err) {
            setErro(err instanceof HttpError ? err.message : "Erro ao bloquear horário")
        } finally {
            setEnviando(false)
        }
    }

    async function excluir(slotId: number) {
        if (!confirm("Remover este bloqueio?")) return
        try {
            await api.delete(`/api/profissionais/${profissionalId}/slots-bloqueados/${slotId}`)
            onAtualizar()
        } catch (err) {
            alert(err instanceof HttpError ? err.message : "Erro ao remover bloqueio")
        }
    }

    return (
        <div>
            {podeGerenciar && (
                <form onSubmit={handleSubmit} className="card" style={{ padding: "16px 20px", marginBottom: 20, display: "flex", gap: 12, alignItems: "flex-end", flexWrap: "wrap" }}>
                    {erro && <div style={{ width: "100%" }}><ErrorMessage mensagem={erro} /></div>}
                    <div>
                        <label className="field-label">Início</label>
                        <input type="datetime-local" required value={form.inicio} onChange={(e) => setForm((f) => ({ ...f, inicio: e.target.value }))} />
                    </div>
                    <div>
                        <label className="field-label">Fim</label>
                        <input type="datetime-local" required value={form.fim} onChange={(e) => setForm((f) => ({ ...f, fim: e.target.value }))} />
                    </div>
                    <button className="btn-primary" type="submit" disabled={enviando}>
                        {enviando ? "Salvando..." : "+ Bloquear horário"}
                    </button>
                </form>
            )}

            <div className="card" style={{ overflow: "hidden" }}>
                {slots === null ? (
                    <Loading />
                ) : slots.length === 0 ? (
                    <EmptyState mensagem="Nenhum horário bloqueado." />
                ) : (
                    <table>
                        <thead>
                        <tr>
                            <th>Início</th>
                            <th>Fim</th>
                            {podeGerenciar && <th></th>}
                        </tr>
                        </thead>
                        <tbody>
                        {slots.map((s) => (
                            <tr key={s.id}>
                                <td className="mono">{s.inicio.replace("T", " ")}</td>
                                <td className="mono">{s.fim.replace("T", " ")}</td>
                                {podeGerenciar && (
                                    <td>
                                        <button className="btn-danger" style={{ padding: "4px 10px", fontSize: 11.5 }} onClick={() => excluir(s.id)}>
                                            Remover
                                        </button>
                                    </td>
                                )}
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    )
}