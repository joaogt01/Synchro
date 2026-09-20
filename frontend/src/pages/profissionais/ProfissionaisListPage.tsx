import { useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { api, HttpError } from "../../lib/api"
import type { ProfissionalResponse } from "../../types/api"
import { useAuth } from "../../lib/auth"
import { Loading } from "../../components/common/Loading"
import { ErrorMessage } from "../../components/common/ErrorMessage"
import { EmptyState } from "../../components/common/EmptyState"

export default function ProfissionaisListPage() {
    const { temRole } = useAuth()
    const navigate = useNavigate()
    const [lista, setLista] = useState<ProfissionalResponse[] | null>(null)
    const [erro, setErro] = useState<string | null>(null)

    useEffect(() => {
        api
            .get<ProfissionalResponse[]>("/api/profissionais")
            .then(setLista)
            .catch((e) => setErro(e instanceof HttpError ? e.message : "Erro ao carregar profissionais"))
    }, [])

    return (
        <div style={{ padding: "28px 32px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                <h1 style={{ fontSize: 19, fontWeight: 800, margin: 0 }}>Profissionais</h1>
                {temRole("ADMIN") && (
                    <Link to="/profissionais/novo" className="btn-primary" style={{ textDecoration: "none" }}>
                        + Novo Profissional
                    </Link>
                )}
            </div>

            {erro && <ErrorMessage mensagem={erro} />}
            {!lista && !erro && <Loading />}

            {lista && (
                <div className="card" style={{ overflow: "hidden" }}>
                    {lista.length === 0 ? (
                        <EmptyState mensagem="Nenhum profissional cadastrado ainda." />
                    ) : (
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome</th>
                                <th>Especialidade</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            {lista.map((p) => (
                                <tr key={p.id} className="clickable" onClick={() => navigate(`/profissionais/${p.id}`)}>
                                    <td className="mono">{p.id}</td>
                                    <td style={{ fontWeight: 600 }}>{p.nome}</td>
                                    <td>{p.especialidade}</td>
                                    <td>
                      <span className={`badge ${p.ativo ? "status-livre" : "status-cancelado"}`}>
                        {p.ativo ? "Ativo" : "Inativo"}
                      </span>
                                    </td>
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