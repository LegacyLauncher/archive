import org.springframework.boot.loader.JarLauncher;

public class Bootstrap extends JarLauncher {
    public static void main(String[] args) throws Exception {
        new Bootstrap().launch(args);
    }

    @Override
    protected String getMainClass() {
        return "net.legacylauncher.bootstrap.Bootstrap";
    }
}
