package com.dtz.netservice.data.model

import java.util.UUID

/**
 * Created by luis rafael on 11/03/18.
 */
class Location {

    var latitude: Double? = null
    var longitude: Double? = null
    var address: String? = null
    var dateTime: String? = null
    var uuid: String? = null

    constructor() {}

    constructor(uuid: String, latitude: Double, longitude: Double, address: String, dateTime: String) {
        this.uuid = uuid
        this.latitude = latitude
        this.longitude = longitude
        this.address = address
        this.dateTime = dateTime
    }
}
