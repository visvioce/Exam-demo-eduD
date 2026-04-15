import pdfplumber

with pdfplumber.open("biye/working/你好改一下这个.pdf") as pdf:
    full_text = ""
    for page in pdf.pages:
        full_text += page.extract_text() + "\n\n"

print(full_text)

with open("extracted_pdf.md", "w", encoding="utf-8") as f:
    f.write(full_text)
