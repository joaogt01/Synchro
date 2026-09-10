# Agendamento

Sistema de agendamento de consultas (estilo clínica/consultório), construído com **Spring Boot 4** e **Java 21**. Permite o cadastro de profissionais, pacientes e o gerenciamento completo do ciclo de vida de uma consulta (agendamento, confirmação, conclusão e cancelamento), com regras de disponibilidade, bloqueio de horários e controle de acesso baseado em papéis (RBAC).

## Stack Tecnológica

- **Java 21**
- **Spring Boot 4.0.7** (`spring-boot-starter-parent`)
- **Spring Data JPA** + **PostgreSQL**
- **Flyway** para versionamento de banco de dados
- **Spring Security** com autenticação via **JWT** (`jjwt`)
- **Bean Validation** (`jakarta.validation`)
- **Lombok**
- **Testcontainers** (PostgreSQL) para testes de integração
- **JUnit 5**, **Mockito**, **AssertJ**
- Build via **Maven** (Maven Wrapper incluso — `./mvnw`)

## Arquitetura

O projeto é organizado por **domínio de negócio** (pacote por feature), e não por camada técnica:

```
com.projetos.agendamento
├── autenticacao      # Usuário, login/registro, JWT, Spring Security
├── profissional      # Profissional, Disponibilidade, Slot Bloqueado
├── paciente          # Paciente
├── consulta          # Consulta (agendamento), validadores, eventos
├── notificacao       # Listener assíncrono de eventos de agendamento
└── utils.exception   # Tratamento global de exceções (RestControllerAdvice)
```

Cada módulo de domínio segue internamente o padrão `controller / service / dto / entity / repository`.

## Modelo de Domínio

### Usuário e Papéis (`UserRole`)
- `PACIENTE` — criado via autorregistro público
- `PROFISSIONAL` — criado apenas através do endpoint de criação de profissional
- `ATENDENTE` — criado apenas por um `ADMIN`
- `ADMIN` — criado apenas por outro `ADMIN`

### Profissional
- Possui `especialidade` e `ativo`
- Tem `Disponibilidade` (janelas recorrentes por dia da semana) e `SlotBloqueado` (bloqueios pontuais de horário)

### Paciente
- Vinculado a um `Usuario`, possui `telefone`

### Consulta
- Vincula um `Profissional` e um `Paciente`, com `inicio`, `fim` e `status`
- **Status** (`StatusAgendamento`): `AGENDADO → CONFIRMADO → COMPLETO`, ou `CANCELADO` a qualquer momento antes da finalização
- Ao ser criada com sucesso, publica o evento `ConsultaAgendadaEvento`, consumido de forma assíncrona (`@Async` + `@TransactionalEventListener(AFTER_COMMIT)`) pelo `NotificacaoConsultaListener`, que registra o envio da notificação (atualmente via log)

## Regras de Agendamento (Validadores)

A criação de uma consulta passa por uma cadeia de validadores (`ValidadorAgendamento`), orquestrada pelo `OrquestradorValidacaoAgendamento`, executados como componentes Spring injetados como lista:

| Validador | Regra |
|---|---|
| `IntervaloValidoValidador` | Início e fim obrigatórios; início deve ser anterior ao fim |
| `NaoAgendarNoPassadoValidador` | Não permite agendar horário no passado |
| `DentroDoHorarioAtendimentoValidador` | O horário deve estar dentro da disponibilidade cadastrada do profissional para aquele dia da semana |
| `HorarioNaoBloqueadoValidador` | O horário não pode coincidir com um slot bloqueado pelo profissional |
| `ProfissionalSemConflitoValidador` | O profissional não pode ter outra consulta (não cancelada) que se sobreponha ao intervalo |
| `PacienteSemConflitoValidador` | O paciente não pode ter outra consulta (não cancelada) que se sobreponha ao intervalo |

Além da validação em nível de aplicação, o banco possui uma **constraint de exclusão** (`EXCLUDE USING gist`, com extensão `btree_gist`) que impede sobreposição de horários por profissional diretamente no PostgreSQL, como camada extra de proteção contra condições de corrida.

## Autenticação e Autorização

- Login/registro via `/api/auth/**` (público)
- Autenticação **stateless** via JWT, validado em `JwtAuthenticationFilter`
- Autorização por método com `@PreAuthorize` (`hasRole`, `hasAnyRole`, `isAuthenticated`)
- Regras de **ownership** (dono do recurso) aplicadas manualmente nos services, via `AutenticacaoUtils`, para casos em que o papel sozinho não é suficiente (ex.: um `PROFISSIONAL` só pode editar seu próprio cadastro; um `PACIENTE` só pode ver a própria consulta)

