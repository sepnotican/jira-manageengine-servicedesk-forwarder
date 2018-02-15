package core;

import com.google.inject.AbstractModule;
import controller.issueTransport.RestJsonCaller;
import controller.issueTransport.RestJsonCallerImpl;
import controller.issueTransport.RestXMLCaller;
import controller.issueTransport.RestXMLCallerImpl;
import service.JiraHandler;
import service.JiraHandlerImpl;
import service.ServiceDeskHandler;
import service.ServiceDeskHandlerImpl;

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
