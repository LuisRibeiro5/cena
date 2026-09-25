package core2d;

import java.awt.Graphics2D;

public final class Bresenham {

    private Bresenham() {
    }

    public static void desenha(Graphics2D g, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int passoX = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int passoY = y0 < y1 ? 1 : -1;
        int erro = dx + dy;

        while (true) {
            g.fillRect(x0, y0, 1, 1);

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int erroDuplo = 2 * erro;
            if (erroDuplo >= dy) {
                erro += dy;
                x0 += passoX;
            }
            if (erroDuplo <= dx) {
                erro += dx;
                y0 += passoY;
            }
        }
    }
}