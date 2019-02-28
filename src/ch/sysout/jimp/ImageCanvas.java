package ch.sysout.jimp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class ImageCanvas extends JPanel {
	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	private int currentTool = -1;

	private Point dragStartPoint;
	private Point dragEndPoint;

	private float dash1[] = { 10.0f };
	private BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

	private Color defaultSelectColor = Color.LIGHT_GRAY;
	private Color defaultSelectColor2 = Color.GREEN;
	private Color backGroundColor = Color.WHITE;
	private Color currentSelectColor;

	private List<Rectangle> cuttedRects = new ArrayList<>();

	private BufferedImage cuttedImage;
	private BufferedImage pastedImage;

	protected int pressedX;
	protected int pressedY;

	public static final int SELECT_TOOL = 0;
	public static final int CROP_TOOL = 1;
	public static final int ROTATE_TOOL = 2;
	public static final int COPY_TOOL = 3;
	public static final int CUT_TOOL = 4;
	public static final int PASTE_TOOL = 5;


	public ImageCanvas() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("escape"), "unselectSelection");
		getActionMap().put("unselectAll", new UnselectSelectionAction());

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control A"), "selectAll");
		getActionMap().put("selectAll", new SelectAllAction());

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control X"), "cutSelection");
		getActionMap().put("cutSelection", new CutSelectionAction());

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control C"), "copySelection");
		getActionMap().put("copySelection", new CopySelectionAction());

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "deleteSelection");
		getActionMap().put("deleteSelection", new DeleteSelectionAction());

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control V"), "pasteSelection");
		getActionMap().put("pasteSelection", new PasteSelectionAction());

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (getCurrentTool() == SELECT_TOOL) {
					if (!hasSelection()) {
						dragStartPoint = e.getPoint();
						dragEndPoint = null;
						currentSelectColor = defaultSelectColor;
					} else {
						if (getSelectionRectangle().contains(e.getPoint())) {
							pressedX = e.getX();
							pressedY = e.getY();
						} else {
							dragStartPoint = e.getPoint();
							dragEndPoint = null;
							currentSelectColor = defaultSelectColor2;
						}
					}
					repaint();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (hasSelection()) {
					if (getSelectionRectangle().contains(e.getPoint())) {

					} else {
						dragStartPoint = null;
						dragEndPoint = null;
					}
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				currentSelectColor = defaultSelectColor2;
				repaint();
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if (getCurrentTool() == SELECT_TOOL) {
					if (currentSelectColor == defaultSelectColor2) {
						if (hasSelection()) {
							Rectangle selectedRectangle = getSelectionRectangle();
							if (selectedRectangle.contains(e.getPoint())) {
								int x = e.getX();
								int y = e.getY();
								dragStartPoint = new Point(x - (selectedRectangle.width / 2), y - (selectedRectangle.height / 2));
								dragEndPoint = new Point((x + selectedRectangle.width) - (selectedRectangle.width / 2), (y + selectedRectangle.height) - (selectedRectangle.height / 2));
							} else {
							}
						} else {
							currentSelectColor = defaultSelectColor;
							dragEndPoint = e.getPoint();
						}
					} else {
						currentSelectColor = defaultSelectColor;
						dragEndPoint = e.getPoint();
					}
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				if (getCurrentTool() == SELECT_TOOL) {
					if (hasSelection()) {
						Cursor cursor = null;
						if (getSelectionRectangle().contains(e.getPoint())) {
							cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
						} else {
							cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
						}
						setCursor(cursor);
					}
				}
			}
		});
	}

	public void unselectSelection() {
		System.err.println("unselect");
		dragStartPoint = null;
		dragEndPoint = null;
		repaint();
	}

	public void copySelection() {
		Rectangle selectedRect = getSelectionRectangle();
		cuttedImage = image.getSubimage(selectedRect.x, selectedRect.y, selectedRect.width, selectedRect.height);
	}

	public void cutSelection() {
		Rectangle selectedRect = getSelectionRectangle();
		cuttedImage = image.getSubimage(selectedRect.x, selectedRect.y, selectedRect.width, selectedRect.height);
		cuttedRects.add(selectedRect);
		dragStartPoint = null;
		dragEndPoint = null;
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		repaint();
	}

	public void deleteSelection() {
		Rectangle selectedRect = getSelectionRectangle();
		cuttedRects.add(selectedRect);
		dragStartPoint = null;
		dragEndPoint = null;
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		repaint();
	}

	public void pasteSelection() {
		pastedImage = cuttedImage;
		dragStartPoint = new Point(0, 0);
		dragEndPoint = new Point(cuttedImage.getWidth(), cuttedImage.getHeight());
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), this);
			if (cuttedRects.size() > 0) {
				g2d.setColor(backGroundColor);
				for (int i = 0; i < cuttedRects.size(); i++) {
					Rectangle r = cuttedRects.get(i);
					g2d.
					fillRect(r.x,
							r.y,
							r.width,
							r.height);
				}
			}

			if (pastedImage != null) {
				g2d.drawImage(pastedImage, 0, 0, pastedImage.getWidth(null), pastedImage.getHeight(null), this);
			}

			if (hasSelection()) {
				Rectangle rect = getSelectionRectangle();
				g2d.setColor(currentSelectColor);
				g2d.setStroke(dashed);
				g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
			}
			g2d.dispose();
		}
	}

	public Rectangle getSelectionRectangle() {
		if (!hasSelection()) {
			return null;
		}
		int startX = (dragStartPoint.x <= dragEndPoint.x) ? dragStartPoint.x : dragEndPoint.x;
		int startY = (dragStartPoint.y <= dragEndPoint.y) ? dragStartPoint.y : dragEndPoint.y;
		int width = (dragEndPoint.x >= dragStartPoint.x) ? dragEndPoint.x - dragStartPoint.x
				: dragStartPoint.x - dragEndPoint.x;
		int height = (dragEndPoint.y >= dragStartPoint.y) ? dragEndPoint.y - dragStartPoint.y
				: dragStartPoint.y - dragEndPoint.y;
		return new Rectangle(startX, startY, width, height);
	}

	public void setImage(String string) throws IOException {
		BufferedImage image = ImageIO.read(new FileInputStream(string));
		this.image = image;
		setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
		repaint();
	}

	public int getCurrentTool() {
		return currentTool;
	}

	public void setCurrentTool(int currentTool) {
		this.currentTool = currentTool;
		dragStartPoint = null;
		dragEndPoint = null;
		repaint();
	}

	public boolean hasSelection() {
		return dragStartPoint != null && dragEndPoint != null;
	}

	public class SelectAllAction implements Action {

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public Object getValue(String arg0) {
			return null;
		}

		@Override
		public boolean isEnabled() {
			setCurrentTool(SELECT_TOOL);
			dragStartPoint = new Point(0, 0);
			dragEndPoint = new Point(image.getWidth(), image.getHeight());
			currentSelectColor = defaultSelectColor2;
			repaint();
			return false;
		}

		@Override
		public void putValue(String arg0, Object arg1) {
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public void setEnabled(boolean arg0) {
		}
	}

	public class UnselectSelectionAction implements Action {

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public Object getValue(String arg0) {
			return null;
		}

		@Override
		public boolean isEnabled() {
			unselectSelection();
			return false;
		}

		@Override
		public void putValue(String arg0, Object arg1) {
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public void setEnabled(boolean arg0) {
		}
	}

	public class CutSelectionAction implements Action {

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public Object getValue(String arg0) {
			return null;
		}

		@Override
		public boolean isEnabled() {
			cutSelection();
			return false;
		}

		@Override
		public void putValue(String arg0, Object arg1) {
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public void setEnabled(boolean arg0) {
		}
	}

	public class CopySelectionAction implements Action {

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public Object getValue(String arg0) {
			return null;
		}

		@Override
		public boolean isEnabled() {
			copySelection();
			return false;
		}

		@Override
		public void putValue(String arg0, Object arg1) {
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public void setEnabled(boolean arg0) {
		}
	}

	public class DeleteSelectionAction implements Action {

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public Object getValue(String arg0) {
			return null;
		}

		@Override
		public boolean isEnabled() {
			deleteSelection();
			return false;
		}

		@Override
		public void putValue(String arg0, Object arg1) {
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public void setEnabled(boolean arg0) {
		}
	}

	public class PasteSelectionAction implements Action {

		@Override
		public void actionPerformed(ActionEvent e) {
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public Object getValue(String arg0) {
			return null;
		}

		@Override
		public boolean isEnabled() {
			pasteSelection();
			return false;
		}

		@Override
		public void putValue(String arg0, Object arg1) {
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener arg0) {
		}

		@Override
		public void setEnabled(boolean arg0) {
		}
	}
}
