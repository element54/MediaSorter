package com.element54.sorter;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.element54.sorter.tags.AudioTag;
import com.element54.sorter.tags.AudioTagService;
import com.element54.utils.file.FileNameCleaner;
import com.element54.utils.file.PathUtils;

public class StructuredSorterService implements SorterService {

    private final AudioTagService audioTagService;
    private final FileNameCleaner fileNameCleaner;

    public StructuredSorterService(AudioTagService audioTagService, FileNameCleaner fileNameCleaner) {
        super();
        this.audioTagService = audioTagService;
        this.fileNameCleaner = fileNameCleaner;
    }

    @Override
    public Path getFile(Path file, Path destFolder) throws SorterServiceException {
        final AudioTag tag;
        try {
            tag = this.audioTagService.readTag(file);
        } catch (final IOException e) {
            throw new SorterServiceException("cannot read tags", e, file);
        }
        final String ext = PathUtils.getExtension(file);
        final boolean audiobook = tag.isAudiobook();
        final boolean music = tag.isMusic();
        try {
            if (audiobook) {
                return getFileAudiobook(destFolder, tag, ext);
            } else if (music) {
                return getFileMusic(destFolder, tag, ext);
            } else {
                throw new SorterServiceException("unknown audiotype " + tag.getAudioType(), null, file);
            }
        } catch (final IOException e) {
            throw new SorterServiceException("IOException", e, file);
        }
    }

    private Path getFileAudiobook(final Path dest, final AudioTag tag, final String ext)
            throws SorterServiceException, IOException {
        final String albumArtist = tag.getAlbumArtist();
        final String artist = tag.getArtist();
        if (artist == null) {
            throw new SorterServiceException("artist lacks", null, tag.getFile());
        }
        if (albumArtist == null) {
            throw new SorterServiceException("albumartist lacks", null, tag.getFile());
        }
        final String numberString = getNumberString(tag, true);
        final String albumString = getAlbumString(tag);
        final Path audiobookFolder = this.fileNameCleaner.create(dest, "Audiobooks");
        final Path artistFolder = this.fileNameCleaner.create(audiobookFolder, albumArtist);
        final Path albumFolder = this.fileNameCleaner.create(artistFolder, albumString);
        return this.fileNameCleaner.create(albumFolder, numberString, ext);
    }

    private Path getFileMusic(final Path dest, final AudioTag tag, final String ext)
            throws SorterServiceException, IOException {
        final Path musicFolder = this.fileNameCleaner.create(dest, "Music");
        if (tag.isCompilation()) {
            return getFileMusicCompilation(musicFolder, tag, ext);
        } else if (tag.getCustom("SINGLE") != null) {
            return getFileMusicSingle(musicFolder, tag, ext);
        } else {
            return getFileMusicDefault(musicFolder, tag, ext);
        }
    }

    private Path getFileMusicDefault(final Path dest, final AudioTag tag, final String ext)
            throws SorterServiceException, IOException {
        final String artist = tag.getArtist();
        if (artist == null) {
            throw new SorterServiceException("artist lacks", null, tag.getFile());
        }
        final String albumArtist = tag.getAlbumArtist();
        if (albumArtist == null) {
            throw new SorterServiceException("albumartist lacks", null, tag.getFile());
        }
        final String albumString = getAlbumString(tag);
        final String fileName = getFileName(tag);
        final Path artistsFolder = this.fileNameCleaner.create(dest, "Artists");
        final Path artistFolder = this.fileNameCleaner.create(artistsFolder, albumArtist);
        final Path albumFolder = this.fileNameCleaner.create(artistFolder, albumString);
        return this.fileNameCleaner.create(albumFolder, fileName, ext);
    }

    private Path getFileMusicCompilation(final Path dest, final AudioTag tag, final String ext)
            throws SorterServiceException, IOException {
        final String artist = tag.getArtist();
        if (artist == null) {
            throw new SorterServiceException("artist lacks", null, tag.getFile());
        }
        final String albumString = getAlbumString(tag);
        final String fileName = getFileName(tag);
        final Integer year = tag.getYear();
        final String compGroup = tag.getCustom("COMPILATIONGROUP") == null ? "Other"
                : tag.getCustom("COMPILATIONGROUP");
        final String compName = tag.getCustom("COMPILATIONNAME");
        final Path compilationFolder = this.fileNameCleaner.create(dest, "Compilations");
        final Path groupFolder = this.fileNameCleaner.create(compilationFolder, compGroup);
        Path nameFolder = null;
        if (compName != null) {
            nameFolder = this.fileNameCleaner.create(groupFolder, year + " " + compName);
        } else {
            nameFolder = this.fileNameCleaner.create(groupFolder, albumString);
        }
        return this.fileNameCleaner.create(nameFolder, fileName, ext);
    }

    private Path getFileMusicSingle(final Path dest, final AudioTag tag, final String ext)
            throws SorterServiceException, IOException {
        final String artist = tag.getArtist();
        if (artist == null) {
            throw new SorterServiceException("artist lacks", null, tag.getFile());
        }
        final String title = tag.getTitle();
        if (title == null) {
            throw new SorterServiceException("title lacks", null, tag.getFile());
        }
        final Path singleFolder = this.fileNameCleaner.create(dest, "Singles");
        final Path artistFolder = this.fileNameCleaner.create(singleFolder, artist);
        return this.fileNameCleaner.create(artistFolder, title, ext);
    }

    private static NumberFormat format2 = new DecimalFormat("00");
    private static NumberFormat format3 = new DecimalFormat("000");

    private String getNumberString(final AudioTag tag, final boolean useFormat3) throws SorterServiceException {
        final Integer disc = tag.getDisk();
        final Integer track = tag.getTrack();
        if (track == null) {
            throw new SorterServiceException("track lacks", null, tag.getFile());
        }
        final NumberFormat format = useFormat3 ? format3 : format2;
        if (disc == null) {
            return format.format(track);
        } else {
            return disc + "-" + format.format(track);
        }
    }

    private String getAlbumString(final AudioTag tag) throws SorterServiceException {
        final boolean noyear = tag.getCustom("NOYEAR") != null;
        final Integer year = tag.getYear();
        final String album = tag.getAlbum();
        if (year == null && !noyear) {
            throw new SorterServiceException("year lacks", null, tag.getFile());
        }
        if (album == null) {
            throw new SorterServiceException("album lacks", null, tag.getFile());
        }
        if (year == null) {
            return album;
        }
        return year + " " + album;
    }

    private String getFileName(final AudioTag tag) throws SorterServiceException {
        final String title = tag.getTitle();
        if (title == null) {
            throw new SorterServiceException("title lacks", null, tag.getFile());
        }
        final String numberString = getNumberString(tag, false);
        return numberString + " " + title;
    }

}
