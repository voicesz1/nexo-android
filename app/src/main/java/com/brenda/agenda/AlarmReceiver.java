package com.brenda.agenda;
import android.content.*;
import org.json.*;
import java.util.concurrent.Executors;
public class AlarmReceiver extends BroadcastReceiver { private static final java.util.concurrent.ExecutorService WORK=Executors.newSingleThreadExecutor();
 public void onReceive(Context c,Intent intent){PendingResult pending=goAsync();WORK.execute(()->{try{
  int id=intent.getIntExtra("id",-1);JSONObject item=Tasks.find(c,id);if(item==null||item.optBoolean("done"))return;
  String action=intent.getAction();
  if(action!=null){c.stopService(new Intent(c,AlarmService.class));if(action.equals("SNOOZE"))Tasks.snooze(c,id);if(action.equals("DONE"))Tasks.finish(c,id,false);if(action.equals("START")){item.put("started",true);Tasks.put(c,item);}}
  else c.startForegroundService(new Intent(c,AlarmService.class).putExtra("id",id).putExtra("title",item.optString("title")));
 }catch(JSONException e){throw new IllegalStateException(e);}finally{pending.finish();}});}
}
