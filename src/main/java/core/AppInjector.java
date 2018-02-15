package core;

import com.google.inject.AbstractModule;
import controller.issueTransport.*;

public class AppInjector extends AbstractModule {

    @Override
    protected void configure() {

        //bind A to B implementation
        //bind(A.class).to(B.class);
        bind(RestJsonCaller.class).to(RestJsonCallerImpl.class);
        bind(RestXMLCaller.class).to(RestXMLCallerImpl.class);
        bind(ServiceDeskHandler.class).to(ServiceDeskHandlerImpl.class);

    }
}
