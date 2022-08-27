package oracle.kv.sample.rmw;

import oracle.kv.KVStore;
import oracle.kv.table.Table;

public class SequenceBuilder {
    private KVStore _store;
    private String _name;
    private int _increment    = 100;
    private long _lockTimeout = 5000;
    private long _initialValue;
    
    public static String SEQUENCE_TABLE = "Sequences";
    private static final Object tableLock = new Object();
    
    
    public Sequence build() {
        assertNotNull(_store, "no store defined");
        assertNotNull(_name, "no sequence name defined");
        

        Table sequenceTable = _store.getTableAPI().getTable(SEQUENCE_TABLE);
        if (sequenceTable == null) {
            sequenceTable = defineSequenceTable(_store, SEQUENCE_TABLE);
        } 
        return new Sequence(_store, sequenceTable, _name, _initialValue,
                _increment, _lockTimeout);
    }
    
    public SequenceBuilder withStore(KVStore store) {
        _store = store;
        return this;
    }
    
    public SequenceBuilder withName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("empty/null sequence name");
        }
        _name = name;
        return this;
    }
    
    public SequenceBuilder withIncrement(int inc) {
        if (inc <= 0) {
            throw new IllegalArgumentException("negative or zero increment");
        }
        _increment = inc;
        return this;
    }
    
    public SequenceBuilder withInitialValue(int value) {
        _initialValue = value;
        return this;
    }
    
    
    
    /**
     * Define a sequence.
     * @param store
     * @param tableName
     * @param sequenceName
     * @return
     */
    public static Table defineSequenceTable(KVStore store, String tableName) {
        synchronized (tableLock) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName 
                + " (" + Sequence.NAME + " STRING, " 
                + Sequence.NEXT_VALUE + " LONG,"
                + " PRIMARY KEY(" + Sequence.NAME + "))";
            store.executeSync(sql);
            return store.getTableAPI().getTable(tableName);
        }
    }
    
    static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new IllegalArgumentException(msg);
    }
    static void assertNotNull(Object obj, String msg) {
        assertTrue(obj != null, msg);
    }
    static void assertNull(Object obj, String msg) {
        assertTrue(obj == null, msg);
    }

}
