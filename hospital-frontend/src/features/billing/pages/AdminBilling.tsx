import { useCallback, useEffect, useState } from "react";
import { Plus, Loader2, AlertTriangle, Eye, Receipt } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import api from "../../../core/api/client";

interface BillingItem { id:number;billingId:number;deskripsi:string;jumlah:number;hargaSatuan:number;subtotal:number }
interface Billing { id:number;pasienId:number;namaPasien:string;totalHarga:number;status:string;tanggalTagihan:string;items:BillingItem[] }

export default function AdminBilling() {
  const [list, setList] = useState<Billing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string|null>(null);

  // Filter
  const [tglFilter, setTglFilter] = useState("");
  const [poFilter, setPoFilter] = useState(""); // pasien name filter (proxy for poli)

  // Modal generate
  const [gOpen, setGOpen] = useState(false); const [apptId, setApptId] = useState(""); const [saving, setSaving] = useState(false);
  const [dOpen, setDOpen] = useState(false); const [sel, setSel] = useState<Billing|null>(null);
  const [iOpen, setIOpen] = useState(false); const [dsk, setDsk] = useState(""); const [jml, setJml] = useState(1); const [hrg, setHrg] = useState(""); const [fErr, setFErr] = useState<string|null>(null);

  const muat = useCallback(async()=>{setLoading(true);setError(null);try{const r=await api.get<Billing[]>("/billing");setList(r.data);}catch(err){setError(toErrorMessage(err));}finally{setLoading(false);}},[]);
  useEffect(()=>{muat();},[muat]);

  const filtered = list.filter(b => {
    if (tglFilter && b.tanggalTagihan !== tglFilter) return false;
    if (poFilter && !b.namaPasien.toLowerCase().includes(poFilter.toLowerCase())) return false;
    return true;
  });

  // Cluster by tanggalTagihan
  const clusters: Record<string, Billing[]> = {};
  filtered.forEach(b => {
    const key = b.tanggalTagihan;
    if (!clusters[key]) clusters[key] = [];
    clusters[key].push(b);
  });

  const handleGenerate = async(e:React.FormEvent)=>{e.preventDefault();setSaving(true);setFErr(null);try{await api.post(`/billing/generate/appointment/${apptId}`);setGOpen(false);setApptId("");await muat();}catch(err){setFErr(toErrorMessage(err));}finally{setSaving(false);}};

  const handleAddItem = async(e:React.FormEvent)=>{e.preventDefault();if(!sel)return;setSaving(true);setFErr(null);try{await api.post(`/billing/${sel.id}/item`,{deskripsi:dsk,jumlah:jml,hargaSatuan:parseFloat(hrg)});setIOpen(false);setDsk("");setJml(1);setHrg("");const r=await api.get<Billing>(`/billing/${sel.id}`);setSel(r.data);}catch(err){setFErr(toErrorMessage(err));}finally{setSaving(false);}};

  const bukaDetail = async(id:number)=>{try{const r=await api.get<Billing>(`/billing/${id}`);setSel(r.data);setDOpen(true);}catch(err){setError(toErrorMessage(err));}};

  if(loading) return <div className="flex flex-col items-center gap-3 py-20"><Loader2 className="animate-spin" size={28} color={C.navy}/><p className="text-sm" style={{color:C.grey}}>Memuat...</p></div>;

  return (
    <div>
      <div className="flex items-center justify-between"><div><h1 className="text-2xl font-bold" style={{color:C.navy}}>Billing & Tagihan</h1><p className="mt-1 text-sm" style={{color:C.grey}}>Cluster per Tanggal</p></div><button onClick={()=>setGOpen(true)} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white" style={{backgroundColor:C.orange}}><Plus size={17}/>Generate</button></div>
      {error&&<div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={18} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{error}</p></div>}

      {/* Filter */}
      <div className="mt-4 flex flex-wrap gap-2 items-center">
        <input type="date" value={tglFilter} onChange={e=>setTglFilter(e.target.value)} className="rounded-full px-3 py-1.5 text-xs font-semibold border bg-white" style={{borderColor:C.line,color:C.navy}}/>
        <input type="text" value={poFilter} onChange={e=>setPoFilter(e.target.value)} placeholder="🔍 Cari nama pasien..." className="rounded-full px-3 py-1.5 text-xs font-semibold border bg-white" style={{borderColor:C.line,color:C.navy}}/>
        {(tglFilter||poFilter)&&<button onClick={()=>{setTglFilter("");setPoFilter("");}} className="text-xs font-semibold" style={{color:C.orange}}>✕ Reset</button>}
      </div>

      {/* Cluster view */}
      <div className="mt-4 space-y-4">
        {Object.keys(clusters).length===0 ? (
          <div className="rounded-3xl border border-dashed py-16 text-center" style={{borderColor:C.line}}><Receipt size={30} color={C.grey} className="mx-auto mb-2"/><p className="text-sm" style={{color:C.grey}}>Tidak ada billing.</p></div>
        ) : Object.entries(clusters).map(([tgl, items]) => (
          <div key={tgl}>
            <div className="rounded-xl px-4 py-2 font-bold text-sm" style={{backgroundColor:C.navy,color:"white"}}>📅 {tgl} — {items.length} tagihan</div>
            <div className="mt-2 overflow-x-auto rounded-3xl border" style={{backgroundColor:C.white,borderColor:C.line}}>
              <table className="w-full text-left text-sm"><thead><tr style={{backgroundColor:C.cream}}><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Pasien</th><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Total</th><th className="px-4 py-2 font-semibold" style={{color:C.navy}}>Status</th><th className="px-4 py-2 text-right font-semibold" style={{color:C.navy}}>Aksi</th></tr></thead>
              <tbody>{items.map(b=>(<tr key={b.id} style={{borderBottom:`1px solid ${C.line}`}}><td className="px-4 py-3 font-semibold" style={{color:C.navy}}>{b.namaPasien}</td><td className="px-4 py-3" style={{color:C.navy}}>Rp {b.totalHarga?.toLocaleString("id-ID")}</td><td className="px-4 py-3"><span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{backgroundColor:b.status==="LUNAS"?"#E0F2E9":b.status==="DIBATALKAN"?"#FCE8E6":C.orangeSoft,color:b.status==="LUNAS"?"#1B8A5A":b.status==="DIBATALKAN"?"#C0392B":C.orange}}>{b.status}</span></td><td className="px-4 py-3 text-right"><button onClick={()=>bukaDetail(b.id)} className="inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold" style={{backgroundColor:C.cream,color:C.navy}}><Eye size={13}/>Detail</button></td></tr>))}</tbody>
            </table>
            </div>
          </div>
        ))}
        </div>

      {/* Modal Generate */}
      {gOpen&&<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={()=>!saving&&setGOpen(false)}><div className="w-full max-w-md rounded-3xl border p-6" style={{backgroundColor:C.white,borderColor:C.line}} onClick={e=>e.stopPropagation()}><h2 className="text-lg font-bold mb-4" style={{color:C.navy}}>Generate Billing</h2><form onSubmit={handleGenerate} className="flex flex-col gap-4"><div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>ID Appointment *</label><input type="number" value={apptId} onChange={e=>setApptId(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required autoFocus/></div>{fErr&&<div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={16} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{fErr}</p></div>}<button type="submit" disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:"Generate"}</button></form></div></div>}

      {/* Modal Detail */}
      {dOpen&&sel&&<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={()=>setDOpen(false)}><div className="w-full max-w-md max-h-[85vh] overflow-y-auto rounded-3xl border p-6" style={{backgroundColor:C.white,borderColor:C.line}} onClick={e=>e.stopPropagation()}><h2 className="text-lg font-bold mb-1" style={{color:C.navy}}>Detail Tagihan</h2><p className="text-xl font-bold mb-2" style={{color:C.navy}}>Rp {sel.totalHarga?.toLocaleString("id-ID")}</p><span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{backgroundColor:sel.status==="LUNAS"?"#E0F2E9":sel.status==="DIBATALKAN"?"#FCE8E6":C.orangeSoft,color:sel.status==="LUNAS"?"#1B8A5A":sel.status==="DIBATALKAN"?"#C0392B":C.orange}}>{sel.status}</span><div className="mt-4 space-y-2">{sel.items?.map(i=><div key={i.id} className="flex justify-between rounded-2xl px-4 py-3" style={{backgroundColor:C.cream}}><div><p className="text-sm font-semibold" style={{color:C.navy}}>{i.deskripsi}</p><p className="text-xs" style={{color:C.grey}}>{i.jumlah} x Rp {i.hargaSatuan?.toLocaleString("id-ID")}</p></div><p className="text-sm font-bold" style={{color:C.navy}}>Rp {i.subtotal?.toLocaleString("id-ID")}</p></div>)}</div>{sel.status==="BELUM_BAYAR"&&<button onClick={()=>{setIOpen(true);setDOpen(false);}} className="mt-4 rounded-full px-5 py-2.5 text-sm font-semibold text-white" style={{backgroundColor:C.orange}}><Plus size={15}/>Tambah Item</button>}</div></div>}

      {/* Modal Tambah Item */}
      {iOpen&&sel&&<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={()=>!saving&&setIOpen(false)}><div className="w-full max-w-md rounded-3xl border p-6" style={{backgroundColor:C.white,borderColor:C.line}} onClick={e=>e.stopPropagation()}><h2 className="text-lg font-bold mb-4" style={{color:C.navy}}>Tambah Item</h2><form onSubmit={handleAddItem} className="flex flex-col gap-4"><input type="text" value={dsk} onChange={e=>setDsk(e.target.value)} placeholder="Deskripsi" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><div className="grid grid-cols-2 gap-3"><input type="number" value={jml} onChange={e=>setJml(Number(e.target.value))} min={1} placeholder="Jumlah" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><input type="number" value={hrg} onChange={e=>setHrg(e.target.value)} min={0} placeholder="Harga" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div>{fErr&&<div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={16} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{fErr}</p></div>}<button type="submit" disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:"Tambah"}</button></form></div></div>}
    </div>
  );
}