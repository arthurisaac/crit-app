package com.dtz.netservice.data.model


/**
 * Created by luis rafael on 27/03/18.
 */
class AudioRecording {

    var audioFile: String? = null
    var dateTime: String? = null

    constructor() {}

    constructor(audioFile: String?, dateTime: String?) {
        this.audioFile = audioFile
        this.dateTime = dateTime
    }

}