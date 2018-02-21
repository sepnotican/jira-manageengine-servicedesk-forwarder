package core;

import com.google.inject.AbstractModule;
import handlers.jira.RestJsonCaller;
import handlers.jira.RestJsonCallerImpl;
import handlers.servicedesk.RestXMLCaller;
import handlers.servicedesk.RestXMLCallerImpl;
import handlers.jira.JiraHandler;
import handlers.jira.JiraHandlerImpl;
import handlers.servicedesk.ServiceDeskHandler;
import handlers.servicedesk.ServiceDeskHandlerImpl;

public class AppInjector extends AbstractModule {

    @Override
    protected void configure() {

        //bind A to B implementation
        //bind(A.class).to(B.class);
        bind(RestJsonCaller.class).to(RestJsonCallerImpl.class);
        bind(RestXMLCaller.class).to(RestXMLCallerImpl.class);
        bind(ServiceDeskHandler.class).to(ServiceDeskHandlerImpl.class);
        bind(JiraHandler.class).to(JiraHandlerImpl.class);
    }
}
