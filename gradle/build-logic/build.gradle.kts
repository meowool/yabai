plugins { `kotlin-dsl` }

dependencies {
  implementationOf(
    libs.gradle.kotlin.dsl,
    libs.kotlin.gradle.plugin,
    libs.android.gradle.plugin,

    platform(libs.kotlin.bom),
    // A workaround to enable version catalog usage in the convention plugin,
    // see https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    files(libs.javaClass.superclass.protectionDomain.codeSource.location),
  )
}
