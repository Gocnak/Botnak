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

package lib.vlcj.java.uk.co.caprica.vlcj.binding.internal;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Description of a module.
 */
public class libvlc_module_description_t extends Structure {

    /**
     *
     */
    private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList("psz_name", "psz_shortname", "psz_longname", "psz_help", "p_next"));

    public static class ByReference extends libvlc_module_description_t implements Structure.ByReference {
    }

    public String psz_name;
    public String psz_shortname;
    public String psz_longname;
    public String psz_help;
    public libvlc_module_description_t.ByReference p_next;

    @Override
    protected List<String> getFieldOrder() {
        return FIELD_ORDER;
    }
}
