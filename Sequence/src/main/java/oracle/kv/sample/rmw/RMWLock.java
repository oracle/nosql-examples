package oracle.kv.sample.rmw;

import java.util.concurrent.TimeUnit;

import oracle.kv.Consistency;
import oracle.kv.Durability;
import oracle.kv.KVStore;
import oracle.kv.RequestTimeoutException;
import oracle.kv.Version;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.ReadOptions;
import oracle.kv.table.ReturnRow;
import oracle.kv.table.ReturnRow.Choice;
import oracle.kv.table.Row;
import oracle.kv.table.WriteOptions;

/**
 * A read-modify-write or 'SELECT FOR UPDATE' lock for NoSQL distributed 
 * database. The lock ensures that a row is written with updated values
 * as its latest values under concurrent attempts. 
 * <br>
 * A row field <code>NEXT_VALUE</code> can be atomically incremented as follows:
 * <pre>
 *    new RMWLock().update(store, row, new Updater() {
 *          public void update(Row row) {
 *              int currentValue = row.gett("NEXT_VALUE").asInt().get();
 *              row.put("NEXT_VALUE" , ++currentValue);
 *          }
 *    }) 
 * </pre>
 * 
 * @author pinaki poddar
 *
 */
public class RMWLock {
    
    private final long _lockTimeoutMs;
    private long _timeRemaining = 5000;
    
    private static int DEFAULT_LOCK_TIMEOUT_MS  = 5000;
    
    

    /**
     * Creates a lock with default lock timeout.
     */
    public RMWLock() {
        this(DEFAULT_LOCK_TIMEOUT_MS);
    }
    
    /**
     * Creates a lock with given with lock timeout in milliseconds.
     */
    public RMWLock(long lockTimeoutMs) {
        _lockTimeoutMs = lockTimeoutMs;
        _timeRemaining = _lockTimeoutMs;
    }

    /**
     * Updates the row identified by given primary key with given update function.
     * 
     * @param store connection to a store
     * @param pk primary key of a row to be updated. The row must exist in database.
     * @param updater an update function to update th erow
     * 
     * @return the updated row
     */
    public Row update(KVStore store, PrimaryKey pk, Updater updater) {
        assertNotNull(pk, "cannot update row identifed by null primary key");
        assertNotNull(updater, "cannot update row with null update function");
        
        // ---------- READ -------------------
        Row row = fetchRow(store, pk);
        
        assertNotNull(row, "cannot find row for primary key " + pk);
        assertNotNull(row.getVersion(), "cannot update row without version");
        
        
        return update(store, row, row.getVersion(), updater);
    }
    
   

    /**
     * updates given record in the database
     * so that the updated record to be the latest state. 
     * The write may not succeed if other threads update the same row.
     * In such case, several attempts are made before a lock time exception
     * is raised.
     * 
     * @param store connection to store
     * @param row record to be updated
     * @param latestVersion the version of the record to match for update.
     * This is not necessarily the version of the given row
     * @param updater a function to update the row. the function is applied
     * before every write attempt.
     * 
     * @return the updated row
     */
    private Row update(KVStore store, Row row, Version version, Updater updater) {
        if (_timeRemaining <= 0) {
            throw new RuntimeException("cannot lock row " + row + " in " 
                    + _lockTimeoutMs + "ms");
        }
        
        
        // ---------- MODIFY -------------------
        updater.update(row);

        // ---------- WRITE -------------------
        ReturnRow rr = row.getTable().createReturnRow(Choice.ALL);
        WriteOptions wo = new WriteOptions(Durability.COMMIT_SYNC, 
                _timeRemaining, TimeUnit.MILLISECONDS);
        version = store.getTableAPI().putIfVersion(row, version, rr, wo);
        
        if (version == null) {
            // uses  returned row for next attempt to update
            return update(store, rr, rr.getVersion(), updater);
        } else {
            return row;
        }
    }
    
    private Row fetchRow(KVStore store, PrimaryKey pk) {
       long startTime = System.currentTimeMillis();
       ReadOptions ro = new ReadOptions(Consistency.ABSOLUTE, 
               _timeRemaining, TimeUnit.MILLISECONDS);
       try {
            Row row = store.getTableAPI().get(pk, ro);
            _timeRemaining -= (System.currentTimeMillis()-startTime);
            return row;
        } catch (RequestTimeoutException ex) {
            throw new RuntimeException("cannot acquire lock on " + pk + " in " 
                    + _lockTimeoutMs + " ms");
        }
    }
    
    private void assertNotNull(Object obj, String msg) {
        if (obj == null) {
            throw new RuntimeException(msg);
        }
    }
}
