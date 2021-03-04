import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`java-library`
	kotlin("jvm") version "1.4.20"
	kotlin("kapt") version "1.4.20"
	maven
}

group = "com.github.lesa1127"
version = "0.0.5-SNAPSHOT"

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.20")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.20")
	implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
	implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testCompileOnly("junit:junit:4.13")
	testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}
tasks.withType<JavaCompile>{
	options.encoding="UTF-8"
}
