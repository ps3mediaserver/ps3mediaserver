/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.newgui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.gui.IFrame;
import net.pms.io.WindowsNamedPipe;
import net.pms.newgui.update.AutoUpdateDialog;
import net.pms.update.AutoUpdater;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

public class LooksFrame extends JFrame implements IFrame, Observer {
	
	private final AutoUpdater autoUpdater;
	private final PmsConfiguration configuration;
	public static final String START_SERVICE = "start.service"; //$NON-NLS-1$

	private static final long serialVersionUID = 8723727186288427690L;
	public TracesTab getTt() {
		return tt;
	}
	
	private FoldTab ft;

	public FoldTab getFt() {
		return ft;
	}

	private StatusTab st;
	private TracesTab tt;
	private TrTab2 tr;
	public TrTab2 getTr() {
		return tr;
	}

	private NetworkTab nt; 
	private AbstractButton reload ;
	
	 public AbstractButton getReload() {
		return reload;
	}
	 
	 private JLabel status;

	protected static final Dimension PREFERRED_SIZE = new Dimension(1000, 700);


	/**
     * Constructs a <code>DemoFrame</code>, configures the UI,
     * and builds the content.
     */
    public LooksFrame(AutoUpdater autoUpdater, PmsConfiguration configuration) {
    	this.autoUpdater = autoUpdater;
    	this.configuration = configuration;
    	assertThat(this.autoUpdater, notNullValue());
    	assertThat(this.configuration, notNullValue());
    	autoUpdater.addObserver(this);
    	update(autoUpdater, null);
    	Options.setDefaultIconSize(new Dimension(18, 18));

        Options.setUseNarrowButtons(true);

        // Global options
        Options.setTabIconsEnabled(true);
        UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY, null);

        // Swing Settings
        LookAndFeel selectedLaf = null;
        if (PMS.get().isWindows()) {
        	try {
				selectedLaf = (LookAndFeel) Class.forName("com.jgoodies.looks.windows.WindowsLookAndFeel").newInstance(); //$NON-NLS-1$
			} catch (Exception e) {
				selectedLaf = new PlasticLookAndFeel();
			}
        }
        else
        	selectedLaf = new PlasticLookAndFeel();
        
        if (selectedLaf instanceof PlasticLookAndFeel) {
            PlasticLookAndFeel.setPlasticTheme(PlasticLookAndFeel.createMyDefaultTheme());
            PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
            PlasticLookAndFeel.setHighContrastFocusColorsEnabled(false);
        } else if (selectedLaf.getClass() == MetalLookAndFeel.class) {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        }

        // Work around caching in MetalRadioButtonUI
        JRadioButton radio = new JRadioButton();
        radio.getUI().uninstallUI(radio);
        JCheckBox checkBox = new JCheckBox();
        checkBox.getUI().uninstallUI(checkBox);

        try {
            UIManager.setLookAndFeel(selectedLaf);
        } catch (Exception e) {
            System.out.println("Can't change L&F: " + e); //$NON-NLS-1$
        }
        
        setTitle("Test"); //$NON-NLS-1$
        setIconImage(readImageIcon("Play1Hot_32.png").getImage()); //$NON-NLS-1$
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setContentPane(buildContent());
        this.setTitle("Java PS3 Media Server v" + PMS.VERSION); //$NON-NLS-1$
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(PREFERRED_SIZE);
		 setSize(PREFERRED_SIZE);
	      //  setResizable(false);
	        Dimension paneSize = getSize();
	        Dimension screenSize = getToolkit().getScreenSize();
	        setLocation(
	            (screenSize.width  - paneSize.width)  / 2,
	            (screenSize.height - paneSize.height) / 2);
	        if (!PMS.getConfiguration().isMinimized() && System.getProperty(START_SERVICE) == null)
	        setVisible(true);
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/images/Play1Hot_256.png")); //$NON-NLS-1$

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem(Messages.getString("LooksFrame.5")); //$NON-NLS-1$
			MenuItem traceItem = new MenuItem(Messages.getString("LooksFrame.6")); //$NON-NLS-1$

			defaultItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
			});

			traceItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
			}
			});

			popup.add(traceItem);
			popup.add(defaultItem);

			final TrayIcon trayIcon = new TrayIcon(image, "Java PS3 Media Server v" + PMS.VERSION, popup); //$NON-NLS-1$

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
				setFocusable(true);
			}

			});
			try {
			tray.add(trayIcon);
			} catch (AWTException e) {
			e.printStackTrace();
			}
			}
    }
	
	protected static ImageIcon readImageIcon(String filename) {
        URL url = LooksFrame.class.getResource("/resources/images/" + filename); //$NON-NLS-1$
        return new ImageIcon(url);
    }

	public JComponent buildContent() {
		JPanel panel = new JPanel(new BorderLayout());
		JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        
        toolBar.add(new JPanel());
        AbstractButton save = createToolBarButton(Messages.getString("LooksFrame.9"), "filesave-48.png", Messages.getString("LooksFrame.9")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				PMS.get().save();
			}
        	
        });
        toolBar.add(save);
        toolBar.addSeparator();
        reload = createToolBarButton(Messages.getString("LooksFrame.12"), "reload_page-48.png", Messages.getString("LooksFrame.12")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        reload.setEnabled(false);
        reload.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					PMS.get().reset();
				} catch (IOException e1) {
				PMS.error(null, e1);
				}
			}
        	
        });
        toolBar.add(reload);
        toolBar.addSeparator();
        AbstractButton quit = createToolBarButton(Messages.getString("LooksFrame.5"), "exit-48.png", Messages.getString("LooksFrame.5")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        quit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				quit();
			}
        	
        });
        toolBar.add(quit);
        if (System.getProperty(START_SERVICE) != null)
        	quit.setEnabled(false);
        toolBar.add(new JPanel());
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(buildMain(), BorderLayout.CENTER);
        status = new JLabel(" "); //$NON-NLS-1$
        status.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 5, 0, 5)));
        panel.add(status, BorderLayout.SOUTH);
        return panel;
	}
	
	public JComponent buildMain() {
		 JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	        //tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		 st = new StatusTab();
		 tt = new TracesTab();
		 tr = new TrTab2(configuration);
		 nt = new NetworkTab(configuration);
		 ft = new FoldTab();
		 
		 tabbedPane.addTab(Messages.getString("LooksFrame.18"),/* readImageIcon("server-16.png"),*/ st.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.19"),/* readImageIcon("mail_new-16.png"),*/ tt.build()); //$NON-NLS-1$
		 
		 tabbedPane.addTab(Messages.getString("LooksFrame.20"),/* readImageIcon("advanced-16.png"),*/ nt.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.21"),/* readImageIcon("player_play-16.png"),*/tr.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.22"), /*readImageIcon("bookmark-16.png"),*/ ft.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.23"),/*  readImageIcon("mail_new-16.png"), */new AboutTab().build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.24"), /* readImageIcon("mail_new-16.png"), */new FAQTab().build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.25"), /*readImageIcon("documentinfo-16.png"),*/ new LinksTab().build()); //$NON-NLS-1$

	        tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		return tabbedPane;
	}
	
	 protected AbstractButton createToolBarButton(String text, String iconName, String toolTipText) {
	        JButton button = new JButton(text, readImageIcon(iconName));
	        button.setToolTipText(toolTipText);
	        button.setFocusable(false);
	        return button;
	    }
	
	 
	 public void quit() {
		 WindowsNamedPipe.loop = false;
		 try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		 System.exit(0);
	 }

	@Override
	public void append(String msg) {
		tt.getList().append(msg);
	}

	@Override
	public void setReadValue(long v, String msg) {
		st.setReadValue(v, msg);
	}

	@Override
	public void setStatusCode(int code, String msg, String icon) {
		st.getJl().setText(msg);
		try {
			st.getImagePanel().set(ImageIO.read(LooksFrame.class.getResourceAsStream("/resources/images/" + icon))); //$NON-NLS-1$
		} catch (IOException e) {
			PMS.error(null, e);
		}
	}

	@Override
	public void setValue(int v, String msg) {
		st.getJpb().setValue(v);
		st.getJpb().setString(msg);
	}

	@Override
	public void setReloadable(boolean b) {
		reload.setEnabled(b);
	}

	@Override
	public void addEngines() {
		tr.addEngines();
	}
	
	public void update(Observable o, Object arg) {
		if (o == autoUpdater) {
			try {
				AutoUpdateDialog.showIfNecessary(this, autoUpdater);
			} catch (NoClassDefFoundError ncdf) {
				PMS.minimal("Class not found: " + ncdf.getMessage()); //$NON-NLS-1$
			}
		}
	}
	
	public void setStatusLine(String line) {
		if (line == null)
			line = " "; //$NON-NLS-1$
		status.setText(line);
	}
}
