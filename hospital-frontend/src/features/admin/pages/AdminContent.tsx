import { useEffect, useState } from "react";
import { Loader2, AlertTriangle, Check, Save } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import api from "../../../core/api/client";

const PAGES = ["beranda", "layanan", "dokter", "tentang", "kontak"];

const PAGE_LABELS: Record<string,string> = {beranda:"Beranda",layanan:"Layanan",dokter:"Dokter",tentang:"Tentang Kami",kontak:"Kontak"};

const SECTIONS: Record<string,string[]> = {
  beranda: ["hero_badge","hero_title","hero_title_highlight","hero_subtitle","layanan_title","tim_medis_title"],
  layanan: ["hero_title","hero_subtitle"],
  dokter: ["hero_title","hero_subtitle"],
  tentang: ["hero_title","hero_subtitle","visi","misi"],
  kontak: ["hero_title","hero_subtitle","alamat","telepon","email","jam_operasional"],
};

export default function AdminContent() {
  const [page, setPage] = useState("beranda");
  const [fields, setFields] = useState<Record<string,string>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string|null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    let aktif = true;
    setLoading(true); setError(null); setSuccess(false);
    api.get<Record<string,string>>(`/content/${page}`)
      .then(r => aktif && setFields(r.data))
      .catch(err => aktif && setError(toErrorMessage(err)))
      .finally(() => aktif && setLoading(false));
    return () => { aktif = false; };
  }, [page]);

  const handleSave = async () => {
    setSaving(true); setError(null); setSuccess(false);
    try {
      await api.put(`/content/${page}`, fields);
      setSuccess(true);
    } catch(err) { setError(toErrorMessage(err)); }
    finally { setSaving(false); }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold" style={{color:C.navy}}>Manajemen Konten</h1>
      <p className="mt-1 text-sm" style={{color:C.grey}}>Edit konten setiap halaman publik</p>

      {/* Page tabs */}
      <div className="mt-4 flex flex-wrap gap-2">
        {PAGES.map(p => (
          <button key={p} onClick={()=>setPage(p)} className="rounded-full px-5 py-2 text-sm font-semibold"
            style={{backgroundColor:page===p?C.navy:C.cream,color:page===p?C.white:C.grey}}>
            {PAGE_LABELS[p]}
          </button>
        ))}
      </div>

      {error && <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={18} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{error}</p></div>}
      {success && <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#E0F2E9"}}><Check size={18} color="#1B8A5A"/><p className="text-sm font-medium" style={{color:"#1B8A5A"}}>Konten berhasil disimpan.</p></div>}

      {loading ? (
        <div className="flex items-center gap-3 py-16 justify-center"><Loader2 className="animate-spin" size={24} color={C.navy}/><span style={{color:C.grey}}>Memuat...</span></div>
      ) : (
        <div className="mt-4 rounded-3xl border p-6" style={{backgroundColor:C.white,borderColor:C.line}}>
          <h2 className="text-lg font-bold mb-4" style={{color:C.navy}}>Halaman: {PAGE_LABELS[page]}</h2>
          <div className="flex flex-col gap-4">
            {SECTIONS[page].map(key => (
              <div key={key}>
                <label className="mb-1 block text-sm font-semibold capitalize" style={{color:C.navy}}>{key.replace(/_/g," ")}</label>
                {key.includes("title") || key.includes("badge") ? (
                  <input type="text" value={fields[key]||""} onChange={e=>setFields({...fields,[key]:e.target.value})}
                    className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}}/>
                ) : (
                  <textarea value={fields[key]||""} onChange={e=>setFields({...fields,[key]:e.target.value})} rows={3}
                    className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}}/>
                )}
              </div>
            ))}
          </div>
          <button onClick={handleSave} disabled={saving} className="mt-6 inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>
            {saving?<Loader2 className="animate-spin" size={17}/>:<Save size={17}/>}
            {saving?"Menyimpan...":"Simpan"}
          </button>
        </div>
      )}
    </div>
  );
}
