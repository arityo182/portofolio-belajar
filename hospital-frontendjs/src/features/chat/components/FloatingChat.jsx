import { useState, useRef, useEffect, useCallback } from "react";
import { useLocation } from "react-router-dom";
import { MessageCircle, X, Send, Sparkles } from "lucide-react";
import { C } from "../../../core/theme";
import { sendPublicMessage } from "../services/publicChatService";
import FloatingChatBubble from "./FloatingChatBubble";
/** Pertanyaan yang disarankan saat panel pertama dibuka. */
const SUGGESTIONS = [
    "Poli apa saja yang tersedia?",
    "Cara daftar sebagai pasien baru?",
    "Jadwal dokter hari ini?",
    "Prosedur booking appointment?",
];
/** Timing animasi typing indicator (ms). */
/**
 * Floating chat widget "Tanya Senta".
 *
 * Muncul sebagai tombol bulat di kanan bawah semua halaman publik.
 * Saat diklik, membuka panel chat kecil dengan Senta AI.
 *
 * Di halaman `/chat` (chat penuh), widget disembunyikan.
 *
 * @returns Komponen floating chat widget
 */
export default function FloatingChat() {
    const location = useLocation();
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [showBadge, setShowBadge] = useState(true);
    const [error, setError] = useState(null);
    const messagesEndRef = useRef(null);
    const inputRef = useRef(null);
    // Sembunyikan widget di halaman /chat (chat penuh)
    const isChatPage = location.pathname === "/chat";
    // Auto-scroll ke bawah
    const scrollToBottom = useCallback(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, []);
    useEffect(() => {
        if (isOpen)
            scrollToBottom();
    }, [messages, isLoading, isOpen, scrollToBottom]);
    // Fokus input saat panel dibuka
    useEffect(() => {
        if (isOpen) {
            inputRef.current?.focus();
        }
    }, [isOpen]);
    /** Kirim pesan user ke Senta. */
    const handleSend = async (text) => {
        const content = (text ?? inputValue).trim();
        if (!content || isLoading)
            return;
        const userMsg = {
            id: `user-${messages.length}`,
            role: "user",
            content,
            timestamp: new Date(),
        };
        const updated = [...messages, userMsg];
        setMessages(updated);
        setInputValue("");
        setError(null);
        setIsLoading(true);
        try {
            const response = await sendPublicMessage(content, updated);
            const assistantMsg = {
                id: `assistant-${messages.length + 1}`,
                role: "assistant",
                content: response,
                timestamp: new Date(),
            };
            setMessages((prev) => [...prev, assistantMsg]);
        }
        catch {
            setError("Senta sedang tidak tersedia. Coba lagi nanti.");
        }
        finally {
            setIsLoading(false);
        }
    };
    /** Kirim saat Enter ditekan. */
    const handleKeyDown = (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };
    // Jangan render di halaman chat penuh
    if (isChatPage)
        return null;
    return (<>
      {/* ─── Floating Button ───────────────── */}
      <button onClick={() => { setIsOpen((prev) => !prev); setShowBadge(false); }} className="fixed bottom-6 right-6 z-50 flex h-14 w-14 items-center justify-center rounded-full shadow-lg transition-all hover:scale-105 hover:shadow-xl" style={{ backgroundColor: C.navy }} aria-label="Tanya Senta">
        {isOpen ? (<X size={22} color={C.white}/>) : (<MessageCircle size={24} color={C.white}/>)}
        {/* Badge notifikasi */}
        {showBadge && !isOpen && (<span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
            1
          </span>)}
      </button>

      {/* ─── Chat Panel ─────────────────────── */}
      {isOpen && (<div className="fixed bottom-20 right-6 z-50 flex flex-col overflow-hidden border shadow-2xl animate-in slide-in-from-bottom-4 fade-in" style={{
                width: "min(380px, 90vw)",
                height: "520px",
                borderRadius: "16px",
                backgroundColor: C.white,
                borderColor: C.line,
            }}>
          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3.5" style={{ backgroundColor: C.navy }}>
            <div className="flex items-center gap-2.5">
              <Sparkles size={18} color={C.white}/>
              <div>
                <p className="text-sm font-bold text-white">Tanya Senta</p>
                <p className="text-[10px] text-white/70">
                  Asisten Virtual RS Medika Sentosa
                </p>
              </div>
            </div>
            <button onClick={() => setIsOpen(false)} className="flex h-7 w-7 items-center justify-center rounded-full transition-colors hover:bg-white/10">
              <X size={16} color={C.white}/>
            </button>
          </div>

          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto bg-white px-4 py-3">
            {messages.length === 0 ? (
            /* Welcome screen */
            <div className="flex flex-col items-center pt-8">
                <div className="mb-3 flex h-14 w-14 items-center justify-center rounded-full text-lg font-bold text-white" style={{ backgroundColor: C.navy }}>
                  S
                </div>
                <p className="text-sm font-semibold" style={{ color: C.navy }}>
                  Halo! Saya Senta 👋
                </p>
                <p className="mt-1 text-center text-xs" style={{ color: C.grey }}>
                  Asisten virtual RS Medika Sentosa.
                  <br />
                  Ada yang bisa saya bantu?
                </p>

                {/* Suggested questions */}
                <div className="mt-4 flex w-full flex-wrap gap-2">
                  {SUGGESTIONS.map((q) => (<button key={q} onClick={() => handleSend(q)} disabled={isLoading} className="rounded-full border px-3 py-1.5 text-xs font-medium transition-colors hover:bg-opacity-5" style={{
                        borderColor: C.navy,
                        color: C.navy,
                    }}>
                      {q} 💬
                    </button>))}
                </div>
              </div>) : (
            /* Chat messages */
            <>
                {messages.map((m) => (<FloatingChatBubble key={m.id} message={m}/>))}
              </>)}

            {/* Typing Indicator */}
            {isLoading && (<div className="mb-3 flex items-center gap-2">
                <div className="flex h-5 w-5 items-center justify-center rounded-full bg-navy text-[10px] font-bold text-white" style={{ backgroundColor: C.navy }}>S</div>
                <div className="flex items-center gap-1 rounded-2xl bg-[#F3F4F6] px-3.5 py-2.5">
                  {[0, 1, 2].map((i) => (<span key={i} className="inline-block h-1.5 w-1.5 rounded-full animate-bounce" style={{
                        backgroundColor: C.grey,
                        animationDelay: `${i * 150}ms`,
                    }}/>))}
                </div>
              </div>)}

            {/* Error message */}
            {error && (<p className="text-center text-xs text-red-500 mb-2">{error}</p>)}

            <div ref={messagesEndRef}/>
          </div>

          {/* Input Area */}
          <div className="border-t px-4 py-3" style={{ borderColor: C.line }}>
            <div className="flex items-center gap-2">
              <input ref={inputRef} value={inputValue} onChange={(e) => setInputValue(e.target.value)} onKeyDown={handleKeyDown} placeholder="Tanya sesuatu..." disabled={isLoading} className="flex-1 bg-transparent text-sm outline-none placeholder:text-gray-400" style={{ fontFamily: "'Poppins', sans-serif", color: C.navy }}/>
              <button onClick={() => handleSend()} disabled={!inputValue.trim() || isLoading} className="flex h-8 w-8 items-center justify-center rounded-full transition-opacity disabled:opacity-30" style={{ backgroundColor: C.navy }}>
                <Send size={14} color={C.white}/>
              </button>
            </div>
            <p className="mt-2 text-center text-[10px]" style={{ color: "#9CA3AF" }}>
              Senta adalah AI, periksa informasi penting dengan petugas RS.
            </p>
          </div>
        </div>)}
    </>);
}
