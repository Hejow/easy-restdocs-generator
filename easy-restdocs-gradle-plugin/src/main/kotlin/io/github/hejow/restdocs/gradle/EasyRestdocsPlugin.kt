package io.github.hejow.restdocs.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

open class EasyRestdocsPlugin : Plugin<Project> {
    private fun <T : EasyRestdocsTask> T.applyConfiguration(): T {
        dependsOn("check")
        group = "documentation"
        description = "generate swaggerUI from rest-docs snippets"
        return this
    }

    override fun apply(project: Project) {
        with(project) {
            afterEvaluate {
                tasks.create("easyRestdocs", EasyRestdocsTask::class.java).applyConfiguration()
            }
        }
    }
}
