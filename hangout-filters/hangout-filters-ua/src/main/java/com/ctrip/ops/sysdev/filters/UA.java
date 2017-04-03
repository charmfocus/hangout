package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import org.apache.log4j.Logger;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UA extends BaseFilter {
	private static final Logger logger = Logger.getLogger(UA.class.getName());

	public UA(Map config) {
		super(config);
	}

	private String source;
	private String target;
	private Parser uaParser;

	protected void prepare() {
		if (!config.containsKey("source")) {
			logger.error("no field configured in Json");
			System.exit(1);
		}
		this.source = (String) config.get("source");

		if (config.containsKey("target")) {
			this.target = (String) config.get("target");
		} else {
			this.target = "ua";
		}

		try {
			this.uaParser = new Parser();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
			System.exit(1);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected Map filter(final Map event) {
		HashMap targetObj = new HashMap();
		if (event.containsKey(this.source)) {
			Client c = uaParser.parse((String) event.get(this.source));
			targetObj.put("userAgent_family", c.userAgent.family);
			targetObj.put("userAgent_major", c.userAgent.major);
			targetObj.put("userAgent_minor", c.userAgent.minor);
			targetObj.put("os_family", c.os.family);
			targetObj.put("os_major", c.os.major);
			targetObj.put("os_minor", c.os.minor);
			targetObj.put("device_family", c.device.family);
		}

		if (this.target != null) {
			event.put(this.target, targetObj);
		} else {
			event.putAll(targetObj);
		}

		return event;
	}
}
