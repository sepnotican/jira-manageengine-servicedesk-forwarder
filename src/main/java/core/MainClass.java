package core;

import dao.IssuesLocalCache;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by muzafar on 5/30/17.
 */

public class MainClass {
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
                    Settings.getSettings().createSettings();
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

                    Settings.getSettings().load();

                    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
                    context.register(AppConfig.class);
                    context.refresh();

                    Runnable issueTransportThread = (Runnable) context.getBean("issueTransportThread");

                    if (Settings.getSettings().isModuleIssueTransortActive()) {

                        new Thread(issueTransportThread).start();
                    } else {
                        Tools.logger.warn("issue Transport Thread is not active in settings.json");
                    }

                }
            }
        }

    }

}