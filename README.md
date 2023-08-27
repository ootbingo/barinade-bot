# BingoBot

## Development

To run the Bot locally you need Docker with the compose plugin.

### Troubleshooting

#### Tests fail due to problems with Testcontainers

This can be caused by a collision between Docker and a VPN.
[See this Stack Overflow answer](https://stackoverflow.com/a/73949950). In case this doesn't help consider running tests
without VPN enabled.
