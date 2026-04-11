# cs320-samuelgutierrez-backend

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/cs320-samuelgutierrez-backend-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- RESTEasy Classic ([guide](https://quarkus.io/guides/resteasy)): REST endpoint framework implementing Jakarta REST and more

## SAM Deploy With S3 Image Storage

This repository now includes a root SAM template at `template.yaml` that provisions:

- The Quarkus Lambda function
- An S3 bucket for Timeline and Memories image uploads
- Bucket CORS rules for browser uploads using presigned URLs
- IAM permissions for generating signed image upload requests

Build first:

```powershell
./mvnw clean package -DskipTests
```

Deploy with guided mode:

```powershell
sam deploy --guided -t template.yaml
```

After the first guided deploy, you can reuse the saved config:

```powershell
sam deploy
```

Set these environment variables for local development:

- `AWS_REGION`
- `S3_BUCKET_NAME`
- `S3_PUBLIC_BASE_URL` (optional; defaults to standard S3 URL pattern)

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
