package com.playlet.internal.constants;


/**
 * 
 * @author csz
 *
 */
public interface Constants {
			
    public static final String WWW = "www.";

    public static final String HTTP = "http://";

    public static final String HTTPS = "https://";
    
    public static final String APP_PACKAGE_NAME = "playletInternalServer";
                
	String  SUCCESS="操作成功";	
	
	String EXCEPTION_MSG = "系统繁忙，请稍后再试！！！";
			
	int PAGENUMBER = 1;
	
	int PAGESIZE =10;
	
	Integer ZERO_INT = 0;
			
	long REDIS_EXPIRE_TIME = 24 * 60 * 60 * 1000;

	Integer USER_JWT_EXPIRE_TIME = 240 * 60 * 60 * 1000;

	long USER_REDIS_EXPIRE_TIME = 240 * 60 * 60 * 1000;
	
	long CODE_EXPIRE_TIME = 5 * 60;
			
	// 响应请求成功code
	Integer HTTP_RES_CODE_200 = 200;
	
	Integer HTTP_RES_CODE_201 = 201;
	
	// 系统错误
	Integer HTTP_RES_CODE_500 = 500;
	
	Integer HTTP_RES_CODE_501 = 501;
	
	Integer HTTP_RES_CODE_502 = 502;
	
	Integer HTTP_RES_CODE_503 = 503;

	Integer HTTP_RES_CODE_404 = 404;
	
	Integer HTTP_RES_CODE_403 = 403;
	
	Integer HTTP_RES_CODE_400 = 400;
	
	Integer HTTP_RES_CODE_601 = 601;

	Integer HTTP_RES_CODE_602 = 602;
	
    /**
     * AUTH_HEADER_START_WITH 请求中token前缀
     */
    public static final String AUTH_HEADER_START_WITH = "Bearer ";

    /**
     * SIGNING_KEY token生成密钥
     */
    public static final String SIGNING_KEY = "+1ffZjZoqBAaJQSMJ66HCzKKoiigbWPDWatYgL59hzSyqv6GBZi/YzepeLiHch3i7DmSRevH9IufZin/Fvanrg==";

    /**
     * HEADER_AUTH 请求header中token的key
     */
    public static final String HEADER_AUTH = "x-playlet-token";
    
    public static final String HEADER_CURRENCY = "x-playlet-currency";
    
    public static final String HEADER_DEVICETYPE = "x-playlet-devicetype";
    
    public static final String HEADER_VERSION = "x-playlet-version";
    
    public static final String HEADER_LANGUAGE = "x-mergpay-language";
    
    
    //配置前端响应头
    public static final String HEADER_ACCESS = "Access-Control-Expose-Headers";
    
    public static final String user_googleKey = "3EDUIKMIXDQIZQIH";
    
    public static final String GOOGLE_COCE = "googleCoce";
    
    

}
