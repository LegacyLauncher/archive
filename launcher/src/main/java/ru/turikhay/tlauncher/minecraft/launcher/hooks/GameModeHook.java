package ru.turikhay.tlauncher.minecraft.launcher.hooks;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;
import ru.turikhay.util.JavaVersion;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

public class GameModeHook implements ProcessHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameModeHook.class);

    private final GameModeInterface gameModeInterface;

    public GameModeHook(DBusConnection session) throws DBusException {
        this.gameModeInterface = session.getRemoteObject("com.feralinteractive.GameMode", "/com/feralinteractive/GameMode", GameModeInterface.class);
    }

    protected static Optional<ProcessHook> tryToCreate() {
        try {
            Callable<DBusConnection> sessionFactory = () -> DBusConnectionBuilder.forSessionBus()
                    .withDisconnectCallback(new DBusDisconnectionLogger())
                    .build();
            try {
                return Optional.of(new GameModeHook(sessionFactory.call()));
            } catch (Throwable t) {
                return Optional.empty();
            }
        } catch (NoClassDefFoundError ignored) {
            // java.lang.NoClassDefFoundError: org/freedesktop/dbus/**
            // => older bootstrap version
        }

        return Optional.empty();
    }

    private static class GameModeEntry {
        private final int callerPid;
        private final int gamePid;
        @Nullable
        private final FileDescriptor callerPidFd;
        @Nullable
        private final FileDescriptor gamePidFd;

        GameModeEntry(int callerPid, int gamePid,
                      @Nullable FileDescriptor callerPidFd, @Nullable FileDescriptor gamePidFd) {
            this.callerPid = callerPid;
            this.gamePid = gamePid;
            this.callerPidFd = callerPidFd;
            this.gamePidFd = gamePidFd;
        }

        GameModeEntry(int callerPid, int gamePid) {
            this(callerPid, gamePid, null, null);
        }

        public int getCallerPid() {
            return callerPid;
        }

        public int getGamePid() {
            return gamePid;
        }

        @Nullable
        public FileDescriptor getCallerPidFd() {
            return callerPidFd;
        }

        @Nullable
        public FileDescriptor getGamePidFd() {
            return gamePidFd;
        }
    }

    private final Map<Process, GameModeEntry> fdMap = new WeakHashMap<>();

    @Override
    @SuppressWarnings("Since15") // GameModeHook itself only created on java11+
    public void processCreated(Process process) {
        try {
            int callerPid = (int) ProcessHandle.current().pid();
            int gamePid = (int) process.pid();
            int callerPidFd;
            int gamePidFd;
            try {
                callerPidFd = CLibrary.INSTANCE.pidfd_open(callerPid, 0);
                gamePidFd = CLibrary.INSTANCE.pidfd_open(gamePid, 0);
            } catch (UnsatisfiedLinkError e) {
                // protect from 'syscall' is cannot be linked
                // If pidfd_open syscall is unknown to the kernel, it just returns negative value from 'syscall'
                callerPidFd = -1;
                gamePidFd = -1;
            }
            if (callerPidFd >= 0 && gamePidFd >= 0) {
                fdMap.put(process, new GameModeEntry(callerPid, gamePid, new FileDescriptor(callerPidFd), new FileDescriptor(gamePidFd)));
                gameModeInterface.RegisterGameByPIDFd(new FileDescriptor(callerPidFd), new FileDescriptor(gamePidFd));
                LOGGER.info("Minecraft process registered in GameMode. Pids: {} / {}, FDs: {} / {}", callerPid, gamePid, callerPidFd, gamePidFd);
            } else {
                fdMap.put(process, new GameModeEntry(callerPid, gamePid));
                gameModeInterface.RegisterGameByPID(callerPid, gamePid);
                LOGGER.info("Minecraft process registered in GameMode. Pids: {} / {}", callerPid, gamePid);
            }
        } catch (DBusExecutionException ignored) {
        }
    }

    @Override
    @SuppressWarnings("Since15") // GameModeHook itself only created on java11+
    public void processDestroyed(Process process) {
        try {
            GameModeEntry entry = fdMap.remove(process);
            if (entry == null) {
                LOGGER.warn("GameMode registration lost (that's a bug), trying to unregister with generic way...");
                int callerPid = (int) ProcessHandle.current().pid();
                int gamePid = (int) process.pid();
                gameModeInterface.UnregisterGameByPID(callerPid, gamePid);
                LOGGER.info("Minecraft process unregistered in GameMode. Pids: {} / {}", callerPid, gamePid);
            } else {
                if (entry.getCallerPidFd() != null && entry.getGamePidFd() != null) {
                    gameModeInterface.UnregisterGameByPIDFd(entry.getCallerPidFd(), entry.getGamePidFd());
                    LOGGER.info("Minecraft process unregistered in GameMode. Pids: {} / {}, FDs: {} / {}",
                            entry.getCallerPid(), entry.getGamePid(), entry.getCallerPidFd().getIntFileDescriptor(), entry.getGamePidFd().getIntFileDescriptor());
                    CLibrary.INSTANCE.close(entry.getGamePidFd().getIntFileDescriptor());
                    CLibrary.INSTANCE.close(entry.getCallerPidFd().getIntFileDescriptor());
                } else {
                    gameModeInterface.UnregisterGameByPID(entry.getCallerPid(), entry.getGamePid());
                    LOGGER.info("Minecraft process unregistered in GameMode. Pids: {} / {}", entry.getCallerPid(), entry.getGamePid());
                }
            }
        } catch (DBusExecutionException ignored) {
        }
    }

    private static class DBusDisconnectionLogger implements IDisconnectCallback {
        @Override
        public void disconnectOnError(IOException e) {
            LOGGER.error("DBus session terminated due to an error", e);
        }
    }

    public static class Loader {
        private static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

        public static Optional<ProcessHook> tryToCreate() {
            if (JavaVersion.getCurrent().getMajor() >= 11) {
                return GameModeHook.tryToCreate();
            } else {
                LOGGER.info("GameModeHook is not available because it requires Java 11+");
            }
            return Optional.empty();
        }
    }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int close(int fd);

        int syscall(int syscall, Object... args);

        default int pidfd_open(/* pid_t */ int pid, /* unsigned */ int flags) {
            return syscall(SYS_pidfd_open, pid, flags);
        }

        int SYS_pidfd_open = 434; // it's the same on x86 and x86_64, but may be different on x32. But that's unsupported anyway.
    }
}
