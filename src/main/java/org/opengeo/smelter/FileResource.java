package org.opengeo.smelter;

import java.io.File;
import java.io.IOException;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class FileResource extends ServerResource {
    private File path;

    public FileResource(File path) {
        this.path = path;
    }

    @Get
    public Representation getRepresentation() throws IOException {
        return new FileRepresentation(path, MediaType.TEXT_JAVASCRIPT);
    }
}
