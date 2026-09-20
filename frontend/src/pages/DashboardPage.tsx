import { Link } from "react-router-dom"
import { useAuth } from "../lib/auth"
import type { UserRole } from "../types/api"

export default function DashboardPage() {
    const { usuario, temRole } = useAuth()

    const atalhos: { to: string; label: string; desc: string; roles?: UserRole[] }[] = [
        { to: "/profissionais", label: "Profissionais", desc: "Ver, cadastrar e gerenciar agendas de profissionais" },
        { to: "/pacientes", label: "Pacientes", desc: "Cadastro e consulta de pacientes", roles: ["ADMIN", "PROFISSIONAL", "ATENDENTE"] },
        { to: "/consultas", label: "Consultas", desc: "Agendar, confirmar, completar e cancelar consultas" },
    ]

    return (
        <div style={{ padding: "28px 32px" }}>
            <h1 style={{ fontSize: 21, fontWeight: 800, margin: "0 0 4px" }}>
                Olá, {usuario?.nome ?? ""} 👋
            </h1>
            <p style={{ color: "#64748b", fontSize: 13.5, margin: "0 0 24px" }}>
                Perfil atual: <strong>{usuario?.role}</strong>
            </p>

            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: 16, marginBottom: 24 }}>
                {atalhos
                    .filter((a) => !a.roles || temRole(...a.roles))
                    .map((a) => (
                        <Link key={a.to} to={a.to} className="card" style={{ padding: "18px 20px", textDecoration: "none", display: "block" }}>
                            <div style={{ fontSize: 14.5, fontWeight: 700, color: "#0f172a", fontFamily: "var(--font-display)", marginBottom: 6 }}>
                                {a.label}
                            </div>
                            <div style={{ fontSize: 12.5, color: "#64748b" }}>{a.desc}</div>
                        </Link>
                    ))}
            </div>

            {temRole("PACIENTE") && (
                <div className="card" style={{ padding: "16px 20px", background: "#fefce8", borderColor: "#fde68a" }}>
                    <strong style={{ fontSize: 13 }}>Aviso:</strong>
                    <p style={{ fontSize: 12.5, color: "#854d0e", margin: "6px 0 0" }}>
                        A API ainda não expõe um endpoint para o paciente consultar o próprio cadastro ou histórico de
                        consultas diretamente. Assim que o backend
                        adicionar esses endpoints, esta tela poderá mostrar seus dados automaticamente.
                    </p>
                </div>
            )}
        </div>
    )
}