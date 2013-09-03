package cn.btbu.netlogin;

/**
 * 全局配置
 * @author zcj
 *
 */
public class Config {
	public static final int SUCCESS_LOGIN=1;
	public static final int SUCCESS_OFF_LINE=3;
	public static final int CHANGETIME=2;
	
	
	/**
	 * 查询
	 */
	public static final String SEARCH="cinfo=%u67E5%u8BE2&einfo=Account%20Info&chgpwd=%u4FEE%u6539%u5BC6%u7801&logout=%u65AD%u5F00&login=%u767B%u5F55&netlogincmd=3&proxyip=127.0.0.1&newpassword=&password=*#pwd#*&account=*#count#*";
	
	/**
	 * 修改密码
	 */
	public static final String MODIFY_UP="cinfo=%u67E5%u8BE2&einfo=Account%20Info&chgpwd=%u4FEE%u6539%u5BC6%u7801&logout=%u65AD%u5F00&login=%u767B%u5F55&netlogincmd=2&proxyip=127.0.0.1&newpassword=*#newpwd#*&password=*#pwd#*&account=*#count#*";
	
	/**
	 * 更改计费方式
	 */
	public static final String CC_1="textfield=*#count#*&textfield2=*#pwd#*&Submit=%CC%E1%BD%BB&nacgicmd=0";
	public static final String CC_2="textfield=*#count#*&textfield2=*#pwd#*&Submit=%CC%E1%BD%BB&nacgicmd=4";
	public static final String CC_3="textfield=*#count#*&textfield2=*#pwd#*&jsidx=1&radio=2&Submit=%CC%E1%BD%BB&nacgicmd=2";
	public static final String CC_4="textfield=*#count#*&textfield2=*#pwd#*&jsidx=2&radio=2&Submit=%CC%E1%BD%BB&nacgicmd=2";
	
	/**
	*强制下线
	*/
	public static final String CC_5="textfield=*#count#*&textfield2=*#pwd#*&Submit=%CC%E1%BD%BB&nacgicmd=9&radio=1&jsidx=1"; 
	
	public static final String PWDSIGN="*#pwd#*";
	public static final String COUNTSIGN="*#count#*";
	public static final String NEWPWDSIGN="*#newpwd#*";
	public static final String SUCCESS_SEARCH_ADD_SIGN=" S222.";
	public static final String SUCCESS_SEARCH="S222";
	public static final String SUCCESS_MODIFY="S202";
	public static final String REPLACESTRING1="<br><pre>";
	public static final String REPLACESTRING2="</pre>";
	protected static final int FAIL_OFF_LINE = -1;
	protected static final int OFF_LINE_SIGN = 100;
}
