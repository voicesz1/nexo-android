package com.brenda.agenda;
import android.content.*;
import org.json.*;
import java.util.*;
public final class Tasks {
 static synchronized JSONObject find(Context c,int id){for(JSONObject i:Store.read(c))if(i.optInt("id")==id)return i;return null;}
 static synchronized void put(Context c,JSONObject item){List<JSONObject> all=Store.read(c);all.removeIf(i->i.optInt("id")==item.optInt("id"));all.add(item);Store.save(c,all);}
 static synchronized void finish(Context c,int id,boolean delete){
  JSONObject i=find(c,id);if(i==null)return;Alarms.cancel(c,i);
  if(delete){List<JSONObject> all=Store.read(c);all.removeIf(t->t.optInt("id")==id);Store.save(c,all);return;}
  try{
   if(i.optBoolean("weekly")){
    JSONObject completed=new JSONObject(i.toString());int historyId;do{historyId=new Random().nextInt(Integer.MAX_VALUE-1)+1;}while(find(c,historyId)!=null);
    completed.put("id",historyId).put("done",true).put("weekly",false).put("completedAt",System.currentTimeMillis());put(c,completed);
    Calendar next=Calendar.getInstance();next.setTimeInMillis(i.optLong("base",i.optLong("time")));do{next.add(Calendar.WEEK_OF_YEAR,1);}while(next.getTimeInMillis()<=System.currentTimeMillis());
    long delta=next.getTimeInMillis()-i.optLong("base",i.optLong("time"));i.put("time",next.getTimeInMillis()).put("base",next.getTimeInMillis()).put("started",false);if(i.optLong("deadline")>0)i.put("deadline",i.optLong("deadline")+delta);put(c,i);Alarms.schedule(c,i);
   }else{i.put("done",true).put("completedAt",System.currentTimeMillis());put(c,i);}
  }catch(JSONException e){throw new IllegalStateException(e);}
 }
 static synchronized boolean snooze(Context c,int id){JSONObject i=find(c,id);if(i==null)return false;Alarms.cancel(c,i);try{i.put("started",false).put("time",System.currentTimeMillis()+10*60*1000);put(c,i);return Alarms.schedule(c,i);}catch(JSONException e){return false;}}
}
