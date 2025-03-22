plugins {
    id("java")
}

group = "org.Youtify"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.apis:google-api-services-youtube:v3-rev222-1.25.0")
    implementation("org.springframework:spring-web:6.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
}

tasks.test {
    useJUnitPlatform()
}