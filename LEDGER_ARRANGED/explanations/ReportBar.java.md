# ReportBar.java

This tab explains only `src/ReportBar.java`.

In the Explorer, open the `explanations` folder and click another name to open that class in its own tab. Read `00 How the program works.md` first if you have not yet.

## What this class is

`ReportBar` is the colored rectangle on the Reports page. It is a Swing panel, so it can sit inside the window. No chart library is used.

## How it works

The constructor receives this bar's amount, the biggest amount in the chart, and a color. It stores `value / max` as a fraction between 0 and 1. When Java needs to draw the panel, `paintComponent` paints a dark track, then a colored rectangle that covers that fraction of the width.

## The code

```java
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
```

## Walk through the code

| Line | What it means |
| --- | --- |
| 1–3 | Import color, size, and `JPanel` (a paintable rectangle). |
| 5–8 | Comment. |
| 9 | `extends JPanel` means a `ReportBar` **is** a panel. |
| 10–11 | `fraction` is 0 to 1. `color` is the fill color. `final` means they are set once. |
| 13–14 | If `max` is 0, use 0 so the program never divides by zero. Otherwise clamp `value / max` between 0 and 1. |
| 15 | Remember the color. |
| 16 | The panel paints its own background. |
| 17 | Ask for about 120 by 14 pixels. The layout may stretch the width. |
| 20 | `paintComponent` runs whenever the bar must be drawn. |
| 21 | Let the normal panel paint first. |
| 22–23 | Read the current width and height. |
| 24–25 | Paint the dark track across the whole bar. |
| 26–27 | Paint the colored part. Its width is `width * fraction`, and at least 1 pixel when the amount is tiny but not zero. |
