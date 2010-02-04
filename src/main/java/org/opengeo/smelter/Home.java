package org.opengeo.smelter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import freemarker.template.Configuration;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class Home extends Finder {
    private static final Configuration freemarker;

    static {
        freemarker = new Configuration();
        freemarker.setClassForTemplateLoading(Home.class, "");
    }

    private Map<String, LibraryRestlet> resourceStates =
        new HashMap<String, LibraryRestlet>();


    public Home(Map<String, LibraryRestlet> resources) {
        resourceStates.putAll(resources);
    }

    @Override public ServerResource find(Request req, Response resp) {
        return new Resource();
    }

    public class Resource extends ServerResource {
        @Get public Representation get() {
            Form params = getRequest().getResourceRef().getQueryAsForm();
            if (params.size() != 0) {
                for (Parameter p : params) {
                    if (resourceStates.containsKey(p.getName())) {
                        String mode = p.getValue();
                        LibraryRestlet lib = resourceStates.get(p.getName());
                        if (mode.equals("minified")) {
                            lib.setMode(LibraryRestlet.Mode.Minified);
                        } else if (mode.equals("loader")) {
                            lib.setMode(LibraryRestlet.Mode.Loader);
                        }
                    }
                }
            } 

            Map<String, Object> context = new HashMap<String, Object>();
            Map<String, String> config =
                new HashMap<String, String>();
            for (Map.Entry<String, LibraryRestlet> entry 
                    : resourceStates.entrySet()) 
            {
                LibraryRestlet lib = entry.getValue();
                String mode = "";
                if (lib.getMode() == LibraryRestlet.Mode.Minified) {
                    mode = "minified";
                } else if (lib.getMode() == LibraryRestlet.Mode.Loader) {
                    mode = "loader";
                }
                config.put(entry.getKey(), mode);
            }

            context.put("libraries", config);
            return new TemplateRepresentation(
                "home.html.ftl",
                freemarker, 
                context,
                MediaType.TEXT_HTML
            );
        }
    }
}
