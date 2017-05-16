package oracle.kv.sample.rmw;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;

/**
 * Tests creating a sequence an getting series of values that
 * are strictly monotonic across concurrent access.
 *  
 * @author pinaki poddar
 *
 */
public class TestSequence  {
    private static AtomicLong lastUpdate = new AtomicLong(-1L);
    private static KVStore store;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
        store = KVStoreFactory.getStore(config);
    }
    static Sequence createSequence(int inc) {
        String name = "Seq"+System.currentTimeMillis();
        return createSequence(name,inc);
    }
    
    static Sequence createSequence(String name, int inc) {
        Sequence seq = new SequenceBuilder().withStore(store)
                .withName(name)
                .withIncrement(inc)
                .build();
        return seq;
    }

    @Test
    public void testSequenceValuesAreMonotonic() {
        int increment = 20;
        Sequence seq = createSequence(increment);
        long last = 0;
        for (int i = 0; i < 2*increment+5; i++) {
            long v = seq.next();
            Assert.assertEquals(v, last+1);
            last = v;
        }
        
    }
    
    @Test
    public void testCurrentValue() {
        int increment = 20;
        Sequence seq = createSequence(increment);
        long v = seq.getCurrent();
        Assert.assertEquals(0, v);
        long next = seq.next();
        Assert.assertEquals(1, next);
    }

    @Test
    public void testSequenceValuesAreMonoticAcrossMultipleThreads() throws InterruptedException {
        int nThread  = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(nThread);
        for (int i = 0; i < nThread; i++) {
            threadPool.submit(new RMWClient(store,3, 10));
        }
        threadPool.shutdown();
        threadPool.awaitTermination(100, TimeUnit.SECONDS);
    }
    
    
    public static class RMWClient implements Runnable {
        private final Sequence _seq;
        private int _increment;
        static String seqName = "RMWTest" + System.currentTimeMillis(); 
        RMWClient(KVStore store, int bacthSize, int L) {
            _increment = L;
            _seq = createSequence(seqName, _increment);
        }
        
        public void run() {
            for (int i = 0; i < _increment; i++) {
                try {
                  long v = _seq.next();
                  Assert.assertTrue(v > lastUpdate.get());
                  lastUpdate.set(v);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    

}
