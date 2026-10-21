package com.brenda.agenda.data;
import android.content.Context;
import androidx.room.*;
@Database(entities={Snapshot.class},version=1,exportSchema=false)
public abstract class AgendaDatabase extends RoomDatabase {
 public abstract SnapshotDao snapshot();
 private static volatile AgendaDatabase instance;
 public static AgendaDatabase get(Context c){if(instance==null)synchronized(AgendaDatabase.class){if(instance==null)instance=Room.databaseBuilder(c.getApplicationContext(),AgendaDatabase.class,"nexo.db").build();}return instance;}
}
