package logic.parse;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import presentation.mail.IncomingMailService;
import data.Message;
import data.PropFile;
import logic.flow.AnswerFlow;
import logic.flow.FunctionFlow;
import logic.flow.TransferFlow;
import logic.parse.SOP.parserVisitor;
import logic.utils.ParseUtils;

public class Distributor implements Runnable{
	private String recipient;
	private String domain;
	private String subdomain;
	private String propfile;
	/** delivery rules ex.) name, grade */
	public ArrayList<String> keys;
	/** delivery parameters */
	public ArrayList<String> values;
	/** subdomain files ex.)rmx.properties */
	private ResourceBundle domBundle;
	private HashMap<String, ResourceBundle> domBundles;
	/**subdomain,domain,prop_file's map*/
	private ArrayList<HashMap<String, String>> domainsMaps;
	private Socket socket;
	private ResourceBundle envBundle;
	private static final Logger log = LoggerFactory.getLogger(Distributor.class);
	private PropFile pf = PropFile.getInstance();

	/**
	 * tree parser created by obunai
	 */
	private User user;
	private User1 user1;
	private parserVisitor userInfo;
	
	boolean transfer_flg;
	boolean answer_flg;
	boolean mixture_flg;
	boolean function_flg;
	
	//コンストラクタ
	public Distributor(Socket socket) {
		try {
			this.socket = socket;
			this.envBundle = pf.getEnvBundle();
			this.domainsMaps = pf.getDomainsMaps();
			domBundles = pf.getDomBundles();
			keys = new ArrayList<String>();
			values = new ArrayList<String>();
			domain = new String();
			subdomain = new String();
			propfile = new String();
			user = new User();
			user1 = new User1();
			transfer_flg = false;
			answer_flg = false;
			mixture_flg = false;
			function_flg = false;
		} catch (NullPointerException E) {
			System.out.println("# Error: " + E.toString());
			System.exit(-1);
		} catch (MissingResourceException E) {
			System.out.println("# Error: " + E.toString());
			System.exit(-1);
		}
	}
	
	@Override
	public void run() {
		parse();
	}
	
	//宛先に応じて処理を振り分ける
	public void parse() {
		try {
			//送られてきたメールをオブジェクトとして得る
			Message oMsg = new Message();
			IncomingMailService icm = new IncomingMailService(socket);
			oMsg = icm.getMessage();
			
			//宛先を得る
			recipient = oMsg.getRecipient();
			
			//自然形式or関数形式のどちらでパースするか決定する
			String recipientForm = ParseUtils.whichForm(recipient);
			
			//関数形式について
			if(recipientForm.equalsIgnoreCase("functionalForm")) {
				domain = ParseUtils.getDomainOfFunctional(recipient);
				subdomain = ParseUtils.getSubdomainOfFunctional(recipient);
				propfile = ParseUtils.getPropfileName(domain, subdomain, domainsMaps);
				domBundle = ParseUtils.searchDomBundle(domain, subdomain, domBundles);
				user.UserStart(recipient, domBundle);
				userInfo = user; //userInfoにはuserとuser1の両方を格納できる(ポリモーフィズム)
			}
			//自然形式について
			else {
				domain = ParseUtils.getDomainOfNatural(recipient);
				System.out.println("@"+domain);
				subdomain = ParseUtils.getSubdomainOfNatural(recipient);
				propfile = ParseUtils.getPropfileName(domain, subdomain, domainsMaps);
				domBundle = ParseUtils.searchDomBundle(domain, subdomain, domBundles);
				user1.User1Start(recipient, domBundle, domain);
				userInfo = user1; //userInfoにはuserとuser1の両方を格納できる(ポリモーフィズム)
			}
			
			
			//関数もしくは自然形式の宛先のとき
			if(userInfo.getNormalFlg()) {
				transfer_flg = ParseUtils.checkForTransfer(userInfo.getKeys(), domBundle);
				answer_flg = ParseUtils.checkForAnswer(userInfo.getKeys(), domBundle);
				mixture_flg = ParseUtils.checkForMixture(userInfo.getKeys(), domBundle);
			}
			//#形式のとき
			else if(userInfo.getFunctionFlg()) {
				function_flg = true;
			}
			
			//それぞれのフラグに応じてflowへ飛ばす
			if(transfer_flg) {
				TransferFlow t_flow = new TransferFlow(oMsg, domBundle, userInfo);
				t_flow.startTransfer();
			}else if(answer_flg) {
				AnswerFlow a_flow = new AnswerFlow(oMsg, domBundle, userInfo);
				a_flow.startAnswer();
			}else if(mixture_flg) {
				
			}else if(function_flg) {
				FunctionFlow f_flow = new FunctionFlow(oMsg, domBundle, userInfo, propfile, domain);
				f_flow.startFunction();
			}else {
				
			}
		}catch(Exception e) {
			
		}
	}
	
}
