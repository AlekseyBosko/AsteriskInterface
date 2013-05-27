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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

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
  //скин интерфейса
  private static String skin;
  
  private static String str;
  private static String inputChannel = null;
  private static String outputChannel = null;
  private static String dialInitChannel = null;
  private static String dialChannel = null;
  private static Boolean error = false;
  private static Iterator<Entry<CallFrame, String>> dialIterator = null;
  private static String bridgeState = null;
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
                      PrintWriter writer = new PrintWriter(telnet.getOutputStream());
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

    //новый поток для обработки входящих вызовов для "истории звонков"
                  TimerTask taskForListNumbers = new TimerTask() {
                      public void run() {
                    	  str = null;

                    	  error = false;
						try {
							str = in.readLine();
							System.out.println(str);
							if (str.startsWith("Event: Hangup")) {
								do {                    	
									inputChannel = null;
		                    	    outputChannel = null;            	  
									str = in.readLine();
									System.out.println(str);
									if (str.startsWith("Channel: SIP/"))
										outputChannel = str.substring(13,str.indexOf("-"));
									if (str.startsWith("Channel: Parked/SIP/"))
										outputChannel = str.substring(20,str.indexOf("-"));
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
									do {
										str = in.readLine();
										System.out.println(str);
										if (str.startsWith("Channel: SIP/"))
											inputChannel = str.substring(13,str.indexOf("-"));
									} while (!str.startsWith("ConnectedLineName:"));
									for (int i = 0; i < Phone.AllExtensions.size(); i++) {
										// исходящий звонок
										if (inputChannel.equals(Phone.AllExtensions.get(i))) {
											Phone.NumForList(outputChannel, 0);
										}
										// входящий звонок
										else if (outputChannel.equals(Phone.AllExtensions.get(i))&& !outputChannel.equals(Phone.Extension)) {
											Phone.NumForList(inputChannel, 1);
										}
									}
								}
							}
							//отлов входящих/исходящих звонков
							if (str.startsWith("Event: Dial")) {
								dialChannel=null;
								dialInitChannel=null;
								str = in.readLine();
								str = in.readLine();
								str = in.readLine();
								//!!!!do while
								dialInitChannel = str.substring(13,str.indexOf("-"));
								str = in.readLine();
								dialChannel = str.substring(17,str.indexOf("-"));
								System.out.println(dialInitChannel);
								System.out.println(dialChannel);
								  for(int i = 0; i < Phone.AllExtensions.size(); i++)
									  if (dialInitChannel.equals(Phone.usualExtensions.get(i)))
									  {	    java.awt.EventQueue.invokeLater(new Runnable() {
											public void run() { 
												CallFrame CallFrame= new CallFrame();
										CallFrame.addOutputCallPanel(dialInitChannel,dialChannel);
										CallFrame.setVisible(true);

											}});
									  }
								  for(int i = 0; i < Phone.usualExtensions.size(); i++)
									  if (dialChannel.equals(Phone.usualExtensions.get(i)))
									  {	    java.awt.EventQueue.invokeLater(new Runnable() {
											public void run() { 
												CallFrame CallFrame= new CallFrame();
										CallFrame.addInputCallPanel(dialInitChannel,dialChannel);
										CallFrame.setVisible(true);

											}});
									  }
							}
							if (str.startsWith("Event: Dial")) {
								dialInitChannel=null;
								dialChannel=null;			
								do{
									str=in.readLine();	
									if(str.startsWith("Bridgestate:")) bridgeState = str.substring(13);
									if(str.startsWith("Channel1:")) dialInitChannel = str.substring(10,str.indexOf("-"));
									if(str.startsWith("Channel2:")) dialChannel = str.substring(10,str.indexOf("-"));
								}while(str.startsWith("Uniqueid1:"));
								
								dialIterator = CallFrame.dialLines.entrySet().iterator();
				                while (dialIterator.hasNext()){
				                    Map.Entry entry = dialIterator.next();
				                    entry.getValue();
				                    entry.getKey();										
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
      /*Event: Bridge
      Privilege: call,all
      Bridgestate: Link
      Bridgetype: core
      Channel1: SIP/100-000003b0
      Channel2: SIP/111-000003b1
      Uniqueid1: 1366821454.964
      Uniqueid2: 1366821454.965
      CallerID1: 100
      CallerID2: 111*/

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