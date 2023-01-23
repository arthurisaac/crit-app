package com.dtz.netservice.data.model

/**
 * Created by luis rafael on 28/03/18.
 */
class GalleryPhoto {

    var getPhotos: Boolean? = null
    var count: Int? = null

    constructor() {}

    constructor(getPhotos: Boolean?, count: Int?) {
        this.getPhotos = getPhotos
        this.count = count
    }

}