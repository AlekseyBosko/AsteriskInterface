package Asterisk_interface;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import Asterisk_interface.MainFrame.infoNode;
import Asterisk_interface.MainFrame.officeNode;

@SuppressWarnings("serial")
public class FirstPageDisplay extends JPanel implements PageInterface{
	
	private Font Font;
	private Font arrFont;
	//панель со стрелочками для переключения между блоками 
	private JPanel MovePanel = new JPanel();;
	//счётчик для блоков
	private int panel=0;
	//блоки
	private JPanel [] FirstPagePanel;
	private int p=0;
	private Container container;
	private SecondPageDisplay second;
	@SuppressWarnings("static-access")
	public FirstPageDisplay(SecondPageDisplay panel,Container cont ,List<officeNode> _Office) { 
		Font = new Font("TimesRoman", Font.BOLD, 60);
		arrFont = new Font("TimesRoman", Font.BOLD, 20);
		second = panel;
		container = cont;
		addButton(_Office);
		addMoveButton();
	}
	//создание кнопок из xml файла
	public void addButton(List<officeNode> _Office){
		List<officeNode> offices = _Office;
		//количество блоков
		p=offices.size()/5;

		FirstPagePanel = new JPanel[p+1];
	    for(int i =0 ; i <= p;i++){
	    	FirstPagePanel[i] = new JPanel();
	    	FirstPagePanel[i].setLayout(new GridLayout(5, 1));
	 
	        }
	    int k=0;
		for (int i = 0,j=0; i < offices.size(); i++,j++) {
			//j - количество кнопок в блоке
			if(j==5){
				j=0;
			    k++;
			        }
			officeNode officeNode = offices.get(i);
		    final List<infoNode> info = officeNode.getInfo();
		    //создание кнопок
		    JButton button = new JButton(officeNode.getName()); 
		    button.setFont(Font);
		    button.setPreferredSize(new Dimension(900,150));
		    button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				second.addButton(info);
				second.addMoveButton();
				second.Layout(0);
	            } 
		    });
		    FirstPagePanel[k].add(button);
		}
	}
	//создание панели с кнопками для переключения между блоками 
	public void addMoveButton(){
     panel=0;		

     final JButton BackButton = new JButton("назад");
     BackButton.setPreferredSize(new Dimension(220,60));
     BackButton.setFont(arrFont);
     BackButton.setEnabled(false);
     final JButton ForwardButton = new JButton("вперёд");
     ForwardButton.setPreferredSize(new Dimension(220,60));
     ForwardButton.setFont(arrFont);
    if(p==0) ForwardButton.setEnabled(false);
     BackButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) {
				Layout(--panel);
				ForwardButton.setEnabled(true);
				if(panel>0) BackButton.setEnabled(true);
				else BackButton.setEnabled(false);
			} 
		});
   
     ForwardButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				
				Layout(++panel);
				BackButton.setEnabled(true);
				if(panel==p) ForwardButton.setEnabled(false);
				else BackButton.setEnabled(true);
			} 
		});	
     
     MovePanel.add(BackButton);
     MovePanel.add(ForwardButton);
	}
	//создание первой страницы приложения
	public void Layout(int k){
		
		int _panel=k;
		container.remove(second);
		this.removeAll();
		this.validate();
		Box contentBox = Box.createVerticalBox(); 
		contentBox.add(Box.createVerticalGlue());
		contentBox.add(FirstPagePanel[_panel]);
		contentBox.add(Box.createVerticalStrut(100));
		contentBox.add(MovePanel);
		contentBox.add(Box.createVerticalGlue());
		this.add(contentBox);
		
        container.add(this,BorderLayout.WEST);
		container.validate();
		container.repaint();
		
	}
}
