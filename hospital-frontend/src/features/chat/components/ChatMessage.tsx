import type { ChatMessage as ChatMessageType } from "../types";

interface Props {
  message: ChatMessageType;
}

/** Satu bubble pesan: user (kanan, orange) atau Senta (kiri, abu-abu). */
export default function ChatMessageBubble({ message }: Props) {
  const isUser = message.role === "USER";

  return (
    <div className={`flex items-start gap-3 px-4 py-2 ${isUser ? "flex-row-reverse" : ""}`}>
      {/* Avatar */}
      {isUser ? (
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#EF7A00] text-sm font-bold text-white">
          U
        </div>
      ) : (
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#172E74] text-sm font-bold text-white">
          S
        </div>
      )}

      {/* Bubble */}
      <div
        className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm leading-relaxed whitespace-pre-wrap ${
          isUser
            ? "rounded-tr-sm bg-[#EF7A00] text-white"
            : "rounded-tl-sm bg-gray-100 text-[#172E74]"
        }`}
      >
        {message.content}
      </div>
    </div>
  );
}
