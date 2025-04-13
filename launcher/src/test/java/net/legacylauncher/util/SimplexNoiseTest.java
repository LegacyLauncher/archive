package net.legacylauncher.util;

import net.legacylauncher.util.async.AsyncThread;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class SimplexNoiseTest {

    @Test
    @Disabled
    void test() throws InterruptedException {
        var window = new JFrame();
        var latch = new CountDownLatch(1);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                latch.countDown();
            }
        });
        var graph = new SimplexGraph();
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.add(graph, BorderLayout.CENTER);
        window.setSize(500, 500);
        window.setVisible(true);
        graph.scheduleRepaint();
        latch.await();
    }

    private static class SimplexGraph extends JComponent {

        private BufferedImage image;

        private long t;

        @Override
        public void paint(Graphics g0) {
            final int size = 32;
            final double xscale = 0.0005, yscale=0.005, movescale = 0.001;

            int w = getWidth(), hw = w / 2, h = getHeight();

            if (image == null || image.getWidth() != w || image.getHeight() != h) {
                image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
            }

            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setBackground(Color.white);
            g.setPaint(Color.black);
            g.clearRect(0, 0, image.getWidth(), image.getHeight());
            for (int x = 0; x < image.getWidth(); x += size) {
                for (int y = 0; y < image.getHeight(); y += size) {
                    float v = (float) SimplexNoise.noise(
                            x * xscale * t * movescale,
                            y * yscale,
                            t * yscale
                    );
                    if (v > 0.4) {
                        g.setComposite(AlphaComposite.SrcOver.derive(v * v));
                        g.fillRect(x, y, size, size);
                    }
                }
            }
            g0.drawImage(image, 0, 0, this);
        }

        void scheduleRepaint() {
            if (this.isDisplayable()) {
                t++;
                SwingUtil.later(() -> repaint());
                AsyncThread.DELAYER.schedule(this::scheduleRepaint, 250, TimeUnit.MILLISECONDS);
            }
        }
    }
}
