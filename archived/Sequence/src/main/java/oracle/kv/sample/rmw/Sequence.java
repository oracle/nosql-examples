package oracle.kv.sample.rmw;

import java.util.concurrent.atomic.AtomicLong;

import oracle.kv.KVStore;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.ReturnRow;
import oracle.kv.table.ReturnRow.Choice;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

/**
 * A sequence for a distributed database.
 * <br>
 * A sequence provides series of monotonic, unique values. These values
 * can be useful for an application as primary key or other identifiers.
 * <br>
 * Usage:
 * 
 * <pre>
 *    // get a persistent sequence named 'mySequeces' from 'Sequences' table
 *    // that fetches 20 unique values in a batch
 *    Sequence seq = Sequence.get(store, "Sequences", "mySequeces", 20);
 *    
 *    // get 100 unique values. The sequence will fetch batches from database
 *    // when necessary
 *    for (int i = 0; i < 100; i++) {
 *       int val = seq.next();
 *    }
 *    
 *  
 * @author pinaki poddar
 *
 */
public class Sequence implements Updater {
    private KVStore _store;
    // master table where this sequence is stored as a row
    private Table _table;
    // the primary key that identifies this sequence
    private PrimaryKey   _pk;
    // name of this sequence used as an identifier 
    private final String _name;
    
    private  AtomicLong _initialValue;
    private  AtomicLong _currentValue;
    private  AtomicLong _finalValue;
    // the number of values reserved in a batch
    private  final int _increment;
    
    
    private RMWLock _lock; 
    

    // name of the field that stores the current value of the sequence
    public static final String NAME       = "name";
    public static final String NEXT_VALUE = "next";
    
    /**
     * Creates a in-memory sequence possibly reading its current state 
     * from database.
     * 
     * @param api the connection to store
     * @param table the table containing the sequences as row
     * @param name name of the sequence
     * @param length number of values to reserve in a batch
     * @param maxReservationAttempt how many times to try to reserve
     * @param start the starting value if not known pass any negative number
     * @param isNew if true then no row of the given name must exist in given table
     */
    Sequence(KVStore store, Table table, String name, long initVal, 
            int increment, long lockTimeoutMS) {
        _store   = store;
        _table = table;
        _name   = name;
        _increment = increment;
        _lock = new RMWLock(lockTimeoutMS);
      
       _pk = _table.createPrimaryKey();
       _pk.put(NAME, _name);
       
       Row row = _store.getTableAPI().get(_pk, null);
       if (row == null) {
           row = _table.createRow();
           row.put(NAME, _name);
           _initialValue = new AtomicLong(initVal);
           row.put(NEXT_VALUE, new AtomicLong(initVal).addAndGet(_increment));
           _store.getTableAPI().put(row, null, null);
           reset(row);
       } else {
           row = _lock.update(_store, _pk, this);
           reset(row);
       }
    }
    
    
    /**
     * Gets next value in this sequence. Repeated call to this method 
     * would return numbers that are strictly monotonic, but not 
     * necessarily contiguous.
     *  
     * @return a number
     */
    public long next() {
        //System.err.println("next() cursor:" + _cursor  + " " + this);
        if (_currentValue.get() >= _finalValue.get()) {
            Row row = _lock.update(_store, _pk, this);
            reset(row);
        }
        return _currentValue.incrementAndGet();
    }
    
    void reset(Row row) {
        long next = row.get(NEXT_VALUE).asLong().get();
        _finalValue   = new AtomicLong(next);
        _initialValue = new AtomicLong(next - _increment);
        _currentValue = new AtomicLong(_initialValue.get());
        //System.err.println("reset" + _row + " version " + _row.getVersion());
    }
    
    /**
     * Gets current value of this sequence.
     */
    public long getCurrent() {
        return _currentValue.get();
    }
    
    
    @Override
    public void update(Row row) {
        long next = row.get(NEXT_VALUE).asLong().get();
        row.put(NEXT_VALUE, next+_increment);
    }
    
    public String toString() {
        return _name + "(" + _initialValue.get() + "-" + _finalValue.get();
    }
}
