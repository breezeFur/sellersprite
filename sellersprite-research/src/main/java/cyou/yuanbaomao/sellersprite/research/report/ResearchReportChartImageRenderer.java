package cyou.yuanbaomao.sellersprite.research.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

/** 将前端使用的同一图表规格渲染为 PDF 可嵌入的 PNG。 */
final class ResearchReportChartImageRenderer {

    private static final int WIDTH = 960;
    private static final int DEFAULT_HEIGHT = 420;
    private static final int LEFT = 88;
    private static final int RIGHT = 32;
    private static final int TOP = 62;
    private static final int BOTTOM = 72;
    private static final int HORIZONTAL_PLOT_LEFT = 360;
    private static final int HORIZONTAL_BOTTOM = 62;
    private static final int HORIZONTAL_ROW_HEIGHT = 48;
    private static final int HORIZONTAL_LABEL_LEFT = 40;
    private static final int HORIZONTAL_LABEL_GAP = 24;
    private static final int HORIZONTAL_LABEL_MAX_LINES = 2;
    private static final Color BRAND = new Color(37, 99, 235);
    private static final Color GRID = new Color(203, 213, 225);
    private static final Color TEXT = new Color(51, 65, 85);

    String renderDataUri(ResearchReportChart chart) {
        try {
            int height = imageHeight(chart);
            BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, height);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            graphics.setColor(new Color(15, 23, 42));
            graphics.drawString(chart.title(), LEFT, 35);
            if (ResearchReportChart.TYPE_HORIZONTAL_BAR.equalsIgnoreCase(chart.type())) {
                drawHorizontalBars(graphics, chart, height);
            } else if (ResearchReportChart.TYPE_BAR.equalsIgnoreCase(chart.type())) {
                drawBars(graphics, chart, height);
            } else {
                drawLine(graphics, chart, height);
            }
            graphics.dispose();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("渲染最终报告图表失败: " + chart.chartCode(), exception);
        }
    }

    private int imageHeight(ResearchReportChart chart) {
        if (!ResearchReportChart.TYPE_HORIZONTAL_BAR.equalsIgnoreCase(chart.type())) {
            return DEFAULT_HEIGHT;
        }
        int rowCount = Math.max(1, firstValues(chart).size());
        return TOP + HORIZONTAL_BOTTOM + rowCount * HORIZONTAL_ROW_HEIGHT;
    }

    private void drawLine(Graphics2D graphics, ResearchReportChart chart, int imageHeight) {
        List<BigDecimal> values = firstValues(chart);
        BigDecimal max = maximum(values);
        int plotWidth = WIDTH - LEFT - RIGHT;
        int plotHeight = imageHeight - TOP - BOTTOM;
        drawGrid(graphics, max, plotWidth, plotHeight, chart.unit());
        if (values.isEmpty()) {
            return;
        }
        int denominator = Math.max(1, values.size() - 1);
        graphics.setColor(BRAND);
        graphics.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int previousX = 0;
        int previousY = 0;
        for (int index = 0; index < values.size(); index++) {
            int x = LEFT + plotWidth * index / denominator;
            int y = TOP + plotHeight - scaled(values.get(index), max, plotHeight);
            if (index > 0) {
                graphics.drawLine(previousX, previousY, x, y);
            }
            graphics.fillOval(x - 5, y - 5, 10, 10);
            previousX = x;
            previousY = y;
        }
        drawCategoryLabels(graphics, chart.categories(), plotWidth, imageHeight);
    }

    private void drawBars(Graphics2D graphics, ResearchReportChart chart, int imageHeight) {
        List<BigDecimal> values = firstValues(chart);
        BigDecimal max = maximum(values);
        int plotWidth = WIDTH - LEFT - RIGHT;
        int plotHeight = imageHeight - TOP - BOTTOM;
        int count = Math.max(1, values.size());
        int slot = Math.max(1, plotWidth / count);
        int barWidth = Math.max(8, Math.min(42, slot - 10));
        graphics.setColor(BRAND);
        for (int index = 0; index < values.size(); index++) {
            int height = scaled(values.get(index), max, plotHeight);
            int x = LEFT + index * slot + (slot - barWidth) / 2;
            int y = TOP + plotHeight - height;
            graphics.fillRoundRect(x, y, barWidth, height, 8, 8);
        }
        graphics.setColor(GRID);
        graphics.drawLine(LEFT, TOP + plotHeight, WIDTH - RIGHT, TOP + plotHeight);
        drawCategoryLabels(graphics, chart.categories(), plotWidth, imageHeight);
    }

    private void drawHorizontalBars(Graphics2D graphics, ResearchReportChart chart, int imageHeight) {
        List<BigDecimal> values = firstValues(chart);
        if (values.isEmpty()) {
            return;
        }
        BigDecimal max = maximum(values);
        int plotWidth = WIDTH - HORIZONTAL_PLOT_LEFT - RIGHT;
        int plotHeight = imageHeight - TOP - HORIZONTAL_BOTTOM;
        int slot = Math.max(1, plotHeight / values.size());
        int barHeight = Math.max(18, Math.min(30, slot - 12));
        drawHorizontalGrid(graphics, max, plotWidth, plotHeight, chart.unit(), imageHeight);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        for (int index = 0; index < values.size(); index++) {
            int y = TOP + index * slot + (slot - barHeight) / 2;
            int barWidth = scaled(values.get(index), max, plotWidth);
            graphics.setColor(BRAND);
            graphics.fillRoundRect(HORIZONTAL_PLOT_LEFT, y, barWidth, barHeight, 8, 8);
            drawHorizontalCategoryLabel(graphics, chart.categories().get(index), y, barHeight);
            drawBarValue(graphics, values.get(index), barWidth, y, barHeight, plotWidth);
        }
    }

    private void drawHorizontalGrid(
            Graphics2D graphics,
            BigDecimal max,
            int plotWidth,
            int plotHeight,
            String unit,
            int imageHeight) {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        FontMetrics metrics = graphics.getFontMetrics();
        int divisions = Math.max(1, Math.min(4, max.setScale(0, RoundingMode.CEILING).intValue()));
        for (int index = 0; index <= divisions; index++) {
            int x = HORIZONTAL_PLOT_LEFT + plotWidth * index / divisions;
            graphics.setColor(GRID);
            graphics.drawLine(x, TOP, x, TOP + plotHeight);
            graphics.setColor(TEXT);
            String label = max.multiply(BigDecimal.valueOf(index))
                    .divide(BigDecimal.valueOf(divisions), 2, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString();
            graphics.drawString(label, x - metrics.stringWidth(label) / 2, imageHeight - 24);
        }
        if (unit != null && !unit.isBlank()) {
            graphics.setColor(TEXT);
            int unitX = HORIZONTAL_PLOT_LEFT + (plotWidth - metrics.stringWidth(unit)) / 2;
            graphics.drawString(unit, unitX, imageHeight - 5);
        }
    }

    private void drawHorizontalCategoryLabel(
            Graphics2D graphics, String category, int barY, int barHeight) {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        graphics.setColor(TEXT);
        FontMetrics metrics = graphics.getFontMetrics();
        int maxWidth = HORIZONTAL_PLOT_LEFT - HORIZONTAL_LABEL_LEFT - HORIZONTAL_LABEL_GAP;
        List<String> lines = wrapLabel(category, metrics, maxWidth);
        int centerY = barY + barHeight / 2;
        int firstBaseline = centerY - (lines.size() - 1) * metrics.getHeight() / 2
                + (metrics.getAscent() - metrics.getDescent()) / 2;
        for (int index = 0; index < lines.size(); index++) {
            graphics.drawString(lines.get(index), HORIZONTAL_LABEL_LEFT,
                    firstBaseline + index * metrics.getHeight());
        }
    }

    private void drawBarValue(
            Graphics2D graphics,
            BigDecimal value,
            int barWidth,
            int barY,
            int barHeight,
            int plotWidth) {
        String label = value.stripTrailingZeros().toPlainString();
        FontMetrics metrics = graphics.getFontMetrics();
        int labelWidth = metrics.stringWidth(label);
        int labelY = barY + (barHeight + metrics.getAscent() - metrics.getDescent()) / 2;
        if (barWidth >= labelWidth + 20) {
            graphics.setColor(Color.WHITE);
            graphics.drawString(label, HORIZONTAL_PLOT_LEFT + barWidth - labelWidth - 10, labelY);
            return;
        }
        graphics.setColor(TEXT);
        int labelX = Math.min(
                HORIZONTAL_PLOT_LEFT + barWidth + 8,
                HORIZONTAL_PLOT_LEFT + plotWidth - labelWidth);
        graphics.drawString(label, labelX, labelY);
    }

    private List<String> wrapLabel(String value, FontMetrics metrics, int maxWidth) {
        String remaining = value == null ? "" : value.trim();
        if (remaining.isEmpty()) {
            return List.of("");
        }
        List<String> lines = new ArrayList<>();
        while (!remaining.isEmpty() && lines.size() < HORIZONTAL_LABEL_MAX_LINES) {
            if (metrics.stringWidth(remaining) <= maxWidth) {
                lines.add(remaining);
                break;
            }
            if (lines.size() == HORIZONTAL_LABEL_MAX_LINES - 1) {
                lines.add(ellipsize(remaining, metrics, maxWidth));
                break;
            }
            int breakIndex = fittingBreakIndex(remaining, metrics, maxWidth);
            lines.add(remaining.substring(0, breakIndex).trim());
            remaining = remaining.substring(breakIndex).trim();
        }
        return lines;
    }

    private int fittingBreakIndex(String value, FontMetrics metrics, int maxWidth) {
        int bestWhitespace = -1;
        for (int index = 1; index < value.length(); index++) {
            if (!Character.isWhitespace(value.charAt(index))) {
                continue;
            }
            if (metrics.stringWidth(value.substring(0, index)) > maxWidth) {
                break;
            }
            bestWhitespace = index;
        }
        if (bestWhitespace > 0) {
            return bestWhitespace;
        }
        int index = 1;
        while (index < value.length() && metrics.stringWidth(value.substring(0, index + 1)) <= maxWidth) {
            index++;
        }
        return index;
    }

    private String ellipsize(String value, FontMetrics metrics, int maxWidth) {
        String ellipsis = "…";
        int end = value.length();
        while (end > 1 && metrics.stringWidth(value.substring(0, end) + ellipsis) > maxWidth) {
            end--;
        }
        return end == value.length() ? value : value.substring(0, end).trim() + ellipsis;
    }

    private void drawGrid(
            Graphics2D graphics, BigDecimal max, int plotWidth, int plotHeight, String unit) {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        for (int index = 0; index <= 4; index++) {
            int y = TOP + plotHeight - plotHeight * index / 4;
            graphics.setColor(GRID);
            graphics.drawLine(LEFT, y, LEFT + plotWidth, y);
            graphics.setColor(TEXT);
            BigDecimal label = max.multiply(BigDecimal.valueOf(index))
                    .divide(BigDecimal.valueOf(4), 0, RoundingMode.HALF_UP);
            graphics.drawString(label.toPlainString(), 12, y + 5);
        }
        if (unit != null && !unit.isBlank()) {
            graphics.drawString(unit, 12, TOP - 16);
        }
    }

    private void drawCategoryLabels(
            Graphics2D graphics, List<String> categories, int plotWidth, int imageHeight) {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        graphics.setColor(TEXT);
        int visibleStep = Math.max(1, (int) Math.ceil(categories.size() / 8.0));
        int denominator = Math.max(1, categories.size() - 1);
        for (int index = 0; index < categories.size(); index += visibleStep) {
            String label = categories.get(index);
            if (label.length() > 14) {
                label = label.substring(0, 13) + "…";
            }
            int x = LEFT + plotWidth * index / denominator;
            graphics.drawString(label, Math.max(LEFT, x - 28), imageHeight - 38);
        }
    }

    private List<BigDecimal> firstValues(ResearchReportChart chart) {
        return chart.series().isEmpty() ? List.of() : chart.series().getFirst().values();
    }

    private BigDecimal maximum(List<BigDecimal> values) {
        return values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE).max(BigDecimal.ONE);
    }

    private int scaled(BigDecimal value, BigDecimal max, int height) {
        return value.max(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(height))
                .divide(max, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
