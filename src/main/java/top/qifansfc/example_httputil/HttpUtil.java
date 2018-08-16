package top.qifansfc.example_httputil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 利用Httpcomponents从Html获取jsoup的document
 * 支持http和https
 */
public class HttpUtil {
	//日志类
	public static Logger LOGGER = Logger.getLogger(HttpUtil.class);
    //请求网址
	private String m_reqUrl;
	//请求方法POST或GET
	private String m_reqMethond;
	//请求头
	private HashMap<String,String> m_reqHeaders;
	//请求体
	private ArrayList<NameValuePair> m_params;
	//请求体编码格式
	private String m_paramsEnCodeMode;
	//返回状态码
	private int m_resStatus;
	//返回头
	private Header[] m_respHeaders;
	//返回内容Document对象
	private Document m_resDoc;
	//设置请求体编码格式
	public void setM_paramsEnCodeMode(String m_paramsEnCodeMode) {
		this.m_paramsEnCodeMode = m_paramsEnCodeMode;
	}
	//添加参数
	public void addParams(String key,String value) {
		try {
			m_params.add(new BasicNameValuePair(key,value));
		}catch(Exception ex) {
			LOGGER.error(ex.getMessage());
		}
	}
	/**
	 * 构造函数，只有网址
	 */
	public HttpUtil(String reqUrl) {
		this(reqUrl,"GET",new HashMap<String,String>());
	}
	/**
	 * 构造函数，只有网址和请求方法
	 */
	public HttpUtil(String reqUrl,String reqMethond) {
		this(reqUrl,reqMethond,new HashMap<String,String>());
	}
	/**
	 * 构造函数，全参数构造
	 */
	public HttpUtil(String reqUrl,String reqMethond,HashMap<String,String> reqHeaders) {
		this.m_reqUrl=reqUrl;
		this.m_reqMethond=reqMethond;
		this.m_reqHeaders=new HashMap<String,String>();
		this.m_params = new ArrayList<NameValuePair>();
		this.m_paramsEnCodeMode="UTF-8";
		if(reqHeaders!=null) {
			if(reqHeaders.size()>0) {
				this.m_reqHeaders.putAll(reqHeaders);
				reqHeaders.clear();
			}else {
				this.m_reqHeaders=new HashMap<String,String>();
			}
		}
//		doHtml();
	}
	/**
	 * Get方法请求，GET方法参数放在Url中，不接收reqBody
	 * @return
	 */
	private Boolean doGet() {
		Boolean result=false;
        try {
        	//开始初始化参数
			CloseableHttpClient httpClient = HttpClients.createDefault();
	        CloseableHttpResponse httpResponse = null;
	        //设置请求体，添加参数
	        String queryString = URLEncodedUtils.format(this.m_params,this.m_paramsEnCodeMode);
	        HttpGet httpGet=new HttpGet(m_reqUrl+"?"+queryString);			
	        //设置请求头
	        for (String key : m_reqHeaders.keySet()) {
	        	httpGet.setHeader(key, m_reqHeaders.get(key));
	        }
			//开始请求并获取返回
	        httpResponse = httpClient.execute(httpGet);
			//获取状态码
	        m_resStatus=httpResponse.getStatusLine().getStatusCode();
			if (m_resStatus == 200) {
				m_respHeaders=httpResponse.getAllHeaders();
			    HttpEntity entity = httpResponse.getEntity();
			    InputStream in = entity.getContent();
		    	m_resDoc=Jsoup.parse(readResponse(in,getRespModeFromRespHeader()));
		    	result=true;
			}else {
				LOGGER.error("访问失败，状态码为："+m_resStatus);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * Post方法请求
	 * @return
	 */
	private Boolean doPost() {
		Boolean result=false;
        try {
        	//开始初始化参数
			CloseableHttpClient httpClient = HttpClients.createDefault();
	        CloseableHttpResponse httpResponse = null;
			HttpPost httpPost=new HttpPost(m_reqUrl);
			//设置请求头
	        for (String key : m_reqHeaders.keySet()) {
	        	httpPost.setHeader(key, m_reqHeaders.get(key));
	        }
	        //设置请求体,添加参数
			httpPost.setEntity(new UrlEncodedFormEntity(this.m_params,this.m_paramsEnCodeMode));
			//开始请求并获取返回
			httpResponse = httpClient.execute(httpPost);
			//获取状态码
			m_resStatus=httpResponse.getStatusLine().getStatusCode();
			if (m_resStatus == 200) {			
				m_respHeaders=httpResponse.getAllHeaders();
			    HttpEntity entity = httpResponse.getEntity();
			    InputStream in = entity.getContent();
		    	m_resDoc=Jsoup.parse(readResponse(in,getRespModeFromRespHeader()));
		    	result=true;
			}else {
				LOGGER.error("访问失败，状态码为："+m_resStatus);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.toString());
			e.printStackTrace();
		}
		return result;		
	}
	/**
	 * 核心函数
	 * @return
	 */
	public Boolean doHtml() {
		Boolean result=false;
		if(m_reqMethond.equalsIgnoreCase("GET")) {
			if(doGet()) {
				result=true;
			}
		}else if(m_reqMethond.equalsIgnoreCase("POST")){
			if(doPost()) {
				result=true;
			}
		}else {
			LOGGER.error("请求方法非POST或GET，不予处理");
		}
		return result;
	}
	//获取返回的文档上
	public Document getResDoc() {
		return m_resDoc;
	}
	//获取返回的请求头
	public HashMap<String, String> getRespHeaders() {
		HashMap<String, String> result=new HashMap<String, String>();
		int index = 0;
		while (index < m_respHeaders.length) {
			result.put(m_respHeaders[index].getName(), m_respHeaders[index].getValue());
			++index;
		}
		return result;
	}
	//获取返回的请求头
	public Header[] getRespHeaders2() {
		return this.m_respHeaders;
	}
	/**
	 * 根据返回的响应头获取编码格式
	 */
	private String getRespModeFromRespHeader() {
		String result="";
		String eMode=getRespHeaders().get("Content-Type");
		if(eMode.indexOf("GBK")!=-1) {
			result="GBK";
		}else if(eMode.indexOf("UTF-16")!=-1){
			result="UTF-16";
		}else {
			result="UTF-8";
		}
		return result;		
	}
	//获取返回状态
	public int getResStatus() {
		return m_resStatus;
	}
	/**
	 * 将输入流转化为字符串
	 * @param in
	 * @return
	 */
    private String readResponse(InputStream in,String encodeMode){
    	String result=null;
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,encodeMode));
			while ((line = reader.readLine()) != null) {
				result+=line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return result;
    }
}