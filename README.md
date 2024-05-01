# Network Caller Library

![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)

## Description

Welcome to the `network-caller` library, an efficient tool designed to simplify and streamline network requests in Android applications. Leveraging the power of Retrofit, this library offers a clean and versatile networking layer for your projects.

The `network-caller` library is an open-source project, which means we welcome contributions and enhancements from the community. I appreciate the involvement of any developer interested in improving and expanding this tool. :)

### Key Features

**Networking Class:** Centralizes network operations, supporting common HTTP methods such as GET, POST, PUT, PATCH, and DELETE.

**DataRequest Builder:** Offers a fluent API for constructing network request with flexible options, including custom HTTP methods, request bodies, headers, and OkHttpClient customization.

**NetworkCaller & NetworkingService Interfaces:** Abstractions to simplify the integration of the library into your Android project.

**Request Functions:** Convenient functions (`request` and `safeRequest`) for making network requests and handling responses with ease. Includes support for specifying HTTP methods, request bodies, headers, and custom OkHttpClient instances.

**Error Handling:** Comprehensive error handling for various scenarios, such as HTTP status code validation, null response bodies, and exceptions during network operations.

## Installation

To integrate `network-caller` into your Android project, follow these steps:

#### Step 1: Add JitPack Repository

Add JitPack as a repository in your project's `settings.gradle` file. Open the file and make sure it includes the following lines:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

#### Step 2: Add Networking Caller Dependency

Add the `network-caller` dependency to your app's `build.gradle` file within the dependencies block:

```gradle
dependencies {
    // Other dependencies...

    implementation 'com.github.98kmc:network-caller:$networkCallerVersion'
}
```

#### Step 3: Sync and Build

Sync your project with Gradle to ensure that the new dependency is recognized. You can do this by clicking on "Sync Now" in the bar that appears at the top of Android Studio.

Now, you're ready to use the `network-caller` library in your Android project.

For the latest version, please check the [releases page](https://github.com/98kmc/network-caller/releases).

### Requirements
To use `network-caller` with Hilt, you need to [configure Hilt](https://developer.android.com/training/dependency-injection/hilt-android) in your project. Before using the `network-caller` library, ensure that the following dependencies are added to your project:

```gradle
dependencies {
    // Other dependencies...

    // Hilt
    implementation "com.google.dagger:hilt-android:$hiltVersion"
    kapt "com.google.dagger:hilt-android-compiler:$hiltVersion"

    // OkHttp
    implementation "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
}
```

## Usage

Using the `network-caller` library is straightforward. First, you need to provide two essential components:

1. **Base URL:**
   
   A base URL of type `URL`. This serves as the foundation for all network requests.

3. **OkHttpClient Instance:**
   
   An instance of `OkHttpClient` with the configurations you deem necessary. This provides the flexibility to customize the client, such as adding JWT authentication.

Here's an example using Dagger Hilt to provide these components:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @NetworkingBaseUrl
    @Singleton
    @Provides
    fun provideBaseUrl(): URL = URL("/")  // Your Url here!

    @NetworkingOkHttp
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
```
then follow these steps to make network requests with ease:

1. **Create a Model:**
   Create a data class representing the data type you want to retrieve:

   ```kotlin
   data class AnyDataType(
       val id: Int?,
       val title: String?
   )
   ```
   
1. **Implement NetworkCaller Interface:**
Implement the `NetworkCaller` interface, which gives you access to the `networkingService(context:)` function. This function requires a context and provides a `NetworkService` object capable of making queries to any URL and returning any data type.

```kotlin
   class DataSource @Inject constructor(
    @ApplicationContext private val context: Context
   ) : NetworkCaller {

        private val service = networkingService(context)

        suspend fun fetchPost(): PostList? = service.request(endpoint = "posts/").execute()

        suspend fun createPost(): Result<AnyDataType> {
    
            val request = service.safeRequest(endpoint = "https://jsonplaceholder.typicode.com/posts/")
                .withMethod(HttpMethod.POST)
                .withBody(
                    mapOf(
                        "title" to "foo",
                        "body" to "bar",
                        "userId" to "1"
                    )
                )
                .withHeaders(
                    mapOf(
                        "Content-type" to "application/json; charset=UTF-8"
                    )
                )

            return request.execute()
        }
  }
   ```
Note: On this functions there is no difference between using the full URL or just the endpoint. If only the endpoint is provided, the `@NetworkingBaseUrl` is used to complete the URL.

Now, you can use this DataSource class to fetch and create data:

```kotlin
   @AndroidEntryPoint
   class MainActivity : ComponentActivity() {

     @Inject
     lateinit var dataSource: DataSource

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContent {
             Text(text = "Hello!")

             LaunchedEffect(Unit) {

                 Log.d(NETWORKING, "onCreate: ${dataSource.fetchPost()}")
                 Log.d(NETWORKING, "onCreate: ${dataSource.createPost()}")
             }
         }
     }
  }
   ```
