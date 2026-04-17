package es.caib.comanda.ms.log.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogFileStream {

    private final InputStream inputStream;
    private final String fileName;
    private final long size;
    private final String contentType;
    private final Path tempFilePath;

    public LogFileStream(InputStream inputStream, String fileName, long size, String contentType) {
        this(inputStream, fileName, size, contentType, null);
    }

    public LogFileStream(InputStream inputStream, String fileName, long size, String contentType, Path tempFilePath) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.tempFilePath = tempFilePath;
    }

    public InputStream getInputStream() {
        if (tempFilePath == null) {
            return inputStream;
        }
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return inputStream.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return inputStream.read(b, off, len);
            }

            @Override
            public void close() throws IOException {
                try {
                    inputStream.close();
                } finally {
                    Files.deleteIfExists(tempFilePath);
                }
            }
        };
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public Path getTempFilePath() {
        return tempFilePath;
    }
}
