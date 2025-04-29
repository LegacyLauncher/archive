package net.legacylauncher.gradle

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.headObject
import aws.sdk.kotlin.services.s3.model.ChecksumAlgorithm
import aws.sdk.kotlin.services.s3.model.NotFound
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import java.security.MessageDigest
import java.util.Base64

abstract class LegacyLauncherS3UploadTask : DefaultTask() {
    @get:Input
    val region: Property<String> = project.objects.property<String>().convention(
        project.eitherProperty("legacylauncher.deploy.region", "LL_S3_DEPLOY_REGION")
    )

    @get:Input
    val endpoint: Property<String> = project.objects.property<String>().convention(
        project.eitherProperty("legacylauncher.deploy.endpoint", "LL_S3_DEPLOY_ENDPOINT")
    )

    @get:Input
    val accessKey: Property<String> = project.objects.property<String>().convention(
        project.eitherProperty("legacylauncher.deploy.accessKey", "LL_S3_DEPLOY_ACCESS_KEY")
    )

    @get:Input
    val secretKey: Property<String> = project.objects.property<String>().convention(
        project.eitherProperty("legacylauncher.deploy.secretKey", "LL_S3_DEPLOY_SECRET_KEY")
    )

    @get:Input
    val version: Property<String> = project.objects.property<String>().convention(
        project.provider { project.version.toString() }
    )

    @get:Input
    val fileName: Property<String> = project.objects.property<String>().convention("")

    @get:Input
    abstract val bucket: Property<String>

    @get:Input
    val brandPrefix: Property<String> = project.objects.property<String>().convention(
        project.provider {
            queryBrand()
        }
    )

    @get:Input
    abstract val entityPrefix: Property<String>

    init {
        group = "deploy"
    }

    @OptIn(DelicateCoroutinesApi::class)
    @TaskAction
    fun run() {
        val files: FileCollection
        val singleFileName: String?
        if (this.fileName.get().isNotEmpty()) {
            files = project.objects.fileCollection().from(this.inputs.files.singleFile)
            singleFileName = this.fileName.get()
        } else {
            files = this.inputs.files.asFileTree
            singleFileName = null
        }
        val bucket = this.bucket.get()
        val entity = entityPrefix.filter { it.isNotEmpty() }.map { "/$it" }.orElse("").get()
        val prefix = "${brandPrefix.get()}$entity"
        S3Client {
            region = this@LegacyLauncherS3UploadTask.region.get()
            endpointUrl = Url.parse(this@LegacyLauncherS3UploadTask.endpoint.get())
            credentialsProvider = StaticCredentialsProvider(Credentials(accessKey.get(), secretKey.get()))
        }.use { s3 ->
            fun upload(file: File, key: String) = runBlocking {
                val localSha256 = generateBase64Checksum(file)
                try {
                    val response = s3.headObject {
                        this.bucket = bucket
                        this.key = key
                    }
                    if (response.metadata?.get("sha256") == localSha256) {
                        logger.debug("Skipping: {}", key)
                        return@runBlocking
                    }
                } catch (_: NotFound) {
                }
                println("Uploading to $bucket: $key")
                s3.putObject {
                    this.bucket = bucket
                    this.key = key
                    this.metadata = mutableMapOf(
                        "legacylauncher-brand" to queryBrand(),
                        "legacylauncher-version" to version.get(),
                        "sha256" to localSha256,
                    )
                    this.checksumAlgorithm = ChecksumAlgorithm.Sha256
                    this.checksumSha256 = localSha256
                    this.contentType = detectContentType(file)
                    this.body = file.asByteStream()
                }
            }
            if (files is FileTree) {
                files.visit {
                    if (isDirectory) return@visit
                    val key = "$prefix/$path"
                    upload(file, key)
                }
            } else {
                files.forEach { file ->
                    val path = singleFileName ?: file.name
                    val key = "$prefix/$path"
                    upload(file, key)
                }
            }
        }
    }

    private fun detectContentType(file: File): String? {
        return when (file.extension) {
            "sha256" -> "text/plain"
            "json" -> "application/json"
            else -> null
        }
    }

    private fun generateBase64Checksum(file: File, algorithm: String = "SHA-256"): String = file.inputStream().use { inputStream ->
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } >= 0) {
            digest.update(buffer, 0, read)
        }
        Base64.getEncoder().encodeToString(digest.digest())
    }

    private fun queryBrand() = project.extensions.getByType(LegacyLauncherBrandExtension::class.java).brand.get()

    companion object {
        fun Project.eitherProperty(gradleProp: String, systemProp: String) = project.provider {
            var prop = project.providers.gradleProperty(gradleProp).orNull
            if (prop == null) {
                prop = project.providers.environmentVariable(systemProp).get()
            }
            prop
        }

        fun Project.bucketProperty(bucket: Buckets): Provider<String> {
            return eitherProperty(bucket.gradleProp, bucket.systemProp)
        }

        enum class Buckets(val gradleProp: String, val systemProp: String) {
            BRANDS("legacylauncher.deploy.bucket.brands", "LL_S3_DEPLOY_BUCKET_BRANDS"),
            LIBS("legacylauncher.deploy.bucket.libs", "LL_S3_DEPLOY_BUCKET_LIBS"),
            UPDATES("legacylauncher.deploy.bucket.updates", "LL_S3_DEPLOY_BUCKET_UPDATES"),
        }
    }
}
