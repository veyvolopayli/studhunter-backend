#!/bin/bash

OUTPUT_FILE="project_code.txt"

[ -f "$OUTPUT_FILE" ] && rm "$OUTPUT_FILE"

find . \( -name "*.kt" -o -name "*.conf" -o -name "*.env" -o -name "*.gradle.kts" \) -exec cat {} + >> "$OUTPUT_FILE"

if [ -f "$OUTPUT_FILE" ]; then
    echo "Все файлы были успешно объединены в $OUTPUT_FILE"
else
    echo "Не удалось создать файл $OUTPUT_FILE"
    exit 1
fi