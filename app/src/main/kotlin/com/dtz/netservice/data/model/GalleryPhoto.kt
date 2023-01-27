package com.dtz.netservice.data.model

/**
 * Created by luis rafael on 28/03/18.
 */
class GalleryPhoto {

    var getPhotos: Boolean? = null
    var count: Int? = null
    var removePhotos: Boolean? = null
    var photoPath: String? = null

    constructor() {}

    constructor(getPhotos: Boolean?, count: Int?, removePhotos: Boolean?, photoPath: String?) {
        this.getPhotos = getPhotos
        this.count = count
        this.removePhotos = removePhotos
        this.photoPath = photoPath
    }

}