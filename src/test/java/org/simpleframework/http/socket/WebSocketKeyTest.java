package org.simpleframework.http.socket;

import java.security.MessageDigest;

import org.simpleframework.common.encode.Base64Encoder;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class WebSocketKeyTest {

    /*
    From RFC 6455

   Concretely, if as in the example above, the |Sec-WebSocket-Key|
   header field had the value "dGhlIHNhbXBsZSBub25jZQ==", the server
   would concatenate the string "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
   to form the string "dGhlIHNhbXBsZSBub25jZQ==258EAFA5-E914-47DA-95CA-
   C5AB0DC85B11".  The server would then take the SHA-1 hash of this,
   giving the value 0xb3 0x7a 0x4f 0x2c 0xc0 0x62 0x4f 0x16 0x90 0xf6
   0x46 0x06 0xcf 0x38 0x59 0x45 0xb2 0xbe 0xc4 0xea.  This value is
   then base64-encoded (see Section 4 of [RFC4648]), to give the value
   "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=".  This value would then be echoed in
   the |Sec-WebSocket-Accept| header field.
     */
    @Test
    public void testKey() throws Exception {
        final String key = "dGhlIHNhbXBsZSBub25jZQ==";
        final String result = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        final byte[] data = result.getBytes("ISO-8859-1");
        digest.update(data);
        final byte[] digested = digest.digest();
        final String value = new String(Base64Encoder.encode(digested));

        AssertJUnit.assertEquals(value, "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=");
    }

}
