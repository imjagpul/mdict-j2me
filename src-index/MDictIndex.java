import java.io.*;
import java.lang.RuntimeException;
import java.util.*;

// just temporary object for colecting of all the words in the input file and their
// references to each lines (Mainly that is the whole pion of the simple index)
// we can easilly sort it as well + deromoving duplicated records
class WordItem {
    String word;
    int line;
    public WordItem(String w, int l) {
        word = w;
        line = l;
    }
}
// object for sorting purpoases we want to sort by line numbers as well as
// data can containt the duplicate lines for each word will be ease to remove it
class WordItemComparator implements Comparator {
    
    public int compare(Object o1, Object o2) throws ClassCastException {
        WordItem i1 = (WordItem) o1;
        WordItem i2 = (WordItem) o2;
        int r = Util.compareStr(i1.word, i2.word);
        if (r != 0) {
             return r;
        } else 
             return i1.line - i2.line;
    }
}


class LineComparator implements Comparator {
    String forWord;
    int onWhichSide;
    Line [] lines;
    
    public LineComparator(String w, int i, Line [] l) {
        forWord = w;
        onWhichSide = i;
        lines = l;
    }
    
    private int wordPos(String s) {
    	int len = forWord.length();
    	int i;
    	boolean f = false;
        for (i = 0; i <= (s.length() - len);i++) {
        	boolean lowWhiteSpace = ((i == 0 )|| Util.isWhiteSpace(s.charAt(i - 1)));
        	boolean highWhiteSpace = (((i + len) == (s.length())) || Util.isWhiteSpace(s.charAt(i + len)));
        	if (lowWhiteSpace && highWhiteSpace && s.regionMatches(i, forWord, 0, len)) {
        		f = true;
        		break;
        	}
        }
        
        if (!f) {
        	//System.out.printf("Excluded ----------- For word '%s'  for line '%s'\n", forWord, s);
        	return Integer.MAX_VALUE;
        } else {
//        	System.out.printf("Included For word '%s'  for line '%s'\n", forWord, s);
        }
        int c = 0;
        boolean w = true;
        for(int x = 0; x < i; x++) {
            char ch = s.charAt(x);
            if (!Util.isWhiteSpace(ch)) {
                if (w) c++;
                w = false;
            } else {
                w = true;
            }
        }
        return c;
    }

    private void split(String s) {
        int i = s.indexOf('\t');
        if (i == -1)
            throw new RuntimeException("Incorect line");
        leftSide = s.substring(0, i);
        int e = s.indexOf('\n');
        if (e == -1)
            throw new RuntimeException("Incorect line");
        rightSide = s.substring(i + 1, e);
    }
    
    private String leftSide;
    private String rightSide;
    
    private void getLineInfo(String line) {
        split(line);
        if (wordPos(leftSide) <= wordPos(rightSide)) {
            key = leftSide;
            translation = rightSide;
        } else {
            key = rightSide;
            translation = leftSide;
        }
        wordPos = wordPos(key);
        int i = key.indexOf(forWord);
        before = key.substring(0, i);
        after = key.substring(i);
    }
    
    private String key;
    private String translation;
    private String before;
    private String after;
    private int wordPos;
    
    
    public int compare(Object o1, Object o2) {
        Line l1 = lines[((Integer) o1).intValue()];
        Line l2 = lines[((Integer) o2).intValue()];
        //first we
        String ls1 = Util.toLowerCase(l1.line);
        getLineInfo(ls1);
        int wordPos1 = wordPos;
        String before1 = before;
        String after1 = after;
        String translation1 = translation;

        String ls2 = Util.toLowerCase(l2.line);
        getLineInfo(ls2);
        int wordPos2 = wordPos;
        String before2 = before;
        String after2 = after;
        String translation2 = translation;
        
        int r;
        if ((r = (wordPos1 - wordPos2)) != 0) {
            return r; 
        } else if ((r = Util.compareStr(before1,before2 )) != 0) {
            return r;
        } else if ((r = Util.compareStr(after1, after2)) != 0) {
            return r;
        } else {
            return Util.compareStr(translation1, translation2);
        }
    }
}

