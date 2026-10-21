package com.brenda.agenda
import android.content.Context
import com.brenda.agenda.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LocalTaskRepository(private val context:Context):TaskRepository {
 override suspend fun load()=withContext(Dispatchers.IO){Store.read(context).map{j->if(!j.optBoolean("done")&&!j.optBoolean("started")&&j.optLong("time")>System.currentTimeMillis())Alarms.schedule(context,j);Task(j.optInt("id"),j.optString("title"),j.optLong("time"),j.optLong("deadline"),j.optString("mode",if(j.optString("category")=="Pessoal")"Pessoal" else "Acadêmico"),j.optString("kind","Tarefa"),j.optInt("minutes",60),j.optInt("priority",2),j.optBoolean("weekly"),j.optBoolean("started"),j.optBoolean("done"),j.optString("steps"))}}
 override suspend fun save(task:Task)=withContext(Dispatchers.IO){
  val old=Tasks.find(context,task.id);if(old!=null)Alarms.cancel(context,old)
  val j=JSONObject().put("id",task.id).put("title",task.title).put("time",task.time).put("base",task.time).put("deadline",task.deadline).put("mode",task.mode).put("kind",task.category).put("category",task.mode).put("minutes",task.minutes).put("priority",task.priority).put("weekly",task.weekly).put("started",task.started).put("done",task.done).put("steps",task.steps)
  Tasks.put(context,j);if(!task.done&&!task.started&&task.time>System.currentTimeMillis())Alarms.schedule(context,j);Unit
 }
 override suspend fun complete(id:Int)=withContext(Dispatchers.IO){Tasks.finish(context,id,false)}
 override suspend fun delete(id:Int)=withContext(Dispatchers.IO){Tasks.finish(context,id,true)}
 override suspend fun snooze(id:Int)=withContext(Dispatchers.IO){Tasks.snooze(context,id);Unit}
}
