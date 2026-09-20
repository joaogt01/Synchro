export function EmptyState({ mensagem }: { mensagem: string }) {
    return (
        <div style={{ textAlign: "center", padding: "32px 16px", color: "#94a3b8", fontSize: 13.5 }}>
            {mensagem}
        </div>
    )
}