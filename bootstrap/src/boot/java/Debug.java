import org.springframework.boot.loader.JarLauncher;

public class Debug extends JarLauncher {
    public static void main(String[] args) throws Exception {
        new Debug().launch(args);
    }

    @Override
    protected String getMainClass() {
        return "net.legacylauncher.bootstrap.BootstrapStarterDebug";
    }
}