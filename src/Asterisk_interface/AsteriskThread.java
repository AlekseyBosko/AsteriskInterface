package Asterisk_interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

class AsteriskThread extends Thread
{ 
    private String hangupInitChannel;
	private String hangupChannel;
	private boolean error;
	private String dialChannel;
	private String dialSubEvent;
	private String dialInitChannel;
	private boolean missCall; 


	@Override
    public void run()
    {
final BufferedReader in = MainFrame.TelnetReader();
final PrintWriter writer = MainFrame.TelnetWriter();
TimerTask taskForListNumbers = new TimerTask() {
    public void run() { 
String str = null;
	try {
		  synchronized(in){
			str = in.readLine();
			if (str.equals("Event: Hangup")) {
				hangupInitChannel = null;
				hangupChannel = null;          	  
		    	  error = false;
		    	  String cause = null;
		    	  missCall = false;
				do {
					str = in.readLine();
					if (str.startsWith("Channel: SIP/"))
						hangupChannel = str.substring(13);
					if (str.startsWith("Channel: Parked/SIP/"))
						error = false;
					if (str.startsWith("Cause: ")) {
						cause=str.substring(7);
			        	if (cause.equals("16")||cause.equals("0")||cause.equals("17"))
							error = false;
						else error = true;
					}
					if (str.startsWith("Cause-txt: User alerting, no answer"))
						error = true;
				} while (!str.startsWith("Cause-txt:"));
				if (error != true) {
					int iter=0;
					do {
						if(cause.equals("17")) {
							MainFrame.MakeInterfaceEnable(false);
							Boolean complete = false; 
							do {
						    str = in.readLine();
							if(str.equals("Event: Hangup")){
							    do {
									str = in.readLine();
									if (str.startsWith("Channel: SIP/")) {
										String channel = str.substring(13);
										for(int i=0;i<Phone.AllExtensions.size();i++)
											if(channel.startsWith(Phone.AllExtensions.get(i))){
												hangupInitChannel = str.substring(13);
												complete =true;
												iter=25;
												MainFrame.MakeInterfaceEnable(true);
												break;
											}
										}	
								} while (!str.startsWith("Uniqueid:"));	
							}
							} while (!complete);	
						}
						str = in.readLine();
						if(str.startsWith("Event: Hangup")){
							do {
								str = in.readLine();
								if (str.startsWith("Channel: SIP/")) {
									hangupInitChannel = str.substring(13);
									}	
								if(str.startsWith("Cause: "))
									if (str.substring(7).equals("0")) {
										missCall = true;
										break;
									}
							} while (!str.startsWith("Cause-txt:"));	
							iter=25;
						}
						if(str.startsWith("Event: New")||str.startsWith("Event: Bridge")) break;
						iter+=1;
					} while (iter<25);
					new Thread() {                                            
						public void run() {                                        
						//удаление bridge каналов и запись в список вызовов только bridge каналов
				   Hashtable<CallFrame, List<String>> bridgeLines = CallFrame.bridgeLines;
						Iterator<Entry<CallFrame, List<String>>> bridgeIterator = bridgeLines.entrySet().iterator();
						while (bridgeIterator.hasNext()) {		       
							Entry<CallFrame, List<String>> entry = bridgeIterator.next();
							List<String> bridgeList = (List<String>) entry.getValue();
							if(bridgeList.get(0).equals(hangupInitChannel)||bridgeList.get(1).equals(hangupChannel))
							{
								bridgeIterator.remove();
								CallFrame.removeFromList(((CallFrame) entry.getKey()));
								((CallFrame) entry.getKey()).setVisible(false);
						        ((CallFrame) entry.getKey()).dispose();
						        String hangupInitNumber = hangupInitChannel.substring(0, hangupInitChannel.indexOf("-"));
						        String hangupNumber = hangupChannel.substring(0,hangupChannel.indexOf("-"));
						        Phone.TypeOfCallForList(hangupInitNumber,hangupNumber,missCall);		
							}
					    }
						}
					}.start();
				}
			}
			//отлов входящих/исходящих звонков
			if (str.startsWith("Event: Dial")) {
				dialChannel=null;
				dialInitChannel=null;
				dialSubEvent = null;
				do{
					str=in.readLine();	
					if(str.startsWith("SubEvent:")) dialSubEvent = str.substring(10);
					if(str.startsWith("Channel:")) dialInitChannel = str.substring(13);
					if(str.startsWith("Destination:")) dialChannel = str.substring(17);
				}while(!str.startsWith("CallerIDNum:"));
				if(dialSubEvent.equals("Begin")){
					String dialNumber = dialChannel.substring(0,dialChannel.indexOf("-"));
					String dialInitNumber = dialInitChannel.substring(0,dialInitChannel.indexOf("-"));
				  for(int i = 0; i < Phone.AllExtensions.size(); i++)
				  { if (dialNumber.equals(Phone.AllExtensions.get(i))&&!dialNumber.equals(Phone.MainExtension))
					  {	    java.awt.EventQueue.invokeLater(new Runnable() {
							@SuppressWarnings("static-access")
							public void run() { 
								
								CallFrame CallFrame= new CallFrame();
								 List<String> bridgeList = new ArrayList<String>();
							    bridgeList.add(dialInitChannel);
								bridgeList.add(dialChannel);
								CallFrame.getBridgeLines().put(CallFrame,bridgeList);
						CallFrame.addInputCallPanel(dialInitChannel,dialChannel);
						CallFrame.setVisible(true);
							}});
					  }
				   else if ( dialInitNumber.equals(Phone.AllExtensions.get(i))&&!dialInitChannel.equals(null)&&!dialChannel.equals(null))
				  {	    java.awt.EventQueue.invokeLater(new Runnable() {
						@SuppressWarnings("static-access")
						public void run() { 
							CallFrame CallFrame= new CallFrame();
							CallFrame.HoldIfNotActive();
							List<String> bridgeList = new ArrayList<String>();
							 bridgeList.add(dialInitChannel);
						     bridgeList.add(dialChannel);
						     CallFrame.getBridgeLines().put(CallFrame,bridgeList);
							CallFrame.addOutputCallPanel( dialInitChannel,dialChannel);
					CallFrame.setVisible(true);
						}});
				}}}
			}
			//проверка вызовов, поставленных на удержание
			if(str.startsWith("Event: MusicOnHold"))
			{
			String musicOnHoldChannel = null;
			String musicOnHoldState=null;
				do{str=in.readLine();
				    //канал поставленный на удержание в сети
				if(str.startsWith("Channel:")) musicOnHoldChannel = str.substring(13);
					//start/stop moh
					if(str.startsWith("State:")) musicOnHoldState = str.substring(7);
				}while(!str.startsWith("UniqueID:"));	
				//проверка вызовов, поставленных на удержание пользователем
				Hashtable<CallFrame, Hashtable<String, String>> parkBridgeLines = CallFrame.getParkBridgeLines();
				Iterator<Entry<CallFrame, Hashtable<String, String>>> mohIterator = parkBridgeLines.entrySet().iterator();
				while (mohIterator.hasNext()) {
					Entry<CallFrame, Hashtable<String, String>> entry = mohIterator.next();
					Hashtable<String, String> hashTable = (Hashtable<String, String>) entry.getValue();
				   //когда Stop позволяем установиться звонку
				    if(musicOnHoldState.equals("Stop")&&hashTable.get("HoldChannel").equals(musicOnHoldChannel)) {
				    	mohIterator.remove();
						List<String> list = new ArrayList<String>();
			         	list.add(Phone.MainExtension);
			 		    list.add(musicOnHoldChannel);
			 		    CallFrame.getBridgeLines().put((CallFrame) entry.getKey(),list);
				    	break;
				    }
				        	
				}
				String exten=null;
				String channelToHangup = null;
				//избежание ошибок(ненужное выполнение функции hangup)
						do{
							str=in.readLine();	
							if(str.equals("Event: ParkedCallTimeOut")){

								do{str=in.readLine();
							    //номер екстеншина для снятия редиректа
								if(str.startsWith("Exten: ")) exten = str.substring(7);
								//канал запаркованного вызова
								if(str.startsWith("Channel: ")) channelToHangup = str.substring(13);
							    }
								while(!str.startsWith("CallerIDNum:"));	
								List<String> list = RedirectPhone.RedirectList();
								for(int i=0; i<list.size();i++)
								{
									if(exten.equals(list.get(i)))
									{	
										writer.print("Action: Hangup\r\n");
										writer.print("Channel: SIP/"+channelToHangup+"\r\n\r\n" );
										writer.flush();
										list.remove(i);
									}													
							     }
								break;
							}
							if(str.startsWith("Event: ParkedCall")||str.startsWith("Event: New")) break;
						}while(!str.startsWith("Bridgestate: Unlink"));
			}
			//проверка события, когда собеседник переводит звонок(в этом случае удаляем CallFrame)
			if(str.startsWith("Event: Transfer"))
			{
				String transferChannel = null;
				do{str=in.readLine();
				//канал переводящий вызов
				if(str.startsWith("Channel: ")) transferChannel = str.substring(13);
			    }
				while(!str.startsWith("Uniqueid:"));	
				Hashtable<CallFrame, List<String>> linkBridgeLines = CallFrame.getLinkBridgeLines(); 
				 Iterator<Entry<CallFrame, List<String>>> linkBridgeIterator = linkBridgeLines.entrySet().iterator();
				 String hangupInitNumber = null;
			     String hangupNumber = null;
				while (linkBridgeIterator.hasNext()) {
					Entry<CallFrame, List<String>> entry = linkBridgeIterator.next();
					List<String> bridgeList = (List<String>) entry.getValue();
					if(transferChannel.startsWith(bridgeList.get(0))||transferChannel.startsWith(bridgeList.get(1)))
					{
						linkBridgeIterator.remove();
				        CallFrame.removeFromList(((CallFrame) entry.getKey()));
					    ((CallFrame) entry.getKey()).setVisible(false);
					    ((CallFrame) entry.getKey()).dispose();	
					    hangupInitNumber = bridgeList.get(0).substring(0,bridgeList.get(0).indexOf("-"));
					    hangupNumber = bridgeList.get(1).substring(0,bridgeList.get(1).indexOf("-"));
					    List<String> initLinesForList = CallFrame.GetInitLinesForList();

						for(int i =0;i<initLinesForList.size();i++){
							if(initLinesForList.get(i).equals(bridgeList.get(1))) 
								{
								//меняем местами, ecли ставили на удержание
								hangupInitNumber = bridgeList.get(1).substring(0,bridgeList.get(1).indexOf("-"));
								hangupNumber = bridgeList.get(0).substring(0,bridgeList.get(0).indexOf("-"));
                                initLinesForList.remove(bridgeList.get(0));
								}
						}
					    Phone.TypeOfCallForList(hangupInitNumber,hangupNumber,false);
					}
				}
			}
			//проверка события, когда запаркованный вызов кладёт трубку сам
			if(str.startsWith("Event: ParkedCallGiveUp"))
			{
				String exten=null;
				String channelToHangup = null;
				do{str=in.readLine();
			    //номер екстеншина для снятия парковки
				if(str.startsWith("Exten: ")) exten = str.substring(7);
				//канал запаркованного вызова
				if(str.startsWith("Channel: ")) channelToHangup = str.substring(13);
			    }
				while(!str.startsWith("CallerIDNum:"));	
				Hashtable<CallFrame, Hashtable<String, String>> parkBridgeLines = CallFrame.getParkBridgeLines();
				Iterator<Entry<CallFrame, Hashtable<String, String>>> parkIterator = parkBridgeLines.entrySet().iterator();
				while (parkIterator.hasNext()) {
					// MainFrame.SleepThread(1000);
					Entry<CallFrame, Hashtable<String, String>> entry = parkIterator.next();
					Hashtable<String, String> hashTable = (Hashtable<String, String>) entry.getValue();
				    if(hashTable.get("NumberToUnhold").equals(exten)&&hashTable.get("HoldChannel").equals(channelToHangup)) {
				    	parkIterator.remove();
				    	CallFrame.removeFromList(((CallFrame) entry.getKey()));
					    ((CallFrame) entry.getKey()).setVisible(false);
					    ((CallFrame) entry.getKey()).dispose();
					    
						List<String> initLinesForList = CallFrame.GetInitLinesForList();
						if(initLinesForList.contains(channelToHangup))
						{
						    Phone.NumForList(channelToHangup.substring(0,channelToHangup.indexOf("-")), "Input");
							initLinesForList.remove(channelToHangup);
					    }
						else Phone.NumForList(channelToHangup.substring(0,channelToHangup.indexOf("-")), "Output");
				       }
				    }

				
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
						bridgeInitChannel = str.substring(14);
						bridgeInitNumber = str.substring(14,str.indexOf("-"));
					}
					if(str.startsWith("Channel2:")) {
						bridgeChannel = str.substring(14);
						bridgeNumber = str.substring(14,str.indexOf("-"));
					}
				}while(!str.startsWith("Uniqueid1:"));	
				//проверка установленных вызовов
				if (bridgeState.equals("Link")) {
					Hashtable<CallFrame, List<String>> bridgeLines = CallFrame.getBridgeLines();
					Iterator<Entry<CallFrame, List<String>>> bridgeIterator = bridgeLines.entrySet().iterator();
					while (bridgeIterator.hasNext()) {
						Entry<CallFrame, List<String>> entry = bridgeIterator.next();
						List<String> bridgeList = (List<String>) entry.getValue();
						if(bridgeInitChannel.startsWith(bridgeList.get(0))&&bridgeChannel.startsWith(bridgeList.get(1)))
						{
							for (int i = 0; i < Phone.AllExtensions.size(); i++) {
								if (bridgeInitNumber.equals(Phone.AllExtensions.get(i)))
								{   
									((CallFrame) entry.getKey()).addDialPanel(bridgeChannel,bridgeInitChannel);
								}
								else if (bridgeNumber.equals(Phone.AllExtensions.get(i)))
								{    
									((CallFrame) entry.getKey()).addDialPanel(bridgeInitChannel,bridgeChannel);
								}
							}
							bridgeIterator.remove();
							List<String> list = new ArrayList<String>();
				         	list.add(bridgeInitChannel);
				 		    list.add(bridgeChannel);
				 		   ((CallFrame) entry.getKey()).HoldIfNotActive();

							CallFrame.getLinkBridgeLines().put((CallFrame) entry.getKey(),list);
							
						}
					}
				}
				//проверка законченных вызовов
				else if (bridgeState.equals("Unlink")) {
					Hashtable<CallFrame, List<String>> linkBridgeLines = CallFrame.getLinkBridgeLines(); 
					 Iterator<Entry<CallFrame, List<String>>> linkBridgeIterator = linkBridgeLines.entrySet().iterator();
					 String hangupInitNumber = null;
				     String hangupNumber = null;
					while (linkBridgeIterator.hasNext()) {
						Entry<CallFrame, List<String>> entry = linkBridgeIterator.next();
						List<String> bridgeList = (List<String>) entry.getValue();
						if(bridgeInitChannel.startsWith(bridgeList.get(0))&&bridgeChannel.startsWith(bridgeList.get(1)))
						{
							linkBridgeIterator.remove();
					        CallFrame.removeFromList(((CallFrame) entry.getKey()));
						    ((CallFrame) entry.getKey()).setVisible(false);
						    ((CallFrame) entry.getKey()).dispose();	
						    hangupInitNumber = bridgeInitChannel.substring(0,bridgeInitChannel.indexOf("-"));
						    hangupNumber = bridgeChannel.substring(0,bridgeChannel.indexOf("-"));
						    List<String> initLinesForList = CallFrame.GetInitLinesForList();
							for(int i =0;i<initLinesForList.size();i++){
								if(initLinesForList.get(i).equals(bridgeChannel)) 
									{
									//меняем местами, ecли ставили на удержание
									hangupInitNumber = bridgeChannel.substring(0,bridgeChannel.indexOf("-"));
									hangupNumber = bridgeInitChannel.substring(0,bridgeInitChannel.indexOf("-"));
                                    initLinesForList.remove(bridgeChannel);
									}
							}
						    Phone.TypeOfCallForList(hangupInitNumber,hangupNumber,false);
						}
					}
				}
			}}
	} catch (IOException e) {
		e.printStackTrace();
	}
    }};
    Timer timerList = new Timer();
    timerList.schedule(taskForListNumbers, 0, 15);
    }
}