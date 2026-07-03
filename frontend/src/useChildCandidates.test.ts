import { describe, expect, it } from "vitest";
import { useChildCandidates } from "./useChildCandidates";

describe("useChildCandidates", () => {
  it("keeps candidate state keyed by child group metadata", () => {
    const candidates = useChildCandidates((group) => group.prpId || "items");
    const group = { prpId: "lines" };

    candidates.updateCandidateKeyword(group, { target: { value: " Ada " } } as unknown as Event);
    candidates.updateCandidatePage(group, { target: { value: "3" } } as unknown as Event);
    candidates.updateCandidatePageSize(group, { target: { value: "25" } } as unknown as Event);

    expect(candidates.candidateState(group)).toMatchObject({
      keyword: " Ada ",
      pageIndex: 1,
      pageSize: 25
    });
  });

  it("stores candidate rows, columns, and totals without business DTO assumptions", () => {
    const candidates = useChildCandidates((group) => group.name || "items");
    const group = { name: "attachments" };

    candidates.setCandidateResults(
      group,
      [{ property: "fileName", title: "File Name" }],
      [{ id: "1", values: { fileName: "contract.pdf" } }],
      { totalItem: 1, totalPage: 1 }
    );

    expect(candidates.candidateColumns(group)).toEqual([{ property: "fileName", title: "File Name" }]);
    expect(candidates.candidateRows(group)).toEqual([{ id: "1", values: { fileName: "contract.pdf" } }]);
    expect(candidates.candidateState(group)).toMatchObject({ totalItem: 1, totalPage: 1 });
  });
});
