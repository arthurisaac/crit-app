package com.dtz.netservice.data.model

/**
 * Created by luis rafael on 28/03/18.
 */
class Image {

    var name: String? = null
    var file: String? = null

    constructor() {}

    constructor(name: String?, file: String?) {
        this.name = name
        this.file = file
    }

}