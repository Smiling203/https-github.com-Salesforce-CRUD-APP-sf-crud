# Salesforce CRUD Application

A Spring Boot web application for creating, viewing, editing, and deleting Salesforce records through the Salesforce REST API.

## Features

- OAuth 2.0 login with Salesforce
- PKCE support for secure authorization-code login
- CRUD operations for Accounts, Opportunities, Leads, Contacts, and Cases
- Infinite-scroll record listing
- Create and edit forms generated from object fields
- Clear Salesforce API error messages in the UI

## Tech stack

- Java 17
- Spring Boot
- Spring Web MVC and WebFlux `WebClient`
- HTML, Bootstrap, and JavaScript
- Salesforce REST API

## Prerequisites

- Java 17 or later
- A Salesforce Developer Edition, Trailhead Playground, or Salesforce org
- A Salesforce External Client App configured for OAuth

## Salesforce configuration

1. In Salesforce, open **Setup**.
2. Search for **External Client Apps**, then create a new External Client App.
3. Enable OAuth and the Authorization Code flow.
4. Set the callback URL to:

   ```text
   http://localhost:8080/oauth/callback
   ```

5. Add the OAuth scope **Manage user data via APIs (`api`)**.
6. If PKCE is required, leave it enabled; this project supports PKCE using `S256`.
7. Copy the app's Consumer Key and Consumer Secret.

## Configure credentials

Never add Salesforce credentials to `application.properties` or commit them to GitHub.

Set the credentials as environment variables before starting the application.

### PowerShell

```powershell
$env:SF_CLIENT_ID = "your_salesforce_consumer_key"
$env:SF_CLIENT_SECRET = "your_salesforce_consumer_secret"
```

The default callback URL is `http://localhost:8080/oauth/callback`. To use a different URL, set `SF_REDIRECT_URI` and update the callback URL in Salesforce to match it exactly.

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

Or run `SfCrudApplication` from your IDE.

Open the application at:

```text
http://localhost:8080/index.html
```

Click **Login to Salesforce**, authorize the application, select an object, and use the table to create, edit, or delete records.

## API endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/oauth/login` | Starts Salesforce OAuth login |
| `GET` | `/oauth/status` | Returns login status |
| `GET` | `/api/{objectName}/fields` | Returns supported fields |
| `GET` | `/api/{objectName}?limit=20&offset=0` | Lists records |
| `POST` | `/api/{objectName}` | Creates a record |
| `PATCH` | `/api/{objectName}/{id}` | Updates a record |
| `DELETE` | `/api/{objectName}/{id}` | Deletes a record |

## Security notes

- OAuth credentials are loaded only from environment variables.
- Do not commit Consumer Keys, Consumer Secrets, access tokens, or screenshots containing credentials.
- Rotate a Salesforce Consumer Secret immediately if it is exposed.
