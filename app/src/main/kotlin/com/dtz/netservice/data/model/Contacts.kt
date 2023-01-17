package com.dtz.netservice.data.model

/**
 * Created by luis rafael on 28/03/18.
 */
class Contacts {

    var contact: String? = null
    var phoneNumber:String?=null

    constructor() {}

    constructor(contact:String?,phoneNumber: String?) {
        this.contact = contact
        this.phoneNumber = phoneNumber
    }

}