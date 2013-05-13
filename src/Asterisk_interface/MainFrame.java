package Asterisk_interface;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial") 
public class MainFrame extends JFrame { 
  private static final int WIDTH = 700; 
  private static final int HEIGHT = 500;

  //первая страница выводимая на экран
  public static FirstPageDisplay FirstPageDisplay;
  //вторая страница выводимая на экран
  public static SecondPageDisplay SecondPageDisplay;

  //первая страница для перевода звонка выводимая на экран
  public static FirstPageDisplay RedirectFirstPage;
  //вторая страница для перевода звонка выводимая на экран
  public static SecondPageDisplay RedirectSecondPage;
//панель, которая появляется при нажатие кнопки Перевод
  public static Redirect RedirectPanel;
  //панель с телефоном
  public static Phone Phone;
  public static Container container;
  //блоки вводимых данных для первой страницы
  public static JPanel [] FirstPagePanel;
  private static BufferedReader in;
  private static PrintWriter writer;
  //скин интерфейса
  private static String skin;
  
  private static String str;
  private static String hangupInitChannel = null;
  private static String hangupChannel = null;
  private static String dialInitChannel = null;
  private static String dialChannel = null;
  private static String dialSubEvent = null;
  private static Boolean error = false;
  private static Iterator<Entry<CallFrame, List<String>>> bridgeIterator = null;
  private static Iterator<Entry<CallFrame, List<String>>> linkBridgeIterator = null;
  //инициализация главного фрейма
public MainFrame() { 
    super("Ip phone interface"); 
    setSize(WIDTH, HEIGHT); 
    Toolkit kit = Toolkit.getDefaultToolkit();      
    setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);  
    setExtendedState(MAXIMIZED_BOTH);
    
    container = this.getContentPane();
    
    JMenuBar menuBar = new JMenuBar(); 
    setJMenuBar(menuBar); 

    Document doc = null;
	
    try {
    	//загрузка xml файла
		doc = getXmlFile();
	    }
	catch (Exception e) {
		e.printStackTrace();
	}   
	//считывание xml файла
	showDocument(doc);
	//отображение окна во весь экран
	//setUndecorated(true);
    
  } 
//загрузка настроек для интерфейса
@SuppressWarnings({ "static-access" })
private static void getConfig() throws IOException{
	   File file = new File( "C:\\temp\\config.txt" );

       BufferedReader br = new BufferedReader (
           new InputStreamReader(
               new FileInputStream(file), "UTF-8"
           )
       );
       String line = null;
       Phone.AllExtensions = new ArrayList<String>();
       Phone.usualExtensions = new ArrayList<String>();
       while ((line = br.readLine()) != null) {
    	   line=line.trim();
    	  if(line.startsWith("Speaker")) {
              Phone.Extension = line.substring(22).trim();
    		  Phone.AllExtensions.add(line.substring(22).trim());
    	  }
    	  if(line.startsWith("Phone number")) {
    		  Phone.usualExtensions.add(line.substring(14).trim());
    		  Phone.AllExtensions.add(line.substring(14).trim());
    	  }
          if(line.startsWith("Ip adress")) Phone.AsteriskIp = line.substring(20).trim();
          if(line.startsWith("User")) Phone.User = line.substring(6).trim();
          if(line.startsWith("Password")) Phone.Password = line.substring(10).trim();
          if(line.startsWith("Context")) Phone.Context=line.substring(9).trim();
          if(line.startsWith("Skin")) skin=line.substring(7).trim();
          if(line.startsWith("ListUser")) Phone.ListUser = line.substring(10).trim();
          if(line.startsWith("ListPassword")) Phone.ListPassword = line.substring(14).trim();
       }
       br.close();
	
}
//загрузка xml файла
  private static Document getXmlFile() throws Exception {
	  try {
		  File file=new File("C:\\temp\\Vitebsk.xml");
          DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
          f.setValidating(false);
          DocumentBuilder builder = f.newDocumentBuilder();
          return builder.parse(file);
	  } catch (Exception exception) {
	      String message = "XML parsing error!";
	      throw new Exception(message);
		          }
  }
