package core.threads;

import com.google.inject.Inject;
import core.Settings;
import core.Tools;
import service.IssuesTransportService;

/**
 * Created by muzafar on 6/14/17.
 */
public class IssueTransportThread implements Runnable {

    @Inject
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
                Tools.logger.error(e.getMessage());
            }
        }
    }
}
