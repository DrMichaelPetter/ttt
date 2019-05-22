package ttt;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Experimental {
	static Preferences experimentalRoot = TTT.userPrefs.node("experimental");
	
	public static enum Code {
		SSHKEYSIZE("keysize","2048"),
		MICTHRESHOLD("micthreshold","0.00000001"),
		THUMBNAILATSTART("thumbnail at start","true");
		
		public String key;
		public String def;
		Code(String key,String def){
			this.key=key;
			this.def=def;
		}
		public String get() {
			return experimentalRoot.get(key, def);
		}
		public void set(String val) {
			experimentalRoot.put(key,val);
		}
	}
	public static void main(String[] args) throws BackingStoreException, IllegalArgumentException, IllegalAccessException{
		int size = (int)(Toolkit.getDefaultToolkit().getScreenResolution()*.25);
		System.setProperty("swing.plaf.metal.controlFont","DejaVu Sans Mono Book-"+size);
		for (String s : experimentalRoot.childrenNames())
			System.out.println(s);
		
		final JFrame frame = new JFrame("TTT Experimental Options");
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		final Map<String,JTextField> map = new HashMap<>();
		ActionListener action = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String newval = map.get(arg0.getActionCommand()).getText();
				experimentalRoot.put(arg0.getActionCommand(), newval);
				try {
					experimentalRoot.flush();
				} catch (BackingStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		for (Code c: Code.values()) {
			JPanel panel = new JPanel();
			JLabel label = new JLabel(c.name());
			panel.add(label);
			JTextField text = new JTextField(experimentalRoot.get(c.key,c.def.toString()));
			panel.add(text);
			map.put(c.key,text);
			JButton butt =new JButton("Save");
			butt.setActionCommand(c.key);

			butt.addActionListener(action);
			panel.add(butt);
			pane.add(panel);
		}
		
		JPanel globpanel = new JPanel(new BorderLayout());
		globpanel.add(pane,BorderLayout.CENTER);
		JButton close=new JButton("Close");
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
				
			}
		});
		globpanel.add(close,BorderLayout.SOUTH);
		frame.add(globpanel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
		frame.setVisible(true);
		
	}
}
