package org.opengeo.smelter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
    private Map<String, Library> config;

    public Main(File configFile) throws IOException {
        this.configFile = configFile;
        this.config = extractLibraries(configFile);
    }

    @Override 
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        Map<String, LibraryRestlet> libraries =
            new HashMap<String, LibraryRestlet>();

        for (Map.Entry<String, Library> entry : config.entrySet()) {
            String name = entry.getKey();
            Library lib = entry.getValue();
            name = name.replaceFirst("\\..*?$", "");
            if (lib.getRoot() != null) {
                String filePath = String.format("/%s/{path}", name);
                String loaderPath = String.format("/%s.js", name);
                Route route = 
                    router.attach(filePath, new FileFinder(lib.getRoot()));
                route.getTemplate()
                    .getVariables()
                    .put("path", new Variable(Variable.TYPE_URI_PATH));
                LibraryRestlet libRestlet =
                    new LibraryRestlet("/" + name, lib);
                router.attach(loaderPath, libRestlet);
                libraries.put(name + ".js", libRestlet);
            }
        }

        router.attach("/", new Home(libraries));
        return router;
    }

    private static Library libraryFromConfig(
        File base,
        Map<String, String> conf
    ) throws IOException {
        String root = conf.get("root");
        String license = conf.get("license");
        String first = conf.get("first");
        String include = conf.get("include");
        String exclude = conf.get("exclude");
        Library lib = new Library();

        if (root != null) {
            lib.setRoot(
                new File(base, root.trim()).getCanonicalFile()
            );
        }

        if (license != null) {
            lib.setLicense(
                new File(base, license.trim()).getCanonicalFile()
            );
        }

        if (first != null) {
            lib.getFirstFiles()
                .addAll(Arrays.asList(first.trim().split("\\s+")));
        }

        if (include != null) {
            lib.getIncludeFiles()
                .addAll(Arrays.asList(include.trim().split("\\s+")));
        }

        if (exclude != null) {
            lib.getExcludeFiles()
                .addAll(Arrays.asList(exclude.trim().split("\\s+")));
        }

        return lib;
    }

    private static Map<String, Library> extractLibraries(File configFile) 
    throws IOException {
        Map<String, Map<String, String>> config = 
            ConfigReader.parse(new BufferedReader(new FileReader(configFile)));
        Map<String, Library> libs = new HashMap<String, Library>();
        for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
            libs.put(
                entry.getKey(),
                libraryFromConfig(configFile.getParentFile(), entry.getValue())
            );
        }
        return libs;
    }

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8080);
        component.getDefaultHost().attach(new Main(new File(args[0])));
        component.start();
    }
}
