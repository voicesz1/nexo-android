package com.brenda.agenda;
import android.content.*;
import org.json.*;
import java.util.*;
import com.brenda.agenda.data.*;
public final class Store {
 public static synchronized List<JSONObject> read(Context c){
  Snapshot snapshot=AgendaDatabase.get(c).snapshot().read();
  String payload=snapshot==null?c.getSharedPreferences("agenda",0).getString("items","[]"):snapshot.payload;
  List<JSONObject> result=new ArrayList<>();
  try {JSONArray array=new JSONArray(payload);for(int n=0;n<array.length();n++)result.add(array.getJSONObject(n));}catch(JSONException e){throw new IllegalStateException("Dados locais inválidos",e);}
  if(snapshot==null)save(c,result);return result;
 }
 public static synchronized void save(Context c,List<JSONObject> items){JSONArray a=new JSONArray();for(JSONObject item:items)a.put(item);Snapshot snapshot=new Snapshot();snapshot.payload=a.toString();AgendaDatabase.get(c).snapshot().save(snapshot);}
}
