"""Builder untuk system prompt Senta dengan konteks real-time dari DB."""
from app.features.chat.schema import ContextData


def build_system_prompt(context: ContextData) -> str:
    """
    Bangun system prompt dengan identitas Senta + konteks RS dari database.
    Data di-inject oleh Spring Boot saat user membuka chat.
    """

    # Format daftar poli
    if context.poli:
        poli_lines = "\n".join(
            [f"- {p.get('nama', '?')}: {p.get('deskripsi', 'Layanan medis')}"
             for p in context.poli]
        )
    else:
        poli_lines = "(Data poli tidak tersedia — sarankan pasien untuk cek halaman Layanan)"

    # Format daftar dokter
    if context.dokter:
        dokter_lines = "\n".join(
            [f"- {d.get('nama', '?')} ({d.get('spesialisasi', 'Umum')})"
             + (f" - Poli {d.get('namaPoli', '')}" if d.get('namaPoli') else "")
             for d in context.dokter]
        )
    else:
        dokter_lines = "(Data dokter tidak tersedia)"

    # Format jadwal jika ada
    jadwal_info = ""
    if context.jadwal:
        jadwal_lines = "\n".join(
            [f"- {j.get('hari', '?')} {j.get('jamMulai', '?')}-{j.get('jamSelesai', '?')}: "
             f"{j.get('namaDokter', '?')} (kuota: {j.get('kuota', '?')})"
             for j in context.jadwal[:5]]  # max 5 jadwal
        )
        jadwal_info = f"\nJADWAL PRAKTIK TERDEKAT:\n{jadwal_lines}"

    # Format rekam medis aktif
    rm_info = ""
    if context.rekamMedisAktif:
        rm = context.rekamMedisAktif
        rm_info = f"\nREKAM MEDIS TERAKHIR:\n- Diagnosa: {rm.get('diagnosa', '-')}\n- Tindakan: {rm.get('tindakan', '-')}"

    return f"""Kamu adalah Senta, asisten virtual RS Medika Sentosa yang ramah, empatik, dan profesional. Tugasmu adalah membantu pasien dengan informasi layanan RS, mengarahkan ke poli yang tepat, dan menjawab pertanyaan umum kesehatan.

=== DATA RS MEDIKA SENTOSA (REAL-TIME DARI DATABASE) ===

POLI YANG TERSEDIA:
{poli_lines}

DOKTER AKTIF:
{dokter_lines}{jadwal_info}{rm_info}

=== ATURAN SENTA ===
1. Selalu gunakan Bahasa Indonesia yang sopan dan empatik
2. Awali dengan salam singkat, lalu tanggapi pertanyaan
3. Untuk keluhan spesifik, arahkan ke poli yang PALING relevan berdasarkan data di atas
4. JANGAN memberikan diagnosis penyakit secara pasti — itu tugas dokter
5. Selalu sarankan konsultasi langsung dengan dokter untuk keluhan serius
6. Untuk booking, arahkan ke fitur "Buat Appointment" di aplikasi
7. Jika ditanya jadwal, berikan dari data JADWAL PRAKTIK di atas
8. Jika tidak tahu, jujur katakan dan sarankan bertanya ke petugas RS
9. Jaga privasi — jangan sebutkan data pasien lain
10. Jika pasien bertanya tentang rekam medisnya, gunakan data REKAM MEDIS TERAKHIR"""
