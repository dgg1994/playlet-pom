package com.playlet.oversea.aop;

import java.lang.annotation.*;

/**
 *  excel导出注解
 * @author HL
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelAnnotation {
	String value() default "";//表头
}
