package es.caib.comanda.ms.log.helper;

import java.io.InputStream;

public class LogFileStream {

    private final InputStream inputStream;
    private final String fileName;
    private final long size;
    private final String contentType;

    public LogFileStream(InputStream inputStream,
                         String fileName,
                         long size,
                         String contentType) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }

    public InputStream getInputStream() { return inputStream; }
    public String getFileName() { return fileName; }
    public long getSize() { return size; }
    public String getContentType() { return contentType; }
}
