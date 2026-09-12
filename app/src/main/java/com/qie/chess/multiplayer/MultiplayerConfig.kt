package com.qie.chess.multiplayer

/**
 * Online "Play vs Friend" needs a Firebase Realtime Database backend, which
 * requires YOUR OWN free Firebase project (so no real credentials ship in
 * this source tree - see README.md -> "Konfigurasi Firebase / Online Multiplayer").
 *
 * Steps to enable:
 *  1. Create a free Firebase project and a Realtime Database.
 *  2. Download google-services.json into app/google-services.json.
 *  3. Uncomment the Firebase plugin/dependencies in build.gradle.kts (root + app).
 *  4. Copy multiplayer/reference/FirebaseOnlineGameConnection.kt.txt into this
 *     package as FirebaseOnlineGameConnection.kt and remove the .txt extension.
 *  5. Flip FIREBASE_CONFIGURED to true below.
 *
 * Until then, the app runs fully offline (VS Bot, Local 2-Player, Puzzles,
 * History, Settings all work with zero configuration) and the "Play vs
 * Friend" screen shows a friendly "not configured yet" message instead of
 * crashing.
 */
object MultiplayerConfig {
    const val FIREBASE_CONFIGURED = false
}
