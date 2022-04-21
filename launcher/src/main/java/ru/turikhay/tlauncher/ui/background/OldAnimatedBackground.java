package ru.turikhay.tlauncher.ui.background;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

public class OldAnimatedBackground extends JComponent implements ISwingBackground {
    private static final Logger LOGGER = LogManager.getLogger(OldAnimatedBackground.class);

    private static final long ONE_TICK_IN_MS = 1000;
    private static final short DAY_CYCLE_IN_TICKS = 1200;

    private static final Color
            NIGHT_COLOR = Color.decode("#000221"),
            DAY_COLOR = Color.decode("#8dbde9"),

    SUNRISE_1 = new Color(227, 58, 58),
            SUNRISE_2 = new Color(227, 175, 58),
            SUN = new Color(255, 255, 255),

    TRANSPARENT = new Color(255, 255, 255, 0);

    private final GrassImage grassImage;
    private final SkyColor skyColor;
    private final SunImage sunImage;
    private final StarsImage starsImage;

    private final DayCycle dayCycle;
    private TickTimer tickTimer;

    public OldAnimatedBackground() {
        dayCycle = new DayCycle(toTimeOfDay(Calendar.getInstance()));//(U.random(0, DAY_CYCLE_IN_TICKS)));
        grassImage = new GrassImage();
        skyColor = new SkyColor();
        sunImage = new SunImage();
        starsImage = new StarsImage();
        //tickTimer = new TickTimer();
        //currentDayCycle = new DayCycle((short) (DAY_CYCLE_IN_TICKS / 2));
        //lastDayCycleUpdate = System.currentTimeMillis();
    }

    private void startTickWorker() {
        if (tickTimer != null) {
            return;
        }
        tickTimer = new TickTimer(this::repaint);
        tickTimer.execute();
        LOGGER.debug("Tick worker started");
    }

    private void stopTickWorker() {
        if (tickTimer == null) {
            return;
        }
        tickTimer.cancel(true);
        tickTimer = null;
        LOGGER.debug("Tick worker stopped");
    }

