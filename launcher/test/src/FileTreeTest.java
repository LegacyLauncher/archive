import org.testng.annotations.Test;
import ru.turikhay.util.U;

import java.io.File;

public class FileTreeTest {

    @Test
    public void test() {
        readDirectory(new File("D:\\Minecraft\\home\\Forge-1.8\\mods"), 0, 2, new StringBuilder());
    }

    private void readDirectory(File dir, int current, int limit, StringBuilder buffer) {
        if(!dir.isDirectory()) {
            plog(dir, " (not a dir)");
            return;
        }

        File[] list = U.requireNotNull(dir.listFiles(), "dir listing: " + dir.getAbsolutePath());

        if(current == 0) {
            plog(dir);
        } else if(list == null || list.length == 0) {
            plog(buffer, "└ [empty]");
        }

        StringBuilder dirBuffer = null;
        File file; String name; boolean emptyDir;

        for (int i = 0; i < list.length; i++) {
            emptyDir = false;

            file = list[i];
            name = file.getName();

            if(file.isDirectory()) {
                String[] subList = file.list();
                if(subList == null || subList.length == 0) {
                    name += " [empty dir]";
                    emptyDir = true;
                }
            } else {
                long length = file.length();
                if(length == 0L) {
                    name += " [empty file]";
                } else {
                    name += " ["+ length / 1024L +" kbytes]";
                }
            }

            plog(buffer, i == list.length - 1? "└ " : "├ ", name);

            if(file.isDirectory() && !emptyDir) {
                if(dirBuffer == null || i == list.length - 1) {
                    dirBuffer = new StringBuilder().append(buffer).append(i == list.length - 1? "  " : "| ").append(' ');
                }

                if(current == limit) {
                    File[] subList = file.listFiles();
                    String str;

                    if(subList != null && subList.length > 0) {
                        StringBuilder s = new StringBuilder();

                        int files = 0, directories = 0;
                        for(File subFile : subList) {
                            if(subFile.isFile()) {
                                files++;
                            }
                            if(subFile.isDirectory()) {
                                directories++;
                            }
                        }

                        s.append("[");

                        switch (files) {
                            case 0:
                                s.append("no files");
                                break;
                            case 1:
                                s.append("1 file");
                                break;
                            default:
                                s.append(files).append(" files");
                        }

                        s.append("; ");

                        switch(directories) {
                            case 0:
                                s.append("no dirs");
                                break;
                            case 1:
                                s.append("1 dir");
                                break;
                            default:
                                s.append(directories).append(" dirs");
                        }

                        s.append(']');

                        str = s.toString();
                    } else {
                        str = "[empty dir]";
                    }
                    plog(dirBuffer, "└ ", str);
                    continue;
                }

                readDirectory(file, current + 1, limit, dirBuffer);
            }
        }
    }

    private void plog(Object... objs) {
        StringBuilder b = new StringBuilder();

        for(Object o : objs) {
            b.append(o);
        }

        U.plog(b);
    }

    private void log(Object... o) {
        U.log("[FileTree]", o);
    }
}
