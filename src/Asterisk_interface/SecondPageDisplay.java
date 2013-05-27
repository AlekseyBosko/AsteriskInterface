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
import javax.swing.JLabel;
import javax.swing.JPanel;

import Asterisk_interface.MainFrame.infoNode;

@SuppressWarnings("serial")
public class SecondPageDisplay extends JPanel implements PageInterface{
	//панель со стрелочками для переключения между блоками 
	private JPanel MovePanel;
	//счётчик для блоков
	private int panel=0;
	//блоки
    private JPanel [] SecondPagePanel;
    private int p=0;
    
	private Font Font;
	private Font arrFont;
	private Container container;
    private FirstPageDisplay first;
	private SecondPageDisplay second;
	
	@SuppressWarnings("static-access")
	public SecondPageDisplay() { 

		Font = new Font("TimesRoman", Font.BOLD, 25);
		arrFont = new Font("TimesRoman", Font.BOLD, 20);
	}
	//создание кнопок из xml файла
	public void addButton(List<infoNode> information){
		final List<infoNode> info = information;
		//количество блоков
		p=info.size()/48;
		SecondPagePanel = new JPanel[p+1];
	    for(int i =0 ; i <= p;i++){
	    	SecondPagePanel[i] = new JPanel();
	    	SecondPagePanel[i].setMaximumSize(new Dimension(900, 500));
	    	SecondPagePanel[i].setLayout(new GridLayout(8, 2,10,10));
	        }
	    int k=0;
		for (int i = 0,j=0; i < info.size(); i++,j++) {
			if(j==16){
				//j - количество кнопок в блоке
				j=0;
			    k++;
			        }
			String job = info.get(i).getName(); 
			String name = info.get(i+1).getName(); 
            final String number = info.get(i+2).getName(); 
    	    //создание кнопок
            JButton button = new JButton(); 
           
            button.setLayout(new BorderLayout());
		    button.setMaximumSize(new Dimension(450,100));
		    button.setPreferredSize(new Dimension(450,100));

            JLabel label1 = new JLabel(job,JLabel.CENTER);
            JLabel label2 = new JLabel(name,JLabel.CENTER);
            label1.setFont(Font);
            label2.setFont(Font);
            
            button.add(BorderLayout.NORTH,label1);
            button.add(BorderLayout.CENTER,label2);

		    button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				if(container == MainFrame.container) Phone.Call(number);
				else RedirectPhone.Redirect(number);
		         }
		    });
		    i+=3;
		    SecondPagePanel[k].add(button);
		}
	}
	//создание панели с кнопками для переключения между блоками 
	public void addMoveButton(){
		 panel=0;
	     MovePanel = new JPanel();
	     final JButton BackButton = new JButton("назад");
	     BackButton.setPreferredSize(new Dimension(190,60));
	     BackButton.setFont(arrFont);
	     BackButton.setEnabled(false);
	     final JButton ForwardButton = new JButton("вперёд");
	     ForwardButton.setPreferredSize(new Dimension(190,60));
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
			
	     String Name = "Вернуться";
			JButton ReturnButton = new JButton(Name); 
			ReturnButton.setPreferredSize(new Dimension(220,80));
			ReturnButton.setFont(Font);
			ReturnButton.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent ev) { 
					  container.remove(second);
					  second.removeAll();
					  second.validate();
					  container.add(first,BorderLayout.WEST);
					  container.validate();
					  container.repaint();
				} 
			});
	     MovePanel.add(BackButton);
	     MovePanel.add(ReturnButton);
	     MovePanel.add(ForwardButton);
		}
	//создание второй страницы приложения
		public void Layout(int k){
			
			int _panel=k;
			
			this.removeAll();
			this.validate();
			container.remove(first);

			Box contentBox = Box.createVerticalBox(); 
			contentBox.add(Box.createVerticalGlue());
			contentBox.add(SecondPagePanel[_panel]);
			contentBox.add(Box.createVerticalStrut(40));
			contentBox.add(MovePanel);
			contentBox.add(Box.createVerticalGlue());
			this.add(contentBox);
			
	        container.add(this,BorderLayout.WEST);
			container.validate();
			container.repaint();
			
		}
		//установка необходимых параметров для переключения между окнами
		public void set(FirstPageDisplay panel, Container cont){
			first = panel;
			second = this;
			container = cont;
		}

}