package gui;

import util.Constants;
import util.misc.Vote;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

public class PieChart extends JPanel
{
    private java.util.List<Double> values = null;

    public PieChart()
    {
        super();
    }

    /**
     * @param options
     */
    public void initialize(java.util.List<Vote.Option> options)
    {
        values = options.stream().map(o -> 0.0).collect(Collectors.toList());
    }

    /**
     *
     * @param v
     */
    public void update(Vote v)
    {
        for (int i = 0; i < v.options.size(); i++)
        {
            values.set(i, (double) v.options.get(i).getCount() / (double) v.getTotalVotes());
        }

        repaint();
    }

    /**
     *
     */
    public void clear()
    {
        if (values != null)
            values.clear();
    }

    /**
     *
     * @param g
     */
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        int width = getSize().width;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int lastPoint = -270;

        if (values != null)
        {
            for (int i = 0; i < values.size(); i++)
            {
                g2d.setColor(Constants.colorArr[i]);
                Double val = values.get(i);
                Double angle = (val / (val <= 1.0d ? 1.0d : 100d)) * 360.0d;
                g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());
                lastPoint = lastPoint + -angle.intValue();
            }
        }
    }
}