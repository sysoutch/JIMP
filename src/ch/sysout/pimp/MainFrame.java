package ch.sysout.pimp;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final String TITLE = "PIMP - Professional Image Manipulation Program";

	private AbstractButton btnSelect = new JToggleButton("Select");
	private AbstractButton btnRotate = new JToggleButton("Rotate");
	private AbstractButton btnCrop = new JToggleButton("Crop");
	private AbstractButton btnCopy = new JButton("Copy");
	private AbstractButton btnCut = new JButton("Cut");
	private AbstractButton btnPaste = new JButton("Paste");
	private AbstractButton btnDelete = new JButton("Delete");
	private AbstractButton[] navigationButtons = { btnSelect, btnRotate, btnCrop, btnCopy, btnCut, btnPaste, btnDelete };

	private ImageCanvas pnlImageCanvas = new ImageCanvas();

	private JMenu mnuFile;
	private JMenu mnuEdit;
	private JMenu mnuSelection;
	private JMenu mnuView;
	private JMenu mnuImage;
	private JMenu mnuColors;
	private JMenu mnuTools;
	private JMenu mnuWindow;
	private JMenu mnuHelp;
	private JMenuItem itmNew;
	private JMenuItem itmOpen;
	private JMenuItem itmUndo;
	private JMenuItem itmRedo;
	private JMenuItem itmHelp;

	public MainFrame() {
		super(TITLE);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		try {
			setIconImage(ImageIO.read(new FileInputStream("resources/images/logo.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		initComponents();
		createAndShowUI();
	}

	private void initComponents() {
		ButtonGroup grp = new ButtonGroup();
		for (AbstractButton btn : navigationButtons) {
			btn.addActionListener(this);
			grp.add(btn);
		}
	}

	private void createAndShowUI() {
		JMenuBar mnb = new JMenuBar();
		mnb.add(mnuFile = new JMenu("File"));
		mnb.add(mnuEdit = new JMenu("Edit"));
		mnb.add(mnuSelection = new JMenu("Selection"));
		mnb.add(mnuView = new JMenu("View"));
		mnb.add(mnuImage = new JMenu("Image"));
		mnb.add(mnuColors = new JMenu("Colors"));
		mnb.add(mnuTools = new JMenu("Tools"));
		mnb.add(mnuWindow = new JMenu("Window"));
		mnb.add(mnuHelp = new JMenu("Help"));
		mnuFile.add(itmNew = new JMenuItem("New"));
		mnuFile.add(itmOpen = new JMenuItem("Open..."));
		mnuEdit.add(itmUndo = new JMenuItem("Undo"));
		mnuEdit.add(itmRedo = new JMenuItem("Redo"));
		mnuHelp.add(itmHelp = new JMenuItem("Help"));

		itmNew.setAccelerator(KeyStroke.getKeyStroke("control N"));
		itmOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));
		itmUndo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
		itmRedo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
		itmHelp.setAccelerator(KeyStroke.getKeyStroke("F1"));

		setJMenuBar(mnb);
		JPanel pnlNavigationBar = new JPanel(new GridLayout(0, 1));
		for (AbstractButton btn : navigationButtons) {
			pnlNavigationBar.add(btn);
		}
		add(pnlNavigationBar, BorderLayout.WEST);
		JScrollPane sp = new JScrollPane(pnlImageCanvas);
		sp.getVerticalScrollBar().setUnitIncrement(16);
		sp.getHorizontalScrollBar().setUnitIncrement(16);
		add(sp);
		pack();
		setSize(1024, 720);
		setLocationRelativeTo(null);
		setVisible(true);
		try {
			pnlImageCanvas.setImage("resources/images/sample.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
				catch (UnsupportedLookAndFeelException e) {}
				catch (ClassNotFoundException e) {}
				catch (InstantiationException e) {}
				catch (IllegalAccessException e) {}
				new MainFrame();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == btnSelect) {
			Cursor cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			pnlImageCanvas.setCursor(cursor);
			pnlImageCanvas.setCurrentTool(ImageCanvas.SELECT_TOOL);
		} else {
			if (src == btnCopy) {
				pnlImageCanvas.copySelection();
			} else if (src == btnCut) {
				pnlImageCanvas.cutSelection();
			} else if (src == btnPaste) {
				pnlImageCanvas.pasteSelection();
			} else if (src == btnDelete) {
				pnlImageCanvas.deleteSelection();
			} else if (src == btnCrop) {
				pnlImageCanvas.cropSelection();
			} else {
				pnlImageCanvas.setCursor(null);
				if (src == btnCrop) {
					pnlImageCanvas.setCurrentTool(ImageCanvas.CROP_TOOL);
				}
				if (src == btnRotate) {
					pnlImageCanvas.setCurrentTool(ImageCanvas.ROTATE_TOOL);
				}
			}
		}
	}
}
