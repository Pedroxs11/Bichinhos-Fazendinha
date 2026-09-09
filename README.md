# Bichinhos & Fazendinha

Jogo Android infantil para crianças pequenas, com bichinhos, cuidados, sons e atividades de fazendinha.

## Objetivo da V1

Criar uma experiência simples, colorida e com pouco texto, preparada para publicação global.

### Bichos iniciais
- Vaca
- Porquinho
- Galinha
- Cachorro

### Áreas principais
- Sons
- Cuidar
- Brincar
- Fazendinha

### Atividades planejadas
- Ouvir sons dos bichos
- Alimentar
- Dar banho
- Secar
- Colocar para dormir
- Regar planta
- Colher fruta
- Pegar ovos

## Progressão
A criança poderá ganhar estrelas e, futuramente, moedas, acessórios, comidas, itens e novos bichinhos.

## Monetização planejada
- Jogo grátis
- Anúncios leves entre blocos de atividades
- Sem anúncios interrompendo ações infantis
- Opção futura de remover anúncios por compra única

> Antes da publicação, a monetização deverá ser configurada de acordo com as políticas da Google Play para conteúdo direcionado a crianças.

## Estado atual
A branch `feat/v1-base` já contém a primeira estrutura Android em Kotlin + Jetpack Compose e uma tela inicial infantil mais visual, com:
- cenário de céu e campo desenhado em Compose
- seleção dos quatro bichinhos em cards grandes
- destaque visual do bichinho escolhido
- balão de mensagem do personagem
- contador de estrelas
- áreas Sons, Cuidar, Brincar e Fazendinha em cards coloridos
- botões grandes pensados para toque infantil

## Próximas etapas
1. Transformar `Cuidar` em uma atividade real: alimentar, banho, secar e dormir
2. Implementar a atividade de Sons
3. Implementar tarefas da Fazendinha: regar, colher e pegar ovos
4. Criar identidade visual e personagens próprios
5. Adicionar sons originais/licenciados
6. Criar sistema de moedas, estrelas e desbloqueios
7. Salvar progresso localmente
8. Preparar controles parentais e monetização adequada ao público infantil
9. Testar e gerar a primeira versão para Android
