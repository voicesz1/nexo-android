package com.brenda.agenda;
import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import org.json.JSONObject;
public class AlarmService extends Service {
 Ringtone sound; final Handler timer=new Handler(Looper.getMainLooper()); int active=-1;
 public IBinder onBind(Intent i){return null;}
 static PendingIntent action(Context c,int id,String action){return PendingIntent.getBroadcast(c,id,new Intent(c,AlarmReceiver.class).setAction(action).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 public int onStartCommand(Intent intent,int flags,int startId){
  if(intent==null){stopSelf();return START_NOT_STICKY;} active=intent.getIntExtra("id",-1);
  NotificationManager nm=getSystemService(NotificationManager.class); NotificationChannel ch=new NotificationChannel("active_alarm","Despertador",NotificationManager.IMPORTANCE_HIGH);ch.setSound(null,null);nm.createNotificationChannel(ch);
  PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(this,"active_alarm").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(intent.getStringExtra("title")).setContentText("Hora de começar • soneca de 10 minutos").setCategory(Notification.CATEGORY_ALARM).setOngoing(true).setContentIntent(open)
   .addAction(new Notification.Action.Builder(null,"Começar",action(this,active,"START")).build())
   .addAction(new Notification.Action.Builder(null,"Adiar 10 min",action(this,active,"SNOOZE")).build())
   .addAction(new Notification.Action.Builder(null,"Concluir",action(this,active,"DONE")).build()).build();
  startForeground(1,n); if(sound!=null)sound.stop();
  sound=RingtoneManager.getRingtone(this,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
  if(sound!=null){sound.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());if(Build.VERSION.SDK_INT>=28)sound.setLooping(true);sound.play();}
  timer.removeCallbacksAndMessages(null);timer.postDelayed(()->stopSelf(),5*60*1000);return START_NOT_STICKY;
 }
 public void onDestroy(){timer.removeCallbacksAndMessages(null);if(sound!=null)sound.stop();super.onDestroy();}
}
