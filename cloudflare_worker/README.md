# Vern TTS Autonomous Telegram Bot (Serverless Webhook)

This folder contains the serverless edge worker for `@VernTTS_bot`. It answers users 24/7 with zero hosting costs, 0ms latency, and no servers to manage.

---

## 🚀 3-Minute Deployment via Cloudflare Workers (Free)

1. Go to [dash.cloudflare.com](https://dash.cloudflare.com/) and create a free account if you don't have one.
2. In the sidebar, click **Compute (Workers & Pages)** -> **Create application** -> **Create Worker**.
3. Name your worker (e.g. `vern-telegram-bot`) and click **Deploy**.
4. Click **Edit code**, delete the default code, and paste the entire contents of `worker.js`.
5. Click **Deploy** (top right).
6. Copy your worker URL (e.g. `https://vern-telegram-bot.<your-subdomain>.workers.dev`).

---

## 🔗 Connect Webhook to Telegram

Once your worker is deployed, link it to your Telegram Bot by opening this URL in any browser (replace `<YOUR_WORKER_URL>` with your Cloudflare URL):

```
https://api.telegram.org/bot8285832720:AAHM20ABnUrB1VzLM81eUgkO6NdXnb7Je-o/setWebhook?url=https://<YOUR_WORKER_URL>
```

Telegram will respond:
```json
{"ok":true,"result":true,"description":"Webhook was set"}
```

🎉 That's it! Your bot is now operating autonomously 24/7 worldwide with 0ms cold starts.

---

## 💻 Alternative: Running Locally or on a VPS

If you prefer not to use Cloudflare Workers, you can simply run:

```bash
python scripts/bot_polling.py
```

The script will clear any webhook and run long-polling continuously to reply to incoming messages.
