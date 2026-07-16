#!/usr/bin/env python3

import unittest

from authorization_review import build_report, parse_rows


class AuthorizationReviewTest(unittest.TestCase):
    def test_flags_orphaned_binding_and_lists_privileged_grants(self):
        rows = parse_rows("""BINDING_ID\tSUBJECT_TYPE\tSUBJECT_ID\tEFFECT\tAPP_ID\tDATABASE_ID\tVALID_UNTIL\tPERMISSION_ID\tACTION_ID\tRESOURCE_TYPE\tRESOURCE_PATTERN\tMIN_RISK\tENABLED
ok\tROLE\tauth:9001\tALLOW\tapp\tdb\tNULL\tapprove\taction.approve\tActionRequest\taction:*\tHIGH\t1
bad\tUSER\tu1\tALLOW\tapp\tdb\tNULL\t\t\t\t\t\tNULL
""")

        report = build_report(rows)

        self.assertEqual("FAIL", report["status"])
        self.assertEqual(["bad"], report["findings"]["orphanedBindings"])
        self.assertEqual("action.approve", report["findings"]["privilegedBindings"][0]["action"])


if __name__ == "__main__":
    unittest.main()
