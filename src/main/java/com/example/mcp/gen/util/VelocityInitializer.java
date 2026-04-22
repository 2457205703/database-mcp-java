package com.example.mcp.gen.util;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;

import java.util.Properties;

public final class VelocityInitializer {

    private static volatile boolean initialized = false;

    private VelocityInitializer() {}

    public static void init() {
        if (initialized) return;
        synchronized (VelocityInitializer.class) {
            if (initialized) return;
            Properties p = new Properties();
            p.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
            p.setProperty("resource.loader.classpath.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            p.setProperty("resource.loader.classpath.cache", "false");
            p.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
            p.setProperty("output.encoding", "UTF-8");
            p.setProperty("runtime.log.logsystem.class",
                "org.apache.velocity.runtime.log.NullLogChute");
            Velocity.init(p);
            initialized = true;
        }
    }
}
