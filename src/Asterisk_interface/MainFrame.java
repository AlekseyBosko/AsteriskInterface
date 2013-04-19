package Asterisk_interface;

import java.awt.Container;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
	setUndecorated(true);
    
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
       
       while ((line = br.readLine()) != null) {
    	   line=line.trim();
    	  if(line.startsWith("Speaker")) {
              Phone.Extension = line.substring(22).trim();
    		  Phone.AllExtensions.add(line.substring(22).trim());
    	  }
    	  if(line.startsWith("Phone number")) Phone.AllExtensions.add(line.substring(14).trim());
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
	    	
			String name = info.get(j+1).getName(); 
            String number = info.get(j+2).getName(); 	  
            Phone.numbers.put(number, name);
            j+=2;
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
                      writer.print("ACTION: CoreShowChannels\r\n\r\n");  
                      writer.flush();     
                      } 
                  catch (SocketException e1) {
                      e1.printStackTrace();}
                  catch (IOException e1) {
                      e1.printStackTrace();}
    //новый поток для обработки входящих вызовов для "истории звонков"
                  TimerTask taskForListNumbers = new TimerTask() {
                      public void run() {
                          String str;
                          String inputChannel = null;
                          String outputChannel = null;
                      try{
                          str = in.readLine();
                          if(str.startsWith("Event: Hangup")){
         	                  do{  
                 	               str = in.readLine(); 
                 	              if(str.startsWith("Channel: SIP/")) inputChannel = str.substring(13,str.indexOf("-"));
                                  if(str.startsWith("ConnectedLineNum: ")) outputChannel = str.substring(18);
                                }
                              while(!str.startsWith("ConnectedLineName:"));
         	                  for(int i=0;i<Phone.AllExtensions.size();i++){
         	                      if(inputChannel.equals(Phone.AllExtensions.get(i))&&!outputChannel.equals("<unknown>")){	
                 	              Phone.NumForList(outputChannel, 0);
         	                      }
         	                      else if(outputChannel.equals(Phone.AllExtensions.get(i))){
                	              Phone.NumForList(inputChannel, 1);
         	                      }
           	                  }
         	                 
                          }   
                     }
                     catch (SocketException e1) {
                          e1.printStackTrace();    
                     } catch (IOException e1) {
                          e1.printStackTrace();
                     }
                     }
                 };
      Timer timer = new Timer();
      timer.schedule(taskForListNumbers, 0, 10);

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