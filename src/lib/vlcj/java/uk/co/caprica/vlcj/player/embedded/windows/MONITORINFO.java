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

package lib.vlcj.java.uk.co.caprica.vlcj.player.embedded.windows;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.RECT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulation of native monitor information.
 */
public class MONITORINFO extends Structure {

    /**
     *
     */
    private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList("cbSize", "rcMonitor", "rcWork", "dwFlags"));

    /**
     *
     */
    public MONITORINFO() {
        this.cbSize = new DWORD(size());
    }

    @Override
    protected List<?> getFieldOrder() {
        return FIELD_ORDER;
    }

    public DWORD cbSize;

    public RECT rcMonitor;

    public RECT rcWork;

    public DWORD dwFlags;
}
