package net.legacylauncher.ui.background;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.ui.background.fx.FxAudioPlayer;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.SimplexNoise;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OldAnimatedBackground extends JComponent implements ISwingBackground {
    private static final long ONE_TICK_IN_MS = 714;
    private static final short DAY_CYCLE_IN_TICKS = 1200;

    private static final Color
            NIGHT_COLOR = new Color(0, 2, 33),
            SUNRISE = new Color(54, 27, 56),
            DAY_COLOR = new Color(141, 189, 233),
            SUNRISE_1 = new Color(227, 58, 58),
            SUNRISE_2 = new Color(227, 175, 58),
            SUN = new Color(255, 255, 255),
            TRANSPARENT = new Color(255, 255, 255, 0);

    private final GrassImage grassImage;
    private final SkyColor skyColor;
    private final CloudImage cloudImage;
    private final SunImage sunImage;
    private final StarsImage starsImage;
    private final DayCycle dayCycle;
    private final TickTimer tickTimer;
    private final Lazy<FxAudioPlayer> audioPlayer;
    private volatile short forcedTimeOfDay = -1;

    public OldAnimatedBackground() {
        dayCycle = new DayCycle(toTimeOfDay(Calendar.getInstance()));
        grassImage = new GrassImage();
        skyColor = new SkyColor();
        cloudImage = new CloudImage();
        sunImage = new SunImage();
        starsImage = new StarsImage();
        tickTimer = new TickTimer();
        audioPlayer = Lazy.of(() -> {
            try {
                return FxAudioPlayer.create(U.makeURL("https://llaun.ch/repo/downloads/mice.mp3"));
            } catch (Throwable t) {
                log.warn("LoopAudioPlayer is not available", t);
                return null;
            }
        });

        if (LegacyLauncher.getInstance().isDebug()) {
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    updateForcedTimeOfDay(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    updateForcedTimeOfDay(null);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    updateForcedTimeOfDay(e);
                }

                private void updateForcedTimeOfDay(MouseEvent e) {
                    forcedTimeOfDay = (short) (e == null ? -1 : (double) e.getX() / getWidth() * DAY_CYCLE_IN_TICKS);
                }
            });
        }
    }

    public Lazy<FxAudioPlayer> getAudioPlayer() {
        return audioPlayer;
    }

    private void startTickWorker() {
        tickTimer.start();
        log.debug("Tick worker started");
    }

    private void stopTickWorker() {
        tickTimer.stop();
        log.debug("Tick worker stopped");
    }

    @Override
    public void paint(Graphics g0) {
        try {
            paint0(g0);
        } catch (RuntimeException rE) {
            log.warn("", rE);
        }
    }

    private void paint0(Graphics g0) {
        final short currentTimeOfDay = forcedTimeOfDay >= 0 ? forcedTimeOfDay : dayCycle.timeOfDay;
        final int width = getWidth(), height = getHeight();
        Graphics2D g = (Graphics2D) g0;

        final BufferedImage scaledGrassImage = grassImage.get(currentTimeOfDay, width, height);
        final int grassY = height - scaledGrassImage.getHeight();

        g.setPaint(skyColor.get(currentTimeOfDay));
        g.fillRect(0, 0, width, grassY);

        final DaySegment segment;
        if ((segment = DaySegment.get(currentTimeOfDay)) != DaySegment.AFTERNOON) {
            final double p = DaySegment.getSegmentCompletion(currentTimeOfDay);
            final float starsOpacity;
            switch (segment) {
                case NIGHT:
                    starsOpacity = 1.f;
                    break;
                case MORNING:
                    starsOpacity = transitionColor(1.f, 0.f, p * 4);
                    break;
                case DUSK:
                    starsOpacity = transitionColor(0.f, 1.f, p < .5 ? 0 : Math.pow(p, 10));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.SrcOver.derive(starsOpacity));
            g.drawImage(starsImage.get(currentTimeOfDay, width, grassY), 0, 0, this);
            g.setComposite(oldComposite);
        }

        g.drawImage(sunImage.get(currentTimeOfDay, width, grassY * 2), sunImage.getSunX(currentTimeOfDay, width, grassY * 2), 0, this);

        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(cloudImage.getOpacity(currentTimeOfDay)));
        g.drawImage(cloudImage.get(currentTimeOfDay, width, grassY), 0, 0, this);
        g.setComposite(oldComposite);

        for (int x = 0; x < width; x += scaledGrassImage.getWidth()) {
            g.drawImage(scaledGrassImage, x, grassY, null);
        }

        if (LegacyLauncher.getInstance() != null && LegacyLauncher.getInstance().isDebug()) {
            g.setPaint(Color.GREEN);
            g.drawString(String.valueOf(currentTimeOfDay) + " " + DaySegment.get(currentTimeOfDay) + " " + DaySegment.getSegmentCompletion(currentTimeOfDay) + " " + DaySegment.getOverallCompletion(currentTimeOfDay), 0, 50);
        }
    }

    @Override
    public void onResize() {
        if (getParent() != null) {
            setSize(getParent().getSize());
        }
    }

    @Override
    public void startBackground() {
        startTickWorker();
        audioPlayer.valueIfInitialized().ifPresent(FxAudioPlayer::play);
    }

    @Override
    public void pauseBackground() {
        stopTickWorker();
        audioPlayer.valueIfInitialized().ifPresent(FxAudioPlayer::pause);
    }

    @Override
    public void loadBackground(String path) {
    }

    static short toTimeOfDay(Calendar calendar) {
        // 14:36 -> 736, 6:56 -> 356, 20:30 -> 1030, etc
        return (short) (calendar.get(Calendar.HOUR_OF_DAY) / 2 * 100 + calendar.get(Calendar.MINUTE));
    }

    static Color transitionColor(Color oldColor, Color newColor, double progress) {
        return new Color(
                transitionColor(oldColor.getRed(), newColor.getRed(), progress),
                transitionColor(oldColor.getGreen(), newColor.getGreen(), progress),
                transitionColor(oldColor.getBlue(), newColor.getBlue(), progress),
                transitionColor(oldColor.getAlpha(), newColor.getAlpha(), progress)
        );
    }

    static int transitionColor(int oldValue, int newValue, double progress) {
        if (progress < 0) {
            return oldValue;
        } else if (progress > 1.) {
            return newValue;
        }
        int value = (int) (oldValue + (newValue - oldValue) * progress);
        if (value < 0) {
            return 0;
        } else if (value > 255) {
            return 255;
        }
        return value;
    }

    static float transitionColor(float oldValue, float newValue, double progress) {
        if (progress < 0) {
            return oldValue;
        } else if (progress > 1.) {
            return newValue;
        }
        float value = (float) (oldValue + (newValue - oldValue) * progress);
        if (value < 0.f) {
            return 0.f;
        } else if (value > 1.f) {
            return 1.f;
        }
        return value;
    }

    static double transitionColor(double oldValue, double newValue, double progress) {
        if (progress < 0) {
            return oldValue;
        } else if (progress > 1.) {
            return newValue;
        }
        double value = oldValue + (newValue - oldValue) * progress;
        if (value < 0.) {
            return 0.;
        } else if (value > 1.) {
            return 1.;
        }
        return value;
    }

    static int transition(int oldValue, int newValue, double progress) {
        if (progress < 0) {
            return oldValue;
        } else if (progress > 1.) {
            return newValue;
        }
        return (int) (oldValue + (newValue - oldValue) * progress);
    }

    static abstract class DynamicImage {
        protected boolean requireSameWorkspaceWidth = false;
        private short requestedTimeOfDay;
        private int requestedWorkspaceWidth, requestedWorkspaceHeight;
        private SoftReference<BufferedImage> imageRef;

        BufferedImage get(short timeOfDay, int width, int height) {
            if (height < 1) {
                imageRef = null;
                return null;
            }
            if (imageRef != null) {
                final BufferedImage oldResizedGrassImage = imageRef.get();
                if (oldResizedGrassImage != null && (!requireSameWorkspaceWidth || requestedWorkspaceWidth == width) &&
                        requestedWorkspaceHeight == height && !needToRepaint(timeOfDay)) {
                    // no need to resize
                    return oldResizedGrassImage;
                }
            }
            final BufferedImage image = generate(timeOfDay, width, height);
            imageRef = new SoftReference<>(image);
            requestedTimeOfDay = timeOfDay;
            requestedWorkspaceHeight = height;
            return image;
        }

        abstract BufferedImage generate(short timeOfDay, int width, int height);

        boolean needToRepaint(short newTimeOfDay) {
            DaySegment oldDaySegment = DaySegment.get(requestedTimeOfDay);
            DaySegment newDaySegment = DaySegment.get(newTimeOfDay);
            if (oldDaySegment != newDaySegment) {
                return true;
            }
            switch (newDaySegment) {
                case NIGHT:
                case AFTERNOON:
                    return false;
                case MORNING:
                case DUSK:
                    return true;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private abstract static class RollingImage extends DynamicImage {
        private final int screens;
        private SoftReference<BufferedImage> imageRef;

        public RollingImage(int screens) {
            this.screens = screens;
        }

        public RollingImage() {
            this(2);
        }

        @Override
        boolean needToRepaint(short newTimeOfDay) {
            return true;
        }

        protected abstract void generateRollingImage(BufferedImage image);

        protected double getRelativeXPos(short timeOfDay) {
            double p = DaySegment.getOverallCompletion(timeOfDay);
            // let A = (screens - (1 / screens)) or (screens - 1.) / screens; e.g. 5 => 4/5, 6 => 5/6, etc.
            // there are basically two parallel graphs.
            // the first starts at x = 0 y = A/2, the second at x = 1/2 y = A.
            // the first one goes down to 0.
            // the second goes down to A/2 to make seamless transition between days
            // both are separated by x = 1/2 because we don't expect rolling images to be visible in the middle of the day
            return ((p > .5 ? 1.5 : .5) - p) * (screens - 1.) / screens;
        }

        @Override
        BufferedImage generate(short timeOfDay, int width, int height) {
            BufferedImage image = getCachedOrGenerate(width * screens, height);
            int x = (int) (getRelativeXPos(timeOfDay) * image.getWidth());
            return image.getSubimage(x, 0, width, height);
        }

        private BufferedImage getCachedOrGenerate(int width, int height) {
            if (imageRef != null) {
                final BufferedImage cached = imageRef.get();
                if (cached != null) {
                    if (cached.getWidth() == width && cached.getHeight() == height) {
                        return cached;
                    }
                }
            }
            BufferedImage generated = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            generateRollingImage(generated);
            imageRef = new SoftReference<>(generated);
            return generated;
        }
    }

    static class StarsImage extends RollingImage {
        private final static int SCALE = 4;

        @Override
        boolean needToRepaint(short newTimeOfDay) {
            double p = DaySegment.getSegmentCompletion(newTimeOfDay);
            switch (DaySegment.get(newTimeOfDay)) {
                case NIGHT:
                    return true;
                case MORNING:
                    return p < .25;
                case AFTERNOON:
                    return false;
                case DUSK:
                    return p > .6;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        protected void generateRollingImage(BufferedImage stars) {
            Graphics2D g = (Graphics2D) stars.getGraphics();
            for (int y = 0; y < stars.getHeight(); y += SCALE) {
                for (int x = 0; x < stars.getWidth(); x += SCALE) {
                    double v = Math.abs(SimplexNoise.noise(x, y));
                    if (v > .98) {
                        double av = SimplexNoise.noise(y, x);
                        float brightness = Math.abs((float) av);
                        g.setPaint(Color.WHITE);
                        g.setComposite(AlphaComposite.SrcOver.derive(brightness));
                        g.fillRect(x, y, SCALE, SCALE);
                    }
                }
            }
        }
    }

    static class CloudImage extends DynamicImage {
        private static final double X_ZOOM = .001, Y_ZOOM = .005, TIME_ZOOM = .001;
        private static final int SQUARE = 50;

        @Override
        boolean needToRepaint(short newTimeOfDay) {
            return true;
        }

        private SoftReference<BufferedImage> imageRef = new SoftReference<>(null);
        private long globalTime = System.currentTimeMillis() / 1000L % 86400L;

        @Override
        BufferedImage generate(short timeOfDay, int width, int height) {
            BufferedImage image = imageRef.get();
            if (image == null || image.getWidth() != width || image.getHeight() != height) {
                // reuse image if possible
                image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                imageRef = new SoftReference<>(image);
            }
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setBackground(new Color(255, 255, 255, 0));
            g.setPaint(Color.WHITE);
            g.clearRect(0, 0, image.getWidth(), image.getHeight());
            final int size = image.getWidth() / SQUARE;
            for (int x = 0; x < image.getWidth(); x += size) {
                for (int y = 0; y < image.getHeight(); y += size) {
                    float v = (float) SimplexNoise.noise(
                            globalTime * TIME_ZOOM * 2. - x * X_ZOOM,
                            y * Y_ZOOM,
                            globalTime * TIME_ZOOM
                    );
                    if (v > .4f) {
                        // fades towards the bottom
                        float yd = 1.f - (float) (y) / (image.getHeight());
                        // base opacity based on noise value
                        g.setComposite(AlphaComposite.SrcOver.derive(v * v * yd));
                        g.fillRect(x, y, size, size);
                    }
                }
            }
            globalTime++;
            return image;
        }

        public float getOpacity(short timeOfDay) {
            double p = DaySegment.getSegmentCompletion(timeOfDay);
            switch (DaySegment.get(timeOfDay)) {
                case NIGHT:
                    return .3f;
                case MORNING:
                    return transitionColor(.3f, 1.f, p);
                case AFTERNOON:
                    return 1.f;
                case DUSK:
                    return transitionColor(1.f, .3f, p);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    static class SunImage extends DynamicImage {
        SunImage() {
        }

        int getSunX(short timeOfDay, int width, int height) {
            double completion = DaySegment.getSegmentCompletion(timeOfDay);
            switch (DaySegment.get(timeOfDay)) {
                case NIGHT:
                    return 0;
                case MORNING:
                    return transition(-width / 3, 0, completion);
                case AFTERNOON:
                    return transition(0, width / 2, completion);
                case DUSK:
                    return transition(width / 2, width - width / 4, completion);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        BufferedImage generate(short timeOfDay, int width, int height) {
            if (DaySegment.get(timeOfDay) == DaySegment.NIGHT) {
                return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            }
            BufferedImage image = new BufferedImage(height, height / 2, BufferedImage.TYPE_INT_ARGB);
            draw(image, timeOfDay);
            return image;
        }

        void draw(BufferedImage image, short timeOfDay) {
            final int width = image.getWidth(), height = image.getHeight();
            Graphics2D g = (Graphics2D) image.getGraphics();
            final int sunX = width / 6, sunWidth = width / 3 * 2, sunHeight = height / 2;
            final double completion = DaySegment.getSegmentCompletion(timeOfDay);
            final Color sunColor;
            final int fromY, toY;
            final int flareY;
            final double flareOpacity;

            switch (DaySegment.get(timeOfDay)) {
                case MORNING:
                    if (completion < .5) {
                        sunColor = transitionColor(SUNRISE_1, SUNRISE_2, completion * 2.);
                    } else {
                        sunColor = transitionColor(SUNRISE_2, SUN, (completion - .5) * 2.);
                    }
                    fromY = height;
                    toY = height - sunHeight;
                    flareOpacity = transitionColor(0., 1., completion * 10.);
                    flareY = transition(sunX, 0, completion);
                    break;
                case AFTERNOON:
                    sunColor = SUN;
                    fromY = height - sunHeight;
                    toY = height - sunHeight;
                    flareOpacity = 1.;
                    flareY = 0;
                    break;
                case DUSK:
                    if (completion < .5) {
                        sunColor = transitionColor(SUN, SUNRISE_2, completion * 2.);
                    } else {
                        sunColor = transitionColor(SUNRISE_2, SUNRISE_1, (completion - .5) * 2.);
                    }
                    fromY = height - sunHeight;
                    toY = height;
                    flareOpacity = 1. - completion;
                    flareY = transition(0, sunHeight / 2, completion);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            final int sunY = (int) (fromY + (toY - fromY) * completion);

            g.setPaint(sunColor);
            g.fillRect(sunX, sunY, sunWidth, sunHeight);

            g.setPaint(transitionColor(TRANSPARENT, U.shiftAlpha(sunColor, -127), flareOpacity));
            g.fillRect(sunX / 2, sunY / 2 + flareY, sunWidth + sunX, sunHeight + sunY + 1);

            g.setPaint(transitionColor(TRANSPARENT, U.shiftAlpha(sunColor, -192), flareOpacity));
            g.fillRect(sunX / 4, sunY / 4 + flareY, sunWidth + sunX * 3 / 2, sunHeight + sunY + 1);
        }
    }

    static class GrassImage extends DynamicImage {
        private static final float NIGHT_COLOR_ALPHA = 0.6f;
        private final BufferedImage grassImage;

        GrassImage() {
            this(Images.loadImageByName("grass.png"));
        }

        GrassImage(BufferedImage grassImage) {
            this.grassImage = Objects.requireNonNull(grassImage, "grassImage");
        }

        BufferedImage generate(short timeOfDay, int width, int height) {
            final int newGrassImageHeight = (int) ((double) height / 2.);
            final int newGrassImageWidth = (int) ((double) newGrassImageHeight / grassImage.getHeight() * grassImage.getWidth());
            final BufferedImage newGrassImage = new BufferedImage(newGrassImageWidth, newGrassImageHeight, BufferedImage.TYPE_INT_RGB);
            draw(newGrassImage, timeOfDay);
            return newGrassImage;
        }

        void draw(BufferedImage grassImage, short timeOfDay) {
            final int width = grassImage.getWidth(), height = grassImage.getHeight();
            Graphics2D g = (Graphics2D) grassImage.getGraphics();
            g.drawImage(this.grassImage, 0, 0, width, height, null);

            float p = (float) DaySegment.getSegmentCompletion(timeOfDay);

            switch (DaySegment.get(timeOfDay)) {
                case NIGHT:
                    g.setComposite(AlphaComposite.SrcOver.derive(NIGHT_COLOR_ALPHA));
                    g.setPaint(NIGHT_COLOR);
                    g.fillRect(0, 0, width, height);
                    break;
                case MORNING:
                    g.setComposite(AlphaComposite.SrcOver.derive(
                            NIGHT_COLOR_ALPHA - (p * NIGHT_COLOR_ALPHA)
                    ));
                    g.setPaint(NIGHT_COLOR);
                    g.fillRect(0, 0, width, height);
                    break;
                case AFTERNOON:
                    break;
                case DUSK:
                    g.setComposite(AlphaComposite.SrcOver.derive((float) DaySegment.getSegmentCompletion(timeOfDay) * NIGHT_COLOR_ALPHA));
                    g.setPaint(NIGHT_COLOR);
                    g.fillRect(0, 0, width, height);
                    break;
            }
        }
    }

    static class SkyColor {
        Color get(short timeOfDay) {
            double p = DaySegment.getSegmentCompletion(timeOfDay);
            switch (DaySegment.get(timeOfDay)) {
                case NIGHT:
                    return transitionColor(NIGHT_COLOR, SUNRISE, Math.pow(p, 8));
                case MORNING:
                    return transitionColor(SUNRISE, DAY_COLOR, p * 2);
                case AFTERNOON:
                    return DAY_COLOR;
                case DUSK:
                    return transitionColor(DAY_COLOR, NIGHT_COLOR, p);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    static class DayCycle {
        private short timeOfDay;

        DayCycle(short timeOfDay) {
            if (timeOfDay < 0 || timeOfDay >= DAY_CYCLE_IN_TICKS) {
                throw new IllegalArgumentException("timeOfDay: " + timeOfDay);
            }
            this.timeOfDay = timeOfDay;
        }

        void add(short ticks) {
            timeOfDay = normalize(timeOfDay + ticks);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("timeOfDay", timeOfDay)
                    .build();
        }

        static short normalize(long ticks) {
            short normalized;
            if (ticks < 0 || ticks >= DAY_CYCLE_IN_TICKS) {
                normalized = 0;
            } else {
                normalized = (short) ticks;
            }
            return normalized;
        }
    }

    enum DaySegment {
        NIGHT, MORNING, AFTERNOON, DUSK;

        private static final DaySegment[] VALUES;
        static final short SEGMENT_LENGTH_IN_TICKS;

        private int segmentIndex;
        private short segmentStart;

        static {
            VALUES = values();
            SEGMENT_LENGTH_IN_TICKS = (short) (DAY_CYCLE_IN_TICKS / VALUES.length);

            for (int i = 0; i < VALUES.length; i++) {
                DaySegment s = VALUES[i];
                s.segmentIndex = i;
                s.segmentStart = (short) (SEGMENT_LENGTH_IN_TICKS * i);
            }
        }

        public boolean isCurrent(short timeOfDay) {
            return DaySegment.getSegmentIndex(timeOfDay) == segmentIndex;
        }

        private static DaySegment getIndex(int index) {
            return VALUES[index];
        }

        private static int getSegmentIndex(short timeOfDay) {
            if (timeOfDay < DaySegment.SEGMENT_LENGTH_IN_TICKS) {
                return 0;
            }
            int index = timeOfDay / DaySegment.SEGMENT_LENGTH_IN_TICKS;
            return Math.min(index, VALUES.length - 1);
        }

        static DaySegment get(short timeOfDay) {
            return getIndex(getSegmentIndex(timeOfDay));
        }

        static double getSegmentCompletion(short timeOfDay) {
            DaySegment segment = get(timeOfDay);
            return ((double) (timeOfDay - segment.segmentStart)) / SEGMENT_LENGTH_IN_TICKS;
        }

        static double getOverallCompletion(short timeOfDay) {
            if (timeOfDay < 0) {
                return 0.;
            }
            return (double) timeOfDay / DAY_CYCLE_IN_TICKS;
        }
    }

    class TickTimer implements Runnable {
        private final ScheduledExecutorService scheduler;
        private final AtomicBoolean started = new AtomicBoolean();
        private long lastDayCycleUpdate;

        TickTimer(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
        }

        TickTimer() {
            this(AsyncThread.DELAYER);
        }

        void start() {
            if (started.compareAndSet(false, true)) {
                run();
            }
        }

        void stop() {
            started.set(false);
        }

        @Override
        public void run() {
            if (!started.get()) {
                return;
            }
            updateDayCycle();
            SwingUtil.later(() -> OldAnimatedBackground.this.repaint());
            scheduler.schedule(
                    this,
                    LegacyLauncher.getInstance().isDebug() ? 250 : ONE_TICK_IN_MS,
                    TimeUnit.MILLISECONDS
            );
        }

        private void updateDayCycle() {
            final long currentTime = System.currentTimeMillis();
            final long msDelta = currentTime - lastDayCycleUpdate;
            if (msDelta < ONE_TICK_IN_MS) {
                return;
            }
            final short deltaTicks = DayCycle.normalize(msDelta / ONE_TICK_IN_MS);
            dayCycle.add(deltaTicks);
            lastDayCycleUpdate = currentTime;
        }
    }
}
