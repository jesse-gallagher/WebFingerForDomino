# WebFinger For Domino

This project provides a minimal subset of the [WebFinger specification](https://www.rfc-editor.org/rfc/rfc7033) geared specifically towards allowing for Mastodon lookups of a username based on a Domino server's host name and directory.

## Usage

Install the plugin on your server in your preferred way, such as via an NSF Update Site.

Once it's installed, add several fields to the Person document of any user you would like to make available to this service:

- `WebFingerEnable` should be the text value "1" if the user should be included in WebFinger lookups at all
- `WebFingerAliases` is an optional multi-value text field of URLs to include in the `aliases` property of the response, such as a blog URL
- `WebFingerProfile` should be the main social profile page for the user, such as "https://some.mastodon/@myuser"
- `WebFingerMastodon` should be the text value "1" if the below Mastodon fields should be used to generate Mastodon/ActivityPub links
- `MastodonUsername` should be the base user name of the user on their Mastodon host, such as "myuser"
- `MastodonHost` should be the host name or base URL of the Mastodon instance, such as "some.mastodon" or "https://some.mastodon". When no procol is specified, "https" is assumed

When configured, the user is accessible via a URL like "https://some.domino/.well-known/webfinger?resource=acct:myuser@some.domino". This will emit a payload like:

```json
{
  "subject": "acct:myuser@some.domino",
  "aliases": [
    "https://some.blog"
  ],
  "links": [
    {
      "rel": "http://webfinger.net/rel/profile-page",
      "href": "https://some.mastodon/@myuser"
    },
    {
      "rel": "self",
      "href": "https://some.mastodon/users/myuser",
      "type": "application/activity+json"
    },
    {
      "template": "https://some.mastodon/authorize_interaction?uri={uri}",
      "rel": "http://ostatus.org/schema/1.0/subscribe"
    }
  ]
}
```

## Requirements

- Domino 9.0.1FP10 (tested only on 12.0.2)

## License

This project is licensed under the Apache License 2.0.
