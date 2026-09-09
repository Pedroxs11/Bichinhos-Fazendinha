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
- Armário

### Atividades planejadas
- Ouvir sons dos bichos
- Alimentar
- Dar banho
- Secar
- Colocar para dormir
- Regar planta
- Colher fruta
- Pegar ovos
- Vestir os bichinhos
- Liberar roupas temporárias por anúncio premiado

## Progressão
A criança poderá ganhar estrelas e, futuramente, moedas, acessórios, comidas, itens e novos bichinhos.

## Monetização planejada
- Jogo grátis
- Anúncios leves entre blocos de atividades
- Sem anúncios interrompendo ações infantis
- Rewarded ad opcional para liberar roupa especial por 24 horas
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

### Cuidar — já funcional
A área `Cuidar` já abre uma tela própria e possui uma rotina sequencial com:
1. alimentar o bichinho
2. dar banho
3. secar
4. colocar para dormir

A criança acompanha uma barra de progresso e recebe 5 estrelas somente após concluir toda a rotina.

### Sons — já funcional
A área `Sons` já possui duas formas de brincar:
1. exploração livre: tocar em vaca, porquinho, galinha ou cachorro para descobrir a onomatopeia do som
2. quiz: o jogo apresenta um som e a criança escolhe qual bichinho corresponde

O quiz tem 4 rodadas, permite tentar novamente sem punição e entrega 3 estrelas após a conclusão.

> Nesta etapa os sons são representados por texto/onomatopeias. Arquivos de áudio reais serão adicionados depois com conteúdo próprio ou devidamente licenciado.

### Armário — funcional
O app já possui uma entrada para o Armário com:
- roupas permanentes de exemplo
- seleção de visual
- roupa especial `Realeza` bloqueada
- botão que simula um anúncio premiado
- liberação da roupa especial por 24 horas
- indicação de horas e minutos restantes
- prazo salvo localmente no aparelho com `SharedPreferences`
- expiração automática após 24 horas
- novo bloqueio após expirar, exigindo novo anúncio para liberar novamente

O prazo continua válido mesmo se o app for fechado e aberto novamente. Quando o AdMob for integrado, a liberação deverá acontecer somente após o callback de recompensa confirmado.

## Próximas etapas
1. Implementar tarefas da Fazendinha: regar, colher e pegar ovos
2. Evoluir as ações de cuidado para interações por toque/arraste
3. Persistir estrelas e roupa atualmente vestida localmente
4. Adicionar áudio real aos bichinhos
5. Criar identidade visual e personagens próprios
6. Criar sistema de moedas, estrelas e desbloqueios
7. Integrar rewarded ads apropriados ao público infantil
8. Preparar controles parentais e monetização adequada ao público infantil
9. Testar e gerar a primeira versão para Android
