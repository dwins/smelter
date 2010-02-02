package org.opengeo.smelter;

import java.io.File;
import java.io.IOException;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.Variable;

public class Main extends Application {
    @Override 
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/home", Home.class);
        try {
            router.attach("/files/{path}", new FileFinder())
                .getTemplate()
                .getVariables()
                .put("path", new Variable(Variable.TYPE_URI_PATH));
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
        return router;
    }

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8080);
        component.getDefaultHost().attach(new Main());
        component.start();
    }
}
