package com.bookshell.server.MsgBoard.setting;

import java.util.Set;

public class BlackList {
    private boolean msgDisabled;    // 全局禁言
    private boolean autoBlocked;   // 自动拉黑用户（触发消息黑名单后）
    private Set<String> nicknameWords;  // 昵称黑名单
    private Set<String> msgWords;   // 消息黑名单

    public boolean isMsgDisabled() {
        return msgDisabled;
    }

    public void setMsgDisabled(boolean msgDisabled) {
        this.msgDisabled = msgDisabled;
    }

    public boolean isAutoBlocked() {
        return autoBlocked;
    }

    public void setAutoBlocked(boolean autoBlocked) {
        this.autoBlocked = autoBlocked;
    }

    public Set<String> getNicknameWords() {
        return nicknameWords;
    }

    public void setNicknameWords(Set<String> nicknameWords) {
        this.nicknameWords = nicknameWords;
    }

    public Set<String> getMsgWords() {
        return msgWords;
    }

    public void setMsgWords(Set<String> msgWords) {
        this.msgWords = msgWords;
    }
}
