package com.puppet.pcore.impl.serialization;

import com.puppet.pcore.Binary;
import com.puppet.pcore.Comment;
import com.puppet.pcore.Default;
import com.puppet.pcore.Symbol;
import com.puppet.pcore.impl.Helpers;
import com.puppet.pcore.impl.serialization.extension.*;
import com.puppet.pcore.regex.Regexp;
import com.puppet.pcore.semver.Version;
import com.puppet.pcore.semver.VersionRange;
import com.puppet.pcore.serialization.Reader;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.puppet.pcore.impl.serialization.extension.Numbers.*;
import static com.puppet.pcore.impl.types.TypeFactory.typeReferenceType;

public abstract class AbstractReader implements Reader {
	private final ArrayList<Object> objectsRead = new ArrayList<>();
	protected final ExtensionAwareUnpacker unpacker;

	protected AbstractReader(ExtensionAwareUnpacker unpacker) {
		this.unpacker = unpacker;
		registerTypes();
	}

	@Override
	public void initialize(List<?> data) {
		throw new UnsupportedOperationException(getClass().getName() + " does not support partial read of data chunks");
	}

	@Override
	public Object read() throws IOException {
		Object obj = unpacker.read();
		if(obj instanceof InnerTabulation)
			return objectsRead.get(((InnerTabulation)obj).index);
		if(obj == null || obj instanceof Number || obj instanceof NotTabulated || obj instanceof Boolean)
			return obj;
		objectsRead.add(obj);
		return obj;
	}

	protected <T> T readPayload(byte[] data, IOFunction<ExtensionAwareUnpacker,T> block) throws IOException {
		return block.apply(unpacker);
	}

	private String readPayloadQName(ExtensionAwareUnpacker ep) throws IOException {
		int segmentCount = ep.readInt();
		String[] segments = new String[segmentCount];
		for(int idx = 0; idx < segmentCount; ++idx)
			segments[idx] = readPayloadString(ep);
		return Helpers.join("::", Arrays.asList(segments));
	}

	private String readPayloadString(ExtensionAwareUnpacker ep) throws IOException {
		Object obj = ep.read();
		if(obj instanceof Long)
			return (String)objectsRead.get(((Number)obj).intValue());
		objectsRead.add(obj);
		return (String)obj;
	}

	private <T> void registerType(byte extensionNumber, PayloadReaderFunction<T> payloadReaderFunction) {
		unpacker.registerType(extensionNumber, payloadReaderFunction);
	}

	private void registerTypes() {
		registerType(INNER_TABULATION, data -> readPayload(data, ep -> new InnerTabulation(ep.readInt())));
		registerType(TABULATION, data -> readPayload(data, ep -> new Tabulation(ep.readInt())));
		registerType(ARRAY_START, data -> readPayload(data, ep -> new ArrayStart(ep.readInt())));
		registerType(MAP_START, data -> readPayload(data, ep -> new MapStart(ep.readInt())));
		registerType(PCORE_OBJECT_START, data -> readPayload(data, ep -> new PcoreObjectStart(readPayloadQName(ep), ep.readInt())));
		registerType(OBJECT_START, data -> readPayload(data, ep -> new ObjectStart(ep.readInt())));
		registerType(SENSITIVE_START, data -> readPayload(data, ep -> SensitiveStart.SINGLETON));
		registerType(DEFAULT, data -> readPayload(data, ep -> Default.SINGLETON));
		registerType(COMMENT, data -> readPayload(data, ep -> new Comment(ep.readString())));
		registerType(REGEXP, data -> readPayload(data, ep -> Regexp.compile(ep.readString())));
		registerType(TYPE_REFERENCE, data -> readPayload(data, ep -> typeReferenceType(ep.readString())));
		registerType(SYMBOL, data -> readPayload(data, ep -> new Symbol(ep.readString())));
		registerType(TIME, data -> readPayload(data, ep -> {
			long sec = ep.readLong();
			long nsec = ep.readLong();
			return Instant.ofEpochSecond(sec, nsec);
		}));
		registerType(TIMESPAN, data -> readPayload(data, ep -> {
			long sec = ep.readLong();
			long nsec = ep.readLong();
			return Duration.ofNanos(sec * 1000000000 + nsec);
		}));
		registerType(VERSION, data -> readPayload(data, ep -> Version.create(ep.readString())));
		registerType(VERSION_RANGE, data -> readPayload(data, ep -> VersionRange.create(ep.readString())));
		registerType(BINARY, data -> readPayload(data, ep -> new Binary(ep.readBytes())));
		registerType(BASE64, data -> readPayload(data, ep -> Binary.fromBase64Strict(ep.readString())));
	}
}
