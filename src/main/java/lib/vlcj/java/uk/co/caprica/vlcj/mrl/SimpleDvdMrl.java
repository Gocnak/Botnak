/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009, 2010, 2011, 2012, 2013, 2014 Caprica Software Limited.
 */

package lib.vlcj.java.uk.co.caprica.vlcj.mrl;

/**
 * Implementation of a media resource locator for DVDs without menus.
 * <p/>
 * This class provides a fluent API for initialising the MRL, e.g.
 * <p/>
 * <pre>
 * String mrl = new SimpleDvdMrl().device("/media/dvd")
 *                                .title(0)
 *                                .chapter(3)
 *                                .angle(1)
 *                                .value();
 * </pre>
 * This will generate <code>"dvdsimple:///media/dvd/@0:3:1"</code>.
 */
public class SimpleDvdMrl extends BaseDvdMrl {

    /**
     *
     */
    private static final String DVD_TYPE = "dvdsimple";

    /**
     *
     */
    public SimpleDvdMrl() {
        type(DVD_TYPE);
    }
}
