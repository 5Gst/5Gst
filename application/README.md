# SpeedtestApplication

## Development setup

### Project download

Make sure to clone git submodules of the repository

```bash
git clone --recurse-submodules https://github.com/5Gst/5Gst.git
```

Or initialize them after the clone

```bash
git clone https://github.com/5Gst/5Gst.git
git submodule init
git submodule update
```

### Local configuration

Make sure that `local.properties` contains `sdk.dir` property set to your Android SDK absolute path.

## Generated code changes

Any commits in [balancer api](./balancerApi) and [service api](./serviceApi) are overwritten by GitHub Actions swagger-codegen workflow.
If you need to apply some changes in generated code, please update the [balancer api patch file](./.swagger-codegen-config/balancerApi.patch) or [service api patch file](./.swagger-codegen-config/serviceApi.patch).

## Build

You can build debug apk with Gradle by executing `./gradlew assembleDebug` on Linux and `gradlew assembleDebug` on Windows.
