package core.threads;

import com.google.inject.Inject;
import core.Settings;
import core.Tools;
import service.IssuesTransportService;

/**
 * Created by muzafar on 6/14/17.
 */
public class IssueTransportThread implements Runnable {

    private IssuesTransportService issuesTransportService;

    @Inject
    public IssueTransportThread(IssuesTransportService issuesTransportService) {
        this.issuesTransportService = issuesTransportService;
    }

    @Override
    public void run() {
        Tools.logger.info("Issue transport thread started.");

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
