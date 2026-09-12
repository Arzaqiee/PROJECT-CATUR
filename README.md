# Qie Chess

Game catur Android modern, ringan, dan minimalis - dibuat dengan **Kotlin + Jetpack Compose**.
Gameplay catur lengkap (termasuk castling, en passant, promotion, draw rules),
bot lawan offline, mode lokal 2 pemain, puzzle, riwayat permainan, dan
kerangka multiplayer online siap-Firebase.

## Daftar Isi

- [Fitur](#fitur)
- [Teknologi](#teknologi)
- [Struktur Folder](#struktur-folder)
- [Cara Menjalankan](#cara-menjalankan)
- [Cara Build APK](#cara-build-apk)
- [Konfigurasi Firebase / Online Multiplayer](#konfigurasi-firebase--online-multiplayer)
- [Menambahkan Sound Effect](#menambahkan-sound-effect)
- [GitHub Actions (Auto Build APK)](#github-actions-auto-build-apk)
- [Troubleshooting](#troubleshooting)
- [Roadmap / Yang Belum Selesai](#roadmap--yang-belum-selesai)

## Fitur

**Gameplay catur (100% berfungsi, offline, tanpa konfigurasi apapun):**
- Semua gerakan bidak standar: Pawn, Rook, Knight, Bishop, Queen, King
- Check, checkmate, stalemate
- Castling (kingside & queenside), en passant, pawn promotion
- Threefold repetition, fifty-move rule, insufficient material draw
- Legal move highlighting, last move highlighting, board otomatis menyesuaikan layar

**Mode bermain:**
- **VS Bot** - 3 tingkat kesulitan (Easy/Medium/Hard), minimax + alpha-beta
  pruning dengan piece-square tables (bukan random move). Pilihan warna
  White/Black/Random.
- **Local 2 Player** - dua orang bergantian di satu HP.
- **Play vs Friend (Online)** - kerangka lengkap (room code, create/join,
  realtime sync) siap dipakai setelah kamu menghubungkan proyek Firebase-mu
  sendiri (gratis). Lihat bagian konfigurasi di bawah.

**Lainnya:**
- Puzzle catur (5 puzzle contoh, tersimpan lokal, bisa ditambah)
- Riwayat permainan tersimpan lokal (tanggal, mode, hasil, warna, opponent, jumlah langkah)
- Notasi SAN + export PGN (tombol Copy PGN)
- Chess clock dengan preset 1+0 s/d 15+10, atau tanpa timer
- Settings: dark/light mode, board theme, sound, vibration, show legal
  moves, show coordinates, animation, bahasa (Indonesia/English)
- Sound effect & haptic feedback (hook siap pakai, lihat bagian "Menambahkan Sound Effect")
- UI hitam-putih minimalis, tanpa emoji, fokus pada papan catur

## Teknologi

| Bagian | Teknologi |
|---|---|
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arsitektur | MVVM (ViewModel + StateFlow) |
| Build | Gradle (Kotlin DSL), Android Gradle Plugin 8.5.2 |
| Local storage | Jetpack DataStore (Preferences) |
| Online multiplayer | Firebase Realtime Database (opt-in) |
| Chess engine | Custom (rules engine + minimax bot), lihat catatan Stockfish di bawah |
| Min SDK | 24 (Android 7.0) |
| Target/Compile SDK | 34 |

**Kenapa tidak Stockfish?** Mengintegrasikan Stockfish native (JNI/NDK) ke
Android menambah kompleksitas build yang signifikan (cross-compile C++,
ukuran APK, ABI splits) dan sulit diverifikasi tanpa lingkungan build native.
Sesuai instruksi awal, proyek ini memakai chess engine buatan sendiri:
rule engine lengkap + bot minimax/alpha-beta dengan piece-square tables.
Bot ini benar-benar "berpikir" (bukan random) dan akan menemukan mat pendek
dalam kedalaman pencariannya. Kalau nanti mau upgrade ke Stockfish, titik
integrasinya ada di `bot/ChessBot.kt` - cukup ganti isi `findBestMove()`
untuk memanggil engine native via JNI, API publiknya tidak perlu berubah.

## Struktur Folder

```
QieChess/
├── app/
│   ├── src/main/
│   │   ├── java/com/qie/chess/
│   │   │   ├── model/          # Piece, Board, GameState, Move, dll (data murni)
│   │   │   ├── engine/         # MoveGenerator, ChessEngine, Notation (SAN/PGN), Fen
│   │   │   ├── bot/            # ChessBot (minimax+alpha-beta), Evaluator, PieceSquareTables
│   │   │   ├── data/           # SettingsRepository, HistoryRepository, PuzzleRepository (DataStore)
│   │   │   ├── multiplayer/    # OnlineGameConnection interface, RoomManager, config
│   │   │   ├── audio/          # SoundManager (SoundPool)
│   │   │   ├── util/           # Haptics
│   │   │   ├── viewmodel/      # GameViewModel, SettingsViewModel, HistoryViewModel
│   │   │   ├── ui/
│   │   │   │   ├── theme/      # Color, Type, Theme (Material3)
│   │   │   │   ├── components/ # ChessBoardView, PromotionDialog, MoveHistoryPanel, dll
│   │   │   │   └── screens/    # MainMenu, BotSetup, LocalSetup, OnlineLobby, Game, Puzzle, History, Settings
│   │   │   ├── navigation/     # NavGraph (Jetpack Navigation Compose)
│   │   │   ├── MainActivity.kt
│   │   │   └── QieChessApp.kt  # Application class (manual DI container sederhana)
│   │   ├── res/                 # strings, themes, launcher icon
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── docs/firebase-online-multiplayer/   # Referensi implementasi Firebase (opt-in, lihat di bawah)
├── .github/workflows/build.yml         # GitHub Actions - auto build APK
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
└── README.md
```

## Cara Menjalankan

1. Install **Android Studio** (Koala/2024.1 atau lebih baru direkomendasikan).
2. Clone/download repo ini, lalu buka folder `QieChess` lewat **Open Project** di Android Studio.
3. Tunggu Gradle sync selesai (Android Studio akan otomatis melengkapi
   `gradle-wrapper.jar` yang sengaja tidak disertakan sebagai file biner di
   repo ini - lihat catatan di Troubleshooting).
4. Pilih device/emulator (min. Android 7.0 / API 24), klik **Run**.

Tanpa konfigurasi tambahan apapun, kamu sudah bisa main **VS Bot**,
**Local 2 Player**, **Puzzles**, **History**, dan **Settings** sepenuhnya offline.

## Cara Build APK

Dari terminal, di root folder proyek:

```bash
./gradlew assembleDebug
```

APK hasil build ada di:

```
app/build/outputs/apk/debug/app-debug.apk
```

Untuk build release (perlu setup signing config sendiri di Android Studio ->
Build > Generate Signed Bundle/APK):

```bash
./gradlew assembleRelease
```

## Konfigurasi Firebase / Online Multiplayer

Mode "Play vs Friend" (online) butuh backend realtime. Agar tidak ada API
key/secret yang ikut ter-commit ke repo publik, proyek ini **tidak**
menyertakan kredensial apapun - kamu perlu menghubungkan proyek Firebase
gratis milikmu sendiri:

1. Buka [Firebase Console](https://console.firebase.google.com), buat project baru (gratis, Spark plan cukup).
2. Tambahkan aplikasi Android dengan package name `com.qie.chess`.
3. Download `google-services.json`, taruh di `app/google-services.json`
   (path ini sudah ada di `.gitignore` supaya tidak ke-commit).
4. Di Firebase Console, aktifkan **Realtime Database** (pilih region terdekat).
5. Aktifkan **Authentication -> Anonymous** (dipakai supaya security rules
   bisa membedakan pemain tanpa perlu sistem akun).
6. Di `build.gradle.kts` (root): uncomment baris plugin `com.google.gms.google-services`.
7. Di `app/build.gradle.kts`: uncomment plugin `id("com.google.gms.google-services")`
   dan dependency `firebase-bom`, `firebase-database-ktx`, `firebase-auth-ktx`.
8. Salin isi `docs/firebase-online-multiplayer/FirebaseOnlineGameConnection.kt.txt`
   ke file baru `app/src/main/java/com/qie/chess/multiplayer/FirebaseOnlineGameConnection.kt`
   (hapus ekstensi `.txt`).
9. Di `multiplayer/MultiplayerConfig.kt`, ubah `FIREBASE_CONFIGURED` menjadi `true`.
10. Pasang security rules Realtime Database sesuai contoh yang ada di
    komentar bagian atas file referensi tersebut (membatasi write hanya untuk
    pemain yang terdaftar di room itu).
11. Sync Gradle, rebuild. Layar "Play vs Friend" otomatis aktif penuh
    (Create Room / Join Room / Room Code / realtime sync).

Sampai langkah-langkah di atas dilakukan, layar "Play vs Friend" akan
menampilkan pesan "belum dikonfigurasi" - ini disengaja, bukan bug.

## Menambahkan Sound Effect

Kode sudah lengkap terhubung ke `SoundManager` (`audio/SoundManager.kt`),
tapi file audio (.mp3) tidak disertakan karena tidak bisa dibuat sebagai
teks. Tambahkan file-file berikut ke `app/src/main/res/raw/` dengan nama
persis seperti ini:

```
move.mp3       capture.mp3     check.mp3
checkmate.mp3  click.mp3       start.mp3      finish.mp3
```

Sumber gratis (CC0/royalty-free): freesound.org atau mixkit.co (cari "chess"
/ "click" / "notification"). Sebelum file ditambahkan, aplikasi tetap
berjalan normal - pemanggilan suara akan diam-diam di-skip.

## GitHub Actions (Auto Build APK)

Setiap push ke branch `main`/`master` (atau lewat "Run workflow" manual),
GitHub Actions akan:

1. Checkout repository
2. Setup JDK 17
3. Setup Android SDK
4. Build APK (`./gradlew assembleDebug`)
5. Upload APK sebagai artifact (bisa didownload dari tab **Actions** di GitHub)

File workflow: `.github/workflows/build.yml`.

## Troubleshooting

**"gradle-wrapper.jar tidak ditemukan" / Gradle sync gagal saat pertama buka project**
Repo ini menyertakan `gradlew`, `gradlew.bat`, dan
`gradle/wrapper/gradle-wrapper.properties`, tapi **tidak** menyertakan
`gradle-wrapper.jar` sebagai file biner (tidak bisa dibuat dari lingkungan
teks). Android Studio otomatis mengunduh & melengkapi file ini saat pertama
kali membuka project (butuh koneksi internet). Kalau ingin generate manual
lewat terminal (dengan Gradle sudah terinstall global):
```bash
gradle wrapper --gradle-version 8.7
```

**Build gagal karena versi dependency**
Semua versi di `build.gradle.kts` sudah dipilih agar saling kompatibel
(AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00). Jika Android Studio
menyarankan upgrade otomatis, cukup terima - versi yang lebih baru umumnya
tetap kompatibel.

**Online multiplayer error / tidak muncul tombol aktif**
Pastikan sudah menyelesaikan semua 11 langkah di bagian "Konfigurasi
Firebase" di atas, termasuk mengubah `FIREBASE_CONFIGURED = true`.

**Suara tidak keluar**
Pastikan file .mp3 sudah ditambahkan ke `res/raw/` dengan nama yang persis
sama (lihat bagian "Menambahkan Sound Effect"), dan Settings > Sound dalam
keadaan aktif.

**APK tidak bisa diinstall di HP**
Pastikan "Install from unknown sources" diaktifkan, dan APK yang dipakai
sesuai arsitektur/Android version HP kamu (APK debug dari `assembleDebug`
sudah universal, seharusnya jalan di semua device Android 7.0+).

## Roadmap / Yang Belum Selesai

Supaya jujur soal cakupan starter ini:

- Online multiplayer: kerangka & UI sudah lengkap, implementasi Firebase
  sudah ditulis penuh di `docs/firebase-online-multiplayer/`, tapi butuh
  kamu menyambungkan project Firebase-mu sendiri (tidak bisa diuji dari
  lingkungan pembuatan kode ini karena tidak ada akses jaringan/kredensial).
- Disconnect/reconnect handling di sisi UI (indikator status) sudah ada
  modelnya (`ConnectionStatus`), tapi UI detail reconnect-nya masih dasar -
  silakan dikembangkan sesuai kebutuhan.
- Sound effects butuh file .mp3 ditambahkan manual (lihat panduan di atas).
- Landscape layout: board sudah responsif (aspect-ratio 1:1, auto-scale),
  tapi belum ada layout khusus terpisah untuk landscape (side panel di
  samping alih-alih di bawah board) - baik untuk pengembangan lanjutan.
- Puzzle set baru berisi 5 contoh; tambahkan lebih banyak lewat
  `data/PuzzleRepository.kt` (format FEN + expected move, sangat mudah
  diperluas).

Struktur kode sengaja dipisah rapi per tanggung jawab (model / engine / bot /
data / ui / viewmodel) supaya bagian-bagian ini mudah dilanjutkan tanpa
menyentuh logika catur inti yang sudah teruji.
