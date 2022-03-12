package ru.turikhay.util.windows.wmi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Codepage {
    private final int codepage;
    private final String charsetName;
    private final Charset charset;

    private Codepage(int codepage, String charsetName, Charset charset) {
        this.codepage = codepage;
        this.charsetName = charsetName;
        this.charset = charset;
    }

    public int getCodepage() {
        return codepage;
    }

    public Charset getCharset() throws UnsupportedCharsetException {
        if (charset == null) {
            throw new UnsupportedCharsetException(charsetName);
        }
        return charset;
    }

    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("codepage", codepage);
        if (charset == null) {
            b.append("charsetName", charsetName);
        } else {
            b.append("charset", charset);
        }
        return b.build();
    }

    private static final Map<Integer, Codepage> knownCodepages = new HashMap<>();
    private static Codepage instance;

    public static Codepage get() throws CodepageException {
        if (instance == null) {
            instance = doDetect();
        }
        return instance;
    }

    public static Codepage getCodepage(int codepage) {
        return knownCodepages.get(codepage);
    }

    private static Codepage doDetect() throws CodepageException {
        String chcpResponse = queryChcp();
        Matcher matcher = Pattern.compile(".*: ([\\d]+)[.]?").matcher(chcpResponse);
        if (matcher.matches() && matcher.groupCount() == 1) {
            int cp;
            try {
                cp = Integer.parseInt(matcher.group(1));
            } catch (Exception e) {
                throw new RuntimeException("could not parse chcp: \"" + matcher.group(1) + "\"", e);
            }
            Codepage codepage = getCodepage(cp);
            if (codepage == null) {
                throw new CodepageException("chcp returned unknown codepage: " + cp);
            }
            return codepage;
        }
        throw new CodepageException("chcp returned unexpected response; lines: \"" + chcpResponse + "\"");
    }

    private static String queryChcp() throws CodepageException {
        String system32 = System.getenv("WINDIR") + "\\system32\\";
        List<String> result;
        try {
            result = WMI.execute(new String[]{system32 + "chcp.com"}, StandardCharsets.US_ASCII);
        } catch (Exception e) {
            throw new CodepageException("Couldn't run chcp.com", e);
        }
        if (result.isEmpty()) {
            throw new CodepageException("chcp returned no lines");
        }
        return result.get(result.size() - 1); // lastLine
    }

    private static void add(int codepage, String charsetName) {
        Charset charset;
        try {
            charset = Charset.forName(charsetName);
        } catch (RuntimeException rE) {
            charset = null;
        }
        knownCodepages.put(codepage, new Codepage(codepage, charsetName, charset));
    }

    static {
        add(37, "IBM037");
        add(437, "IBM437");
        add(500, "IBM500");
        add(708, "ASMO-708");
        add(720, "Windows-1256");
        add(737, "ibm737");
        add(775, "ibm775");
        add(850, "ibm850");
        add(852, "ibm852");
        add(855, "IBM855");
        add(857, "ibm857");
        add(858, "IBM00858");
        add(860, "IBM860");
        add(861, "ibm861");
        add(862, "ibm862");
        add(863, "IBM863");
        add(864, "IBM864");
        add(865, "IBM865");
        add(866, "CP866");
        add(869, "ibm869");
        add(870, "IBM870");
        add(874, "windows-874");
        add(875, "cp875");
        add(932, "shift_jis");
        add(936, "gb2312");
        add(949, "ks_c_5601-1987");
        add(950, "big5");
        add(1026, "IBM1026");
        add(1047, "IBM1047");
        add(1140, "IBM01140");
        add(1141, "IBM01141");
        add(1142, "IBM01142");
        add(1143, "IBM01143");
        add(1144, "IBM01144");
        add(1145, "IBM01145");
        add(1146, "IBM01146");
        add(1147, "IBM01147");
        add(1148, "IBM01148");
        add(1149, "IBM01149");
        add(1200, "utf-16");
        add(1250, "windows-1250");
        add(1251, "windows-1251");
        add(1252, "windows-1252");
        add(1253, "windows-1253");
        add(1254, "windows-1254");
        add(1255, "windows-1255");
        add(1256, "windows-1256");
        add(1257, "windows-1257");
        add(1258, "windows-1258");
        add(1361, "Johab");
        add(10004, "x-macarabic");
        add(10005, "x-machebrew");
        add(10006, "x-macgreek");
        add(10007, "x-maccyrillic");
        add(10010, "x-macromania");
        add(10017, "x-macukraine");
        add(10021, "x-macthai");
        add(10029, "x-MacCentralEurope");
        add(10079, "x-maciceland");
        add(10081, "x-macturkish");
        add(12000, "utf-32");
        add(12001, "utf-32BE");
        add(20000, "ISO-2022-CN");
        add(20127, "us-ascii");
        add(20273, "IBM273");
        add(20277, "IBM277");
        add(20278, "IBM278");
        add(20280, "IBM280");
        add(20284, "IBM284");
        add(20285, "IBM285");
        add(20290, "IBM290");
        add(20297, "IBM297");
        add(20420, "IBM420");
        add(20424, "IBM424");
        add(20838, "IBM-Thai");
        add(20866, "koi8-r");
        add(20871, "IBM871");
        add(20932, "EUC-JP");
        add(21025, "cp1025");
        add(21866, "koi8-u");
        add(28591, "iso-8859-1");
        add(28592, "iso-8859-2");
        add(28593, "iso-8859-3");
        add(28594, "iso-8859-4");
        add(28595, "iso-8859-5");
        add(28596, "iso-8859-6");
        add(28597, "iso-8859-7");
        add(28598, "iso-8859-8");
        add(28599, "iso-8859-9");
        add(28603, "iso-8859-13");
        add(28605, "iso-8859-15");
        add(50220, "iso-2022-jp");
        add(50221, "csISO2022JP");
        add(50222, "iso-2022-jp");
        add(50225, "iso-2022-kr");
        add(51932, "euc-jp");
        add(51936, "EUC-CN");
        add(51949, "euc-kr");
        add(54936, "GB18030");
        add(65001, "utf-8");
    }

}