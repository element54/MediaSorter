package com.element54.sorter;

import java.nio.file.Path;

public class SorterServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    protected final Path file;

    public SorterServiceException(final String message, final Throwable cause, final Path file) {
        super(message, cause);
        this.file = file;
    }

    public Path getFile() {
        return this.file;
    }
}