## Endpoints Principais

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/auth/registrar` | Público (cria `PACIENTE`) |
| POST | `/api/auth/login` | Público |
| POST | `/api/admin/usuarios` | `ADMIN` (cria `ADMIN`/`ATENDENTE`) |
| POST | `/api/profissionais` | `ADMIN` |
| GET | `/api/profissionais` / `/{id}` | Autenticado |
| PUT | `/api/profissionais/{id}` | `ADMIN` ou dono |
| DELETE | `/api/profissionais/{id}` | `ADMIN` |
| POST/GET | `/api/profissionais/{id}/disponibilidade` | `ADMIN`/`PROFISSIONAL` (dono) / autenticado |
| POST | `/api/pacientes` | `ADMIN` |
| GET | `/api/pacientes/{id}` | `ADMIN`, `PROFISSIONAL`, `ATENDENTE` ou dono |
| GET | `/api/pacientes` | `ADMIN`, `PROFISSIONAL`, `ATENDENTE` |
| PUT | `/api/pacientes/{id}` | `ADMIN` ou dono |
| DELETE | `/api/pacientes/{id}` | `ADMIN` |
| POST | `/api/consultas` | `ADMIN`, `ATENDENTE`, `PROFISSIONAL` |
| GET | `/api/consultas/{id}` | Autenticado (com verificação de ownership) |
| GET | `/api/consultas` | Autenticado (filtros forçados por papel) |
| POST | `/api/consultas/{id}/confirmar` | `ADMIN`, `ATENDENTE`, `PROFISSIONAL` dono |
| POST | `/api/consultas/{id}/completar` | `ADMIN`, `PROFISSIONAL` dono |
| POST | `/api/consultas/{id}/cancelar` | `ADMIN`, `PROFISSIONAL` dono |

## Tratamento de Erros

O `GlobalExceptionHandler` centraliza a conversão de exceções em respostas padronizadas (`ApiError`):

- `AccessDeniedException` → `403 Forbidden`
- `ResourceNotFoundException` → `404 Not Found`
- `IllegalArgumentException` (violação de regra de negócio) → `409 Conflict`
- `IllegalStateException` (transição de estado inválida) → `409 Conflict`
- Qualquer outra exceção → `500 Internal Server Error`

## Banco de Dados

Migrações Flyway em `src/main/resources/db/migration`:

1. `V1` — tabela `usuarios`
2. `V2` — tabelas `profissionais` e `pacientes`
3. `V3` — tabelas `disponibilidade` e `slots_bloqueados`
4. `V4` — tabela `consulta` (com unique constraint inicial por `profissional_id` + `inicio`)
5. `V5` — substitui a unique constraint por uma **exclusion constraint** baseada em intervalo (`tsrange`), permitindo detectar sobreposição real de horários (não apenas mesmo instante de início)

## Como Rodar

### Pré-requisitos
- JDK 21
- Docker (para o PostgreSQL via `docker-compose`, ou para os testes com Testcontainers)

### Passos

1. Copie o arquivo de variáveis de ambiente:
   ```bash
   cp .env.example .env
   ```
   Ajuste `JWT_SECRET` (mínimo 32 bytes) e demais variáveis conforme necessário.

2. Suba o banco de dados:
   ```bash
   docker compose up -d
   ```

3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

A aplicação sobe por padrão na porta definida em `SERVER_PORT` (`8080`).

### Rodando os testes

```bash
./mvnw test
```

Os testes de integração usam **Testcontainers** e sobem automaticamente um container PostgreSQL (`postgres:18`) — não é necessário Docker Compose rodando para eles, apenas o Docker daemon disponível.

## Cobertura de Testes

O projeto possui uma suíte considerável de testes:

- **Testes unitários de serviço** (Mockito): `UsuarioServiceTest`, `ProfissionalServiceTest`, `PacienteServiceTest`, `DisponibilidadeServiceTest`, `ConsultaServiceTest` — cobrindo regras de negócio, ownership e papéis
- **Testes unitários de validadores**: `IntervaloValidoValidadorTest`, `NaoAgendarNoPassadoValidadorTest`, `ProfissionalSemConflitoValidadorTest`
- **Testes de evento/listener**: `NotificacaoConsultaListenerTest`
- **Testes de integração** (Testcontainers + Spring Boot completo): `ConsultaServiceIntegrationTest`, `ConsultaAgendamentoEventoIntegracaoTest`, `FlywayMigrationTest`, `AgendamentoApplicationTests`

## Status Atual / Observações

- O core de agendamento (criação, confirmação, conclusão, cancelamento, validações de conflito e disponibilidade) está implementado e testado.
- O fluxo de notificação após o agendamento está implementado de forma assíncrona, porém **o envio real ainda é simulado via log** (`NotificacaoConsultaListener.enviarNotificacao`) — não há integração com e-mail/SMS/push ainda.
- Campos de metadados do projeto no `pom.xml` (nome, descrição, URL, licença, desenvolvedores, SCM) ainda estão em branco e podem ser preenchidos.
- Não há documentação OpenAPI/Swagger configurada até o momento.
