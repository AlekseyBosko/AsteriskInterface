package Asterisk_interface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;

@SuppressWarnings("serial")
public class Phone extends JPanel {
	
	protected static Font NumButtonsFont;
	protected static Font ButtonsFont;
	protected static Font DisplayFont;
    //поле для ввода номера
	protected static JTextField DisplayField;
	//максимальное количество цифр в поле ввода
	protected int MaxChars;
	//номер телефона с громой связью
	public static String MainExtension;
	//номера телефонов без громкой связи
	public static ArrayList<String> usualExtensions;
	//все возможные номера принадлежащие телефону
	public static ArrayList<String> AllExtensions;
	//IP адрес asterisk
	public static String AsteriskIp;
	//имя пользователя AMI для записи
	public static String WriteUser;
	//пароль пользователя AMI для записи
	public static String WriteUserPassword;
	//имя пользователя AMI для чтения(иногда используется для записи)
	public static String ReadUser;
	//пароль пользователя AMI для чтения(иногда используется для записи)
	public static String ReadUserPassword;
	//контекст для телефона с громкой связью
	public static String Context;
	//телефон для повторного звонка
	private static String Redial;
	//путь к папке с фотографиями
	public static String PhotoFolder;
	//имя defaut фотографии
	public static String DefaultPhoto;
	
	private static JButton CallButton;
	private JButton RedirectButton;


	//путь для картинки красной стелки в "истории звонков"
	private static URL urlRed;
	//путь для картинки зелёной стелки в "истории звонков"
	private static URL urlGreen;
	//путь для картинки неотвеченого звонка в "истории звонков"
	private static URL urlMissCall;
	//панель "истории звонков"
	private static Box listBox;
	private static JPanel butPanel;
	//хэш массив (номера-имя)
	public static Hashtable<String, List<String>> numbers = new Hashtable<String, List<String>>();
	
