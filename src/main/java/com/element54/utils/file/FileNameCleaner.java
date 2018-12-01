package com.element54.utils.file;

import java.io.IOException;
import java.nio.file.Path;

public interface FileNameCleaner {

    default public Path create(final Path parent, final String name) throws IOException {
        return create(parent, name, null);
    }

    public Path create(Path parent, String name, String extension) throws IOException;

}
