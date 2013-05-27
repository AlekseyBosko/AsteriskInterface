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
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
//телефон, который отрисовывается при переводе звонка
@SuppressWarnings("serial")
public class RedirectPhone extends Phone{
	private static JTextField RedirectField;
    private static List<String> redirectList = new ArrayList<String>();
    
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
					new MessageFrame("Укажите номер для перевода звонка");
				else Redirect(RedirectField.getText());				 
	            } 
		    });
		//создание кнопки возврата в гланое окно
		URL urlClean = ClassLoader.getSystemResource("Banned-icon.png");
		JButton cleanButton = new JButton(new ImageIcon(urlClean));
		//JButton cleanButton = new JButton("Отмена");
		cleanButton.setFont(ButtonsFont);
		cleanButton.setPreferredSize(new Dimension(180,90));
		cleanButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				RedirectField.setText("");
	            } 
		    });

		JButton removeButton = new JButton("Вернуться");
		removeButton.setFont(ButtonsFont);
		removeButton.setPreferredSize(new Dimension(280,100));
		removeButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent ev) { 
				RedirectField.setText("");
				CallFrame.MakeFramesVisible(true);
				MainFrame.RedirectPanel.setVisible(false);
				MainFrame.RedirectPanel.dispose();

	            } 
		    });
		 JPanel FuncPanel = new JPanel();
		 FuncPanel.add(RedirButton);
		 FuncPanel.add(cleanButton);
		 FuncPanel.add(removeButton);
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
	public static void Redirect(String redirectNumber){
		try {
 			Socket telnet = new Socket(AsteriskIp, 5038);
            telnet.setKeepAlive(true);
            PrintWriter writer = new PrintWriter(telnet.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
                           
               writer.print("Action: login\r\n");
               writer.print("UserName: "+WriteUser+"\r\n");
               writer.print("Secret: "+WriteUserPassword+"\r\n\r\n");           
               writer.flush();             

               String channelToPark = null;
               String phoneChannel = null;
               CallFrame callFrame = null;
               Hashtable<CallFrame, List<String>> linkBridgeLines = CallFrame.getLinkBridgeLines(); 
               Iterator<Entry<CallFrame, List<String>>> linkBridgeIterator = linkBridgeLines.entrySet().iterator();
   			while (linkBridgeIterator.hasNext()) {
   				Entry<CallFrame, List<String>> entry = linkBridgeIterator.next();
        			List<String> bridgeList = (List<String>) entry.getValue();
        			
   				String bridgeInitNumber=bridgeList.get(0).substring(0,bridgeList.get(0).indexOf("-"));
   				String bridgeNumber=bridgeList.get(1).substring(0,bridgeList.get(1).indexOf("-"));
   				
				for(int i=0;i<Phone.AllExtensions.size();i++)
				{
					if(bridgeInitNumber.equals(Phone.AllExtensions.get(i)))
					{		
			            phoneChannel = bridgeList.get(0);
						channelToPark = bridgeList.get(1);
						callFrame=(CallFrame) entry.getKey();
					}
					else if(bridgeNumber.equals(Phone.AllExtensions.get(i)))
					{
			            phoneChannel = bridgeList.get(1);
						channelToPark = bridgeList.get(0);
						callFrame=(CallFrame) entry.getKey();
					}
				}		
				linkBridgeIterator.remove();
	   			CallFrame.removeFromList(callFrame);
	   			callFrame.setVisible(false);
	   			callFrame.dispose();


   			}

         System.out.println(channelToPark);
                
               if(channelToPark==null) {
                	 RedirectField.setText("");
                	 writer.flush();
                	 telnet.close();
                	new MessageFrame("Отсутствует телефонный разговор для перевода");
                 }
                 else{
                 writer.print("Action: Park\r\n"); 
                 writer.print("Channel: SIP/"+channelToPark+"\r\n");
                 writer.print("Channel2: SIP/"+phoneChannel+"\r\n\r\n");
                 writer.print("ACTION: ParkedCalls\r\n\r\n");  
                 writer.flush();

                 boolean flagToPark = false;
                 String str= null,channel = null;

                 do
                 {str = in.readLine();
                 if(str.startsWith("Event: ParkedCall"))
                      { do{
                      	str = in.readLine();
                      	if(str.startsWith("Channel: SIP/"+channelToPark))
                      	{flagToPark=true;
                      	break;}
                      	channel = str;
                      }
                      while(!str.startsWith("From:"));
                      }
                  if(flagToPark==true) break;
                }
               while(!str.startsWith("Event: ParkedCallsComplete"));
                 
                 String parkNumber = channel.substring(7);
                 String callerId=channelToPark.substring(0,channelToPark.indexOf("-"));

                 writer.print("Action: Originate\r\n");
                 writer.print("Channel: SIP/"+redirectNumber+"\r\n" );
                 writer.print("Exten: "+parkNumber+"\r\n");
                 writer.print("Context: parkedcalls\r\n");
                 writer.print("Priority: 1\r\n");
                 writer.print("CallerId: phone<"+callerId+">\r\n");
                 writer.print("Async: yes\r\n\r\n");
                 writer.print("Action: LOGOFF\r\n\r\n");
                 writer.flush();
                 telnet.close();
                 
                new MessageFrame("Номер "+callerId +" был успешно переведён на абонента "+redirectNumber);
     			 RedirectField.setText("");
     			//добавление в список для предотвращения возврата
     			 redirectList.add(parkNumber);
				    List<String> initLinesForList = CallFrame.GetInitLinesForList();
    			  if(initLinesForList.contains(channel)) initLinesForList.remove(channel);  	
                 }
     			 MainFrame.RedirectPanel.setVisible(false);
     			 MainFrame.RedirectPanel.dispose();
     			CallFrame.MakeFramesVisible(true);
              
            }
  catch (SocketException e1) {
     e1.printStackTrace();    
 } catch (IOException e1) {
     e1.printStackTrace();
 }
} 
    public static List<String> RedirectList(){
    return redirectList;
    }
	
}
