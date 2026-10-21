package com.brenda.agenda.data;
import androidx.room.*;
@Dao public interface SnapshotDao {
 @Query("SELECT * FROM agenda_snapshot WHERE id=1") Snapshot read();
 @Insert(onConflict=OnConflictStrategy.REPLACE) void save(Snapshot value);
}
