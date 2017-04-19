package com.bytetobyte.xwallet.network.api.models;

import java.util.List;

/**
 * Created by bruno on 12.04.17.
 */
public class TwitterEntities {
    private List<TwitterUrl> urls;
    private List<TwitterMedia> media;

    public List<TwitterMedia> getMedia() {
        return media;
    }

    /**
     *
     * @return
     */
    public TwitterMedia findPhoto() {
        TwitterMedia result = null;

        if (media != null) {
            for (TwitterMedia m : media) {
                System.out.println(" media type : " + m.getType());

                if (m.getType().equalsIgnoreCase("photo")) {
                    result = m;

                    System.out.println(" photo : " + result.getMediaUrlHttps());

                    //break;
                }
            }
        }

        return result;
    }
}
