package com.brenda.agenda.domain
import org.junit.Assert.*
import org.junit.Test
import java.time.*
class PlannerTest {
 private val zone=ZoneId.of("America/Sao_Paulo")
 private fun time(day:Int,hour:Int)=LocalDate.of(2026,10,day).atTime(hour,0).atZone(zone).toInstant().toEpochMilli()
 @Test fun `work avoids class and nightly rest`() {
  val tasks=listOf(Task(1,"Aula",time(21,8),category="Aula",minutes=120),Task(2,"Trabalho",time(21,8),time(21,15),minutes=120))
  val plan=Planner().create(tasks,time(21,8),zone=zone)
  assertEquals(0,plan.missingMinutes);assertEquals(120,plan.blocks.sumOf{it.minutes});assertTrue(plan.blocks.all{it.start>=time(21,10) && it.start+it.minutes*60000<=time(21,15)})
 }
 @Test fun `overload is reported instead of extending into sleep`() {
  val plan=Planner().create(listOf(Task(1,"Trabalho",time(21,21),time(22,8),minutes=180)),time(21,21),dailyMinutes=120,zone=zone)
  assertTrue(plan.missingMinutes>0);assertTrue(plan.blocks.all{it.start+it.minutes*60000<=time(21,22)})
 }
 @Test fun `weekly class blocks future occurrence`() {
  val tasks=listOf(Task(1,"Aula",time(14,8),category="Aula",minutes=120,weekly=true),Task(2,"Trabalho",time(21,8),time(21,13),minutes=50))
  val plan=Planner().create(tasks,time(21,8),zone=zone)
  assertEquals(time(21,10),plan.blocks.first().start)
 }
 @Test fun `both modes share capacity and deadlines have precedence`() {
  val tasks=listOf(Task(1,"Pessoal",time(21,8),time(22,20),mode="Pessoal",minutes=60),Task(2,"Urgente",time(21,8),time(21,20),minutes=60))
  val plan=Planner().create(tasks,time(21,8),dailyMinutes=60,zone=zone)
  assertEquals(2,plan.blocks.first().taskId);assertEquals(0,plan.missingMinutes)
  assertTrue(plan.blocks.filter{it.taskId==1}.all{it.start>=time(22,8)})
 }
}
