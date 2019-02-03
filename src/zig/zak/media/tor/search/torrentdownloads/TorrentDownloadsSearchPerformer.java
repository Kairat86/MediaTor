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

package zig.zak.media.tor.search.torrentdownloads;

import zig.zak.media.tor.search.CrawlableSearchResult;
import zig.zak.media.tor.search.SearchMatcher;
import zig.zak.media.tor.search.torrent.TorrentRegexSearchPerformer;

/**
 * @author alejandroarturom
 */
public class TorrentDownloadsSearchPerformer extends TorrentRegexSearchPerformer<TorrentDownloadsSearchResult> {

    private static final int MAX_RESULTS = 20;
    private static final String REGEX = "(?is)<a href=\"/torrent/([0-9]*?/.*?)\">*?";
    private static final String HTML_REGEX = "(?is).*?<li><a rel=\"nofollow\" href=\"http://itorrents.org/torrent/(?<torrentid>.*?).torrent?(.*?)\">.*?"  +
            "<span>Name:.?</span>(?<filename>.*?)(<a.*>)?</a></p></div>.*?"   +
            "<span>Total Size:.?</span>(?<filesize>.*?)&nbsp;(?<unit>[A-Z]+)</p></div>.*?"  +
            "<span>Magnet:.*?</span>.*?<a href=\"(?<magnet>.*?)\".*?"  +
            "<span>Seeds:.?</span>.?(?<seeds>\\d*?)</p></div>.*?" +
            "<span>Torrent added:.?</span>.?(?<time>[0-9\\-]+).*</p></div>.*?";

    public TorrentDownloadsSearchPerformer(String domainName, long token, String keywords, int timeout) {
        super(domainName, token, keywords, timeout, 1, 2 * MAX_RESULTS, MAX_RESULTS, REGEX, HTML_REGEX);
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        String transformedKeywords = encodedKeywords.replace("0%20", "+");
        return "https://" + getDomainName() + "/search/?search=" + transformedKeywords;
    }

    @Override
    public CrawlableSearchResult fromMatcher(SearchMatcher matcher) {
        String itemId = matcher.group(1);
        return new TorrentDownloadsTempSearchResult(getDomainName(), itemId);
    }

    @Override
    protected int htmlPrefixOffset(String html) {
        int offset = html.indexOf("Torrent Search Results<span>");
        return offset > 0 ? offset : 0;
    }

    @Override
    protected int htmlSuffixOffset(String html) {
        int offset = html.indexOf("<h1>RECENT SEARCHES");
        return offset > 0 ? offset : 0;
    }

    @Override
    protected TorrentDownloadsSearchResult fromHtmlMatcher(CrawlableSearchResult sr, SearchMatcher matcher) {
        return new TorrentDownloadsSearchResult(getDomainName(), sr.getDetailsUrl(), matcher);
    }

    @Override
    protected boolean isValidHtml(String html) {
        return html != null && !html.contains("Cloudflare");
    }


/**
 public static void main(String[] args) throws Exception {
 //SEARCH_RESULTS_REGEX TEST CODE

 //         String resultsHTML = FileUtils.readFileToString(new File("/Users/alejandroarturom/Desktop/torrentdownloads-results.html"));
 //         final Pattern resultsPattern = Pattern.compile(SEARCH_RESULTS_REGEX);
 //
 //         final SearchMatcher matcher = SearchMatcher.from(resultsPattern.matcher(resultsHTML));
 //         while (matcher.find()) {
 //         System.out.println(matcher.group(1));
 //         }


 String resultHTML = FileUtils.readFileToString(new File("/Users/alejandroarturom/Desktop/testa.html"));
 final Pattern detailPattern = Pattern.compile(TORRENT_DETAILS_PAGE_REGEX);
 final SearchMatcher detailMatcher = SearchMatcher.from(detailPattern.matcher(resultHTML));

 if (detailMatcher.find()) {
 System.out.println("TorrentID: " + detailMatcher.group("torrentid"));
 System.out.println("File name: " + detailMatcher.group("filename"));
 System.out.println("Size: " + detailMatcher.group("filesize"));
 System.out.println("Unit: " + detailMatcher.group("unit"));
 System.out.println("Date: " + detailMatcher.group("time"));
 System.out.println("Seeds: " + detailMatcher.group("seeds"));
 } else {
 System.out.println("No detail matched.");
 }

 }
 */
}