//считывание xml файла
  @SuppressWarnings("static-access")
private void showDocument(Document doc) {
	StringBuffer content = new StringBuffer();
	Node node = doc.getChildNodes().item(0);
	newFirmNode firmNode = new newFirmNode(node);
	
	content.append("Название фирмы \n");

    List<officeNode> offices = firmNode.getOffices();
    //инициализация окон приложения
    RedirectPanel = new Redirect();

    SecondPageDisplay = new SecondPageDisplay();
    FirstPageDisplay = new FirstPageDisplay(SecondPageDisplay, container,offices);

    RedirectSecondPage = new SecondPageDisplay();
    RedirectFirstPage = new FirstPageDisplay(RedirectSecondPage,RedirectPanel.GetCont(),offices);

   
	RedirectSecondPage.set(RedirectFirstPage,RedirectPanel.GetCont());
	RedirectFirstPage.Layout(0);

	SecondPageDisplay.set(FirstPageDisplay, container);
	FirstPageDisplay.Layout(0);
	

	//RedirectPanel.addRedirectPanel();

	Phone = new Phone();
    Phone.CreatePhone();
    validate();
    

    //загрузка данных для подписей "истории звонков"
	for (int i = 0; i < offices.size(); i++) {
		officeNode officeNode = offices.get(i);
	    List<infoNode> info = officeNode.getInfo();	    
	    for (int j=0; j < info.size();j++) {
	        List<String> newList = new ArrayList<String>();
			String name = info.get(j+1).getName(); 
            String number = info.get(j+2).getName(); 
            String photo = info.get(j+3).getName(); 
            newList.add(name);
            newList.add(photo);
            Phone.numbers.put(number, newList);
            j+=3;
	    }
	}

 }
  public static PrintWriter ReturnWriter()
  {
	  return writer;
  }
  
 public static void main(String[] args) {  
	//загрузка настроек для интерфейса
	  try {
			getConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//установка скина для приложения	z
	  java.awt.EventQueue.invokeLater(new Runnable() {
		    public void run() {
		            try {
		            	SubstanceLookAndFeel laf = new SubstanceMarinerLookAndFeel();
		                UIManager.setLookAndFeel(laf);
		                String skinClassName = "org.pushingpixels.substance.api.skin."+skin+"Skin";
		                SubstanceLookAndFeel.setSkin(skinClassName);
		                JDialog.setDefaultLookAndFeelDecorated(true);
		                
		            } catch (UnsupportedLookAndFeelException e) {
		                throw new RuntimeException(e);
		            }
		    }
		    });
	  //главная функция программы
	  java.awt.EventQueue.invokeLater(new Runnable() {
		    @SuppressWarnings("static-access")
			public void run() {
	               MainFrame frame = new MainFrame(); 
                   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
                   frame.setVisible(true); 
	             
                   try{
	                  @SuppressWarnings("resource")
					Socket telnet = new Socket(Phone.AsteriskIp, 5038);
                      telnet.setKeepAlive(true);
                      writer = new PrintWriter(telnet.getOutputStream());
                      in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
                  
                      writer.print("Action: login\r\n");
                      writer.print("UserName: "+Phone.ListUser+"\r\n");
                      writer.print("Secret: "+Phone.ListPassword+"\r\n\r\n");           
                      writer.flush();     
                      } 
                  catch (SocketException e1) {
                      e1.printStackTrace();}
                  catch (IOException e1) {
                      e1.printStackTrace();}

    //новый поток для обработки событий происходящих в asterisk
                  TimerTask taskForListNumbers = new TimerTask() {
                      @SuppressWarnings("unchecked")
					public void run() {
                    	  str = null;

						try {
							str = in.readLine();
							System.out.println(str);
							if (str.equals("Event: Hangup")) {
								hangupInitChannel = null;
								hangupChannel = null;          	  
		                    	  error = false;
								do {
									str = in.readLine();
									System.out.println(str);
									if (str.startsWith("Channel: SIP/"))
										hangupChannel = str.substring(13,str.indexOf("-"));
									if (str.startsWith("Channel: Parked/SIP/"))
										hangupChannel = str.substring(20,str.indexOf("-"));
									if (str.startsWith("Cause: ")) {
										if (!str.substring(7).equals("16"))
											error = true;
										System.out.println(str.substring(7));
									}
									if (str.startsWith("Cause-txt: User alerting, no answer"))
										error = true;
								} while (!str.startsWith("Cause-txt:"));
								System.out.println(error);
								if (error != true) {
									int iter=0;
									do {
										str = in.readLine();
										if(str.startsWith("Event: Hangup")){
											do {
												str = in.readLine();
												
												if (str.startsWith("Channel: SIP/")) {
													hangupInitChannel = str.substring(13,str.indexOf("-"));break;}											
											} while (!str.startsWith("ConnectedLineName:"));	
											iter=25;
										}
										if(str.startsWith("Event: New")||str.startsWith("Event: Bridge")) break;
										iter+=1;
									} while (iter<25);
									for (int i = 0; i < Phone.AllExtensions.size(); i++) {
										// исходящий звонок
										System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
										if (hangupInitChannel!=null&&hangupInitChannel.equals(Phone.AllExtensions.get(i))&&hangupChannel!=null) {
											Phone.NumForList(hangupChannel,hangupInitChannel, 0);
											System.out.println("outputChannel:  "+hangupChannel);
										}
										// входящий звонок
										else if (hangupChannel!=null&&hangupChannel.equals(Phone.AllExtensions.get(i))&&hangupInitChannel!=null) {
											Phone.NumForList(hangupInitChannel,hangupChannel, 1);
										}
									}
								}
							}
							//отлов входящих/исходящих звонков
							if (str.startsWith("Event: Dial")) {
								dialChannel=null;
								dialInitChannel=null;
								dialSubEvent = null;
								do{System.out.println(str);
									str=in.readLine();	
									if(str.startsWith("SubEvent:")) dialSubEvent = str.substring(10);
									//if(str.startsWith("Channel:")) dialInitChannel = str.substring(13,str.indexOf("-"));
									if(str.startsWith("Channel:")) dialInitChannel = str.substring(13);
									if(str.startsWith("Destination:")) dialChannel = str.substring(17,str.indexOf("-"));
								}while(!str.startsWith("CallerIDNum:"));
								//dialInitChannel = str.substring(13,str.indexOf("-"));
								//str = in.readLine();
								//dialChannel = str.substring(17,str.indexOf("-"));
								System.out.println("****************1111111111*****************");
								System.out.println(dialInitChannel);
								System.out.println(dialChannel);
								if(dialSubEvent.equals("Begin")){
								  for(int i = 0; i < Phone.usualExtensions.size(); i++)
								  { if (dialChannel.equals(Phone.usualExtensions.get(i)))
									  {	    java.awt.EventQueue.invokeLater(new Runnable() {
											public void run() { 
												CallFrame CallFrame= new CallFrame();
										CallFrame.addInputCallPanel(dialInitChannel,dialChannel);
										CallFrame.setVisible(true);
											}});
									  }
								   else if (dialInitChannel.substring(0,dialInitChannel.indexOf("-")).equals(Phone.usualExtensions.get(i)))
								  {	    java.awt.EventQueue.invokeLater(new Runnable() {
										public void run() { 
											CallFrame CallFrame= new CallFrame();
									CallFrame.addOutputCallPanel(dialInitChannel.substring(0,dialInitChannel.indexOf("-")),dialChannel);
									CallFrame.setVisible(true);
										}});
								}}}
							}
							//проверка вызовов, поставленных на удержание
							if(str.startsWith("Event: MusicOnHold"))
							{
							String musicOnHoldNumber = null;
							String musicOnHoldState=null;
								do{str=in.readLine();
								    //канал поставленный на удержание в сети
									if(str.startsWith("Channel:")) musicOnHoldNumber = str.substring(13,str.indexOf("-"));
									//start/stop moh
									if(str.startsWith("State:")) musicOnHoldState = str.substring(7);
								}while(!str.startsWith("UniqueID:"));	
								System.out.println("musicOnHoldChannel"+musicOnHoldNumber);
								System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
								System.out.println(musicOnHoldState);
								//проверка вызовов, поставленных на удержание пользователем
								Iterator<Entry<CallFrame, Hashtable<String, String>>> mohIterator = CallFrame.parkBridgeLines.entrySet().iterator();
								while (mohIterator.hasNext()) {
									Map.Entry entry = mohIterator.next();
									Hashtable<String, String> hashTable = (Hashtable<String, String>) entry.getValue();
								   System.out.println("!!!!!!!!!!"+hashTable+"!!!!!!!!");
								   //когда Stop позволяем установиться звонку
								    if(musicOnHoldState.equals("Stop")&&hashTable.get("HoldNumber").equals(musicOnHoldNumber)) {
								    	CallFrame.parkBridgeLines.remove((CallFrame) entry.getKey());
										List<String> list = new ArrayList<String>();
							         	list.add(Phone.Extension);
							 		    list.add(musicOnHoldNumber);
										CallFrame.bridgeLines.put((CallFrame) entry.getKey(),list);
								    	System.out.println("OK");
								    	break;
								    }
								        	
								}
								if(musicOnHoldState.equals("Start")){
								//for (int i = 0; i < Phone.AllExtensions.size(); i++) {
									//if (musicOnHoldChannel.equals(Phone.AllExtensions.get(i)))
									//{   
								//избежание ошибок(ненужное выполнение функции hangup)
										do{
											str=in.readLine();	
											if(str.startsWith("Event: ParkedCall")) break;
											System.out.println(str);
										}while(!str.startsWith("Bridgestate: Unlink"));
								}
									//}
							//	}
							}
							//проверка установленных/законченных вызовов
							if (str.startsWith("Event: Bridge")) {
								String bridgeInitChannel=null;
								String bridgeChannel=null;
								String bridgeState = null;	
								String bridgeInitNumber=null;
								String bridgeNumber=null;
								do{
									str=in.readLine();	
									if(str.startsWith("Bridgestate:")) bridgeState = str.substring(13);
									if(str.startsWith("Channel1:")) {
										bridgeInitChannel = str.substring(10);
										bridgeInitNumber = str.substring(14,str.indexOf("-"));
									}
									if(str.startsWith("Channel2:")) {
										bridgeChannel = str.substring(10);
										bridgeNumber = str.substring(14,str.indexOf("-"));
									}
								}while(!str.startsWith("Uniqueid1:"));	
								System.out.println("bridgeChannel   "+bridgeState);
								//проверка установленных вызовов
								if (bridgeState.equals("Link")) {
									System.out.println("!!!!!!   "+CallFrame.bridgeLines);
									bridgeIterator = CallFrame.bridgeLines.entrySet().iterator();
									while (bridgeIterator.hasNext()) {
										Map.Entry entry = bridgeIterator.next();
										System.out.println("bridgeInitNumber"+bridgeInitNumber);
										System.out.println("bridgeNumber"+bridgeNumber);
										List<String> bridgeList = (List<String>) entry.getValue();
										if(bridgeList.get(0).equals(bridgeInitNumber)&&bridgeList.get(1).equals(bridgeNumber))
										{ System.out.println("***************((((******************"+bridgeList);
											for (int i = 0; i < Phone.AllExtensions.size(); i++) {
												if (bridgeInitNumber.equals(Phone.AllExtensions.get(i)))
												{   
													((CallFrame) entry.getKey()).addDialPanel(bridgeNumber,bridgeInitNumber);
												}
												else if (bridgeNumber.equals(Phone.AllExtensions.get(i)))
												{    
													((CallFrame) entry.getKey()).addDialPanel(bridgeInitNumber,bridgeNumber);
												}
											}
											List<String> list = new ArrayList<String>();
								         	list.add(bridgeInitChannel);
								 		    list.add(bridgeChannel);
											CallFrame.linkBridgeLines.put((CallFrame) entry.getKey(),list);
											CallFrame.bridgeLines.remove((CallFrame) entry.getKey());
											((CallFrame) entry.getKey()).HoldIfNotActive();
										}
									}
								}
								//проверка законченных вызовов
								else if (bridgeState.equals("Unlink")) {
									System.out.println(CallFrame.linkBridgeLines);
									linkBridgeIterator = CallFrame.linkBridgeLines.entrySet().iterator();
									while (linkBridgeIterator.hasNext()) {
										Map.Entry entry = linkBridgeIterator.next();
										System.out.println("bridgeInitChannel"+bridgeInitChannel);

										List<String> bridgeList = (List<String>) entry.getValue();
										if(bridgeList.get(0).equals(bridgeInitChannel)&&bridgeList.get(1).equals(bridgeChannel))
										{System.out.println("!!!!!6457457!!!!!!!!!!!!!!!");
										    ((CallFrame) entry.getKey()).setVisible(false);
										    ((CallFrame) entry.getKey()).dispose();
										    CallFrame.linkBridgeLines.remove((CallFrame) entry.getKey());
										     
										}
									}
								}
							}
						} catch (SocketException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
                 };
      Timer timerList = new Timer();
      timerList.schedule(taskForListNumbers, 0, 10);
		    }
	    });
  } 
  //классы для считывания xml файла(определяются конфигурацией файла)
  public static class newFirmNode {

	        Node node;
 
        public newFirmNode(Node node) {
	            this.node = node;
	        }
	 
        public List<officeNode> getOffices() {
	            ArrayList<officeNode> offices = new ArrayList<officeNode>();
 
	            NodeList officeNodes = node.getChildNodes();
	 
            for (int i = 0; i < officeNodes.getLength(); i++) {
	                Node node = officeNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
	 
	                    officeNode officeNode = new officeNode(node);
	                    offices.add(officeNode);
	                }
            }
 
	            return offices;
	        }
  }      
	    public static class officeNode {
	 
	    	 Node node;
	    	 
	         public officeNode(Node node) {
	 	            this.node = node;
	 	        }
	         public List<infoNode> getInfo() {
	 	            ArrayList<infoNode> info = new ArrayList<infoNode>();
	 	            NodeList infoNodes = node.getChildNodes();
	             for (int i = 0; i < infoNodes.getLength(); i++) {
	 	                Node node = infoNodes.item(i);
	 	 
	                 if (node.getNodeType() == Node.ELEMENT_NODE) {
	                	 infoNode infoNode = new infoNode(node);
	                	 info.add(infoNode);
	 	                }
	             }
	  
	 	            return info;
	 	        }
	        public String getName() {

           NamedNodeMap attributes = node.getAttributes();

	            Node nameAttrib = attributes.getNamedItem("name");

	            return nameAttrib.getNodeValue();
	        }
	    }
	    public static class infoNode {
	 
	        Node node;

	        public infoNode(Node node) {
	            this.node = node;
	        }

        public String getName() {
	 
	            NamedNodeMap attributes = node.getAttributes();
	 
	            Node nameAttrib = attributes.getNamedItem("name");

            return nameAttrib.getNodeValue();
	        }
      }
	        
}