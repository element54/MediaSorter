package com.element54.sorter.tags.impl;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import com.element54.sorter.tags.AudioTag;
import com.element54.sorter.tags.AudioType;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.ID3v2Frame;
import com.mpatric.mp3agic.ID3v2FrameSet;
import com.mpatric.mp3agic.ID3v2TextFrameData;
import com.mpatric.mp3agic.InvalidDataException;

public class MP3agicTag implements AudioTag {
    private static final Pattern TRACK_PATTERN = Pattern.compile("^(\\d+)(/\\d+)?$");
    private static final Pattern DISK_PATTERN = Pattern.compile("^(\\d+)(/\\d+)?$");
    private static final Pattern YEAR_PATTERN = Pattern.compile("^(\\d+)(/\\d+)?$");

    private final ID3v2 tag;

    private String artist = null;
    private String albumArtist = null;
    private String album = null;
    private String title = null;
    private Integer year = null;
    private Integer track = null;
    private Integer disk = null;
    private boolean compilation = false;
    private AudioType type = null;
    private final Path file;

    public MP3agicTag(final ID3v2 tag, final Path file) {
        this.tag = tag;
        this.file = file;

        this.type = getAudioType(tag);
        this.compilation = tag.isCompilation();
        this.artist = tag.getArtist();
        if ("Various Artists".equalsIgnoreCase(this.artist)) {
            this.artist = null;
        }
        this.albumArtist = tag.getAlbumArtist();
        if ("Various Artists".equalsIgnoreCase(this.albumArtist)) {
            this.albumArtist = null;
        }
        this.album = tag.getAlbum();
        this.title = tag.getTitle();
        this.year = parseYear(tag);

        if (tag.getTrack() != null) {
            final String s = tag.getTrack();
            final Matcher m = TRACK_PATTERN.matcher(s);
            if (m.matches()) {
                this.track = Integer.valueOf(m.group(1));
            }
        }
        if (tag.getPartOfSet() != null) {
            final String s = tag.getPartOfSet();
            final Matcher m = DISK_PATTERN.matcher(s);
            if (m.matches()) {
                this.disk = Integer.valueOf(m.group(1));
            }
        }

    }

    private Integer parseYear(ID3v2 tag) {
        if (tag.getYear() != null) {
            final Integer year = filterYear(tag.getYear());
            if (year != null) {
                return year;
            }
        }
        if (this.year == null && tag.getFrameSets().containsKey("TDRC")) {
            final ID3v2TextFrameData frameData = extractTextFrameData(tag, "TDRC");
            if (frameData != null && frameData.getText() != null) {
                return filterYear(frameData.getText().toString());
            }
        }
        return null;
    }

    private Integer filterYear(String str) {
        try {
            return Integer.valueOf(str);
        } catch (final NumberFormatException e) {
            final Matcher m = YEAR_PATTERN.matcher(str);
            if (m.matches()) {
                return Integer.valueOf(m.group(1));
            } else {
                return null;
            }
        }
    }

    private AudioType getAudioType(final ID3v2 tag) {
        final String audiobook = getCustom("AUDIOBOOK");
        if (audiobook != null) {
            return AudioType.AUDIOBOOK;
        }
        return AudioType.MUSIC;
    }

    @Override
    public String getCustom(final String name) {
        if (this.tag instanceof ID3v23Tag) {
            return getCustom23(name);
        } else {
            return getCustom24(name);
        }
    }

    public String getCustom23(final String name) {
        final Map<String, ID3v2FrameSet> map = this.tag.getFrameSets();
        final ID3v2FrameSet set = map.get("TXXX");
        if (set == null) {
            return null;
        }
        final List<ID3v2Frame> frames = set.getFrames();
        for (final ID3v2Frame f : frames) {
            try {
                final byte[] data = f.getData();
                final ID3v2TextFrameData frameData = new ID3v2TextFrameData(false, f.getData());
                final String str = frameData.getText().toString();
                final int i = str.indexOf(0);
                if (i <= 0) {
                    continue;
                }
                final String key = str.substring(0, i);
                if (name.equalsIgnoreCase(key)) {
                    return str.substring(i + 1, str.length());
                }
            } catch (final InvalidDataException e) {
                continue;
            }
        }
        return null;
    }

    public String getCustom24(final String name) {
        final Map<String, ID3v2FrameSet> map = this.tag.getFrameSets();
        final ID3v2FrameSet set = map.get("TXXX");
        if (set == null) {
            return null;
        }
        final List<ID3v2Frame> frames = set.getFrames();
        for (final ID3v2Frame f : frames) {
            final byte[] data = f.getData();

            final int i1 = ArrayUtils.indexOf(data, (byte) 0, 1);
            final int i2 = ArrayUtils.indexOf(data, (byte) 0, i1 + 1);
            if (i1 >= i2 || i1 < 0 || i2 < 0) {
                continue;
            }
            final String key = new String(Arrays.copyOfRange(data, 1, i1));
            final String val = new String(Arrays.copyOfRange(data, i1 + 1, i2));
            if (key.equalsIgnoreCase(name)) {
                return val;
            }
        }
        return null;
    }

    protected ID3v2TextFrameData extractTextFrameData(ID3v2 tag, String id) {
        final Map<String, ID3v2FrameSet> frameSets = this.tag.getFrameSets();
        final ID3v2FrameSet frameSet = frameSets.get(id);
        if (frameSet != null) {
            final ID3v2Frame frame = frameSet.getFrames().get(0);
            ID3v2TextFrameData frameData;
            try {
                frameData = new ID3v2TextFrameData(false, frame.getData());
                return frameData;
            } catch (final InvalidDataException e) {
                // do nothing
            }
        }
        return null;
    }

    @Override
    public String getArtist() {
        return this.artist;
    }

    @Override
    public String getAlbumArtist() {
        return this.albumArtist;
    }

    @Override
    public String getAlbum() {
        return this.album;
    }

    @Override
    public Integer getDisk() {
        return this.disk;
    }

    @Override
    public Integer getTrack() {
        return this.track;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public Integer getYear() {
        return this.year;
    }

    @Override
    public AudioType getAudioType() {
        return this.type;
    }

    @Override
    public boolean isCompilation() {
        return this.compilation;
    }

    @Override
    public Path getFile() {
        return this.file;
    }

}
