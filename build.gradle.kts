import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
	id("com.github.johnrengelman.shadow") version "5.1.0"
	id( "com.github.ben-manes.versions") version "0.38.0"
	java
	kotlin("jvm") version "1.4.31"
	kotlin("plugin.spring") version "1.4.21"
}

group = "uk.gow.dwp.dataworks"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	jcenter()
	maven(url = "https://jitpack.io")
}

dependencies {
	implementation("com.beust:klaxon:5.4")
	implementation("com.github.dwp:dataworks-common-logging:0.0.6")
	implementation("com.google.code.gson:gson:2.8.6")
	implementation("io.prometheus:simpleclient:0.9.0")
	implementation("io.prometheus:simpleclient_caffeine:0.9.0")
	implementation("io.prometheus:simpleclient_logback:0.9.0")
	implementation("io.prometheus:simpleclient_pushgateway:0.9.0")
	implementation("io.prometheus:simpleclient_spring_web:0.9.0")
	implementation("org.apache.commons:commons-compress:1.20")
	implementation("org.apache.commons:commons-text:1.9")
	implementation("org.apache.hbase:hbase-client:1.4.13")
	implementation("org.apache.hbase:hbase-server:1.4.13")
	implementation("org.apache.httpcomponents:httpclient:4.5.13")
	implementation("org.bouncycastle:bcprov-ext-jdk15on:1.68")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.retry:spring-retry:1.3.1")
	implementation("org.springframework:spring-context:5.3.5")
	implementation("software.amazon.awssdk:s3:2.16.28")


}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

application {
	mainClassName = "uk.gov.dwp.dataworks.snapshot.SnapshotExporterApplicationKt"
}
