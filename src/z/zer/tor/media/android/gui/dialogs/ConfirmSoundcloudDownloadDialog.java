/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2017, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package z.zer.tor.media.android.gui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import z.zer.tor.media.R;
import z.zer.tor.media.android.gui.tasks.AsyncStartDownload;
import z.zer.tor.media.android.gui.util.UIUtils;
import z.zer.tor.media.search.SearchResult;
import z.zer.tor.media.search.soundcloud.SoundCloudSearchResult;
import z.zer.tor.media.util.JsonUtils;
import z.zer.tor.media.util.Ref;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfirmSoundcloudDownloadDialog extends AbstractConfirmListDialog<SoundCloudSearchResult> {

    public static ConfirmSoundcloudDownloadDialog newInstance(
            Context ctx,
            String dialogTitle,
            String dialogText,
            List<SoundCloudSearchResult> listData) {
        ConfirmSoundcloudDownloadDialog dlg = new ConfirmSoundcloudDownloadDialog();
        SoundcloudSearchResultList srList = new SoundcloudSearchResultList();
        srList.listData = listData;

        dlg.prepareArguments(R.drawable.download_icon, dialogTitle, dialogText, JsonUtils.toJson(srList),
                SelectionMode.MULTIPLE_SELECTION);

        dlg.setOnYesListener(new OnStartDownloadsClickListener(ctx, dlg));
        return dlg;
    }

    private static void startDownloads(Context ctx, List<? extends SearchResult> srs) {
        if (srs != null && !srs.isEmpty()) {
            for (SearchResult sr : srs) {
                new AsyncStartDownload(ctx, sr);
            }
            UIUtils.showTransfersOnDownloadStart(ctx);
        }
    }

    @Override
    public List<SoundCloudSearchResult> deserializeData(String listDataInJSON) {
        SoundcloudSearchResultList srList = JsonUtils.toObject(listDataInJSON, SoundcloudSearchResultList.class);
        return srList.listData;
    }

    @Override
    public ConfirmListDialogDefaultAdapter<SoundCloudSearchResult> createAdapter(Context context,
                                                                                 List<SoundCloudSearchResult> listData,
                                                                                 SelectionMode selectionMode,
                                                                                 Bundle bundle) {
        return new SoundcloudPlaylistConfirmListDialogAdapter(context, listData, selectionMode);
    }


    @Override
    protected OnStartDownloadsClickListener createOnYesListener() {
        return new OnStartDownloadsClickListener(getActivity(), this);
    }

    private static class SoundcloudSearchResultList {
        List<SoundCloudSearchResult> listData;
    }

    // TODO: this class needs heavy refactor/cleanup
    private static class OnStartDownloadsClickListener implements View.OnClickListener {
        private final WeakReference<Context> ctxRef;
        private final WeakReference<AbstractConfirmListDialog> dlgRef;

        OnStartDownloadsClickListener(Context ctx, AbstractConfirmListDialog dlg) {
            ctxRef = new WeakReference<>(ctx);
            dlgRef = new WeakReference<>(dlg);
        }

        @Override
        public void onClick(View v) {
            if (Ref.alive(ctxRef) && Ref.alive(dlgRef)) {
                final AbstractConfirmListDialog dlg = dlgRef.get();

                final AbstractConfirmListDialog.SelectionMode selectionMode = dlg.getSelectionMode();
                List<SoundCloudSearchResult> results = (selectionMode == AbstractConfirmListDialog.SelectionMode.NO_SELECTION) ?
                        (List<SoundCloudSearchResult>) dlg.getList() :
                        new ArrayList<>();

                if (results.isEmpty()) {
                    results.addAll(dlg.getChecked());
                }

                if (!results.isEmpty()) {
                    startDownloads(ctxRef.get(), results);
                    dlg.dismiss();
                }
            }
        }
    }

    private static class SoundcloudPlaylistConfirmListDialogAdapter extends ConfirmListDialogDefaultAdapter<SoundCloudSearchResult> {
        SoundcloudPlaylistConfirmListDialogAdapter(Context context, List list, AbstractConfirmListDialog.SelectionMode selectionMode) {
            super(context, list, selectionMode);
        }

        @Override
        public CharSequence getItemTitle(SoundCloudSearchResult data) {
            return data.getDisplayName();
        }

        @Override
        public long getItemSize(SoundCloudSearchResult data) {
            return data.getSize();
        }

        @Override
        public CharSequence getItemThumbnailUrl(SoundCloudSearchResult data) {
            return data.getThumbnailUrl();
        }

        @Override
        public int getItemThumbnailResourceId(SoundCloudSearchResult data) {
            return -1;
        }

        @Override
        public String getCheckedSum() {
            if (checked==null || checked.isEmpty()) {
                return null;
            }

            long totalBytes = 0;
            for (SoundCloudSearchResult sr : (Set<SoundCloudSearchResult>) checked) {
                totalBytes += sr.getSize();
            }

            return UIUtils.getBytesInHuman(totalBytes);
        }
    }
}
