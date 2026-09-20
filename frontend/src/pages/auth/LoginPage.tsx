import { FormEvent, useState } from "react"
import { Link, useLocation, useNavigate } from "react-router-dom"
import { useAuth } from "../../lib/auth"
import { HttpError } from "../../lib/api"
import { ErrorMessage } from "../../components/common/ErrorMessage"

export default function LoginPage() {
    const { login } = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const [email, setEmail] = useState("")
    const [senha, setSenha] = useState("")
    const [erro, setErro] = useState<string | null>(null)
    const [enviando, setEnviando] = useState(false)

    const from = (location.state as { from?: Location })?.from?.pathname ?? "/"

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setEnviando(true)
        try {
            await login({ email, senha })
            navigate(from, { replace: true })
        } catch (err) {
            setErro(err instanceof HttpError ? err.message : "Não foi possível entrar")
        } finally {
            setEnviando(false)
        }
    }

    return (
        <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc" }}>
            <div className="card" style={{ padding: "32px 36px", width: 380 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 24 }}>
                    <div
                        style={{
                            width: 36, height: 36, borderRadius: 9,
                            background: "linear-gradient(135deg, #0f766e, #14b8a6)",
                            display: "flex", alignItems: "center", justifyContent: "center",
                            color: "#fff", fontWeight: 800, fontSize: 18, fontFamily: "var(--font-display)",
                        }}
                    >+</div>
                    <div>
                        <div style={{ fontSize: 15, fontWeight: 700, fontFamily: "var(--font-display)" }}>MedAgenda</div>
                        <div style={{ fontSize: 11, color: "#94a3b8" }}>Entrar na sua conta</div>
                    </div>
                </div>

                {erro && <ErrorMessage mensagem={erro} />}

                <form onSubmit={handleSubmit}>
                    <div style={{ marginBottom: 14 }}>
                        <label className="field-label">E-mail</label>
                        <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder="voce@exemplo.com" />
                    </div>
                    <div style={{ marginBottom: 20 }}>
                        <label className="field-label">Senha</label>
                        <input type="password" required value={senha} onChange={(e) => setSenha(e.target.value)} placeholder="••••••••" />
                    </div>
                    <button className="btn-primary" type="submit" disabled={enviando} style={{ width: "100%", justifyContent: "center" }}>
                        {enviando ? "Entrando..." : "Entrar"}
                    </button>
                </form>

                <p style={{ textAlign: "center", fontSize: 12.5, color: "#64748b", marginTop: 18 }}>
                    Não tem conta? <Link to="/registrar" style={{ color: "#0f766e", fontWeight: 600, textDecoration: "none" }}>Cadastre-se como paciente</Link>
                </p>
            </div>
        </div>
    )
}