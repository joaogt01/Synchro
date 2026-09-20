import { useState } from "react"
import { NavLink, Outlet } from "react-router-dom"
import { useAuth } from "../../lib/auth"
import type { UserRole } from "../../types/api"

interface NavItem {
    to: string
    label: string
    icon: string
    roles?: UserRole[]
}

const navItems: NavItem[] = [
    { to: "/", label: "Dashboard", icon: "⊞" },
    { to: "/profissionais", label: "Profissionais", icon: "♦" },
    { to: "/pacientes", label: "Pacientes", icon: "♥", roles: ["ADMIN", "PROFISSIONAL", "ATENDENTE"] },
    { to: "/consultas", label: "Consultas", icon: "◷" },
    { to: "/admin/usuarios", label: "Usuários (Admin)", icon: "◈", roles: ["ADMIN"] },
]

export function AppLayout() {
    const { usuario, logout, temRole } = useAuth()
    const [sidebarOpen, setSidebarOpen] = useState(true)

    const itemsVisiveis = navItems.filter((item) => !item.roles || temRole(...item.roles))

    return (
        <div style={{ display: "flex", height: "100vh", overflow: "hidden", background: "#f8fafc" }}>
            <aside
                style={{
                    width: sidebarOpen ? 220 : 62,
                    flexShrink: 0,
                    background: "#fff",
                    borderRight: "1px solid #e2e8f0",
                    display: "flex",
                    flexDirection: "column",
                    transition: "width 0.2s",
                    overflow: "hidden",
                }}
            >
                <div style={{ padding: "18px 16px 14px", display: "flex", alignItems: "center", gap: 10, borderBottom: "1px solid #f1f5f9" }}>
                    <div
                        style={{
                            width: 32,
                            height: 32,
                            borderRadius: 8,
                            background: "linear-gradient(135deg, #0f766e, #14b8a6)",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            flexShrink: 0,
                            fontSize: 16,
                            color: "#fff",
                            fontWeight: 800,
                            fontFamily: "var(--font-display)",
                        }}
                    >
                        +
                    </div>
                    {sidebarOpen && (
                        <div>
                            <div style={{ fontSize: 13.5, fontWeight: 700, fontFamily: "var(--font-display)", color: "#0f172a", lineHeight: 1.2 }}>
                                MedAgenda
                            </div>
                            <div style={{ fontSize: 10.5, color: "#94a3b8", fontFamily: "var(--font-mono)" }}>v0.1 · CLÍNICA</div>
                        </div>
                    )}
                </div>

                <nav style={{ flex: 1, padding: "12px 10px", display: "flex", flexDirection: "column", gap: 2 }}>
                    {itemsVisiveis.map((item) => (
                        <NavLink
                            key={item.to}
                            to={item.to}
                            end={item.to === "/"}
                            className={({ isActive }) => `sidebar-item${isActive ? " active" : ""}`}
                            style={{ textDecoration: "none", justifyContent: sidebarOpen ? "flex-start" : "center" }}
                            title={!sidebarOpen ? item.label : undefined}
                        >
                            <span style={{ fontSize: 16, flexShrink: 0 }}>{item.icon}</span>
                            {sidebarOpen && <span>{item.label}</span>}
                        </NavLink>
                    ))}
                </nav>

                <div style={{ padding: "12px 10px", borderTop: "1px solid #f1f5f9" }}>
                    <button
                        className="sidebar-item"
                        style={{ border: "none", width: "100%", justifyContent: sidebarOpen ? "flex-start" : "center", background: "none", cursor: "pointer" }}
                        onClick={() => setSidebarOpen((v) => !v)}
                    >
                        <span style={{ fontSize: 15 }}>{sidebarOpen ? "←" : "→"}</span>
                        {sidebarOpen && <span>Recolher</span>}
                    </button>
                    {sidebarOpen && usuario && (
                        <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 14px", marginTop: 4 }}>
                            <div
                                style={{
                                    width: 28,
                                    height: 28,
                                    borderRadius: "50%",
                                    background: "linear-gradient(135deg,#0f766e,#14b8a6)",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    color: "#fff",
                                    fontSize: 11,
                                    fontWeight: 700,
                                    flexShrink: 0,
                                }}
                            >
                                {usuario.nome.split(" ").map((n) => n[0]).slice(0, 2).join("")}
                            </div>
                            <div style={{ overflow: "hidden" }}>
                                <div style={{ fontSize: 12, fontWeight: 600, color: "#0f172a", whiteSpace: "nowrap", textOverflow: "ellipsis", overflow: "hidden" }}>
                                    {usuario.nome}
                                </div>
                                <div style={{ fontSize: 10.5, color: "#94a3b8", fontFamily: "var(--font-mono)" }}>{usuario.role}</div>
                            </div>
                        </div>
                    )}
                    {sidebarOpen && (
                        <button className="btn-ghost" style={{ width: "100%", justifyContent: "center", marginTop: 8 }} onClick={logout}>
                            Sair
                        </button>
                    )}
                </div>
            </aside>

            <main style={{ flex: 1, overflow: "auto", display: "flex", flexDirection: "column" }}>
                <Outlet />
            </main>
        </div>
    )
}