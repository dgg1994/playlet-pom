package com.playlet.internal.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * 字符串字段反序列化：兼容 JSON 字符串与 JSON 数组/对象两种入参。
 * <p>
 * 前端把 tags、attachments 这类字段直接以数组提交时，若目标字段为 String，
 * Jackson 默认会抛 MismatchedInputException，被全局异常处理器包装成“JSON 格式错误”。
 * 使用本反序列化器后，数组/对象节点会被序列化为字符串存入，文本节点原样返回。
 */
public class JsonFlexibleStringDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		JsonNode node = parser.readValueAsTree();
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isValueNode()) {
			return node.asText();
		}
		return node.toString();
	}
}
