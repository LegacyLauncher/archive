package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.configuration.AbstractConfiguration;
import ru.turikhay.tlauncher.configuration.Configurable;
import ru.turikhay.util.MinecraftUtil;

import java.util.*;

public class JavaManagerConfig implements Configurable {
    public static final String
            PATH_ROOT_DIR = "minecraft.jre.dir",
            PATH_ARGS = "minecraft.javaargs",
            PATH_MC_ARGS = "minecraft.args",
            PATH_JRE_TYPE = "minecraft.jre.type",
            PATH_USE_OPTIMIZED_ARGS = "minecraft.improvedargs";

    private String rootDir;
    private String args, mcArgs;
    private JreType jreType;
    private boolean useOptimizedArguments;

    public JavaManagerConfig(String rootDir, String args, String mcArgs, JreType jreType, boolean useOptimizedArguments) {
        this.rootDir = rootDir;
        this.args = args;
        this.mcArgs = mcArgs;
        this.jreType = jreType;
        this.useOptimizedArguments = useOptimizedArguments;
    }

    public JavaManagerConfig() {
    }

    public Optional<String> getRootDir() {
        return Optional.ofNullable(rootDir);
    }

    public String getRootDirOrDefault() {
        return getRootDir().orElse(getDefaultRootDir());
    }

    public Optional<String> getArgs() {
        return Optional.ofNullable(args);
    }

    public Optional<String> getMinecraftArgs() {
        return Optional.ofNullable(mcArgs);
    }

    public Optional<JreType> getJreType() {
        return Optional.ofNullable(jreType);
    }

    public JreType getJreTypeOrDefault() {
        return getJreType().orElse(new Recommended());
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setMcArgs(String mcArgs) {
        this.mcArgs = mcArgs;
    }

    public void setJreType(JreType jreType) {
        this.jreType = jreType;
    }

    public boolean useOptimizedArguments() {
        return useOptimizedArguments;
    }

    public void setUseOptimizedArguments(boolean useOptimizedArguments) {
        this.useOptimizedArguments = useOptimizedArguments;
    }

    @Override
    public void load(AbstractConfiguration configuration) {
        this.rootDir = configuration.get(PATH_ROOT_DIR);
        this.args = configuration.get(PATH_ARGS);
        this.mcArgs = configuration.get(PATH_MC_ARGS);

        String jreTypeString = configuration.get(PATH_JRE_TYPE);
        if (jreTypeString == null) {
            jreTypeString = Recommended.TYPE;
        }
        JreType jreType;
        switch (jreTypeString) {
            case Current.TYPE:
                jreType = new Current();
                break;
            case Custom.TYPE:
                jreType = new Custom();
                break;
            case Recommended.TYPE:
            default: // defaults to recommended
                jreType = new Recommended();
                break;
        }
        if (jreType instanceof Configurable) {
            ((Configurable) jreType).load(configuration);
        }
        this.jreType = jreType;

        this.useOptimizedArguments = configuration.getBoolean(PATH_USE_OPTIMIZED_ARGS);
    }

    @Override
    public void save(AbstractConfiguration configuration) {
        configuration.set(PATH_ROOT_DIR, rootDir);
        configuration.set(PATH_ARGS, args);
        configuration.set(PATH_MC_ARGS, mcArgs);
        if (this.jreType == null) {
            configuration.set(PATH_JRE_TYPE, null);
        } else {
            configuration.set(PATH_JRE_TYPE, this.jreType.getType());
        }
        configuration.set(PATH_USE_OPTIMIZED_ARGS, this.useOptimizedArguments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaManagerConfig)) return false;

        JavaManagerConfig that = (JavaManagerConfig) o;

        if (useOptimizedArguments != that.useOptimizedArguments) return false;
        if (!Objects.equals(rootDir, that.rootDir)) return false;
        if (!Objects.equals(args, that.args)) return false;
        if (!Objects.equals(mcArgs, that.mcArgs)) return false;
        return Objects.equals(jreType, that.jreType);
    }

    @Override
    public int hashCode() {
        int result = rootDir != null ? rootDir.hashCode() : 0;
        result = 31 * result + (args != null ? args.hashCode() : 0);
        result = 31 * result + (mcArgs != null ? mcArgs.hashCode() : 0);
        result = 31 * result + (jreType != null ? jreType.hashCode() : 0);
        result = 31 * result + (useOptimizedArguments ? 1 : 0);
        return result;
    }

    public static String getDefaultRootDir() {
        return MinecraftUtil.getSystemRelatedDirectory("tlauncher/mojang_jre").getAbsolutePath();
    }

    public interface JreType {
        String getType();
    }

    public static class Recommended implements JreType {
        public static final String TYPE = "recommended";

        @Override
        public int hashCode() {
            return Recommended.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Recommended;
        }

        @Override
        public String getType() {
            return TYPE;
        }
    }

    public static class Current implements JreType {
        public static final String TYPE = "current";

        @Override
        public int hashCode() {
            return Current.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Current;
        }

        @Override
        public String getType() {
            return TYPE;
        }
    }

    public static class Custom implements JreType, Configurable {
        public static final String
                PATH_CUSTOM_PATH = "minecraft.jre.custom.path",
                TYPE = "custom";

        private String path;

        public Optional<String> getPath() {
            return Optional.ofNullable(path);
        }

        public Custom() {
        }

        public Custom(String path) {
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Custom)) return false;

            Custom custom = (Custom) o;

            return Objects.equals(path, custom.path);
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }

        @Override
        public void load(AbstractConfiguration configuration) {
            path = configuration.get(PATH_CUSTOM_PATH);
        }

        @Override
        public void save(AbstractConfiguration configuration) {
            configuration.set(PATH_CUSTOM_PATH, path);
        }

        @Override
        public String getType() {
            return TYPE;
        }
    }

    private static final Map<String, Class<? extends JreType>> TYPES = new LinkedHashMap<>();

    static {
        TYPES.put(Recommended.TYPE, Recommended.class);
        TYPES.put(Current.TYPE, Current.class);
        TYPES.put(Custom.TYPE, Custom.class);
    }

    public static Set<String> keys() {
        return Collections.unmodifiableSet(TYPES.keySet());
    }

    public static JreType createByType(String type) throws IllegalArgumentException {
        Class<? extends JreType> jreTypeClass = TYPES.get(type);
        if (jreTypeClass == null) {
            throw new IllegalArgumentException("unknown jreType: " + type);
        }
        try {
            return jreTypeClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("couldn't create jreType: " + jreTypeClass, e);
        }
    }
}
