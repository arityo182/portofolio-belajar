/** Layout utama chat: sidebar kiri + chat window kanan. */
import { useState, useEffect, useCallback } from "react";
import { Menu } from "lucide-react";
import ChatSidebar from "../components/ChatSidebar";
import ChatWindow from "../components/ChatWindow";
import * as chatService from "../services/chatService";
import type { ChatMessage, ChatSession } from "../types";

export default function ChatPage() {
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Load daftar sesi saat mount
  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    try {
      const data = await chatService.getSessions();
      setSessions(data);
    } catch {
      // ignore — sidebar kosong
    }
  };

  // Load history saat klik sesi
  const handleSelectSession = useCallback(async (id: number) => {
    setActiveSessionId(id);
    setSidebarOpen(false);
    try {
      const history = await chatService.getHistory(id);
      setMessages(history.messages);
    } catch {
      setMessages([]);
    }
  }, []);

  // Kirim pesan
  const handleSend = useCallback(async (text: string) => {
    setLoading(true);

    // Optimistic: tambahin bubble USER dulu
    const tempUser: ChatMessage = {
      id: Date.now(),
      sessionId: activeSessionId || 0,
      sessionJudul: "",
      role: "USER",
      content: text,
      createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, tempUser]);

    try {
      const response = await chatService.sendMessage({
        message: text,
        sessionId: activeSessionId,
      });

      // Update session ID jika baru dibuat
      if (!activeSessionId) {
        setActiveSessionId(response.sessionId);
      }

      // Tambahin respons ASSISTANT
      setMessages((prev) => [...prev, response]);

      // Refresh sidebar
      loadSessions();
    } catch {
      // Tambahin pesan error
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          sessionId: activeSessionId || 0,
          sessionJudul: "",
          role: "ASSISTANT",
          content: "Maaf, Senta sedang tidak tersedia. Silakan coba lagi nanti.",
          createdAt: new Date().toISOString(),
        },
      ]);
    } finally {
      setLoading(false);
    }
  }, [activeSessionId]);

  // Chat baru
  const handleNewChat = () => {
    setActiveSessionId(null);
    setMessages([]);
    setSidebarOpen(false);
  };

  // Hapus sesi
  const handleDeleteSession = async (id: number) => {
    try {
      await chatService.deleteSession(id);
      if (activeSessionId === id) {
        setActiveSessionId(null);
        setMessages([]);
      }
      setSessions((prev) => prev.filter((s) => s.id !== id));
    } catch {
      // ignore
    }
  };

  const activeSession = sessions.find((s) => s.id === activeSessionId);

  return (
    <div className="flex h-[calc(100vh-64px)]" style={{ fontFamily: "Poppins, sans-serif" }}>
      {/* Mobile hamburger */}
      <button
        onClick={() => setSidebarOpen(true)}
        className="fixed bottom-4 left-4 z-40 flex h-12 w-12 items-center justify-center rounded-full bg-[#EF7A00] text-white shadow-lg md:hidden"
      >
        <Menu size={22} />
      </button>

      {/* Sidebar */}
      <ChatSidebar
        sessions={sessions}
        activeSessionId={activeSessionId}
        onNewChat={handleNewChat}
        onSelectSession={handleSelectSession}
        onDeleteSession={handleDeleteSession}
        mobileOpen={sidebarOpen}
        onCloseMobile={() => setSidebarOpen(false)}
      />

      {/* Chat window */}
      <ChatWindow
        messages={messages}
        loading={loading}
        onSend={handleSend}
        sessionJudul={activeSession?.judul}
      />
    </div>
  );
}
