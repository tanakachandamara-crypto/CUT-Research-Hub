package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Paper implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private String title, content;
    private final LocalDateTime uploadDate = LocalDateTime.now();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Paper(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getUploadDate() { return uploadDate; }

    @Override
    public String toString() {
        return title + " (uploaded " + DTF.format(uploadDate) + ")";
    }
}