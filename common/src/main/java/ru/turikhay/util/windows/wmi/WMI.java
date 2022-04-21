package ru.turikhay.util.windows.wmi;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class WMI {
    static final String CRLF = "\r\n";

    private final String oWMIQuery;
    private final Charset charset;

    public WMI(String oWMIQuery, Charset encoding) {
        this.oWMIQuery = oWMIQuery;
        this.charset = encoding;
    }

    private void writeVbFile(File file, String wmiQueryStr, String wmiCommaSeparatedFieldName) throws IOException {
        try (Writer vbs = new OutputStreamWriter(new FileOutputStream(file), charset)) {

            vbs.append("Dim oWMI : Set oWMI = GetObject(\"winmgmts:").append(oWMIQuery).append("\")").append(CRLF);
            vbs.append("Dim classComponent : Set classComponent = oWMI.ExecQuery(\"").append(wmiQueryStr).append("\")").append(CRLF);
            vbs.append("Dim obj, strData").append(CRLF);
            vbs.append("For Each obj in classComponent").append(CRLF);

            for (String fieldName : StringUtils.split(wmiCommaSeparatedFieldName, ',')) {
                vbs.append("  strData = strData & obj.").append(fieldName).append(" & VBCrLf").append(CRLF);
            }

            vbs.append("Next").append(CRLF);
            vbs.append("wscript.echo strData").append(CRLF);
        }
    }

    public List<String> getWMIValues(String wmiQueryStr, String fieldName) throws Exception {
        File tmpFile = File.createTempFile("wmi", ".vbs");
        tmpFile.deleteOnExit();
        try {
            writeVbFile(tmpFile, wmiQueryStr, fieldName);
            return execute(new String[]{"cmd.exe", "/C", "cscript.exe", tmpFile.getAbsolutePath()}, charset);
        } finally {
            tmpFile.delete();
        }
    }

    private String getEnvVar(String envVarName) throws Exception {
        String varName = "%" + envVarName + "%";
        String envVarValue = execute(new String[]{"cmd.exe", "/C", "echo " + varName}, charset).get(0);
        if (envVarValue.equals(varName)) {
            throw new Exception("Environment variable '" + envVarName + "' does not exist!");
        }
        return envVarValue;
    }

    private void writeStrToFile(String filename, String data) throws Exception {
        FileWriter output = new FileWriter(filename);
        output.write(data);
        output.flush();
        output.close();
    }

    static List<String> execute(String[] cmdArray, Charset charset) throws Exception {
        Process process = Runtime.getRuntime().exec(cmdArray);
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
        List<String> output = new ArrayList<>();
        String line;
        while ((line = input.readLine()) != null) {
            if (!line.contains("Copyright") && !line.contains("Microsoft") && !line.isEmpty()) {
                output.add(line);
            }
        }
        process.destroy();
        return output;
    }

    public static List<String> getAVSoftwareList() throws Exception {
        Charset charset = Codepage.get() == null ? Charset.defaultCharset() : Codepage.get().getCharset();
        WMI wmi = new WMI("{impersonationLevel=impersonate}!\\\\.\\root\\SecurityCenter2", charset);
        return wmi.getWMIValues("Select * from AntiVirusProduct", "displayName");
    }
}
