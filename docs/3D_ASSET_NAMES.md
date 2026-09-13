# 3D animal asset slots

Place final stylized animal renders in `app/src/main/res/drawable-nodpi/` (preferred for bitmap renders) or another drawable resource folder using these exact resource names.

- `animal_chick_3d` — Pintinho
- `animal_rabbit_3d` — Coelho
- `animal_dog_3d` — Cachorro
- `animal_pig_3d` — Porquinho
- `animal_duck_3d` — Pato
- `animal_sheep_3d` — Ovelha
- `animal_goat_3d` — Cabra
- `animal_cow_3d` — Vaca
- `animal_horse_3d` — Cavalo
- `animal_donkey_3d` — Burrinho

Recommended source format: transparent-background WebP or PNG with a square canvas and consistent character scale/padding across the collection.

The UI resolves the 3D resource first and automatically falls back to the current vector art when the 3D resource is absent. This lets assets be introduced one at a time without changing progression or gameplay logic.
