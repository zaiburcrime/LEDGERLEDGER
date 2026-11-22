import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * A simple bar for the Reports page.
 * It is just a colored rectangle. No chart library.
 */
public class ReportBar extends JPanel {
    private final double fraction;
    private final Color color;

    ReportBar(double value, double max, Color color) {
        this.fraction = max <= 0 ? 0 : Math.max(0, Math.min(1, value / max));
        this.color = color;
        setOpaque(true);
        setPreferredSize(new Dimension(120, 14));
    }

    @Override protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        g.setColor(new Color(50, 50, 58));
        g.fillRect(0, 0, w, h);
        g.setColor(color);
        if (fraction > 0) g.fillRect(0, 0, Math.max(1, (int) Math.round(w * fraction)), h);
    }
}
