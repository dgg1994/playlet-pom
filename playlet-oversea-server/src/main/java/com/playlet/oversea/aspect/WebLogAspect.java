package com.playlet.oversea.aspect;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.playlet.oversea.base.ResponseBase;

import lombok.extern.slf4j.Slf4j;

/**
 * @category 日志统一管理（敏感字段脱敏）
 * @author csz
 *
 */
@Aspect
@Component
@Slf4j
public class WebLogAspect {

	private static final Set<String> SENSITIVE_KEYS = new HashSet<>();
	static {
		SENSITIVE_KEYS.add("password");
		SENSITIVE_KEYS.add("userpassword");
		SENSITIVE_KEYS.add("paypassword");
		SENSITIVE_KEYS.add("formerpassword");
		SENSITIVE_KEYS.add("newpassword");
		SENSITIVE_KEYS.add("oldpassword");
		SENSITIVE_KEYS.add("googlecode");
		SENSITIVE_KEYS.add("googlesecretkey");
		SENSITIVE_KEYS.add("emailcode");
		SENSITIVE_KEYS.add("telcode");
		SENSITIVE_KEYS.add("smscode");
		SENSITIVE_KEYS.add("token");
		SENSITIVE_KEYS.add("idtoken");
		SENSITIVE_KEYS.add("secret");
		SENSITIVE_KEYS.add("secretkey");
		SENSITIVE_KEYS.add("master_secret");
		SENSITIVE_KEYS.add("access-key");
		SENSITIVE_KEYS.add("accesskey");
	}

    @Pointcut("execution(public * com.playlet.oversea.service..*.*(..))")
    public void webLog() {}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null) {
        	 HttpServletRequest request = attributes.getRequest();
             log.info("##################### 请求开始 ####################");
             log.info("URL : " + request.getRequestURL().toString());
             log.info("HTTP_METHOD : " + request.getMethod());
             log.info("IP : " + request.getRemoteAddr());
             Object[] args = joinPoint.getArgs();
             if (args != null && args.length > 0 && args[0] != null) {
                 try {
                     String parameterJson = maskSensitiveJson(JSON.toJSONString(args[0]));
                     log.info("PARAMETER：" + request.getRequestURL() + "-[" + parameterJson + "]");
                 } catch (JSONException e) {
                     log.error("PARAMETER：" + request.getRequestURL() + "-[]");
                 } catch (Exception e) {
                     log.info("PARAMETER：" + request.getRequestURL() + "-[unserializable]");
                 }
             }

             Enumeration<String> enu = request.getParameterNames();
             while (enu.hasMoreElements()) {
                 String name = enu.nextElement();
                 String value = request.getParameter(name);
                 if (isSensitiveKey(name)) {
                     value = "***";
                 }
                 log.info("name:{" + name + "},value:{" + value + "}");
             }

        }
       
    }
    
    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(JoinPoint point, Object ret) throws Throwable {
        
        String type = point.getSignature().toLongString().split(" ")[1];
        if (type != null && !"void".equals(type) && !"String".equals(type)) {
            try {
                if (ret != null) {
                    // 判断 ret 是否是字符串类型
                    if (ret instanceof String) {
                        log.info("RESPONSE (String) : " + ret);
                    } 
                    // ✅ 增加对 Boolean 类型的判断
                    else if (ret instanceof Boolean) {
                        log.info("RESPONSE (Boolean) : " + ret);
                    }
                    // ✅ 增加对基本类型的判断
                    else if (ret instanceof Number) {
                        log.info("RESPONSE (Number) : " + ret);
                    }
                    else {
                        // 只有当 ret 是对象时才尝试解析为 ResponseBase
                        try {
                            ResponseBase base = JSONObject.parseObject(JSON.toJSONString(ret), ResponseBase.class);
                            log.info("RESPONSE11 : " + point.getSignature().toString().substring(point.getSignature().toString().indexOf(" ")) + "-" + base.getMsg());
                        } catch (Exception e) {
                            // 如果不是 ResponseBase 对象，直接打印（脱敏）
                            log.info("RESPONSE : " + maskSensitiveJson(JSON.toJSONString(ret)));
                        }
                    }
                }
            } catch (JSONException e) {
                log.error("JSON解析错误", e);
            }
        }
        
        log.info("##################### 请求结束 ####################");
    }

	private static boolean isSensitiveKey(String key) {
		if (key == null) {
			return false;
		}
		String k = key.toLowerCase(Locale.ROOT).replace("_", "");
		if (SENSITIVE_KEYS.contains(k)) {
			return true;
		}
		return k.contains("password") || k.contains("secret") || k.contains("token");
	}

	private static String maskSensitiveJson(String json) {
		if (json == null || json.isEmpty()) {
			return json;
		}
		try {
			Object parsed = JSON.parse(json);
			maskNode(parsed);
			return JSON.toJSONString(parsed);
		} catch (Exception e) {
			return "[masked]";
		}
	}

	@SuppressWarnings("unchecked")
	private static void maskNode(Object node) {
		if (node instanceof JSONObject) {
			JSONObject obj = (JSONObject) node;
			for (Map.Entry<String, Object> e : obj.entrySet()) {
				if (isSensitiveKey(e.getKey())) {
					obj.put(e.getKey(), "***");
				} else {
					maskNode(e.getValue());
				}
			}
		} else if (node instanceof JSONArray) {
			JSONArray arr = (JSONArray) node;
			for (Object o : arr) {
				maskNode(o);
			}
		} else if (node instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) node;
			for (Map.Entry<String, Object> e : map.entrySet()) {
				if (isSensitiveKey(e.getKey())) {
					e.setValue("***");
				} else {
					maskNode(e.getValue());
				}
			}
		}
	}

}
