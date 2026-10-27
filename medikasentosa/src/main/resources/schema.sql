-- Auto-generated schema for Medika Sentosa
-- Tables created in FK dependency order

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    nama VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    token_version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS poli (
    id BIGSERIAL PRIMARY KEY,
    nama VARCHAR(255) NOT NULL,
    deskripsi TEXT,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS dokter (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    poli_id BIGINT REFERENCES poli(id),
    no_str VARCHAR(255) NOT NULL,
    spesialisasi VARCHAR(255) NOT NULL,
    no_hp VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS pasien (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    no_rekam_medis VARCHAR(255) UNIQUE,
    nik VARCHAR(255) NOT NULL,
    tanggal_lahir DATE NOT NULL,
    jenis_kelamin VARCHAR(1) NOT NULL,
    alamat TEXT,
    no_hp VARCHAR(255) NOT NULL,
    gol_darah VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS obat (
    id BIGSERIAL PRIMARY KEY,
    nama VARCHAR(255) NOT NULL UNIQUE,
    kategori VARCHAR(255) NOT NULL,
    satuan VARCHAR(255) NOT NULL,
    stok INTEGER NOT NULL,
    harga NUMERIC(12,2) NOT NULL,
    deskripsi TEXT,
    tanggal_produksi DATE,
    tanggal_kadaluarsa DATE,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS jadwal_dokter (
    id BIGSERIAL PRIMARY KEY,
    dokter_id BIGINT REFERENCES dokter(id),
    hari VARCHAR(10) NOT NULL,
    jam_mulai TIME NOT NULL,
    jam_selesai TIME NOT NULL,
    kuota INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS appointment (
    id BIGSERIAL PRIMARY KEY,
    pasien_id BIGINT REFERENCES pasien(id),
    dokter_id BIGINT REFERENCES dokter(id),
    jadwal_id BIGINT REFERENCES jadwal_dokter(id),
    tanggal DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    keluhan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS antrian (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT UNIQUE REFERENCES appointment(id),
    nomor_urut INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rekam_medis (
    id BIGSERIAL PRIMARY KEY,
    pasien_id BIGINT REFERENCES pasien(id),
    dokter_id BIGINT REFERENCES dokter(id),
    appointment_id BIGINT UNIQUE REFERENCES appointment(id),
    diagnosa TEXT,
    tindakan TEXT,
    catatan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resep (
    id BIGSERIAL PRIMARY KEY,
    rekam_medis_id BIGINT REFERENCES rekam_medis(id),
    obat_id BIGINT REFERENCES obat(id),
    nama_obat VARCHAR(255),
    dosis VARCHAR(255),
    jumlah INTEGER NOT NULL,
    aturan_pakai VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lab_order (
    id BIGSERIAL PRIMARY KEY,
    rekam_medis_id BIGINT REFERENCES rekam_medis(id),
    jenis_pemeriksaan VARCHAR(255) NOT NULL,
    catatan TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lab_hasil (
    id BIGSERIAL PRIMARY KEY,
    lab_order_id BIGINT UNIQUE REFERENCES lab_order(id),
    hasil_text TEXT NOT NULL,
    nilai_normal TEXT,
    interpretasi TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS radiologi (
    id BIGSERIAL PRIMARY KEY,
    rekam_medis_id BIGINT REFERENCES rekam_medis(id),
    jenis_radiologi VARCHAR(255) NOT NULL,
    hasil_deskripsi TEXT,
    catatan_dokter TEXT,
    gambar_url TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS screenings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    label VARCHAR(255) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    probabilities TEXT,
    risk TEXT,
    gradcam_image TEXT,
    latency_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS kamar (
    id BIGSERIAL PRIMARY KEY,
    nomor_kamar VARCHAR(255) NOT NULL UNIQUE,
    tipe_kamar VARCHAR(10) NOT NULL,
    kapasitas INTEGER NOT NULL,
    terisi INTEGER NOT NULL DEFAULT 0,
    harga_per_malam NUMERIC(12,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS rawat_inap (
    id BIGSERIAL PRIMARY KEY,
    pasien_id BIGINT REFERENCES pasien(id),
    dokter_id BIGINT REFERENCES dokter(id),
    kamar_id BIGINT REFERENCES kamar(id),
    tanggal_masuk DATE NOT NULL,
    tanggal_keluar DATE,
    diagnosa_awal TEXT,
    catatan TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS jadwal_operasi (
    id BIGSERIAL PRIMARY KEY,
    pasien_id BIGINT REFERENCES pasien(id),
    dokter_id BIGINT REFERENCES dokter(id),
    tanggal_operasi DATE NOT NULL,
    jam_mulai TIME NOT NULL,
    jenis_operasi VARCHAR(255) NOT NULL,
    ruang_operasi VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    catatan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS billing (
    id BIGSERIAL PRIMARY KEY,
    pasien_id BIGINT REFERENCES pasien(id),
    appointment_id BIGINT UNIQUE REFERENCES appointment(id),
    total_harga NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    tanggal_tagihan DATE NOT NULL,
    catatan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS billing_item (
    id BIGSERIAL PRIMARY KEY,
    billing_id BIGINT REFERENCES billing(id),
    deskripsi VARCHAR(255) NOT NULL,
    jumlah INTEGER NOT NULL,
    harga_satuan NUMERIC(12,2) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS pembayaran (
    id BIGSERIAL PRIMARY KEY,
    billing_id BIGINT UNIQUE REFERENCES billing(id),
    metode_pembayaran VARCHAR(20) NOT NULL,
    jumlah_bayar NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    bukti_transfer TEXT,
    tanggal_bayar TIMESTAMP NOT NULL,
    catatan TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS content (
    id BIGSERIAL PRIMARY KEY,
    page_key VARCHAR(255) NOT NULL,
    section_key VARCHAR(255) NOT NULL,
    value TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(page_key, section_key)
);

CREATE TABLE IF NOT EXISTS chat_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    judul VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES chat_session(id),
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP
);
