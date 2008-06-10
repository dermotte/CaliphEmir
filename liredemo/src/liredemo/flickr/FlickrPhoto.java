package liredemo.flickr;

import java.awt.image.BufferedImage;

/**
 * ...
 * Date: 10.06.2008
 * Time: 09:36:39
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FlickrPhoto {
    String title, url, photourl;
    BufferedImage img = null;
    
    public FlickrPhoto(String title, String url, String photourl) {
        this.title = title;
        this.url = url;
        this.photourl = photourl;
    }

    public String toString() {
        return title + ": " + url + " (" + photourl + ")";
    }
}
