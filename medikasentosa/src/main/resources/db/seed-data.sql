-- ================================================
-- RS MEDIKA SENTOSA — SEED DATA REALISTIS
-- Jalankan: psql -h localhost -U core -d medika_sentosa -f seed-data.sql
-- ================================================

BEGIN;

-- Bersihkan data lama (urutan sesuai FK dependencies)
TRUNCATE pembayaran, billing_item, billing, radiologi, lab_hasil, lab_order,
         resep, rekam_medis, antrian, appointment, jadwal_operasi, rawat_inap,
         jadwal_dokter, dokter, pasien, obat, kamar, screenings, content,
         poli, users
RESTART IDENTITY CASCADE;

-- Reset sequences
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE poli_id_seq RESTART WITH 1;
ALTER SEQUENCE dokter_id_seq RESTART WITH 1;
ALTER SEQUENCE pasien_id_seq RESTART WITH 1;
ALTER SEQUENCE jadwal_dokter_id_seq RESTART WITH 1;
ALTER SEQUENCE appointment_id_seq RESTART WITH 1;
ALTER SEQUENCE antrian_id_seq RESTART WITH 1;
ALTER SEQUENCE rekam_medis_id_seq RESTART WITH 1;
ALTER SEQUENCE resep_id_seq RESTART WITH 1;
ALTER SEQUENCE obat_id_seq RESTART WITH 1;
ALTER SEQUENCE kamar_id_seq RESTART WITH 1;
ALTER SEQUENCE lab_order_id_seq RESTART WITH 1;
ALTER SEQUENCE lab_hasil_id_seq RESTART WITH 1;
ALTER SEQUENCE radiologi_id_seq RESTART WITH 1;
ALTER SEQUENCE billing_id_seq RESTART WITH 1;
ALTER SEQUENCE billing_item_id_seq RESTART WITH 1;
ALTER SEQUENCE pembayaran_id_seq RESTART WITH 1;

