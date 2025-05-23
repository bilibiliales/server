package com.bookshell.server.MsgBoard.setting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Message {
    private String id;
    private String nickname;
    private String content;
    private LocalDateTime timestamp;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestampAsInstant() {
        return timestamp.atZone(ZoneId.systemDefault()).toInstant();
    }

    public void setTimestampFromInstant(Instant instant) {
        this.timestamp = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
