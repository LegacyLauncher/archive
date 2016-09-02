import org.testng.annotations.Test;

@Test
public class IntToByte {

    @Test
    public void test() {
        boolean hadOne;
        char c = 'с';

        if (hadOne = ((byte) (c >>> 24) != 0)) {
            write((byte) (c >>> 24));
        }

        if (hadOne || (hadOne = ((byte) (c >>> 16)) != 0)) {
            write((byte) (c >>> 16));
        }

        if (hadOne || ((byte) (c >>> 8)) != 0) {
            write((byte) (c >>> 8));
        }

        write((byte) c);
    }

    private void write(byte b) {
        System.out.println(b);
    }

    private static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }
}
