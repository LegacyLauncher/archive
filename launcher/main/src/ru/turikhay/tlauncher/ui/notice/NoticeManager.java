package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.configuration.BootConfiguration;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.scenes.NoticeScene;
import ru.turikhay.tlauncher.updater.Stats;
import ru.turikhay.util.U;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class NoticeManager implements LocalizableComponent {
    private static final int HIDDEN_DELAY = 1000 * 60 * 60 * 24 * 7; // 1 week

    private final TLauncherFrame frame;

    private final List<NoticeManagerListener> listeners = new ArrayList<NoticeManagerListener>();
    private final Map<Locale, List<Notice>> byLocaleMap = new HashMap<Locale, List<Notice>>();
    private final Map<Notice, NoticeTextSize> cachedSizeMap = new HashMap<Notice, NoticeTextSize>();

    private Notice selectedNotice;

    NoticeManager(TLauncherFrame frame, Map<String, List<Notice>> config) {
        this.frame = frame;

        if(!config.isEmpty()) {
            for (Map.Entry<String, List<Notice>> entry : config.entrySet()) {
                String key = entry.getKey();
                Locale locale = U.getLocale(entry.getKey());

                if (locale == null) {
                    log("[WARN] Couldn't parse locale:", key);
                    continue;
                }
                if (entry.getValue() == null) {
                    log("[WARN] Notice list is null:", key);
                    continue;
                }
                if (entry.getValue().isEmpty()) {
                    log("[WARN] Notice list is empty:", key);
                    continue;
                }

                List<Notice> noticeList = new ArrayList<Notice>();
                for (Notice notice : entry.getValue()) {
                    if (notice == null) {
                        log("[WARN] Found null selectedNotice in", key);
                        continue;
                    }
                    noticeList.add(notice);

                    NoticeTextSize textSize = new NoticeTextSize(notice);
                    textSize.pend(new ParamPair(TLauncherFrame.getFontSize(), -1));
                    //textSize.pend(TLauncherFrame.getFontSize() + NoticeScene.ADDED_FONT_SIZE);

                    cachedSizeMap.put(notice, textSize);
                }

                U.shuffle(noticeList);

                List<Notice> noticesThatMadeMeReplaceOthersDamnIHateIt = new ArrayList<>();
                for (int i = 0; i < noticeList.size(); i++) {
                    Notice notice = noticeList.get(i);
                    if(noticesThatMadeMeReplaceOthersDamnIHateIt.contains(notice)) {
                        continue;
                    }
                    int pos = notice.getPos();
                    if(pos > -1) {
                        if(pos >= noticeList.size()) {
                            pos = noticeList.size() - 1;
                        }
                        Notice replacement = noticeList.get(pos);
                        noticeList.set(i, replacement);
                        noticeList.set(pos, notice);

                        noticesThatMadeMeReplaceOthersDamnIHateIt.add(notice);

                        i =- 1; // redo the position
                    }
                }

                byLocaleMap.put(locale, Collections.unmodifiableList(noticeList));
                log("Added", noticeList.size(), "notices for", locale);
            }

            if(frame != null){
                Locale ruRU = U.getLocale("ru_RU"), ukUA = U.getLocale("uk_UA");
                if(ruRU != null && ukUA != null && byLocaleMap.get(ruRU) != null && byLocaleMap.get(ukUA) == null) {
                    byLocaleMap.put(ukUA, byLocaleMap.get(ruRU));
                }

                List<Notice> globalNoticeList = byLocaleMap.get(Locale.US);
                if(globalNoticeList != null) {
                    for(Locale locale : LangConfiguration.getAvailableLocales()) {
                        if(byLocaleMap.get(locale) == null) {
                            byLocaleMap.put(locale, globalNoticeList);
                        }
                    }
                }
                /*Locale current = frame.getLauncher().getLang().getLocale(), enUS = Locale.US;
                if(byLocaleMap.get(current) == null && enUS != null) {
                    byLocaleMap.put(current, byLocaleMap.get(enUS));
                }*/
            }

            selectRandom();
        } else {
            log("[WARN] Notice map is empty");
        }
    }

    public NoticeManager(TLauncherFrame frame, BootConfiguration config) {
        this(frame, config.getNotices());
    }

    public void addListener(NoticeManagerListener l, boolean updateImmidiately) {
        listeners.add(U.requireNotNull(l, "listener"));
        l.onNoticeSelected(selectedNotice);
    }

    public Notice getSelectedNotice() {
        return selectedNotice;
    }

    public List<Notice> getForCurrentLocale() {
        if(frame == null) {
            return null;
        }
        Locale currentLocale = frame.getLauncher().getLang().getLocale();
        return getForLocale(currentLocale);
    }

    public List<Notice> getForLocale(Locale locale) {
        return byLocaleMap.get(U.requireNotNull(locale, "locale"));
    }

    public void selectNotice(Notice notice) {
        this.selectedNotice = notice;
        for(NoticeManagerListener l :listeners) {
            l.onNoticeSelected(notice);
        }
    }

    private NoticeTextSize getTextSize(Notice notice) {
        U.requireNotNull(notice, "notice");
        NoticeTextSize cachedSize = cachedSizeMap.get(notice);
        if(cachedSize == null) {
            cachedSizeMap.put(notice, cachedSize = new NoticeTextSize(notice));
        }
        return cachedSize;
    }

    Dimension getTextSize(Notice notice, ParamPair param) {
        return getTextSize(notice).get(param);
    }

    public void preloadNotice(Notice notice, float fontSize) {
        getTextSize(notice).pend(new ParamPair(fontSize, -1));
    }

    public boolean isHidden(Notice notice) {
        if(notice == null) {
            return false;
        }
        long expiryDate = frame.getLauncher().getSettings().getLong("notice.id." + notice.getId());
        if(System.currentTimeMillis() > expiryDate) {
            setHidden(notice, false, false);
            return false;
        }
        return true;
    }

    private void setHidden(Notice notice, boolean hidden, boolean notify) {
        if(notify) {
            if (hidden) {
                Stats.noticeHiddenByUser(notice);
            } else {
                Stats.noticeShownByUser(notice);
            }
        }
        frame.getLauncher().getSettings().set("notice.id." + notice.getId(), hidden? System.currentTimeMillis() + HIDDEN_DELAY : null);
    }

    public void setHidden(Notice notice, boolean hidden) {
        setHidden(notice, hidden, true);
    }

    public void setAllHidden() {
        List<Notice> noticeList = getForCurrentLocale();
        if(noticeList == null || noticeList.isEmpty()) {
            return;
        }
        for(Notice notice : noticeList) {
            setHidden(notice, true, false);
        }
    }

    @Override
    public void updateLocale() {
        selectRandom();
    }

    private final String logPrefix = '[' + getClass().getSimpleName() + ']';

    private void log(Object... o) {
        U.log(logPrefix, o);
    }

    public void selectRandom() {
        Notice selected = null;
        selecting: {
            List<Notice> list = getForCurrentLocale();

            if(list == null) {
                break selecting;
            }

            List<Notice> available = new ArrayList<Notice>();
            for(Notice notice : list) {
                if(isHidden(notice)) {
                    continue;
                }
                available.add(notice);
            }
            if(available.isEmpty()) {
                break selecting;
            }

            selected = available.get(new Random().nextInt(available.size()));
        }
        selectNotice(selected);
    }
}
