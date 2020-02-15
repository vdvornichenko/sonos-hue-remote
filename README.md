# sonos-hue-remote
Integration with Philips Hue Remote and Sonos ecosystem (control Sonos speakers with Hue Remote).

How to use:

1) Generate Developer Api Key using instructions on Hue website (https://developers.meethue.com/develop/get-started-2/)
2) Find the local Hue bridge ip
3) Populate bridgeIp and the apiKey with such values
4) Pass program arguments when laucnhing the code (e.g. 'Bathroom - Sonos:Bathroom';'Master Bathroom - Sonos:Master Bathroom','Living Room - Sonos:Living Room (LF,RF)')
5) Philips Remote button clicks will send commands to Sonos devices.

This code can be packed in a jar package and run as a daemon on local server, NAS, rasperry PI etc.
