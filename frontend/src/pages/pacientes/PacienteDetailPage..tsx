import { FormEvent, useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { PacienteResponse } from "../../types/api"
import { useAuth } from "../../lib/auth"
import { Loading } from "../../components/common/Loading"
import { ErrorMessage } from "../../components/common/ErrorMessage"

export default function PacienteDetailPage() {
    const { id } = useParams<{ id: string }>()
    const pacienteId = Number(id)
    const navigate = useNavigate()
    const { usuario, temRole } = useAuth()

    const [paciente, setPaciente] = useState<PacienteResponse | null>(null)
    const [erro, setErro] = useState<string | null>(null)
    const [telefoneForm, setTelefoneForm] = useState("")
    const [salvando, setSalvando] = useState(false)
    const [erroSalvar, setErroSalvar] = useState<string | null>(null)

    useEffect(() => {
        api
            .get<PacienteResponse>(`/api/pacientes/${pacienteId}`)
            .then((p) => {
                setPaciente(p)
                setTelefoneForm(p.telefone ?? "")
            })
            .catch((e) => setErro(e instanceof HttpError ? e.message : "Erro ao carregar paciente"))
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [pacienteId])

    const ehDono = !!paciente && !!usuario && usuario.id === paciente.usuarioId
    const podeEditar = temRole("ADMIN") || (temRole("PACIENTE") && ehDono)

    async function salvar(e: FormEvent) {
        e.preventDefault()
        if (!paciente) return
        setErroSalvar(null)
        setSalvando(true)
        try {
            const atualizado = await api.put<PacienteResponse>(`/api/pacientes/${pacienteId}`, {
                usuarioId: paciente.usuarioId,
                telefone: telefoneForm,
            })
            setPaciente(atualizado)
        } catch (err) {
            setErroSalvar(err instanceof HttpError ? err.message : "Erro ao salvar")
        } finally {
            setSalvando(false)
        }
    }

    async function excluir() {
        if (!confirm("Excluir este paciente?")) return
        try {
            await api.delete(`/api/pacientes/${pacienteId}`)
            navigate("/pacientes", { replace: true })
        } catch (err) {
            alert(err instanceof HttpError ? err.message : "Erro ao excluir paciente")
        }
    }

    if (erro) return <div style={{ padding: 32 }}><ErrorMessage mensagem={erro} /></div>
    if (!paciente) return <Loading />

    return (
        <div style={{ padding: "28px 32px", maxWidth: 480 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 20 }}>
                <h1 style={{ fontSize: 19, fontWeight: 800, margin: 0 }}>{paciente.nomeUsuario}</h1>
                {temRole("ADMIN") && <button className="btn-danger" onClick={excluir}>Excluir</button>}
            </div>

            <div className="card" style={{ padding: "20px 24px" }}>
                {erroSalvar && <ErrorMessage mensagem={erroSalvar} />}
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">ID do Paciente</label>
                    <div className="mono" style={{ fontSize: 13.5 }}>{paciente.id}</div>
                </div>
                <div style={{ marginBottom: 14 }}>
                    <label className="field-label">ID do Usuário</label>
                    <div className="mono" style={{ fontSize: 13.5 }}>{paciente.usuarioId}</div>
                </div>

                {podeEditar ? (
                    <form onSubmit={salvar}>
                        <label className="field-label">Telefone</label>
                        <input type="text" value={telefoneForm} onChange={(e) => setTelefoneForm(e.target.value)} style={{ marginBottom: 14 }} />
                        <button className="btn-primary" type="submit" disabled={salvando}>
                            {salvando ? "Salvando..." : "Salvar"}
                        </button>
                    </form>
                ) : (
                    <div>
                        <label className="field-label">Telefone</label>
                        <div style={{ fontSize: 13.5 }}>{paciente.telefone}</div>
                    </div>
                )}
            </div>
        </div>
    )
}