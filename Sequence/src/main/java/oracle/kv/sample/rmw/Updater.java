package oracle.kv.sample.rmw;

import oracle.kv.table.Row;

/**
 * A function to update the row.
 * 
 * @author pinaki poddar
 *
 */
public interface Updater {
    /**
     * Update the given row.
     * @param row a row to be updated.
     */
    void update(Row row);
}
