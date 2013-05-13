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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;

@SuppressWarnings("serial")
public class Phone extends JPanel {
	
	protected static Font listNamesFont;
	protected static Font NumButtonsFont;
	protected static Font ButtonsFont;
	protected static Font DisplayFont;
    //поле для ввода номера
	protected static JTextField DisplayField;
	//максимальное количество цифр в поле
	protected int MaxChars;
	//номер телефона с громой связью
	public static String Extension;
	//номера телефонов без громкой связи
	public static ArrayList<String> usualExtensions;
	//все возможные номера принадлежащие телефону
	public static ArrayList<String> AllExtensions;
	//IP адрес asterisk
	public static String AsteriskIp;
	//имя пользователя для AMI
	public static String User;
	//пароль пользователя для AMI
	public static String Password;
	//имя пользователя для AMI для истории звонков
	public static String ListUser;
	//пароль пользователя для AMI для истории звонков
	public static String ListPassword;
	//контекст для телефона с громкой связью
	public static String Context;
	//телефон для повторного звонка
	private static String Redial;
	
	private JButton CallButton;
	private JButton RedirectButton;
	//путь для красной стелки в "истории звонков"
	private static URL urlRed;
	//путь для зелёной стелки в "истории звонков"
	private static URL urlGreen;
	//панель "истории звонков"
	private static JPanel listPanel;
	//хэш массив (номера-имя)
	public static Hashtable<String, List<String>> numbers = new Hashtable<String, List<String>>();
	
	public Phone() { 
		listNamesFont = new Font("Serif", Font.BOLD, 20);
		NumButtonsFont = new Font("Serif", Font.BOLD, 40);
		ButtonsFont = new Font("Serif", Font.PLAIN, 26);
		DisplayFont = new Font("Serif", Font.PLAIN, 80);
		MaxChars = 11;
		setLayout(new BorderLayout());
		listPanel= new JPanel();
		listPanel.setPreferredSize(new Dimension(300,760));
		listPanel.setMaximumSize(new Dimension(300,760));
		listPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		listPanel.setVisible(false);
		
		urlRed = ClassLoader.getSystemResource("red.png");
	    urlGreen = ClassLoader.getSystemResource("green.png");
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
		//CallButton = new JButton(new ImageIcon(urlCall));
	    CallButton = new JButton("call");
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
		//JButton RemoveButton = new JButton(new ImageIcon(urlRemove));
		JButton RemoveButton = new JButton("remove");
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
				listPanel.setVisible(!listPanel.isVisible());		
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
	//функция звонок
	public static void Call(final String num){
	   
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {  
		CallFrame CallFrame= new CallFrame();
		CallFrame.HoldIfNotActive();
		CallFrame.addOutputCallPanel(Extension,num);
     	CallFrame.setVisible(true);
    	}});
		
		Redial = num;
		try {
			Socket telnet = new Socket(AsteriskIp, 5038);
            telnet.setKeepAlive(true);
            PrintWriter writer = new PrintWriter(telnet.getOutputStream());
                 writer.print("Action: login\r\n");
                 writer.print("UserName: "+User+"\r\n");
                 writer.print("Secret: "+Password+"\r\n\r\n");
                 writer.print("Action: Originate\r\n");
                 writer.print("Channel: SIP/"+Extension+"\r\n" );
                 writer.print("Exten: "+num+"\r\n");
                 writer.print("Context: "+Context+"\r\n");
                 writer.print("Priority: 1\r\n");
                 writer.print("CallerId: phone<"+num+">\r\n");
                 writer.print("Async: yes\r\n\r\n");
                 writer.print("Action: LOGOFF\r\n\r\n");
                 writer.flush();
                 telnet.close();
            }          
    catch (SocketException e1) {
       e1.printStackTrace();    
   } catch (IOException e1) {
       e1.printStackTrace();
   }


    }
	
	
	public JButton Sleep(){
	JButton button = new JButton("Затемнить");
    button.setVisible(true);
    button.setLocation(500,500);
    button.setPreferredSize(new Dimension(290,130));
	button.setFont(new Font("Serif", Font.PLAIN, 40));
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
      		//f1.setSize(300, 300);
      		Toolkit kit = Toolkit.getDefaultToolkit();      
      	    f1.setLocation((kit.getScreenSize().width - 300)/2, (kit.getScreenSize().height - 300)/2);
      		f1.setUndecorated(true);
      		f1.setVisible(true);
      		f1.setExtendedState(JFrame.MAXIMIZED_BOTH);

      }
    });
    return button; 
	}
	
	//удаление callframe исходящих и входящих выховов и добавление новой записи в "список звонков"
