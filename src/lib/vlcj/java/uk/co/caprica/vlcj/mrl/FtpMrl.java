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
 * Implementation of an HTTP media resource locator.
 * <p/>
 * This class provides a fluent API for initialising the MRL, e.g.
 * <p/>
 * <pre>
 * String mrl = new FtpMrl().host("www.myhost.com")
 *                          .port("21")
 *                          .path("/media/example.mp4")
 *                          .value();
 * </pre>
 * This will generate <code>"ftp://www.myhost.com:21/media/example.mp4"</code>.
 */
public class FtpMrl extends UrlMrl {

    /**
     *
     */
    private static final String FTP_TYPE = "ftp";

    /**
     *
     */
    public FtpMrl() {
        type(FTP_TYPE);
    }
}
