import { ref } from "vue";
import type { ListDataItem, QueryDataDetailItemGroup, TableColumnInfo } from "./api";

export interface ChildCandidateState {
  keyword: string;
  pageIndex: number;
  pageSize: number;
  queried: boolean;
  totalItem: number;
  totalPage: number;
}

const defaultState: ChildCandidateState = {
  keyword: "",
  pageIndex: 1,
  pageSize: 10,
  queried: false,
  totalItem: 0,
  totalPage: 0
};

export function useChildCandidates(groupKey: (group: QueryDataDetailItemGroup) => string) {
  const rows = ref<Record<string, ListDataItem[]>>({});
  const columns = ref<Record<string, TableColumnInfo[]>>({});
  const state = ref<Record<string, ChildCandidateState>>({});

  function candidateState(group: QueryDataDetailItemGroup): ChildCandidateState {
    return state.value[groupKey(group)] || defaultState;
  }

  function setCandidateState(group: QueryDataDetailItemGroup, patch: Partial<ChildCandidateState>) {
    const key = groupKey(group);
    state.value = {
      ...state.value,
      [key]: {
        ...candidateState(group),
        ...patch
      }
    };
  }

  function setCandidateResults(
    group: QueryDataDetailItemGroup,
    nextColumns: TableColumnInfo[],
    nextRows: ListDataItem[],
    totals: Pick<ChildCandidateState, "totalItem" | "totalPage">
  ) {
    const key = groupKey(group);
    columns.value = {
      ...columns.value,
      [key]: nextColumns
    };
    rows.value = {
      ...rows.value,
      [key]: nextRows
    };
    setCandidateState(group, { ...totals, queried: true });
  }

  function setCandidateView(group: QueryDataDetailItemGroup, nextColumns: TableColumnInfo[]) {
    const key = groupKey(group);
    columns.value = { ...columns.value, [key]: nextColumns };
    rows.value = { ...rows.value, [key]: [] };
    setCandidateState(group, { pageIndex: 1, queried: false, totalItem: 0, totalPage: 0 });
  }

  function inputValue(event: Event) {
    const target = event.target as { value?: unknown } | null;
    return typeof target?.value === "string" ? target.value : "";
  }

  function updateCandidateKeyword(group: QueryDataDetailItemGroup, event: Event) {
    setCandidateState(group, { keyword: inputValue(event), pageIndex: 1 });
  }

  return {
    candidateColumns: (group: QueryDataDetailItemGroup) => columns.value[groupKey(group)] || [],
    candidateRows: (group: QueryDataDetailItemGroup) => rows.value[groupKey(group)] || [],
    candidateState,
    setCandidateResults,
    setCandidateState,
    setCandidateView,
    updateCandidateKeyword
  };
}
