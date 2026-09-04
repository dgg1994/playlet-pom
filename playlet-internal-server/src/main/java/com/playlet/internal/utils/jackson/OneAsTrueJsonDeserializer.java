package com.playlet.internal.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 导航开关入参：true/1 → 1，其它 → 2。
 */
public class OneAsTrueJsonDeserializer extends JsonDeserializer<Integer> {

	@Override
	public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonToken token = p.getCurrentToken();
		if (token == JsonToken.VALUE_TRUE) {
			return 1;
		}
		if (token == JsonToken.VALUE_FALSE) {
			return 2;
		}
		if (token != null && token.isNumeric()) {
			return p.getIntValue() == 1 ? 1 : 2;
		}
		String text = p.getValueAsString();
		if ("1".equals(text) || "true".equalsIgnoreCase(text)) {
			return 1;
		}
		return 2;
	}
}
