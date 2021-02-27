//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
ext.set("kotlin_version","1.4.20")
buildscript {
	repositories {
		mavenCentral()
		google()
		jcenter()
	}
	dependencies {
		//classpath(kotlin("gradle-plugin", version = "1.3.72"))
		// NOTE: Do not place your application dependencies here; they belong
		// in the individual module build.gradle files
	}
}
allprojects {
	repositories {
		mavenCentral()
		google()
		jcenter()
	}
}


task("clean"){
	delete(rootProject.buildDir)
}