package com.dtz.netservice.data.model

/**
 * Created by luis rafael on 28/03/18.
 */
class Photo {

    var nameRandom: String? = null
    var dateTime: String? = null
    var urlPhoto: String? = null
    var location: MutableMap<String, Any>? = null

    constructor() {}

    constructor(nameRandom: String?, dateTime: String?, urlPhoto: String?, location: MutableMap<String, Any>?) {
        this.nameRandom = nameRandom
        this.dateTime = dateTime
        this.urlPhoto = urlPhoto
        this.location = location
    }

}