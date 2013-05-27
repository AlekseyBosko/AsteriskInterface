package Asterisk_interface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;

//класс вспомагательных сообщений
@SuppressWarnings("serial")
public class MessageFrame extends JDialog {

	public MessageFrame(String message){
	    setSize(600,250);
	    Toolkit kit = Toolkit.getDefaultToolkit(); 
	    setLocation((kit.getScreenSize().width - getWidth())/2, (kit.getScreenSize().height - getHeight())/2);  
	    UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1.0);
	    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    	JLabel label = new JLabel(message);
		label.setMinimumSize(new Dimension(300, 70));
		label.setFont(new Font("TimesRoman", Font.PLAIN, 20));
		label.setAlignmentX(CENTER_ALIGNMENT);
	    
    	JButton okButton = new JButton("OK");
    	okButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				MessageFrame.this.setVisible(false);
				MessageFrame.this.dispose();
	            } 
		    });
    	okButton.setFont(new Font("TimesRoman", Font.PLAIN, 25));
    	okButton.setPreferredSize(new Dimension(170, 60));
    	setMaximumSize(new Dimension(170, 60));
    	okButton.setAlignmentX(CENTER_ALIGNMENT);
		Box contBox = Box.createVerticalBox();
		contBox.add(Box.createVerticalGlue());
		contBox.add(label);
		contBox.add(Box.createVerticalStrut(40));
		contBox.add(okButton);
		contBox.add(Box.createVerticalGlue());
		contBox.setBackground(new Color(0,100,0));
		add(contBox); 

		setModal(true);
		setVisible(true);
	}
}
