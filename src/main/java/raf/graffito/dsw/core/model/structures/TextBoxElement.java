package raf.graffito.dsw.core.model.structures;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.model.factory.slideElement.SlideElementType;
import raf.graffito.dsw.core.model.structures.SlideElement;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TextBoxElement extends SlideElement {
    ///  Element za textbox
    private String text = "";
    // Styling
    private String fontFamily = "Arial";
    private int fontStyle = Font.PLAIN;
    private static int amountOfCopies = 0;

    // Base font sizing (scales with box size)
    private int baseFontSize = 18;
    private int minFontSize = 10;
    private int maxFontSize = 72;

    // Used as reference size for scaling (captured at creation time)
    private final Dimension baseBoxSize;

    private Color borderColor = new Color(60, 140, 255);
    private Color textColor = Color.BLACK;

    public TextBoxElement(String name, Point pos, Dimension size, int rotateRatio) {
        super(pos, size, rotateRatio);
        super.setName(name);
        this.baseBoxSize = new Dimension(Math.max(1, size.width), Math.max(1, size.height));
    }

    @Override
    public void paint(Graphics2D g) {
        AffineTransform oldTx = g.getTransform();
        Shape oldClip = g.getClip();

        try {
            int cx = pos.x + size.width / 2;
            int cy = pos.y + size.height / 2;

            // rotate whole textbox (border + text)
            g.rotate(Math.toRadians(rotateRatio), cx, cy);

            Rectangle box = new Rectangle(pos.x, pos.y, size.width, size.height);

            // border
            g.setColor(borderColor);
            g.draw(box);

            // clip so text can never draw outside
            g.setClip(box);

            // padding
            int pad = 8;
            int innerW = Math.max(1, size.width - pad * 2);
            int innerH = Math.max(1, size.height - pad * 2);

            // compute font size based on resize (scale relative to initial box)
            int fontSize = computeScaledFontSize(size, baseBoxSize);
            Font font = new Font(fontFamily, fontStyle, fontSize);
            g.setFont(font);

            FontMetrics fm = g.getFontMetrics();

            // choose text (placeholder if empty)
            String drawText = (text == null) ? "" : text;
            boolean placeholder = drawText.isBlank();
            if (placeholder) {
                g.setColor(new Color(0, 0, 0, 120));
                drawText = "Double-click to edit...";
            } else {
                g.setColor(textColor);
            }

            // wrap into lines that fit innerW
            List<String> lines = wrapText(drawText, fm, innerW);

            int lineH = fm.getHeight();
            int maxLines = Math.max(1, innerH / Math.max(1, lineH));

            // draw lines top-to-bottom; clip already active
            int x = pos.x + pad;
            int y = pos.y + pad + fm.getAscent();

            for (int i = 0; i < lines.size() && i < maxLines; i++) {
                g.drawString(lines.get(i), x, y + i * lineH);
            }

            // optional: if text exceeds space, show ellipsis on last line
            if (!placeholder && lines.size() > maxLines && maxLines > 0) {
                int last = maxLines - 1;
                String ell = fitWithEllipsis(lines.get(last), fm, innerW);
                g.drawString(ell, x, y + last * lineH);
            }

        } finally {
            g.setTransform(oldTx);
            g.setClip(oldClip);
        }
    }

    @Override
    public Prototype clone() {
        String baseName = getName() + ".copy";

        String cloneName = (amountOfCopies == 0)
                ? baseName
                : baseName + amountOfCopies;

        TextBoxElement e = new TextBoxElement(
                cloneName,
                getPos(),
                getSize(),
                getRotateRatio()
        );
        e.setText(getText());

        return e;
    }

    private int computeScaledFontSize(Dimension current, Dimension base) {
        double sx = current.width / (double) Math.max(1, base.width);
        double sy = current.height / (double) Math.max(1, base.height);

        // use the smaller scale so text doesn’t overflow vertically
        double s = Math.min(sx, sy);

        int scaled = (int) Math.round(baseFontSize * s);

        if (scaled < minFontSize) scaled = minFontSize;
        if (scaled > maxFontSize) scaled = maxFontSize;

        return scaled;
    }

    private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();

        // support manual newlines
        String[] paragraphs = text.split("\\R", -1);

        for (String para : paragraphs) {
            if (para.isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = para.split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String w : words) {
                if (line.length() == 0) {
                    // if a single word is too long, split it
                    if (fm.stringWidth(w) <= maxWidth) {
                        line.append(w);
                    } else {
                        lines.addAll(splitLongWord(w, fm, maxWidth));
                    }
                    continue;
                }

                String candidate = line + " " + w;
                if (fm.stringWidth(candidate) <= maxWidth) {
                    line.append(" ").append(w);
                } else {
                    lines.add(line.toString());
                    line.setLength(0);

                    if (fm.stringWidth(w) <= maxWidth) {
                        line.append(w);
                    } else {
                        lines.addAll(splitLongWord(w, fm, maxWidth));
                    }
                }
            }

            if (line.length() > 0) lines.add(line.toString());
        }

        return lines;
    }

    private static List<String> splitLongWord(String word, FontMetrics fm, int maxWidth) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            cur.append(word.charAt(i));
            if (fm.stringWidth(cur.toString()) > maxWidth) {
                // back off one char
                cur.setLength(cur.length() - 1);
                if (cur.length() > 0) parts.add(cur.toString());
                cur.setLength(0);
                cur.append(word.charAt(i));
            }
        }

        if (cur.length() > 0) parts.add(cur.toString());
        return parts;
    }

    private static String fitWithEllipsis(String s, FontMetrics fm, int maxWidth) {
        String ell = "...";
        if (fm.stringWidth(ell) > maxWidth) return ""; // nothing fits

        if (fm.stringWidth(s) <= maxWidth) return s;

        String trimmed = s;
        while (!trimmed.isEmpty() && fm.stringWidth(trimmed + ell) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ell;
    }
}
