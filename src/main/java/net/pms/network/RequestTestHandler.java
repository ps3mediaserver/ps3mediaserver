package net.pms.network;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import net.pms.io.ProcessWrapperImpl;
import net.pms.io.WindowsNamedPipe;
import net.pms.network.RequestHandlerV2;

import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.stream.ChunkedStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestTestHandler extends SimpleChannelHandler {
	class ByteTextFiles {
		FileOutputStream fos;
		FileWriter fw;
		String sIpPort;
		
		public ByteTextFiles(String sGroupName, String sIpPort) {
			this.sIpPort = sIpPort;
			String lTime = MillisToString();
			String s1 = LOGFOLDER + sGroupName + "-" + sIpPort + "-" + lTime;
			try {
				fw = new FileWriter( s1 + ".log");
				fos = new FileOutputStream( s1 + ".dat");
				if (mapBTF.get(sIpPort)!=null) {
					fosError.write(MillisToString() + ": " + sIpPort + ": ByteTextFiles: already in map !\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mapBTF.put(sIpPort, this);
		}
		
		public void close() {
			try {
				fw.flush();
				fos.flush();
				fw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fw=null;
			fos=null;
			mapBTF.remove(sIpPort);
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(RequestTestHandler.class);

	private final static SortedMap<String, ByteTextFiles> mapBTF = Collections.synchronizedSortedMap(new TreeMap(new HashMap<String, ByteTextFiles>()));
	private final static String LOGFOLDER = System.getProperty("user.home")+ "\\desktop\\ps3_log\\";

//	private static FileOutputStream fosError; 
	private static FileWriter fosError; 
	
	private final ChannelGroup group;
	private final String sGroupname;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS", Locale.ENGLISH);
	private static String MillisToString() {
		return  sdf.format(new Date(System.currentTimeMillis()));
	}

	public RequestTestHandler(ChannelGroup group) {
		this.group = group;
		sGroupname = group.getName();
		try {
			fosError = new FileWriter( LOGFOLDER + sGroupname + "-error.log");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    	logEvent(true, ctx, e);
        super.handleUpstream(ctx, e);
    }
 

 
    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    	logEvent(false, ctx, e);
        super.handleDownstream(ctx, e);
    }
    
    public String getLogFile(String sUpDown, ChannelHandlerContext ctx, ChannelEvent e) {
    	String sFile = sGroupname + "-" + sUpDown + "-" + getIpPort(ctx, e);
    	return sFile;
    }
    
    public String getIpPort(ChannelHandlerContext ctx, ChannelEvent e) {
    	InetSocketAddress isa = (InetSocketAddress) e.getChannel().getRemoteAddress();
    	return isa.getAddress().getHostAddress() + "-" + isa.getPort();
    }
    
    public void logEvent(boolean bUp, ChannelHandlerContext ctx, ChannelEvent e) throws IOException{
    	String sIpPort = getIpPort(ctx, e);
    	ByteTextFiles btf = mapBTF.get(sIpPort);
    	
    	// check, if you need to open a file
    	if (bUp) {
    		if (e instanceof ChannelStateEvent) {
    			ChannelStateEvent cse = (ChannelStateEvent) e;
    			String sEvent = cse.toString();
    			if (sEvent.contains("OPEN"))  {
    				if (btf!=null) {
    					fosError.write(MillisToString() + ": " + sIpPort + ": second OPEN received !\n");
    					fosError.flush();
    					btf.close();
    				}
    				btf = new ByteTextFiles(sGroupname, sIpPort);
    			}
     		}
    	}
    	

    	// now do the logging
    	String sPref = MillisToString() + ": "  + ((bUp) ? "up":"down");
    	if (btf==null) {
			fosError.write(sPref + ": btf should not be null here !: " + e + "\n");
			fosError.flush();
    	}
    	
    	
        if (e instanceof ChannelStateEvent) {
            btf.fw.write(sPref + ", Channel state changed: " + e + "\n");
        } else  if (e instanceof ExceptionEvent) {
        	btf.fw.write(sPref  + ", ExceptionEvent: " + e + "\n");
        } else  if (e instanceof MessageEvent) {
        	//            logger.info(sPref + ", MessageEvent: " + e + "\n");
        	MessageEvent mes = (MessageEvent) e;
        	Object o = mes.getMessage();
        	if (o instanceof DefaultHttpRequest) {
        		DefaultHttpRequest dhr = (DefaultHttpRequest) o;
        		String sResponse = dhr.getContent().toString(Charset.defaultCharset());
        		btf.fw.write(sPref + ", MessageEvent: DefaultHttpRequest: " + mes + "\n" + sResponse + "\n");
        	}  else if (o instanceof DefaultHttpResponse) {
        		DefaultHttpResponse dhr = (DefaultHttpResponse) o;
        		String sResponse = dhr.getContent().toString(Charset.defaultCharset());
        		btf.fw.write(sPref + ", MessageEvent: DefaultHttpResponse: " + mes + "\n" + sResponse + "\n");
        	} else if (o instanceof ChunkedStream) {
        		ChunkedStream chs = (ChunkedStream) o;
        		String s = "Chunk: ";
				try {
					s = "Chunk: transferredbytes: " + chs.getTransferredBytes() + ", hasNextChunk: " + chs.hasNextChunk() + ", isEndOfInput: " + chs.isEndOfInput();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
        		btf.fw.write(sPref + ", MessageEvent: ChunkedStream: " + mes + "\n" + s + "\n");
        	} else if (o instanceof BigEndianHeapChannelBuffer) {
        		
        		BigEndianHeapChannelBuffer buf = (BigEndianHeapChannelBuffer) o;
        		String s = "BigEndianHeapChannelBuffer: ";
				try {
					s = "BigEndianHeapChannelBuffer: buf: readerIndex: " + buf.readerIndex() + ", readableBytes: " + buf.readableBytes() + ", hasArray: " + buf.hasArray() +"\n";
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				btf.fos.write(buf.array(), buf.readerIndex(), buf.readableBytes());
				btf.fos.flush();
        		btf.fw.write(sPref + ", MessageEvent: BigEndianHeapChannelBuffer: " + mes + "\n" + s + "\n");
        	} else {
            	btf.fw.write(sPref + ", MessageEvent: Unknown Type: " + o.getClass().getName() + "\n");
        	}        
        } else  if (e instanceof WriteCompletionEvent) {
        	btf.fw.write(sPref + ", WriteCompletionEvent: " + e + "\n");
        	WriteCompletionEvent mes = (WriteCompletionEvent) e;
        	// insert something here ?
       	
        } else if (e instanceof ChildChannelStateEvent) {
        	btf.fw.write(sPref + ", ChildChannelStateEvent: " + e + "\n");
        } else {
        	btf.fw.write(sPref + ", unknown event type: " + e + "\n");
        };

    	
    	
    	// check, if you need to close a file
    	if (bUp) {
    		if (e instanceof ChannelStateEvent) {
    			ChannelStateEvent cse = (ChannelStateEvent) e;
    			String sEvent = cse.toString();
    			if (sEvent.contains("CLOSED"))  {
    				if (btf==null) {
    					fosError.write(MillisToString() + ": " + sIpPort + ": CLOSED received on non existing file!\n");
    					fosError.flush();
    				} else {
    					btf.close();
    				}
    			}
    		}
    	}
    }

}
