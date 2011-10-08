
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RemoteDataServer implements Runnable{
	
		// local settings
		private int PORT;
		private InetAddress ipAddress;
		
		
		//remote settings
		private int clientPort;
		private InetAddress listenerAddress;
		
		private DatagramSocket server;
		private byte[] buf;
		private DatagramPacket dgp;
		
		private String message;
		private AutoBot bot;
		
		private static final int REFRESH_THRESHOLD = 10;
		private int count = 0;
		
		private ServerListener window;
		
		private ImageSender sender;

		
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
		
		public void setClientPort(int port){  clientPort = port; }
		
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
				setConnectButtonEnabled(false);
			}
			catch(Exception e){
				setListenerMessage("Unable to connect");
				setConnectButtonEnabled(false);
			}
			
			while(connected){
				// get message from sender
				try{ server.receive(dgp);
				
					// store the packets address for sending images out
					listenerAddress = dgp.getAddress();
				
					// translate and use the message to automate the desktop
					message = new String(dgp.getData(), 0, dgp.getLength());
					if (message.equals("Connectivity"))
					{
						setListenerMessage("Trying to Connect");
						server.send(dgp); //echo the message back
					}
					
					else if(message.equals("Connected"))
					{
						server.send(dgp); //echo the message back
					}
					
					else if(message.equals("Close"))
					{
						setListenerMessage("Controller has Disconnected. Trying to reconnect."); //echo the message back
					}
					
					else
					{
						setListenerMessage("Connected to Controller");
						bot.handleMessage(message);
						
						count++;
						if(count == REFRESH_THRESHOLD)
						{
							count = 0;
							sendImage();
						}
					}
				}catch(Exception e){
					System.out.println(e);
					setListenerMessage("Disconnected");
					setConnectButtonEnabled(false);
					connected = false;
				}
			}
		}
		
		private void setListenerMessage(String msg){
			if(window != null){
				window.setMessage(msg);
			}
		}
		
		private void setConnectButtonEnabled(boolean enable){
			if(window != null){
				window.setConnectButtonEnabled(enable);
			}
		}
		
		public void sendImage(){
			if(sender == null && listenerAddress != null)
			{
				sender = new ImageSender(listenerAddress, clientPort);
			}
			
			if(sender != null)
			{
				sender.setPort(clientPort);
				sender.setImage( bot.getScreenCap() );
			
				Thread send_image_thread = new Thread(sender);
				send_image_thread.start();
			}
		}

		
	}