package com.dtz.netservice.services.connect

import com.dtz.netservice.di.PerService
import com.dtz.netservice.services.base.InterfaceInteractorService

/**
 * Created by luis rafael on 27/03/18.
 */
@PerService
interface InterfaceInteractorConnect<S : InterfaceServiceConnect> : InterfaceInteractorService<S> {

    fun startContacts()
    fun startCallsSending()
    fun startMessages()
    fun startCallLogs()

}