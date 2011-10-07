import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.imageio.ImageIO;

public class ImageSender implements Runnable{
	
	private InetAddress clientAddr;
	private int clientPort;
	private DatagramSocket socket;
	byte[] buf = new byte[65000];
	public boolean connected = false;
	
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
	
	public void run(){
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "bmp", buffer);
		}catch(IOException e){
			System.out.println("EXCEPTION:");
			System.out.println(e);
		}
		
		byte[] data = buffer.toByteArray();
	    
		//data = new String("testing").getBytes();
		
		
        DatagramPacket out = new DatagramPacket(data, data.length, clientAddr, clientPort);
        try{
        	System.out.println("Sending   "+data.length + "   to: "+out.getAddress().getHostAddress() +"  on port:"+out.getPort());

            socket.send(out);
        	System.out.println("Sending   "+data.length + "   to: "+out.getAddress().getHostAddress() +"  on port:"+out.getPort());
        	
        }catch(IOException e){
        	System.out.println("EXCEPTION: "+data.length);
        	e.printStackTrace();
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

}
