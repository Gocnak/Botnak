package gui;

import sun.swing.DefaultLookup;
import util.Constants;
import util.misc.Vote;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by Nick on 4/29/2017.
 * <p>
 * Created to override text color of the vote list
 */
public class VoteCellRenderer extends DefaultListCellRenderer implements ListCellRenderer<Object>
{
    private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        setComponentOrientation(list.getComponentOrientation());

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index)
        {

            bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
            fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

            isSelected = true;
        }

        if (isSelected)
        {
            setBackground(bg == null ? list.getSelectionBackground() : bg);
            //setForeground(fg == null ? list.getSelectionForeground() : fg);
            setForeground(Constants.colorArr[index % Constants.colorArr.length]);
        } else
        {
            setBackground(list.getBackground());
            //setForeground(list.getForeground());
            setForeground(Constants.colorArr[index % Constants.colorArr.length]);
        }

        if (value instanceof Icon)
        {
            setIcon((Icon) value);
            setText("");
        } else if (value instanceof Vote.Option)
        {
            // Calculate the string based on the data in the option
            Vote.Option opt = (Vote.Option) value;
            double percent;
            int total = opt.getParent().getTotalVotes();
            if (total > 0)
                percent = ((double) opt.getCount() / (double) total) * 100.0;
            else
                percent = 0.0;
            String toSet = String.format("%s: %d (%.2f%%)", opt.getName(), opt.getCount(), percent);
            setText(toSet);
        } else
        {
            setIcon(null);
            setText((value == null) ? "" : value.toString());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());

        Border border = null;
        if (cellHasFocus)
        {
            if (isSelected)
            {
                border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
            }
            if (border == null)
            {
                border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder");
            }
        } else
        {
            border = NO_FOCUS_BORDER;
        }
        setBorder(border);

        return this;
    }
}