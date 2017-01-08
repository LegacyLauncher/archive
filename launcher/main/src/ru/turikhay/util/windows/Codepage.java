package ru.turikhay.util.windows;

import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.exceptions.UnsupportedEnvirnomentException;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Codepage {
    private final int codepage;
    private final Charset charset;

    private Codepage(int codepage, Charset charset) {
        this.codepage = codepage;
        this.charset = U.requireNotNull(charset, "charset");
        logPrefix = "[Charset][" + charset + "]";
    }

    public int getCodepage() {
        return codepage;
    }

    public Charset getCharset() {
        return charset;
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("codepage", codepage)
                .append("charset", charset)
                .build();
    }

    private final String logPrefix;

    private void ilog(Object... o) {
        U.log(logPrefix, o);
    }

    private static final List<Codepage> pages = new ArrayList<Codepage>();
    private static Codepage detectedCodepage;
    private static CodepageDetectorThread thread = new CodepageDetectorThread();

    public static Codepage get() throws UnsupportedEnvirnomentException {
        UnsupportedEnvirnomentException.ensureUnder(OS.WINDOWS);

        if (thread == null || Thread.currentThread() == thread) {
            return detectedCodepage;
        }

        if (!thread.isAlive()) {
            thread.start();
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            //
        }

        return detectedCodepage;
    }

    public static Codepage get(int cp) {
        for (Codepage codepage : pages) {
            if (codepage.codepage == cp) {
                return codepage;
            }
        }
        return null;
    }

    private static void add(int codepage, String charsetName) {
        Charset charset;

        try {
            charset = Charset.forName(charsetName);
        } catch (RuntimeException rE) {
            log("Charset", charsetName, codepage, "is not supported");
            return;
        }

        pages.add(new Codepage(codepage, charset));
    }

    private static void log(Object... o) {
        U.log("[Charset]", o);
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
        add(863, "IBM863");
        add(864, "IBM864");
        add(865, "IBM865");
        add(866, "CP1251");
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

    private static class CodepageDetectorThread extends ExtendedThread implements JavaProcessListener {
        @Override
        public void run() {
            try {
                detect();
            } catch (Exception e) {
                log("could not detect code page", e);
            }
            thread = null;
        }

        private void detect() throws Exception {
            String system32 = System.getenv("WINDIR") + "\\system32\\";
            JavaProcess process = new JavaProcess(new ProcessBuilder(system32 + "chcp.com").start());
            process.safeSetExitRunnable(this);

            int timer = 0;
            while (process.isRunning()) {
                if (++timer > 15) {
                    log("Oops, we're taking too long");
                    throw new RuntimeException("timeout");
                }
                U.sleepFor(250);
            }

            if (StringUtils.isBlank(lastLine)) {
                throw new RuntimeException("last line is blank");
            }

            Matcher matcher = Pattern.compile(".*: ([\\d]+)").matcher(lastLine);
            if (matcher.matches() && matcher.groupCount() == 1) {
                int cp;
                try {
                    cp = Integer.parseInt(matcher.group(1));
                } catch (Exception e) {
                    throw new RuntimeException("could not parse chcp: \"" + matcher.group(1) + "\"", e);
                }
                log("System code page detected:", cp);

                Codepage codepage = get(cp);
                if (codepage == null) {
                    throw new UnsupportedCharsetException(String.valueOf(cp));
                }

                log("Code page selected", codepage);
                detectedCodepage = codepage;
            }
        }

        private String lastLine;

        @Override
        public void onJavaProcessLog(JavaProcess var1, String var2) {
            log(var1.getRawProcess(), var2);
            lastLine = var2;
        }

        @Override
        public void onJavaProcessEnded(JavaProcess var1) {
        }

        @Override
        public void onJavaProcessError(JavaProcess var1, Throwable var2) {
        }
    }
}