// the Main object of the index definition of the record one word containt refernece
// to dictionary lines
// Array of these objects is making index by itself
class WordIndexItem {
    String word;
    int [] links;

    public WordIndexItem(String w, Vector l) {
        word = w;
        links = new int [l.size()];
        for(int i = 0; i < l.size();i++) {
            Integer ii = (Integer) l.elementAt(i);
            links[i] = ii.intValue();
        }
    }
    
    public void sortLinks(Line [] lines) {
        Integer [] ar = new Integer[links.length];
        for (int i= 0;i < ar.length; i++)
            ar[i] = new Integer(links[i]);
        LineComparator lineComparator = new LineComparator(word, 1, lines);
		Arrays.sort(ar, lineComparator);
        for (int i= 0;i < ar.length; i++)
            links[i] = ar[i].intValue();
    }
    
    public void updateLinks(Line [] lineList) {
        for(int i = 0; i < links.length; i++) {
            links[i] = lineList[links[i]].offset;
        }
    }
    
    public void writeLines(DataOutputStream s) throws IOException {
        for(int i=0; i < links.length; i++) {
            Util.write3BytesInt(s, links[i]);
        }
    	
    }
    
    public void write(DataOutputStream s) throws IOException, Exception {
        s.write(Util.strToBytes(word));
        writeLines(s);
    }
    
}

// Data line of the dictionary defacto data page just temorary objeck hold the offset
// of the line in result file,
class Line {
    String line;
    int offset;
    public Line(String s, int o) {
        line = s;
        offset = o;
    }
    
    public void write(DataOutputStream s) throws IOException, Exception {
        s.write(Util.strToBytes(line));
    }
}


class Util {
    public static int KEY_RETURN = 127;
  

    public static final String unescape(String s) {
    	StringBuffer sb = new StringBuffer();
    	int i = 0, len = s.length();
    	String d;
    	int ch;
    	while (i < len) {
    		if (s.charAt(i) == '\\') {
        		i++;
        		if (i >= len) {
        			sb.append('\\');
        			break;
        		}
        		if (s.charAt(i) == 'u') {
        			i++;
        			if (i+4 > len) {
        				System.out.printf("Can not unescape this line\n%s\n", s);
        				throw new RuntimeException("Escape Error");
        			}
        			d = s.substring(i, i + 4);
        			try {
        				ch = Integer.parseInt(d, 16);
        			} catch (NumberFormatException e) {
        				System.out.printf("Can not unescape this line\n%s\n", s);
        				throw new RuntimeException("Escape Error");
        			}
        			i += 4;
        			sb.append((char) ch);
         		} else {
        			sb.append('\\');
        			sb.append(s.charAt(i));
        			i++;
        		}
    		} else {
    			sb.append(s.charAt(i));
        		i++;
    		}
    	}
    	
    	
    	return sb.toString();
    }
    
