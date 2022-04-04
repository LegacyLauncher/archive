package ru.turikhay.tlauncher.ui.notice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.configuration.BootConfiguration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.util.U;

import java.awt.*;
import java.util.List;
import java.util.*;

public final class NoticeManager implements LocalizableComponent, Blockable {
    private static final Logger LOGGER = LogManager.getLogger(NoticeManager.class);

    private static final int HIDDEN_DELAY = 1000 * 60 * 60 * 24 * 7; // 1 week

    private final TLauncherFrame frame;

    private final List<NoticeManagerListener> listeners = new ArrayList<>();
    private final Map<Locale, List<Notice>> byLocaleMap = new HashMap<>();
    private final Map<Notice, NoticeTextSize> cachedSizeMap = new HashMap<>();

    private Notice selectedNotice, promotedNotice;
    private boolean forceSelected;

    NoticeManager(TLauncherFrame frame, Map<String, List<Notice>> config) {
        this.frame = frame;

        if (!config.isEmpty()) {
            for (Map.Entry<String, List<Notice>> entry : config.entrySet()) {
                String key = entry.getKey();
                Locale locale = U.getLocale(entry.getKey());

                if (locale == null) {
                    LOGGER.warn("Couldn't parse locale: {}", key);
                    continue;
                }
                if (entry.getValue() == null) {
                    LOGGER.warn("Notice list is null: {}", key);
                    continue;
                }
                if (entry.getValue().isEmpty()) {
                    LOGGER.debug("Notice list is empty: {}", key);
                    continue;
                }

                List<Notice> noticeList = new ArrayList<>();
                for (Notice notice : entry.getValue()) {
                    if (notice == null) {
                        LOGGER.warn("Found null selectedNotice in {}", key);
                        continue;
                    }
                    noticeList.add(notice);

                    NoticeTextSize textSize = new NoticeTextSize(notice);
                    textSize.get(new ParamPair(TLauncherFrame.getFontSize(), -1));

                    cachedSizeMap.put(notice, textSize);
                }

                Collections.shuffle(noticeList);
                noticeList.sort(Comparator.comparingInt(Notice::getPos));

                byLocaleMap.put(locale, Collections.unmodifiableList(noticeList));
                LOGGER.debug("Added {} notices for {}", noticeList.size(), locale);
            }

            if (frame != null) {
                Locale ruRU = U.getLocale("ru_RU"), ukUA = U.getLocale("uk_UA");
                if (ruRU != null && ukUA != null && byLocaleMap.get(ruRU) != null && byLocaleMap.get(ukUA) == null) {
                    byLocaleMap.put(ukUA, byLocaleMap.get(ruRU));
                }

                List<Notice> globalNoticeList = byLocaleMap.get(Locale.US);
                if (globalNoticeList != null) {
                    for (Locale locale : LangConfiguration.getAvailableLocales()) {
                        byLocaleMap.putIfAbsent(locale, globalNoticeList);
                    }
                }
                /*Locale current = frame.getLauncher().getLang().getLocale(), enUS = Locale.US;
                if(byLocaleMap.get(current) == null && enUS != null) {
                    byLocaleMap.put(current, byLocaleMap.get(enUS));
                }*/
            }

            /*AsyncThread.execute(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        if (forceSelected) {
                            return;
                        }
                        if(!changeLocked) {
                            selectRandom();
                        }
                        U.sleepFor(60 * 1000);
                    }
                }
            });*/
            selectRandom();
        } else {
            LOGGER.debug("Notice map is empty");
        }
    }

    public NoticeManager(TLauncherFrame frame, BootConfiguration config) {
        this(frame, config.getNotices());
    }

    public void addListener(NoticeManagerListener l, boolean updateImmidiately) {
        listeners.add(Objects.requireNonNull(l, "listener"));
        l.onNoticeSelected(selectedNotice);
        if (promotedNotice != null) {
            final Notice promoted = promotedNotice;
            l.onNoticePromoted(promoted);
        }
    }

    public Notice getSelectedNotice() {
        return selectedNotice;
    }

    public Notice getPromotedNotice() {
        return promotedNotice;
    }

    public List<Notice> getForCurrentLocale() {
        if (frame == null) {
            return null;
        }
        Locale currentLocale = frame.getLauncher().getLang().getLocale();
        return getForLocale(currentLocale);
    }

