package ru.turikhay.tlauncher.ui.center;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.util.U;

public class CenterPanelPipe_unused extends JComponent implements ResizeableComponent {
	private static final long serialVersionUID = -4783918557432088966L;
	public static final int PIPE_LONG = 30, PIPE_SHORT = 20;
	
	private final CenterPanel parent;
	
	// Used in onResize();
	private Component targetComponent;
	
	// Used in paintComponent();
	private PipeOrientation pipeOrientation;
	private int pipeX, pipeY;
	
	protected CenterPanelPipe_unused(CenterPanel panel) {
		this.parent = panel;
		this.pipeOrientation = null;
		
		setOpaque(false);
	}
	
	public CenterPanel getParent() {
		return parent;
	}
	
	public PipeOrientation getOrientation() {
		return pipeOrientation;
	}
	
	public boolean canDisplay() {
		return !(pipeOrientation == null || targetComponent == null);
	}
	
	public Component getTarget() {
		return targetComponent;
	}
	
	public void setTarget(Component comp) {
		this.targetComponent = comp;
		onResize();
	}
	
	@Override
	public void onResize() {
		if(targetComponent == null) return;
		
		MainPane pane = parent.tlauncher.getFrame().mp;
		
		Point
			parentPoint = pane.getLocationOf(parent),
			targetPoint = pane.getLocationOf(targetComponent);
		
		int safeZone = CenterPanel.ARC_SIZE;
		
		// Calculate center coords of target
		int
			targetX = targetPoint.x + targetComponent.getWidth() / 2,
			targetY = targetPoint.y + targetComponent.getHeight() / 2;
		
		// Calculate X coords of parent
		int
			parentMinX = parentPoint.x,
			parentMaxX = parentMinX + parent.getWidth();
		
		// Calculate Y coords of parent
		int
			parentMinY = parentPoint.y,
			parentMaxY = parentMinY + parent.getHeight();
		
		boolean
			fitsX = U.interval(parentMinX + safeZone, parentMaxX - safeZone, targetX),
			fitsY = U.interval(parentMinY + safeZone, parentMaxY - safeZone, targetY);
		
		int pipeWidth, pipeHeight;
		pipeOrientation = null;
		
		if(fitsX) {
			// UP or DOWN
			
			pipeWidth = PIPE_LONG;
			pipeHeight = PIPE_SHORT;
			
			pipeX = targetX - pipeWidth / 2;
			
			if(targetY <= parentMinY) {
				// UP
				pipeY = parentMinY - pipeWidth;
				pipeOrientation = PipeOrientation.UP;
				//
			} else if(targetY > parentMaxY) {
				// DOWN
				pipeY = parentMaxY + pipeHeight;
				pipeOrientation = PipeOrientation.DOWN;
				//
			} // else - nothing.
		}
		else if(fitsY) {
			// LEFT or RIGHT
			
			pipeWidth = PIPE_SHORT;
			pipeHeight = PIPE_LONG;
			
			pipeY = targetY - pipeHeight / 2;
			
			if(targetX <= parentMinX) {
				// LEFT
				pipeX = parentMinX - pipeWidth;
				pipeOrientation = PipeOrientation.LEFT;
			} else if(targetX > parentMaxY) {
				// RIGHT
				pipeX = parentMaxX + pipeHeight;
				pipeOrientation = PipeOrientation.RIGHT;
			} // else - nothing
		}
		else
			return;
		
		if(pipeOrientation == null) return;
		
		setLocation(pipeX, pipeY);
		setSize(pipeWidth, pipeHeight);
	}
	
	@Override
	public void paintComponent(Graphics g0) {
		if(targetComponent == null || pipeOrientation == null) return;
		
		int
			maxX = getWidth(),
			maxY = getHeight(),
			midX = maxX / 2,
			midY = maxY / 2;
		
		int[]
			triangleX = new int[3],
			triangleY = new int[3];
		
		switch(pipeOrientation){
		case DOWN:
			triangleX[0] = 0;
			triangleY[0] = 0;
			
			triangleX[1] = midX;
			triangleY[1] = maxY;
			
			triangleX[2] = maxX;
			triangleY[2] = 0;
			break;
		case LEFT:
			triangleX[0] = maxX;
			triangleY[0] = 0;
			
			triangleX[1] = 0;
			triangleY[1] = midY;
			
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
		case UP:
			triangleX[0] = 0;
			triangleY[0] = maxY;
			
			triangleX[1] = midX;
			triangleY[1] = 0;
			
			triangleX[2] = maxX;
			triangleY[2] = maxY;
			break;
		default:
			throw new IllegalArgumentException("Unknown orientation: "+ pipeOrientation);
		}
		
		CenterPanelTheme theme = parent.getTheme();
		
		Graphics2D g = (Graphics2D) g0;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(theme.getPanelBackground());
		g.fillPolygon(triangleX, triangleY, 3);
		
		g.setColor(theme.getBorder());
		g.drawPolygon(triangleX, triangleY, 3);
	}
	
	protected void log(Object...o){ U.log("[CPipe]", o); } 
	
	public enum PipeOrientation {
		LEFT, UP, RIGHT, DOWN;
	}
}
