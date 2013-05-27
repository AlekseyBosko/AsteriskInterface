package Asterisk_interface;



	import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
	//панель, которая появляется при нажатие кнопки Перевод
	@SuppressWarnings("serial")
	public class CallFrame extends JDialog { 
		
		private static final int WIDTH = 400; 
		private static final int HEIGHT = 530;
		private Font timeFont;
		private Font nameFont;
		private Font buttonFont;
		private int hour=0,minute=0,second=0;
		private Date time = new Date();
		private DateFormat dateFormate = new SimpleDateFormat("HH:mm:ss");
		private JLabel label1 = new JLabel();
	    private Timer callTimer = new Timer();
	    private TimerTask callTimeTask;
		private boolean startTimerFlag=false;
		private JButton answerButton;
		private JButton resumeButton;
		//телефон, который отрисовывается при переводе
	    private Container container;


        private static List<CallFrame> callFrameList = new ArrayList<CallFrame>();

	    public static Hashtable<CallFrame, List<String>> bridgeLines = new Hashtable<CallFrame, List<String>>();  
	    private static Hashtable<CallFrame, List<String>> linkBridgeLines = new Hashtable<CallFrame, List<String>>();
	    private static Hashtable<CallFrame, Hashtable<String, String>> parkBridgeLines = new Hashtable<CallFrame, Hashtable<String, String>>();
	    private static List<String> initLinesForList = new ArrayList<String>();  
		public CallFrame() {	
		    setSize(WIDTH, HEIGHT); 
		    Toolkit kit = Toolkit.getDefaultToolkit(); 

		    if(MainFrame.xLocationForCallFrame+450>=kit.getScreenSize().width) 
		    	MainFrame.xLocationForCallFrame=MainFrame.xLocationForCallFrame-MainFrame.frame.getWidth()+550;
		    setLocation(MainFrame.xLocationForCallFrame, (kit.getScreenSize().height - HEIGHT)/2);  
		    MainFrame.xLocationForCallFrame+=450;
		    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		    UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1.0);
		    timeFont = new Font("Serif", Font.PLAIN, 20);
		    nameFont = new Font("TimesRoman", Font.PLAIN, 22);
		    buttonFont = new Font("Serif", Font.PLAIN, 25);
		    container = this.getContentPane();
			setAlwaysOnTop( true );
			setVisible(false);

	        callFrameList.add(this);
		}
		
		 public void addInputCallPanel(final String remoteChannel, final String localChannel){
				if(remoteChannel!=null&&localChannel!=null){
			 final String remoteNumber = remoteChannel.substring(0,remoteChannel.indexOf("-"));

			 JPanel panel = new JPanel();
			
			answerButton = new JButton("Ответить");
			answerButton.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent ev) { 
					HoldIfNotActive();					
					Answer(remoteChannel);	                 
		            } 
			    });
		
			
		    answerButton.setPreferredSize(new Dimension(170, 80));
			answerButton.setBackground(new Color(0,30,0));
			answerButton.setForeground(new Color(255,255,255));
			answerButton.setFont(buttonFont);
			panel.add(answerButton);
			
			JButton hangupButton = new JButton("Завершить");
			hangupButton.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent ev) { 
	                 Hangup(remoteChannel);
		            } 
			    });
			hangupButton.setPreferredSize(new Dimension(170, 80));
			hangupButton.setBackground(new Color(100,0,0));
			hangupButton.setForeground(new Color(255,255,255));
			hangupButton.setFont(buttonFont);
			panel.add(hangupButton);

			
			addPanel(remoteNumber,null,panel);
	         CallFrame.MakeFramesNotEnable(false);
	  		new CallFramesTrue().start();
				}
				else {
					removeFromList(this);
					setVisible(false);
					dispose();
				}
		 }

		 public void addOutputCallPanel( String localChannel, String remoteChannel){
			if(!localChannel.equals(null)&&!remoteChannel.equals(null)){
			 String remoteNumber = remoteChannel.substring(0,remoteChannel.indexOf("-"));
			JPanel panel = new JPanel();
			panel.add(HangupButton(remoteChannel));

			addPanel(remoteNumber,null,panel);
	         CallFrame.MakeFramesNotEnable(false);
	  		new CallFramesTrue().start();
		 }
			else {
				removeFromList(this);
				setVisible(false);
				dispose();
			}
		 }

		 public void addDialPanel(final String remoteChannel, final String localChannel){
			 if(remoteChannel!=null&&localChannel!=null){
	    	  java.awt.EventQueue.invokeLater(new Runnable() {
	  		    public void run() {
	  		    	String remoteNumber = remoteChannel.substring(0,remoteChannel.indexOf("-"));
		JPanel panel = new JPanel();		panel.add(HangupButton(remoteChannel));
		label1.setPreferredSize(new Dimension(200, 70));
		label1.setFont(timeFont);
		label1.setAlignmentX(CENTER_ALIGNMENT);
		addPanel(remoteNumber,label1,panel);
		if(startTimerFlag==false) {
			StartCallTime();
			startTimerFlag=true;
		}
		setSize(new Dimension(400,600));
		}});
	    }
				else {
					removeFromList(this);
					setVisible(false);
					dispose();
				}
		 }
	    private JButton HangupButton(String num)
	    {final String localNumber = num;
	    	JButton hangupButton = new JButton("Завершить");
			hangupButton.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent ev) { 
	                 StopCallTime();
	                 Hangup(localNumber);

		            } 
			    });
			hangupButton.setMinimumSize(new Dimension(100, 100));
			hangupButton.setPreferredSize(new Dimension(230, 100));
			hangupButton.setBackground(new Color(100,0,0));
			hangupButton.setForeground(new Color(255,255,255));
			hangupButton.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			return hangupButton;
			
	    }
	    
		 private void addPanel(String remoteNum,JLabel l,JPanel p){
				container.removeAll();
		       JLabel label = l;
		       JPanel panel =p;
		    	JLabel label2 = new JLabel();
				label2.setPreferredSize(new Dimension(200, 70));
				label2.setFont(nameFont);
				label2.setAlignmentX(CENTER_ALIGNMENT);
				
				if(Phone.numbers.get(remoteNum)!=null) label2.setText(Phone.numbers.get(remoteNum).get(0));
				else label2.setText(remoteNum);
				
				Box photoBox = LoadPhoto(remoteNum);
				
				Box contBox = Box.createVerticalBox();
				contBox.add(Box.createVerticalGlue());
				contBox.add(label2);
				contBox.add(Box.createVerticalStrut(5));
				contBox.add(photoBox);
				if(label!=null){
					contBox.add(Box.createVerticalStrut(5));
					contBox.add(label);
				}
				contBox.add(Box.createVerticalStrut(20));
				contBox.add(panel);
				contBox.add(Box.createVerticalGlue());
					         
	           add(contBox);
				this.container.validate();
				this.container.repaint();

		 }
	    private Box LoadPhoto(String number)
	    {   File f=null;
		    ImagePanel ImagePanel = new ImagePanel();
	        ImagePanel.setLayout(new BorderLayout());
	        
	    	if(Phone.numbers.get(number)!=null) f=new File(Phone.PhotoFolder + Phone.numbers.get(number).get(1));
	        if(f==null||f.equals(null)) f=new File(Phone.DefaultPhoto);
	        try {
	        	ImagePanel.setImage(ImageIO.read(f));
			} catch (IOException e) {
				e.printStackTrace();				
			}
	        
			Box contentBox = Box.createHorizontalBox();
			contentBox.add(Box.createHorizontalGlue());
			contentBox.add(Box.createHorizontalStrut(80));
			contentBox.add(ImagePanel);
			contentBox.add(Box.createHorizontalStrut(80));
			contentBox.add(Box.createVerticalGlue());

			return contentBox;
	    }
	    public static void removeFromList(CallFrame frame)
	    {
	    	if(callFrameList.contains(frame))callFrameList.remove(frame);
	    }
	    //делает все CallFrame недоступным(state=false)/доступными(state=true) для нажатий на кнопки
	    public static void MakeFramesNotEnable(Boolean state)
	    {
	    	for(int i =0;i<callFrameList.size();i++)
	    		callFrameList.get(i).setEnabled(state);
	    }
	  //делает все CallFrame невидимыми(state=false)/видимыми(state=true)
	    public static void MakeFramesVisible(Boolean state)
	    {
	    	for(int i =0;i<callFrameList.size();i++)
	    		callFrameList.get(i).setVisible(state);
	    }
	    //делает все CallFrame недоступным на timeTosleep мс для нажатий на кнопки
	    public static void MakeCallFramesSleep(int timeTosleep)
	    {  			  
   		   new CallFramesTrue().start();
	    }
	    
	    
	    //добавляет кнопку возвонить к фрейму и убирает фотографию(пока не начнёт растягиваться)
	    public void MakeFrameNotActive(final CallFrame FrameToHold)
	    {  java.awt.EventQueue.invokeLater(new Runnable() {
  		    public void run() {
	    	resumeButton = new JButton("Возобновить звонок");
	    	resumeButton.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent ev) {   
	                 String numberToUnhold = parkBridgeLines.get(FrameToHold).get("NumberToUnhold");
	                 String holdChannel = parkBridgeLines.get(FrameToHold).get("HoldChannel");
	                 
	                 FrameToHold.HoldIfNotActive();
	                 Resume(numberToUnhold,holdChannel.substring(0,holdChannel.indexOf("-")));
		            } 
			    });
		
	    	resumeButton.setPreferredSize(new Dimension(300, 130));
	    	resumeButton.setBackground(new Color(0,25,0));
	    	resumeButton.setForeground(new Color(255,255,255));
	    	resumeButton.setFont(new Font("TimesRoman", Font.PLAIN, 35));
			FrameToHold.add(resumeButton,BorderLayout.NORTH);
			FrameToHold.setPreferredSize(new Dimension(400,400));
			FrameToHold.pack();
  		    }});

	    }
	    
	    
	    public void MakeFrameActive(final CallFrame FrameToUnhold)
	    {  java.awt.EventQueue.invokeLater(new Runnable() {
  			public void run() {

  		    	FrameToUnhold.setSize(new Dimension(400,600));
  		    }});

	    }
        
        public void HoldIfNotActive(){
        	Iterator<Entry<CallFrame, List<String>>> linkBridgeIterator = linkBridgeLines.entrySet().iterator();
			while (linkBridgeIterator.hasNext()) {
				Entry<CallFrame, List<String>> entry = linkBridgeIterator.next();

     			List<String> bridgeList = (List<String>) entry.getValue();
				String bridgeInitNumber=bridgeList.get(0).substring(0,bridgeList.get(0).indexOf("-"));
				String bridgeNumber=bridgeList.get(1).substring(0,bridgeList.get(1).indexOf("-"));
				for(int i=0;i<Phone.AllExtensions.size();i++)
				{
					if(!this.equals((CallFrame) entry.getKey()) && bridgeInitNumber.equals(Phone.AllExtensions.get(i)))
					{					
						MakeCallNotActive(bridgeList.get(1),bridgeList.get(0),bridgeNumber,(CallFrame) entry.getKey());
						
						MakeFrameNotActive((CallFrame) entry.getKey());
					}
					else if(!this.equals((CallFrame) entry.getKey()) && bridgeNumber.equals(Phone.AllExtensions.get(i)))
					{
						MakeCallNotActive(bridgeList.get(0),bridgeList.get(1),bridgeInitNumber,(CallFrame) entry.getKey());
						MakeFrameNotActive((CallFrame) entry.getKey());
					}
			}
		}
	}
        //выбранный вызов становится активным, а все остальные переходят в hold(park)
	public void MakeCallNotActive(String channelToHold, String phoneChannel,
			String numberToHold, CallFrame FrameToHold) {
		try {
			Socket telnet = new Socket(Phone.AsteriskIp, 5038);
			telnet.setKeepAlive(true);
			PrintWriter writer = new PrintWriter(telnet.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(
					telnet.getInputStream()));

			writer.print("Action: login\r\n");
			writer.print("UserName: " + Phone.WriteUser + "\r\n");
			writer.print("Secret: " + Phone.WriteUserPassword + "\r\n\r\n");
			writer.flush();
			writer.print("Action: Park\r\n");
			writer.print("Channel: SIP/" + channelToHold + "\r\n");
			writer.print("Channel2: SIP/" + phoneChannel + "\r\n");
			writer.print("Timeout: 1200000\r\n\r\n"); // 20 минут
			writer.print("ACTION: ParkedCalls\r\n\r\n");
			writer.flush();
			String str;
			String numberToUnhold = null, channel = null, from = null;
			do {
				str = in.readLine();
				if (str.equals("Event: ParkedCall")) {
					do {
						str = in.readLine();
						if (str.startsWith("Exten: "))
							numberToUnhold = str.substring(7);
						if (str.startsWith("Channel: "))
							channel = str.substring(13);
						if (str.startsWith("From: "))
							from = str.substring(10);

					} while (!str.startsWith("Timeout:"));
					if (channel.equals(channelToHold)
							&& from.equals(phoneChannel)) {
						// установка значений для звонка, который уходит в hold

						Hashtable<String, String> hashtable = new Hashtable<String, String>();
						hashtable.put("HoldChannel", channelToHold);
						hashtable.put("NumberToUnhold", numberToUnhold);
						parkBridgeLines.put(FrameToHold, hashtable);

						linkBridgeLines.remove(FrameToHold);
						numberToUnhold = null;
						channel = null;
						from = null;
						break;
					}
				}
			} while (!str.equals("Event: ParkedCallsComplete"));

			writer.print("Action: LOGOFF\r\n\r\n");
			writer.flush();
			telnet.close();

		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	public void HangupBridgeCalls(){
		Iterator<Entry<CallFrame, List<String>>> bridgeIterator = bridgeLines.entrySet().iterator();
		while (bridgeIterator.hasNext()) {
			Entry<CallFrame, List<String>> entry = bridgeIterator.next();
			List<String> bridgeList = (List<String>) entry.getValue();
			if(bridgeList.get(0).startsWith(Phone.MainExtension))
			{ 
				PrintWriter writer = MainFrame.TelnetWriter();

				writer.print("Action: Hangup\r\n");
				writer.print("Channel: SIP/" + bridgeList.get(0) + "\r\n\r\n");
				writer.flush();
				bridgeIterator.remove();
				removeFromList(((CallFrame) entry.getKey()));
				((CallFrame) entry.getKey()).setVisible(false);
		        ((CallFrame) entry.getKey()).dispose();
		        
			}
		
		}
	}    
	
        public void StartCallTime()
        {		  	        
	        callTimeTask = new TimerTask() {
	            @SuppressWarnings("deprecation")
				public void run() {
	          	  second++;
	          	  if(second==60) {minute++;second=0;}
	          	  if(minute==60) {hour++;minute=0;}
	          	  time.setHours(hour);
	          	  time.setMinutes(minute);
	          	  time.setSeconds(second);
	          	  label1.setText("Время разговора: "+dateFormate.format(time));
	          	  container.validate();
	        		container.repaint();
	            }
	        };
	      hour=0;minute=0;second=0;
	      callTimer.schedule(callTimeTask, 0, 1000);
        }

        public void StopCallTime()
        {
        	if(callTimeTask!=null) callTimeTask.cancel();
        	callTimer.purge();
        }

	public void Hangup(final String channelToHangup) {

		PrintWriter writer = MainFrame.TelnetWriter();
		writer.print("Action: Hangup\r\n");
		writer.print("Channel: SIP/" + channelToHangup + "\r\n\r\n");
		writer.flush();
		MakeFramesNotEnable(false);
		new CallFramesTrue().start();
	}

	public void Resume(final String numberToUnhold, final String holdNumber) {

		HangupBridgeCalls();
		
		PrintWriter writer = MainFrame.TelnetWriter();

		writer.print("Action: Originate\r\n");
		writer.print("Channel: SIP/" + Phone.MainExtension + "\r\n");
		writer.print("Exten: " + numberToUnhold + "\r\n");
		writer.print("Context: parkedcalls\r\n");
		writer.print("Priority: 1\r\n");
		writer.print("CallerId: phone<" + holdNumber + ">\r\n");
		writer.print("Async: yes\r\n\r\n");
		writer.flush();
		MakeFramesNotEnable(false);
		new CallFramesTrue().start();
		
	}
        
	public void Answer(final String channel) {

		new Thread() {
			public void run() {
					HangupBridgeCalls();

					PrintWriter writer = MainFrame.TelnetWriter();

					writer.print("Action: Redirect\r\n");
					writer.print("Channel: SIP/" + channel + "\r\n");
					writer.print("Exten: " + Phone.MainExtension + "\r\n");
					writer.print("Context: " + Phone.Context + "\r\n");
					writer.print("Priority: 1\r\n\r\n");
					writer.flush();

					bridgeLines.remove(CallFrame.this);
					List<String> bridgeList = new ArrayList<String>();
					bridgeList.add(channel);
					bridgeList.add(Phone.MainExtension);
					bridgeLines.put(CallFrame.this, bridgeList);
					if (initLinesForList.contains(channel))
						initLinesForList.remove(channel);
					initLinesForList.add(channel);
					MakeFramesNotEnable(false);
					new CallFramesTrue().start();
			}
		}.start();
	}
//возвращает список каналов, которые являются инициаторами(проблема со списком вызовов из-за park)
	public static List<String> GetInitLinesForList() {
		return initLinesForList;
	}
//возвращает список каналов bridgeLines предтендентов на звонок 
	public static Hashtable<CallFrame, List<String>> getBridgeLines() 
	{
		return bridgeLines;
	}
	
//возвращает список каналов linkBridgeLines между которыми установлен звонок
	public synchronized static Hashtable<CallFrame, List<String>> getLinkBridgeLines() 
	{
		return linkBridgeLines;
	}
//возвращает список каналов поставленных на удержание
	public synchronized static Hashtable<CallFrame, Hashtable<String, String>> getParkBridgeLines()
	{
		return parkBridgeLines;
	}
 }
	
  class CallFramesTrue extends Thread
	{ 
	   
		@Override
	    public void run()
	    {
			try {
				sleep(2200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		   CallFrame.MakeFramesNotEnable(true);

	    }
		}
