pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        gradlePluginPortal()
        flatDir {
            dirs("libs") // .aar dosyalarının bulunduğu dizin
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        flatDir {
            dirs("libs") // Bu dizin, .aar dosyalarının bulunduğu dizini temsil etmeli
        }
    }
}
