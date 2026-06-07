plugins {
    id("java-library")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.80"
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
}

// NeoForge 1.21.1 port by beihaimc
// Original: CommissarMado/Frontline-Combat-Pack
version = "${project.property("mod_version")}-mc${project.property("minecraft_version")}"
group = "frontline.combat.fcp"

repositories {
    mavenLocal()
    mavenCentral()
    flatDir { dir("libs") }
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    maven {
        name = "Curios"
        url = uri("https://maven.theillusivec4.top/")
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        name = "Jared's Maven"
        url = uri("https://maven.blamejared.com/")
        content {
            includeGroup("mezz.jei")
        }
    }
}

base {
    archivesName.set(project.property("mod_id") as String)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

neoForge {
    version = project.property("neo_version") as String

    parchment {
        mappingsVersion = project.property("parchment_mappings_version") as String
        minecraftVersion = project.property("parchment_minecraft_version") as String
    }

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", project.property("mod_id") as String)
        }
        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", project.property("mod_id") as String)
        }
        create("data") {
            data()
            programArguments.addAll(
                "--mod", project.property("mod_id") as String,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
        configureEach {
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create(project.property("mod_id") as String) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main.get().resources {
    srcDir("src/generated/resources")
}

dependencies {
    // SBW is a local dependency - must be built from Mercurows/SuperbWarfare 1.21 branch
    // and placed in libs/ or installed to local maven
    implementation("software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.5")

    implementation("com.atsuishio.superbwarfare:Mercurows-SuperbWarfare-2262c45:0.8.9-mc1.21.1")

    runtimeOnly("top.theillusivec4.curios:curios-neoforge:9.2.0+1.21.1")
    compileOnly("top.theillusivec4.curios:curios-neoforge:9.2.0+1.21.1:api")

    implementation("thedarkcolour:kotlinforforge-neoforge:5.10.0")

    compileOnly("mezz.jei:jei-1.21.1-common-api:${project.property("jei_version")}")
    compileOnly("mezz.jei:jei-1.21.1-neoforge-api:${project.property("jei_version")}")
    runtimeOnly("mezz.jei:jei-${project.property("minecraft_version")}-neoforge:${project.property("jei_version")}")
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to project.property("minecraft_version"),
        "minecraft_version_range" to project.property("minecraft_version_range"),
        "neo_version" to project.property("neo_version"),
        "neo_version_range" to project.property("neo_version_range"),
        "loader_version_range" to project.property("loader_version_range"),
        "mod_id" to project.property("mod_id"),
        "mod_name" to project.property("mod_name"),
        "mod_license" to project.property("mod_license"),
        "mod_version" to project.property("mod_version"),
        "mod_authors" to project.property("mod_authors"),
        "mod_description" to project.property("mod_description")
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateModMetadata)
neoForge.ideSyncTask(generateModMetadata)

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("file://${project.projectDir}/repo")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("generateModMetadata"))
}

kotlin {
    jvmToolchain(21)
}
