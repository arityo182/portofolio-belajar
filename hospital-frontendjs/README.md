# Hospital Frontend (JavaScript)

Versi JavaScript dari frontend RS Medika Sentosa dan sekarang menjadi
frontend utama project. Versi TypeScript lama tetap disimpan di
`../hospital-frontend/` sebagai arsip. Perilaku aplikasi sama — seluruh
anotasi dan deklarasi tipe dihapus, `*.tsx` menjadi `*.jsx`, dan `*.ts`
menjadi `*.js`.

## Tech Stack

- React 19
- Vite 8
- Tailwind CSS 4
- React Router 7
- Axios
- Vitest + Testing Library

## Menjalankan

```bash
npm install
npm run dev
```

Frontend berjalan di `http://localhost:5173`. Vite mem-proxy request `/api`
ke backend Spring Boot di `http://localhost:8080`.

## Perintah

```bash
npm run dev        # development server
npm run build      # build production ke dist/
npm run preview    # preview hasil build
npm run lint       # ESLint
npm test           # jalankan test sekali
npm run test:watch # test mode watch
```

## Catatan

- Tidak ada type-checking (`tsc`) pada versi ini; konfigurasi editor
  tersedia di `jsconfig.json` dan `checkJs` dinonaktifkan.
- Konfigurasi test membaca `src/test-setup.js`.
- Docker: `Dockerfile` memakai base image `medika-frontend-deps:latest`,
  dibangun dari `Dockerfile.deps`.
