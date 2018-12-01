package com.element54.utils.file;

import java.io.IOException;
import java.nio.file.Path;

import com.element54.utils.string.StringCleaner;
import com.element54.utils.string.StringUtils;

public class FilteredFileNameCleaner implements FileNameCleaner {

    protected final StringCleaner ns;

    public FilteredFileNameCleaner(final StringCleaner ns) {
        super();
        this.ns = ns;
    }

    @Override
    public Path create(final Path parent, final String name, final String ext) throws IOException {
        final String newName = this.ns.purge(name);
        if (StringUtils.isEmpty(newName)) {
            throw new IOException("\"" + name + "\" is empty after purge");
        }
        if (StringUtils.isEmpty(ext)) {
            return parent.resolve(newName);
        } else {
            return parent.resolve(newName + "." + ext);
        }
    }

}
