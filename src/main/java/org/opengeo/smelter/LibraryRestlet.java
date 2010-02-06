package org.opengeo.smelter;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import freemarker.template.Configuration;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class LibraryRestlet extends Finder {
    private String prefix;
    public Library library;

    private static final Configuration freemarker;

    static {
        freemarker = new Configuration();
        freemarker.setClassForTemplateLoading(LibraryRestlet.class, "");
    }

    static enum Mode { Minified, Loader }
    private Mode mode = Mode.Loader;
    
    public LibraryRestlet(String prefix, Library library) {
        this.prefix = prefix;
        this.library = library;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() { 
        return mode;
    }

    @Override
    public ServerResource find(Request request, Response response) {
        return new LibraryResource();
    }

    public class LibraryResource extends ServerResource {
        @Get
        public Representation get() {
            switch (mode) {
                case Minified: 
                    return getMinified();
                case Loader:
                    return getLoader();
                default:
                    return new StringRepresentation(
                        "Unexpected application state...",
                        MediaType.TEXT_PLAIN
                    );
            }
        }

        private Representation getMinified() {
            return new StringRepresentation( 
                "Minification is easy if you don't worry about preserving " +
                "behavior.", 
                MediaType.TEXT_JAVASCRIPT
            );
        }

        private Representation getLoader() {
            Map<String, Object> configMap = new HashMap<String, Object>();
            List<String> includes = new ArrayList<String>();

            try {
                includes.addAll(library.getSortedPaths());
            } catch (IOException ioe) {
                System.out.println(ioe);
            }

            configMap.put("includes", includes);
            configMap.put("prefix", prefix);
            freemarker.setClassForTemplateLoading(LibraryRestlet.class, "");

            return new TemplateRepresentation(
                "loader.js.ftl",
                freemarker, 
                configMap,
                MediaType.TEXT_JAVASCRIPT
            );
        }
    }
}
