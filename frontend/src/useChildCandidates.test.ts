import { describe, expect, it } from "vitest";
import { candidateRecordInfo, useChildCandidates } from "./useChildCandidates";

describe("useChildCandidates", () => {
  it("matches the legacy candidate record-count feedback", () => {
    expect(candidateRecordInfo({ queried: false, totalItem: 0 })).toBe("记录未知 请查询");
    expect(candidateRecordInfo({ queried: true, totalItem: 0 })).toBe("共0条记录");
    expect(candidateRecordInfo({ queried: true, totalItem: 12 })).toBe("共12条记录");
  });

  it("keeps candidate state keyed by child group metadata", () => {
    const candidates = useChildCandidates((group) => group.prpId || "items");
    const group = { prpId: "lines" };

    candidates.setCandidateState(group, { pageIndex: 3 });
    candidates.updateCandidateKeyword(group, { target: { value: " Ada " } } as unknown as Event);

    expect(candidates.candidateState(group)).toMatchObject({
      keyword: " Ada ",
      pageIndex: 1,
      pageSize: 10,
      queried: false
    });
  });

  it("stores candidate rows, columns, and totals without business DTO assumptions", () => {
    const candidates = useChildCandidates((group) => group.name || "items");
    const group = { name: "attachments" };

    candidates.setCandidateView(group, [{ property: "old", title: "Old" }]);
    expect(candidates.candidateState(group).queried).toBe(false);

    candidates.setCandidateResults(
      group,
      [{ property: "fileName", title: "File Name" }],
      [{ id: "1", items: [{ prpId: "fileName", fmtValue: "contract.pdf" }] }],
      { totalItem: 1, totalPage: 1 }
    );

    expect(candidates.candidateColumns(group)).toEqual([{ property: "fileName", title: "File Name" }]);
    expect(candidates.candidateRows(group)).toEqual([{ id: "1", items: [{ prpId: "fileName", fmtValue: "contract.pdf" }] }]);
    expect(candidates.candidateState(group)).toMatchObject({ queried: true, totalItem: 1, totalPage: 1 });
  });
});
