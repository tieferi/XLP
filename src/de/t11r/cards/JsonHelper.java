// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonHelper {

	public static Parser parser(File file) throws Exception {
		return new Parser(Json.createParser(new FileReader(file)));
	}
	
	public static class Parser {

		private JsonParser parser;
		private Event event;
		
		private Parser(JsonParser parser) {
			this.parser = parser;
			next();
		}
		
		public void close() {
			parser.close();
		}
		
		private void next() {
			if (parser.hasNext()) {
				event = parser.next();
			} else {
				event = null;
			}
		}
		
		private boolean event(Event event) {
			if (this.event == event) {
				next();
				return true;
			} else {
				return false;
			}
		}
		
		public boolean startObject() {
			return event(Event.START_OBJECT);
		}
		
		public boolean endObject() {
			return event(Event.END_OBJECT);
		}
		
		public boolean startArray() {
			return event(Event.START_ARRAY);
		}
		
		public boolean endArray() {
			return event(Event.END_ARRAY);
		}
		
		public boolean end() {
			return event == null;
		}
		
		public boolean key(String key) {
			if (this.event == Event.KEY_NAME && parser.getString().equals(key)) {
				next();
				return true;
			} else {
				return false;
			}
		}
		
		public String key() {
			String key = null;
			if (event == Event.KEY_NAME) {
				key = parser.getString();
			} else {
				fail();
			}
			next();
			return key;
		}

		public boolean value() {
			return event == Event.VALUE_STRING || event == Event.VALUE_NUMBER
				|| event == Event.VALUE_FALSE || event == Event.VALUE_TRUE || event == Event.VALUE_NULL;
		}

		public String stringValue() {
			String value = null;
			if (event == Event.VALUE_STRING) {
				value = parser.getString();
			} else if (event != Event.VALUE_NULL) {
				fail();
			}
			next();
			return value;
		}
		
		public Integer integerValue() {
			Integer value = null;
			if (event == Event.VALUE_NUMBER) {
				value = parser.getInt();
			} else if (event != Event.VALUE_NULL) {
				fail();
			}
			next();
			return value;
		}
		
		public Boolean booleanValue() {
			Boolean value = null;
			if (event == Event.VALUE_TRUE) {
				value = Boolean.TRUE;
			} else if (event == Event.VALUE_FALSE) {
				value = Boolean.FALSE;
			} else if (event != Event.VALUE_NULL) {
				fail();
			}
			next();
			return value;
		}

		public void skipKey() {
			if (event == Event.KEY_NAME) {
				next();
			} else {
				fail();
			}
		}

		public void skipValue() {
			if (value()) {
				next();
			} else if (event == Event.START_OBJECT) {
				parser.skipObject();
				next();
			} else if (event == Event.START_ARRAY) {
				parser.skipArray();
				next();
			} else {
				fail();
			}
		}
		
		public void fail() {
			throw new RuntimeException(); 
		}
	}

	public static JsonGenerator generator(File file) throws Exception {
		Map<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
		return Json.createGeneratorFactory(config).createGenerator(new FileWriter(file));
	}
}
