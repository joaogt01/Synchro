import { Navigate, Route, Routes } from "react-router-dom"
import { RequireAuth, RequireRole } from "./lib/routeGuards"
import { AppLayout } from "./components/layout/AppLayout"

import LoginPage from "./pages/auth/LoginPage"
import RegisterPage from "./pages/auth/RegisterPage"
import DashboardPage from "./pages/DashboardPage"

import ProfissionaisListPage from "./pages/profissionais/ProfissionaisListPage"
import ProfissionalDetailPage from "./pages/profissionais/ProfissionalDetailPage"
import ProfissionalCreatePage from "./pages/profissionais/ProfissionalCreatePage"

import PacientesListPage from "./pages/pacientes/PacientesListPage"
import PacienteDetailPage from "./pages/pacientes/PacienteDetailPage"
import PacienteCreatePage from "./pages/pacientes/PacienteCreatePage"

import ConsultasListPage from "./pages/consultas/ConsultasListPage"
import ConsultaDetailPage from "./pages/consultas/ConsultaDetailPage"
import ConsultaCreatePage from "./pages/consultas/ConsultaCreatePage"

import AdminUsuariosPage from "./pages/admin/AdminUsuariosPage"

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/registrar" element={<RegisterPage />} />

            <Route
                element={
                    <RequireAuth>
                        <AppLayout />
                    </RequireAuth>
                }
            >
                <Route index element={<DashboardPage />} />

                <Route path="profissionais" element={<ProfissionaisListPage />} />
                <Route
                    path="profissionais/novo"
                    element={
                        <RequireRole roles={["ADMIN"]}>
                            <ProfissionalCreatePage />
                        </RequireRole>
                    }
                />
                <Route path="profissionais/:id" element={<ProfissionalDetailPage />} />

                <Route
                    path="pacientes"
                    element={
                        <RequireRole roles={["ADMIN", "PROFISSIONAL", "ATENDENTE", "PACIENTE"]}>
                            <PacientesListPage />
                        </RequireRole>
                    }
                />
                <Route
                    path="pacientes/novo"
                    element={
                        <RequireRole roles={["ADMIN"]}>
                            <PacienteCreatePage />
                        </RequireRole>
                    }
                />
                <Route path="pacientes/:id" element={<PacienteDetailPage />} />

                <Route path="consultas" element={<ConsultasListPage />} />
                <Route
                    path="consultas/nova"
                    element={
                        <RequireRole roles={["ADMIN", "ATENDENTE", "PROFISSIONAL"]}>
                            <ConsultaCreatePage />
                        </RequireRole>
                    }
                />
                <Route path="consultas/:id" element={<ConsultaDetailPage />} />

                <Route
                    path="admin/usuarios"
                    element={
                        <RequireRole roles={["ADMIN"]}>
                            <AdminUsuariosPage />
                        </RequireRole>
                    }
                />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    )
}