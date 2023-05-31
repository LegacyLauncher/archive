package org.newsclub.net.unix;

import java.io.FileDescriptor;
import java.io.IOException;

public class Hack {
    private Hack() {
    }

    public static FileDescriptor createFileDescriptor(int fd) throws IOException {
        FileDescriptor fileDescriptor = new FileDescriptor();
        NativeUnixSocket.initFD(fileDescriptor, fd);
        return fileDescriptor;
    }

    public static int getFileDescriptor(FileDescriptor fileDescriptor) throws IOException {
        return NativeUnixSocket.getFD(fileDescriptor);
    }
}
