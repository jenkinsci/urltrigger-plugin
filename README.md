# URL Trigger Plugin

Polls a URL and triggers a build when the content changes. Part of the xtrigger plugin suite.

## Supported Protocols

Currently only `http` and `ftp` are supported

## Supported Content Types

Plain text is supported, but the following have support for parsed content. For example the textual content can change, but if the keys and values still are identical a build will not be triggered.

* XML
* JSON

## Example in Json

This http reponse body will not trigger a build
```js
// initial value
{"status":404}
// new value
{
    "status" : 404
}
```

This reposne body will trigger a build
```js
// old value
{
    "status" : 404
}
// new value
{
    "status" : 200
}
```
