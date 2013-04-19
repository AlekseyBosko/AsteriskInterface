package Asterisk_interface;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JDialog;
//панель, которая появляется при нажатие кнопки Перевод
@SuppressWarnings("serial")
public class Redirect extends JDialog { 
	
	private static final int WIDTH = 1700; 
	private static final int HEIGHT = 1070;
	private Font Font;
	
	//public static Redirect RedirectPanel = new Redirect();
	//телефон, который отрисовывается при переводе
	private RedirectPhone RedirPhone = new RedirectPhone();
    private static Container container;

	@SuppressWarnings("static-access")
	public Redirect() {	
	    setSize(WIDTH, HEIGHT); 
	    Toolkit kit = Toolkit.getDefaultToolkit();      
	    setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);  
	    Font = new Font("Serif", Font.PLAIN, 20);
	    container = this.getContentPane();
		setLayout(new BorderLayout());
		addRedirectPanel();
	}
	
	  //создание и прорисовка окна перевода
    public void addRedirectPanel(){
       
    	RedirPhone.CreateRedirectPhone();
    	  
    	this.add(RedirPhone,BorderLayout.EAST);
	    this.setModal(true);
	    this.setVisible(false); 

    }
    public static Container GetCont()
    {
    	return container;
    }

}
