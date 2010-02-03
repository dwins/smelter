package org.opengeo.smelter;

import java.io.File;
import java.io.IOException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

public class FileFinder extends Finder {
    private File base;

    public FileFinder(File base) {
        try {
            System.out.println("Setting up file finder: " + base.getCanonicalPath());
        } catch (Exception e) {
            // don't recover??
        }
        this.base = base;
    }

    public FileFinder() throws IOException {
        // use working directory by default
        this(new File(System.getProperty("user.dir")));
    }

    @Override
    public ServerResource find(Request request, Response response) {
        String path = (String) request.getAttributes().get("path");
        File f = new File(base, path);
        try {
            if (f.getCanonicalPath().startsWith(base.getCanonicalPath())) {
                return new FileResource(f);
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
            // not found, not a problem
        }

        return null;
    }
}
