package com.brenda.agenda.data;
import androidx.room.*;
@Entity(tableName="agenda_snapshot")
public class Snapshot {
 @PrimaryKey public int id=1;
 @androidx.annotation.NonNull public String payload="[]";
}
