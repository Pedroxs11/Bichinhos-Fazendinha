# Fábrica de Jogos — Base Minha Fazendinha

A Fazendinha agora funciona como projeto-base para os próximos jogos infantis.

## O que já é reutilizável

- Navegação principal e botão voltar
- HUD de estrelas e corações
- Progresso persistente via SharedPreferences
- Sistema de desbloqueio por estrelas
- Recompensas configuráveis
- Lista de personagens/animais configurável
- Lista de acessórios configurável
- Player de sons com proteção contra sobreposição
- Tela de sons
- Sequência de cuidados
- Minigames por etapas e toques
- Área bônus desbloqueável
- Armário/personagem
- Build automático de APK no GitHub Actions

## Arquivo central

`app/src/main/java/com/minhafazendinha/game/GameConfig.kt`

Para criar um novo jogo a partir desta base, começar alterando:

1. `TITLE` e `SUBTITLE`
2. personagens em `animals`
3. acessórios em `accessories`
4. recompensas
5. quantidade de estrelas para desbloqueios
6. arte, textos, sons e regras específicas do tema

## Regra de produção rápida

A lógica comum deve permanecer na base. Evitar copiar regras específicas para várias telas. Tudo que variar entre jogos deve migrar gradualmente para `GameConfig` ou componentes reutilizáveis.

Objetivo de produção: primeiro clone em até 3 dias; depois reduzir o processo para aproximadamente 1 jogo por dia quando a base estiver madura.

## Checklist de um novo jogo

- Definir tema e nome
- Trocar elenco
- Trocar textos
- Trocar sons
- Trocar cenários/arte
- Ajustar recompensas e desbloqueios
- Rodar build
- Testar fluxo completo no aparelho
- Gerar APK de teste
- Fazer polimento visual final

## Estado atual da Fazendinha

A versão 0.6.0 é o marco funcional usado para consolidar essa arquitetura. O polimento visual profissional permanece separado da lógica para não atrasar a construção da base.
