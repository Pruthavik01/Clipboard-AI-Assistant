
# 📋 Clipboard AI Assistant

A real-time desktop clipboard monitoring tool built in Java that provides smart responses using Google Gemini AI. It also supports OCR (Optical Character Recognition) for any images copied to the clipboard.
(mainly used for mcq and online tests)
---

## 🧩 Features

- ✅ **Monitors system clipboard** in real-time for text or image content.
- 🧠 **Integrates Google Gemini 1.5 Flash** for intelligent responses.
- 🖼️ **Extracts text from images** using Tesseract OCR (via Tess4J).
- 🔔 **Shows popup system notifications** with AI-generated content.
- 🚫 **Avoids duplicate responses** for the same copied content.

---

## 🛠️ Tech Stack

- Java 20
- [Tess4J](https://github.com/nguyenq/tess4j)
- [OkHttp](https://square.github.io/okhttp/)
- [Google Gemini AI API](https://ai.google.dev/)
- JSON (Gson, org.json)
- Maven (with Shade plugin for fat JAR)

---

## 🚀 How to Run

### 1. Prerequisites

- Java 20 installed
- Maven installed
- Tesseract OCR installed on your system

You can install Tesseract OCR and get language data files here:
👉 [Tesseract GitHub](https://github.com/tesseract-ocr/tessdata)

Set `tessdata` path in code:
```java
tesseract.setDatapath("D:/OCR/tessdata");  // Adjust path as per your system
