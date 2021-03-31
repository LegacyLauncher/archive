package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.notice.Notice;
import ru.turikhay.tlauncher.ui.notice.NoticeAction;
import ru.turikhay.tlauncher.ui.notice.UrlNoticeImage;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class RickrollManager {
    public static final int SPECIAL_NOTICE_ID = 90000;

    private static boolean enabled;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void trigger() {
        if(!TLauncher.getInstance().isReady()) {
            throw new IllegalStateException();
        }

        if(enabled) {
            return;
        }

        enabled = true;
        TLauncher.getInstance().getFrame().mp.background.loadBackground();
        TLauncher.getInstance().getSettings().set("aprilFools", 2021);

        AsyncThread.execute(() -> {
            Alert.showMessage("Попались!", "Это неправда, мы пошутили. С днём смеха!");
        });
    }

    public static boolean isApril1st() {
        return Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL &&
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1 &&
                TLauncher.getInstance().getSettings().getInteger("aprilFools") != 2021;
    }

    public static Notice getAprilFoolsNotice() {
        NoticeAction action = new NoticeAction("april-fools") {
            @Override
            protected List<? extends JMenuItem> getMenuItemList() {
                trigger();
                return Collections.singletonList(
                        new JMenuItem("Хи-хи")
                );
            }
        };
        return U.getRandom(new Notice[]{
                new Notice(SPECIAL_NOTICE_ID, 0,
                        "<b>Нотч вернулся в Mojang и выкупил Minecraft</b><br/>По данным Bloomberg сделка обошлась в $20 млрд",
                        new UrlNoticeImage(Images.getRes("mojang.png")),
                        action),
                new Notice(SPECIAL_NOTICE_ID, 0,
                        "<b>РКН заблокирует доступ к YouTube, TikTok и Likee</b>.<br/>Ограничение не обойдет даже VPN »",
                        new UrlNoticeImage(Images.getRes("warning.png")),
                        action),
                new Notice(SPECIAL_NOTICE_ID, 0,
                        "<b>В 1.18 анонсировали новое измерение: Эфир</b><br/>Любимый мод многих игроков теперь в ваниле!",
                        new UrlNoticeImage(Images.getRes("mojang.png")),
                        action),
                new Notice(SPECIAL_NOTICE_ID, 0,
                        "<b>Влад А4 заявил, что уходит с YouTube</b><br/>Многомиллионная аудитория в шоке",
                        new UrlNoticeImage(Images.getRes("youtube.png")),
                        action),
                new Notice(SPECIAL_NOTICE_ID, 0,
                        "<b>Minecraft 1.18 не выйдет на Java Edition</b><br/>Разработчики решили сконцентрироваться на Bedrock Edition",
                        new UrlNoticeImage(Images.getRes("mojang.png")),
                        action),
                new Notice(SPECIAL_NOTICE_ID, 0,
                        "<b>Теперь за читы в Minecraft будут штрафовать</b><br/>Разработчики решили сконцентрироваться на Bedrock Edition",
                        new UrlNoticeImage(Images.getRes("mojang.png")),
                        action),
                new Notice(SPECIAL_NOTICE_ID, 0,
                        "<b>В Brawl Stars началась раздача гемов и вещей из Brawl Pass</b><br/>Чтобы их получить, нужно выполнить одно условие »",
                        new UrlNoticeImage(Images.getRes("star.png")),
                        action),
        });
    }
}
