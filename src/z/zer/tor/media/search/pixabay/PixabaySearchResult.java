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

package z.zer.tor.media.search.pixabay;

import z.zer.tor.media.search.AbstractSearchResult;
import z.zer.tor.media.search.CrawlableSearchResult;

/**
 * @author gubatron
 * @author aldenml
 */
final class PixabaySearchResult extends AbstractSearchResult implements CrawlableSearchResult {

    private final String apiUrl;

    PixabaySearchResult(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public String getDisplayName() {
        return apiUrl;
    }

    @Override
    public String getDetailsUrl() {
        return apiUrl;
    }

    @Override
    public String getSource() {
        return "Pixabay";
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
