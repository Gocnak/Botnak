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

package lib.vlcj.java.uk.co.caprica.vlcj.medialist.events;

import lib.vlcj.java.uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import lib.vlcj.java.uk.co.caprica.vlcj.medialist.MediaList;
import lib.vlcj.java.uk.co.caprica.vlcj.medialist.MediaListEventListener;

/**
 * Encapsulation of a media list will delete item event.
 */
class MediaListWillDeleteItemEvent extends AbstractMediaListEvent {

    /**
     * Native media instance that will deleted.
     */
    private final libvlc_media_t mediaInstance;

    /**
     * Index from which the item will be deleted.
     */
    private final int index;

    /**
     * Create a media list event.
     *
     * @param mediaList     media list the event relates to
     * @param mediaInstance native media instance that will be added
     * @param index         index from which the item will be deleted
     */
    MediaListWillDeleteItemEvent(MediaList mediaList, libvlc_media_t mediaInstance, int index) {
        super(mediaList);
        this.mediaInstance = mediaInstance;
        this.index = index;
    }

    @Override
    public void notify(MediaListEventListener listener) {
        listener.mediaListWillDeleteItem(mediaList, mediaInstance, index);
    }
}
