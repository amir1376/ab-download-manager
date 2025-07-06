plugins {
    // this project is used in both gradle and main project
    // the gradle version usually is lower than our project kotlin version
    // so we use the version that is used in gradle to fix version compatibility
    kotlin("jvm") version embeddedKotlinVersion apply false
}
