package pacman.entries.pacman;

public class MathHelper {

    public static double Log2(double x) {
        if(x == 0) return 0;
        return (Math.log10(x)/Math.log10(2));
    }

}
