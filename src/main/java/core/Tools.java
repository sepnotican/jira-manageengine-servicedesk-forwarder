package core;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by muzafar on 5/31/17.
 */
public final class Tools {

    public static final Logger logger = LogManager.getLogger(Tools.class);

    public static String encodeTo1251(String income) {
        String result = null;

        try {
            byte[] byte1251 = income.getBytes("UTF-8");
            result = new String(byte1251, "windows-1251");
        } catch (IOException e) {
            Tools.logger.error(e.getMessage());
        }
        return result;
    }

}
