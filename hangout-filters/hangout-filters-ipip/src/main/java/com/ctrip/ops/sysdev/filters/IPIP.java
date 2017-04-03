package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ipip.IP;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IPIP extends BaseFilter {
    private static final Logger logger = Logger.getLogger(IPIP.class.getName());

    public IPIP(Map config) {
        super(config);
    }

    private String source;
    private String target;

    private final static String[] areaNames = {"country", "province", "city", "district"};


    protected void prepare() {
        if (!config.containsKey("source")) {
            logger.error("no field configured in Json");
            System.exit(1);
        }
        this.source = (String) config.get("source");

        if (config.containsKey("target")) {
            this.target = (String) config.get("target");
        } else {
            this.target = "area";
        }

        if (config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "ipfail";
        }


        String database = "";
        // A File object pointing to your GeoIP2 or GeoLite2 database
        if (config.containsKey("database")) {
            database = (String) config.get("database");
        } else {
            logger.error("no database configured in IPIP");
            System.exit(1);
        }

        try {
            IP.enableFileWatch = true;
            IP.load(database);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Map filter(final Map event) {
        InetAddress ipAddress;

        String ip = (String) event.get(source);
        List<String> areas = Arrays.asList(IP.find(ip));

        HashMap<String, String> targetObj = new HashMap();

        int idx = 0;
        int maxIdx = areaNames.length - 1;
        for (String v : areas) {
            String areaName = areaNames[idx++];

            if (v.equals("") || v.isEmpty()) {
                continue;
            }

            targetObj.put(areaName, v);
            if (idx > maxIdx) {
                break;
            }
        }

        if (this.target != null) {
            event.put(this.target, targetObj);
        } else {
            event.putAll(targetObj);
        }

        return event;
    }
}
