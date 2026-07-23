import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Loader2, AlertTriangle, Check, CreditCard } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import api from "../../../core/api/client";

export default function BayarTagihan() {
  const loc = useLocation(); const nav = useNavigate();
  const params = new URLSearchParams(loc.search);
  const billingId = params.get("id") || "";
  const total = params.get("total") || "0";

  const [metode, setMetode] = useState("TUNAI");
  const [jumlah, setJumlah] = useState(total);
  const [bukti, setBukti] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string|null>(null);
  const [sukses, setSukses] = useState(false);

  const handleBayar = async(e:React.FormEvent)=>{e.preventDefault();setSaving(true);setError(null);try{await api.post("/pembayaran/proses",{billingId:Number(billingId),metodePembayaran:metode,jumlahBayar:parseFloat(jumlah),buktiTransfer:metode==="TRANSFER"?bukti.trim()||undefined:undefined});setSukses(true);}catch(err){setError(toErrorMessage(err));}finally{setSaving(false);}};

  if(sukses) return <section className="mx-auto max-w-2xl px-5 py-16 text-center"><div className="rounded-[2rem] border p-10" style={{backgroundColor:C.white,borderColor:C.line}}><Check size={56} color="#1B8A5A"/><h1 className="mt-4 text-2xl font-bold" style={{color:C.navy}}>Pembayaran Berhasil</h1><p className="mt-2 text-sm" style={{color:C.grey}}>Tagihan Anda telah dilunasi.</p><button onClick={()=>nav("/tagihan-saya")} className="mt-6 rounded-full px-6 py-3 text-sm font-semibold text-white" style={{backgroundColor:C.orange}}>Kembali ke Tagihan</button></div></section>;

  return (
    <div>
      <section style={{backgroundColor:C.navy}} className="relative overflow-hidden"><div className="pointer-events-none absolute -right-20 -top-16 h-64 w-64 rounded-full opacity-15" style={{backgroundColor:C.blue}}/><div className="relative mx-auto max-w-4xl px-5 py-14"><span className="inline-flex items-center gap-2 rounded-full px-3.5 py-1.5 text-xs font-semibold" style={{backgroundColor:"rgba(255,255,255,0.12)",color:C.blue}}><CreditCard size={14}/>Bayar Tagihan</span><h1 className="mt-5 text-4xl font-bold text-white">Pembayaran Tagihan</h1><p className="mt-4 max-w-xl text-base" style={{color:"#C9D2EC"}}>Pilih metode pembayaran dan selesaikan tagihan Anda.</p></div></section>
      <section className="mx-auto max-w-md px-5 py-12">
        <div className="rounded-3xl border p-6" style={{backgroundColor:C.white,borderColor:C.line}}>
          <h2 className="text-lg font-bold mb-1" style={{color:C.navy}}>Detail Pembayaran</h2>
          <p className="text-2xl font-bold mb-4" style={{color:C.navy}}>Rp {parseInt(total).toLocaleString("id-ID")}</p>
          <form onSubmit={handleBayar} className="flex flex-col gap-4">
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Metode Pembayaran</label><select value={metode} onChange={e=>setMetode(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line,color:C.navy}}><option>TUNAI</option><option>TRANSFER</option><option>KARTU</option><option>BPJS</option></select></div>
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Jumlah Bayar</label><input type="number" value={jumlah} onChange={e=>setJumlah(e.target.value)} min={0} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line,color:C.navy}} required/></div>
            {metode==="TRANSFER"&&<div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>URL Bukti Transfer</label><input type="text" value={bukti} onChange={e=>setBukti(e.target.value)} placeholder="/uploads/bukti/trx-001.jpg" className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none" style={{borderColor:C.line}}/></div>}
            {error&&<div className="flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={16} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{error}</p></div>}
            <button type="submit" disabled={saving} className="rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:"Konfirmasi Pembayaran"}</button>
          </form>
        </div>
      </section>
    </div>
  );
}
