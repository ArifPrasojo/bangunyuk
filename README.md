# ⏰ BangunYuk - Smart Mission Alarm

**BangunYuk** adalah aplikasi alarm Android cerdas berbasis Jetpack Compose dan Material 3 yang dirancang khusus untuk memastikan Anda benar-benar bangun dari tempat tidur dengan berbagai pilihan misi interaktif dan verifikasi foto.

---

## 📱 Fitur Utama

1. **📷 Misi Verifikasi Foto Multi-Spot (Custom Spots)**
   - Daftarkan spot foto bangun tidur satu per satu dengan nama kustom (contoh: *Toilet/Kamar Mandi*, *Wastafel Cermin*, *Dispenser Air*, *Dapur*, *Meja Belajar*, dll.).
   - Atur 1 atau lebih spot target dalam satu alarm (misi bertahap).
   - Verifikasi pencocokan gambar berbasis algoritma perbandingan visual (*histogram & edge features*) untuk memastikan keaslian lokasi.

2. **🔢 Misi Matematika (Multi-Tingkat Kesulitan)**
   - Pilihan tingkat kesulitan (Mudah, Sedang, Sulit).
   - Jumlah soal dapat disesuaikan (1 - 10 soal).

3. **📱 Misi Goyang HP (Shake Phone)**
   - Menggunakan sensor akselerometer perangkat.
   - Target goyangan dapat diatur (10 - 100 kali).

4. **🚶 Misi Langkah Kaki (Step Counter)**
   - Menggunakan sensor deteksi langkah (*Step Counter / Sensor Fusion*).
   - Memaksa pengguna berdiri dan berjalan keluar dari kamar tidur.

5. **🧠 Misi Kuis & Ingatan (Memory Tile Quiz)**
   - Tantangan memori visual untuk mengaktifkan fokus otak di pagi hari.

6. **⚡ Misi Kombinasi Bertahap (Multi-Stage Missions)**
   - Menggabungkan beberapa jenis misi sekaligus (contoh: *Matematika* ➡️ *Goyang HP* ➡️ *Foto Toilet*).

7. **🛡️ Fitur Anti-Cheat & Keandalan Alarm**
   - Fullscreen Alarm Intent & Lockscreen Notification.
   - Pengaturan volume maksimal dan getaran kencang.
   - Opsi Snooze fleksibel dengan batas pengulangan.
   - Ringtone kustom dan nada alarm lokal.

---

## 🛠️ Arsitektur & Teknologi

- **Bahasa:** Kotlin (100%)
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Database Lokal:** Room Database (SQLite)
- **Kamera & Image Processing:** AndroidX CameraX & Custom Bitmap Feature Engine
- **Sensor:** Android Sensor Manager (Accelerometer & Step Detector)
- **Alarm Scheduling:** Android `AlarmManager` dengan `ForegroundService` & `Wakelock`

## 📄 Lisensi
Dibuat dengan ❤️ menggunakan Kotlin & Jetpack Compose.