    public List<Notice> getForLocale(Locale locale) {
        return byLocaleMap.get(Objects.requireNonNull(locale, "locale"));
    }

    public void selectNotice(Notice notice, boolean forceSet) {
        if (this.forceSelected && !forceSet) {
            return;
        }

        this.selectedNotice = notice;
        this.forceSelected = notice != null && forceSet;
        for (NoticeManagerListener l : listeners) {
            l.onNoticeSelected(notice);
        }
    }

    public void setPromoted(Notice promoted) {
        this.promotedNotice = promoted;
        for (NoticeManagerListener l : listeners) {
            l.onNoticePromoted(promoted);
        }
    }

    public boolean isPromotedAllowed() {
        return frame.getLauncher().getSettings().getBoolean("notice.promoted");
    }

    public void setPromotedAllowed(boolean allowPromoted) {
        frame.getLauncher().getSettings().set("notice.promoted", allowPromoted);
        if (allowPromoted) {
            pickPromoted(getForCurrentLocale());
        } else {
            setPromoted(null);
        }
    }

    private NoticeTextSize getTextSize(Notice notice) {
        Objects.requireNonNull(notice, "notice");
        NoticeTextSize cachedSize = cachedSizeMap.get(notice);
        if (cachedSize == null) {
            cachedSizeMap.put(notice, cachedSize = new NoticeTextSize(notice));
        }
        return cachedSize;
    }

    Dimension getTextSize(Notice notice, ParamPair param) {
        return getTextSize(notice).get(param);
    }

    public boolean isHidden(Notice notice) {
        if (notice == null) {
            return false;
        }
        long expiryDate = frame.getLauncher().getSettings().getLong("notice.id." + notice.getId());
        if (System.currentTimeMillis() > expiryDate) {
            setHidden(notice, false, false);
            return false;
        }
        return true;
    }

    private void setHidden(Notice notice, boolean hidden, boolean notify) {
        if (notify) {
            if (hidden) {
                Stats.noticeHiddenByUser(notice);
            } else {
                Stats.noticeShownByUser(notice);
            }
        }
        frame.getLauncher().getSettings().set("notice.id." + notice.getId(), hidden ? System.currentTimeMillis() + HIDDEN_DELAY : null);
        if (hidden && selectedNotice == notice) {
            selectNotice(null, true);
        }
        if (hidden && promotedNotice == notice) {
            setPromoted(null);
        }
    }

    public void setHidden(Notice notice, boolean hidden) {
        setHidden(notice, hidden, true);
    }

    private void setAllHidden(boolean hidden) {
        List<Notice> noticeList = getForCurrentLocale();
        if (noticeList == null || noticeList.isEmpty()) {
            return;
        }
        for (Notice notice : noticeList) {
            setHidden(notice, hidden, false);
        }
    }

    public void setAllHidden() {
        setAllHidden(true);
    }

    public void restoreHidden() {
        setAllHidden(false);
        selectRandom();
    }

    @Override
    public void updateLocale() {
        selectRandom();
    }

    public void selectRandom() {
        Notice selected = null;
        List<Notice> list = getForCurrentLocale();
        if (!frame.getLauncher().isNoticeDisabled() && list != null) {
            List<Notice> available = new ArrayList<>();
            for (Notice notice : list) {
                if (isHidden(notice)) {
                    continue;
                }
                available.add(notice);
            }
            if (!available.isEmpty()) {
                selected = available.get(new Random().nextInt(available.size()));
            }
        }
        pickPromoted(list);
        selectNotice(selected, false);
    }

    private void pickPromoted(List<Notice> list) {
        Notice promoted = null;
        boolean promotedAllowed = isPromotedAllowed();
        if (promotedAllowed && list != null) {
            for (Notice notice : list) {
                if (!notice.isPromoted()) {
                    continue;
                }
                if (isHidden(notice)) {
                    continue;
                }
                promoted = notice;
                break;
            }
        }
        this.promotedNotice = promoted;

        for (NoticeManagerListener l : listeners) {
            l.onNoticePromoted(promoted);
        }
    }

    @Override
    public void block(Object var1) {
    }

    @Override
    public void unblock(Object var1) {
    }
}
