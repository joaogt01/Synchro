export function Loading({ label = "Carregando..." }: { label?: string }) {
    return (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: 48, color: "#94a3b8", fontSize: 13.5 }}>
            {label}
        </div>
    )
}