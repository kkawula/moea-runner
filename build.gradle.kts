plugins {
    base
}

subprojects {
    repositories {
        mavenCentral()
    }
}


tasks.register("createBinaries") {
    dependsOn(subprojects.map { it.tasks.matching { task -> task.name == "installDist" } })
}