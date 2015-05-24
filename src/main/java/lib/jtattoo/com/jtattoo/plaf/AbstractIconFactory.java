/*
* Copyright (c) 2002 and later by MH Software-Entwicklung. All Rights Reserved.
*  
* JTattoo is multiple licensed. If your are an open source developer you can use
* it under the terms and conditions of the GNU General Public License version 2.0
* or later as published by the Free Software Foundation.
*  
* see: gpl-2.0.txt
* 
* If you pay for a license you will become a registered user who could use the
* software under the terms and conditions of the GNU Lesser General Public License
* version 2.0 or later with classpath exception as published by the Free Software
* Foundation.
* 
* see: lgpl-2.0.txt
* see: classpath-exception.txt
* 
* Registered users could also use JTattoo under the terms and conditions of the 
* Apache License, Version 2.0 as published by the Apache Software Foundation.
*  
* see: APACHE-LICENSE-2.0.txt
*/

package lib.jtattoo.com.jtattoo.plaf;

import javax.swing.*;

/**
 * @author Michael Hagen
 */
public interface AbstractIconFactory {

    Icon getOptionPaneErrorIcon();

    Icon getOptionPaneWarningIcon();

    Icon getOptionPaneInformationIcon();

    Icon getOptionPaneQuestionIcon();

    Icon getFileChooserDetailViewIcon();

    Icon getFileChooserHomeFolderIcon();

    Icon getFileChooserListViewIcon();

    Icon getFileChooserNewFolderIcon();

    Icon getFileChooserUpFolderIcon();

    Icon getMenuIcon();

    Icon getIconIcon();

    Icon getMaxIcon();

    Icon getMinIcon();

    Icon getCloseIcon();

    Icon getPaletteCloseIcon();

    Icon getRadioButtonIcon();

    Icon getCheckBoxIcon();

    Icon getComboBoxIcon();

    Icon getTreeComputerIcon();

    Icon getTreeFloppyDriveIcon();

    Icon getTreeHardDriveIcon();

    Icon getTreeFolderIcon();

    Icon getTreeLeafIcon();

    Icon getTreeCollapsedIcon();

    Icon getTreeExpandedIcon();

    Icon getMenuArrowIcon();

    Icon getMenuCheckBoxIcon();

    Icon getMenuRadioButtonIcon();

    Icon getUpArrowIcon();

    Icon getDownArrowIcon();

    Icon getLeftArrowIcon();

    Icon getRightArrowIcon();

    Icon getSplitterUpArrowIcon();

    Icon getSplitterDownArrowIcon();

    Icon getSplitterLeftArrowIcon();

    Icon getSplitterRightArrowIcon();

    Icon getSplitterHorBumpIcon();

    Icon getSplitterVerBumpIcon();

    Icon getThumbHorIcon();

    Icon getThumbVerIcon();

    Icon getThumbHorIconRollover();

    Icon getThumbVerIconRollover();
}