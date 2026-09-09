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

### Atividades da V1
- Ouvir sons dos bichos
- Alimentar
- Dar banho
- Secar
- Colocar para dormir
- Jogar bola com o bichinho
- Regar planta
- Colher fruta
- Pegar ovos
- Vestir os bichinhos
- Liberar roupas temporárias por anúncio premiado

## Progressão
A criança ganha estrelas ao concluir atividades completas. O saldo de estrelas fica salvo localmente no aparelho para continuar disponível mesmo após fechar e abrir o jogo.

Futuramente, a progressão poderá incluir moedas, acessórios, comidas, itens e novos bichinhos.

## Monetização planejada
- Jogo grátis
- Anúncios leves entre blocos de atividades
- Sem anúncios interrompendo ações infantis
- Rewarded ad opcional para liberar roupa especial por 24 horas
- Opção futura de remover anúncios por compra única

> Antes da publicação, a monetização deverá ser configurada de acordo com as políticas da Google Play para conteúdo direcionado a crianças.

## Estado atual
A branch `feat/v1-base` já contém a estrutura Android em Kotlin + Jetpack Compose e uma tela inicial infantil visual, com:
- cenário de céu e campo desenhado em Compose
- seleção dos quatro bichinhos em cards grandes
- destaque visual do bichinho escolhido
- balão de mensagem do personagem
- contador de estrelas persistente
- áreas Sons, Cuidar, Brincar e Fazendinha em cards coloridos
- botões grandes pensados para toque infantil

### Cuidar — funcional
A área `Cuidar` possui uma rotina sequencial com:
1. alimentar o bichinho
2. dar banho
3. secar
4. colocar para dormir

A criança acompanha a barra de progresso e recebe 5 estrelas somente após concluir toda a rotina.

### Sons — funcional
A área `Sons` possui duas formas de brincar:
1. exploração livre: tocar em vaca, porquinho, galinha ou cachorro para descobrir a onomatopeia do som
2. quiz: o jogo apresenta um som e a criança escolhe qual bichinho corresponde

O quiz tem 4 rodadas, permite tentar novamente sem punição e entrega 3 estrelas após a conclusão.

> Nesta etapa os sons são representados por texto/onomatopeias. Arquivos de áudio reais serão adicionados depois com conteúdo próprio ou devidamente licenciado.

### Brincar — funcional
A área `Brincar` agora possui um minijogo simples de bola:
- a criança toca para jogar a bola com o bichinho
- são 5 jogadas por rodada
- há barra de progresso e mensagens de incentivo
- a recompensa é entregue somente no final
- conclusão da rodada rende 2 estrelas

### Fazendinha — funcional
A área `Fazendinha` possui três tarefas sequenciais:
1. regar a horta
2. colher frutas
3. pegar ovos

A criança acompanha a barra de progresso, recebe feedback visual em cada tarefa e ganha 4 estrelas somente após completar as três atividades.

### Armário — funcional
O app já possui uma entrada para o Armário com:
- roupas permanentes de exemplo
- seleção de visual
- roupa especial `Realeza` bloqueada
- botão que simula um anúncio premiado
- liberação da roupa especial por 24 horas
- indicação de horas e minutos restantes
- prazo salvo localmente no aparelho com `SharedPreferences`
- roupa atualmente equipada salva localmente
- reabertura do jogo mantendo a roupa escolhida
- expiração automática da Realeza após 24 horas
- retorno automático ao visual padrão se a roupa temporária expirar equipada
- novo bloqueio após expirar, exigindo novo anúncio para liberar novamente

O prazo e a roupa equipada continuam válidos mesmo se o app for fechado e aberto novamente. Quando o AdMob for integrado, a liberação deverá acontecer somente após o callback de recompensa confirmado.

## Persistência atual
Já ficam salvos localmente:
- saldo de estrelas
- roupa equipada
- horário de desbloqueio da roupa temporária

## Próximas etapas
1. Evoluir as ações de cuidado, brincadeira e fazendinha para interações por toque/arraste e animações
2. Adicionar áudio real aos bichinhos
3. Criar identidade visual e personagens próprios inspirados no mockup aprovado
4. Criar sistema de moedas, estrelas e desbloqueios mais completo
5. Integrar rewarded ads apropriados ao público infantil
6. Preparar controles parentais e monetização adequada ao público infantil
7. Testar em aparelho real e corrigir problemas de usabilidade
8. Gerar a primeira versão Android para testes
