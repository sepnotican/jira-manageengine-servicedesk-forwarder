package core.threads;

import core.Settings;
import core.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.IssuesTransportService;

/**
 * Created by muzafar on 6/14/17.
 */
@Component
public class IssueTransportThread implements Runnable {

    @Autowired
    private IssuesTransportService issuesTransportService;

    @Override
    public void run() {


        Tools.logger.info("Issue transport service started.");

        int timeout;

        while (true) {

            try {
                Settings.getSettings().load();
                timeout = Math.max(Settings.getSettings().getServicedeskTransportTimeoutSec() * 1000, 30000);
                Thread.sleep(timeout);
                issuesTransportService.checkForChanges();

            } catch (Exception e) {
                e.printStackTrace();
                StringBuilder message = new StringBuilder("Interrupted!! {{" + '\n' + e.getMessage() + '\n');
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    message.append(stackTraceElement.toString());
                }
                message.append("}}\n");
                Tools.logger.error(message.toString());
            }

        }


    }

}
