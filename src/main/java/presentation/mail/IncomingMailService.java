package presentation.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.*;

import data.Message;
import data.State;

public class IncomingMailService {
	//メンバ変数
	private Socket socket;
	private BufferedReader in;
	private OutputStreamWriter out;
	private State connState;
	private Message oMsg;
	private static final Logger log = LoggerFactory.getLogger(IncomingMailService.class);
	
	//コンストラクタ
	public IncomingMailService(Socket socket) throws IOException{
		try {
			this.socket = socket;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new OutputStreamWriter(socket.getOutputStream());
			connState = new State(socket);
			oMsg = new Message();
			this.sendAck("220"+connState.getServerName()+"SMTP");
			this.conversation();
		}catch(IOException e) {
			log.warn("# Error: " + e.toString());
			socket.close();
		}catch (Exception e) {
			log.warn("# Error: " + e.toString());
			socket.close();
		}
	}
	
	private void sendAck(String acknowledgement) throws IOException{
		try {
			out.write(acknowledgement + "\r\n");
			out.flush();
		} catch (IOException e) {
			log.warn("# Error: " + e.toString());
			socket.close();
		}
		log.info("S :  Send  : [" + acknowledgement + "]");
	}
	
	private void conversation() throws IOException{
		try {
			String line = new String();
			while((line = in.readLine())!=null) {
				log.info("S :Received: [" + line + "]");
				
				// 1. HELO
				if (line.startsWith("HELO")) {
					oMsg.initiate();
					connState.setState("MAIL");
					this.sendAck("250 " + connState.getServerName());
				}
				
				//1'.EHLO
				if (line.startsWith("EHLO")) {
					oMsg.initiate();
					connState.setState("MAIL");
					this.sendAck("250 " + connState.getServerName());
				}
				
				// 2. MAIL
				if (line.startsWith("MAIL")) {
					oMsg.setSender(line);
					// Change the state to RCPT.
					connState.setState("RCPT");
					this.sendAck("250 ok");
				}
				
				// 3. RCPT
				if (line.startsWith("RCPT")) {
					//StateがRCPTのときは、1通目のメールである事を表している
					//StateがDATAのときは、まだ1通以上のメールがある事を表している
					if (connState.getState().equals("RCPT")
							|| connState.getState().equals("DATA")) {
						oMsg.setRecipient(line);
						connState.setState("DATA");
						this.sendAck("250 ok");
					} else {//送信者のアドレスが分からないとき
						this.sendAck("503 MAIL first (#5.5.1)");
					}
				}
				
				// 4. DATA
				if (line.equals("DATA")) {
					if (connState.getState().equals("DATA")) {
						this.sendAck("354 go ahead");
						// ヘッダーの読み込み
						while (true) {
							line = in.readLine();
							oMsg.addHeader(line);
							//subjectをヘッダーから取り出す
							if(line.contains("Subject:"))
								oMsg.setSubject(checkSubject(line));
							// The message header ends with "\r\n", whose length
							// becomes zero in Java strings.
							if (line.length() == 0)
								break;
						}

						// 本文の読み込み
						while (true) {
							line = in.readLine();
							oMsg.addBody(line);
							// The message body ends with "\r\n.\r\n", which is
							// "." in Java strings.
							if (line.equals("."))
								break;
						}
						
						this.setMessage(oMsg);
						// Send this mail to the actual recipients.
						//originalMessage.initiate();
						connState.setState("MAIL");
						this.sendAck("250 ok");
						// The recipient's e-mail address has not been specified
						// yet.
					} else if (connState.getState().equals("RCPT")) {

						this.sendAck("503 RCPT first (#5.5.1)");

						// The sender's e-mail address has not been specified
						// yet.
					} else {
						this.sendAck("503 MAIL first (#5.5.1)");
					}
				}
				
				// 5. QUIT
				if (line.equals("QUIT")) {
					//originalMessage.initiate();

					connState.setState("QUIT");

					this.sendAck("221 " + connState.getServerName());

					System.out.println("S : Closed : ("
							+ connState.getClientName() + ")");
					System.out.println();

					// Close the connection with the client.
					socket.close();

					break;
				}
				
				if (line.equals("RSET")) {
					oMsg.initiate();
					this.sendAck("250 ok");
				}

				// 6. Handle all the requests other than those mentioned above.
//				if (unimplemented) {
//					this.sendAck("502 unimplemented (#5.5.1)");
//
//				}
			}
		}catch (IOException e) {
			log.warn("# Error: " + e.toString());
			socket.close();
		} catch (Exception e) {
			log.warn("# Error: " + e.toString());
			socket.close();
		}
	}
	
	private String checkSubject(String str) {
			int i = str.indexOf(':');
			return str.substring(i + 1);
	}
	
	public void setMessage(Message oMsg) {
		this.oMsg = oMsg;
	}
	
	public Message getMessage() {
		return oMsg;
	}
}
