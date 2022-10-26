package com.koeltv.databasemanager

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.PrintStream

class Logger(private val stream: PrintStream): PropertyChangeListener {
    override fun propertyChange(evt: PropertyChangeEvent) {
        stream.println("New event: ${evt.propertyName}")
        stream.println("${evt.newValue}\n")
    }
}