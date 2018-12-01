package com.element54.sorter.tags;

import java.nio.file.Path;

public interface AudioTag {
    public AudioType getAudioType();

    public String getArtist();

    public String getAlbumArtist();

    public String getAlbum();

    public Integer getDisk();

    public Integer getTrack();

    public String getTitle();

    public Integer getYear();

    public boolean isCompilation();

    public Path getFile();

    public String getCustom(String key);

    default public boolean isAudiobook() {
        return getAudioType() == AudioType.AUDIOBOOK;
    }

    default public boolean isMusic() {
        return getAudioType() == AudioType.MUSIC;
    }
}
