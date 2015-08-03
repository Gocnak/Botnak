package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PieChart extends JPanel {

    enum Type {
        STANDARD, SIMPLE_INDICATOR, GRADED_INDICATOR
    }

    private Type type = null; //the type of pie chart

    private ArrayList<Double> values;
    private ArrayList<Color> colors;

    private ArrayList<Double> gradingValues;
    private ArrayList<Color> gradingColors;

    double percent = 0; //percent is used for simple indicator and graded indicator

    public PieChart(int percent) {

        type = Type.SIMPLE_INDICATOR;
        this.percent = percent;
    }

    public PieChart(ArrayList<Double> values, ArrayList<Color> colors) {

        type = Type.STANDARD;

        this.values = values;
        this.colors = colors;
    }

    public PieChart(int percent, ArrayList<Double> gradingValues, ArrayList<Color> gradingColors) {
        type = Type.GRADED_INDICATOR;

        this.gradingValues = gradingValues;
        this.gradingColors = gradingColors;
        this.percent = percent;

    }

    @Override
    protected void paintComponent(Graphics g) {

        int width = getSize().width;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (type == Type.SIMPLE_INDICATOR) {

            //colours used for simple indicator
            Color backgroundColor = Color.WHITE;
            Color mainColor = Color.BLUE;

            g2d.setColor(backgroundColor);
            g2d.fillOval(0, 0, width, width);
            g2d.setColor(mainColor);
            Double angle = (percent / 100) * 360;
            g2d.fillArc(0, 0, width, width, -270, -angle.intValue());

        } else if (type == Type.STANDARD) {

            int lastPoint = -270;

            for (int i = 0; i < values.size(); i++) {
                g2d.setColor(colors.get(i));

                Double val = values.get(i);
                Double angle = (val / 100) * 360;

                g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());
                //System.out.println("fill arc " + lastPoint + " " + -angle.intValue());

                lastPoint = lastPoint + -angle.intValue();
            }
        } else if (type == Type.GRADED_INDICATOR) {

            int lastPoint = -270;

            double gradingAccum = 0;

            for (int i = 0; i < gradingValues.size(); i++) {
                g2d.setColor(gradingColors.get(i));
                Double val = gradingValues.get(i);
                gradingAccum = gradingAccum + val;
                Double angle = null;
                /**
                 * If the sum of the gradings is greater than the percent, then we want to recalculate
                 * the last wedge, and break out of drawing.
                 * */
                if (gradingAccum > percent) {

                    //System.out.println("gradingAccum > percent");

                    //get the previous accumulated segments. Segments minus last one
                    double gradingAccumMinusOneSegment = gradingAccum - val;

                    //make an adjusted calculation of the last wedge
                    angle = ((percent - gradingAccumMinusOneSegment) / 100) * 360;

                    g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());

                    lastPoint = lastPoint + -angle.intValue();

                    break;

                } else {

                    //System.out.println("normal");
                    angle = (val / 100) * 360;

                    g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());

                    //System.out.println("fill arc " + lastPoint + " " + -angle.intValue());

                    lastPoint = lastPoint + -angle.intValue();
                }
            }
        }
    }
}