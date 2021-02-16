import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;



/** 
 * store info about each word in searchnig text
 */
class WordInfo {
	int cursor;
	int lineIndexPosition;
	short lineCount;
	WordInfo (int c, int i, int lc) {
		cursor = c;
		lineIndexPosition = i;
		lineCount = (short)lc;
		
	}
}

public class DictScreen extends Canvas implements CommandListener, DictTextFormat {
	Bytes leftTextImage;
	Bytes rightTextImage;

	static final int sliderRes = 6;
	static final int frameWidth = 4;
	static final int lineWidth = 1;
	static final int textGap = 3;

	static final int colorHeader = 0x00f4a479;
	static final int colorBlack = 0x00;
	static final int colorFrame = 0x00851f1f;
	static final int colorErrorHighlight = 0xdead8c; 
	static final int colorListEditText = 0x00;
	static final int colorResultBck = 0x00cdb77d;
	static final int colorBck = 0x00e6e5c4;
	static final int colorListBckHighlisted = 0x00ffffff;
	static final int cornerAngle = 5;

	
	// --------------------------------------------------
	static int MAX_WORD_COUNT = 32;
	Dictionary app;

	Command cmdSearch = new Command(Dictionary.getLocaleMessage(Dictionary.LC_TRANSLATE), Command.ITEM, 1);
	Command cmdBack = new Command(Dictionary.getLocaleMessage(Dictionary.LC_BACK), Command.BACK, 1);
	Command cmdExit = new Command(Dictionary.getLocaleMessage(Dictionary.LC_EXIT), Command.EXIT, 1);
	Command cmdNew = new Command(Dictionary.getLocaleMessage(Dictionary.LC_NEW), Command.ITEM, 1);
	Command cmdHelp = new Command(Dictionary.getLocaleMessage(Dictionary.LC_HELP), Command.HELP, 1);
	boolean cmdExitVisible = false;
	boolean cmdBackVisible = false;
	boolean cmdNewVisible = false;
	boolean cmdSearchVisible = false;
	boolean cmdHelpVisible = false;

	DictReader dictReader;
	Vector historyRecords = new Vector();
	Bytes currentSearch = new Bytes(32);

	int state = EDITING;
	static final int EDITING = 1;
	static final int BROWSING = 2;
	static final int SEARCHING = 3;
	static final int HELP = 4;

	Bytes text = new Bytes(32);
	WordInfo [] textLookups = new WordInfo[MAX_WORD_COUNT] ;
	String [] keyMap;
	Timer blinkTimer;
    boolean cursorBlinkVisible = false;
    int searchTimerCycle = -1;
    int textTimerCycle = -1;
	
