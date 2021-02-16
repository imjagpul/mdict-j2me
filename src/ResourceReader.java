import java.io.IOException;
import java.io.InputStream;


public class ResourceReader extends InputStream{
    private int bufferCount = 1;
    private int chunkSize = -1;
    private ChunkBuffer [] buffers;
    private String name;
    private int cycle = 0;
    private int position;
    private Class thisClass;
    private int mark = 0;
    
    class ChunkBuffer {
        byte [] buffer = new byte[chunkSize];
        int chunkId = -1;
        int liveCycle;

        private void readBuffer(int chunk) {

            InputStream stream;

            if (chunkId == chunk) return;
            try {
                stream = thisClass.getResourceAsStream( name + String.valueOf(chunk));
                chunkId = chunk;
                liveCycle = cycle;
                stream.read(buffer);
                stream.close();

            } catch (Exception e) {}
        }
    
    }
    
    public ResourceReader(String n) {
        name = n;
        thisClass = this.getClass();
        position = 0;

        if (chunkSize == -1) {
        	try {
				InputStream stream = thisClass.getResourceAsStream(name + "0");
				chunkSize = stream.available();
				stream.close();
				stream = null;
			} catch (IOException e) {}
        }

        if (buffers == null) {
            buffers = new ChunkBuffer[bufferCount];
            for(int i = 0; i < bufferCount; i++)
            	buffers[i] = new ChunkBuffer();
        }
    }

    private int lastChunk = -1;
    private byte [] lastBuffer;
    
    private byte [] getBuffer (int chunk) {
    	if (chunk == lastChunk) return lastBuffer;
    	lastChunk = chunk;
        int firstEmpty = -1;
        int firstMinStats = -1;
        int posFound = -1;
        int min = ++cycle;
        for(int i = 0; i < bufferCount;i++) {
            if (firstEmpty == -1 && buffers[i].chunkId == -1) {
                firstEmpty = i;
            }
            if (buffers[i].chunkId == chunk) {
                posFound = i;
                buffers[i].liveCycle = cycle;
                break;
                
            }

            if (buffers[i].chunkId != -1 && buffers[i].liveCycle <= min) {
                min = buffers[i].liveCycle;
                firstMinStats = i;
            }
 
        }
       
        if (posFound != -1) {
        	lastBuffer = buffers[posFound].buffer; 
            return lastBuffer;
        }
        
        if (firstEmpty != -1) {
            buffers[firstEmpty].readBuffer(chunk);
            lastBuffer = buffers[firstEmpty].buffer; 
            return lastBuffer;
        }

        buffers[firstMinStats].readBuffer(chunk);
        lastBuffer = buffers[firstMinStats].buffer;
        return lastBuffer;
    }

    public int read(byte [] b, int pos, int len) {
        int p = position % chunkSize;
        int chunk = position / chunkSize;
        byte [] buffer = getBuffer(chunk);
        if ((p + len) <= chunkSize) {
            System.arraycopy(buffer, p, b, pos, len);
        } else {
            int len1 = chunkSize - p;
            int len2 = len - len1;
            System.arraycopy(buffer, p, b, pos, len1);
            buffer = getBuffer(chunk + 1);
            System.arraycopy(buffer, 0, b, pos + len1, len2);
        }
        position += len;
        return len;
    }

    public int read() {
        int p = position % chunkSize;
        int chunk = position++ / chunkSize;
        byte [] buffer = getBuffer(chunk);
        return buffer[p] & 0xff;
    }

    public void mark(int readLimit) {
    	mark = position;
    }
    
    public void reset() {
    	position = mark;
    }
    
    public long skip(long n) {
    	position += n;
    	return n;
    }

    public int available() {
    	return Integer.MAX_VALUE;
    }
    
    public boolean markSupported() {
    	return true;
    }
}