@SuppressWarnings("deprecation")
public static void NumForList(final String outputChan,String inputChan,final int flagHangup)
{ //flagHangup - указывает входящий или исходящий звонок(0-исходящий/1-входящий)
	String initChannel=null;  //канал, который инициировал звонок
	String channel=null;   //канал, который принимает звонок
	Iterator<Entry<CallFrame, List<String>>> bridgeIterator = null;
	//установка переменных канала в зависимости от типа звонка(входящий/исходящий)
	if(flagHangup==0) {
		initChannel=inputChan;
		channel=outputChan;
	}
	else {
		initChannel=outputChan;
		channel=inputChan;
	}
	//поиск и удаление существующих callframe исходящих и входящих выховов
	bridgeIterator = CallFrame.bridgeLines.entrySet().iterator();
	while (bridgeIterator.hasNext()) {
		Map.Entry entry = bridgeIterator.next();
		List<String> bridgeList = (List<String>) entry.getValue();
		if(bridgeList.get(0).equals(initChannel)&&bridgeList.get(1).equals(channel))
		{
			((CallFrame) entry.getKey()).setVisible(false);
	        ((CallFrame) entry.getKey()).dispose();
			CallFrame.bridgeLines.remove((CallFrame) entry.getKey());
		}
	}

	java.awt.EventQueue.invokeLater(new Runnable() {
	    public void run() {
             if(listPanel.countComponents()==10) listPanel.remove(9);   
	         JButton button = new JButton(); 
	         button.setPreferredSize(new Dimension(300,70));
	         button.setMaximumSize(new Dimension(300,70));
	         button.addActionListener(new ActionListener() { 
		     public void actionPerformed(ActionEvent ev) { 
		     	Call(outputChan);
              } 
	         });
           	 button.setLayout(new BorderLayout());
           	JLabel label1 = null;
             if(numbers.get(outputChan)!=null) {
            	 label1 = new JLabel(numbers.get(outputChan).get(0),JLabel.CENTER);
                 label1.setFont(listNamesFont);
             }
             JLabel label2;
           //if(flagHangup==0) label2 = new JLabel(outputChan,new ImageIcon(urlRed),JLabel.CENTER);
          // else label2 = new JLabel(outputChan,new ImageIcon(urlGreen),JLabel.CENTER);
             if(flagHangup==0) label2 = new JLabel(outputChan + "исходящий",JLabel.CENTER);
             else label2 = new JLabel(outputChan+ "входящий",JLabel.CENTER);
             //label2.setFont(NumButtonsFont);
				if (label1 != null) {
					button.add(BorderLayout.NORTH, label1);
					button.add(BorderLayout.CENTER, label2);
				}  
				else button.add(BorderLayout.CENTER, label2);
             listPanel.add(button,0);
             listPanel.validate();
             listPanel.repaint();
	    }});
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
	 
	 JButton sleep = Sleep();
	 JPanel slPanel = new JPanel();
	 slPanel.setLayout(new BorderLayout());
	 slPanel.add(sleep,BorderLayout.EAST);
     Box listBox = Box.createVerticalBox(); 
     listBox.setPreferredSize(new Dimension(140,620));
     listBox.add(Box.createVerticalGlue());
     //listBox.add(sleep);
     contentBox.add(Box.createVerticalStrut(10));
     listBox.add(listPanel);
     listBox.add(Box.createVerticalGlue());
     this.add(slPanel,BorderLayout.NORTH);
     this.add(contentBox,BorderLayout.WEST);
     this.add(listBox,BorderLayout.CENTER);
	 MainFrame.container.add(this,BorderLayout.CENTER);
	}
	

}
