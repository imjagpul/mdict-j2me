
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DictReader {
    final static int MAX_LINE_COUNT = 150;
    private int cursorLength;
    
    private DataInputStream wordData;
    // main index just ane page is active all time aff appliacation running
	private byte [] rootIndexPage;
	// secondary index which is searched after tha root index page it is loadad dynamically
	private byte [] pageIndex;
	
    Bytes wordBuffer = new Bytes(32);
    // cursor to index page
	public int current = 0;
	private int listRowIndex;
	private int listRowCount;
    int low;
    int high;
    private int bookmark;
    public int lineIndexPos;
    public int countOfLines;
    private int cursor;
    
    private boolean listCursorVisible;

    public String allChars;
    
    
    public String leftLanguage;
    public String rightLanguage;
    public String keyMap1;
    public String keyMap2;
    private int lineIndexOffset;
    public DictReader(int rowCount) {
        String otherChars;
        int wordCount;
    	wordData = new DataInputStream(new ResourceReader("i"));
    	try {
    		wordData.mark(1000000); //ther is not seek so we will provide seek troght skip
    		wordData.skip(4); //we really do need a header size as
                Bytes.alphaCharsLowerCase = wordData.readUTF();
                Bytes.alphaCharsUpperCase = wordData.readUTF();
                Bytes.numberChars = wordData.readUTF();
                otherChars = wordData.readUTF();
                Bytes.wordChars = wordData.readUTF();
                keyMap1 = wordData.readUTF();
                keyMap2 = wordData.readUTF();
                leftLanguage = wordData.readUTF();
                rightLanguage = wordData.readUTF();
                wordCount = wordData.readInt();
	        cursorLength = wordData.readInt();
	        pageIndex = new byte[wordData.readInt()];
	        lineIndexOffset = wordData.readInt();
	        rootIndexPage = new byte[wordData.readInt()];
	        wordData.read(rootIndexPage);
			allChars = Bytes.numberChars + Bytes.wordChars + Bytes.alphaCharsUpperCase +
				Bytes.alphaCharsLowerCase + otherChars +  "\t";
			Bytes.sortStr = allChars;
                if (rowCount > wordCount) rowCount = wordCount;
 	        low =getOffset(rootIndexPage, 0);
	        high = getOffset(rootIndexPage, rootIndexPage.length -3);
			listRowCount = rowCount;
			while (rowCount > 0) {
				high = previousCursor(high -1);
				rowCount--;
			}
			bookmark = low;
			cursor = low;
			listRowIndex = 0;
			listCursorVisible = false;
			current = low;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private char byteToChar(byte b) {
    	try {
    		return allChars.charAt(b);
    	} catch (StringIndexOutOfBoundsException ex) {
    		ex.printStackTrace();
    		System.out.println(b);
    		return '?';
    	}
    	
    }
    
    public boolean listCursorVisible() {
    	return listCursorVisible;
    }
    
    public int listRowCount () {
    	return listRowCount;
    }

    private int previousCursor(int pos) throws IOException {
    	pos--;
    	seek(pos);
    	while ((wordData.read() & 0x80) == 0) {
    		pos--;
    		seek(pos);
    	}
    	return pos;
    }
    
    private int nextCursor(int pos) throws IOException {
    	pos += 3;
    	seek(pos);
    	while ((wordData.read() & 0x80) == 0) {
    		pos++;
    	}
    	return pos;
    }

    private int getOffset(byte [] buffer, int i) {
        int b2 = ((int)(buffer[i] & 0x7f)) << 14;
        int b3 = ((int)(buffer[i+1] & 0x7f)) << 7;
        int b4 = ((int)(buffer[i+2] & 0x7f));
    	return (b2 | b3 |b4);
	}
    
	
	private void seek(int pos) throws IOException {
		wordData.reset();
		wordData.skip(pos);
	}
	
	
	public int wordLenght() {
		return wordBuffer.size();
	}
	
	public char [] wordBuffer() {
		return wordBuffer.buffer;
	}

	public int fetch() {
		int c = cursor;
		int b1, b2, b3;
		wordBuffer.erase();
		try {
			seek(cursor);
			b1 = (wordData.read() & 0x7f) << 14;
			b2 = (wordData.read() & 0x7f) << 7;
			b3 = (wordData.read() & 0x7f);
			lineIndexPos = (b1 | b2 | b3);
			cursor += cursorLength;
			while (((b1 = wordData.read()) & 0x80) ==  0) {
				wordBuffer.append(byteToChar((byte) b1));
				cursor++;
			}
			b1 = (b1 & 0x7f) << 14;
			b2 = (wordData.read() & 0x7f) << 7;
			b3 = (wordData.read() & 0x7f);
			countOfLines  = (b1 | b2 | b3);
			countOfLines = (countOfLines - lineIndexPos) / 3;
			lineIndexPos += lineIndexOffset; 
		} catch (IOException e) {
			wordBuffer.erase();
			lineIndexPos = -1;
			countOfLines = 0;
		}
		return c;
	}
	
	public void setToBookmark() {
		cursor = bookmark;
	}
	
	private void setBookmark(int pos) {
		if (pos < low) pos = low;
		if (pos > high) pos = high;
		bookmark = pos;
	}
	
	public void reset() {
		setBookmark(0);
		current = bookmark;
		listRowIndex = 0;
		listCursorVisible = false;
	}
	
	public void next() {
		try {
			if (listCursorVisible) {
				if (listRowIndex < (listRowCount - 1)) {
					current = nextCursor(current);
					listRowIndex++;
				} else {
					int cur = nextCursor(bookmark);
					setBookmark(cur);
					if (cur == bookmark) {
						current = nextCursor(current);
					}
				}
			} else {
				listCursorVisible = true;
			}
			cursor = current;
			fetch();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	public void previous() {
		try {
			if (listCursorVisible) {
				if (listRowIndex > 0) {
					current = previousCursor(current);
					listRowIndex--;
				} else {
					setBookmark(previousCursor(bookmark));
					current = bookmark;
				}
			} else {
				listCursorVisible = true;
			}
			cursor = current;
			fetch();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
    private int read3BytesInt(InputStream stream) throws IOException {
        int i1 = (stream.read() & 0xff) << 16;
        int i2 = (stream.read() & 0xff) << 8;
        int i3 = (stream.read() & 0xff);

        return (i1 | i2 | i3);
    }
    
    
    private int [] getLineIndex(int indexPos, int lineCount, int [] old) throws IOException {
        int wordIndex;
        int count = 0;
        int [] res;

        seek(indexPos);
        
        if (old == null) {
            res = new int [lineCount];
            for (int i =0; i < lineCount; i++) {
                res[i] = read3BytesInt(wordData);
            }
        } else {   
            for (int i = 0; i < lineCount; i++) {
                wordIndex = read3BytesInt(wordData);
                for (int y = count;y < old.length; y++) {
                    if (old[y] == wordIndex) {
                        old[count] = wordIndex;
                        count++;
                        break;
                    }
                }
            }
            res = new int[count];
            System.arraycopy(old, 0, res, 0, count);
        }    
        return res;
    }

    public boolean posByWord(Bytes s) {
    	if (s.empty()) {
    		reset();
    		return listCursorVisible;
    	}
		try {
			if (pos(s, rootIndexPage, rootIndexPage.length - cursorLength) < 0) {
				if (current > 0) current--;
				findCursor(rootIndexPage);
			}
			int offsetHigh;
			int offsetLow;
			offsetLow = getOffset(rootIndexPage, current);
			current += 3;
			while ((rootIndexPage[current] & 0x80) == 0) {
				current++;
			}
			offsetHigh = getOffset(rootIndexPage, current);
			seek(offsetLow);
			wordData.read(pageIndex, 0, offsetHigh -offsetLow);

			listCursorVisible = (pos(s, pageIndex, offsetHigh - offsetLow) == 0);
			current += offsetLow;

			setBookmark(current);
			listRowIndex = 0;
			if (bookmark != current) {
				int c = bookmark;
				while (c < current) {
					c = nextCursor(c);
					listRowIndex++;
				}
			}
			if (listCursorVisible) {
				cursor = current;
				fetch();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return listCursorVisible;
	}

	private void findCursor(byte[] buffer) {
		while((current > 0) && ((buffer[current] & 0x80) == 0)) current--;
		
	}
	
    private int pos(Bytes b, byte [] buffer, int lhigh){
        int cmp;
        int hh = lhigh;
        int llow = 0;

        current = llow;
		updateCursor(current, buffer, hh);
        cmp = b.compareTo(wordBuffer);
        if (cmp <= 0) return cmp;
        if (b.empty()) return -1;

        current = lhigh - 1;
        findCursor(buffer);
		updateCursor(current, buffer, hh);
        cmp = b.compareTo(wordBuffer);
        if (cmp >= 0) return cmp;

        while (true ) {
        	current = ((lhigh - llow) / 2) + llow;
            findCursor(buffer);
            if (current <= llow) {
            	current = lhigh -1;
                findCursor(buffer);
            	if (current <= llow) {
            		current = lhigh;
            		updateCursor(current, buffer, hh);
                    cmp = b.compareTo(wordBuffer);
            		return cmp;
            	}	
            }
    		updateCursor(current, buffer, hh);
            cmp = b.compareTo(wordBuffer);
            if (cmp == 0)return 0;
            if (cmp < 0) {
                lhigh = current;
            } else {
                llow  = current;
            }
        }
    }	

	
	private void updateCursor(int pos, byte [] buffer, int high) {
		byte b;
		pos += 3;
		wordBuffer.erase();
		do  {
			b = buffer[pos++];
			wordBuffer.append(byteToChar(b));
			//TODO Maybe I do not need test pos < high
		} while ((pos < high) && ((buffer[pos] & 0x80) == 0));
	}


	private void readLine(int filePos, Bytes b) {
		int c;
		try {
			b.erase();
			seek(filePos);
			while ((c = wordData.read()) != 127) {
				b.append(byteToChar((byte)c));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void makeNoResult(DictTextFormat formater) {
    	formater.startTextFormat();
    	formater.appendHeader(new Bytes(Dictionary.getLocaleMessage(Dictionary.LC_NORESULT)));
    	formater.appendLine();
    	formater.stopTextFormat();
	}
	
    public void search(WordInfo [] info, Bytes b, DictTextFormat formater) {
        boolean first;
        int [] lineIndex = null;
        Bytes header = new Bytes(1024);
        Bytes resultLine = new Bytes(1024);
        Bytes firstWord = null;
        
        int i;
        
        for(int c = 0; c < b.wordCount(); c++) {
            Bytes word = b.getWord(c);
            if (c == 0)
                firstWord = word;
            if ((info[c].cursor > 0) || b.wordCount() > 1)
				try {
					lineIndex = getLineIndex(info[c].lineIndexPosition, info[c].lineCount, lineIndex);
				} catch (IOException e) {
					makeNoResult(formater);
					return;
				}
        }
        if (lineIndex == null || lineIndex.length == 0) {
        	makeNoResult(formater);
            return;
        }
        formater.startTextFormat();
        int lineCount = (lineIndex.length < MAX_LINE_COUNT) ? lineIndex.length : MAX_LINE_COUNT;
        first = true;
        for(int c = 0; c < lineCount; c++) {
            readLine(lineIndex[c], resultLine);

            i = resultLine.indexOf('\t');
            if (i != -1) {
                if (resultLine.indexOfWord(firstWord) > i) {
                   if ((header.size() != (resultLine.size() - i -1)) || (!header.regionMatches(resultLine, i + 1, resultLine.size() - i -1))) {
                       header.replace(resultLine, i + 1);
                       if (!first) {
                           formater.appendLine();
                       } else {
                           first = false;
                       }
                       formater.appendHeader(header);
                   }
                   resultLine.setEnd(i);
                   formater.appendItemLeft(resultLine);
                } else {
                   if ((header.size() != i) || !header.regionMatches(resultLine, 0, i)) {
                       header.replace(resultLine, 0, i);
                       if (!first) {
                           formater.appendLine();
                       } else {
                           first = false;
                       }    
                       formater.appendHeader(header);
                   }
                   resultLine.setStart(i + 1);
                   formater.appendItemRight(resultLine);
                }
            }
        }
        formater.stopTextFormat();
    }
}