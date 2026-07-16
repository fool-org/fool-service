#!/usr/bin/env python3
"""Generate the periodic role/permission review from the live Docker database."""

from __future__ import annotations

import argparse
import csv
from datetime import datetime, timezone
import io
import json
from pathlib import Path
import subprocess
import sys


REVIEW_SQL = r"""
SELECT b.BINDING_ID, b.SUBJECT_TYPE, b.SUBJECT_ID, b.EFFECT,
       b.APP_ID, b.DATABASE_ID, b.VALID_UNTIL,
       p.PERMISSION_ID, p.ACTION_ID, p.RESOURCE_TYPE, p.RESOURCE_PATTERN,
       p.MIN_RISK, p.ENABLED
  FROM FOOL_AUTHZ_BINDING b
  LEFT JOIN FOOL_AUTHZ_PERMISSION p ON p.PERMISSION_ID = b.PERMISSION_ID
 ORDER BY b.SUBJECT_TYPE, b.SUBJECT_ID, b.BINDING_ID;
"""


def parse_rows(raw: str) -> list[dict[str, str]]:
    text = raw.strip()
    if not text:
        return []
    return list(csv.DictReader(io.StringIO(text), delimiter="\t"))


def build_report(rows: list[dict[str, str]]) -> dict[str, object]:
    orphaned = [row["BINDING_ID"] for row in rows if not row.get("PERMISSION_ID")]
    disabled = [row["BINDING_ID"] for row in rows if row.get("ENABLED") == "0"]
    wildcard = [row["PERMISSION_ID"] for row in rows
                if row.get("RESOURCE_PATTERN") == "*" or row.get("ACTION_ID") == "*"]
    privileged = [
        {
            "bindingId": row["BINDING_ID"],
            "subject": f'{row["SUBJECT_TYPE"]}:{row["SUBJECT_ID"]}',
            "action": row.get("ACTION_ID", ""),
            "risk": row.get("MIN_RISK", ""),
        }
        for row in rows if row.get("MIN_RISK") in {"HIGH", "CRITICAL"} and row.get("EFFECT") == "ALLOW"
    ]
    status = "FAIL" if orphaned or disabled else "PASS"
    return {
        "status": status,
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "bindingCount": len(rows),
        "findings": {
            "orphanedBindings": orphaned,
            "disabledPermissionBindings": disabled,
            "wildcardPermissions": sorted(set(filter(None, wildcard))),
            "privilegedBindings": privileged,
        },
    }


def live_rows() -> list[dict[str, str]]:
    completed = subprocess.run(
        ["docker", "compose", "exec", "-T", "mysql", "mysql", "-uroot", "-pPa88word",
         "--batch", "--raw", "car_wash", "-e", REVIEW_SQL],
        check=True,
        text=True,
        capture_output=True,
    )
    return parse_rows(completed.stdout)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path)
    parser.add_argument("--strict", action="store_true")
    args = parser.parse_args()
    try:
        report = build_report(live_rows())
    except (OSError, subprocess.CalledProcessError) as exc:
        print(f"authorization review failed: {exc}", file=sys.stderr)
        return 2
    rendered = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered, encoding="utf-8")
    print(rendered, end="")
    return 1 if args.strict and report["status"] != "PASS" else 0


if __name__ == "__main__":
    raise SystemExit(main())
