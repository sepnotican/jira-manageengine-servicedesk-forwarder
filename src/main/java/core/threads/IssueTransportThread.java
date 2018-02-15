package core.threads;

import com.google.inject.Inject;
import controller.issueTransport.IssuesTransportController;
import core.Settings;
import core.Tools;

/**
 * Created by muzafar on 6/14/17.
 */
public class IssueTransportThread implements Runnable {

    private IssuesTransportController issuesTransportController;

    @Inject
    public IssueTransportThread(IssuesTransportController issuesTransportController) {
        this.issuesTransportController = issuesTransportController;
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
                issuesTransportController.checkForChanges();
            } catch (Exception e) {
                e.printStackTrace();
                Tools.logger.error(e.getMessage());
            }
        }
    }
}
