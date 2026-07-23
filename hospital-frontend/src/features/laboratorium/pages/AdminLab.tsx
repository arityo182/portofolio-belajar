import { useCallback, useEffect, useState } from "react";
import { Plus, Loader2, AlertTriangle, FileText, Search } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import api from "../../../core/api/client";

interface LabOrder { id:number; rekamMedisId:number; namaPasien:string; noRekamMedis?:string; nik?:string; jenisPemeriksaan:string; catatan:string|null; status:string; createdAt:string }
interface LabHasil { id:number; labOrderId:number; jenisPemeriksaan:string; namaPasien:string; hasilText:string; nilaiNormal:string|null; interpretasi:string|null }

export default function AdminLab() {
  const [orders, setOrders] = useState<LabOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string|null>(null);
  const [sok, setSok] = useState("");

  // Modal create
  const [mOpen, setMOpen] = useState(false); const [saving, setSaving] = useState(false);
  const [rmId, setRmId] = useState(""); const [jenis, setJenis] = useState(""); const [catatan, setCatatan] = useState("");
  const [fErr, setFErr] = useState<string|null>(null);

  // Modal hasil
  const [hOpen, setHOpen] = useState(false); const [sel, setSel] = useState<LabOrder|null>(null);
  const [hText, setHText] = useState(""); const [nNormal, setNNormal] = useState(""); const [interp, setInterp] = useState("");
  const [hLoading, setHLoading] = useState(false); const [existing, setExisting] = useState<LabHasil|null>(null);

  const muat = useCallback(async()=>{
    setLoading(true); setError(null);
    try { const r=await api.get<LabOrder[]>("/lab/order"); setOrders(r.data); }
    catch(err){ setError(toErrorMessage(err)); }
    finally{ setLoading(false); }
  },[]);
  useEffect(()=>{muat();},[muat]);

  const filtered = orders.filter(o =>
    !sok || o.namaPasien.toLowerCase().includes(sok.toLowerCase()) ||
    (o.noRekamMedis||"").toLowerCase().includes(sok.toLowerCase()) ||
    (o.nik||"").toLowerCase().includes(sok.toLowerCase())
  );

  const handleCreate = async(e:React.FormEvent)=>{
    e.preventDefault(); setSaving(true); setFErr(null);
    try { await api.post("/lab/order",{rekamMedisId:Number(rmId),jenisPemeriksaan:jenis,catatan:catatan.trim()||undefined});
      setMOpen(false); setRmId("");setJenis("");setCatatan(""); await muat(); }
    catch(err){ setFErr(toErrorMessage(err)); } finally{ setSaving(false); }
  };

  const bukaHasil = async(o:LabOrder)=>{
    setSel(o); setHText("");setNNormal("");setInterp("");setExisting(null);setFErr(null);setHOpen(true);
    try { const r=await api.get<LabHasil>(`/lab/hasil/order/${o.id}`); setExisting(r.data); setHText(r.data.hasilText);setNNormal(r.data.nilaiNormal??"");setInterp(r.data.interpretasi??""); }
    catch{ /* belum ada hasil */ }
  };

  const handleSaveHasil = async(e:React.FormEvent)=>{
    e.preventDefault(); if(!sel) return; setHLoading(true); setFErr(null);
    try { await api.post("/lab/hasil",{labOrderId:sel.id,hasilText:hText,nilaiNormal:nNormal.trim()||undefined,interpretasi:interp.trim()||undefined});
      setHOpen(false); await muat(); }
    catch(err){ setFErr(toErrorMessage(err)); } finally{ setHLoading(false); }
  };

  if(loading) return <div className="flex flex-col items-center gap-3 py-20"><Loader2 className="animate-spin" size={28} color={C.navy}/><p className="text-sm" style={{color:C.grey}}>Memuat...</p></div>;

  return (
    <div>
      <div className="flex items-center justify-between">
        <div><h1 className="text-2xl font-bold" style={{color:C.navy}}>Laboratorium</h1><p className="mt-1 text-sm" style={{color:C.grey}}>Order & hasil pemeriksaan lab</p></div>
        <button onClick={()=>setMOpen(true)} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white" style={{backgroundColor:C.orange}}><Plus size={17}/>Buat Order</button>
      </div>

      {error && <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={18} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{error}</p></div>}

      {/* Search */}
      <div className="mt-4 relative max-w-sm">
        <Search size={18} color={C.grey} className="absolute left-3 top-1/2 -translate-y-1/2" />
        <input type="text" value={sok} onChange={e=>setSok(e.target.value)} placeholder="Cari nama, No.RM, atau NIK..." className="w-full rounded-2xl border pl-10 pr-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}}/>
      </div>

      <div className="mt-4 overflow-x-auto rounded-3xl border" style={{backgroundColor:C.white,borderColor:C.line}}>
        <table className="w-full text-left text-sm">
          <thead><tr style={{backgroundColor:C.cream,borderBottom:`1px solid ${C.line}`}}>
            <th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Pasien</th>
            <th className="px-4 py-3 font-semibold" style={{color:C.navy}}>No. RM</th>
            <th className="px-4 py-3 font-semibold" style={{color:C.navy}}>NIK</th>
            <th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Pemeriksaan</th>
            <th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Status</th>
            <th className="px-4 py-3 text-right font-semibold" style={{color:C.navy}}>Aksi</th>
          </tr></thead>
          <tbody>
            {filtered.length===0 ? (
              <tr><td colSpan={6} className="px-4 py-10 text-center" style={{color:C.grey}}>Tidak ada data.</td></tr>
            ) : filtered.map(o=>(
              <tr key={o.id} style={{borderBottom:`1px solid ${C.line}`}}>
                <td className="px-4 py-3 font-semibold" style={{color:C.navy}}>{o.namaPasien}</td>
                <td className="px-4 py-3" style={{color:C.grey}}>{o.noRekamMedis||"-"}</td>
                <td className="px-4 py-3" style={{color:C.grey}}>{o.nik||"-"}</td>
                <td className="px-4 py-3" style={{color:C.grey}}>{o.jenisPemeriksaan}</td>
                <td className="px-4 py-3"><span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{backgroundColor:o.status==="SELESAI"?"#E0F2E9":o.status==="PROSES"?C.blueSoft:C.orangeSoft,color:o.status==="SELESAI"?"#1B8A5A":o.status==="PROSES"?C.navy:C.orange}}>{o.status}</span></td>
                <td className="px-4 py-3 text-right"><button onClick={()=>bukaHasil(o)} className="inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold" style={{backgroundColor:C.cream,color:C.navy}}><FileText size={13}/>{o.status==="SELESAI"?"Lihat":"Isi"}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal Create */}
      {mOpen && <Modal title="Buat Order Lab" onClose={()=>!saving&&setMOpen(false)}>
        <form onSubmit={handleCreate} className="flex flex-col gap-4">
          <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>ID Rekam Medis *</label><input type="number" value={rmId} onChange={e=>setRmId(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required autoFocus/></div>
          <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Jenis Pemeriksaan *</label><input type="text" value={jenis} onChange={e=>setJenis(e.target.value)} placeholder="mis. Darah Lengkap" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div>
          <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Catatan</label><textarea value={catatan} onChange={e=>setCatatan(e.target.value)} rows={2} className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}}/></div>
          {fErr && <ErrBox msg={fErr} />}
          <button type="submit" disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:"Simpan"}</button>
        </form>
      </Modal>}

      {/* Modal Hasil */}
      {hOpen && sel && <Modal title={`Hasil: ${sel.jenisPemeriksaan}`} onClose={()=>!hLoading&&setHOpen(false)}>
        <p className="text-sm mb-4" style={{color:C.grey}}>Pasien: <b style={{color:C.navy}}>{sel.namaPasien}</b> · RM: {sel.noRekamMedis||"-"} · NIK: {sel.nik||"-"}</p>
        {existing ? (
          <div className="space-y-3">
            <div><span className="text-xs font-semibold" style={{color:C.grey}}>Hasil</span><pre className="mt-1 whitespace-pre-wrap rounded-2xl p-4 text-sm" style={{backgroundColor:C.cream,color:C.navy}}>{existing.hasilText}</pre></div>
            {existing.nilaiNormal && <div><span className="text-xs font-semibold" style={{color:C.grey}}>Normal</span><p className="mt-1 text-sm" style={{color:C.navy}}>{existing.nilaiNormal}</p></div>}
            {existing.interpretasi && <div><span className="text-xs font-semibold" style={{color:C.grey}}>Interpretasi</span><p className="mt-1 text-sm" style={{color:C.navy}}>{existing.interpretasi}</p></div>}
          </div>
        ) : (
          <form onSubmit={handleSaveHasil} className="flex flex-col gap-4">
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Hasil *</label><textarea value={hText} onChange={e=>setHText(e.target.value)} rows={4} className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div>
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Nilai Normal</label><input value={nNormal} onChange={e=>setNNormal(e.target.value)} placeholder="mis. 12-16 g/dL" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}}/></div>
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Interpretasi</label><textarea value={interp} onChange={e=>setInterp(e.target.value)} rows={2} className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}}/></div>
            {fErr && <ErrBox msg={fErr} />}
            <button type="submit" disabled={hLoading} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{hLoading?<Loader2 className="animate-spin" size={17}/>:"Simpan Hasil"}</button>
          </form>
        )}
      </Modal>}
    </div>
  );
}
function Modal({title,children,onClose}:{title:string;children:React.ReactNode;onClose:()=>void}){return <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onClose}><div className="w-full max-w-md max-h-[85vh] overflow-y-auto rounded-3xl border p-6" style={{backgroundColor:"white",borderColor:"#E7E7DE"}} onClick={e=>e.stopPropagation()}><h2 className="text-lg font-bold mb-4" style={{color:"#172E74"}}>{title}</h2>{children}</div></div>}
function ErrBox({msg}:{msg:string}){return <div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={16} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{msg}</p></div>}