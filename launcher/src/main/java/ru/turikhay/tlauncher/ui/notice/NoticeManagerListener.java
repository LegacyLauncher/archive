package ru.turikhay.tlauncher.ui.notice;

public interface NoticeManagerListener {
    void onNoticeSelected(Notice notice);

    void onNoticePromoted(Notice promotedNotice);
}
