import { useCallback, useEffect, useState } from "react";
import { Plus, Loader2, AlertTriangle, Scissors } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import api from "../../../core/api/client";

interface Operasi { id:number; pasienId:number; namaPasien:string; dokterId:number; namaDokter:string; namaPoli?:string; tanggalOperasi:string; jamMulai:string; jenisOperasi:string; ruangOperasi:string; status:string; catatan:string|null }

export default function AdminOperasi() {
  const [list, setList] = useState<Operasi[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string|null>(null);

  // Filter
  const [poliFilter, setPoliFilter] = useState("");
  const [tglFilter, setTglFilter] = useState("");
  const poliNames = [...new Set(list.map(o => o.namaPoli).filter(Boolean))];

  // Modal
  const [mOpen, setMOpen] = useState(false); const [saving, setSaving] = useState(false);
  const [pasienId, setPasienId] = useState(""); const [dokterId, setDokterId] = useState("");
  const [tgl, setTgl] = useState(""); const [jam, setJam] = useState("08:00");
  const [jenis, setJenis] = useState(""); const [ruang, setRuang] = useState("");
  const [catatan, setCatatan] = useState(""); const [fErr, setFErr] = useState<string|null>(null);

  const muat = useCallback(async()=>{setLoading(true);setError(null);try{const r=await api.get<Operasi[]>("/operasi");setList(r.data);}catch(err){setError(toErrorMessage(err));}finally{setLoading(false);}},[]);
  useEffect(()=>{muat();},[muat]);

  const filtered = list.filter(o => {
    if (poliFilter && o.namaPoli !== poliFilter) return false;
    if (tglFilter && o.tanggalOperasi !== tglFilter) return false;
    return true;
  });

  const clusters: Record<string, Operasi[]> = {};
  filtered.forEach(o => {
    const key = `${o.namaPoli || "Tanpa Poli"} | ${o.tanggalOperasi}`;
    if (!clusters[key]) clusters[key] = [];
    clusters[key].push(o);
  });

  const handleCreate = async(e:React.FormEvent)=>{e.preventDefault();setSaving(true);setFErr(null);try{await api.post("/operasi",{pasienId:Number(pasienId),dokterId:Number(dokterId),tanggalOperasi:tgl,jamMulai:jam+":00",jenisOperasi:jenis,ruangOperasi:ruang,catatan:catatan.trim()||undefined});setMOpen(false);setPasienId("");setDokterId("");setTgl("");setJam("08:00");setJenis("");setRuang("");setCatatan("");await muat();}catch(err){setFErr(toErrorMessage(err));}finally{setSaving(false);}};

  const updateStatus = async(id:number, status:string)=>{try{await api.patch(`/operasi/${id}/status?status=${status}`);await muat();}catch(err){setError(toErrorMessage(err));}};

  if(loading) return <div className="flex flex-col items-center gap-3 py-20"><Loader2 className="animate-spin" size={28} color={C.navy}/><p className="text-sm" style={{color:C.grey}}>Memuat...</p></div>;

  return (
    <div>
      <div className="flex items-center justify-between"><div><h1 className="text-2xl font-bold" style={{color:C.navy}}>Jadwal Operasi</h1><p className="mt-1 text-sm" style={{color:C.grey}}>Penjadwalan & cluster per Poli + Tanggal</p></div><button onClick={()=>setMOpen(true)} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white" style={{backgroundColor:C.orange}}><Plus size={17}/>Jadwalkan</button></div>
      {error&&<div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={18} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{error}</p></div>}

      {/* Filter */}
      <div className="mt-4 flex flex-wrap gap-2 items-center">
        <select value={poliFilter} onChange={e=>setPoliFilter(e.target.value)} className="rounded-full px-3 py-1.5 text-xs font-semibold border bg-white" style={{borderColor:C.line,color:C.navy}}>
          <option value="">🏥 Semua Poli</option>
          {poliNames.map(p=><option key={p} value={p}>{p}</option>)}
        </select>
        <input type="date" value={tglFilter} onChange={e=>setTglFilter(e.target.value)} className="rounded-full px-3 py-1.5 text-xs font-semibold border bg-white" style={{borderColor:C.line,color:C.navy}}/>
        {(poliFilter||tglFilter)&&<button onClick={()=>{setPoliFilter("");setTglFilter("");}} className="text-xs font-semibold" style={{color:C.orange}}>✕ Reset</button>}
      </div>

      {/* Cluster view */}
      <div className="mt-4 space-y-4">
        {Object.keys(clusters).length===0 ? (
          <div className="rounded-3xl border border-dashed py-16 text-center" style={{borderColor:C.line}}><Scissors size={30} color={C.grey} className="mx-auto mb-2"/><p className="text-sm" style={{color:C.grey}}>Tidak ada jadwal operasi.</p></div>
        ) : Object.entries(clusters).map(([label, items]) => (
          <div key={label}>
            <div className="rounded-xl px-4 py-2 font-bold text-sm" style={{backgroundColor:C.navy,color:"white"}}>📋 {label} — {items.length} operasi</div>
            <div className="mt-2 overflow-x-auto rounded-3xl border" style={{backgroundColor:C.white,borderColor:C.line}}>
              <table className="w-full text-left text-sm"><thead><tr style={{backgroundColor:C.cream}}><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Pasien</th><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Dokter</th><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Jam</th><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Jenis</th><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Ruang</th><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Status</th><th className="px-4 py-2 text-right font-semibold" style={{color:C.navy}}>Aksi</th></tr></thead>
              <tbody>{items.map(o=>{const st=o.status;return <tr key={o.id} style={{borderBottom:`1px solid ${C.line}`}}><td className="px-4 py-2 font-semibold" style={{color:C.navy}}>{o.namaPasien}</td><td className="px-4 py-2" style={{color:C.grey}}>{o.namaDokter}</td><td className="px-4 py-2" style={{color:C.grey}}>{o.jamMulai}</td><td className="px-4 py-2" style={{color:C.grey}}>{o.jenisOperasi}</td><td className="px-4 py-2" style={{color:C.grey}}>{o.ruangOperasi}</td><td className="px-4 py-2"><span className="rounded-full px-2 py-0.5 text-xs font-semibold" style={{backgroundColor:st==="SELESAI"?"#E0F2E9":st==="BERLANGSUNG"?C.blueSoft:st==="BATAL"?"#FCE8E6":C.orangeSoft,color:st==="SELESAI"?"#1B8A5A":st==="BERLANGSUNG"?C.navy:st==="BATAL"?"#C0392B":C.orange}}>{st}</span></td><td className="px-4 py-2 text-right"><div className="flex justify-end gap-1">{st==="TERJADWAL"&&<button onClick={()=>updateStatus(o.id,"BERLANGSUNG")} className="rounded-full px-2 py-0.5 text-xs font-semibold text-white" style={{backgroundColor:C.blue}}>Mulai</button>}{st==="BERLANGSUNG"&&<button onClick={()=>updateStatus(o.id,"SELESAI")} className="rounded-full px-2 py-0.5 text-xs font-semibold text-white" style={{backgroundColor:"#1B8A5A"}}>Selesai</button>}{(st==="TERJADWAL"||st==="BERLANGSUNG")&&<button onClick={()=>updateStatus(o.id,"BATAL")} className="rounded-full px-2 py-0.5 text-xs font-semibold" style={{backgroundColor:"#FCE8E6",color:"#C0392B"}}>Batal</button>}</div></td></tr>})}</tbody>
            </table>
            </div>
          </div>
        ))}
      </div>

      {mOpen&&<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={()=>!saving&&setMOpen(false)}><div className="w-full max-w-md rounded-3xl border p-6" style={{backgroundColor:C.white,borderColor:C.line}} onClick={e=>e.stopPropagation()}><h2 className="text-lg font-bold mb-4" style={{color:C.navy}}>Jadwalkan Operasi</h2><form onSubmit={handleCreate} className="flex flex-col gap-4"><div className="grid grid-cols-2 gap-3"><input type="number" value={pasienId} onChange={e=>setPasienId(e.target.value)} placeholder="ID Pasien" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><input type="number" value={dokterId} onChange={e=>setDokterId(e.target.value)} placeholder="ID Dokter" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div><div className="grid grid-cols-2 gap-3"><input type="date" value={tgl} onChange={e=>setTgl(e.target.value)} className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><input type="time" value={jam} onChange={e=>setJam(e.target.value)} className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div><input type="text" value={jenis} onChange={e=>setJenis(e.target.value)} placeholder="Jenis Operasi" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><input type="text" value={ruang} onChange={e=>setRuang(e.target.value)} placeholder="Ruang (mis. OK-1)" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><textarea value={catatan} onChange={e=>setCatatan(e.target.value)} rows={2} placeholder="Catatan" className="rounded-2xl border px-4 py-2.5 text-sm outline-none resize-none" style={{borderColor:C.line}}/>{fErr&&<div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={16} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{fErr}</p></div>}<button type="submit" disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:"Simpan"}</button></form></div></div>}
    </div>
  );
}