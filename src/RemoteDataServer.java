import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.imageio.ImageIO;

public class RemoteDataServer implements Runnable{
		int PORT;
		
		private InetAddress ipAddress;
		private DatagramSocket server;
		private byte[] buf;
		private DatagramPacket dgp;
		
		private String message;
		private AutoBot bot;
		
		private ServerListener window;
		
		public RemoteDataServer(int port){
			PORT = port;
			buf = new byte[1000];
			dgp = new DatagramPacket(buf, buf.length);
			bot = new AutoBot();
		}
		
		public RemoteDataServer(){
			buf = new byte[1000];
			dgp = new DatagramPacket(buf, buf.length);
			bot = new AutoBot();
		}
		
		public void setPort(int port){ PORT = port; }
		
		public void setIP(InetAddress inet){ ipAddress = inet; }
		
		public void setServerListener(ServerListener listener){ window = listener; }
		
		public void shutdown(){
			try{ server.close(); }
			catch(Exception e){}
		}

		public void run(){
			boolean connected = false;
			try {
				server = new DatagramSocket(PORT, ipAddress);
				connected = true;
			}
			catch(BindException e){
				setListenerMessage("Port "+PORT+" is already in use. Use a different Port");
			}
			catch(Exception e){
				setListenerMessage("Unable to connect");
			}
			
			while(connected){
				// get message from sender
				try{ server.receive(dgp);
				
					// translate and use the message to automate the desktop
					message = new String(dgp.getData(), 0, dgp.getLength());
					if (message.equals("Connectivity")){
						setListenerMessage("Trying to Connect");
						server.send(dgp); //echo the message back
					}else if(message.equals("Connected")){
						server.send(dgp); //echo the message back
					}else if(message.equals("Close")){
						setListenerMessage("Controller has Disconnected. Trying to reconnect."); //echo the message back
					}else{
						setListenerMessage("Connected to Controller");
						bot.handleMessage(message);
					}
				}catch(Exception e){
					System.out.println(e);
					setListenerMessage("Disconnected");
					connected = false;}
			}
		}
		
		private void setListenerMessage(String msg){
			if(window != null){
				window.setMessage(msg);
			}
		}
		
		public void sendImage(){
			Thread send_image_thread = new Thread(new ImageSender());
			send_image_thread.start();
		}

		//TODO this class needs to be moved out or made private
		public class ImageSender implements Runnable{
			public void run(){
				BufferedImage img = bot.getScreenCap();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				try{
					ImageIO.write(img, "png", buffer);
				}catch(IOException e){System.out.println(e);}	
				byte[] data = buffer.toByteArray();
			    
	            DatagramPacket out = new DatagramPacket(data, data.length, ipAddress, PORT);
	            try{ server.send(out); }
	            catch(IOException e){//System.out.println(e);
	            	
	            }
			}// close run
		}// close ImageSender class
	}