	public Phone() { 
		NumButtonsFont = new Font("Serif", Font.BOLD, 40);
		ButtonsFont = new Font("TimesRoman", Font.PLAIN, 26);
		DisplayFont = new Font("TimesRoman", Font.PLAIN, 80);
		MaxChars = 11;
		setLayout(new BorderLayout());

		urlRed = ClassLoader.getSystemResource("red.png");
	    urlGreen = ClassLoader.getSystemResource("green.png");
	    urlMissCall = ClassLoader.getSystemResource("miss.png");
	}
	//добавление кнопок(0,1,2,3,4,5,6,7,8,9,*,#)
	public JPanel addNumberButtons(JTextField DispField){
		final JTextField Field = DispField;
		JPanel ButtonPanel = new JPanel();
	    ButtonPanel.setLayout(new GridLayout(4, 3));
	    ButtonPanel.setPreferredSize(new Dimension(500,320));
	    ButtonPanel.setMaximumSize(new Dimension(500,500));
	    
	    JButton [] NumButtons = new JButton[10];
	    JButton StarButtons = new JButton("*");
	    StarButtons.setFont(NumButtonsFont);
	    StarButtons.setPreferredSize(new Dimension(100,75));
	    StarButtons.addActionListener(new ActionListener() { 
    	public void actionPerformed(ActionEvent ev) { 
    		if(Field.getText().length()<MaxChars) 
    			Field.setText(Field.getText()+"*");
    		if(!MainFrame.RedirectPanel.isVisible()) CallButton.setEnabled(true);    		
            } 
	    });
	    JButton latticeButtons = new JButton("#");
	    latticeButtons.setFont(NumButtonsFont);
	    latticeButtons.setPreferredSize(new Dimension(100,75));
	    latticeButtons.addActionListener(new ActionListener() { 
    	public void actionPerformed(ActionEvent ev) { 
    		if(Field.getText().length()<MaxChars) 
    			Field.setText(Field.getText()+"#");
    		if(!MainFrame.RedirectPanel.isVisible()) CallButton.setEnabled(true);     		  	 	
            } 
	    });

	    
	    for (int i=0; i<10; i++){
	    	final String name=""+i;
            NumButtons[i]=new JButton(""+i); 
            NumButtons[i].setFont(NumButtonsFont);
	    	NumButtons[i].setPreferredSize(new Dimension(100,75));
	    	NumButtons[i].addActionListener(new ActionListener() { 
	    	public void actionPerformed(ActionEvent ev) { 
	    		if(Field.getText().length()<MaxChars)
	    			Field.setText(Field.getText()+name);
	    		if(!MainFrame.RedirectPanel.isVisible()) CallButton.setEnabled(true);      	  
	            } 
		    });
        }
	    
	    for (int i=1;i<=9;i++){
	    	ButtonPanel.add(NumButtons[i]);
        }
	    ButtonPanel.add(StarButtons);
	    ButtonPanel.add(NumButtons[0]);
	    ButtonPanel.add(latticeButtons);
       
	    return ButtonPanel;
		
	}
	//добавление кнопок телефона
	public JPanel addFuncButtons(){
		//добавление кнопки Повтор
		final JButton RedialButton = new JButton("Повтор");
		RedialButton.setPreferredSize(new Dimension(180,90));
		RedialButton.setFont(ButtonsFont);
		RedialButton.setEnabled(false);
		RedialButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				Call(Redial); 
	            } 
		    });
		//добавление кнопки Позвонить
	    URL urlCall = ClassLoader.getSystemResource("phone-green-icon.png");
		CallButton = new JButton(new ImageIcon(urlCall));
	    // CallButton = new JButton("call");
		CallButton.setPreferredSize(new Dimension(180,90));
		if(DisplayField.getText().isEmpty()) CallButton.setEnabled(false);
		else CallButton.setEnabled(true);
		CallButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				Call(DisplayField.getText());
				RedialButton.setEnabled(true);	 
	            } 
		    });
		//добавление кнопки Очистить дисплей
	    URL urlRemove = ClassLoader.getSystemResource("Banned-icon.png");
		JButton RemoveButton = new JButton(new ImageIcon(urlRemove));
		//JButton RemoveButton = new JButton("remove");
		RemoveButton.setPreferredSize(new Dimension(180,90));
		RemoveButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				DisplayField.setText("");	
				CallButton.setEnabled(false);    		 
	            } 
		    });		
		//добавление кнопки Перевести звонок
		RedirectButton = new JButton();
		RedirectButton.setPreferredSize(new Dimension(180,90));
		RedirectButton.setLayout(new BorderLayout());
        JLabel label1 = new JLabel("Перевести",JLabel.CENTER);
        JLabel label2 = new JLabel("звонок",JLabel.CENTER);
        label1.setFont(ButtonsFont);
        label2.setFont(ButtonsFont);         
        RedirectButton.add(BorderLayout.NORTH,label1);
        RedirectButton.add(BorderLayout.CENTER,label2);    
		RedirectButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) {
				CallFrame.MakeFramesVisible(false);
			    MainFrame.RedirectFirstPage.Layout(0);
				MainFrame.RedirectPanel.setVisible(true);

	            } 
		    });
		//добавление кнопки история звонков
		JButton ListButton = new JButton();
		ListButton.setPreferredSize(new Dimension(180,90));
		ListButton.setMaximumSize(new Dimension(180,90));
		ListButton.setLayout(new BorderLayout());
        label1 = new JLabel("История",JLabel.CENTER);
        label2 = new JLabel("звонков",JLabel.CENTER);
        label1.setFont(ButtonsFont);
        label2.setFont(ButtonsFont);         
        ListButton.add(BorderLayout.NORTH,label1);
        ListButton.add(BorderLayout.CENTER,label2);    
        ListButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 			
				listBox.setVisible(!listBox.isVisible());		
			}
		    });
        //прикрепление функциональных кнопок к одной панели
	    JPanel panel = new JPanel();
	    panel.setMinimumSize(new Dimension(650,200));
	    panel.add(RedialButton);
	    panel.add(RedirectButton);
	    panel.add(ListButton);
	    panel.add(CallButton);
	    panel.add(RemoveButton);

	    return panel;
	}
	
	public static JButton GetCallButton()
	{
		return CallButton;
	}
	//функция звонок
	@SuppressWarnings("static-access")
	public static void Call(final String num) {

		CallFrame CallFrame = new CallFrame();

		CallFrame.HoldIfNotActive();
		Redial = num;

		PrintWriter writer = MainFrame.TelnetWriter();

		writer.print("Action: Originate\r\n");
		writer.print("Channel: SIP/" + MainExtension + "\r\n");
		writer.print("Exten: " + num + "\r\n");
		writer.print("Context: " + Context + "\r\n");
		writer.print("Priority: 1\r\n");
		writer.print("CallerId: phone<" + num + ">\r\n");
		writer.print("Async: yes\r\n\r\n");
		writer.flush();
		
		CallFrame.removeFromList(CallFrame);
		CallFrame.dispose();
		//для последовательного вывод на экран CallFrames
		MainFrame.xLocationForCallFrame-=450;
 		//остановка главного потока на 1300 миллисекунд, чтобы поток Asterisk смог обработать команды
		   CallButton.setEnabled(false);
		   try{

		          Thread.sleep(600);		
		      }catch(InterruptedException e){}
         new CallButtonTrue().start();
         CallFrame.MakeFramesNotEnable(false);
 		new CallFramesTrue().start();

	}
	
	public JButton Sleep(){
	JButton button = new JButton("Затемнить");
    button.setVisible(true);
    button.setLocation(500,500);
    button.setPreferredSize(new Dimension(290,130));
	button.setFont(new Font("TimesRoman", Font.PLAIN, 40));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {        	
      		final JFrame f1 = new JFrame();
            UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1.0);
      		Color c = new Color(0,0,0);
      		Container con = f1.getContentPane();
      		con.setBackground(c );  		
      		f1.addMouseMotionListener(new MouseMotionAdapter() {   				 
      				    public void mouseDragged(MouseEvent evt) {      				 
      				    	 f1.dispose();
      				    }			   			 
      				});
      		f1.addMouseListener(new MouseListener() {
					public void mousePressed(MouseEvent e) {
						 f1.dispose();						
					}
					public void mouseReleased(MouseEvent e) {
						 f1.dispose();		
					}
					public void mouseClicked(MouseEvent e) {
					}
					public void mouseExited(MouseEvent e) {						
					}
					public void mouseEntered(MouseEvent e) {			
					}
      		});
      		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      		Toolkit kit = Toolkit.getDefaultToolkit();      
      	    f1.setLocation((kit.getScreenSize().width - 300)/2, (kit.getScreenSize().height - 300)/2);
      		f1.setUndecorated(true);
      		f1.setVisible(true);
      		f1.setExtendedState(JFrame.MAXIMIZED_BOTH);

      }
    });
    return button; 
	}

	public static void TypeOfCallForList(String hangupInitNumber,
			String hangupNumber, Boolean missCall) {

		for (int i = 0; i < AllExtensions.size(); i++) {
			// исходящий звонок
			if (hangupNumber.equals(AllExtensions.get(i)) && missCall) {
				NumForList(hangupInitNumber, "MissCall");
			} else if (hangupInitNumber.equals(AllExtensions.get(i))) {
				NumForList(hangupNumber, "Output");
			}
			// входящий звонок
			else if (hangupNumber.equals(AllExtensions.get(i))) {
				NumForList(hangupInitNumber, "Input");

			}
		}
	}
	//удаление callframe исходящих и входящих выховов и добавление новой записи в "список звонков"
	@SuppressWarnings("deprecation")
	public synchronized static void NumForList(final String outerNum,
			final String flagHangup) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (butPanel.countComponents() == 10)
					butPanel.remove(9);
				JButton button = new JButton();
				button.setPreferredSize(new Dimension(300, 70));
				button.setMaximumSize(new Dimension(300, 70));
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						Call(outerNum);
					}
				});
				button.setLayout(new BorderLayout());
				JLabel label1 = null;
				if (numbers.get(outerNum) != null) {
					label1 = new JLabel(numbers.get(outerNum).get(0), JLabel.CENTER);
					label1.setFont(new Font("TimesRoman", Font.BOLD, 18));
				}
				JLabel label2 = null;

				if (flagHangup.equals("Output"))
					label2 = new JLabel(outerNum, new ImageIcon(urlRed),JLabel.CENTER);
				else if (flagHangup.equals("Input"))
					label2 = new JLabel(outerNum, new ImageIcon(urlGreen),JLabel.CENTER);
				else if (flagHangup.equals("MissCall"))
					label2 = new JLabel(outerNum, new ImageIcon(urlMissCall),JLabel.CENTER);
				// if(flagHangup.equals("Output")) label2 = new JLabel(outerNum + "исходящий",JLabel.CENTER);
				// else if(flagHangup.equals("Input")) label2 = new JLabel(outerNum+ "входящий",JLabel.CENTER);
				// else if(flagHangup.equals("MissCall")) label2 = new JLabel(outerNum+ "missCall",JLabel.CENTER);
				label2.setFont(new Font("Serif", Font.BOLD, 22));
				if (label1 != null) {
					button.add(BorderLayout.NORTH, label1);
					button.add(BorderLayout.CENTER, label2);
				} else
					button.add(BorderLayout.CENTER, label2);
				butPanel.add(button, 0);
				butPanel.validate();
				butPanel.repaint();
			}
		});
	}
