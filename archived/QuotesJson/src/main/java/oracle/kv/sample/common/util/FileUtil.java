package oracle.kv.sample.common.util;

import java.io.File;

/**
 * Utility Class
 * 
 * @author vsettipa
 * 
 */
public class FileUtil {
    
    public static boolean isValidPath(String path) {
	boolean flag = false;
	File file = null;
	if (StringUtil.isNotEmpty(path)) {
	    file = new File(path);
	    if (file.exists()) {
		flag = true;
	    }
	} // EOF if
	
	return flag;
    } // isValidPath
    
}