-- ================================================
-- 1. USERS (20 user)
-- Password: arie12345 → BCrypt hash
-- ================================================
INSERT INTO users (id, nama, email, password, role, is_active, created_at) VALUES
(1,  'Admin Medika',       'admin@medikasentosa.id',    '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'ADMIN',  true, '2026-01-01 08:00:00'),
(2,  'dr. Ahmad Fauzi',    'ahmad.fauzi@medika.id',     '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'DOKTER', true, '2026-01-01 08:00:00'),
(3,  'dr. Sari Wijaya',    'sari.wijaya@medika.id',     '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'DOKTER', true, '2026-02-01 08:00:00'),
(4,  'dr. Budi Hartono',   'budi.hartono@medika.id',    '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'DOKTER', true, '2026-02-15 08:00:00'),
(5,  'dr. Dewi Lestari',   'dewi.lestari@medika.id',    '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'DOKTER', true, '2026-03-01 08:00:00'),
(6,  'dr. Rudi Hermawan',  'rudi.hermawan@medika.id',   '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'DOKTER', true, '2026-03-15 08:00:00'),
(7,  'dr. Maya Putri',     'maya.putri@medika.id',      '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'DOKTER', true, '2026-04-01 08:00:00'),
(8,  'dr. Hendra Gunawan', 'hendra.gunawan@medika.id',  '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'DOKTER', true, '2026-04-15 08:00:00'),
(9,  'Budi Santoso',       'budi.santoso@gmail.com',    '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-01 08:00:00'),
(10, 'Ani Rahmawati',      'ani.rahmawati@gmail.com',   '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-02 08:00:00'),
(11, 'Herman Salim',       'herman.salim@gmail.com',    '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-03 08:00:00'),
(12, 'Siti Nurhaliza',     'siti.nurhaliza@gmail.com',  '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-04 08:00:00'),
(13, 'Rizal Pratama',      'rizal.pratama@gmail.com',   '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-05 08:00:00'),
(14, 'Linda Kusuma',       'linda.kusuma@gmail.com',    '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-06 08:00:00'),
(15, 'Agus Supriyadi',     'agus.supriyadi@gmail.com',  '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-07 08:00:00'),
(16, 'Tiara Anggraini',    'tiara.anggraini@gmail.com', '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-08 08:00:00'),
(17, 'Dedi Irawan',        'dedi.irawan@gmail.com',     '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-09 08:00:00'),
(18, 'Rina Marlina',       'rina.marlina@gmail.com',    '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-10 08:00:00'),
(19, 'Fajar Nugroho',      'fajar.nugroho@gmail.com',   '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-11 08:00:00'),
(20, 'Nurul Hidayah',      'nurul.hidayah@gmail.com',   '$2b$10$Z8iltxi7kcloNG9qezAojOmptgyguD32AF72.gaupFz1tMjcqM6J6', 'PASIEN', true, '2026-05-12 08:00:00');

-- ================================================
-- 2. POLI (5 poli)
-- ================================================
INSERT INTO poli (id, nama, deskripsi, is_active) VALUES
(1, 'Poli Umum',               'Pelayanan kesehatan umum dan pemeriksaan dasar',                               true),
(2, 'Poli Jantung',            'Diagnosis dan pengobatan penyakit kardiovaskular',                               true),
(3, 'Poli Tulang & Ortopedi',  'Penanganan gangguan tulang, sendi, dan otot termasuk fisioterapi',              true),
(4, 'Poli Penyakit Dalam',     'Diagnosis lanjutan penyakit organ dalam dan metabolik',                          true),
(5, 'Poli Anak',               'Pelayanan kesehatan anak dan tumbuh kembang (0-18 tahun)',                       true);

-- ================================================
-- 3. DOKTER (7 dokter)
-- ================================================
INSERT INTO dokter (id, user_id, poli_id, no_str, spesialisasi, no_hp, is_active) VALUES
(1, 2, 1, 'STR.2020.001', 'Dokter Umum',              '0812-1000-001', true),
(2, 3, 1, 'STR.2019.002', 'Dokter Umum',              '0812-1000-002', true),
(3, 4, 2, 'STR.2018.003', 'Spesialis Jantung',        '0812-1000-003', true),
(4, 5, 3, 'STR.2017.004', 'Spesialis Ortopedi',       '0812-1000-004', true),
(5, 6, 4, 'STR.2019.005', 'Spesialis Penyakit Dalam', '0812-1000-005', true),
(6, 7, 5, 'STR.2018.006', 'Spesialis Anak',           '0812-1000-006', true),
(7, 8, 3, 'STR.2020.007', 'Spesialis Ortopedi',       '0812-1000-007', true);

-- ================================================
-- 4. JADWAL_DOKTER (14 jadwal)
-- ================================================
INSERT INTO jadwal_dokter (id, dokter_id, hari, jam_mulai, jam_selesai, kuota, is_active) VALUES
(1,  1, 'SENIN',   '08:00', '12:00', 15, true),
(2,  1, 'RABU',    '13:00', '17:00', 15, true),
(3,  2, 'SELASA',  '08:00', '12:00', 12, true),
(4,  2, 'KAMIS',   '13:00', '17:00', 12, true),
(5,  3, 'SENIN',   '09:00', '13:00', 10, true),
(6,  3, 'JUMAT',   '14:00', '18:00', 10, true),
(7,  4, 'RABU',    '08:00', '12:00', 10, true),
(8,  4, 'SABTU',   '09:00', '13:00', 10, true),
(9,  5, 'SELASA',  '09:00', '14:00', 10, true),
(10, 5, 'KAMIS',   '08:00', '12:00', 10, true),
(11, 6, 'SENIN',   '08:00', '12:00', 10, true),
(12, 6, 'RABU',    '13:00', '17:00', 10, true),
(13, 7, 'KAMIS',   '09:00', '13:00', 10, true),
(14, 7, 'JUMAT',   '08:00', '12:00', 10, true);

-- ================================================
-- 5. PASIEN (12 pasien)
-- ================================================
INSERT INTO pasien (id, user_id, no_rekam_medis, nik, tanggal_lahir, jenis_kelamin, alamat, no_hp, gol_darah) VALUES
(1,  9,  'RM-2026-000001', '3174010101800001', '1980-01-01', 'L', 'Jl. Melati No.12, Jakarta Pusat',  '0813-2000-001', 'A'),
(2,  10, 'RM-2026-000002', '3175021503850002', '1985-03-15', 'P', 'Jl. Anggrek No.5, Jakarta Selatan','0813-2000-002', 'B'),
(3,  11, 'RM-2026-000003', '3276032007900003', '1990-07-20', 'L', 'Jl. Mawar No.8, Bandung',          '0813-2000-003', 'O'),
(4,  12, 'RM-2026-000004', '3374022509950004', '1995-09-25', 'P', 'Jl. Kenanga No.3, Semarang',        '0813-2000-004', 'AB'),
(5,  13, 'RM-2026-000005', '3471051006680005', '1968-06-10', 'L', 'Jl. Dahlia No.21, Yogyakarta',      '0813-2000-005', 'A'),
(6,  14, 'RM-2026-000006', '3578031811820006', '1982-11-18', 'P', 'Jl. Melur No.7, Surabaya',          '0813-2000-006', 'B'),
(7,  15, 'RM-2026-000007', '3671092204600007', '1960-04-22', 'L', 'Jl. Pucuk Merah No.14, Medan',      '0813-2000-007', 'O'),
(8,  16, 'RM-2026-000008', '3775023008980008', '1998-08-30', 'P', 'Jl. Sakura No.9, Denpasar',         '0813-2000-008', 'A'),
(9,  17, 'RM-2026-000009', '5101051510750009', '1975-10-15', 'L', 'Jl. Teratai No.4, Makassar',         '0813-2000-009', 'AB'),
(10, 18, 'RM-2026-000010', '5171021202880010', '1988-02-12', 'P', 'Jl. Flamboyan No.11, Palembang',    '0813-2000-010', 'B'),
(11, 19, 'RM-2026-000011', '3273152802920011', '1992-02-28', 'L', 'Jl. Cempaka No.16, Tangerang',      '0813-2000-011', 'A'),
(12, 20, 'RM-2026-000012', '3403110505850012', '1985-05-05', 'P', 'Jl. Alamanda No.2, Depok',           '0813-2000-012', 'O');

-- ================================================
-- 6. APPOINTMENT (15 appointment)
-- ================================================
INSERT INTO appointment (id, pasien_id, dokter_id, jadwal_id, tanggal, status, keluhan, created_at) VALUES
(1,  1,  1, 1,  '2026-07-10', 'SELESAI',    'Demam tinggi 3 hari disertai batuk berdahak',                      '2026-07-10 07:30:00'),
(2,  2,  2, 3,  '2026-07-11', 'SELESAI',    'Nyeri sendi lutut kiri terutama saat naik tangga',                 '2026-07-11 07:45:00'),
(3,  3,  3, 5,  '2026-07-12', 'SELESAI',    'Nyeri dada kiri menjalar ke lengan kiri, sesak napas ringan',      '2026-07-12 08:00:00'),
(4,  4,  4, 7,  '2026-07-14', 'SELESAI',    'Nyeri punggung bawah kronis sejak 2 bulan yang lalu',               '2026-07-14 07:30:00'),
(5,  5,  5, 9,  '2026-07-15', 'DIPERIKSA',  'Sering buang air kecil, mudah haus, berat badan turun',            '2026-07-15 08:15:00'),
(6,  6,  6, 11, '2026-07-16', 'DIPERIKSA',  'Anak demam 38.5°C, ruam merah di kulit, tidak nafsu makan',        '2026-07-16 07:30:00'),
(7,  7,  7, 13, '2026-07-17', 'DIPERIKSA',  'Nyeri pergelangan tangan kanan setelah jatuh 1 minggu lalu',       '2026-07-17 08:00:00'),
(8,  8,  1, 2,  '2026-07-18', 'DIPERIKSA',  'Sakit kepala hebat sebelah kanan, mual, sensitif terhadap cahaya',  '2026-07-18 13:15:00'),
(9,  9,  2, 4,  '2026-07-21', 'BATAL',      'Kontrol gula darah dan kolesterol',                                 '2026-07-21 13:00:00'),
(10, 10, 3, 6,  '2026-07-22', 'BATAL',      'Kontrol pasca operasi',                                            '2026-07-22 14:00:00'),
(11, 11, 4, 8,  '2026-07-23', 'MENUNGGU',   'Nyeri bahu kiri setelah berolahraga',                               '2026-07-23 09:00:00'),
(12, 12, 5, 10, '2026-07-24', 'MENUNGGU',   'Perut mual dan nyeri ulu hati setelah makan pedas',                '2026-07-24 08:00:00'),
(13, 1,  6, 12, '2026-07-25', 'MENUNGGU',   'Anak-anak: imunisasi rutin lengkap, nafsu makan berkurang',         '2026-07-25 13:00:00'),
(14, 3,  7, 14, '2026-07-26', 'MENUNGGU',   'Sakit lutut kanan, sulit berjalan jauh, terdengar bunyi krek',     '2026-07-26 08:00:00'),
(15, 2,  1, 1,  '2026-07-28', 'MENUNGGU',   'Batuk kering selama 1 minggu, tidak sembuh dengan obat warung',    '2026-07-28 07:30:00');

-- ================================================
-- 7. ANTRIAN (13 antrian, dari appointment non-BATAL)
-- ================================================
INSERT INTO antrian (id, appointment_id, nomor_urut, created_at) VALUES
(1,  1,  1, '2026-07-10 07:30:00'),
(2,  2,  1, '2026-07-11 07:45:00'),
(3,  3,  1, '2026-07-12 08:00:00'),
(4,  4,  1, '2026-07-14 07:30:00'),
(5,  5,  1, '2026-07-15 08:15:00'),
(6,  6,  1, '2026-07-16 07:30:00'),
(7,  7,  1, '2026-07-17 08:00:00'),
(8,  8,  1, '2026-07-18 13:15:00'),
(9,  11, 1, '2026-07-23 09:00:00'),
(10, 12, 1, '2026-07-24 08:00:00'),
(11, 13, 1, '2026-07-25 13:00:00'),
(12, 14, 1, '2026-07-26 08:00:00'),
(13, 15, 1, '2026-07-28 07:30:00');

-- ================================================
-- 8. REKAM_MEDIS (8 rekam medis — 1 per appointment SELESAI/DIPERIKSA)
-- ================================================
INSERT INTO rekam_medis (id, pasien_id, dokter_id, appointment_id, diagnosa, tindakan, catatan, created_at) VALUES
(1,  1, 1, 1,
 'ISPA (Infeksi Saluran Pernapasan Akut)',
 'Pemeriksaan fisik, resep antibiotik oral, rontgen dada',
 'Pasien disarankan istirahat cukup, minum air hangat, kontrol jika demam tidak turun 3 hari',
 '2026-07-10 09:00:00'),
(2,  2, 2, 2,
 'Osteoarthritis Lutut Grade 2',
 'Pemeriksaan fisik, rontgen lutut AP/Lateral, resep analgesik',
 'Disarankan fisioterapi, kurangi aktivitas naik tangga, kontrol 2 minggu',
 '2026-07-11 09:15:00'),
(3,  3, 3, 3,
 'Angina Pektoris Stabil + Hipertensi Grade 1',
 'EKG, tes darah lengkap, rujukan spesialis jantung, resep Amlodipine & Captopril',
 'Pasien diresepkan antihipertensi, disarankan hindari stres fisik, diet rendah garam, kontrol 1 minggu',
 '2026-07-12 09:30:00'),
(4,  4, 4, 4,
 'HNP Lumbal Ringan + Dispepsia Fungsional',
 'Pemeriksaan neurologis, MRI lumbal, resep analgesik, muscle relaxant, dan antasida',
 'Disarankan fisioterapi, latihan penguatan core, atur jadwal makan teratur',
 '2026-07-14 09:00:00'),
(5,  5, 5, 5,
 'Diabetes Mellitus Tipe 2 (HbA1c: 8.2%)',
 'Tes gula darah puasa & HbA1c, resep Metformin 500mg 2x1 + Glibenclamide 5mg, edukasi diet',
 'Kontrol ke Poli Penyakit Dalam setiap bulan, catat gula darah harian di rumah',
 '2026-07-15 09:45:00'),
(6,  6, 6, 6,
 'Rubella (Campak Jerman)',
 'Pemeriksaan fisik, tes serologi, antipiretik, vitamin C',
 'Isolasi di rumah, tidak kontak dengan ibu hamil, kontrol setelah ruam hilang',
 '2026-07-16 09:00:00'),
(7,  7, 7, 7,
 'Fraktur Radius Distal (Colles Fracture) Ringan',
 'Pemeriksaan rontgen, pemasangan gips bawah siku, resep analgesik & vitamin D3',
 'Gips dibuka 4-6 minggu, latihan ROM setelah gips dilepas, kontrol setiap 2 minggu',
 '2026-07-17 09:15:00'),
(8,  8, 1, 8,
 'Migrain dengan Aura',
 'Pemeriksaan neurologis singkat, CT-Scan kepala, resep Sumatriptan dan analgesik',
 'Hindari pemicu (cahaya terang, kurang tidur), istirahat di ruangan gelap jika serangan',
 '2026-07-18 14:00:00');

-- ================================================
-- 9. OBAT (15 master data obat)
-- ================================================
INSERT INTO obat (id, nama, kategori, satuan, stok, harga, deskripsi, tanggal_produksi, tanggal_kadaluarsa, is_active) VALUES
(1,  'Amlodipine 5mg',           'Antihipertensi',     'Tablet', 200, 2500.00,   'Obat hipertensi golongan calcium channel blocker',                          '2026-01-01', '2028-01-01', true),
(2,  'Captopril 25mg',           'Antihipertensi',     'Tablet', 150, 1800.00,   'ACE inhibitor untuk hipertensi',                                            '2026-02-01', '2028-02-01', true),
(3,  'Amoxicillin 500mg',        'Antibiotik',         'Kapsul', 300, 3500.00,   'Antibiotik spektrum luas golongan penicillin',                               '2026-03-01', '2027-03-01', true),
(4,  'Ciprofloxacin 500mg',      'Antibiotik',         'Tablet', 120, 4500.00,   'Antibiotik fluoroquinolone untuk infeksi bakteri',                           '2026-01-15', '2027-06-15', true),
(5,  'Metformin 500mg',          'Antidiabetes',       'Tablet', 250, 1500.00,   'Obat diabetes tipe 2 golongan biguanide',                                   '2026-02-10', '2028-02-10', true),
(6,  'Glibenclamide 5mg',        'Antidiabetes',       'Tablet', 180, 2000.00,   'Obat diabetes tipe 2 golongan sulfonylurea',                                '2026-03-15', '2028-03-15', true),
(7,  'Paracetamol 500mg',        'Analgesik',          'Tablet', 500, 500.00,    'Analgesik-antipiretik untuk nyeri ringan-sedang dan demam',                  '2026-04-01', '2029-04-01', true),
(8,  'Ibuprofen 400mg',          'Analgesik',          'Tablet', 200, 1200.00,   'NSAID untuk nyeri dan inflamasi',                                           '2026-03-20', '2028-03-20', true),
(9,  'Omeprazole 20mg',          'Antasida',           'Kapsul', 180, 3000.00,   'Proton pump inhibitor untuk GERD dan dispepsia',                             '2026-02-20', '2028-02-20', true),
(10, 'Ranitidine 150mg',         'Antasida',           'Tablet', 160, 2200.00,   'Antagonis reseptor H2 untuk asam lambung berlebih',                         '2026-01-10', '2027-12-10', true),
(11, 'Diazepam 5mg',             'Sedatif',            'Tablet', 80,  5000.00,   'Sedatif-ansiolitik untuk kejang dan ansietas (PSIKOTROPIKA)',              '2026-02-01', '2027-02-01', true),
(12, 'Sumatriptan 50mg',         'Antimigrain',        'Tablet', 60,  8500.00,   'Agonis serotonin 5-HT1 untuk migrain akut',                                  '2026-03-01', '2028-03-01', true),
(13, 'Triheksifenidil 2mg',      'Antiparkinson',      'Tablet', 50,  3500.00,   'Antikolinergik untuk gejala parkinson',                                     '2026-01-20', '2028-01-20', true),
(14, 'Vitamin C 500mg',          'Vitamin',            'Tablet', 400, 800.00,    'Suplemen vitamin C untuk daya tahan tubuh',                                 '2026-04-15', '2028-04-15', true),
(15, 'Vitamin D3 1000 IU',       'Vitamin',            'Kapsul', 350, 1200.00,   'Suplemen vitamin D untuk kesehatan tulang dan imun',                        '2026-04-10', '2028-04-10', true);

-- ================================================
-- 10. RESEP (16 resep, 2 per rekam medis)
-- ================================================
INSERT INTO resep (id, rekam_medis_id, obat_id, nama_obat, dosis, jumlah, aturan_pakai, created_at) VALUES
-- RM #1 (ISPA - Budi Santoso / dr. Ahmad Fauzi)
(1,  1,  3,  'Amoxicillin 500mg',         '500mg', 10, '3x1 sesudah makan (habiskan)',  '2026-07-10 09:05:00'),
(2,  1,  7,  'Paracetamol 500mg',         '500mg', 12, '3x1 jika demam (minimal 4 jam interval)', '2026-07-10 09:05:00'),
-- RM #2 (Osteoarthritis - Ani Rahmawati / dr. Sari Wijaya)
(3,  2,  8,  'Ibuprofen 400mg',           '400mg', 10, '2x1 sesudah makan (lambung aman)',         '2026-07-11 09:20:00'),
(4,  2,  15, 'Vitamin D3 1000 IU',        '1000 IU', 30, '1x1 pagi sesudah makan',                   '2026-07-11 09:20:00'),
-- RM #3 (Angina + Hipertensi - Herman Salim / dr. Budi Hartono)
(5,  3,  1,  'Amlodipine 5mg',            '5mg', 30, '1x1 pagi (tekanan darah)',            '2026-07-12 09:35:00'),
(6,  3,  2,  'Captopril 25mg',            '25mg', 30,'1x1 pagi',                              '2026-07-12 09:35:00'),
-- RM #4 (HNP + Dispepsia - Siti Nurhaliza / dr. Dewi Lestari)
(7,  4,  8,  'Ibuprofen 400mg',           '400mg', 10, '2x1 sesudah makan',                  '2026-07-14 09:05:00'),
(8,  4,  9,  'Omeprazole 20mg',           '20mg', 14, '1x1 pagi sebelum makan',               '2026-07-14 09:05:00'),
-- RM #5 (Diabetes - Rizal Pratama / dr. Rudi Hermawan)
(9,  5,  5,  'Metformin 500mg',           '500mg', 30, '2x1 sesudah makan',                  '2026-07-15 09:50:00'),
(10, 5,  6,  'Glibenclamide 5mg',         '5mg', 30,  '1x1 pagi sebelum makan',               '2026-07-15 09:50:00'),
-- RM #6 (Rubella - Linda Kusuma / dr. Maya Putri)
(11, 6,  7,  'Paracetamol 500mg',         '500mg', 10, '4x1 jika demam (interval 6 jam)',    '2026-07-16 09:05:00'),
(12, 6,  14, 'Vitamin C 500mg',           '500mg', 30, '1x1 pagi sesudah makan',              '2026-07-16 09:05:00'),
-- RM #7 (Fraktur - Agus Supriyadi / dr. Hendra Gunawan)
(13, 7,  8,  'Ibuprofen 400mg',           '400mg', 12, '2x1 sesudah makan',                  '2026-07-17 09:20:00'),
(14, 7,  15, 'Vitamin D3 1000 IU',        '1000 IU', 30, '1x1 pagi',                         '2026-07-17 09:20:00'),
-- RM #8 (Migrain - Tiara Anggraini / dr. Ahmad Fauzi)
(15, 8,  12, 'Sumatriptan 50mg',          '50mg', 4,   '1x saat serangan migrain (maks 2x/hari)', '2026-07-18 14:05:00'),
(16, 8,  7,  'Paracetamol 500mg',         '500mg', 8,  '3x1 jika nyeri',                      '2026-07-18 14:05:00');

-- ================================================
-- 11. KAMAR (10 kamar)
-- ================================================
INSERT INTO kamar (id, nomor_kamar, tipe_kamar, kapasitas, terisi, harga_per_malam, is_active) VALUES
(1,  'R-101', 'REGULER', 4, 2, 350000.00,  true),
(2,  'R-102', 'REGULER', 4, 1, 350000.00,  true),
(3,  'R-103', 'REGULER', 4, 0, 350000.00,  true),
(4,  'R-201', 'REGULER', 2, 1, 350000.00,  true),
(5,  'R-202', 'REGULER', 2, 0, 350000.00,  true),
(6,  'VIP-301', 'VIP', 2, 0, 750000.00,    true),
(7,  'VIP-302', 'VIP', 2, 1, 750000.00,    true),
(8,  'VIP-303', 'VIP', 1, 0, 750000.00,    true),
(9,  'VVIP-401', 'VVIP', 1, 0, 1500000.00, true),
(10, 'ICU-501', 'ICU', 4, 1, 2500000.00,   true);

-- ================================================
-- 12. LAB_ORDER (10 order lab)
-- ================================================
INSERT INTO lab_order (id, rekam_medis_id, jenis_pemeriksaan, catatan, status, created_at) VALUES
(1,  3,  'Darah Lengkap',            'Skrining awal angina',                               'SELESAI', '2026-07-12 09:45:00'),
(2,  3,  'EKG',                       'Rekam EKG 12-lead',                                  'SELESAI', '2026-07-12 09:46:00'),
(3,  5,  'Gula Darah Puasa',         'Cek gula darah puasa untuk konfirmasi DM',           'SELESAI', '2026-07-15 09:55:00'),
(4,  5,  'HbA1c',                     'Kontrol gula darah 3 bulan terakhir',                'SELESAI', '2026-07-15 09:56:00'),
(5,  3,  'Profil Lipid',             'Cek kolesterol total, LDL, HDL, trigliserida',       'MENUNGGU','2026-07-12 09:40:00'),
(6,  3,  'Fungsi Ginjal',            'Cek ureum, kreatinin, eGFR — baseline antihipertensi',       'MENUNGGU','2026-07-12 09:41:00'),
(7,  1,  'Hitung Jenis Leukosit',    'Konfirmasi infeksi bakteri vs virus',                'PROSES',  '2026-07-10 09:10:00'),
(8,  4,  'Rontgen Lumbal AP/Lateral','Konfirmasi HNP',                                      'PROSES',  '2026-07-14 09:10:00'),
(9,  7,  'Rontgen Pergelangan Tangan','Evaluasi fraktur radius distal',                     'PROSES',  '2026-07-17 09:25:00'),
(10, 2,  'Rontgen Lutut AP/Lateral',  'Evaluasi osteoarthritis',                            'SELESAI', '2026-07-11 09:25:00');

-- ================================================
-- 13. LAB_HASIL (5 hasil lab, dari order yang SELESAI)
-- ================================================
INSERT INTO lab_hasil (id, lab_order_id, hasil_text, nilai_normal, interpretasi, created_at) VALUES
-- Darah Lengkap (Angina + Hipertensi - Herman Salim)
(1, 1,
 'Hb: 14.2 g/dL, Leukosit: 8,500/uL, Trombosit: 280,000/uL, Eritrosit: 4.8 juta/uL, Hematokrit: 42%',
 'Hb: 13-17 g/dL, Leukosit: 4,000-10,000/uL, Trombosit: 150,000-450,000/uL',
 'Hasil darah lengkap dalam batas normal, tidak ada indikasi infeksi',
 '2026-07-12 10:30:00'),
-- EKG (Angina + Hipertensi - Herman Salim)
(2, 2,
 'Irama sinus, HR 78 bpm, gelombang T inverted di lead V1-V3, tidak ada elevasi ST',
 'Irama sinus reguler, HR 60-100 bpm, gelombang T positif di semua lead',
 'T inverted di V1-V3 mengindikasikan iskemia miokard anterior, perlu evaluasi lanjutan',
 '2026-07-12 10:35:00'),
-- Gula Darah Puasa (Diabetes - Rizal Pratama)
(3, 3,
 'Glukosa Puasa: 145 mg/dL',
 'Glukosa Puasa Normal: 70-100 mg/dL',
 'Hiperglikemia puasa, konsisten dengan Diabetes Mellitus Tipe 2',
 '2026-07-15 10:45:00'),
-- HbA1c (Diabetes - Rizal Pratama)
(4, 4,
 'HbA1c: 8.2%',
 'Normal: <5.7%, Prediabetes: 5.7-6.4%, Diabetes: >6.5%',
 'HbA1c tinggi menunjukkan diabetes tidak terkontrol dengan baik, perlu penyesuaian terapi',
 '2026-07-15 10:50:00'),
-- Rontgen Lutut (Osteoarthritis - Ani Rahmawati)
(5, 10,
 'Penyempitan celah sendi medial tibiofemoral, osteofit marginal, sklerosis subkondral ringan, alignment varus 2 derajat',
 'Celah sendi simetris, tidak ada osteofit, korteks halus',
 'Osteoarthritis lutut grade 2 (Kellgren-Lawrence), sesuai dengan gejala klinis',
 '2026-07-11 10:15:00');

-- ================================================
-- 14. RADIOLOGI (8 order radiologi)
-- ================================================
INSERT INTO radiologi (id, rekam_medis_id, jenis_radiologi, hasil_deskripsi, catatan_dokter, gambar_url, status, created_at) VALUES
(1, 2,  'X-Ray Lutut AP/Lateral',
 'Penyempitan celah sendi medial, osteofit di tepi tibia dan femur, sklerosis subkondral, tidak ada fraktur',
 'Osteoarthritis grade 2, sesuai keluhan nyeri sendi lutut',
 '/uploads/rad/rg_20260711_001.jpg', 'SELESAI', '2026-07-11 09:20:00'),
(2, 4,  'X-Ray Lumbal AP/Lateral',
 'Kurva lordosis normal, penyempitan diskus intervertebralis L4-L5, tidak ada spondilolistesis',
 'Konfirmasi HNP ringan L4-L5',
 '/uploads/rad/rg_20260714_001.jpg', 'SELESAI', '2026-07-14 09:12:00'),
(3, 7,  'X-Ray Pergelangan Tangan Kanan AP/Lateral',
 'Fraktur transversal radius distal tanpa dislokasi, alignment baik, tidak intra-artikular',
 'Fraktur Colles ringan, cukup dengan gips konservatif',
 '/uploads/rad/rg_20260717_001.jpg', 'SELESAI', '2026-07-17 09:22:00'),
(4, 3,  'X-Ray Dada PA',
 'Jantung ukuran normal, CTR 48%, tidak ada kardiomegali, lapangan paru bersih, sinus costofrenicus tajam',
 'Rontgen dada normal, tidak ada tanda gagal jantung',
 '/uploads/rad/rg_20260712_001.jpg', 'SELESAI', '2026-07-12 09:40:00'),
(5, 1,  'X-Ray Dada PA',                          NULL, 'Pasien ISPA, rontgen untuk pastikan tidak ada pneumonia',    NULL, 'MENUNGGU', '2026-07-10 09:08:00'),
(6, 6,  'X-Ray Dada PA',                          NULL, 'Anak demam dengan ruam, evaluasi kemungkinan komplikasi paru', NULL, 'MENUNGGU', '2026-07-16 09:08:00'),
(7, 8,  'CT-Scan Kepala',                         NULL, 'Pasien migrain kronis, evaluasi kemungkinan lesi intrakranial', NULL, 'MENUNGGU', '2026-07-18 14:10:00'),
(8, 5,  'USG Abdomen',                            NULL, 'Pasien diabetes, evaluasi komplikasi organ abdominal',        NULL, 'MENUNGGU', '2026-07-15 09:52:00');

-- ================================================
-- 15. BILLING (8 billing, dari appointment SELESAI/DIPERIKSA)
-- ================================================
INSERT INTO billing (id, pasien_id, total_harga, status, tanggal_tagihan, catatan, created_at) VALUES
-- Appointment #1 (SELESAI) - Pasien 1
(1, 1,  350000.00,  'LUNAS',       '2026-07-10', 'Konsultasi + obat ISPA',               '2026-07-10 09:45:00'),
-- Appointment #2 (SELESAI) - Pasien 2
(2, 2,  550000.00,  'LUNAS',       '2026-07-11', 'Konsultasi + rontgen lutut + obat',     '2026-07-11 10:00:00'),
-- Appointment #3 (SELESAI) - Pasien 3
(3, 3,  750000.00,  'BELUM_BAYAR', '2026-07-12', 'Konsultasi + EKG + darah lengkap + obat','2026-07-12 10:30:00'),
-- Appointment #4 (SELESAI) - Pasien 4
(4, 4,  850000.00,  'LUNAS',       '2026-07-14', 'Konsultasi + MRI + rontgen + obat',     '2026-07-14 10:00:00'),
-- Appointment #5 (DIPERIKSA) - Pasien 5
(5, 5,  450000.00,  'BELUM_BAYAR', '2026-07-15', 'Konsultasi + tes darah + obat diabetes','2026-07-15 10:30:00'),
-- Appointment #6 (DIPERIKSA) - Pasien 6
(6, 6,  300000.00,  'BELUM_BAYAR', '2026-07-16', 'Konsultasi + obat anak',                '2026-07-16 09:30:00'),
-- Appointment #7 (DIPERIKSA) - Pasien 7
(7, 7,  400000.00,  'BELUM_BAYAR', '2026-07-17', 'Konsultasi + rontgen + gips + obat',    '2026-07-17 09:45:00'),
-- Appointment #8 (DIPERIKSA) - Pasien 8
(8, 8,  350000.00,  'BELUM_BAYAR', '2026-07-18', 'Konsultasi + obat migrain',             '2026-07-18 14:30:00');

-- ================================================
-- 16. BILLING_ITEM (15 item billing)
-- ================================================
INSERT INTO billing_item (id, billing_id, deskripsi, jumlah, harga_satuan, subtotal) VALUES
-- Billing #1 (Budi Santoso - ISPA)
(1,  1, 'Biaya Konsultasi Dokter Umum',        1, 150000.00, 150000.00),
(2,  1, 'Amoxicillin 500mg (10 tablet)',        1, 35000.00,  35000.00),
(3,  1, 'Paracetamol 500mg (12 tablet)',        1, 6000.00,   6000.00),
(4,  1, 'Biaya Administrasi',                   1, 159000.00, 159000.00),
-- Billing #2 (Ani Rahmawati - Osteoarthritis)
(5,  2, 'Biaya Konsultasi Dokter Umum',        1, 150000.00, 150000.00),
(6,  2, 'Rontgen Lutut AP/Lateral',             1, 250000.00, 250000.00),
(7,  2, 'Ibuprofen + Vitamin D3',               1, 150000.00, 150000.00),
-- Billing #3 (Herman Salim - Angina)
(8,  3, 'Biaya Konsultasi Spesialis Jantung',   1, 250000.00, 250000.00),
(9,  3, 'EKG + Darah Lengkap',                  1, 300000.00, 300000.00),
(10, 3, 'Amlodipine + Paracetamol',             1, 50000.00,  50000.00),
(11, 3, 'Biaya Administrasi',                   1, 150000.00, 150000.00),
-- Billing #4 (Siti Nurhaliza - HNP)
(12, 4, 'Biaya Konsultasi Spesialis Ortopedi',  1, 250000.00, 250000.00),
(13, 4, 'MRI Lumbal + Rontgen',                  1, 450000.00, 450000.00),
(14, 4, 'Obat (Ibuprofen + Diazepam + Vit D)',   1, 150000.00, 150000.00),
-- Billing #6 (Linda Kusuma - Rubella)
(15, 6, 'Biaya Konsultasi Spesialis Anak',      1, 200000.00, 200000.00);

-- ================================================
-- 17. PEMBAYARAN (3 pembayaran, dari billing LUNAS)
-- ================================================
INSERT INTO pembayaran (id, billing_id, metode_pembayaran, jumlah_bayar, status, bukti_transfer, tanggal_bayar, catatan, created_at) VALUES
(1, 1, 'TUNAI',    350000.00, 'BERHASIL', NULL,                               '2026-07-10 10:00:00', 'Dibayar langsung di kasir',                                    '2026-07-10 10:00:00'),
(2, 2, 'TRANSFER', 550000.00, 'BERHASIL', '/uploads/bukti/trf_20260711_001.jpg','2026-07-11 11:00:00', 'Transfer via BCA, sudah dikonfirmasi',                         '2026-07-11 11:00:00'),
(3, 4, 'TRANSFER', 850000.00, 'BERHASIL', '/uploads/bukti/trf_20260714_001.jpg','2026-07-14 11:30:00', 'Transfer via Mandiri, dari Ibu Siti Nurhaliza',               '2026-07-14 11:30:00');

COMMIT;

-- ================================================
-- VERIFIKASI
-- ================================================
SELECT '=== VERIFIKASI SEED DATA ===' AS info;
SELECT 'users'          AS tabel, COUNT(*) AS jumlah FROM users
UNION ALL SELECT 'poli',           COUNT(*) FROM poli
UNION ALL SELECT 'dokter',         COUNT(*) FROM dokter
UNION ALL SELECT 'jadwal_dokter',  COUNT(*) FROM jadwal_dokter
UNION ALL SELECT 'pasien',         COUNT(*) FROM pasien
UNION ALL SELECT 'appointment',    COUNT(*) FROM appointment
UNION ALL SELECT 'antrian',        COUNT(*) FROM antrian
UNION ALL SELECT 'rekam_medis',    COUNT(*) FROM rekam_medis
UNION ALL SELECT 'obat',           COUNT(*) FROM obat
UNION ALL SELECT 'resep',          COUNT(*) FROM resep
UNION ALL SELECT 'kamar',          COUNT(*) FROM kamar
UNION ALL SELECT 'lab_order',      COUNT(*) FROM lab_order
UNION ALL SELECT 'lab_hasil',      COUNT(*) FROM lab_hasil
UNION ALL SELECT 'radiologi',      COUNT(*) FROM radiologi
UNION ALL SELECT 'billing',        COUNT(*) FROM billing
UNION ALL SELECT 'billing_item',   COUNT(*) FROM billing_item
UNION ALL SELECT 'pembayaran',     COUNT(*) FROM pembayaran
ORDER BY tabel;
