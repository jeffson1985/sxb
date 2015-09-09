/*
 * @(#) $Id: ByteToCharUTF7.java,v 1.1.2.1 2004/09/29 00:57:59 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp.io;

/**
 * UTF-7のデコーダです。
 * <p>
 * </p>
 * @author Shin
 */
public class ByteToCharUTF7 extends ByteToCharConverter {
    public String getCharacterEncoding() {
        return "UTF7";
    }

    public int flush(char[] chars, int off, int len) {
        byteOff = 0;
        charOff = 0;
        b64Context = false;
        currentB64Off = 0;
        currentChar = 0;
        return 0;
    }
    public void reset() {
        byteOff = 0;
        charOff = 0;
        b64Context = false;
        currentB64Off = 0;
        currentChar = 0;
    }

    private boolean b64Context = false;
    private int currentB64Off = 0;
    private char currentChar = 0;

    public int convert(
        byte[] bytes,
        int byteStart,
        int byteEnd,
        char[] chars,
        int charStart,
        int charEnd)
        {
        charOff = charStart;

        for (byteOff = byteStart; byteOff < byteEnd; byteOff++) {
            if (charOff >= charEnd) {
            }
            if (b64Context) {
                if (bytes[byteOff] == '-') {
                    if (currentB64Off != 0 && currentChar > 0) {
                        chars[charOff] = currentChar;
                        charOff++;
                    }
                    b64Context = false;
                    continue;
                }
                int part =
                    (
                        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                            + "abcdefghijklmnopqrstuvwxyz0123456789+/").indexOf(
                        bytes[byteOff]);
                if (part == -1) {
                   
                }

                switch (currentB64Off) {
                    case 0 :
                        currentChar = (char) (part << 10);
                        break;
                    case 1 :
                        currentChar |= (char) (part << 4);
                        break;
                    case 2 :
                        currentChar |= (char) (part >> 2);
                        chars[charOff] = currentChar;
                        charOff++;
                        currentChar = (char) ((part & 0x03) << 14);
                        break;
                    case 3 :
                        currentChar |= (char) (part << 8);
                        break;
                    case 4 :
                        currentChar |= (char) (part << 2);
                        break;
                    case 5 :
                        currentChar |= (char) (part >> 4);
                        chars[charOff] = currentChar;
                        charOff++;
                        currentChar = (char) ((part & 0x0f) << 12);
                        break;
                    case 6 :
                        currentChar |= (char) (part << 6);
                        break;
                    case 7 :
                        currentChar |= (char)part;
                        chars[charOff] = currentChar;
                        charOff++;
                        break;
                }
                currentB64Off = (currentB64Off + 1) % 8;
                continue;
            }

            if (bytes[byteOff] == '+') {
                // shift character
                // This is start of the Base64 sequence.
                b64Context = true;
                currentB64Off = 0;
                continue;
            }
            chars[charOff] = (char)bytes[byteOff];
            charOff++;
        }
        return charOff - charStart;
    }

    /*
        public static void main(String[] args) throws Exception {
            System.setProperty("file.encoding.pkg", "com.sk_jp.io");
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            byte[] b = new byte[2048];
            int len;
            while ((len = System.in.read(b)) != -1) {
                o.write(b, 0, len);
            }
            byte[] bytes = o.toByteArray();
    
            System.out.println(new String(bytes, "UTF7"));
        }
    */
}
