// 	Copyright 2010 Justin Taylor
// 	This software can be distributed under the terms of the
// 	GNU General Public License. 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class ServerWindow extends ServerListener implements ActionListener{
	
	public RemoteDataServer server;
	
	private Thread sThread; //server thread
	
	private static final int WINDOW_HEIGHT = 200;
	private static final int WINDOW_WIDTH = 350;
	
	private String ipAddress;
	
	private JFrame window = new JFrame("Remote Desktop Server");
	
	private JLabel addressLabel = new JLabel("");
	private JLabel portLabel = new JLabel("PORT: ");
	private JTextArea[] buffers = new JTextArea[3];
	private JTextField portTxt = new JTextField(5);
	private JTextField ipTxt = new JTextField(10);
	private JLabel serverMessages = new JLabel("Not Connected");
	
	private JButton connectButton = new JButton("Connect");
	private JButton disconnectButton = new JButton("Disconnect");
	
	public ServerWindow(){
		
		server = new RemoteDataServer();
		server.setServerListener( this );
		
		window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		connectButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		
		Container c = window.getContentPane();
		c.setLayout(new FlowLayout());
		
		try{
			InetAddress ip = InetAddress.getLocalHost();
			ipAddress = ip.getHostAddress();
			addressLabel.setText("IP Address: ");
			ipTxt.setText(ipAddress);
		}
		catch(Exception e){addressLabel.setText("IP Address Could Not be Resolved, Try typing in the IP address.");}
		
		int x;
		for(x = 0; x < 3; x++){
			buffers[x] = new JTextArea("", 1, 30);
			buffers[x].setEditable(false);
			buffers[x].setBackground(window.getBackground());
		}
		
		c.add(addressLabel);
		c.add(ipTxt);
		c.add(buffers[0]);
		c.add(portLabel);
		portTxt.setText("5444");
		c.add(portTxt);
		c.add(buffers[1]);
		c.add(connectButton);
		c.add(disconnectButton);
		c.add(buffers[2]);
		c.add(serverMessages);
		ipTxt.setSize(100, 20);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.setResizable(false);
	}
	
	public void actionPerformed(ActionEvent e){
		Object src = e.getSource();
		
		if(src instanceof JButton){
			if((JButton)src == connectButton){
				int port = Integer.parseInt(portTxt.getText());
				try{
					//InetAddress ip = InetAddress.getByName(ipTxt.getText());
					InetAddress ip = InetAddress.getByName("192.168.1.104");
					runServer(port, ip);
				}catch(UnknownHostException err){
					serverMessages.setText("Error: Check that the ip you have entered is correct.");
				}
			}
				
			else if((JButton)src == disconnectButton){
				closeServer();
			}
		}
	}
	
	public void runServer(int port, InetAddress ip){
		if(port <= 9999){
			server.setPort(port);
			server.setIP(ip);
			sThread = new Thread(server);
			sThread.start();
			serverMessages.setText("Waiting for connection on " + ip);
			connectButton.setEnabled(false);
		}else{
			serverMessages.setText("The port Number must be less than 10000");
		}
	}
	
	public void closeServer(){
		server.shutdown();
		
		serverMessages.setText("Disconnected");
		connectButton.setEnabled(true);
	}
	
	public void setMessage(String msg){
		serverMessages.setText(msg);
	}
	
	public static void main(String[] args){
		new ServerWindow();
	}
}
