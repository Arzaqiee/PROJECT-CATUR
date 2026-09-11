# Qie Chess

Prototype game catur web yang siap di-host di GitHub Pages dan nantinya dibungkus menjadi APK.

## Fitur
- Main menu modern
- VS Bot offline (bot prototype)
- Local 2 Player
- VS Friend sebagai placeholder untuk integrasi realtime berikutnya
- Legal move dasar
- Capture & promotion dasar
- Timer 10 menit
- Undo / restart / resign
- Move history
- Local history
- Sound dibuat langsung dengan Web Audio API (tanpa MP3 eksternal)
- Vibration jika perangkat/browser mendukung
- Settings
- Responsive mobile UI

## Menjalankan
Tidak membutuhkan server. Buka `index.html` di browser.

Untuk GitHub Pages:
1. Upload semua file.
2. Settings → Pages.
3. Pilih branch `main` dan folder `/root`.
4. Simpan.

## Struktur
- `index.html`
- `style.css`
- `script.js`
- `sounds/` (disiapkan untuk sound asset berikutnya)
- `.github/workflows/pages.yml`

## Catatan
Ini prototype. Untuk versi production, gunakan chess rules engine yang memvalidasi check, checkmate, castling, en passant, threefold repetition, dan fifty-move rule. Untuk bot kuat, integrasikan Stockfish. Multiplayer online memerlukan backend realtime (misalnya Firebase/Supabase/WebSocket server).
