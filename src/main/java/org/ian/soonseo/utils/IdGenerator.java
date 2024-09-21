package org.ian.soonseo.utils;

import java.util.concurrent.ThreadLocalRandom;

public class IdGenerator {

    private static final String ID_FORMAT = "%08x-%04x-%04x-%04x-%012x";

    public static String genId() {
        long timestamp = System.currentTimeMillis();
        long timePart = timestamp & 0xFFFFFFFFFFFFL;
        long randomPartA = ThreadLocalRandom.current().nextLong() & 0xFFFFFFFFFFFFFL;
        long randomPartB = ThreadLocalRandom.current().nextLong() & 0x3FFFFFFFFFFFFFFFL;
        long version = 0x7000L;
        long mostSignificantBits = (timePart << 16) | version | ((randomPartA >>> 32) & 0xFFFFL);
        long leastSignificantBits = (randomPartA << 32) | (randomPartB & 0xFFFFFFFFL);

        return formatId(mostSignificantBits, leastSignificantBits);
    }

    private static String formatId(long mostSigBits, long leastSigBits) {
        return String.format(ID_FORMAT,
                (mostSigBits >> 32) & 0xFFFFFFFFL,
                (mostSigBits >> 16) & 0xFFFFL,
                (mostSigBits) & 0xFFFFL,
                (leastSigBits >> 48) & 0xFFFFL,
                (leastSigBits) & 0xFFFFFFFFFFFFL);
    }

}
