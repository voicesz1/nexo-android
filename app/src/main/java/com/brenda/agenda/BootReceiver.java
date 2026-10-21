package com.brenda.agenda;
import android.content.*;
import org.json.JSONObject;
import java.util.concurrent.Executors;
public class BootReceiver extends BroadcastReceiver { private static final java.util.concurrent.ExecutorService WORK=Executors.newSingleThreadExecutor();
 public void onReceive(Context c,Intent intent){if(!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))return;PendingResult result=goAsync();WORK.execute(()->{try{for(JSONObject i:Store.read(c))if(i.optLong("time")>System.currentTimeMillis()&&!i.optBoolean("started")&&!i.optBoolean("done"))Alarms.schedule(c,i);}finally{result.finish();}});}
}
