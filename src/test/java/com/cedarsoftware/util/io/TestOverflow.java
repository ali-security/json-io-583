package com.cedarsoftware.util.io;

import org.junit.Test;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for CVE-2023-34610 fix - stack overflow protection
 * Based on the original TestOverflow.groovy from the CVE patch
 */
public class TestOverflow
{
    private final static int TOO_DEEP_NESTING = 9999;
    private final static String TOO_DEEP_DOC = nestedDoc(TOO_DEEP_NESTING, "[ ", "] ", "0");

    private static String nestedDoc(int nesting, String open, String close, String content) {
        StringBuilder sb = new StringBuilder(nesting * (open.length() + close.length()));
        for (int i = 0; i < nesting; ++i) {
            sb.append(open);
            if ((i & 31) == 0) {
                sb.append("\n");
            }
        }
        sb.append("\n").append(content).append("\n");
        for (int i = 0; i < nesting; ++i) {
            sb.append(close);
            if ((i & 31) == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Test
    public void testOverflow()
    {
        try {
            JsonReader.jsonToJava(TOO_DEEP_DOC);
            fail("Expected IOException for exceeding maximum parsing depth");
        } catch (IOException e) {
            assertTrue("Expected 'Maximum parsing depth exceeded' message", 
                      e.getMessage().contains("Maximum parsing depth exceeded"));
        }
    }
}