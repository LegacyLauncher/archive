package ru.turikhay.tlauncher.ui.center;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;

public class CenterPanelPipe_unused extends JComponent implements ResizeableComponent {
    private static final long serialVersionUID = -4783918557432088966L;
    public static final int PIPE_LONG = 30;
    public static final int PIPE_SHORT = 20;
    private final CenterPanel parent;
    private Component targetComponent;
    private CenterPanelPipe_unused.PipeOrientation pipeOrientation;
    private int pipeX;
    private int pipeY;

    protected CenterPanelPipe_unused(CenterPanel panel) {
        parent = panel;
        pipeOrientation = null;
        setOpaque(false);
    }

    public CenterPanel getParent() {
        return parent;
    }

    public CenterPanelPipe_unused.PipeOrientation getOrientation() {
        return pipeOrientation;
    }

    public boolean canDisplay() {
        return pipeOrientation != null && targetComponent != null;
    }

    public Component getTarget() {
        return targetComponent;
    }

    public void setTarget(Component comp) {
        targetComponent = comp;
        onResize();
    }

    public void onResize() {
        if (targetComponent != null) {
            MainPane pane = parent.tlauncher.getFrame().mp;
            Point parentPoint = pane.getLocationOf(parent);
            Point targetPoint = pane.getLocationOf(targetComponent);
            byte safeZone = 24;
            int targetX = targetPoint.x + targetComponent.getWidth() / 2;
            int targetY = targetPoint.y + targetComponent.getHeight() / 2;
            int parentMinX = parentPoint.x;
            int parentMaxX = parentMinX + parent.getWidth();
            int parentMinY = parentPoint.y;
            int parentMaxY = parentMinY + parent.getHeight();
            boolean fitsX = U.interval(parentMinX + safeZone, parentMaxX - safeZone, targetX);
            boolean fitsY = U.interval(parentMinY + safeZone, parentMaxY - safeZone, targetY);
            pipeOrientation = null;
            byte pipeWidth;
            byte pipeHeight;
            if (fitsX) {
                pipeWidth = 30;
                pipeHeight = 20;
                pipeX = targetX - pipeWidth / 2;
                if (targetY <= parentMinY) {
                    pipeY = parentMinY - pipeWidth;
                    pipeOrientation = CenterPanelPipe_unused.PipeOrientation.UP;
                } else if (targetY > parentMaxY) {
                    pipeY = parentMaxY + pipeHeight;
                    pipeOrientation = CenterPanelPipe_unused.PipeOrientation.DOWN;
                }
            } else {
                if (!fitsY) {
                    return;
                }

                pipeWidth = 20;
                pipeHeight = 30;
                pipeY = targetY - pipeHeight / 2;
                if (targetX <= parentMinX) {
                    pipeX = parentMinX - pipeWidth;
                    pipeOrientation = CenterPanelPipe_unused.PipeOrientation.LEFT;
                } else if (targetX > parentMaxY) {
                    pipeX = parentMaxX + pipeHeight;
                    pipeOrientation = CenterPanelPipe_unused.PipeOrientation.RIGHT;
                }
            }

            if (pipeOrientation != null) {
                setLocation(pipeX, pipeY);
                setSize(pipeWidth, pipeHeight);
            }
        }
    }

    public void paintComponent(Graphics g0) {
        if (targetComponent != null && pipeOrientation != null) {
            int maxX = getWidth();
            int maxY = getHeight();
            int midX = maxX / 2;
            int midY = maxY / 2;
            int[] triangleX = new int[3];
            int[] triangleY = new int[3];
            switch (pipeOrientation) {
                case LEFT:
                    triangleX[0] = maxX;
                    triangleY[0] = 0;
                    triangleX[1] = 0;
                    triangleY[1] = midY;
                    triangleX[2] = maxX;
                    triangleY[2] = maxY;
                    break;
                case UP:
                    triangleX[0] = 0;
                    triangleY[0] = maxY;
                    triangleX[1] = midX;
                    triangleY[1] = 0;
                    triangleX[2] = maxX;
                    triangleY[2] = maxY;
                    break;
                case RIGHT:
                    triangleX[0] = 0;
                    triangleY[0] = 0;
                    triangleX[1] = maxX;
                    triangleY[1] = midY;
                    triangleX[2] = 0;
                    triangleY[2] = maxY;
                    break;
                case DOWN:
                    triangleX[0] = 0;
                    triangleY[0] = 0;
                    triangleX[1] = midX;
                    triangleY[1] = maxY;
                    triangleX[2] = maxX;
                    triangleY[2] = 0;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown orientation: " + pipeOrientation);
            }

            CenterPanelTheme theme = parent.getTheme();
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(theme.getPanelBackground());
            g.fillPolygon(triangleX, triangleY, 3);
            g.setColor(theme.getBorder());
            g.drawPolygon(triangleX, triangleY, 3);
        }
    }

    protected void log(Object... o) {
        U.log("[CPipe]", o);
    }

    public enum PipeOrientation {
        LEFT,
        UP,
        RIGHT,
        DOWN
    }
}
