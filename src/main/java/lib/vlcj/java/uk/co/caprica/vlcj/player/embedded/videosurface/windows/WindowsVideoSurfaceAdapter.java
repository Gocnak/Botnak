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

package lib.vlcj.java.uk.co.caprica.vlcj.player.embedded.videosurface.windows;

import com.sun.jna.Pointer;
import lib.vlcj.java.uk.co.caprica.vlcj.binding.LibVlc;
import lib.vlcj.java.uk.co.caprica.vlcj.logger.Logger;
import lib.vlcj.java.uk.co.caprica.vlcj.player.MediaPlayer;
import lib.vlcj.java.uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter;

/**
 * Implementation of a video surface adapter for Windows.
 */
public class WindowsVideoSurfaceAdapter implements VideoSurfaceAdapter {

    /**
     * Serial version.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void attach(LibVlc libvlc, MediaPlayer mediaPlayer, long componentId) {
        Logger.debug("attach(componentId={})", componentId);
        libvlc.libvlc_media_player_set_hwnd(mediaPlayer.mediaPlayerInstance(), Pointer.createConstant(componentId));
    }
}
