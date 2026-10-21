# NEXO — estrutura do MVP

## Objetivo
Organizador com modo Acadêmico e Pessoal. Os filtros alteram o que aparece na tela, mas o planejamento considera todos os compromissos para não reservar o mesmo horário duas vezes.

## Navegação e funcionalidades
| Destino | Conteúdo implementado | Evolução prevista |
|---|---|---|
| Início | Próxima atividade, três prioridades do dia, totais e adição rápida | Saudação contextual e resumo de tempo livre |
| Agenda | Navegação diária, compromissos dos modos e recorrência semanal | Grade semanal/mensal e destaque visual de conflitos |
| Tarefas | Cadastro, edição, exclusão, conclusão, modo, prioridade, duração, prazo, etapas em texto | Checklist estruturado e filtros adicionais |
| Foco | Sessão de 25 minutos, pausa de 5, pausar/reiniciar e total concluído | Serviço em segundo plano, vínculo da sessão à tarefa e histórico semanal |
| Acadêmico | Cadastro de aulas, provas e livros, recorrência e prazo de devolução | Entidades próprias para disciplinas, professores e empréstimos; múltiplos lembretes |
| Planejador | Sugestões por prazo/prioridade, duração, disponibilidade, pausas, aulas e sono; aviso de sobrecarga | Aplicar sugestões como sessões e replanejamento automático |
| Estatísticas | Totais de tarefas concluídas, vencidas e minutos de foco | Séries e comparativos semanais |
| Configurações | Início/fim do dia, carga diária, modo persistido e acesso aos ajustes de permissões | DataStore, tema escuro e preferências detalhadas |

Barra inferior: Início, Agenda, Tarefas, Foco e Mais. Mais dá acesso às quatro áreas complementares.

## Arquitetura atual
Um módulo Android, organizado por responsabilidades:

- `presentation`: NexoViewModel, estado observável e operações no ciclo de vida.
- `MainActivity.kt`: composição das telas em Jetpack Compose, tema, navegação e formulários.
- `domain`: modelos Task/Block/Plan, interface TaskRepository e Planner sem dependências Android.
- `data`: implementação local do repositório; acesso em Dispatchers.IO; banco Room.
- Integração Android: AlarmReceiver, AlarmService, BootReceiver e Alarms.

Fluxo: Compose → ViewModel → TaskRepository → Room. Alarmes e notificações usam o mesmo armazenamento.

A primeira versão de Room usa um snapshot JSON transacional para preservar os dados do protótipo anterior. A leitura inicial importa os dados antigos de SharedPreferences se o banco ainda não foi preenchido. Próxima migração: tabelas relacionais de tarefas, disciplinas, sessões, empréstimos e recorrências; preservar histórico por migrations e exportar schemas. Preferências ainda usam SharedPreferences. Não há login ou sincronização.

## Planejamento
Sem IA generativa. Ordena tarefas por prazo e depois prioridade, distribui blocos de até 50 minutos e acrescenta pausas de 10 minutos. Respeita início/fim do dia, carga diária, compromissos fixos e aulas semanais, incluindo eventos que atravessam meia-noite. Para tarefas sem prazo, considera horizonte de 14 dias. Para tarefas com prazo, limita a busca a 90 dias para manter o cálculo previsível.

Sugestões são consultivas: não alteram a agenda ou criam alarmes sem uma ação de edição. A sobrecarga representa minutos não alocados, sem recomendar sacrificar o sono. Replanejamento nesta versão é solicitado pelo botão Gerar sugestões.

## Alarmes
AlarmManager para horários exatos escolhidos pelo usuário, condicionado às permissões do aparelho. Serviço de reprodução em primeiro plano mantém o toque por até cinco minutos, com Começar, Adiar e Concluir. WorkManager é uma evolução para trabalho adiável, não substitui o despertador exato. Ao editar, cancela o aviso antigo; ao reiniciar, restaura os futuros não concluídos. Concluir uma recorrência guarda histórico e prepara a próxima semana.

Limitações: um toque ativo por vez; sem tela cheia; Android 26/27 sem repetição contínua; políticas do fabricante e volume dependem do aparelho. Foco exige a tela aberta e não é um despertador.

## Visual
Fundo claro levemente esverdeado, verde petróleo como cor principal, lavanda para Acadêmico, areia para Pessoal, títulos de alto contraste, cartões arredondados e espaçamento generoso. A informação principal aparece antes dos detalhes. Sem dados de demonstração misturados aos dados reais.

## Validação
Testes de domínio cobrem aulas, sono, sobrecarga, recorrência semanal e competição entre modos. Compilação, análise Android e verificação visual no emulador complementam os testes. Testar alarmes reais com tela bloqueada, permissões negadas, edição, soneca, reinicialização e conclusão de recorrência antes de uso cotidiano.

## Referências técnicas
- [Compose compiler](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [Room](https://developer.android.com/jetpack/androidx/releases/room)
- [Alarmes Android](https://developer.android.com/develop/background-work/services/alarms)
