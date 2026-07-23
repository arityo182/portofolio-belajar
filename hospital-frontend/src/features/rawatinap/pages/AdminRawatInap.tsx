import { useCallback, useEffect, useState } from "react";
import { Plus, Loader2, AlertTriangle, LogIn, LogOut } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import api from "../../../core/api/client";

interface Kamar { id:number; nomorKamar:string; tipeKamar:string; kapasitas:number; terisi:number; hargaPerMalam:number; isActive:boolean }
interface RawatInap { id:number; pasienId:number; namaPasien:string; dokterId:number; namaDokter:string; kamarId:number; nomorKamar:string; hargaPerMalam:number; tanggalMasuk:string; tanggalKeluar:string|null; diagnosaAwal:string; catatan:string|null; status:string }

export default function AdminRawatInap() {
  const [kamar, setKamar] = useState<Kamar[]>([]);
  const [ri, setRi] = useState<RawatInap[]>([]);
  const [loading, setLoading] = useState(true); const [error, setError] = useState<string|null>(null);
  const [tab, setTab] = useState<"kamar"|"ri">("kamar");

  // Kamar form
  const [kOpen, setKOpen] = useState(false); const [saving, setSaving] = useState(false);
  const [nomor, setNomor] = useState(""); const [tipe, setTipe] = useState("REGULER");
  const [kap, setKap] = useState(1); const [harga, setHarga] = useState("");

  // Masuk form
  const [mOpen, setMOpen] = useState(false); const [fErr, setFErr] = useState<string|null>(null);
  const [pasienId, setPasienId] = useState(""); const [dokterId, setDokterId] = useState("");
  const [kamarId, setKamarId] = useState(""); const [tglMasuk, setTglMasuk] = useState("");
  const [diagnosa, setDiagnosa] = useState(""); const [catatanRI, setCatatanRI] = useState("");

  const muat = useCallback(async()=>{setLoading(true);setError(null);try{const[kRes,rRes]=await Promise.all([api.get<Kamar[]>("/kamar"),api.get<RawatInap[]>("/rawat-inap")]);setKamar(kRes.data);setRi(rRes.data);}catch(err){setError(toErrorMessage(err));}finally{setLoading(false);}},[]);
  useEffect(()=>{muat();},[muat]);

  const handleAddKamar = async(e:React.FormEvent)=>{e.preventDefault();setSaving(true);setFErr(null);try{await api.post("/kamar",{nomorKamar:nomor,tipeKamar:tipe,kapasitas:kap,hargaPerMalam:parseFloat(harga)});setKOpen(false);await muat();}catch(err){setFErr(toErrorMessage(err));}finally{setSaving(false);}};

  const handleMasuk = async(e:React.FormEvent)=>{e.preventDefault();setSaving(true);setFErr(null);try{await api.post("/rawat-inap/masuk",{pasienId:Number(pasienId),dokterId:Number(dokterId),kamarId:Number(kamarId),tanggalMasuk:tglMasuk,diagnosaAwal:diagnosa,catatan:catatanRI.trim()||undefined});setMOpen(false);await muat();}catch(err){setFErr(toErrorMessage(err));}finally{setSaving(false);}};

  const handleKeluar = async(id:number)=>{const tgl = prompt("Tanggal keluar (YYYY-MM-DD):", new Date().toISOString().slice(0,10)); if(!tgl)return; try{await api.patch(`/rawat-inap/${id}/keluar?tanggalKeluar=${tgl}`);await muat();}catch(err){setError(toErrorMessage(err));}};

  if(loading) return <div className="flex flex-col items-center gap-3 py-20"><Loader2 className="animate-spin" size={28} color={C.navy}/><p className="text-sm" style={{color:C.grey}}>Memuat...</p></div>;

  return (
    <div>
      <div className="flex items-center justify-between"><div><h1 className="text-2xl font-bold" style={{color:C.navy}}>Rawat Inap</h1><p className="mt-1 text-sm" style={{color:C.grey}}>Manajemen kamar & rawat inap</p></div><div className="flex gap-2">{tab==="kamar"?<button onClick={()=>setKOpen(true)} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white" style={{backgroundColor:C.orange}}><Plus size={17}/>Tambah Kamar</button>:<button onClick={()=>setMOpen(true)} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white" style={{backgroundColor:C.orange}}><LogIn size={17}/>Pasien Masuk</button>}</div></div>
      {error&&<div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={18} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{error}</p></div>}
      
      <div className="mt-4 flex gap-2">{[
        {key:"kamar",label:"Kamar"},{key:"ri",label:"Rawat Inap"}
      ].map(t=><button key={t.key} onClick={()=>setTab(t.key as"kamar"|"ri")} className="rounded-full px-5 py-2 text-sm font-semibold" style={{backgroundColor:tab===t.key?C.navy:C.cream,color:tab===t.key?C.white:C.grey}}>{t.label}</button>)}</div>

      {tab==="kamar"?<div className="mt-4 overflow-x-auto rounded-3xl border" style={{backgroundColor:C.white,borderColor:C.line}}><table className="w-full text-left text-sm"><thead><tr style={{backgroundColor:C.cream}}><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Nomor</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Tipe</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Terisi/Kapasitas</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Harga/Malam</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Status</th></tr></thead><tbody>{kamar.map(k=><tr key={k.id} style={{borderBottom:`1px solid ${C.line}`}}><td className="px-4 py-3 font-semibold" style={{color:C.navy}}>{k.nomorKamar}</td><td className="px-4 py-3" style={{color:C.grey}}>{k.tipeKamar}</td><td className="px-4 py-3"><span className="font-semibold" style={{color:k.terisi>=k.kapasitas?"#C0392B":C.navy}}>{k.terisi}</span><span style={{color:C.grey}}>/{k.kapasitas}</span></td><td className="px-4 py-3" style={{color:C.grey}}>Rp {k.hargaPerMalam?.toLocaleString("id-ID")}</td><td className="px-4 py-3"><span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{backgroundColor:k.isActive?C.blueSoft:C.line,color:k.isActive?C.navy:C.grey}}>{k.isActive?"Aktif":"Nonaktif"}</span></td></tr>)}</tbody></table></div>
      :<div className="mt-4 overflow-x-auto rounded-3xl border" style={{backgroundColor:C.white,borderColor:C.line}}><table className="w-full text-left text-sm"><thead><tr style={{backgroundColor:C.cream}}><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Pasien</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Kamar</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Masuk</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Status</th><th className="px-4 py-3 text-right font-semibold" style={{color:C.navy}}>Aksi</th></tr></thead><tbody>{ri.length===0?<tr><td colSpan={5} className="px-4 py-10 text-center" style={{color:C.grey}}>Belum ada rawat inap.</td></tr>:ri.map(r=><tr key={r.id} style={{borderBottom:`1px solid ${C.line}`}}><td className="px-4 py-3 font-semibold" style={{color:C.navy}}>{r.namaPasien}</td><td className="px-4 py-3" style={{color:C.grey}}>{r.nomorKamar} ({r.tanggalMasuk})</td><td className="px-4 py-3" style={{color:C.grey}}>{r.tanggalMasuk}</td><td className="px-4 py-3"><span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{backgroundColor:r.status==="AKTIF"?C.blueSoft:r.status==="SELESAI"?"#E0F2E9":C.orangeSoft,color:r.status==="AKTIF"?C.navy:r.status==="SELESAI"?"#1B8A5A":C.orange}}>{r.status}</span></td><td className="px-4 py-3 text-right">{r.status==="AKTIF"&&<button onClick={()=>handleKeluar(r.id)} className="inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold" style={{backgroundColor:"#FCE8E6",color:"#C0392B"}}><LogOut size={13}/>Keluar</button>}</td></tr>)}</tbody></table></div>}

      {kOpen&&<Modal title="Tambah Kamar" onClose={()=>!saving&&setKOpen(false)}><form onSubmit={handleAddKamar} className="flex flex-col gap-4"><div><input type="text" value={nomor} onChange={e=>setNomor(e.target.value)} placeholder="Nomor Kamar" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div><div><select value={tipe} onChange={e=>setTipe(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}}><option>REGULER</option><option>VIP</option><option>VVIP</option><option>ICU</option></select></div><div className="grid grid-cols-2 gap-3"><input type="number" value={kap} onChange={e=>setKap(Number(e.target.value))} min={1} placeholder="Kapasitas" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><input type="number" value={harga} onChange={e=>setHarga(e.target.value)} placeholder="Harga/malam" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div>{fErr&&<ErrorMsg msg={fErr}/>}<button type="submit" disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:"Simpan"}</button></form></Modal>}

      {mOpen&&<Modal title="Pasien Masuk Rawat Inap" onClose={()=>!saving&&setMOpen(false)}><form onSubmit={handleMasuk} className="flex flex-col gap-4"><div className="grid grid-cols-2 gap-3"><input type="number" value={pasienId} onChange={e=>setPasienId(e.target.value)} placeholder="ID Pasien" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><input type="number" value={dokterId} onChange={e=>setDokterId(e.target.value)} placeholder="ID Dokter" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div><div className="grid grid-cols-2 gap-3"><input type="number" value={kamarId} onChange={e=>setKamarId(e.target.value)} placeholder="ID Kamar" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><input type="date" value={tglMasuk} onChange={e=>setTglMasuk(e.target.value)} className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/></div><input type="text" value={diagnosa} onChange={e=>setDiagnosa(e.target.value)} placeholder="Diagnosa Awal" className="rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}} required/><textarea value={catatanRI} onChange={e=>setCatatanRI(e.target.value)} rows={2} placeholder="Catatan" className="rounded-2xl border px-4 py-2.5 text-sm outline-none resize-none" style={{borderColor:C.line}}/>{fErr&&<ErrorMsg msg={fErr}/>}<button type="submit" disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:"Simpan"}</button></form></Modal>}
    </div>
  );
}

function Modal({title,children,onClose}:{title:string;children:React.ReactNode;onClose:()=>void}){return <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onClose}><div className="w-full max-w-md rounded-3xl border p-6" style={{backgroundColor:"white",borderColor:"#E7E7DE"}} onClick={e=>e.stopPropagation()}><h2 className="text-lg font-bold mb-4" style={{color:"#172E74"}}>{title}</h2>{children}</div></div>}
function ErrorMsg({msg}:{msg:string}){return <div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={16} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{msg}</p></div>}
