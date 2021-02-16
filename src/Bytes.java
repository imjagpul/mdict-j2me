import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * ByteString is similar to java.lang.String, except that it contains
 * bytes instead of characters.
 */
public class Bytes {
    static public String alphaCharsLowerCase = "abcdefghijklmnopqrstuvwxyz";
    static public String alphaCharsUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static public String numberChars = "0123456789";
    static public String wordChars = "'`";
    static public String sortStr = numberChars + wordChars + alphaCharsUpperCase + alphaCharsLowerCase;
/*
    public static int utf8readLine(byte [] src, char [] dst) {
        int pos = 0;
        int i = 0;
        while(true) {
            dst[i] = utf8getchar(src, pos);
            pos += utf8charlen(src, pos);
            if (dst[i] == '\n') break;
            i++;
        }
        return i + 1;
    }
    
    public static int utf8toChars(byte [] src, char [] dst, int len) {
        int pos = 0;
        int i = 0;
        while(pos < len) {
            dst[i] = utf8getchar(src, pos);
            pos += utf8charlen(src, pos);
            i++;
        }
        return i;
    }
    
    public static int utf8charlen(byte [] s, int pos) {
        byte b = s[pos];
        int l;
        if ((b & 0x80) == 0) {
            l = 1;
        } else if ((b & 0xfe) == 0xfc) {
            l = 6;
        } else if ((b & 0xfc) == 0xf8) {
            l = 5;
        } else if ((b & 0xf8) == 0xf0) {
            l = 4;
        } else if ((b & 0xf0) == 0xe0) {
            l = 3;
        } else if ((b & 0xe0) == 0xc0) {
            l = 2;
        } else {
            l = 0;
        }
        
        if (l == 0) {
            throw new RuntimeException("Wrong unicode sequence");
        }
        return l;
    }
    
    static public char utf8getchar(byte [] s, int pos) {
        int l = utf8charlen(s, pos);
        int r =0;
        if (l == 1) {
            return (char) s[pos];
        } else if (l == 2) {
            r = ((s[pos] & 0x1f) << 6) | (s[pos+1] & 0x3f);
            return (char) r;
        } else {
            return '>';
        }
    }
  */  
    public char []  buffer;
    private int length;
    
    /**
     *  create new Bytes with new allocated buffer of size of String
     */
    public Bytes (String s) {
        length = s.length();
        buffer = new char[length];
        s.getChars(0, length, buffer, 0);
    }
    
    /**
     *  create new Bytes with the given size
     */
    public Bytes(int size) {
        buffer = new char[size];
        length = 0;
    }

    /**
     * Create new Bytes from the rosource if the resource is not found return
     * empty Bytes
     *
     */
    public static Bytes readFromResource(String resource) {
        Bytes r = new Bytes(0);
        InputStream in = r.buffer.getClass().getResourceAsStream(resource);
        InputStreamReader reader;
        if (in == null)
            return r;
        try {
            reader = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return r;
        }
        int len;
        char [] b = new char[256];
	try {
            while ((len = reader.read(b)) > 0)
                r.append(b, 0, len);
        } catch (IOException ex) {
            r.erase();
        }
        return r;
    }
    

    public void ensureCapacity (int num) {
	if ((buffer.length - length) >= num) return;
	char [] newBuffer;
        if ((buffer.length * 2) > (num + length))
               newBuffer = new char[buffer.length * 2];
        else
               newBuffer = new char[length + num];
        System.arraycopy(buffer, 0, newBuffer, 0, length);
        buffer = newBuffer;
    }
    
    public void append (char n) {
	ensureCapacity(1);
	buffer[length] = n;
	length++;
    }

    public void append(String s) {
        int len = s.length();
        ensureCapacity(len);
        s.getChars(0, len, buffer, length);
        length += len;
    }
    
    public void append (char [] that, int pos, int len ) {
	ensureCapacity(len);
        System.arraycopy(that, pos, buffer, length, len);
        length += len;
    }

    public void append(Bytes that, int start) {
       append(that.buffer, start, that.length - start); 
    }

    public void append(Bytes that) {
        append(that, 0);
    }
    
    public void append(Bytes that, int start, int end) {
        append(that.buffer, start, end - start);
    }

