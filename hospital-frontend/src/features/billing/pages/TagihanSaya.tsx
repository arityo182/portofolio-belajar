import { useCallback, useEffect, useState } from "react";
import { AxiosError } from "axios";
import { Loader2, AlertTriangle, Receipt, Eye } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import { useAuth } from "../../../core/context/AuthContext";
import { getPasienByUserId } from "../../pasien/services/pasienService";
import api from "../../../core/api/client";

interface BillingItem { id:number;deskripsi:string;jumlah:number;hargaSatuan:number;subtotal:number }
interface Billing { id:number;namaPasien:string;totalHarga:number;status:string;tanggalTagihan:string;items:BillingItem[] }

export default function TagihanSaya() {
  const { user } = useAuth();
  const [list, setList] = useState<Billing[]>([]);
  const [loading, setLoading] = useState(true); const [error, setError] = useState<string|null>(null);
  const [sel, setSel] = useState<Billing|null>(null); const [dOpen, setDOpen] = useState(false);

  const muat = useCallback(async()=>{if(!user)return;setLoading(true);setError(null);try{const p=await getPasienByUserId(user.id);const r=await api.get<Billing[]>(`/billing/pasien/${p.data.id}`);setList(r.data);}catch(err){if(err instanceof AxiosError && err.response?.status===404)setError("Profil pasien belum lengkap.");else setError(toErrorMessage(err));}finally{setLoading(false);}},[user]);
  useEffect(()=>{muat();},[muat]);

  const bukaDetail = async(id:number)=>{try{const r=await api.get<Billing>(`/billing/${id}`);setSel(r.data);setDOpen(true);}catch(err){setError(toErrorMessage(err));}};

  if(loading) return <div className="flex flex-col items-center gap-3 py-20"><Loader2 className="animate-spin" size={28} color={C.navy}/><p className="text-sm" style={{color:C.grey}}>Memuat tagihan...</p></div>;

  return (
    <div>
      <section style={{backgroundColor:C.navy}} className="relative overflow-hidden"><div className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15" style={{backgroundColor:C.blue}}/><div className="relative mx-auto max-w-5xl px-5 py-14"><span className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold" style={{backgroundColor:"rgba(255,255,255,0.12)",color:C.blue}}><Receipt size={14}/>Tagihan Saya</span><h1 className="mt-5 text-4xl font-bold text-white">Tagihan & Pembayaran</h1><p className="mt-4 max-w-xl text-base" style={{color:"#C9D2EC"}}>Lihat tagihan dari kunjungan Anda dan lakukan pembayaran.</p></div></section>
      <section className="mx-auto max-w-5xl px-5 py-12">
        {error?<div className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16" style={{borderColor:C.line}}><AlertTriangle size={30} color={C.orange}/><p className="text-sm" style={{color:C.grey}}>{error}</p></div>
        :list.length===0?<div className="flex flex-col items-center gap-3 rounded-3xl border border-dashed px-6 py-16" style={{borderColor:C.line}}><Receipt size={30} color={C.grey}/><p className="text-sm" style={{color:C.grey}}>Belum ada tagihan.</p></div>
        :<div className="flex flex-col gap-4">{list.map(b=>(<div key={b.id} className="rounded-3xl border p-5 flex items-center justify-between" style={{backgroundColor:C.white,borderColor:C.line}}><div><p className="font-bold" style={{color:C.navy}}>Rp {b.totalHarga?.toLocaleString("id-ID")}</p><p className="text-sm" style={{color:C.grey}}>{b.tanggalTagihan} · {b.items?.length??0} item</p></div><div className="flex items-center gap-3"><span className="rounded-full px-3 py-1 text-xs font-semibold" style={{backgroundColor:b.status==="LUNAS"?"#E0F2E9":b.status==="DIBATALKAN"?"#FCE8E6":C.orangeSoft,color:b.status==="LUNAS"?"#1B8A5A":b.status==="DIBATALKAN"?"#C0392B":C.orange}}>{b.status}</span><button onClick={()=>bukaDetail(b.id)} className="flex h-8 w-8 items-center justify-center rounded-lg" style={{backgroundColor:C.cream}}><Eye size={15} color={C.navy}/></button></div></div>))}</div>}
      </section>

      {dOpen&&sel&&<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={()=>setDOpen(false)}><div className="w-full max-w-md max-h-[85vh] overflow-y-auto rounded-3xl border p-6" style={{backgroundColor:"white",borderColor:"#E7E7DE"}} onClick={e=>e.stopPropagation()}><h2 className="text-lg font-bold mb-2" style={{color:"#172E74"}}>Detail Tagihan</h2><p className="text-xl font-bold mb-1" style={{color:C.navy}}>Rp {sel.totalHarga?.toLocaleString("id-ID")}</p><span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{backgroundColor:sel.status==="LUNAS"?"#E0F2E9":sel.status==="DIBATALKAN"?"#FCE8E6":C.orangeSoft,color:sel.status==="LUNAS"?"#1B8A5A":sel.status==="DIBATALKAN"?"#C0392B":C.orange}}>{sel.status}</span><div className="mt-4 space-y-2">{sel.items?.map(i=><div key={i.id} className="flex justify-between rounded-2xl px-4 py-3" style={{backgroundColor:C.cream}}><div><p className="text-sm font-semibold" style={{color:C.navy}}>{i.deskripsi}</p><p className="text-xs" style={{color:C.grey}}>{i.jumlah} x Rp {i.hargaSatuan?.toLocaleString("id-ID")}</p></div><p className="text-sm font-bold" style={{color:C.navy}}>Rp {i.subtotal?.toLocaleString("id-ID")}</p></div>)}</div></div></div>}
    </div>
  );
}
