
/*
 * @author : Dakshesh Patel
 * HTTP 0.8 Reponse Srver
 * 
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;



// HTTP 0.8 error response
 enum RESPONSE {
	BAD_REQUEST("400 Bad Request"),
	NOT_FOUND("404 Not Found"),
	NOT_IMPLEMENTED("501 Not Implemented"),
	INTERNAL_ERROR("500 Internal Error"),
	OK("200 OK"),
	TIME_OUT("408 Request Timeout");
	 
	
	
	private String execute;
	
	RESPONSE(String execute) {
		this.execute = execute;
	}
	
	public String execute() {
		return this.execute;
	}
	 
	
}
 
 /*
  * Create a socket 
  * Create a client socket
  * Throw it to thread -> Client Service
  * Be available for another client connection
  */
public class SimpleHTTPServer {
	static boolean isSeverConnected = true;
	public static void main(String[] args) throws IOException {
		ServerSocket server_socket = null;
		
		
		int port = Integer.parseInt(args[0]);
		
		try {
			server_socket = new ServerSocket(port);
			System.out.println("--------------SimpleHTTPServer (HTTP 0.8)-----------------");
			System.out.println("----------------------------------------------------------");
			System.out.println("Server is Listening on " + server_socket.getInetAddress() + "/" + port);
			System.out.println("----------------------------------------------------------");
			
		} catch (Exception e) {
			System.out.println("Could not create server socket!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		while(isSeverConnected) {
			try {
				Socket myclient_socket = server_socket.accept();
				System.out.println("Connection has been established on " + myclient_socket.getInetAddress());
				ClientService client_service = new ClientService(myclient_socket);
				client_service.start();
				
			} catch(Exception e) {
				System.out.println("Cant create thread");
				System.exit(-1);
			}
		}
		// TODO Auto-generated method stub
		
	}	

	

}

/*
 * ClientService
 * Retrive a response from client
 * Convert it into the directed format
 * Then Check for the Assigned Cases
 * Create Response and send it to the client
 * Wait for quarter seconds to close connection
 * 
 * 
 */
class ClientService extends Thread {
	Socket client_socket;
	boolean isThreadConnected = true;
	InputStreamReader reader = null;
	BufferedReader br_in = null;
	
	OutputStreamWriter writer = null;
	PrintWriter pr_out = null;
	
	public ClientService( Socket client_socket) {
		super();
		this.client_socket = client_socket;
	}
	
	/*
	 * Return true if file exists
	 */
	public boolean isFileExists(String path) {
		File file = new File(path);
		if(file.exists() && !file.isDirectory()) { 
		    // do something
			return true;
		} else {
			return false;
		}
		
	}
	
	/*
	 * ReadfromFile
	 */
	public String readFromFile(String path, PrintWriter pr_out)  {
		
		File file = new File(path.substring(1));
		StringBuilder sb = new StringBuilder();
		if(file.canRead()) {
		
			
			try {
			BufferedReader br = new BufferedReader(new FileReader(path.substring(1)));
			String sCurrentLine ;
	
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine);
				sb.append(System.lineSeparator());
			}
			br.close();
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
			return sb.toString();
		} else {
			pr_out.println(RESPONSE.INTERNAL_ERROR.execute());
			pr_out.flush();
			isThreadConnected = false;
		}
		return sb.toString();
	}
	
	/*
	 * @parameter start : Date() - Timer start
	 * @parameter tillWhtSeconds : Int - at what second you want to stop the timer and return true
	 * check if seconds reaches to tillwhtSeconds then return true
	 */
	public boolean isTimeOut(Date start, int tillWhtSeconds) {
		
		while(true) {
			Date current = new Date();
			int difference_seconds = (int) Math.abs(((start.getTime()/ 1000) - (current.getTime()/ 1000)));
			if(difference_seconds > tillWhtSeconds) {
				
				return true;
			}
			//System.out.println(difference_seconds);
		}
	}
	
	
	public void run() {
		
		
		
		Thread timeOutException_Thread = null;
		try {
			
			writer = new OutputStreamWriter(this.client_socket.getOutputStream());
			pr_out = new PrintWriter(writer);
			
			
				reader = new InputStreamReader(this.client_socket.getInputStream());
				br_in = new BufferedReader(reader);
				
				
				/*
				 * TimeOutThread
				 * Check if client does not send a response with in 3 seconds
				 * then send a TIME_OUT response to the client 
				 * close socket
				 */
				timeOutException_Thread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(isTimeOut(new Date(), 2) && !client_socket.isClosed()) {
							
							try {
								writer = new OutputStreamWriter(client_socket.getOutputStream());
								 pr_out = new PrintWriter(writer);
								pr_out.println(RESPONSE.TIME_OUT.execute());
								pr_out.flush();
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								
							} finally {
								try {
									pr_out.close();
									Thread.sleep(250);
									client_socket.close();
								} catch (Exception e) {
									
								}
							}
							
						}
					}
					
				});
				
				//start time_out thread
				timeOutException_Thread.start();
				
				
					
						try {
								
								/*
								 * read client_message
								 * split the message by whitespace
								 * if there is only 2 whitespace then client message is possible for further process, else BAD REQUEST
								 * if command is not GET, then send server 501 Not Implemented 
								 * if path does not start with "/" then, 400 Bad Request
								 * if file does not exists -> 404 Not Found
								 * else there is no error, then create response by reading the file -> 200 OK + "\n\n + filedata + "\n" 	
								 */
								while(isThreadConnected) {
									
									String client_message = br_in.readLine();
									String[] str_arry = null;
									
										  
										
									
									if (client_message != null) {
										
										
										 str_arry = client_message.split(" ");
										
										
										if(str_arry.length == 2 ) {
											//client_message is eligible for further process
											
											String command = str_arry[0];
											String path = str_arry[1];
											if (!command.equals("GET")) {
												//POST instead of GET
												pr_out.println(RESPONSE.NOT_IMPLEMENTED.execute());
												pr_out.flush();
												isThreadConnected = false;
												
											} else if(path.charAt(0) != '/') {
												
												//400 Bad Request
												pr_out.println(RESPONSE.BAD_REQUEST.execute());
												pr_out.flush();
												isThreadConnected = false;
											
											} else if (!isFileExists(path.substring(1))) { 
											
										
												//File does not exists
												pr_out.println(RESPONSE.NOT_FOUND.execute());
												pr_out.flush();
												isThreadConnected = false;
												
											} else {
												
												
												
												//200OK
												//create response 
												
												 String response = RESPONSE.OK.execute() + "\n\n" + readFromFile(path, pr_out) + "\n";
													
													pr_out.println(response);
													pr_out.flush();
													
													isThreadConnected = false;
												
											}
										
										
											
										
										} else {
											//400 Bad Request
											pr_out.println(RESPONSE.BAD_REQUEST.execute());
											pr_out.flush();
											isThreadConnected = false;
											
										}
										
									
										
									} 
									
										
									
								} 
								} catch (SocketTimeoutException e) {
									
									//internal Error 510
									pr_out.println(RESPONSE.INTERNAL_ERROR.execute());
									pr_out.flush();
									isThreadConnected = false;
									//e.printStackTrace();
								} 
								
							
				
			
								
							
			
		} catch (Exception e) {
			//e.printStackTrace();
			
		
		}
		finally {
			try {
				//timeOutException_Thread.interrupt();
				pr_out.close();
				Thread.sleep(250);
				br_in.close();
				this.client_socket.close();
				System.out.println("Connection has been successfully closed on " + client_socket.getInetAddress());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		}
	
}
