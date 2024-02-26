package io.github.hejow.restdocs.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

open class EasyRestdocsTask : DefaultTask() {
    @Input
    @Optional
    var servers: List<String> = listOf()

    @Input
    @Optional
    var title: String? = null

    @Input
    @Optional
    var apiDescription: String? = null

    @Input
    @Optional
    var apiVersion: String? = null

    @Input
    @Optional
    var outputFileNamePrefix: String? = null

    @TaskAction
    fun generate() {
        print(this.toString())
    }
}
