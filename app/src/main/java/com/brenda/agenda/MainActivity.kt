package com.brenda.agenda

import android.os.Bundle
import android.app.*
import android.content.*
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brenda.agenda.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.random.Random

private val Ink=Color(0xFF192C38)
private val Teal=Color(0xFF166B60)
private val Cream=Color(0xFFF5F6F2)
private fun stamp(time:Long)=Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM • HH:mm"))

class MainActivity:ComponentActivity(){
 private val model:NexoViewModel by viewModels()
 private var permissionsGranted by mutableStateOf(false)
 override fun onCreate(state:Bundle?){super.onCreate(state);androidx.core.view.WindowCompat.getInsetsController(window,window.decorView).isAppearanceLightStatusBars=true;if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"),1)
  setContent{MaterialTheme(colorScheme=lightColorScheme(primary=Teal,background=Cream,surface=Color.White,onSurface=Ink),typography=Typography()){Nexo(model)}}}
 override fun onResume(){super.onResume();permissionsGranted=getSystemService(NotificationManager::class.java).areNotificationsEnabled()&&(android.os.Build.VERSION.SDK_INT<31||getSystemService(AlarmManager::class.java).canScheduleExactAlarms());model.refresh()}
 fun date(initial:Long, result:(Long)->Unit){val c=Calendar.getInstance().apply{timeInMillis=initial};DatePickerDialog(this,{_,y,m,d->c.set(y,m,d);TimePickerDialog(this,{_,h,min->c.set(Calendar.HOUR_OF_DAY,h);c.set(Calendar.MINUTE,min);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);result(c.timeInMillis)},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show()},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}
 @Composable fun Nexo(vm:NexoViewModel){
  val tasks by vm.tasks.collectAsState();val error by vm.error.collectAsState();val mode by vm.mode.collectAsState()
  var page by remember{mutableStateOf("Início")};var editor by remember{mutableStateOf<Task?>(null)};var editing by remember{mutableStateOf(false)};var selected by remember{mutableStateOf<Task?>(null)}
  val allowed=permissionsGranted;var offset by remember{mutableStateOf(0)};var showDone by remember{mutableStateOf(false)}

  val visible=tasks.filter{mode=="Todos"||it.mode==mode}
  Scaffold(containerColor=Cream,bottomBar={NavigationBar(containerColor=Color.White){listOf("Início","Agenda","Tarefas","Foco","Mais").forEachIndexed{index,name->NavigationBarItem(selected=page==name,onClick={page=name},icon={Text(listOf("◈","▦","✓","◷","•••")[index],fontSize=22.sp)},label={Text(name)})}}},floatingActionButton={if(page!="Foco")FloatingActionButton(onClick={editor=null;editing=true},containerColor=Teal,contentColor=Color.White){Text("+",fontSize=28.sp)}}){padding->
   LazyColumn(Modifier.padding(padding).fillMaxSize(),contentPadding=PaddingValues(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
    item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("NEXO",letterSpacing=4.sp,fontWeight=FontWeight.Black,color=Teal,fontSize=16.sp);Text(if(page=="Início")"Seu tempo, com calma." else page,fontWeight=FontWeight.Bold,fontSize=28.sp,color=Ink)};Surface(shape=RoundedCornerShape(16.dp),color=Color(0xFFE2EDE6)){Text("N",Modifier.padding(15.dp),color=Teal,fontWeight=FontWeight.Bold)}}}
    item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Todos","Acadêmico","Pessoal").forEach{FilterChip(selected=mode==it,onClick={vm.setMode(it)},label={Text(it)})}}}
    if(!allowed)item{Panel("Ative seus lembretes","Receba seus avisos no horário combinado."){TextButton(onClick={if(android.os.Build.VERSION.SDK_INT>=31&&!getSystemService(AlarmManager::class.java).canScheduleExactAlarms())startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:$packageName")))else startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,packageName))}){Text("Abrir ajustes")}}}
    if(error!=null)item{Text(error!!,color=MaterialTheme.colorScheme.error);TextButton(onClick={vm.refresh()}){Text("Tentar novamente")}}
    when(page){
     "Início"->{
      val pending=visible.filter{!it.done}.sortedBy{it.time};val today=LocalDate.now();val day=pending.filter{Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate()<=today}
      item{Surface(color=Teal,shape=RoundedCornerShape(28.dp)){Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("UM PASSO DE CADA VEZ",color=Color(0xFFBFE5D7),fontSize=12.sp,letterSpacing=2.sp);Text(if(day.isEmpty())"Espaço para respirar." else "${day.size} atividades no seu dia",fontSize=27.sp,color=Color.White,fontWeight=FontWeight.Bold);Text(pending.firstOrNull()?.let{"A seguir: ${it.title}\n${stamp(it.time)}"}?:"Organize sua rotina sem abrir mão do descanso.",color=Color.White);TextButton(onClick={page="Planejador"},colors=ButtonDefaults.textButtonColors(contentColor=Color.White)){Text("Encontrar um horário livre →")}}}}
      item{Text("Suas prioridades",fontSize=20.sp,fontWeight=FontWeight.Bold)}
      if(day.isEmpty())item{Panel("Dia leve","Adicione um compromisso ou aproveite para descansar.")}
      day.take(3).forEach{t->item{TaskCard(t){selected=t}}}
      item{Panel("Seu progresso","${visible.count{it.done}} atividades concluídas • ${visible.count{!it.done}} em aberto")}
     }
     "Agenda"->{
      val day=LocalDate.now().plusDays(offset.toLong())
      item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){TextButton(onClick={offset--}){Text("←")};Text(day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),Modifier.padding(top=12.dp));TextButton(onClick={offset++}){Text("→")}}}
      val agenda=visible.filter{!it.done&&(Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate()==day || (it.weekly && !day.isBefore(Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate())&&Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).dayOfWeek==day.dayOfWeek))}.sortedBy{Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalTime()}
      if(agenda.isEmpty())item{Panel("Agenda livre","Não há compromissos neste dia.")};agenda.forEach{t->item{TaskCard(t){selected=t}}}
      item{Text("A agenda reúne os dois modos. O planejador considera todos os compromissos, mesmo quando você filtra a tela.",color=Color.Gray,fontSize=13.sp)}
     }
     "Tarefas"->{

      item{Row(verticalAlignment=Alignment.CenterVertically){Switch(showDone,{showDone=it});Text("Mostrar concluídas",Modifier.padding(start=8.dp))}}
      val filtered=visible.filter{it.done==showDone}.sortedWith(compareBy<Task>{if(it.deadline>0)it.deadline else Long.MAX_VALUE}.thenByDescending{it.priority})
      if(filtered.isEmpty())item{Panel("Tudo em ordem","Nenhuma atividade nesta seleção.")};filtered.forEach{t->item{TaskCard(t){selected=t}}}
     }
     "Foco"->item{Focus(vm)}
     "Mais"->listOf("Acadêmico" to "Disciplinas, aulas, avaliações e biblioteca","Planejador" to "Encontre espaço para o que importa","Estatísticas" to "Acompanhe seu progresso","Configurações" to "Sono, disponibilidade e modos").forEach{(title,body)->item{Panel(title,body){TextButton(onClick={page=title}){Text("Abrir →")}}}}
     "Acadêmico"->{
      item{Panel("Sua vida acadêmica","Use aulas semanais para reservar sua grade. Cadastre provas e empréstimos com seus horários de aviso.")}
      item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Aula","Prova","Livro").forEach{kind->OutlinedButton(onClick={editor=Task(Random.nextInt(1,Int.MAX_VALUE),"",System.currentTimeMillis()+3600000,mode="Acadêmico",category=kind);editing=true}){Text("+ $kind")}}}}
      tasks.filter{it.mode=="Acadêmico"&&!it.done}.sortedBy{it.time}.forEach{t->item{TaskCard(t){selected=t}}}
     }
     "Planejador"->item{PlannerPanel(vm,tasks)}
     "Estatísticas"->item{val focus by vm.focusMinutes.collectAsState();Panel("Seu progresso, sem pressão","${tasks.count{it.done}} atividades concluídas\n${tasks.count{!it.done&&it.deadline>0&&it.deadline<System.currentTimeMillis()}} prazos vencidos\n$focus minutos de foco concluídos\n\nTotais desde o início do uso.")}
     "Configurações"->item{Preferences(vm)}
    }
    item{Spacer(Modifier.height(72.dp))}
   }
  }
  if(editing)Editor(editor,onClose={editing=false},onSave={vm.save(it);editing=false})
  selected?.let{t->AlertDialog(onDismissRequest={selected=null},title={Text(t.title)},text={Column{Text("${t.mode} • ${t.category}\n${stamp(t.time)}\n${t.minutes} minutos" );if(t.steps.isNotBlank())Text("\nEtapas\n${t.steps}")
   if(!t.done){TextButton(onClick={stopService(Intent(this@MainActivity,AlarmService::class.java));vm.start(t);page="Foco";selected=null}){Text("Começar e abrir foco")};TextButton(onClick={stopService(Intent(this@MainActivity,AlarmService::class.java));vm.snooze(t);selected=null}){Text("Adiar 10 minutos")};TextButton(onClick={stopService(Intent(this@MainActivity,AlarmService::class.java));vm.complete(t);selected=null}){Text("Concluir")}}
   TextButton(onClick={editor=t;editing=true;selected=null}){Text("Editar")};TextButton(onClick={vm.delete(t);selected=null}){Text("Excluir")}}},confirmButton={TextButton(onClick={selected=null}){Text("Fechar")}})}
 }
 @Composable fun Panel(title:String,body:String,content:@Composable ()->Unit={}){Surface(shape=RoundedCornerShape(24.dp),color=Color.White,modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,fontWeight=FontWeight.Bold,fontSize=19.sp);Text(body,color=Color(0xFF65726F),lineHeight=23.sp);content()}}}
 @Composable fun TaskCard(t:Task,click:()->Unit){Surface(onClick=click,shape=RoundedCornerShape(22.dp),color=Color.White,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)){Surface(color=if(t.mode=="Acadêmico")Color(0xFFE8E5F6) else Color(0xFFF9ECD9),shape=RoundedCornerShape(12.dp)){Text(if(t.done)"✓" else if(t.mode=="Acadêmico")"A" else "P",Modifier.padding(12.dp),fontWeight=FontWeight.Bold)};Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Text(t.title,fontWeight=FontWeight.SemiBold,fontSize=17.sp);Text("${t.category} • ${stamp(t.time)}",fontSize=13.sp,color=Color.Gray);if(t.deadline>0)Text("Prazo: ${stamp(t.deadline)}",fontSize=12.sp,color=Teal);if(t.weekly)Text("Toda semana",fontSize=12.sp,color=Teal)}}}}
 @Composable fun Editor(old:Task?,onClose:()->Unit,onSave:(Task)->Unit){
  var title by remember{mutableStateOf(old?.title?:"")};var mode by remember{mutableStateOf(old?.mode?:"Acadêmico")};var kind by remember{mutableStateOf(old?.category?:"Tarefa")};var minutes by remember{mutableStateOf((old?.minutes?:60).toString())};var priority by remember{mutableStateOf(old?.priority?:2)};var time by remember{mutableStateOf(old?.time?:System.currentTimeMillis()+3600000)};var deadline by remember{mutableStateOf(old?.deadline?:0L)};var weekly by remember{mutableStateOf(old?.weekly?:false)};var steps by remember{mutableStateOf(old?.steps?:"")};var validation by remember{mutableStateOf("")}
  AlertDialog(onDismissRequest=onClose,title={Text(if(old==null)"Novo compromisso" else "Editar compromisso")},text={Column(Modifier.heightIn(max=490.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
   OutlinedTextField(title,{title=it},label={Text("Título ou disciplina")},singleLine=true)
   Row{listOf("Acadêmico","Pessoal").forEach{FilterChip(mode==it,{mode=it},label={Text(it)})}}
   Text("Tipo de atividade",fontWeight=FontWeight.Bold);Row(Modifier.horizontalScroll(rememberScrollState())){listOf("Tarefa","Aula","Prova","Livro","Compromisso","Descanso").forEach{FilterChip(kind==it,{kind=it},label={Text(it)})}}
   OutlinedTextField(minutes,{minutes=it.filter(Char::isDigit)},label={Text("Duração estimada em minutos")},singleLine=true)
   TextButton(onClick={date(time){time=it}}){Text("Alarme / início: ${stamp(time)}")}
   TextButton(onClick={date(if(deadline>0)deadline else time+86400000){deadline=it}}){Text(if(deadline==0L)"+ Prazo de entrega ou devolução" else "Prazo: ${stamp(deadline)}")}
   if(deadline>0)TextButton(onClick={deadline=0}){Text("Remover prazo")}
   Row(verticalAlignment=Alignment.CenterVertically){Checkbox(weekly,{weekly=it});Text("Repetir semanalmente")}
   Row{listOf(1 to "Baixa",2 to "Média",3 to "Alta").forEach{(p,label)->FilterChip(priority==p,{priority=p},label={Text(label)})}}
   OutlinedTextField(steps,{steps=it},label={Text("Etapas, professor ou observações")},minLines=2)
   Text("O aviso toca no início. Para livros, escolha um aviso antes da devolução.",fontSize=12.sp)
   if(validation.isNotEmpty())Text(validation,color=MaterialTheme.colorScheme.error)
  }},confirmButton={TextButton(onClick={val duration=minutes.toIntOrNull();validation=when{title.isBlank()->"Informe o título.";duration==null||duration !in 1..1440->"Use uma duração entre 1 e 1440 minutos.";time<=System.currentTimeMillis()->"Escolha um horário futuro.";deadline>0&&deadline<time->"O prazo precisa ser depois do início.";else->""};if(validation.isEmpty())onSave(Task(old?.id?:Random.nextInt(1,Int.MAX_VALUE),title.trim(),time,deadline,mode,kind,duration!!,priority,weekly,steps=steps))}){Text("Salvar")}},dismissButton={TextButton(onClick=onClose){Text("Cancelar")}})
 }
 @Composable fun PlannerPanel(vm:NexoViewModel,tasks:List<Task>){var plan by remember{mutableStateOf<Plan?>(null)};val scope=rememberCoroutineScope();Panel("Planejador inteligente","Blocos de até 50 minutos, com pausas. Aulas, compromissos e descanso ficam reservados. Analisa os dois modos juntos."){
  Button(onClick={scope.launch{plan=vm.plan()}}){Text("Gerar sugestões")}
  plan?.let{p->Text(if(p.missingMinutes>0)"Sobrecarga: faltam ${p.missingMinutes} minutos disponíveis antes dos prazos." else "As atividades cabem na disponibilidade informada.",color=if(p.missingMinutes>0)MaterialTheme.colorScheme.error else Teal)
   if(p.blocks.isEmpty())Text("Cadastre tarefas com duração e prazo para planejar.")
   p.blocks.take(20).forEach{b->Text("${stamp(b.start)} · ${b.minutes} min\n${b.title}",Modifier.padding(vertical=8.dp))}
   Text("Sugestões não criam alarmes automaticamente. Ajuste o início da tarefa para reservar um dos horários. Gere novamente quando sua rotina mudar.",fontSize=12.sp)
  }
 }}
 @Composable fun Focus(vm:NexoViewModel){var remaining by rememberSaveableCompat{mutableStateOf(25*60)};var running by rememberSaveableCompat{mutableStateOf(false)};var pause by rememberSaveableCompat{mutableStateOf(false)}
  LaunchedEffect(running){while(running){delay(1000);remaining--;if(remaining<=0){running=false;if(!pause)vm.recordFocus();pause=!pause;remaining=if(pause)5*60 else 25*60}}}
  Panel(if(pause)"Uma pausa merecida" else "Uma coisa de cada vez","25 minutos de foco. Depois, cinco minutos para respirar."){
   Text("%02d:%02d".format(remaining/60,remaining%60),fontSize=64.sp,fontWeight=FontWeight.Light,color=Teal,modifier=Modifier.padding(vertical=28.dp))
   Button(onClick={running=!running}){Text(if(running)"Pausar" else "Iniciar")};TextButton(onClick={running=false;pause=false;remaining=25*60}){Text("Reiniciar")};Text("Mantenha esta tela aberta durante a sessão. O cronômetro desta versão não envia aviso em segundo plano.",fontSize=12.sp)
  }
 }
 @Composable fun Preferences(vm:NexoViewModel){val wake by vm.wake.collectAsState();val sleep by vm.sleep.collectAsState();val budget by vm.budget.collectAsState();Panel("Sua rotina vem primeiro","O planejamento preserva a noite e limita a carga diária."){
  Text("Começar o dia: ${wake}h");Slider(wake.toFloat(),{vm.settings(it.toInt(),sleep,budget)},valueRange=5f..12f,steps=6)
  Text("Encerrar o dia: ${sleep}h");Slider(sleep.toFloat(),{vm.settings(wake,it.toInt(),budget)},valueRange=18f..23f,steps=4)
  Text("Tempo para tarefas: $budget minutos por dia");Slider(budget.toFloat(),{vm.settings(wake,sleep,(it.toInt()/30)*30)},valueRange=30f..240f,steps=6)
  Text("Seletor de modos no topo das telas. Seus dados ficam neste aparelho.",fontSize=13.sp)
 }}
}
@Composable private fun <T> rememberSaveableCompat(calculation:()->T):T=remember(calculation=calculation)