	Font editFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD,Font.SIZE_LARGE);
	Font listFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
	Font textFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
	Font headerFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
	Font smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
	Font editHelperFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
	
	View resultView;
	int height;
	int width;
	int editFontHeight;
	int listFontHeight;
	int smallFontHeight;
	int editHelperFontHeight;
	int editHeight;
	int resultViewTop;
	int textGap2 = textGap * 2;
	int textGap3 = textGap * 3;
	int textGap4 = textGap * 4;
	int listWidth;
	
	DictScreen(Dictionary p) {
		app = p;
		for (int i = 0; i < MAX_WORD_COUNT; i ++) {
			textLookups[i] = new WordInfo(-1, 0, 0);
		}

		width = getWidth();
		height = getHeight();
		editFontHeight = editFont.getHeight();
		listFontHeight = listFont.getHeight();
		smallFontHeight = smallFont.getHeight();
		editHelperFontHeight = smallFont.getHeight();
		editHeight = 6 + editFontHeight;
		resultViewTop = editHeight + lineWidth + frameWidth;
		int listRowCount = (height - (resultViewTop + frameWidth + smallFontHeight)) / listFontHeight;
		resultView = new View(textGap2, resultViewTop, width - sliderRes - textGap3, height - resultViewTop);
		resultView.linkIsUnderlined = false;
		resultView.xIndent = textGap;
		listWidth = listFont.stringWidth("wwwwwwwwwwww") + textGap2 + (frameWidth * 2);
		dictReader = new DictReader(listRowCount);
		keyMap = strToKeyMap(dictReader.keyMap1);
		leftTextImage = new Bytes("/" + dictReader.leftLanguage + ".png");
		rightTextImage = new Bytes("/" + dictReader.rightLanguage + ".png");
		updateCommands();
		setCommandListener(this);
		blinkTimer = new Timer();
   		blinkTimer.schedule(new CursorBlinkTask(), 100, 500);
	}

	void setCmdExitVisible (boolean b) {
		if (cmdExitVisible != b) {
			if (!cmdExitVisible) {
				addCommand(cmdExit);
			} else {
				removeCommand(cmdExit);
			}
			cmdExitVisible = b;
		}
	}
	void setCmdNewVisible (boolean b) {
		if (cmdNewVisible != b) {
			if (!cmdNewVisible) {
				addCommand(cmdNew);
			} else {
				removeCommand(cmdNew);
			}
			cmdNewVisible = b;
		}
	}
	void setCmdSearchVisible (boolean b) {
		if (cmdSearchVisible != b) {
			if (!cmdSearchVisible) {
				addCommand(cmdSearch);
			} else {
				removeCommand(cmdSearch);
			}
			cmdSearchVisible = b;
		}
	}
	void setCmdHelpVisible (boolean b) {
		if (cmdHelpVisible != b) {
			if (!cmdHelpVisible) {
				addCommand(cmdHelp);
			} else {
				removeCommand(cmdHelp);
			}
			cmdHelpVisible = b;
		}
	}
	void setCmdBackVisible (boolean b) {
		if (cmdBackVisible != b) {
			if (!cmdBackVisible) {
				addCommand(cmdBack);
			} else {
				removeCommand(cmdBack);
			}
			cmdBackVisible = b;
		}
	}
	
	private void updateCommands () {
		setCmdExitVisible((state != SEARCHING));
		setCmdSearchVisible((state != SEARCHING) && (!text.empty())&& (state != HELP));
		setCmdNewVisible((state != SEARCHING)  && (state != HELP) && (!text.empty()));
		setCmdBackVisible((state != SEARCHING));
		setCmdHelpVisible((state != SEARCHING) && (state != HELP));
	}
	
	boolean curVisible() {
		return cursorBlinkVisible;
	}
	

    private String lastKeyChars;
    private int keyCharIndex;

    
    String [] strToKeyMap(String s) {
    	String [] keyM = new String [10];
    	StringBuffer sb = new StringBuffer(10);
    	int i = 0;
    	int len = s.length();
    	char ch;
    	int pos = 0;
    	while (i < len) {
    		ch = s.charAt(i);
    		if (ch == '\t') {
    			keyM[pos++] = sb.toString();
    			sb.delete(0, sb.length());
    		} else {
    			sb.append(ch);
    		}
    		i++;
    	}
		keyM[pos++] = sb.toString();
    	return keyM;
    }
    
    
    class CursorBlinkTask extends TimerTask {
    	public void run() {
    		cursorBlinkVisible = !cursorBlinkVisible; 
    		if (state == EDITING) repaint();

    		if (searchTimerCycle >= 0) {
        		if (searchTimerCycle > 10) {
        			searchTimerCycle = -1;
        			boolean b = true;
        			for(int i =0; i < text.wordCount();i++)	b &= (textLookups[i].cursor > 0);
        			if (b) search(true);
        		} else {
        			searchTimerCycle ++;
        		}
        	}
    		
    		if (textTimerCycle >= 0) {
    			if (textTimerCycle > 1) {
    				textTimerCycle = -1;
    	            lastKeyChars = null;
    	            searchTimerCycle = 0;
    	            repaint();
    				
    			} else {
    				textTimerCycle++;
    			}
    		}
    	}

    }
    
    void startSearchTimer() {
    	searchTimerCycle = 0;
    }
    
    
    void stopSearchTimer() {
    	searchTimerCycle = -1;
    }
    

    private void startTextTimer() {
    	stopSearchTimer();
    	textTimerCycle = 0;
    }

    private void stopTextTimer() {
    	textTimerCycle = -1;
    }

    private void listUpdateEdit() {
    	int word  = text.wordCount() -1;
        int p = text.wordPos(word);
        text.delete(text.tokenLen(p));
        text.append(dictReader.wordBuffer);
        word  = text.wordCount() -1;
    	textLookups[word].cursor = dictReader.current;
		textLookups[word].lineIndexPosition = dictReader.lineIndexPos;
		textLookups[word].lineCount = (short) dictReader.countOfLines;
    }

    void updateAllLookups() {
    	for (int w = 0; w < text.wordCount(); w++) {
    		lookupList(w);
    	}
    }
    
    private void lookupList(int word) {
    	if (word == -2) word = text.wordCount() -1;
    	word = Math.max(word, 0);
    	dictReader.posByWord(text.getWord(word));
        if (dictReader.listCursorVisible()) {
        	textLookups[word].cursor = dictReader.current;
        	textLookups[word].lineIndexPosition = dictReader.lineIndexPos;
        	textLookups[word].lineCount = (short) dictReader.countOfLines;
        } else {
        	textLookups[word].cursor = -1;
        }	
    }
    
    
    private boolean editKeyPressed(int key) {
        String ch;
        if (key > KEY_NUM9 || key < KEY_NUM0) return false;
        state = EDITING;
        cursorBlinkVisible = true;
        ch = keyMap[key - KEY_NUM0];
        if ((lastKeyChars == null) || (lastKeyChars != ch)) {
            stopTextTimer();
            keyCharIndex = 0;
            lastKeyChars = ch;
        } else {
            if (keyCharIndex >= (lastKeyChars.length() - 1)) keyCharIndex = 0;
            else keyCharIndex++;
        }

        if (textTimerCycle== -1) text.append((char) 0);
        char sch = lastKeyChars.charAt(keyCharIndex);
        text.setByte(text.size() - 1, sch);
        if (!Bytes.isWordChar(sch)) {
        	int word = text.wordCount();
        	lookupList(word - 2);
        }
        lookupList(-2);
        startTextTimer();
        return true;
    }

    void textDelete() {
        cursorBlinkVisible = true;
    	state = EDITING;
        stopTextTimer();
        text.delete(1);
        lastKeyChars = null;
        lookupList(-2);
        startSearchTimer();
    }

    boolean resultViewKeyPressed(int gameKey) {
		if (gameKey == DOWN) {
			resultView.pageDown();
		}else if (gameKey == UP) {
			resultView.pageUp();
		}else if (gameKey == LEFT) {
			resultView.prevHyperlink();
		}else if (gameKey == RIGHT) {
			resultView.nextHyperlink(true);
		} else {
			return false;
		}
		return true;
    }

    public void commandAction(Command cmd, Displayable display) {
		if (cmdSearch == cmd) {
                        if (state == BROWSING) {
				text.replace(resultView.getHyperlink());
				text.toLowerCase();
				updateAllLookups();
				search(true);
                        } else if (state == EDITING) {
				search(true);
                        }
//			keyPressed(getKeyCode(FIRE));
		} else if (cmdBack == cmd) {
			back();
		} else if (cmdNew == cmd) {
			keyPressed(KEY_STAR);
		} else if (cmdHelp == cmd) {
			help();
		} else if (cmdExit == cmd) {
			app.exit();
		}
		
	}
    
	protected final void keyPressed(int key) {
		int gKey = getGameAction(key);
		
		if (state == BROWSING ) {
			if (editKeyPressed(key)) {

			}else if (key == KEY_STAR) {
				clear();
			} else 	if (key == -8) {
				textDelete();
			} else if (key == KEY_POUND) {
				state = EDITING;
			} else if (resultViewKeyPressed(gKey)) {

			} else if (gKey == FIRE) {
				text.replace(resultView.getHyperlink());
				text.toLowerCase();
				updateAllLookups();
				search(true);
			}

		} else if (state == EDITING) {
			if (editKeyPressed(key)) {

			}else if (key == KEY_STAR) {
				clear();
			} else if (key == -8 || gKey == LEFT) {
				textDelete();
			} else if (gKey == DOWN) {
				dictReader.next();
				listUpdateEdit();
				startSearchTimer();
			} else if (gKey == UP) {
				dictReader.previous();
				listUpdateEdit();
				startSearchTimer();
			} else if (gKey == FIRE) {
				search(true);
			}
			
		} else 	if (state == HELP) {
			resultViewKeyPressed(gKey);
		} else {
			return;
		}
		updateCommands ();
		repaint();
    }

    protected void keyRepeated(int key) {
    	this.keyPressed(key);
    }
    
	public void search(boolean writeHistory) {
    	stopTextTimer();
    	stopSearchTimer();
    	text.trim();
    	if (text.empty()) return;
    	
 		if (!currentSearch.equals(text)) {
			try {
				state = SEARCHING;
				updateCommands ();
				repaint();
				serviceRepaints();
				dictReader.search(textLookups, text, this);
				currentSearch.replace(text, 0);
				if (writeHistory)
					historyRecords.addElement(text.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		state = BROWSING;
		updateCommands ();
		repaint();
	}

	public void clearAll() {
		currentSearch.erase();
		resultView.items.removeAllElements();
		clear();
		updateCommands ();
		repaint();
	}
	
	public void clear() {
		text.erase();
		for(int i=0; i < MAX_WORD_COUNT;i++) textLookups[i].cursor = -1;
		dictReader.reset();
        state = EDITING;
	}
	
	public void help() {
		state = HELP;
		text.replace(Dictionary.getLocaleMessage(Dictionary.LC_HELP));
		resultView.setText(Bytes.readFromResource(Dictionary.locale + "help.html"));
		updateCommands();
		repaint();
	}
	
	public void back() {
		if (state == EDITING && (!currentSearch.empty())) {
			text.replace(currentSearch, 0);
			updateAllLookups();
			state = BROWSING;
			updateCommands();
			repaint();
		} else if (state == BROWSING) {
			int last = (historyRecords.size());
			if (last > 0) historyRecords.removeElementAt(last -1);
			last--;
			if (last > 0) {
				text.replace((String) historyRecords.elementAt(last -1));
				updateAllLookups();
				search(false);
			} else {
				clearAll();
			}
		} else if (state == HELP) {
			if (!currentSearch.empty()) {
				text.replace(currentSearch, 0);
				currentSearch.erase();
				updateAllLookups();
				search(false);
			} else {
				clearAll();
			}
		}		
	}
	

	Bytes textBuf = new Bytes(32);
	
	void appendText(Bytes text) {
		int i = 0;
		int max = text.size();
		boolean isWhitespace = Bytes.isWordChar(text.charAt(0));
		textBuf.erase();
		while (i < max) {
			char ch = text.charAt(i);
			if (isWhitespace != Bytes.isWordChar(ch)) {
				if (isWhitespace) {
					resultView.appendHyperlink(textBuf, null);
					isWhitespace = false;
				} else {
					resultView.appendText(textBuf);
					isWhitespace = true;
				}
				textBuf.erase();
			}
			textBuf.append(ch);
			i++;
		}
		if (isWhitespace) {
			resultView.appendHyperlink(textBuf, null);
		} else {
			resultView.appendText(textBuf);
		}
	}
	
	public void appendHeader(Bytes text) {
		resultView.setLayoutFont(headerFont);
		appendText(text);
	}

	public void appendItemLeft(Bytes text) {
		resultView.setLayoutFont(textFont);
		resultView.layoutDd(true);
		resultView.appendImage(leftTextImage);
		appendText(text);
		resultView.layoutDd(false);
	}
	public void appendItemRight(Bytes text) {
		resultView.setLayoutFont(textFont);
		resultView.layoutDd(true);
		resultView.appendImage(rightTextImage);
		appendText(text);
		resultView.layoutDd(false);
	}

	public void startTextFormat() {
		resultView.preparePage();
	}

	public void stopTextFormat() {
		resultView.commitPage();
	}

	public void appendLine() {
		resultView.appendHR();
	}
	
	protected void paint(Graphics g) {
		HtmlHRulerItem.color = colorFrame;
		HtmlTextItem.colorBckHighlight = colorListBckHighlisted;
		HtmlTextItem.colorTextHighlight = colorListEditText; 
		
		int x = 0;
		int y = 0;
		int yres = editHeight;
		int xres = width;
		int tokenLeftPos = 0;
		int tokenWidth;
		int textWidth = editFont.charsWidth(text.buffer, 0, text.size());

		
		g.setColor(colorHeader);
		g.fillRect(x, y, xres, yres);
		xres -= textGap2; yres -= 4; x += textGap; y += 2;
		if (state == EDITING) {
			g.setColor(colorBck);
			g.fillRoundRect(x, y, xres, yres, cornerAngle, cornerAngle);
		}
		g.setClip(x + textGap, y, xres - textGap, yres);
		x += textGap;
		y++;
		
		if (textWidth > (xres - textGap3))
			x -= textWidth - (xres - textGap3);
		if (state == EDITING) {
			g.setColor(colorErrorHighlight);
			for (int w = 0; w < text.wordCount(); w++) {
				if (textLookups[w].cursor == -1) {
					int wordPos = text.wordPos(w);
					if (wordPos > 0) tokenLeftPos = editFont.charsWidth(text.buffer, 0, wordPos);
					tokenWidth = text.tokenLen(wordPos);
					tokenWidth = editFont.charsWidth(text.buffer, wordPos, tokenWidth);
					g.fillRoundRect(tokenLeftPos + x, y, tokenWidth, editFontHeight, cornerAngle, cornerAngle);
				}
			}
		}
		
		g.setColor(colorListEditText);
		g.setFont(editFont);
		g.drawChars(text.buffer, 0, text.size(), x , y, 0);
		x += textWidth + textGap;
		int textEnd = x;
		if (curVisible() && (state == EDITING)) {
			g.setColor(colorFrame);
//			g.setStrokeStyle(Graphics.DOTTED);
			g.drawRect(x, y + 1, 1, editFont.getBaselinePosition());
			g.setStrokeStyle(Graphics.SOLID);
		}

		g.setClip(0, 0, width, height);
		
		y = editHeight;
		g.setColor(colorBlack);
		g.fillRect(0, y, width, lineWidth);
		y += lineWidth;
		g.setColor(colorFrame);
		g.fillRect(0, y, width, frameWidth);
		y += frameWidth;
		g.setColor(colorResultBck);
			yres = height - y;
		g.fillRect(0, y, width, yres);

		if (state == SEARCHING) {
			g.setColor(colorFrame);
			textWidth = editFont.stringWidth(Dictionary.getLocaleMessage(Dictionary.LC_SEARCHING)) + textGap4;
			x = (width - textWidth) / 2;
			y = ((yres - (editFontHeight + textGap2)) / 2) + resultViewTop;
			g.fillRoundRect(x, y, textWidth, editFontHeight + textGap2, cornerAngle, cornerAngle);
			
			g.setColor(colorResultBck);
			g.drawString(Dictionary.getLocaleMessage(Dictionary.LC_SEARCHING), width / 2,	 y + textGap, Graphics.TOP | Graphics.HCENTER);
			
		} else {
			int sliderHeight;
			int sliderTop;
			resultView.paint(g);
			if (resultView.maxHeight > resultView.height) {
				sliderHeight = (resultView.height * resultView.height) / resultView.maxHeight;
				sliderTop =  ((resultView.height) * resultView.currentPosY) / resultView.maxHeight;
				sliderTop +=resultViewTop;
				sliderHeight++;
			} else {
				sliderHeight = resultView.height;
				sliderTop = resultViewTop;
			}
			g.setColor(colorBck);
			x = width - sliderRes;
			yres = height - resultViewTop;
			g.fillRect(x, resultViewTop, sliderRes, yres);
			g.setColor(colorFrame);
			g.fillRect(x, sliderTop, sliderRes, sliderHeight);
			g.setColor(colorBlack);
			g.drawRect(x, sliderTop, sliderRes - 1, sliderHeight -1);
		}
		
		if (state == EDITING) {
			
			int listHeight = listFontHeight * dictReader.listRowCount();
			xres = listWidth;
			x = width - xres - sliderRes - textGap2;
			if (x < textGap2) {
				x = textGap2;
			}
			xres = width - sliderRes - textGap2 -x;
			y = editHeight + lineWidth;
			yres = listHeight + smallFontHeight + (frameWidth / 2);
			g.setColor(colorFrame);
			g.fillRoundRect(x, y, xres, yres, cornerAngle, cornerAngle);
			g.setColor(colorBck);
			g.setFont(smallFont);
			g.drawString(Dictionary.getLocaleMessage(Dictionary.LC_WHISPERRER),
					xres + x - frameWidth, listHeight + y, Graphics.RIGHT | Graphics.TOP);
			x += frameWidth;
			xres -= (frameWidth + frameWidth);
			g.fillRect(x, y, xres, listHeight);
			x+=textGap;
			xres -= textGap2;
			g.setClip(x, y, xres, listHeight);
			g.setFont(listFont);
			char [] buffer = dictReader.wordBuffer();
			dictReader.setToBookmark();
			for (int i = 0; i < dictReader.listRowCount(); i++) {
				if (dictReader.fetch() == dictReader.current & dictReader.listCursorVisible()) {
					g.setColor(colorListBckHighlisted);
					g.fillRoundRect(x, y, xres, listFontHeight, cornerAngle, cornerAngle);
				}
				g.setColor(colorListEditText);

				g.drawChars(buffer, 0, dictReader.wordLenght(), x, y, 0);
				y += listFontHeight;
				g.setClip(0, 0, width, height);
			}

			if (lastKeyChars != null) {
				g.setFont(editHelperFont);
				y = textGap;
				yres = editHelperFontHeight;
				xres = editHelperFont.substringWidth(lastKeyChars, 0, lastKeyChars.length());
				x= width - xres - textGap;
				if (textEnd > x) return;
				g.setColor(colorFrame);
				g.fillRoundRect(x-textGap, y - textGap, xres + (textGap2), yres + (textGap2), cornerAngle, cornerAngle);
				g.setColor(colorBck);
				g.fillRect(x, y, xres, yres);
				xres = editHelperFont.substringWidth(lastKeyChars, 0, keyCharIndex);
				g.setColor(colorListBckHighlisted);
				g.fillRect(x + xres, y, editHelperFont.charWidth(lastKeyChars.charAt(keyCharIndex)), yres);
				g.setColor(colorListEditText);
				g.drawSubstring(lastKeyChars, 0, lastKeyChars.length(), x, y, 0);
			}
		}
		
	}

}
