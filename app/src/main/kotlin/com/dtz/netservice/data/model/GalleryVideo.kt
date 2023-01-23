package com.dtz.netservice.data.model

/**
 * Created by luis rafael on 28/03/18.
 */
class GalleryVideo {

    var getVideos: Boolean? = null
    var count: Int? = null

    constructor() {}

    constructor(getVideos: Boolean?, count: Int?) {
        this.getVideos = getVideos
        this.count = count
    }

}