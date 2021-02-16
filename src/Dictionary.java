import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class Dictionary extends MIDlet{
	public static final int LC_WHISPERRER = 6;
	public static final int LC_SEARCHING = 5;
	public static final int LC_NEW = 4;
	public static final int LC_EXIT = 3;
	public static final int LC_HELP = 2;
	public static final int LC_BACK = 1;
	public static final int LC_TRANSLATE = 0;
	public static final int LC_NORESULT = 7;
	public static String locale;
    private static String [] messages;

    public static String getLocaleMessage(int c) {
		if (c >= messages.length) {
			return "NULL";
		} else {
			return messages[c];
		}
	}
    
    private DictScreen dictScreen;
    
    public Dictionary() throws IOException {
    	String [] tmpMessages = new String[32];
    	locale = System.getProperty("microedition.locale").substring(0, 2);
    	InputStream is = this.getClass().getResourceAsStream(locale + "locale");
    	InputStreamReader r;
    	try {
        	r = new InputStreamReader(is, "UTF-8");
    	} catch (Exception ex) {
    		locale = "en";
            ex.printStackTrace();
        	is = this.getClass().getResourceAsStream("enlocale");
        	r = new InputStreamReader(is, "UTF-8");
        }
    	int ch;
    	int mesId = 0;
    	StringBuffer sb = new StringBuffer();
    	do {
    		ch =  r.read();
    		if ((char)ch == '\n') {
    			tmpMessages[mesId++] = sb.toString();
    			sb.delete(0, sb.length());
    		} else {
    			sb.append((char) ch);
    		}
    	} while(ch > 0);
    	messages = new String[mesId];
    	for(int i = 0; i < mesId; i++) {
    		messages[i] = tmpMessages[i];
    	}
        dictScreen = new DictScreen(this);
    }
    
    public void exit() {
    	dictScreen = null;
    	messages = null;
        destroyApp(true);
        notifyDestroyed();
   }
    
    protected void startApp() {
        Display.getDisplay(this).setCurrent(dictScreen);
    }

    protected void destroyApp(boolean unconditional) {
        Display.getDisplay(this).setCurrent(null);
    }
    
    protected void pauseApp() {
    }
}
