package com.puppet.pcore.impl.serialization.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppet.pcore.impl.serialization.SerializationFactoryImpl;
import com.puppet.pcore.serialization.Reader;
import com.puppet.pcore.serialization.Writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

public class JsonSerializationFactory extends SerializationFactoryImpl {
	static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public Reader readerOn(InputStream in) throws IOException {
		return new JsonReader(new JsonUnpacker(in));
	}

	@Override
	public Reader reader() {
		return new JsonReader(new JsonUnpacker(Collections.emptyList()));
	}

	@Override
	public Writer writerOn(Map<String,Object> options, OutputStream out) throws IOException {
		return new JsonWriter(options, new JsonPacker(out, options));
	}
}