    /**
     *	exclude chars and convert them as excludeChars table say
     *
     */
    public static final String excludeChars(String s) {
        StringBuffer sb = new StringBuffer();
        char ch;
        int index;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            index = MDictIndex.excludeChars.indexOf(ch);
            if (index >=0) {
                ch = MDictIndex.excludeCharsTo.charAt(index);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public static String toLowerCase(String s) {
    	StringBuffer sb = new StringBuffer();
        char ch;
        int index;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            index = Header.alphaCharsUpperCase.indexOf(ch);
            if (index >=0) {
                ch = Header.alphaCharsLowerCase.charAt(index);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public static int wordCount(String s) {
    	int len = s.length();
    	if(len == 0) return 0;
    	int c = 0, i = 0;
    	boolean w = isWhiteSpace(s.charAt(0));
    	boolean isWord = w;
    	while (i < len) {
    		w = !isWhiteSpace(s.charAt(i));
    		if (isWord != (w)) {
    			if (w) c++;
    			isWord = w;
    		}
    		i++;
    	}
    	return c;
    }
    
	public static void addLineWordsToList(int lineN, String line, Vector list) {
        StringBuffer sb = new StringBuffer();
        char ch;
        for(int i = 0; i < line.length(); i++) {
            ch = line.charAt(i);
            if (!Util.isWhiteSpace(ch)) {
                sb.append(ch);
            } else {
                if (sb.length() > 0) {
                    list.add(new WordItem(Util.toLowerCase(sb.toString()), lineN));
                    sb.delete(0, sb.length());
                }    
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
    }
	/**
	 * 	compares two strings same way as in String.compare except it is used
	 *  the specified charset in dictionary file
	 *  order is numbers the uppers case chars then lower case chars and the other chars
	 *   
	 * @param s1
	 * @param s2
	 * @return
	 * @throws ClassCastException if char is not defined in those charsets
	 */
    public static int compareStr(String s1, String s2) throws ClassCastException {
        int i = 0;
        while ((i < s1.length()) && (i < s2.length())) {
            int i1 = charToByte(s1.charAt(i));
            int i2 = charToByte(s2.charAt(i));
            if (i1 - i2 != 0) return i1 - i2;
            i++;
        }
        return s1.length() - s2.length();
    }

    public static boolean isWhiteSpace(char ch) {
    	return (ch == '\n' || ch == '\t' || Header.otherChars.indexOf(ch) >= 0);
    }
    
    public static int charToByte(char ch) {
    	int i = MDictIndex.allChars.indexOf(ch);
    	if (i == -1) {
    			System.out.println((int) ch);
    			String s = Integer.toHexString((int) ch);
    			while (s.length() < 4) s = "0" + s;
    			s = "\\u" + s;
//    			System.out.printf("%s  %s\n", s, Util.unescape(s));
                System.out.printf("Char '%s' (%s) is not allowed in file.\n" +
                		"Add it in OtherChars section or strip it down in " +
                		"ExcludeChars at beginning of the imported file.\n", ch, s);
                throw new ClassCastException("Char");
    	}
    	return i;
    }

    public static byte [] strToBytes(String s) throws Exception {
    	byte [] bb = new byte [s.length()];
    	int index;
    	for(int i=0; i < s.length();i++) {
    		if (s.charAt(i) == '\n') {
    			index = KEY_RETURN;
    		}
    		else {
    			index = charToByte(s.charAt(i));
        		if (index > (KEY_RETURN - 1)) {
        			System.out.printf("The charset is to wide sorry..");
        			throw new Exception("Wrong charset");
        		}
    		}
    		
    		bb[i] = (byte) index;
    	}
    	
    	return bb;
    }

    static void write3BytesInt(OutputStream s, int i) throws IOException {
        byte b1, b2, b3;
        b1 = (byte) ((i >> 16) & 0xff);
        b2 = (byte) ((i >> 8) & 0xff);
        b3 = (byte) (i & 0xff);
        s.write(b1);
        s.write(b2);
        s.write(b3);
    }

    public static int intToCursor(int offset) {
    	int o3, o2, o1;
    	o1 =  offset &     0x7f;
    	o2 = (offset &   0x3f80) << 1;
    	o3 = (offset & 0x1FC000) << 2;
    	offset = o3 | o2 | o1;
    	offset |= 0x00800000;
    	return offset;
    }
}

/* Line filter
 * object read each line from the raw file troght LineReder end return
 * each line by call filterde by specific parameters if the line does not corespodn to current filter
 * filter skip this line and match the next until find the correct one
 */
 
class LineFilter {
	BufferedReader reader;

    public LineFilter(BufferedReader r) {
        reader = r;
    }
    
    private boolean filterEmptyTranslation(String s) {
        if (MDictIndex.acceptEmptyTranslation) return true;
        int crIndex =  s.indexOf('\n');
        int tabIndex = s.indexOf('\t');
        return (crIndex - tabIndex > 1);
    }
    
    private boolean filterMaxCountWords(String s) {
    	if (MDictIndex.maxWordCount == Integer.MAX_VALUE) return true;
        int i =  s.indexOf('\t');
    	String side = s.substring(0, i);
    	int wc = Util.wordCount(side);
    	if (wc == 0 || wc > MDictIndex.maxWordCount) return false;
//    	System.out.printf("%s;%d\n", side, wc);
        int ie =  s.indexOf('\n');
    	side = s.substring(i+1, ie);
    	wc = Util.wordCount(side);
    	if (wc == 0 || wc > MDictIndex.maxWordCount) return false;
//    	System.out.printf("%s;%d\n", side, wc);
    	
        
    	return true;
    }
    
    /*
     *  return true if line is contain both sides of the transaction
     *  at the moment I will test just the right side if is emty
     * but I should chech both sides for any character data not include white spaces
     */
    private boolean filterShorts(String s) {
        if (MDictIndex.acceptShorts) return true;
        boolean prevCharWhiteSpace = true;
        int i = 0;
        StringBuffer sb = new StringBuffer();
        char ch;
        int tabPos;
        if (s.indexOf('\t') == -1 || s.indexOf('\n') == -1) return false;
        while ((ch = s.charAt(i)) != '\t') {
            if (!Util.isWhiteSpace(ch)) {
                if (prevCharWhiteSpace) {
                    sb.append(ch);
                }
                prevCharWhiteSpace = false;
            } else {
                prevCharWhiteSpace = true;
            }
            i++;
        }
        tabPos = i;
//        if ((sb.length() > 1) && s.regionMatches(false, i + 1, sb.toString(), 0, sb.length())) {
        if ((sb.length() > 1) && s.substring(i + 1, s.length() - 1).trim().equals(sb.toString())) {
//            System.out.println(s+ "xxx" + sb.toString());
            return false;
        }
        sb.delete(0, sb.length());
        i++;
        prevCharWhiteSpace = true;
        while ((ch = s.charAt(i)) != '\n') {
            if (!Util.isWhiteSpace(ch)) {
                if (prevCharWhiteSpace) {
                    sb.append(ch);
                }
                prevCharWhiteSpace = false;
            } else {
                prevCharWhiteSpace = true;
            }
            i++;
        }
        if (sb.length() > 1 && s.substring(0, tabPos).trim().equals(sb.toString())) {
            return false;
        }
        
        return true;
    }
    
    public String readLine() {
        boolean filter;
        String s;
        String r;
        do {
            try {
				s = reader.readLine();
			} catch (IOException e) {
				s = null;
			}
            if (s == null) return null;
            s = Util.excludeChars(s);
            s += "\n";
            r = s;
            s = Util.toLowerCase(s);
            filter = filterEmptyTranslation(s);
            filter &= filterShorts(s);
            filter &= filterMaxCountWords(s);
        } while (!filter);
        return r;
    }
}


class Header {
    static public String alphaCharsLowerCase = "";
    static public String alphaCharsUpperCase = "";
    static public String numberChars = "";
    static public String otherChars = "";
    static public String otherCharsNoWhiteSpace = "";
    public String keyMap1 = "";
    public String keyMap2 = "";
    
	int wordCount;
    String [] languages = new String[2];
    Vector rootIndex = new Vector();
    int maxSizeOfIndexPage = 0;
    int lineIndexOffset;
    

    public void updateOffsets() {
    	int offset = headerSize();
    	for(int i = 0; i < rootIndex.size();i++) {
    		((WordItem) rootIndex.elementAt(i)).line += offset;
    	}
    	lineIndexOffset += offset + MDictIndex.cursorSize;
    }
    
    private int indexSize() {
    	int r = 0;

    	for(int i = 0; i < rootIndex.size();i++) {
    		// that is size of leading chars plus the position in file
    		r += MDictIndex.cursorSize;
    		r += ((WordItem)rootIndex.elementAt(i)).word.length();
    	}
        	
        r += MDictIndex.cursorSize; //Last index much easier to work with
        
    	return r;
    }
    
    public int headerSize() {
        int r = 4; // int size of the header
        
        r += 4; // int how many words in index dictonary contatin

        r += 4; //max size of indexPage;
        
        try {
            // size of the alpha chars bytes lower case
        	// plus end of line char
            r += (alphaCharsLowerCase.getBytes("UTF-8").length) + 2;
            // size of alpha chars bytes upper case
        	// plus end of line char
            r += alphaCharsUpperCase.getBytes("UTF-8").length + 2;
          
            // size of number chars
            r += numberChars.getBytes("UTF-8").length + 2;
            // size all others chars
            r += otherChars.getBytes("UTF-8").length + 2;
            //
            r += otherCharsNoWhiteSpace.getBytes("UTF-8").length + 2;

            // size of keyMap + end of line
            r += keyMap1.getBytes("UTF-8").length + 2;
            r += keyMap2.getBytes("UTF-8").length + 2;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        
        r += 8; // languages codes

        r += 4; // int how big is page index (index) Size is specifiled in bytes
        
        r += indexSize();
        r += 4;
        r += 4;
        return r;
    }

    public void write(DataOutputStream s) throws IOException, Exception {
        s.writeInt(headerSize());
        
        s.writeUTF(alphaCharsLowerCase);
        s.writeUTF(alphaCharsUpperCase);
        s.writeUTF(numberChars);
        s.writeUTF(otherChars);
        s.writeUTF(otherCharsNoWhiteSpace);
        s.writeUTF(keyMap1);
        s.writeUTF(keyMap2);
        s.writeUTF(languages[0]);
        s.writeUTF(languages[1]);
        s.writeInt(wordCount);
        s.writeInt(MDictIndex.cursorSize);
        s.writeInt(maxSizeOfIndexPage);
        s.writeInt(lineIndexOffset);
        
        s.writeInt(indexSize());
        for (int i=0; i < rootIndex.size();i++) {
        	int offset = Util.intToCursor(((WordItem)rootIndex.elementAt(i)).line);
        	Util.write3BytesInt(s, offset);
        	s.write(Util.strToBytes(((WordItem)rootIndex.elementAt(i)).word));
        }
        Util.write3BytesInt(s, Util.intToCursor(lineIndexOffset - MDictIndex.cursorSize));
    }
}




public class MDictIndex {
	static boolean acceptEmptyTranslation = false;
	static boolean acceptShorts = false;
	static public String excludeChars = "";
	static public String excludeCharsTo = "";
	static public String allChars = "";
	static final int cursorSize = 3; // it is not implemented yet do not change
	static boolean verbose = false;
	static boolean printInfo = true;
    static int chunkSize = 6000;
    static String input_file = "/home/jaromes/work/mdict_encz.txt";
    static String output_file = "/home/jaromes/src/eclipse-projects/MDict/res/i";
    static boolean indexAlphaOnly = false;
    static int maxWordCount = Integer.MAX_VALUE; 
    
    static int checkParam(String[] args, String param) {
    	for (int i = 0; i < args.length; i++) {
    		if (param.compareTo(args[i]) == 0) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    
    public static void main(String[] args) {
    	Header header = new Header();
        
        BufferedReader in;
        int i;
        acceptShorts = (checkParam(args, "-fs") == -1);
        acceptEmptyTranslation = (checkParam(args, "-fa") >= 0);
        indexAlphaOnly = (checkParam(args, "-ia") >=0);
        i = checkParam(args, "-fm");
        if (i >= 0) {
        	try {
        		maxWordCount = Integer.parseInt(args[i + 1]);
        	} catch (Exception ex) {
        		System.out.println("Wrong value for \"-fm\" switch must be > 0.");
        		return;
        	}
        }
        i = checkParam(args, "-cs");
        if (i >= 0) {
        	try {
        		chunkSize = Integer.parseInt(args[i + 1]);
        	} catch (Exception ex) {
        		System.out.println("Wrong value for \"-cs\" switch must be > 0.");
        		return;
        	}
        }

        i = checkParam(args, "-i");
        try {
        	if (i == -1) throw new Exception("Paramater is missing.");
        	input_file = args[i + 1];
        } catch (Exception ex) {
        	System.out.println("Input file must be specified with parameter \"-i\" something like \"-i /directory/file.txt\"");
        	return;
        }
		try {
			in = new BufferedReader(new FileReader(input_file));
		} catch (FileNotFoundException e) {
			System.out.printf("File \"%s\"not found!\n", input_file);
			return;
		}
        i = checkParam(args, "-o");
        try {
        	if (i == -1) throw new Exception("Paramater is missing.");
        	output_file = args[i + 1];
        } catch (Exception ex) {
        	System.out.println("Output directory must be specified with parameter \"-o\" something like \"-o /directory\"");
        	return;
        }
        if (output_file.charAt(output_file.length() - 1) != File.separatorChar) {
        	output_file += File.separator;
        }
        File file = new File(output_file);
        if (!file.isDirectory()) {
        	System.out.printf("Directory \"%s\" is not directory or directory is missing.\n", output_file);
        	return;
        }
        if (!file.exists()) {
        	System.out.printf("Directory \"%s\" not exist.\n", output_file);
        	return;
        }
        file = null;
        
        output_file += "i";
        
		LineFilter filter = new LineFilter(in);
        
        String s;
        if (acceptShorts) s = "yes"; else s = "no";
        System.out.printf("Accept shorts: %s\n", s);
        if (acceptEmptyTranslation) s = "yes"; else s = "no";
        System.out.printf("Accept Emty translation: %s\n", s);
        System.out.printf("Maximum count of words on one side: %d\n", maxWordCount);
        System.out.printf("Chunk size: %d\n", chunkSize);
        if (indexAlphaOnly) s = "yes"; else s = "no";
        System.out.printf("Index by alpha chars only: %s\n", s);
        System.out.printf("Ouput files are: %sXXXX\n", output_file);
        System.out.println("--------------------------------------------------------------");
        
        
        Vector tmpList = new Vector();
        Vector lineVector = new Vector();
        String line;

        // Read a header of file charset key pads and all iformatio how to build a index
        try {
			line = in.readLine();
			while (!line.startsWith("DATA:")) {
				if (line.startsWith("ExcludeChars:")) {
					MDictIndex.excludeChars = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("ExcludeCharsTo:")) {
					MDictIndex.excludeCharsTo = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("Languages:")) {
					header.languages[0] = Util.unescape(line.substring(line.indexOf(':') + 1, line.indexOf(':') + 3));
					header.languages[1] = Util.unescape(line.substring(line.indexOf(',') + 1, line.indexOf(',') + 3));
				} else if (line.startsWith("CharsetLowerCase:")) {
					Header.alphaCharsLowerCase = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("CharsetUpperCase:")) {
					Header.alphaCharsUpperCase = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("Numbers:")) {
					Header.numberChars = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("OtherChars:")) {
					Header.otherChars = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("OtherCharsNoWhiteSpace:")) {
					Header.otherCharsNoWhiteSpace = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("KeyMap1:")) {
					header.keyMap1 = Util.unescape(line.substring(line.indexOf(':') + 1));
				} else if (line.startsWith("KeyMap2:")) {
					header.keyMap2 = Util.unescape(line.substring(line.indexOf(':') + 1));
				}

				line = in.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				in.close();
			} catch (IOException e1) {}
			return;
		}

		allChars = Header.numberChars + Header.otherCharsNoWhiteSpace + Header.alphaCharsUpperCase +
			Header.alphaCharsLowerCase + Header.otherChars + "\t";

        // process the file and store it in temporary vectors
        if (printInfo)
        	System.out.println("Reading and filtering the input file");

        while((line = filter.readLine()) != null) {
            Util.addLineWordsToList(lineVector.size(), line, tmpList);
            lineVector.add(line);
        }

        // copy all words to array for sorting reasons
        WordItem [] wordList = new WordItem[tmpList.size()];
        for(i = 0; i < tmpList.size(); i++)
            wordList[i] = (WordItem) tmpList.elementAt(i);
        tmpList.removeAllElements();
        // sort the list of the words ordered by word and the by line
        if (printInfo)
        	System.out.println("Sorting the extracted words.");
        try {
        	Arrays.sort(wordList, new WordItemComparator());
        } catch (ClassCastException ex) {
        	System.out.println("Index can not be build.");
			try {
				in.close();
			} catch (IOException e) {}
			return;
        }
        
        // removing duplicate words
        if (printInfo)
        	System.out.println("Removing duplicate words");
        i = 0;
        Vector linkList = new Vector();
        int prevLine = -1;
        int linkCount = 0;
        int maxLinkCount = 0;
        int pageSize = chunkSize;
        header.lineIndexOffset = 0;
        
        do {
            String prevWord = wordList[i].word;
            while (i < wordList.length && prevWord.equals(wordList[i].word)) {
                if (prevLine != wordList[i].line) {
                    linkList.add(new Integer(wordList[i].line));
                    prevLine = wordList[i].line;
                    linkCount++;
                }
                i++;
            }
            

        	if (pageSize >= chunkSize) {
            	header.rootIndex.add(new WordItem(prevWord, header.lineIndexOffset));
            	header.maxSizeOfIndexPage = Math.max(pageSize, header.maxSizeOfIndexPage);
        		pageSize = 0;
            }
            tmpList.add(new WordIndexItem(prevWord, linkList));
			header.lineIndexOffset += prevWord.length() + cursorSize;
        	pageSize += prevWord.length() + cursorSize;
            linkList.removeAllElements();
            maxLinkCount = Math.max(linkCount, maxLinkCount);
            prevLine = -1;
            linkCount = 0;
        } while (i < wordList.length);

        wordList = null;
        WordIndexItem [] index = new WordIndexItem[tmpList.size()];
        for(i = 0; i < tmpList.size(); i++) index[i] = (WordIndexItem) tmpList.elementAt(i);
        tmpList.removeAllElements();
        tmpList = null;
        
        header.wordCount = index.length;
        //update correct offsets in root index + update offset of line index 
        header.updateOffsets();

        if (printInfo) {
            System.out.printf("Word count: %d   Index length include root index: %d Max link count of one word: %d\n",
            		header.wordCount, header.lineIndexOffset, maxLinkCount);

            if (verbose) {
            	System.out.println("Root index");
            	for(i = 0; i < header.rootIndex.size();i++) {
            		System.out.printf("\t %s %d\n", ((WordItem)header.rootIndex.elementAt(i)).word,	((WordItem)header.rootIndex.elementAt(i)).line);
            	}
            }
        }
        
        int offset = header.lineIndexOffset;
        // we know position of line index in result file but not position of data lines
        // so count position (offset) of data lines in file
        for (i = 0; i < index.length; i++)
        	offset += (index[i].links.length * cursorSize); 
        if (printInfo) 
        	System.out.println("Processing data lines");
        // store file offset to each line
        Line [] lineList = new Line[lineVector.size()];
        for(i = 0; i < lineVector.size(); i++) {
        	line = (String) lineVector.elementAt(i);
            lineList[i] = new Line(line, offset);
            offset += line.length();
        }
        lineVector.removeAllElements();
        
        // replaces line number by the offset in the main index
        // and sort lines grouped by word by word position on each side..
        if (printInfo) 
        	System.out.println("Replacing reference line number by the offset");
        for(i = 0; i < index.length;i++) {
        	// sorting a lines for each word
            index[i].sortLinks(lineList);
            //update links to the lines by line file offset
            index[i].updateLinks(lineList);
        }
         
        // write file
        if (printInfo) 
        	System.out.println("Creating the file");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        try {
        	//write out header, header include root index
			header.write(out);
			
			//write out main index
			// pseudo format: 3 bytes point to line index and then word string
            offset = 0;
            for(i = 0; i < index.length; i++) {
                Util.write3BytesInt(out, Util.intToCursor(offset));
                out.write(Util.strToBytes(index[i].word));
                offset += index[i].links.length * cursorSize;
            }
            // we have to write one more record as we need have posibiy retreive the
            // count of the lines for the last word in index
            Util.write3BytesInt(out, Util.intToCursor(offset));
            
            // write out index of data lines for each word
            for(i = 0; i < index.length;i++) {
            	index[i].writeLines(out);
            }
            //write out data lines
            for(i =0; i < lineList.length; i++)
            	lineList[i].write(out);
            
            out.flush();
            
            
            // and file chunker fo separate whole data to small files as midp can not access
            // ramdomly so we will read whole data pages
            if (printInfo)
            	System.out.println("Writing the file as the chunks");
            
            byte [] output = bos.toByteArray();
            int len = Math.min(chunkSize, output.length);
            int chunk = 0;
            int pos = 0;
            FileOutputStream outFile;
            while (len > 0) {
                outFile = new FileOutputStream(output_file + String.valueOf(chunk));
                outFile.write(output, pos, len);
                outFile.close();
                chunk++;
                pos = chunk * chunkSize;
                len = output.length - (pos);
                if (len > chunkSize) len = chunkSize;
            }
            if (printInfo)
            	System.out.println("Data size is " + out.size());
            System.out.println("Data were written.");
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }



}

