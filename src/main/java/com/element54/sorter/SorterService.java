package com.element54.sorter;

import java.nio.file.Path;

public interface SorterService {
    public Path getFile(final Path file, final Path destFolder) throws SorterServiceException;
}
