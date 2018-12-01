package com.element54.sorter.tags.impl;

import java.io.IOException;
import java.nio.file.Path;

import com.element54.sorter.tags.AudioTag;
import com.element54.sorter.tags.AudioTagService;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class MP3agicTagService implements AudioTagService {

    @Override
    public AudioTag readTag(Path file) throws IOException {
        final Mp3File m;
        try {
            m = new Mp3File(file);
        } catch (UnsupportedTagException | InvalidDataException | IOException e) {
            throw new IOException("cannot read tag " + file.toString(), e);
        }
        if (!m.hasId3v2Tag()) {
            throw new IOException("missing id3v2 tag " + file.toString());
        }
        final ID3v2 tag = m.getId3v2Tag();
        return new MP3agicTag(tag, file);
    }

}
