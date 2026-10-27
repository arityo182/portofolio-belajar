import { C } from "../../../core/theme";
/** Render markdown-like bold and bullet points safely as React elements */
function renderContent(text) {
    const lines = text.split("\n");
    return lines.map((line, i) => {
        // Bold: **text**
        const parts = line.split(/(\*\*.*?\*\*)/g);
        const rendered = parts.map((part, j) => {
            if (part.startsWith("**") && part.endsWith("**")) {
                return <strong key={j}>{part.slice(2, -2)}</strong>;
            }
            return part;
        });
        // Bullet: - text
        if (line.startsWith("- ")) {
            return (<div key={i}>
          • {rendered.map((r) => (typeof r === "string" ? r.replace(/^- /, "") : r))}
        </div>);
        }
        return <div key={i}>{rendered}</div>;
    });
}
export default function FloatingChatBubble({ message }) {
    const isUser = message.role === "user";
    return (<div className={`flex ${isUser ? "justify-end" : "justify-start"} mb-3`}>
      {!isUser && (<div className="mr-2 mt-1 flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold text-white" style={{ backgroundColor: C.navy }}>
          S
        </div>)}
      <div className="max-w-[75%] rounded-2xl px-3.5 py-2.5 text-sm leading-relaxed" style={isUser
            ? {
                backgroundColor: C.orange,
                color: C.white,
                borderRadius: "16px 16px 4px 16px",
            }
            : {
                backgroundColor: "#F3F4F6",
                color: "#1F2937",
                borderRadius: "16px 16px 16px 4px",
            }}>
        {renderContent(message.content)}
      </div>
    </div>);
}
