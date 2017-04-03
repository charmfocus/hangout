package com.ctrip.ops.sysdev.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONObject;

public class DateFormatter {
	private static final Logger log = Logger.getLogger(DateFormatter.class
			.getName());
	private static Pattern p = Pattern.compile("(\\%\\{.*?\\})");
	private static DateTimeFormatter ISOformatter = ISODateTimeFormat
			.dateTimeParser().withOffsetParsed();

	public static String format(Map event, String format, String Timezone) {
		Matcher m = p.matcher(format);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String match = m.group();
			String key = match.substring(2, match.length() - 1);
			if (key.equalsIgnoreCase("+s")) {
				Object o = event.get("@timestamp");
				if (o.getClass() == Long.class) {
					m.appendReplacement(sb, o.toString());
				}
			} else if (key.startsWith("+")) {
				DateTimeFormatter formatter = DateTimeFormat.forPattern(
						key.substring(1, key.length())).withZone(
						DateTimeZone.forID(Timezone));
				Object o = event.get("@timestamp");
				if (o == null) {
					DateTime timestamp = new DateTime();
					m.appendReplacement(sb, timestamp.toString(formatter));
				} else {
					if (o.getClass() == DateTime.class) {
						m.appendReplacement(sb,
								((DateTime) o).toString(formatter));
					} else if (o.getClass() == Long.class) {
						DateTime timestamp = new DateTime((Long) o);
						m.appendReplacement(sb, timestamp.toString(formatter));
					} else if (o.getClass() == String.class) {
						DateTime timestamp = ISOformatter
								.parseDateTime((String) o);
						m.appendReplacement(sb, timestamp.toString(formatter));
					}
				}
			} else if (event.containsKey(key)) {
				m.appendReplacement(sb, (String) event.get(key));
			} else if(key.indexOf(".") > 0) {
				String[] keys = key.split("\\.");
				if (event.containsKey(keys[0])) {
					Object _val = event.get(keys[0]);
					for (int i = 1; i < keys.length; ++i) {
                        if (!(_val instanceof HashMap)) {
                            break;
						}
						_val = ((Map)_val).get(keys[i]);
					}

                    m.appendReplacement(sb, String.valueOf(_val));
				}

			}

		}
		m.appendTail(sb);

		return sb.toString();
	}
}
