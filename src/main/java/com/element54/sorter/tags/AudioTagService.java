package com.element54.sorter.tags;

import java.io.IOException;
import java.nio.file.Path;

public interface AudioTagService {
    public AudioTag readTag(Path file) throws IOException;
}
