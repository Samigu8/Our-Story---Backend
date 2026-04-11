package com.example.resource;

/*
 * Provides presigned S3 upload URLs for timeline and memory image uploads.
 */

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Path("/uploads")
public class S3UploadResource {

    private static final Set<String> ALLOWED_FOLDERS = Set.of("timeline", "memories");

    @ConfigProperty(name = "app.s3.bucket")
    Optional<String> bucketName;

    @ConfigProperty(name = "app.s3.region")
    String region;

    @ConfigProperty(name = "app.s3.public-base-url")
    Optional<String> publicBaseUrl;

    public static class PresignRequest {
        public String fileName;
        public String contentType;
        public String folder;
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", message))
                .build();
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "image.jpg";
        }

        String sanitized = fileName.trim().replaceAll("[^A-Za-z0-9._-]", "-");
        return sanitized.isEmpty() ? "image.jpg" : sanitized;
    }

    private String buildPublicUrl(String key) {
        String resolvedBucket = bucketName.orElse("").trim();
        String resolvedPublicBaseUrl = publicBaseUrl.orElse("").trim();

        if (!resolvedPublicBaseUrl.isBlank()) {
            return resolvedPublicBaseUrl.endsWith("/")
                    ? resolvedPublicBaseUrl + key
                    : resolvedPublicBaseUrl + "/" + key;
        }

        return "https://" + resolvedBucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    @POST
    @Path("/presign")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPresignedUpload(PresignRequest request) {
        String resolvedBucket = bucketName.orElse("").trim();
        if (resolvedBucket.isBlank()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "S3 bucket is not configured on the server."))
                    .build();
        }

        if (request == null) {
            return badRequest("Upload details are required.");
        }

        String folder = request.folder == null ? "" : request.folder.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_FOLDERS.contains(folder)) {
            return badRequest("Upload folder must be timeline or memories.");
        }

        String contentType = request.contentType == null ? "" : request.contentType.trim().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            return badRequest("Only image files are allowed.");
        }

        String safeFileName = sanitizeFileName(request.fileName);
        LocalDate today = LocalDate.now();
        String key = folder + "/"
                + today.getYear() + "/"
                + String.format("%02d", today.getMonthValue()) + "/"
                + UUID.randomUUID() + "-" + safeFileName;

        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(resolvedBucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            return Response.ok(Map.of(
                    "uploadUrl", presignedRequest.url().toString(),
                    "fileUrl", buildPublicUrl(key),
                    "key", key,
                    "expiresInSeconds", 600
            )).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Unable to create an upload URL right now."))
                    .build();
        }
    }
}
