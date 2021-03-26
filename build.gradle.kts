import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
	id("com.github.johnrengelman.shadow") version "5.1.0"
	id( "com.github.ben-manes.versions") version "0.38.0"
	java
	kotlin("jvm") version "1.4.31"
}

group = "uk.gow.dwp.dataworks"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.apache.hbase:hbase-client:1.4.13")
	implementation("org.apache.hbase:hbase-server:1.4.13")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
