package com.anatawa12.crashReportPublisher

import net.minecraft.launchwrapper.ITweaker
import net.minecraft.launchwrapper.LaunchClassLoader
import java.io.File

@Suppress("unused")
class CrashReportPublisherTweaker : ITweaker {
    override fun acceptOptions(args: MutableList<String>?, gameDir: File?, assetsDir: File?, profile: String?) {
        loadConfig(gameDir ?: File("."))
    }

    override fun injectIntoClassLoader(classLoader: LaunchClassLoader) {
        classLoader.registerTransformer(CrashReportPublisherClassTransformer::class.java.name)
    }

    override fun getLaunchTarget(): String? = null

    override fun getLaunchArguments(): Array<String> = emptyArray()
}