//создание панели с кнопками истории звонков
	public void HistoryList() {
		listBox = Box.createVerticalBox();
		listBox.setVisible(false);
		JLabel labelForList = new JLabel("История звонков");
		labelForList.setFont(new Font("TimesRoman", Font.PLAIN, 35));
		butPanel = new JPanel();
		butPanel.setPreferredSize(new Dimension(310, 760));
		butPanel.setMaximumSize(new Dimension(310, 760));
		butPanel.setMinimumSize(new Dimension(310, 760));
		butPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		listBox.add(Box.createVerticalGlue());
		listBox.add(Box.createVerticalStrut(10));
		labelForList.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		listBox.add(labelForList);
		listBox.add(butPanel);
		listBox.add(Box.createVerticalGlue());

	}
//создание телефона и отображение его на экране
	public void CreatePhone(){    
	 DisplayField = new JTextField();
	 DisplayField.setPreferredSize(new Dimension(500,150));
	 DisplayField.setMaximumSize(new Dimension(500,150));
	 DisplayField.setEditable(false);
	 DisplayField.setHorizontalAlignment(SwingConstants.CENTER);
	 DisplayField.setFont(DisplayFont);
	 
	 DisplayField.setBorder(BorderFactory.createLineBorder(Color.BLACK)); 
	 
	 JPanel ButtonPanel = addNumberButtons(DisplayField);
	 JPanel CallBox = addFuncButtons();
	 Box contentBox = Box.createVerticalBox(); 
	 contentBox.setPreferredSize(new Dimension(600,620));
	 contentBox.add(Box.createVerticalGlue());
	 contentBox.add(Box.createVerticalStrut(40));
	 contentBox.add(DisplayField);
	 contentBox.add(Box.createVerticalStrut(10));
	 contentBox.add(ButtonPanel);
	 contentBox.add(Box.createVerticalStrut(10));
	 contentBox.add(CallBox);
	 contentBox.add(Box.createVerticalGlue());
	 
	 HistoryList();
	 JButton sleep = Sleep();
	 JPanel slPanel = new JPanel();
	 slPanel.setLayout(new BorderLayout());
	 slPanel.add(sleep,BorderLayout.EAST);
     Box boxForList = Box.createVerticalBox(); 
     boxForList.setPreferredSize(new Dimension(140,620));
     boxForList.add(Box.createVerticalGlue());
     contentBox.add(Box.createVerticalStrut(10));
     boxForList.add(listBox);
     boxForList.add(Box.createVerticalGlue());
     
     this.add(slPanel,BorderLayout.NORTH);
     this.add(contentBox,BorderLayout.WEST);
     this.add(boxForList,BorderLayout.CENTER);
	 MainFrame.container.add(this,BorderLayout.CENTER);
	}
	

}
class CallButtonTrue extends Thread
{ 
   
	@Override
    public void run()
    {
		try {
			sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(Phone.DisplayField.getText().isEmpty()) Phone.GetCallButton().setEnabled(false);
		else Phone.GetCallButton().setEnabled(true);
    }
	}
