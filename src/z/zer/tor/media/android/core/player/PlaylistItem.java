/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package z.zer.tor.media.android.core.player;

import z.zer.tor.media.android.core.FileDescriptor;

public class PlaylistItem {

    private final FileDescriptor fd;

    public PlaylistItem(FileDescriptor fd) {
        this.fd = fd;
    }

    public FileDescriptor getFD() {
        return fd;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlaylistItem && fd.equals(((PlaylistItem) o).fd);
    }

    @Override
    public int hashCode() {
        return fd.hashCode();
    }
}
