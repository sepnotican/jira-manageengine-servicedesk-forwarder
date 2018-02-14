package core;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.apache.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

/**
 * Created by muzafar on 5/31/17.
 */
public class Tools implements Observable {

    private static final org.apache.log4j.Logger logger = LogManager.getLogger(Tools.class);
    private static FileHandler logFileHandler = null;
    private static Date lastCheckedDate = new Date(System.currentTimeMillis());
    private static Tools self;
    private ArrayList<InvalidationListener> observers = new ArrayList<>();

    private Tools() {
    }

    public static Tools getSelf() {
        if (self == null)
            self = new Tools();
        return self;
    }

    public static void initLogger() {

        Date currDate = new Date(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String logfileName = df.format(currDate) + ".log";

        File fileCheck = new File(logfileName);

        int i = 1;
        //todo direcotry by month+year
        while (fileCheck.exists()) {
            logfileName = String.format(df.format(currDate) + "-%d.log", i);
            fileCheck = new File(logfileName);
            i++;
        }

        try {
            if (logFileHandler != null) {
                logFileHandler.close();
                logger.removeHandler(logFileHandler);
            }

            SimpleFormatter simpleFormatter = new SimpleFormatter();
            logFileHandler = new FileHandler(logfileName);
            logFileHandler.setFormatter(simpleFormatter);

            logger.addHandler(logFileHandler);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        logger.info("New logfile initialized: " + logfileName);
    }

    public static String encodeTo1251(String income) {
        String result = null;

        try {
            byte[] byte1251 = income.getBytes("UTF-8");
            result = new String(byte1251, "windows-1251");
        } catch (IOException e) {
            Tools.logger.severe(e.getMessage());
        }
        return result;
    }

    public synchronized void checkDate() {

        Date thisMoment = new Date(System.currentTimeMillis());

        if (lastCheckedDate.getDate() < thisMoment.getDate()
                || lastCheckedDate.getMonth() < thisMoment.getMonth()
                || lastCheckedDate.getYear() < thisMoment.getYear()) {
            Tools.initLogger();
            updateObservers();
        }
        lastCheckedDate = thisMoment;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        observers.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        observers.remove(listener);
    }

    private void updateObservers() {
        observers.forEach(observer -> observer.invalidated(this));
    }
}