    public void replace(String s) {
        erase();
        ensureCapacity(s.length());
        for(int i = 0; i < s.length(); i++) {
            buffer[i] = s.charAt(i);
        }
        length = s.length();
    }
    
    public void replace(Bytes b, int start) {
        erase();
        append(b, start);
    }
    
    public void replace(Bytes b, int start, int end) {
        erase();
        append(b, start, end);
    }

    public void delete(int len) {
        if (length >= len)
               length -= len;
    }

    
    public boolean empty() {
        return (buffer == null || length <= 0);
    }
    
    public void erase () {
    	length = 0;
    }
    
    public void setEnd(int end) {
        length = end;
    }
    
    public void setStart(int start) {
    	int newSize = length - start;
    	System.arraycopy(buffer, start, buffer, 0, newSize);
    	length = newSize;
    }
    
    public void setByte(int pos, char b) {
        buffer[pos] = b;
    }

    /**
     * Return byte at given offset.
     */
    public char charAt (int i) {
	if (i >= length || i < 0) throw new IndexOutOfBoundsException();
        return buffer[i];
    }
    

    /**
     * Returns the offset for the first occurrence of a given byte
     * in the ByteString if present, and -1 otherwise.
     */
    public int indexOf (char x) {
        for (int i = 0; i < length; i++) {
            if (buffer[i] == x)
                return i;
        }
        return -1;
    }

    /**
     * Returns the offset for the first occurrence of a given byte
     * in the ByteString to the right of the given offset if present, and
     * -1 otherwise.
     */
    
    public int indexOf (char x, int offset) {
        for (int i = offset + 1; i < length; i++) {
            if (buffer[i] == x)
                return i;
        }
        return -1;
    }
    
    public boolean regionMatches(Bytes b, int start, int len) {
        if (length < len) return false;
        int i = 0;
        while (i < len) {
            char x = buffer[i];
            char y = b.buffer[i + start];
            if (!sameByteIgnoreCase(x, y)) return false;
            i++;
        }
        return true;
    }
   
    public boolean regionMatches(int from, String s) {
        int len = s.length();
        if ((length - from) < len) return false;
        int i = 0;
        while (i < len) {
            char x = buffer[i + from];
            char y = s.charAt(i);
            if (!sameByteIgnoreCase(x, y)) return false;
            i++;
        }
        return true;
    }
    public boolean equals(Bytes s) {
        if (length != s.size()) return false;
        int i = 0;
        while (i < length) {
            if (buffer[i] != s.buffer[i]) return false;
            i++;
        }
        return true;
    }
    
    public boolean equals(String s) {
        if (length != s.length()) return false;
        int i = 0;
        while (i < length) {
            if (buffer[i] != s.charAt(i)) return false;
            i++;
        }
        return true;
    }
    /*
    public int compareTo(Bytes b) {
        int i = 0;
        int len = b.length;
        while ((i < length) && (i < len)) {
            int r = buffer[i] - b.buffer[i];
            if (r != 0) return r;
            i++;
        }
        return (length - len);
    }
    */

    public int compareTo(Bytes b) {
        int i = 0;
        int len = b.length;
        while ((i < length) && (i < len)) {
            int i1 = sortStr.indexOf(buffer[i]);
            if (i1 == -1) i1 = buffer[i];
            int i2 = sortStr.indexOf(b.buffer[i]);
            if (i2 == -1) i2 = b.buffer[i];
            int r = i1 - i2;
            if (r != 0) return r;
            i++;
        }
        return (length - len);
    }

    public void trim() {
    	int low , i = 0;
    	while (i < length  && isWhitespace(buffer[i])) i++;
    	low= i;
    	i = length -1;
    	while(i > 0 && isWhitespace(buffer[i])) i--;
    	i++;
    	if (i <= low) {
    		length = 0;
    		return;
    	}
    	System.arraycopy(buffer, low, buffer, 0, i - low);
    	length = i - low; 
    }
    public void toLowerCase() {
        int i = 0;
        while (i < length) {
            int c = alphaCharsUpperCase.indexOf(buffer[i]);
            if (c != -1) {
                buffer[i] = alphaCharsLowerCase.charAt(c);
            }
            i++;
        }
    }
    
    /**
     *  Return size of the content of the buffer
     */
    public int size () {
        return length;
    }

