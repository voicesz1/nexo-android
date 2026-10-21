# NEXO

Organizador Android para estudos e vida pessoal. Interface em Kotlin e Jetpack Compose, estado com ViewModel, armazenamento local Room e alarmes com soneca.

## Abrir
No Android Studio, abra esta pasta, selecione um JDK 17 em **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** e sincronize. O projeto usa Gradle 8.11.1 e SDK Android 35. Os caminhos locais do SDK e do Java não são compartilhados no repositório.

## Áreas
Início, Agenda, Tarefas, Foco, Acadêmico, Planejador, Estatísticas e Configurações. A barra inferior contém cinco destinos; as quatro áreas complementares estão em Mais.

O planejador sugere blocos de estudo e tarefas respeitando compromissos, aulas semanais, pausas, descanso e disponibilidade diária. As sugestões não alteram automaticamente os alarmes.

Veja `docs/MVP-NEXO.md` para arquitetura, funcionalidades atuais e próximas etapas. Dados antigos são importados ao inicializar o banco local.

## Executar e verificar
Use Run no Android Studio. Pelo Gradle Wrapper: `assembleDebug`, `testDebugUnitTest` e `lintDebug`. Instalador de desenvolvimento em `app/build/outputs/apk/debug/app-debug.apk`.

Autorize notificações e alarmes exatos no aparelho. O despertador usa som por até cinco minutos, com Começar, Adiar dez minutos e Concluir. Apenas um alarme toca por vez. Sem tela cheia; volume e comportamento dependem do Android. Foco exige manter sua tela aberta.

Antes de usar no dia a dia, verificar alarmes com tela bloqueada, edição cancelando aviso anterior, soneca, permissão negada, conclusão semanal e reinicialização. Agenda mensal, checklist estruturado, sincronização, relatórios semanais e aplicação automática do planejamento são evoluções previstas.
