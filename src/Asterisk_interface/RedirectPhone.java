package Asterisk_interface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
//телефон, который отрисовывается при переводе звонка
@SuppressWarnings("serial")
public class RedirectPhone extends Phone{
	private static JTextField RedirectField;
	
	public RedirectPhone() { 
	}
	//создание телефона(создание почти не отличается от обычного телефона, только в нём отсутсвует большая часть функциональных кнопок)
	public void CreateRedirectPhone(){
		//создание поле для ввода номера
		RedirectField = new JTextField();
	    RedirectField.setPreferredSize(new Dimension(500,100));
		RedirectField.setMaximumSize(new Dimension(500,100));
		RedirectField.setEditable(false);
		RedirectField.setHorizontalAlignment(SwingConstants.CENTER);
		RedirectField.setFont(DisplayFont);
		RedirectField.setBorder(BorderFactory.createLineBorder(Color.BLACK)); 
		//создание кнопки перевести
		JButton RedirButton = new JButton("Перевести");
		RedirButton.setPreferredSize(new Dimension(180,90));
		RedirButton.setFont(ButtonsFont);
		RedirButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				if(RedirectField.getText().isEmpty())
					JOptionPane.showMessageDialog(null,"Укажите номер для перевода звонка", "Ошибка", JOptionPane.WARNING_MESSAGE);
				else Redirect(RedirectField.getText());				 
	            } 
		    });
		//создание кнопки возврата в гланое окно
		JButton CancelButton = new JButton("Отмена");
		CancelButton.setFont(ButtonsFont);
		CancelButton.setPreferredSize(new Dimension(180,90));
		CancelButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				RedirectField.setText("");
				MainFrame.RedirectPanel.setVisible(false);
				MainFrame.RedirectPanel.dispose();
	            } 
		    });
		 JPanel FuncPanel = new JPanel();
		 FuncPanel.add(RedirButton);
		 FuncPanel.add(CancelButton);
		 //сбор всех компонентов в одну панель
		 JPanel ButtonPanel = addNumberButtons(RedirectField);

		 Box contentBox = Box.createVerticalBox(); 
		 contentBox.setPreferredSize(new Dimension(600,620));
		 contentBox.add(Box.createVerticalGlue());
		 contentBox.add(Box.createVerticalStrut(40));
		 contentBox.add(RedirectField);
		 contentBox.add(Box.createVerticalStrut(10));
		 contentBox.add(ButtonPanel);
		 contentBox.add(Box.createVerticalStrut(10));
		 contentBox.add(FuncPanel);
		 contentBox.add(Box.createVerticalGlue());
		 
	     this.add(contentBox);
		}
	//функция перевода
	public static void Redirect(String red){
		String redirect=red;
		try {
 			Socket telnet = new Socket(AsteriskIp, 5038);
            telnet.setKeepAlive(true);
            PrintWriter writer = new PrintWriter(telnet.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
                           
               writer.print("Action: login\r\n");
               writer.print("UserName: "+User+"\r\n");
               writer.print("Secret: "+Password+"\r\n\r\n");           
               writer.print("ACTION: CoreShowChannels\r\n\r\n");  
               writer.flush();             

               String str;
               String Channel=null;
               String Channel2= null;
               int lastDial = 0;
               do{
            	   str = in.readLine();
                   for(int i=0;i<AllExtensions.size();i++){
            	       if(str.startsWith("Channel: SIP/"+AllExtensions.get(i))){
            		       if(lastDial==0) {
            			       lastDial=Integer.parseInt(str.substring(str.indexOf("-")+1),16);
            			       Channel2 = str.substring(9);
            			   }
            		       else if(lastDial<=Integer.parseInt(str.substring(str.indexOf("-")+1),16))
            		       {
            			       lastDial=Integer.parseInt(str.substring(str.indexOf("-")+1),16);
            			       Channel2 = str.substring(9);
            		       }
            		       else break;
                    	   do{
                               str = in.readLine();
                               if(str.startsWith("BridgedChannel: SIP/"))
                               {Channel = str.substring(16);
                               break;}
                           }
                           while(!str.startsWith("BridgedUniqueID"));
            	       }     	   
                   }
              }
             while(!str.startsWith("Event: CoreShowChannelsComplete"));
                
               if(Channel==null) {
                	 RedirectField.setText("");
                	 writer.flush();
                	 telnet.close();
                	 JOptionPane.showMessageDialog(null,"Отсутствует телефонный " +
                 	 		"разговор для перевода", "Ошибка", JOptionPane.WARNING_MESSAGE);
                 }
                 else{
                 writer.print("Action: Park\r\n"); 
                 writer.print("Channel: "+Channel+"\r\n");
                 writer.print("Channel2: "+Channel2+"\r\n\r\n");
                 writer.print("ACTION: ParkedCalls\r\n\r\n");  
                 writer.flush();

                 int k=0;
                 String number = null;
                 do
                 {str = in.readLine();
                 if(str.startsWith("Event: ParkedCall"))
                      { do{
                      	str = in.readLine();
                      	if(str.startsWith("Channel: "+Channel))
                      	{k=1;
                      	break;}
                      	number = str;
                      }
                      while(!str.startsWith("From:"));
                      }
                  if(k==1) break;
                }
               while(!str.startsWith("Event: ParkedCallsComplete"));
                 
                 String park = number.substring(7);
                 String callerId=Channel.substring(4,Channel.indexOf("-"));

                 writer.print("Action: Originate\r\n");
                 writer.print("Channel: SIP/"+redirect+"\r\n" );
                 writer.print("Exten: "+park+"\r\n");
                 writer.print("Context: parkedcalls\r\n");
                 writer.print("Priority: 1\r\n");
                 writer.print("CallerId: phone<"+callerId+">\r\n");
                 writer.print("Async: yes\r\n\r\n");
                 writer.flush();
                 telnet.close();
                 
                 JOptionPane.showMessageDialog(null,"Номер "+callerId +" был успешно переведён на абонента "+redirect,
                		 "Перевод",  JOptionPane.INFORMATION_MESSAGE);
     			 RedirectField.setText("");
     			 MainFrame.RedirectPanel.setVisible(false);
     			 MainFrame.RedirectPanel.dispose();
                 }       
              
            }
  catch (SocketException e1) {
     e1.printStackTrace();    
 } catch (IOException e1) {
     e1.printStackTrace();
 }
} 
    
	
	
}
