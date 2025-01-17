# Web2PDF SDK

**Web2PDF SDK** is a Kotlin library for converting HTML, Markdown, and web content to PDF files with customizable print options.

## Features

- **HTML to PDF**: Convert HTML strings into PDFs.
- **Markdown to PDF**: Transform Markdown into polished PDFs.
- **Web Content to PDF**: Capture and convert structured web data.
- **Custom Print Options**: Easily configure PDF settings like margins, headers, and footers.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.goquati:web2pdf:$VERSION")
}
```

## Usage

start service

```bash
docker run -p 8080:8080 goquati/web2pdf:latest
```

### HTML to PDF

```kotlin
import io.github.goquati.web2pdf.dsl.*
import io.github.goquati.web2pdf.apis.Web2PdfApi

val api = Web2PdfApi(baseUrl = "http://localhost:8080", client = yourKtorHttpClient)

val response = api.convertHtml2Pdf("<html><body><h1>Hello, PDF!</h1></body></html>") {
    // optional printing options:
    // landscape = ...
    // displayHeaderFooter = ...
    // printBackground = ...
    // scale = ...
    // paperWidth = ...
    // paperHeight = ...
    // marginTop = ...
    // marginBottom = ...
    // marginLeft = ...
    // marginRight = ...
    // pageRanges = ...
    // headerTemplate = ...
    // footerTemplate = ...
    // preferCSSPageSize = ...
    // generateTaggedPDF = ...
    // generateDocumentOutline = ...
}
```

### Markdown to PDF

```kotlin
val response = api.convertMarkdown2Pdf("# Markdown Example") {
    // optional printing options
}
```

### Web Content to PDF

```kotlin
val response = api.convertWeb2Pdf(Web2Pdf(url = "https://example.com")) {
    // optional printing options
}
```

## License

This project is licensed under the [MIT License](LICENSE).
