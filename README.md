# BingoBot

## Development

To run the Bot locally you need Docker with the compose plugin.

### Running the app locally

Start the development database by running `docker compose up -d`. Then launch the Spring Boot app with the
`dev`-profile. If you wish, you can create a `local`-profile, which will not be pushed to GitHub.

### Troubleshooting

#### DB connections or tests fail while VPN is active

This can be caused by a collision between Docker and a VPN.
[See this Stack Overflow answer](https://stackoverflow.com/a/73949950).

If there is no collision you might need to consult your VPN's documentation on how to bypass it for Docker IPs
(default: `172.17.0.0/16`). You might find the information when looking for "split tunneling".
