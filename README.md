# web2pdf

`web2pdf` is a lightweight REST API service that allows you to generate PDF files from web pages using Chromium. The service is implemented in Kotlin and provides both GET and POST endpoints, enabling flexible usage for different scenarios.

## Features

- **Chromium-based PDF generation:** Leverages Chromium's print-to-PDF functionality to ensure high-quality PDF output.
- **GET Endpoint:** Easily configure your print settings via a URL, which can be shared or called directly in the browser to generate a PDF.
- **POST Endpoint:** Designed for programmatic access, allowing more complex API interactions.

## Installation

The easiest way to get `web2pdf` up and running is by using the provided Docker image.

### Docker

1. Pull the Docker image:
   ```bash
   docker pull goquati/web2pdf:latest
   ```

2. Run the container:
   ```bash
   docker run -d -p 8080:8080 goquati/web2pdf:latest
   ```

This will start the `web2pdf` service on port `8080` of your local machine.

## Usage

### GET Endpoint

The GET endpoint allows you to generate a PDF by simply calling a URL. This is useful for quick, one-off conversions or sharing with others.

**Endpoint:** `/web2pdf`

**Example:**

```bash
GET /web2pdf?url=http://example.com&header[Authorization]=my-token&cookie[my-cookie]=my-cookie-value&options.landscape=false&options.printBackground=true
```

**Query Parameters:**

- `url`: (Required) The URL of the webpage you want to convert to PDF.
- `header`: (Optional) HTTP headers to include in the request.
- `cookie`: (Optional) Cookies to include in the request.
- `options`: (Optional) PDF print options such as `landscape`, `printBackground`, `scale`, `paperWidth`, `paperHeight`, etc.

**Response:**

- **200 OK**: Returns the generated PDF.
- **400 Bad Request**: Returns an error response if the request is invalid.

### POST Endpoint

The POST endpoint is ideal for more API-like usage, where you need to pass more complex data structures.

**Endpoint:** `/web2pdf`

**Example:**

```bash
POST /web2pdf
Content-Type: application/json

{
  "url": "http://example.com",
  "headers": {
    "Authorization": "my-token"
  },
  "cookies": {
    "my-cookie": "my-cookie-value"
  },
  "options": {
    "landscape": false,
    "printBackground": true
  }
}
```

**Request Body:**

- `url`: (Required) The URL of the webpage you want to convert to PDF.
- `headers`: (Optional) HTTP headers to include in the request.
- `cookies`: (Optional) Cookies to include in the request.
- `options`: (Optional) PDF print options, similar to the GET endpoint.

**Response:**

- **200 OK**: Returns the generated PDF.
- **400 Bad Request**: Returns an error response if the request is invalid.

## OpenAPI Specification

The full OpenAPI specification for the `web2pdf` service is available in the repository at [src/main/resources/oas.yaml](./src/main/resources/oas.yaml). This specification provides detailed information about the endpoints, request/response formats, and available parameters.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

With `web2pdf`, you can easily convert web pages to PDFs with just a URL or a simple API call. Whether you're looking to automate the process or need quick access to a PDF version of a page, `web2pdf` has you covered.