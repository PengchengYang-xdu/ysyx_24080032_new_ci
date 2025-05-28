#!/bin/bash

# 传入参数：RESULT_DIR DESIGN
RESULT_DIR=$1
DESIGN=$2
PRJ_PATH=$3

RPT_FILE="${RESULT_DIR}/${DESIGN}.rpt"
STAT_FILE="${RESULT_DIR}/synth_stat.txt"
OUTPUT_FILE="${PRJ_PATH}/record/${DESIGN}_report.md"

# 清空文件或新建
> "$OUTPUT_FILE"

# 截取 rpt 文件前11行写入 md 文件
if [ -f "$RPT_FILE" ]; then
  echo "### Report: ${DESIGN}.rpt (first 11 lines)" >> "$OUTPUT_FILE"
  head -n 11 "$RPT_FILE" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"
else
  echo "Error: Report file not found: $RPT_FILE" >&2
fi

# 查找 synth_stat.txt 中包含 'Chip area for module' 的行，追加写入 md 文件
if [ -f "$STAT_FILE" ]; then
  echo "### Chip area info from synth_stat.txt" >> "$OUTPUT_FILE"
  grep "Chip area for" "$STAT_FILE" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"
else
  echo "Error: synth_stat.txt not found: $STAT_FILE" >&2
fi

echo "Report generated: $OUTPUT_FILE"