    @Override
    public void paint(Graphics g0) {
        final short currentTimeOfDay = dayCycle.timeOfDay;
        final int width = getWidth(), height = getHeight();
        Graphics2D g = (Graphics2D) g0;

        final BufferedImage scaledGrassImage = grassImage.get(currentTimeOfDay, width, height);
        final int grassY = height - scaledGrassImage.getHeight();

        g.setPaint(skyColor.get(currentTimeOfDay));
        g.fillRect(0, 0, width, grassY);

        final DaySegment segment;
        if ((segment = DaySegment.get(currentTimeOfDay)) != DaySegment.AFTERNOON) {
            final double completion = DaySegment.getSegmentCompletion(currentTimeOfDay);
            final float starsOpacity;
            switch (segment) {
                case NIGHT:
                    starsOpacity = 1.f;
                    break;
                case MORNING:
                    starsOpacity = transitionColor(1.f, 0.f, completion * 2.);
                    break;
                case DUSK:
                    starsOpacity = transitionColor(0.f, 1.f, completion);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.SrcOver.derive(starsOpacity));
            g.drawImage(starsImage.get(currentTimeOfDay, width, grassY * 2), 0, 0, null);
            g.setComposite(oldComposite);
        }

        g.drawImage(sunImage.get(currentTimeOfDay, width, grassY * 2), sunImage.getSunX(currentTimeOfDay, width, grassY * 2), 0, null);

        for (int x = 0; x < width; x += scaledGrassImage.getWidth()) {
            g.drawImage(scaledGrassImage, x, grassY, null);
        }

        if (TLauncher.getInstance() != null && TLauncher.getInstance().isDebug()) {
            g.setPaint(Color.GREEN);
            g.drawString(String.valueOf(currentTimeOfDay) + " " + DaySegment.get(currentTimeOfDay) + " " + DaySegment.getSegmentCompletion(currentTimeOfDay), 0, 50);
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
    }

    @Override
    public void pauseBackground() {
        stopTickWorker();
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

    static class StarsImage extends DynamicImage {
        private final static int STAR_RAND_BOUND = 200, STAR_RAND_GUESS = 0, SCALE_DIFF = 120, SCALE_MIN_ADD = 4;

        private final long seed;
        private final Random random;
        private final java.util.List<Star> stars = new ArrayList<>();

        StarsImage(long seed) {
            this.requireSameWorkspaceWidth = true;

            this.seed = seed;
            this.random = new Random(seed);
        }

        StarsImage() {
            this(System.currentTimeMillis());
        }

        @Override
        boolean needToRepaint(short newTimeOfDay) {
            return false;
        }

        private void generateStars(int width, int height, int scale) {
            random.setSeed(seed);
            stars.clear();

            for (int y = 0; y < height; y += scale) {
                for (int x = 0; x < width; x += scale) {
                    if (random.nextInt(STAR_RAND_BOUND) == STAR_RAND_GUESS) {
                        stars.add(new Star(x, y, random.nextFloat()));
                    }
                }
            }
        }

        @Override
        BufferedImage generate(short timeOfDay, int width, int height) {
            final int scale = height / SCALE_DIFF + SCALE_MIN_ADD;
            generateStars(width, height, scale);
            BufferedImage image = new BufferedImage(width, height / 2 + 1, BufferedImage.TYPE_INT_ARGB);
            draw(image, scale);
            return image;
        }

        void draw(BufferedImage bufferedImage, int scale) {
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            for (Star star : stars) {
                g.setPaint(Color.WHITE);
                g.setComposite(AlphaComposite.SrcOver.derive(star.brightness));
                g.fillRect(star.x, star.y, scale, scale);
            }
        }

        static class Star {
            private final int x, y;
            private final float brightness;

            public Star(int x, int y, float brightness) {
                this.x = x;
                this.y = y;
                this.brightness = brightness;
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
            //g.setPaint(new Color(255, 0, 0, 128));
            //g.fillRect(0, 0, width, height);

            //g.setPaint(new Color(255, 255, 0, 128));
            //g.fillRect(width / 6, height / 2, width / 3 * 2, height);

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
                        //flareY = 0;//transition(, 0, (completion - .5) * 2.);
                    } else {
                        sunColor = transitionColor(SUNRISE_2, SUN, (completion - .5) * 2.);
                        //flareY = 0;//transition(sunHeight / 4, 0, (completion - .5) * 2.);
                    }
                    //sunY = transition(height, height - sunHeight, completion);
                    fromY = height;
                    toY = height - sunHeight;
                    flareOpacity = transitionColor(0., 1., completion * 10.);
                    flareY = transition(sunX, 0, completion);
                    //flareY =
                    break;
                case AFTERNOON:
                    sunColor = SUN;
                    //sunY = height - sunHeight;
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
                    //sunY = transition(height - sunHeight, height, completion);
                    fromY = height - sunHeight;
                    toY = height;
                    flareOpacity = 1. - completion;
                    flareY = transition(0, sunHeight / 2, completion);
                    //flareY = 0;
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
        private int scale;

        GrassImage() {
            this(Images.loadImageByName("grass.png"));
        }

        GrassImage(BufferedImage grassImage) {
            this.grassImage = Objects.requireNonNull(grassImage, "grassImage");
        }

        BufferedImage generate(short timeOfDay, int width, int height) {
            final int newGrassImageHeight = (int) ((double) height / 2.);
            final int newGrassImageWidth = (int) ((double) newGrassImageHeight / grassImage.getHeight() * grassImage.getWidth());
            this.scale = (int) ((double) newGrassImageHeight / grassImage.getHeight());

            final BufferedImage newGrassImage = new BufferedImage(newGrassImageWidth, newGrassImageHeight, BufferedImage.TYPE_INT_RGB);
            draw(newGrassImage, timeOfDay);
            return newGrassImage;
        }

        int getScale() {
            return scale;
        }

        void draw(BufferedImage grassImage, short timeOfDay) {
            final int width = grassImage.getWidth(), height = grassImage.getHeight();
            Graphics2D g = (Graphics2D) grassImage.getGraphics();
            g.drawImage(this.grassImage, 0, 0, width, height, null);

            switch (DaySegment.get(timeOfDay)) {
                case NIGHT:
                    g.setComposite(AlphaComposite.SrcOver.derive(NIGHT_COLOR_ALPHA));
                    g.setPaint(NIGHT_COLOR);
                    g.fillRect(0, 0, width, height);
                    break;
                case MORNING:
                    g.setComposite(AlphaComposite.SrcOver.derive(NIGHT_COLOR_ALPHA - (float) (DaySegment.getSegmentCompletion(timeOfDay) * NIGHT_COLOR_ALPHA)));
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
            double completion = DaySegment.getSegmentCompletion(timeOfDay);
            switch (DaySegment.get(timeOfDay)) {
                case NIGHT:
                    return NIGHT_COLOR;
                case MORNING:
                    return transitionColor(NIGHT_COLOR, DAY_COLOR, completion * 2.);
                case AFTERNOON:
                    return DAY_COLOR;
                case DUSK:
                    return transitionColor(DAY_COLOR, NIGHT_COLOR, completion);
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DayCycle dayCycle = (DayCycle) o;
            return timeOfDay == dayCycle.timeOfDay;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeOfDay);
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
                //s.segmentEnd = (short) (SEGMENT_LENGTH_IN_TICKS * (i + 1) - 1);
            }
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

    class TickTimer extends SwingWorker<Void, Void> {
        private final Runnable onUpdate;
        private long lastDayCycleUpdate;

        TickTimer(Runnable onUpdate) {
            this.onUpdate = Objects.requireNonNull(onUpdate, "onUpdate");
        }

        @Override
        protected Void doInBackground() {
            try {
                LOGGER.debug("First tick: {}", dayCycle);
                lastDayCycleUpdate = System.currentTimeMillis();
                while (!isCancelled()) {
                    updateDayCycle();
                    onUpdate.run();
                    Thread.sleep(ONE_TICK_IN_MS);
                }
            } catch (InterruptedException in) {
                LOGGER.debug("interrupted");
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.debug("cancelled");
            return null;
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
