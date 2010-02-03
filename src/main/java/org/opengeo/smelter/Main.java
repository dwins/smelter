package org.opengeo.smelter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.Variable;
import org.opengeo.smelter.config.ConfigReader;

public class Main extends Application {
    private File configFile;
    private Map<String, Map<String, String>> config;

    public Main(File configFile) throws IOException {
        this.configFile = configFile;
        this.config = 
            ConfigReader.parse(new BufferedReader(new FileReader(configFile)));
    }

    @Override 
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/home", Home.class);
        for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
            String name = entry.getKey();
            name = name.replaceFirst("\\..*?$", "");
            String root = entry.getValue().get("root");
            if (root != null) {
                Route route = router.attach(
                    "/" + name + "/{path}", 
                    new FileFinder(
                        new File(configFile.getParent(), root.trim())
                    )
                );
                route.getTemplate()
                    .getVariables()
                    .put("path", new Variable(Variable.TYPE_URI_PATH));
                router.attach(
                    "/" + name + ".js",
                    new LibraryRestlet("/" + name, entry.getValue())
                );
            }
        }
        return router;
    }

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8080);
        component.getDefaultHost().attach(new Main(new File(args[0])));
        component.start();
    }
}
