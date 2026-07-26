import { C } from "../../../core/theme";
import type { FloatingMessage } from "../types";

/**
 * Properti untuk {@link FloatingChatBubble}.
 */
interface BubbleProps {
  /** Data pesan */
  message: FloatingMessage;
}

/**
 * Auto-scroll ke bawah saat mount / konten berubah.
 */
function sanitize(text: string): string {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\n/g, "<br/>")
    .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
    .replace(/- (.+)/g, "&bull; $1");
}

/**
 * Satu bubble chat di floating widget. Tampilan berbeda untuk user vs assistant.
 *
 * @param props - lihat {@link BubbleProps}
 * @returns Elemen bubble
 */
export default function FloatingChatBubble({ message }: BubbleProps) {
  const isUser = message.role === "user";

  return (
    <div className={`flex ${isUser ? "justify-end" : "justify-start"} mb-3`}>
      {!isUser && (
        <div
          className="mr-2 mt-1 flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold text-white"
          style={{ backgroundColor: C.navy }}
        >
          S
        </div>
      )}
      <div
        className="max-w-[75%] rounded-2xl px-3.5 py-2.5 text-sm leading-relaxed"
        style={
          isUser
            ? {
                backgroundColor: C.orange,
                color: C.white,
                borderRadius: "16px 16px 4px 16px",
              }
            : {
                backgroundColor: "#F3F4F6",
                color: "#1F2937",
                borderRadius: "16px 16px 16px 4px",
              }
        }
        dangerouslySetInnerHTML={{ __html: sanitize(message.content) }}
      />
    </div>
  );
}
