import pl.allegro.tech.build.axion.release.domain.VersionConfig

plugins {
    id("pl.allegro.tech.build.axion-release")
    id("io.github.gradle-nexus.publish-plugin")
}

group = "io.wttech.graal.templating"
version = scmVersion.version

nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

configure<VersionConfig> {
    checks.aheadOfRemote = false
    checks.isSnapshotDependencies = false

    val antora = "templating/src/main/docs/antora/antora.yml"
    val bridgePackageJson = "templating/src/main/js/bridge/package.json"
    val bridgeReactPackageJson = "templating/src/main/js/bridge-react/package.json"

    hooks.pre("fileUpdate", mutableMapOf(
            "files" to listOf(antora),
            "pattern" to KotlinClosure2<String, pl.allegro.tech.build.axion.release.domain.hooks.HookContext, String>({ _, c ->
                c.addCommitPattern(antora)
                "version: '(.*)'" }),
            "replacement" to KotlinClosure2<String, pl.allegro.tech.build.axion.release.domain.hooks.HookContext, String>({ v, _ -> "version: '$v'" })
    ))
    hooks.pre("fileUpdate", mutableMapOf(
            "files" to listOf(bridgePackageJson, bridgeReactPackageJson),
            "pattern" to KotlinClosure2<String, pl.allegro.tech.build.axion.release.domain.hooks.HookContext, String>({ _, c ->
                c.addCommitPattern(bridgePackageJson)
                c.addCommitPattern(bridgeReactPackageJson)
                "\"version\": \"(.*)\"" }),
            "replacement" to KotlinClosure2<String, pl.allegro.tech.build.axion.release.domain.hooks.HookContext, String>({ v, _ -> "\"version\": \"$v\"" })
    ))
    hooks.pre("commit")
}
