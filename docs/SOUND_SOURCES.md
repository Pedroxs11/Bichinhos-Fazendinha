# Fontes de áudio dos bichinhos

Objetivo: substituir os sons sintetizados por gravações reais, curtas e reconhecíveis, com licença segura para distribuição comercial.

## Fontes já selecionadas

- Cachorro — `George vuf 1996.ogg` — Wikimedia Commons — domínio público.
- Cavalo — `Wiehern.ogg` — Wikimedia Commons — domínio público.
- Porquinho — `Mudchute pig 1.ogg` — Wikimedia Commons — gravação curta de porco.
- Ovelha — `Sheep bleat.ogg` — Wikimedia Commons — CC0 1.0.

## Critérios

1. Preferir domínio público ou CC0.
2. Usar clipes curtos e claros, sem música ou voz humana.
3. Evitar arquivos com ruído excessivo de fundo.
4. Manter o áudio local no APK para funcionar offline.
5. Nomear os recursos finais como `sound_<animal>` em `app/src/main/res/raw/`.

## Nomes esperados no app

- `sound_chick`
- `sound_rabbit`
- `sound_dog`
- `sound_pig`
- `sound_duck`
- `sound_sheep`
- `sound_goat`
- `sound_cow`
- `sound_horse`
- `sound_donkey`

Observação: até o arquivo real existir, o código atual pode usar fallback temporário. O objetivo final é remover a dependência dos sons sintetizados para os animais com gravação real disponível.
