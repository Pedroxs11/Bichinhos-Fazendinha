# Animal audio assets

The game must prefer bundled, real animal recordings in `app/src/main/res/raw/` and remain fully usable offline.

## Required filenames

- `sound_chick` — Pintinho
- `sound_rabbit` — Coelho
- `sound_dog` — Cachorro
- `sound_pig` — Porquinho
- `sound_duck` — Pato
- `sound_sheep` — Ovelha
- `sound_goat` — Cabra
- `sound_cow` — Vaca
- `sound_horse` — Cavalo
- `sound_donkey` — Burrinho
- `sound_chicken` — Galinha (legacy compatibility)

Android accepts common raw audio extensions such as `.mp3`, `.wav` and `.ogg`. The resource name itself must match the names above.

## V1 quality rules

- Use a recognizable recording of the correct species; do not use synthesized approximations as final content.
- Prefer clean clips around 0.5–2.5 seconds with little background noise.
- Avoid music, speech, watermarks or other animals mixed into the clip.
- Normalize volume so switching between animals does not produce large jumps.
- Trim long silence from the beginning and end.
- Keep the app offline: final playback must come from the APK, not a remote URL.
- Only ship audio that we have the right to redistribute in the app. Record the source and license before adding each clip.

## Runtime behavior

`AnimalSoundPlayer.kt` resolves the bundled raw resource first. If a required file is still missing during development, the old synthesized cue is used only as a temporary fallback so gameplay does not break.

Repeated fast taps are debounced and a new animal sound stops the previous one, preventing overlapping audio in the Sounds quiz and animal cards.
