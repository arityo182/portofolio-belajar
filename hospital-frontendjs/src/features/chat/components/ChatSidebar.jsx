import { Plus, Trash2, MessageSquare } from "lucide-react";
import { useState } from "react";
/** Sidebar kiri — daftar sesi chat + tombol Chat Baru. */
export default function ChatSidebar({ sessions, activeSessionId, onNewChat, onSelectSession, onDeleteSession, mobileOpen, onCloseMobile, }) {
    const [confirmDelete, setConfirmDelete] = useState(null);
    const handleDelete = (id, e) => {
        e.stopPropagation();
        if (confirmDelete === id) {
            onDeleteSession(id);
            setConfirmDelete(null);
        }
        else {
            setConfirmDelete(id);
            setTimeout(() => setConfirmDelete(null), 3000);
        }
    };
    return (<div className={`flex flex-col h-full border-r bg-[#172E74] text-white
        ${mobileOpen ? "fixed inset-y-0 left-0 z-50 w-72" : "hidden md:flex md:w-64"}`} style={{ borderColor: "rgba(255,255,255,0.1)" }}>
      {/* Header */}
      <div className="shrink-0 border-b px-4 py-3 flex items-center justify-between" style={{ borderColor: "rgba(255,255,255,0.1)" }}>
        <div>
          <h1 className="text-lg font-bold">Senta AI</h1>
          <p className="text-xs opacity-60">RS Medika Sentosa</p>
        </div>
        {onCloseMobile && (<button onClick={onCloseMobile} className="md:hidden text-white text-xl">&times;</button>)}
      </div>

      {/* New Chat button */}
      <div className="px-3 py-3">
        <button onClick={onNewChat} className="flex w-full items-center gap-2 rounded-xl border border-white/20 px-4 py-2.5 text-sm font-semibold transition hover:bg-white/10">
          <Plus size={16}/> Chat Baru
        </button>
      </div>

      {/* Session list */}
      <div className="flex-1 overflow-y-auto px-3 pb-3">
        {sessions.length === 0 && (<p className="px-2 text-xs opacity-50 text-center mt-8">
            Belum ada percakapan. Mulai chat baru!
          </p>)}
        {sessions.map((s) => (<button key={s.id} onClick={() => onSelectSession(s.id)} className={`group flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-left text-sm transition mb-0.5 ${activeSessionId === s.id ? "bg-white/15 font-semibold" : "hover:bg-white/5"}`}>
            <MessageSquare size={14} className="shrink-0 opacity-60"/>
            <span className="flex-1 truncate">{s.judul || "Percakapan baru"}</span>
            <button onClick={(e) => handleDelete(s.id, e)} className="shrink-0 opacity-0 group-hover:opacity-100 transition" title="Hapus sesi">
              <Trash2 size={14} className={confirmDelete === s.id ? "text-red-400" : "text-white/40 hover:text-red-300"}/>
            </button>
          </button>))}
      </div>
    </div>);
}
