import { useState, useRef, useEffect, type KeyboardEvent } from "react";
import { Send } from "lucide-react";

interface Props {
  onSend: (message: string) => void;
  disabled?: boolean;
}

/** Input teks + tombol kirim untuk chat. */
export default function ChatInput({ onSend, disabled }: Props) {
  const [text, setText] = useState("");
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    inputRef.current?.focus();
  }, [disabled]); // re-focus setelah selesai loading

  const handleSend = () => {
    const trimmed = text.trim();
    if (!trimmed || disabled) return;
    onSend(trimmed);
    setText("");
  };

  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="flex items-end gap-2 border-t bg-white px-4 py-3" style={{ borderColor: "#E7E7DE" }}>
      <textarea
        ref={inputRef}
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Tanyakan sesuatu ke Senta..."
        disabled={disabled}
        rows={1}
        className="flex-1 resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00] disabled:opacity-50"
        style={{ borderColor: "#E7E7DE", color: "#172E74", maxHeight: "120px" }}
        onInput={(e) => {
          const el = e.currentTarget;
          el.style.height = "auto";
          el.style.height = Math.min(el.scrollHeight, 120) + "px";
        }}
      />
      <button
        onClick={handleSend}
        disabled={disabled || !text.trim()}
        className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#EF7A00] text-white transition hover:bg-[#d46900] disabled:opacity-40"
      >
        <Send size={18} />
      </button>
    </div>
  );
}
