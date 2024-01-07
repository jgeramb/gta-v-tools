package dev.justix.gtavtools.util;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.StreamSupport;

public class DNSUtil {

    private static final DirContext CONTEXT;

    static {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        DirContext context = null;

        try {
            context = new InitialDirContext(properties);
        } catch (NamingException ignore) {
        }

        CONTEXT = context;
    }

    private static Attribute getAttributes(String host, String recordType) throws NamingException {
        return CONTEXT.getAttributes(host, new String[] { recordType }).get(recordType);
    }

    private static List<String> getRecords(String host, String recordType) {
        try {
            final Iterator<?> iterator = getAttributes(host, recordType).getAll().asIterator();
            final Iterable<Object> iterable = () -> (Iterator<Object>) iterator;

            return StreamSupport
                    .stream(iterable.spliterator(), false)
                    .map(Object::toString)
                    .toList();
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
    }

    public static List<String> getCNAMERecords(String host) {
        return getRecords(host, "CNAME");
    }

    public static List<String> getARecords(String host) {
        return getRecords(host, "A");
    }

}
