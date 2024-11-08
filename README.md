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

### PGP Keys

Additionally, you can specify a PGP key in your user document and have it included in your WebFinger profile. To do that, specify these items:

- `pgpPublicKey` should contain the full PGP public key, including the "BEGIN" and "END" lines
- `pgpFingerprint` can optionally contain the shorthand fingerprint of your key
- `pgpAlgorithm` can optionally contain the algorithm used for your key
- `WebFingerPGP` should be set to "1" to include the entry in your profile

Additionally, when a PGP key is present, the key will be available at a URL like "https://some.domino/.webfinger-pgp-key/myuser@some.domino".

### Extensibility

The output of the profile JSON can be extended by registering an IBM-Commons-compatible extension class implementing `org.openntf.webfinger.ext.WebFingerContributor`. These contributor classes can request additional items to read from the directory and then append values to the result JSON to be emitted to the client.

## Requirements

- Domino 14 or above

## License

This project is licensed under the Apache License 2.0.