    /**
     *  return true if the buffer is null or the size of the content is zero
     */
    public boolean emty() {
        return (buffer == null || length == 0);
    }
    
    
    public static boolean isLetter(char ch) {
        return (alphaCharsLowerCase.indexOf(ch) != -1 ||
                alphaCharsUpperCase.indexOf(ch) != -1);
    }

    public static boolean isDigit(char ch) {
    	return (numberChars.indexOf(ch) != -1);
    }
    
    public static boolean isLetterOrDigit(char b) {
        return (isLetter(b) || isDigit(b));
    }
    
    public static boolean isWordChar(char ch) {
    	return (isLetterOrDigit(ch) || wordChars.indexOf(ch) != -1);
    }
    
    public static boolean isWhitespace (char ch) {
    	return (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r');
    }

    public static boolean sameByteIgnoreCase(char x, char y) {
    	int i = alphaCharsUpperCase.indexOf(x); 
    	if (i != -1) x = alphaCharsLowerCase.charAt(i);
    	i = alphaCharsUpperCase.indexOf(y); 
    	if (i != -1) y = alphaCharsLowerCase.charAt(i);
    	
//        if (x >= 'a' && x <= 'z') x -= ('a' - 'A'); 
//        if (y >= 'a' && y <= 'z') y -= ('a' - 'A'); 
        return (x == y);
    }
    

    public int parseInt(int start, int end, int radix) {
        int r = 0;
        int i = end;
        int m = 1;
        while (i >= start) {
            r += Character.digit(buffer[i], radix) * m;
            i--;
            m = m * radix;
        }
        
        return r;
    }
  
    /**
     * Convert to String
     */
    public String toString () {
        if (length == 0) return null;
        return new String(buffer, 0, length);
    }
    
  
    /**
     * Return substring from offset start (inclusive) till offset
     * finish (exclusive).
     */
    public String substring (int start, int finish) {
	if (start > finish || finish > length)
	    throw new IndexOutOfBoundsException();
        return new String(buffer, start, finish - start);
    }
  

    /**
     *              SIMPLE TOKENIZER OF LETTER DIGIT TOKENS
     *
     *
     */
    
    /**
     * Return count of the the tokens separated by non letter or diggits tokens,
     * if size of the Bytes is zero returns zero
     *
     */
    public int wordCount() {
        int c = 0;
        if (size() <= 0) return 0;
        if (size() > 0) c++;
        int i = 0;
        boolean space, isWord;
        space = !isWordChar(charAt(0));
        while (i < size()) {
        	isWord =  isWordChar(charAt(i));
            if (isWord == space) {
                space = !isWord;
                if (!isWord) c++;
            }
            i++;
        }
        return c;
    }

    
    public int wordPos(int word) {
        boolean white = true;
        int idx = 0;
        int len = size();
        int count = 0;
        while (idx < len) {
            if (isWordChar(charAt(idx))) {
                if (white) {
                    if (count == word)
                        return idx;
                    count++;
                }
                white = false;
            } else {
                white = true;
            }
            idx++;
        }
        if (!white)
            idx = -1;
        return idx;
    }

    public int tokenLen(int pos) {
        int count = 0;
        if (pos >= size() || pos < 0) return 0;
        boolean isLD = isWordChar(charAt(pos));
        while (pos < size() && isLD == isWordChar(charAt(pos++))) {
            count++;
        }
        return count;
    }

    public int indexOfWord(Bytes word) {
        int end = size() - word.size();
        int idx = 0;
        
        boolean white = true;
        boolean found;
        char ch1;
        char ch2;
        
        while (idx <= end) {
            char b = charAt(idx);
            if (isWordChar(b) && white) {
                found = true;
                for (int i = 0; i < word.size(); i++) {
                	ch1 = charAt(idx + i);
                	ch2 = word.charAt(i);
                    found &= Bytes.sameByteIgnoreCase(ch1, ch2);
                }
                if (found && ((idx == end) ||
                		!isWordChar(charAt(word.size() + idx)))) {
                    return idx;
                }
            }
            white = !isWordChar(b);
            idx++;
        }
        return -1;
    }
    
    public Bytes getWord(int wordIndex) {
        int pos = wordPos(wordIndex);
        int len = tokenLen(pos);
        Bytes r = new  Bytes(len);
        r.length = len;
        System.arraycopy(buffer, pos, r.buffer, 0, len);
        return r;
    }
}
