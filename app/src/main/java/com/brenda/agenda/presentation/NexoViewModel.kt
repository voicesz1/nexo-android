package com.brenda.agenda
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brenda.agenda.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class NexoViewModel(app:Application):AndroidViewModel(app){
 private val repository:TaskRepository=LocalTaskRepository(app)
 val tasks=MutableStateFlow<List<Task>>(emptyList());val error=MutableStateFlow<String?>(null)
 val preferences=app.getSharedPreferences("nexo_settings",0)
 val wake=MutableStateFlow(preferences.getInt("wake",8));val sleep=MutableStateFlow(preferences.getInt("sleep",22));val budget=MutableStateFlow(preferences.getInt("budget",120))
 val focusMinutes=MutableStateFlow(preferences.getInt("focus",0));val mode=MutableStateFlow(preferences.getString("mode","Todos")?:"Todos")
 fun run(action:suspend ()->Unit){viewModelScope.launch{try{action();tasks.value=repository.load()}catch(e:Exception){if(e is CancellationException)throw e;error.value="Não foi possível atualizar os dados. Tente novamente."}}}
 fun refresh()=run{}
 fun save(t:Task)=run{repository.save(t)}
 fun complete(t:Task)=run{repository.complete(t.id)}
 fun delete(t:Task)=run{repository.delete(t.id)}
 fun snooze(t:Task)=run{repository.snooze(t.id)}
 fun start(t:Task)=save(t.copy(started=true))
 fun setMode(value:String){mode.value=value;preferences.edit().putString("mode",value).apply()}
 fun settings(w:Int,s:Int,b:Int){wake.value=w;sleep.value=s;budget.value=b;preferences.edit().putInt("wake",w).putInt("sleep",s).putInt("budget",b).apply()}
 fun recordFocus(){focusMinutes.value+=25;preferences.edit().putInt("focus",focusMinutes.value).apply()}
 suspend fun plan():Plan=withContext(Dispatchers.Default){Planner().create(tasks.value,System.currentTimeMillis(),wake.value,sleep.value,budget.value)}
 init{refresh()}
}
