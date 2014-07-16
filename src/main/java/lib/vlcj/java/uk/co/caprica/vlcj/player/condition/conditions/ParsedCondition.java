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

package lib.vlcj.java.uk.co.caprica.vlcj.player.condition.conditions;

import lib.vlcj.java.uk.co.caprica.vlcj.logger.Logger;
import lib.vlcj.java.uk.co.caprica.vlcj.player.MediaPlayer;
import lib.vlcj.java.uk.co.caprica.vlcj.player.condition.DefaultCondition;

/**
 * Implementation of a condition that waits for the media player to report that
 * media has been parsed.
 */
public class ParsedCondition extends DefaultCondition<Integer> {

    /**
     * Create a condition.
     *
     * @param mediaPlayer media player
     */
    public ParsedCondition(MediaPlayer mediaPlayer) {
        super(mediaPlayer);
    }

    @Override
    public final void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {
        Logger.debug("mediaParsedChange(mediaPlayer={},newStatus={})", mediaPlayer, newStatus);
        ready(newStatus);
    }
}
