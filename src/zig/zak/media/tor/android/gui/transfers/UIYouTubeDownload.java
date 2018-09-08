/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
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

package zig.zak.media.tor.android.gui.transfers;

import zig.zak.media.tor.android.gui.services.Engine;
import zig.zak.media.tor.search.youtube.YouTubeCrawledSearchResult;
import zig.zak.media.tor.transfers.TransferState;
import zig.zak.media.tor.transfers.YouTubeDownload;

/**
 * @author gubatron
 * @author aldenml
 */
public final class UIYouTubeDownload extends YouTubeDownload {

    private final TransferManager manager;
    private long demuxedDownloadSize = -1;

    UIYouTubeDownload(TransferManager manager, YouTubeCrawledSearchResult sr) {
        super(sr);
        this.manager = manager;
    }

    @Override
    public void remove(boolean deleteData) {
        super.remove(deleteData);

        manager.remove(this);
    }

    @Override
    protected void onComplete() {
        manager.incrementDownloadsToReview();
        Engine.instance().notifyDownloadFinished(getDisplayName(), savePath);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UIYouTubeDownload && getName().equals(((UIYouTubeDownload) obj).getName());
    }

    @Override
    public long getSize() {
        if (isDemuxDownload()) {
            TransferState state = getState();
            if (state == TransferState.SCANNING ||
                state == TransferState.COMPLETE) {
                if (demuxedDownloadSize == -1) {
                    demuxedDownloadSize = savePath.length();
                }
                return demuxedDownloadSize;
            } else {
                return super.getSize();
            }
        }
        return super.getSize();
    }
}