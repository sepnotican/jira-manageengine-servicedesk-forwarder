package core.threads;

import com.google.inject.Inject;
import controller.issueTransport.IssuesTransportController;
import core.Settings;
import org.apache.log4j.Logger;

/**
 * Created by muzafar on 6/14/17.
 */
public class IssueTransportThread implements Runnable {

    private IssuesTransportController issuesTransportController;
    protected static Logger logger = Logger.getLogger(IssueTransportThread.class);

    @Inject
    public IssueTransportThread(IssuesTransportController issuesTransportController) {
        this.issuesTransportController = issuesTransportController;
    }

    @Override
    public void run() {
        logger.info("Issue transport thread started.");

        int timeout;
        while (true) {
            try {
                Settings.getInstance().load();
                timeout = Math.max(Settings.getInstance().getServicedeskTransportTimeoutSec() * 1000, 30000);
                Thread.sleep(timeout);
                issuesTransportController.checkForChanges();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
    }
}
