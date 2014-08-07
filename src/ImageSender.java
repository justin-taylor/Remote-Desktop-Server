import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class ImageSender implements Runnable{
	
	private InetAddress clientAddr;
	private int clientPort;
	private DatagramSocket socket;
	public boolean connected = false;
	
	public static final float SIZETHRESHOLD = 100f;
	
	private BufferedImage img;
	
	public ImageSender(String ip, int port){
		try{
			clientAddr = InetAddress.getByName(ip);
		}
		catch (Exception e){
			System.out.print("Exception setting ip address");
		}
		
		clientPort = port;
		
		try {
	           socket = new DatagramSocket();	           
	           connected = true;
	           
	       }
	       catch (Exception e) {
	           System.out.print("Could not bind to a port");
	       }
	}
	
	public ImageSender(InetAddress ip, int port){
		clientAddr = ip;
		clientPort = port;
		
		try {
	           socket = new DatagramSocket();	           
	           connected = true;
	           
	       }
	       catch (Exception e) {
	           System.out.print("Could not bind to a port");
	       }
	}
	
	public void setImage(BufferedImage image)
	{
		img = image;
	}
	
	public void setPort(int port)
	{
		clientPort = port;
	}
	
	public void run(){
		ByteArrayOutputStream buffer;
		
		try{
			ByteArrayOutputStream tmp = compressImage(img, 1.0f);
		    ImageIO.write(img, "jpeg", tmp);
		    tmp.close();
		    
		    int contentLength = tmp.size();
		    float compress = 64000.0f/contentLength;
		    System.out.println("Compress size "+compress);
		    
		    if(compress > 1.0) {
		    	buffer = tmp;
		    } else {
		    	buffer = compressImage(img, compress);
		    }

		}catch(IOException e){
			System.out.println(e);
			return;
		}
		
		byte[] data = buffer.toByteArray();
        DatagramPacket out = new DatagramPacket(data, data.length, clientAddr, clientPort);
        
        try {
			socket.send(out);
			buffer.close();
		} catch (IOException e) {
			System.out.println("Data Length = "+data.length);
			e.printStackTrace();
		}
	}
	
	private BufferedImage scaleImage(BufferedImage image, int width, int height)
	{
		BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Paint scaled version of image to new image
		Graphics2D graphics2D = scaledImage.createGraphics();
		
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		
		graphics2D.drawImage(image, 0, 0, width, height, null); 
		graphics2D.dispose();
		return scaledImage;
	}
	
	private BufferedImage scaleImage(BufferedImage image, int width, int height, float scale)
	{
		BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// Paint scaled version of image to new image
		Graphics2D graphics2D = scaledImage.createGraphics();
		AffineTransform xform = AffineTransform.getScaleInstance(scale, scale);
		
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		
		graphics2D.drawImage(image, xform, null);
		
		graphics2D.dispose();
		return scaledImage;
	}
	
	
	/*
	 * 
	 * Compression
	 * 
	 */
	private ByteArrayOutputStream compressImage(BufferedImage image, float quality) throws IOException 
	{
		// Get a ImageWriter for jpeg format.
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
		if (!writers.hasNext()) throw new IllegalStateException("No writers found");
		ImageWriter writer = (ImageWriter) writers.next();
		
		while(!writer.getDefaultWriteParam().canWriteCompressed() && writers.next() != null)
		{
			writer = writers.next();
		}
		
		
		// Create the ImageWriteParam to compress the image.
		ImageWriteParam param = writer.getDefaultWriteParam();
		
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality);
		
		// The output will be a ByteArrayOutputStream (in memory)
		ByteArrayOutputStream bos = new ByteArrayOutputStream(32768);
		ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
		writer.setOutput(ios);
		writer.write(null, new IIOImage(image, null, null), param);
		ios.flush();
		
		return bos;
		
		/*
		// otherwise the buffer size will be zero!
		// From the ByteArrayOutputStream create a RenderedImage.
		ByteArrayInputStream in = new ByteArrayInputStream(bos.toByteArray());
		RenderedImage out = ImageIO.read(in);
		int size = bos.toByteArray().length;
		showImage("Compressed to " + quality + ": " + size + " bytes", out); 
		*/
	}
	
	
}
