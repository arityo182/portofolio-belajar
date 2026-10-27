import { useEffect, useRef } from "react";
import ChatMessageBubble from "./ChatMessage";
import TypingIndicator from "./TypingIndicator";
import ChatInput from "./ChatInput";
/** Area pesan utama — scroll otomatis ke bawah saat pesan baru. */
export default function ChatWindow({ messages, loading, onSend, sessionJudul }) {
    const bottomRef = useRef(null);
    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, loading]);
    return (<div className="flex flex-1 flex-col h-full">
      {/* Header */}
      <div className="shrink-0 border-b px-4 py-3" style={{ borderColor: "#E7E7DE" }}>
        <h2 className="text-lg font-bold" style={{ color: "#172E74" }}>
          {sessionJudul || "Chat dengan Senta"}
        </h2>
        <p className="text-xs" style={{ color: "#A0A0A0" }}>
          Asisten virtual RS Medika Sentosa
        </p>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto py-2" style={{ backgroundColor: "#FAFAFA" }}>
        {messages.length === 0 && !loading && (<div className="flex flex-col items-center justify-center h-full px-4 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-[#172E74] mb-4">
              <span className="text-2xl font-bold text-white">S</span>
            </div>
            <p className="text-lg font-semibold" style={{ color: "#172E74" }}>
              Halo! Saya Senta 👋
            </p>
            <p className="mt-1 text-sm" style={{ color: "#A0A0A0" }}>
              Tanyakan apa saja tentang layanan RS Medika Sentosa.
            </p>
          </div>)}

        {messages.map((msg) => (<ChatMessageBubble key={msg.id} message={msg}/>))}
        {loading && <TypingIndicator />}
        <div ref={bottomRef}/>
      </div>

      {/* Input */}
      <ChatInput onSend={onSend} disabled={loading}/>
    </div>);
}
