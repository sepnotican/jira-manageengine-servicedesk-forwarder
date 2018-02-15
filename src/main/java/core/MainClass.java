package core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import core.threads.IssueTransportThread;
import dao.IssuesLocalCache;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by muzafar on 5/30/17.
 */

public class MainClass {
    private static final Logger logger = Logger.getLogger(MainClass.class);

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage :" +
                    "\n\t--createSettings : create settings.json" +
                    "\n\t--createDB : create SQLite database specified in settings.json (if not exists)" + //todo
                    "\n\t--run : run the application" +
                    "\n\t--clearTaskCache : empty task cache table (use it when db file became big)");
            System.exit(0);
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--createSettings": {
                    Settings.getInstance().createSettings();
                    System.out.println("Settings created.");
                    System.exit(0);
                }
                case "--createDB": {
                    IssuesLocalCache.instance.createTaskTransferStructure();
                    System.exit(0);
                }
                case "--clearTaskCache": {
                    IssuesLocalCache.instance.clearTaskCache();
                    System.exit(0);
                }
                case "--run": {

                    try {

                        File logDir = new File("log");
                        if (!(logDir.exists() && logDir.isDirectory()))
                            if (!logDir.mkdir()) {
                                throw new RuntimeException("FATAL ERROR : Unable to create \"log\" directory!");
                            }

                        Settings.getInstance().load();

                        Injector injector = Guice.createInjector(new AppInjector());

//                        Runnable issueTransportThread = new IssueTransportThread();
                        Runnable issueTransportThread = injector.getInstance(IssueTransportThread.class);

                        if (Settings.getInstance().isModuleIssueTransortActive()) {
                            new Thread(issueTransportThread).start();
                        } else {
                            logger.warn("issue Transport Thread is not active in settings.json");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        System.exit(-1);
                    }

                }
            }
        }

    }

}
