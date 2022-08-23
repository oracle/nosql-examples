package oracle.kv.sample.common.util;

/**
 * Utility Class
 * 
 * @author vsettipa
 * 
 */
public class StringUtil {
    public static boolean isEmpty(String src) {
	boolean flag = false;
	if (src == null || src.trim().isEmpty()) {
	    flag = true;
	}
	return flag;
    } // isEmpty
    
    public static boolean isNotEmpty(String src) {
	return !isEmpty(src);
    } // isNotEmpty
    
}
