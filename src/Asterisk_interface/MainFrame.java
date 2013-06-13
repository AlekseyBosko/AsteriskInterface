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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
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
  public static MainFrame frame;
  //скин интерфейса
  private static String skin;
   
  private static BufferedReader telnetReader;
  private static PrintWriter telnetWriter;

  //положение CallFrames по оси x
  public static int xLocationForCallFrame = 100;
  //поток Asterisk
  private static AsteriskThread AsteriskThread;
  //имя файла, в котором хранится информация о пользователе
  private static String UsersInfoFile;

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
       Phone.usualExtensions = new ArrayList<String>();
       while ((line = br.readLine()) != null) {
    	   line=line.trim();
    	  if(line.startsWith("Speaker")) {
              Phone.MainExtension = line.substring(22).trim();
    		  Phone.AllExtensions.add(line.substring(22).trim());
    	  }
    	  if(line.startsWith("Phone number")) {
    		  Phone.usualExtensions.add(line.substring(14).trim());
    		  Phone.AllExtensions.add(line.substring(14).trim());
    	  }
          if(line.startsWith("Ip adress")) Phone.AsteriskIp = line.substring(20).trim();
          else if(line.startsWith("WriteUser")) Phone.WriteUser = line.substring(11).trim();
          else if(line.startsWith("WritePassword")) Phone.WriteUserPassword = line.substring(15).trim();
          else if(line.startsWith("Context")) Phone.Context=line.substring(9).trim();
          else if(line.startsWith("Skin")) skin=line.substring(7).trim();
          else if(line.startsWith("ReadUser")) Phone.ReadUser = line.substring(10).trim();
          else if(line.startsWith("ReadPassword")) Phone.ReadUserPassword = line.substring(14).trim();
          else if(line.startsWith("UsersInfoFile")) UsersInfoFile = line.substring(15).trim();
          else if(line.startsWith("PhotoFolder")) Phone.PhotoFolder = line.substring(13).trim();
          else if(line.startsWith("DefaultPhoto")) Phone.DefaultPhoto = line.substring(14).trim();
       }
       br.close();
	
}
//загрузка xml файла
  private static Document getXmlFile() throws Exception {
	  try {
		  File file=new File(UsersInfoFile);
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
	
	Phone = new Phone();
    Phone.CreatePhone();
    validate();
    

    //загрузка данных для подписей "истории звонков"
	for (int i = 0; i < offices.size(); i++) {
		officeNode officeNode = offices.get(i);
   	    List<positionNode> position = officeNode.getPosition();
	    for (int j=0; j < position.size();j++) {
	    	positionNode positionNode = position.get(j);
	    	List<infoNode> info = positionNode.getInfo();
	        List<String> newList = new ArrayList<String>();
			String name = info.get(0).getName(); 
            String number = info.get(1).getNumber(); 
            String photo = info.get(2).getPhoto(); 
            newList.add(name);
            newList.add(photo);
            Phone.numbers.put(number, newList);
            j+=1;
	    }
	}

 }
  public static PrintWriter TelnetWriter()
  {
	  return telnetWriter;
  }
  public static BufferedReader TelnetReader()
  { 
	  return telnetReader;
  }

  //остановка главного потока на timeToSleep миллисекунд, чтобы поток Asterisk смог обработать команды
  public static void SleepThread(int timeToSleep){
	  
      try{
          Thread.sleep(timeToSleep);		
      }catch(InterruptedException e){}
	  
  }
  //вовращает поток asterisk
  public static AsteriskThread getAsteriskThread(){
	  return AsteriskThread;
  }
  //делает главный фрейм недоступным для нажатий на кнопки
  public static void MakeInterfaceEnable(Boolean state)
  {
	  frame.setEnabled(state);
	  CallFrame.MakeFramesNotEnable(state);
  }

 public static void main(String[] args) {  
	//загрузка настроек для интерфейса
	  try {
			getConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//установка скина для приложения
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
	               frame = new MainFrame(); 
                   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
                   frame.setVisible(true); 
	             
                   try{
	                  @SuppressWarnings("resource")
					Socket telnet = new Socket(Phone.AsteriskIp, 5038);
	                  telnet.setKeepAlive(true);
                      telnetWriter = new PrintWriter(telnet.getOutputStream());
                      telnetReader = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
                  
                      telnetWriter.print("Action: login\r\n");
                      telnetWriter.print("UserName: "+Phone.ReadUser+"\r\n");
                      telnetWriter.print("Secret: "+Phone.ReadUserPassword+"\r\n\r\n");           
                      telnetWriter.flush();     
                      
                      } 
                  catch (SocketException e1) {
                      e1.printStackTrace();}
                  catch (IOException e1) {
                      e1.printStackTrace();}

                   AsteriskThread = new AsteriskThread();
                   AsteriskThread.start();
		    	}

	    });

  } 
 
  //классы для считывания xml файла(определяются конфигурацией файла)B 
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
	         public List<positionNode> getPosition() {
	 	            ArrayList<positionNode> position = new ArrayList<positionNode>();
	 	            NodeList positionNodes = node.getChildNodes();
	             for (int i = 0; i < positionNodes.getLength(); i++) {
	 	                Node node = positionNodes.item(i);
	 	 
	                 if (node.getNodeType() == Node.ELEMENT_NODE) {
	                	 positionNode positionNode = new positionNode(node);
	                	 position.add(positionNode);
	 	                }
	             }
	  
	 	            return position;
	 	        }
	        public String getName() {

           NamedNodeMap attributes = node.getAttributes();

	            Node nameAttrib = attributes.getNamedItem("name");

	            return nameAttrib.getNodeValue();
	        }
	    }
	    public static class positionNode {
	   	 
	    	 Node node;
	    	 
	         public positionNode(Node node) {
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
        public String getNumber() {
       	 
            NamedNodeMap attributes = node.getAttributes();
 
            Node nameAttrib = attributes.getNamedItem("number");

        return nameAttrib.getNodeValue();
        }
        public String getPhoto() {
       	 
            NamedNodeMap attributes = node.getAttributes();
 
            Node nameAttrib = attributes.getNamedItem("photo");

        return nameAttrib.getNodeValue();
        }
      }
	        
}
