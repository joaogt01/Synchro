import { FormEvent, useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { PacienteResponse } from "../../types/api"
import { useAuth } from "../../lib/auth"
import { Loading } from "../../components/common/Loading"
import { ErrorMessage } from "../../components/common/ErrorMessage"
import { EmptyState } from "../../components/common/EmptyState"

export default function PacientesListPage() {
    const { temRole } = useAuth()
    const navigate = useNavigate()

    if (temRole("PACIENTE")) {
        return <BuscaPacientePorIdPaciente />
    }

    return <ListaCompleta />

    function ListaCompleta() {
        const [lista, setLista] = useState<PacienteResponse[] | null>(null)
        const [erro, setErro] = useState<string | null>(null)

        useEffect(() => {
            api
                .get<PacienteResponse[]>("/api/pacientes")
                .then(setLista)
                .catch((e) => setErro(e instanceof HttpError ? e.message : "Erro ao carregar pacientes"))
        }, [])

        return (
            <div style={{ padding: "28px 32px" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                    <h1 style={{ fontSize: 19, fontWeight: 800, margin: 0 }}>Pacientes</h1>
                    {temRole("ADMIN") && (
                        <Link to="/pacientes/novo" className="btn-primary" style={{ textDecoration: "none" }}>
                            + Novo Paciente
                        </Link>
                    )}
                </div>

                {erro && <ErrorMessage mensagem={erro} />}
                {!lista && !erro && <Loading />}

                {lista && (
                    <div className="card" style={{ overflow: "hidden" }}>
                        {lista.length === 0 ? (
                            <EmptyState mensagem="Nenhum paciente cadastrado ainda." />
                        ) : (
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nome</th>
                                    <th>Telefone</th>
                                </tr>
                                </thead>
                                <tbody>
                                {lista.map((p) => (
                                    <tr key={p.id} className="clickable" onClick={() => navigate(`/pacientes/${p.id}`)}>
                                        <td className="mono">{p.id}</td>
                                        <td style={{ fontWeight: 600 }}>{p.nomeUsuario}</td>
                                        <td>{p.telefone}</td>
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
}

function BuscaPacientePorIdPaciente() {
    const navigate = useNavigate()
    const [idBusca, setIdBusca] = useState("")

    function handleSubmit(e: FormEvent) {
        e.preventDefault()
        if (idBusca) navigate(`/pacientes/${idBusca}`)
    }

    return (
        <div style={{ padding: "28px 32px" }}>
            <h1 style={{ fontSize: 19, fontWeight: 800, marginBottom: 12 }}>Pacientes</h1>
            <div className="card" style={{ padding: "18px 20px", maxWidth: 480, marginBottom: 16 }}>
                <p style={{ fontSize: 12.5, color: "#64748b", margin: "0 0 12px" }}>
                    A API ainda não oferece um endpoint de "meu cadastro de paciente". Se você já sabe o ID do seu
                    cadastro de paciente, pode consultá-lo abaixo (o acesso só é permitido ao próprio dono).
                </p>
                <form onSubmit={handleSubmit} style={{ display: "flex", gap: 10 }}>
                    <input type="number" placeholder="ID do paciente" value={idBusca} onChange={(e) => setIdBusca(e.target.value)} />
                    <button className="btn-primary" type="submit">Ver</button>
                </form>
            </div>
        </div>
    )
}