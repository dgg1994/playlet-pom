package com.playlet.internal.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 导航开关出参：库值仅 1 序列化为 true，其它为 false。
 */
public class OneAsTrueJsonSerializer extends JsonSerializer<Integer> {

	@Override
	public void serialize(Integer value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeBoolean(value != null && value == 1);
	}
}
