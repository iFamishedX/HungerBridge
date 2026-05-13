# HungerBridge

HungerBridge is a unified **Fabric + Paper/Purpur** backend used by
**HungerLib** to execute commands and write logs inside a Minecraft server
without using RCON.

It exposes a small, secure HTTP API:

- `POST /run` — execute a command as console (with optional silent mode)
- `POST /log` — write raw text to the server console
- `GET /ping` — health check

HungerBridge works identically on:

- **Paper/Purpur 1.21.11**
- **Fabric 1.21.11**

---

## Configuration

Generated automatically on first run.

```yaml
port: 1913

enabled_endpoints:
  run: true
  log: true
  ping: true

auth:
  enabled: true
  key: "CHANGE_ME"