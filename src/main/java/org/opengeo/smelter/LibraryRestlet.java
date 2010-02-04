package org.opengeo.smelter;

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
    public Map<String, String> config;

    private static final Configuration freemarker;

    static {
        freemarker = new Configuration();
        freemarker.setClassForTemplateLoading(LibraryRestlet.class, "");
    }

    static enum Mode { Minified, Loader }
    private Mode mode = Mode.Loader;
    
    public LibraryRestlet(String prefix, Map<String, String> config) {
        this.prefix = prefix;
        this.config = config;
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
            includes.addAll(
                Arrays.asList(config.get("first").trim().split("\\s+"))
            );

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
