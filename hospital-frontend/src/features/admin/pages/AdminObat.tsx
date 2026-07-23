import { useCallback, useEffect, useState } from "react";
import { Plus, Pencil, Trash2, X, Loader2, AlertTriangle, Check } from "lucide-react";
import { C } from "../../../core/theme";
import { toErrorMessage } from "../../../core/api/apiError";
import api from "../../../core/api/client";

interface Obat {
  id: number; nama: string; kategori: string; satuan: string;
  stok: number; harga: number; deskripsi: string | null; isActive: boolean;
  tanggalProduksi?: string; tanggalKadaluarsa?: string;
}

export default function AdminObat() {
  const [obatList, setObatList] = useState<Obat[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Obat | null>(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const [nama, setNama] = useState(""); const [kategori, setKategori] = useState("");
  const [satuan, setSatuan] = useState("tablet"); const [stok, setStok] = useState(0);
  const [harga, setHarga] = useState(""); const [deskripsi, setDeskripsi] = useState("");
  const [tglProduksi, setTglProduksi] = useState(""); const [tglKadaluarsa, setTglKadaluarsa] = useState("");

  const muat = useCallback(async () => {
    setLoading(true); setError(null);
    try { const r = await api.get<Obat[]>("/obat"); setObatList(r.data); }
    catch (err) { setError(toErrorMessage(err)); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { muat(); }, [muat]);

  const resetForm = () => { setNama(""); setKategori(""); setSatuan("tablet"); setStok(0); setHarga(""); setDeskripsi(""); setTglProduksi(""); setTglKadaluarsa(""); setFormError(null); };
  const bukaTambah = () => { setEditing(null); resetForm(); setModalOpen(true); };
  const bukaEdit = (o: Obat) => { setEditing(o); setNama(o.nama); setKategori(o.kategori); setSatuan(o.satuan); setStok(o.stok); setHarga(String(o.harga)); setDeskripsi(o.deskripsi ?? ""); setTglProduksi(o.tanggalProduksi??""); setTglKadaluarsa(o.tanggalKadaluarsa??""); setFormError(null); setModalOpen(true); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = { nama, kategori, satuan, stok, harga: parseFloat(harga), deskripsi: deskripsi.trim() || undefined, tanggalProduksi: tglProduksi||undefined, tanggalKadaluarsa: tglKadaluarsa||undefined };
    setSaving(true); setFormError(null);
    try {
      if (editing) await api.put(`/obat/${editing.id}`, payload);
      else await api.post("/obat", payload);
      setModalOpen(false); await muat();
    } catch (err) { setFormError(toErrorMessage(err)); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Nonaktifkan obat ini?")) return;
    setDeletingId(id);
    try { await api.delete(`/obat/${id}`); await muat(); }
    catch (err) { setError(toErrorMessage(err)); }
    finally { setDeletingId(null); }
  };

  if (loading) return <div className="flex flex-col items-center gap-3 py-20"><Loader2 className="animate-spin" size={28} color={C.navy}/><p className="text-sm" style={{color:C.grey}}>Memuat...</p></div>;

  return (
    <div>
      <div className="flex items-center justify-between">
        <div><h1 className="text-2xl font-bold" style={{color:C.navy}}>Manajemen Obat</h1><p className="mt-1 text-sm" style={{color:C.grey}}>Kelola data master obat (FR-37)</p></div>
        <button onClick={bukaTambah} className="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold text-white transition-transform hover:-translate-y-0.5" style={{backgroundColor:C.orange}}><Plus size={17}/>Tambah Obat</button>
      </div>
      {error && <div className="mt-4 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={18} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{error}</p></div>}
      <div className="mt-6 overflow-x-auto rounded-3xl border" style={{backgroundColor:C.white,borderColor:C.line}}>
        <table className="w-full text-left text-sm">
          <thead><tr style={{backgroundColor:C.cream,borderBottom:`1px solid ${C.line}`}}><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Nama</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Kategori</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Satuan</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Stok</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Harga</th><th className="px-4 py-3 font-semibold" style={{color:C.navy}}>Status</th><th className="px-4 py-3 text-right font-semibold" style={{color:C.navy}}>Aksi</th></tr></thead>
          <tbody>{obatList.length===0 ? <tr><td colSpan={7} className="px-4 py-10 text-center" style={{color:C.grey}}>Belum ada obat.</td></tr> : obatList.map(o=>(
            <tr key={o.id} style={{borderBottom:`1px solid ${C.line}`}}>
              <td className="px-4 py-3 font-semibold" style={{color:C.navy}}>{o.nama}</td>
              <td className="px-4 py-3" style={{color:C.grey}}>{o.kategori}</td>
              <td className="px-4 py-3" style={{color:C.grey}}>{o.satuan}</td>
              <td className="px-4 py-3" style={{color:C.grey}}>{o.stok}</td>
              <td className="px-4 py-3" style={{color:C.grey}}>Rp {o.harga?.toLocaleString("id-ID")}</td>
              <td className="px-4 py-3"><span className="rounded-full px-2.5 py-1 text-xs font-semibold" style={{backgroundColor:o.isActive?C.blueSoft:C.line,color:o.isActive?C.navy:C.grey}}>{o.isActive?"Aktif":"Nonaktif"}</span></td>
              <td className="px-4 py-3"><div className="flex items-center justify-end gap-2">
                <button onClick={()=>bukaEdit(o)} className="flex h-8 w-8 items-center justify-center rounded-lg" style={{backgroundColor:C.cream}}><Pencil size={15} color={C.navy}/></button>
                {o.isActive && <button onClick={()=>handleDelete(o.id)} disabled={deletingId===o.id} className="flex h-8 w-8 items-center justify-center rounded-lg disabled:opacity-50" style={{backgroundColor:"#FCE8E6"}}>{deletingId===o.id?<Loader2 className="animate-spin" size={15} color="#C0392B"/>:<Trash2 size={15} color="#C0392B"/>}</button>}
              </div></td>
            </tr>))}</tbody>
        </table>
      </div>
      {modalOpen && <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={()=>!saving&&setModalOpen(false)}>
        <div className="w-full max-w-md max-h-[90vh] overflow-y-auto rounded-3xl border p-6" style={{backgroundColor:C.white,borderColor:C.line}} onClick={e=>e.stopPropagation()}>
          <div className="flex items-center justify-between"><h2 className="text-lg font-bold" style={{color:C.navy}}>{editing?"Edit Obat":"Tambah Obat"}</h2><button onClick={()=>!saving&&setModalOpen(false)} className="flex h-8 w-8 items-center justify-center rounded-full" style={{backgroundColor:C.cream}}><X size={18} color={C.navy}/></button></div>
          <form onSubmit={handleSubmit} className="mt-4 grid gap-4 sm:grid-cols-2">
            <div className="sm:col-span-2"><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Nama Obat <span style={{color:C.orange}}>*</span></label><input type="text" value={nama} onChange={e=>setNama(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}} required autoFocus/></div>
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Kategori <span style={{color:C.orange}}>*</span></label><input type="text" value={kategori} onChange={e=>setKategori(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}} required/></div>
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Satuan <span style={{color:C.orange}}>*</span></label><select value={satuan} onChange={e=>setSatuan(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}}><option>tablet</option><option>kapsul</option><option>ml</option><option>botol</option></select></div>
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Stok <span style={{color:C.orange}}>*</span></label><input type="number" value={stok} onChange={e=>setStok(Number(e.target.value))} min={0} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}} required/></div>
            <div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Harga <span style={{color:C.orange}}>*</span></label><input type="number" value={harga} onChange={e=>setHarga(e.target.value)} min={0} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}} required/></div>
            <div className="sm:col-span-2"><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Deskripsi</label><textarea value={deskripsi} onChange={e=>setDeskripsi(e.target.value)} rows={2} className="w-full resize-none rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}}/></div>
            <div className="grid grid-cols-2 gap-3"><div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Tgl Produksi</label><input type="date" value={tglProduksi} onChange={e=>setTglProduksi(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}}/></div><div><label className="mb-1 block text-sm font-semibold" style={{color:C.navy}}>Tgl Kadaluarsa</label><input type="date" value={tglKadaluarsa} onChange={e=>setTglKadaluarsa(e.target.value)} className="w-full rounded-2xl border px-4 py-2.5 text-sm outline-none focus:border-[#EF7A00]" style={{borderColor:C.line,color:C.navy}}/></div></div>
            {formError && <div className="sm:col-span-2 flex items-start gap-2 rounded-2xl px-4 py-3" style={{backgroundColor:"#FCE8E6"}}><AlertTriangle size={16} color="#C0392B"/><p className="text-sm font-medium" style={{color:"#C0392B"}}>{formError}</p></div>}
            <div className="sm:col-span-2 flex items-center gap-3"><button type="submit" disabled={saving} className="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white disabled:opacity-50" style={{backgroundColor:C.orange}}>{saving?<Loader2 className="animate-spin" size={17}/>:<Check size={17}/>}{saving?"Menyimpan...":"Simpan"}</button><button type="button" onClick={()=>!saving&&setModalOpen(false)} className="rounded-full px-6 py-3 text-sm font-semibold" style={{backgroundColor:C.cream,color:C.navy}}>Batal</button></div>
          </form>
        </div>
      </div>}
    </div>
  );
}
