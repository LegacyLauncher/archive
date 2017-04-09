package ru.turikhay.tlauncher.ui.notice;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NoticePanelTest {

    @Test
    public void test() throws Exception {
        Notice notice = new Notice(
                1, -1,
                "<b>MineMages</b> – пожалуй, лучший сервер с <b>мини-играми</b>: <b>SkyWars</b>, <b>BedWars</b>, а также <b>RPG</b> и <b>Survival</b>! А ещё у нас регулярный <b>Розыгрыш Админки</b>!",
                NoticeImage.DEFAULT_IMAGE,
                null
        );
        NoticePanel panel = new NoticePanel(null);
        panel.setNotice(notice);
    }

}