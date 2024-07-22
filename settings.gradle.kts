pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        /*maven { setUrl("https://devrepo.kakao.com/nexus/content/groups/public/") }
        maven { setUrl("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }*/
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    //kakao
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/") }
    }

    //kakaomobility
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url  = java.net.URI("https://devrepo.kakaomobility.com/repository/kakao-mobility-android-knsdk-public/")}
        maven { url  = java.net.URI("https://www.jitpack.io") }
    }
}

rootProject.name = "smart-car"
include(":app")
