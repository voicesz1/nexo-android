package com.brenda.agenda.domain

import java.time.*

data class Task(val id:Int, val title:String, val time:Long, val deadline:Long=0, val mode:String="Acadêmico", val category:String="Tarefa", val minutes:Int=60, val priority:Int=2, val weekly:Boolean=false, val started:Boolean=false, val done:Boolean=false, val steps:String="")
data class Block(val taskId:Int,val title:String,val start:Long,val minutes:Int)
data class Plan(val blocks:List<Block>,val missingMinutes:Int)
interface TaskRepository { suspend fun load():List<Task>; suspend fun save(task:Task); suspend fun complete(id:Int); suspend fun delete(id:Int); suspend fun snooze(id:Int) }

/** Deterministic earliest-deadline allocation; fixed appointments and sleep stay reserved. */
class Planner {
 fun create(tasks:List<Task>, now:Long, wakeHour:Int=8, sleepHour:Int=22, dailyMinutes:Int=120, zone:ZoneId=ZoneId.systemDefault()):Plan {
  val fixed=tasks.filter{!it.done && it.category in listOf("Aula","Prova","Compromisso","Descanso")}
  val work=tasks.filter{!it.done && it.category !in listOf("Aula","Prova","Compromisso","Descanso","Livro")}.sortedWith(compareBy<Task>{if(it.deadline>0)it.deadline else Long.MAX_VALUE}.thenByDescending{it.priority})
  val blocks=mutableListOf<Block>();val used=mutableMapOf<LocalDate,Int>();var missing=0
  for(task in work){
   var remaining=task.minutes;var cursor=Instant.ofEpochMilli(maxOf(now,task.time)).atZone(zone).withSecond(0).withNano(0)
   if(cursor.toInstant().toEpochMilli()<maxOf(now,task.time))cursor=cursor.plusMinutes(1)
   val horizon=Instant.ofEpochMilli(now).plusSeconds(90*86400)
   val end=if(task.deadline>0)minOf(Instant.ofEpochMilli(task.deadline),horizon) else Instant.ofEpochMilli(now).plusSeconds(14*86400)
   while(remaining>0 && cursor.toInstant()<end){
    if(cursor.hour<wakeHour){cursor=cursor.toLocalDate().atTime(wakeHour,0).atZone(zone);continue}
    if(cursor.hour>=sleepHour || (used[cursor.toLocalDate()]?:0)>=dailyMinutes){cursor=cursor.toLocalDate().plusDays(1).atTime(wakeHour,0).atZone(zone);continue}
    val size=minOf(remaining,50,dailyMinutes-(used[cursor.toLocalDate()]?:0));val start=cursor.toInstant().toEpochMilli();val finish=start+size*60000L
    val busy=fixed.any{overlap(it,start,finish,zone)}||blocks.any{start<it.start+it.minutes*60000L+10*60000L&&finish>it.start}
    if(!busy && finish<=end.toEpochMilli() && !cursor.plusMinutes(size.toLong()).toLocalTime().isAfter(LocalTime.of(sleepHour,0))){blocks+=Block(task.id,task.title,start,size);remaining-=size;used[cursor.toLocalDate()]=(used[cursor.toLocalDate()]?:0)+size;cursor=cursor.plusMinutes(size+10L)}else cursor=cursor.plusMinutes(10)
   };missing+=remaining
  };return Plan(blocks,missing)
 }
 private fun overlap(task:Task,start:Long,end:Long,zone:ZoneId):Boolean {
  if(!task.weekly)return start<task.time+task.minutes*60000L && end>task.time
  val original=Instant.ofEpochMilli(task.time).atZone(zone);val day=Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
  return (-1L..0L).any{offset->val date=day.plusDays(offset);val occurrence=date.atTime(original.toLocalTime()).atZone(zone).toInstant().toEpochMilli();date.dayOfWeek==original.dayOfWeek&&occurrence>=task.time&&start<occurrence+task.minutes*60000L&&end>occurrence}
 }
}
