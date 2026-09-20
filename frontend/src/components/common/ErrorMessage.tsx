export function ErrorMessage({ mensagem, detalhes }: { mensagem: string; detalhes?: string[] }) {
    return (
        <div
            style={{
                background: "#fef2f2",
                border: "1px solid #fecaca",
                color: "#b91c1c",
                borderRadius: 10,
                padding: "12px 16px",
                fontSize: 13,
                marginBottom: 16,
            }}
        >
            <div style={{ fontWeight: 600 }}>{mensagem}</div>
            {detalhes && detalhes.length > 0 && (
                <ul style={{ margin: "6px 0 0", paddingLeft: 18 }}>
                    {detalhes.map((d, i) => (
                        <li key={i}>{d}</li>
                    ))}
                </ul>
            )}
        </div>
    )
}