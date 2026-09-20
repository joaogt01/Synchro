import { FormEvent, useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { useAuth } from "../../lib/auth"
import { HttpError } from "../../lib/api"
import { ErrorMessage } from "../../components/common/ErrorMessage"

export default function RegisterPage() {
    const { registrar } = useAuth()
    const navigate = useNavigate()
    const [nome, setNome] = useState("")
    const [email, setEmail] = useState("")
    const [senha, setSenha] = useState("")
    const [erro, setErro] = useState<string | null>(null)
    const [detalhes, setDetalhes] = useState<string[]>([])
    const [enviando, setEnviando] = useState(false)

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setErro(null)
        setDetalhes([])
        setEnviando(true)
        try {
            await registrar({ nome, email, senha })
            navigate("/", { replace: true })
        } catch (err) {
            if (err instanceof HttpError) {
                setErro(err.message)
                setDetalhes(err.detalhes)
            } else {
                setErro("Não foi possível concluir o cadastro")
            }
        } finally {
            setEnviando(false)
        }
    }

    return (
        <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc" }}>
            <div className="card" style={{ padding: "32px 36px", width: 400 }}>
                <div style={{ marginBottom: 20 }}>
                    <div style={{ fontSize: 15, fontWeight: 700, fontFamily: "var(--font-display)" }}>Criar conta de paciente</div>
                    <div style={{ fontSize: 11.5, color: "#94a3b8", marginTop: 2 }}>
                        Este cadastro público sempre cria uma conta com papel PACIENTE.
                    </div>
                </div>

                {erro && <ErrorMessage mensagem={erro} detalhes={detalhes} />}

                <form onSubmit={handleSubmit}>
                    <div style={{ marginBottom: 14 }}>
                        <label className="field-label">Nome completo</label>
                        <input type="text" required value={nome} onChange={(e) => setNome(e.target.value)} />
                    </div>
                    <div style={{ marginBottom: 14 }}>
                        <label className="field-label">E-mail</label>
                        <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
                    </div>
                    <div style={{ marginBottom: 20 }}>
                        <label className="field-label">Senha (mínimo 8 caracteres)</label>
                        <input type="password" required minLength={8} value={senha} onChange={(e) => setSenha(e.target.value)} />
                    </div>
                    <button className="btn-primary" type="submit" disabled={enviando} style={{ width: "100%", justifyContent: "center" }}>
                        {enviando ? "Enviando..." : "Criar conta"}
                    </button>
                </form>

                <p style={{ textAlign: "center", fontSize: 12.5, color: "#64748b", marginTop: 18 }}>
                    Já tem conta? <Link to="/login" style={{ color: "#0f766e", fontWeight: 600, textDecoration: "none" }}>Entrar</Link>
                </p>
            </div>
        </div>
    )
}