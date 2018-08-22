package top.qifansfc.example_httputil;

import org.apache.log4j.Logger;

public class Test {

	//日志类
	public static Logger LOGGER = Logger.getLogger(Test.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HttpUtil httpUtil=new HttpUtil("https://www.baidu.com");
		httpUtil.doHtml();
		LOGGER.info(httpUtil.getResDoc().html());
		LOGGER.info("test1");
		LOGGER.info("test2");
		LOGGER.info("test334");
	}
}
