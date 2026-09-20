export type UserRole = 'PACIENTE' | 'PROFISSIONAL' | 'ADMIN' | 'ATENDENTE'

export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'COMPLETO' | 'CANCELADO'

export interface LoginRequest {
    email: string
    senha: string
}

export interface RegistroRequest {
    nome: string
    email: string
    senha: string
}

export interface LoginResponse {
    token: string
    tokenType: string
}

export interface UsuarioResponse {
    id: number
    nome: string
    email: string
    role: UserRole
}

export interface NovoUsuarioPrivilegiadoRequest {
    nome: string
    email: string
    senha: string
    role: UserRole
}

export interface ProfissionalRequest {
    nome: string
    email: string
    senha: string
    especialidade: string
}

export interface ProfissionalAtualizarRequest {
    especialidade: string
}

export interface ProfissionalResponse {
    id: number
    idUsuario: number
    nome: string
    especialidade: string
    ativo: boolean
}

export interface DisponibilidadeRequest {
    diaDaSemana: number
    horaInicio: string
    horaFim: string
}

export interface DisponibilidadeResponse {
    id: number
    profissionalId: number
    diaDaSemana: number
    horaInicio: string
    horaFim: string
}

export interface SlotBloqueadoRequest {
    inicio: string
    fim: string
}

export interface SlotBloqueadoResponse {
    id: number
    profissionalId: number
    inicio: string
    fim: string
}

export interface PacienteRequest {
    usuarioId: number
    telefone: string
}

export interface PacienteResponse {
    id: number
    usuarioId: number
    nomeUsuario: string
    telefone: string
}

export interface ConsultaRequest {
    profissionalId: number
    pacienteId: number
    inicio: string // ISO LocalDateTime
    fim: string
}

export interface ConsultaResponse {
    id: number
    profissional: { id: number; nome: string } | null
    paciente: { id: number; nome: string } | null
    inicio: string
    fim: string
    status: StatusAgendamento
}

export interface ApiError {
    timestamp: string
    status: number
    erro: string
    mensagem: string
    detalhes: string[]
}